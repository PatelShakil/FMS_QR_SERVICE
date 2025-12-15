package com.shakilpatel.qr.ejb;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@Singleton
@Startup
public class KafkaProducerEJB {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerEJB.class);

    private KafkaProducer<String, String> producer;
    private boolean kafkaEnabled = true;

    // Kafka Topics
    public static final String TOPIC_QR_GENERATED = "qr.generated";
    public static final String TOPIC_QR_ASSIGNED = "qr.assigned";
    public static final String TOPIC_QR_ACCEPTED = "qr.accepted";
    public static final String TOPIC_QR_REJECTED = "qr.rejected";
    public static final String TOPIC_QR_DEPLOYMENT_SUBMITTED = "qr.deployment.submitted";
    public static final String TOPIC_QR_DEPLOYED = "qr.deployed";
    public static final String TOPIC_QR_DEPLOYMENT_REJECTED = "qr.deployment.rejected";
    public static final String TOPIC_QR_SCANNED = "qr.scanned";
    public static final String TOPIC_QR_ACTIVATED = "qr.activated";
    public static final String TOPIC_QR_DEACTIVATED = "qr.deactivated";

    @PostConstruct
    public void init() {
        try {
            String kafkaBootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
            if (kafkaBootstrapServers == null || kafkaBootstrapServers.isEmpty()) {
                kafkaBootstrapServers = "localhost:9092";
            }

            String kafkaEnabledEnv = System.getenv("KAFKA_ENABLED");
            if ("false".equalsIgnoreCase(kafkaEnabledEnv)) {
                kafkaEnabled = false;
                logger.warn("⚠️ Kafka is DISABLED via environment variable");
                return;
            }

            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "1");
            props.put(ProducerConfig.RETRIES_CONFIG, 3);
            props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
            props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);

            this.producer = new KafkaProducer<>(props);
            logger.info("✅ Kafka Producer initialized: {}", kafkaBootstrapServers);
        } catch (Exception e) {
            logger.error("❌ Failed to initialize Kafka Producer: {}", e.getMessage(), e);
            kafkaEnabled = false;
        }
    }

    @PreDestroy
    public void cleanup() {
        if (producer != null) {
            try {
                producer.close();
                logger.info("✅ Kafka Producer closed");
            } catch (Exception e) {
                logger.error("❌ Error closing Kafka Producer: {}", e.getMessage());
            }
        }
    }

    /**
     * Publish event to Kafka
     */
    public void publishEvent(String topic, String key, JsonObject payload) {
        if (!kafkaEnabled || producer == null) {
            logger.debug("Kafka disabled, skipping event: {}", topic);
            return;
        }

        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, payload.toString());

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    logger.error("❌ Failed to send Kafka event to {}: {}", topic, exception.getMessage());
                } else {
                    logger.debug("✅ Event published to {} [partition: {}, offset: {}]",
                            topic, metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            logger.error("❌ Error publishing event to {}: {}", topic, e.getMessage());
        }
    }

    /**
     * QR Generated Event
     */
    public void publishQRGenerated(Long qrCodeId, String qrCodeSlug, String formId, Long orgId) {
        JsonObject payload = Json.createObjectBuilder()
                .add("qrCodeId", qrCodeId)
                .add("qrCodeSlug", qrCodeSlug)
                .add("formId", formId)
                .add("orgId", orgId)
                .add("timestamp", System.currentTimeMillis())
                .build();

        publishEvent(TOPIC_QR_GENERATED, qrCodeSlug, payload);
    }

    /**
     * QR Assigned Event
     */
    public void publishQRAssigned(Long qrCodeId, String qrCodeSlug, Long distributorId, Long assignedBy,String distributorEmail) {
        JsonObject payload = Json.createObjectBuilder()
                .add("qrCodeId", qrCodeId)
                .add("qrCodeSlug", qrCodeSlug)
                .add("distributorId", distributorId)
                .add("distributorEmail",distributorEmail)
                .add("assignedBy", assignedBy)
                .add("timestamp", System.currentTimeMillis())
                .build();

        publishEvent(TOPIC_QR_ASSIGNED, qrCodeSlug, payload);
    }

    /**
     * QR Accepted Event
     */
    public void publishQRAccepted(Long qrCodeId, String qrCodeSlug, Long distributorId) {
        JsonObject payload = Json.createObjectBuilder()
                .add("qrCodeId", qrCodeId)
                .add("qrCodeSlug", qrCodeSlug)
                .add("distributorId", distributorId)
                .add("timestamp", System.currentTimeMillis())
                .build();

        publishEvent(TOPIC_QR_ACCEPTED, qrCodeSlug, payload);
    }

    /**
     * QR Rejected Event
     */
    public void publishQRRejected(Long qrCodeId, String qrCodeSlug, Long distributorId, String reason) {
        JsonObject payload = Json.createObjectBuilder()
                .add("qrCodeId", qrCodeId)
                .add("qrCodeSlug", qrCodeSlug)
                .add("distributorId", distributorId)
                .add("reason", reason)
                .add("timestamp", System.currentTimeMillis())
                .build();

        publishEvent(TOPIC_QR_REJECTED, qrCodeSlug, payload);
    }

    /**
     * QR Deployment Submitted Event
     */
    public void publishDeploymentSubmitted(Long qrCodeId, String qrCodeSlug, Long distributorId, String location) {
        JsonObject payload = Json.createObjectBuilder()
                .add("qrCodeId", qrCodeId)
                .add("qrCodeSlug", qrCodeSlug)
                .add("distributorId", distributorId)
                .add("location", location != null ? location : "")
                .add("timestamp", System.currentTimeMillis())
                .build();

        publishEvent(TOPIC_QR_DEPLOYMENT_SUBMITTED, qrCodeSlug, payload);
    }

    /**
     * QR Deployed Event (Approved)
     */
    public void publishQRDeployed(Long qrCodeId, String qrCodeSlug, Long distributorId, Long approvedBy) {
        JsonObject payload = Json.createObjectBuilder()
                .add("qrCodeId", qrCodeId)
                .add("qrCodeSlug", qrCodeSlug)
                .add("distributorId", distributorId)
                .add("approvedBy", approvedBy)
                .add("timestamp", System.currentTimeMillis())
                .build();

        publishEvent(TOPIC_QR_DEPLOYED, qrCodeSlug, payload);
    }

    /**
     * QR Deployment Rejected Event
     */
    public void publishDeploymentRejected(Long qrCodeId, String qrCodeSlug, Long rejectedBy, String reason) {
        JsonObject payload = Json.createObjectBuilder()
                .add("qrCodeId", qrCodeId)
                .add("qrCodeSlug", qrCodeSlug)
                .add("rejectedBy", rejectedBy)
                .add("reason", reason)
                .add("timestamp", System.currentTimeMillis())
                .build();

        publishEvent(TOPIC_QR_DEPLOYMENT_REJECTED, qrCodeSlug, payload);
    }

    /**
     * QR Scanned Event
     */
    public void publishQRScanned(Long qrCodeId, String qrCodeSlug, Long scannedBy, String ipAddress) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("qrCodeId", qrCodeId)
                .add("qrCodeSlug", qrCodeSlug)
                .add("ipAddress", ipAddress != null ? ipAddress : "")
                .add("timestamp", System.currentTimeMillis());

        if (scannedBy != null) {
            builder.add("scannedBy", scannedBy);
        }

        publishEvent(TOPIC_QR_SCANNED, qrCodeSlug, builder.build());
    }

    /**
     * QR Activated Event
     */
    public void publishQRActivated(Long qrCodeId, String qrCodeSlug, Long updatedBy) {
        JsonObject payload = Json.createObjectBuilder()
                .add("qrCodeId", qrCodeId)
                .add("qrCodeSlug", qrCodeSlug)
                .add("updatedBy", updatedBy)
                .add("timestamp", System.currentTimeMillis())
                .build();

        publishEvent(TOPIC_QR_ACTIVATED, qrCodeSlug, payload);
    }

    /**
     * QR Deactivated Event
     */
    public void publishQRDeactivated(Long qrCodeId, String qrCodeSlug, Long updatedBy) {
        JsonObject payload = Json.createObjectBuilder()
                .add("qrCodeId", qrCodeId)
                .add("qrCodeSlug", qrCodeSlug)
                .add("updatedBy", updatedBy)
                .add("timestamp", System.currentTimeMillis())
                .build();

        publishEvent(TOPIC_QR_DEACTIVATED, qrCodeSlug, payload);
    }
}
