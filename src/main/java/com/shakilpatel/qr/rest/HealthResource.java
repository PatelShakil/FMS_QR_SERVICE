package com.shakilpatel.qr.rest;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/qr/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    private static final Logger logger = LoggerFactory.getLogger(HealthResource.class);

    @PersistenceContext(unitName = "qrPU")
    private EntityManager em;

    /**
     * Health check endpoint GET /api/health
     */
    @GET
    public Response healthCheck() {
        try {
            // Check database connectivity
            em.createNativeQuery("SELECT 1").getSingleResult();

            long uptime = getUptime();
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long totalMemory = runtime.maxMemory() / (1024 * 1024);

            JsonObject health = Json.createObjectBuilder()
                    .add("service", "qr-service")
                    .add("status", "UP")
                    .add("timestamp", System.currentTimeMillis())
                    .add("uptime", uptime + " seconds")
                    .add("environment", System.getenv().getOrDefault("ENV", "development"))
                    .add("version", "1.0.0")
                    .add("dependencies", Json.createObjectBuilder()
                            .add("database", Json.createObjectBuilder()
                                    .add("status", "UP")
                                    .add("type", "PostgreSQL"))
                            .add("kafka", Json.createObjectBuilder()
                                    .add("status", isKafkaEnabled() ? "ENABLED" : "DISABLED"))
                            .add("minio", Json.createObjectBuilder()
                                    .add("status", isMinioEnabled() ? "ENABLED" : "DISABLED")))
                    .add("memory", Json.createObjectBuilder()
                            .add("used", usedMemory + " MB")
                            .add("total", totalMemory + " MB"))
                    .build();

            return Response.ok(health).build();

        } catch (Exception e) {
            logger.error("❌ Health check failed: {}", e.getMessage());

            JsonObject error = Json.createObjectBuilder()
                    .add("service", "qr-service")
                    .add("status", "DOWN")
                    .add("timestamp", System.currentTimeMillis())
                    .add("error", e.getMessage())
                    .build();

            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(error).build();
        }
    }

    /**
     * Readiness probe (Kubernetes) GET /api/health/ready
     */
    @GET
    @Path("/ready")
    public Response readinessCheck() {
        try {
            // Check database
            em.createNativeQuery("SELECT 1").getSingleResult();

            JsonObject ready = Json.createObjectBuilder()
                    .add("status", "READY")
                    .add("timestamp", System.currentTimeMillis())
                    .build();

            return Response.ok(ready).build();

        } catch (Exception e) {
            logger.error("❌ Readiness check failed: {}", e.getMessage());

            JsonObject notReady = Json.createObjectBuilder()
                    .add("status", "NOT_READY")
                    .add("error", e.getMessage())
                    .build();

            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(notReady).build();
        }
    }

    /**
     * Liveness probe (Kubernetes) GET /api/health/live
     */
    @GET
    @Path("/live")
    public Response livenessCheck() {
        JsonObject alive = Json.createObjectBuilder()
                .add("status", "ALIVE")
                .add("timestamp", System.currentTimeMillis())
                .build();

        return Response.ok(alive).build();
    }

    private long getUptime() {
        return (System.currentTimeMillis() - java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000;
    }

    private boolean isKafkaEnabled() {
        String kafkaEnabled = System.getenv("KAFKA_ENABLED");
        return kafkaEnabled == null || !"false".equalsIgnoreCase(kafkaEnabled);
    }

    private boolean isMinioEnabled() {
        String minioEnabled = System.getenv("MINIO_ENABLED");
        return minioEnabled == null || !"false".equalsIgnoreCase(minioEnabled);
    }
}
