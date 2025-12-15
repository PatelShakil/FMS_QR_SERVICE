package com.shakilpatel.qr.rest;

import com.shakilpatel.qr.dto.ApiResponse;
import com.shakilpatel.qr.ejb.KafkaProducerEJB;
import com.shakilpatel.qr.ejb.QRAnalyticsEJB;
import com.shakilpatel.qr.ejb.QRCodeEJB;
import com.shakilpatel.qr.ejb.QRScanEJB;
import com.shakilpatel.qr.entity.QRCode;
import com.shakilpatel.qr.entity.QRScan;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@Path("/qr")
@Produces(MediaType.APPLICATION_JSON)
public class QRPublicResource {

    private static final Logger logger = LoggerFactory.getLogger(QRPublicResource.class);

    @Inject
    private QRCodeEJB qrCodeEJB;

    @Inject
    private QRScanEJB scanEJB;

    @Inject
    private QRAnalyticsEJB analyticsEJB;

    @Inject
    private KafkaProducerEJB kafkaProducer;

    /**
     * Public QR Code Redirect (Citizen Scan) GET /{qrCodeSlug} Example: GET
     * /QR_ABC123
     */
    @GET
    @Path("/s/{qrCodeSlug}")
    public Response scanQRCode(@PathParam("qrCodeSlug") String qrCodeSlug,
                               @Context HttpServletRequest request) {
        try {
            logger.info("🔍 QR Code scanned: {}", qrCodeSlug);

            // Find QR Code
            QRCode qrCode = qrCodeEJB.findBySlug(qrCodeSlug);
            if (qrCode == null) {
                logger.warn("⚠️ QR Code not found: {}", qrCodeSlug);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"QR Code not found\"}")
                        .build();
            }
            logger.info("QR CODE : {}", qrCode.toString());
            logger.info("QR CODE STATUS : {}", qrCode.getStatus());

            // Check if QR is active
            if (!qrCode.getStatus().equalsIgnoreCase("ACTIVE")) {
                logger.warn("⚠️ QR Code inactive: {}", qrCodeSlug);
                return Response.status(Response.Status.GONE)
                        .entity("{\"error\":\"This QR Code is no longer active\"}")
                        .build();
            }

            // Record scan
            QRScan scan = new QRScan();
            scan.setQrCodeId(qrCode.getQrCodeId());
            scan.setScannedAt(LocalDateTime.now());
            scan.setIpAddress(getClientIpAddress(request));
            scan.setUserAgent(request.getHeader("User-Agent"));
            scan.setDeviceType(detectDeviceType(request.getHeader("User-Agent")));

            scanEJB.recordScan(scan);

            // Increment counters
            qrCodeEJB.incrementScanCount(qrCode.getQrCodeId());
            analyticsEJB.incrementScanCount(qrCode.getQrCodeId(), scan.getDeviceType());

            // Publish Kafka event
            kafkaProducer.publishQRScanned(
                    qrCode.getQrCodeId(),
                    qrCode.getQrCodeSlug(),
                    null, // anonymous scan
                    scan.getIpAddress()
            );

            // Redirect to feedback form
            String redirectUrl = System.getenv("FEEDBACK_SERVICE_URL");
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                redirectUrl = "http://localhost:5174";
            }
            String formUrl = String.format("%s/fill/%s", redirectUrl, qrCode.getFormId());

            logger.info("✅ Redirecting to form: {}", formUrl);

//            return Response.seeOther(URI.create(formUrl)).build();
            return Response.status(Response.Status.OK)
                    .entity(ApiResponse.success(qrCode.getFormId(),"QR Code Scanned Successfully"))
                    .build();

        } catch (Exception e) {
            logger.error("❌ Error processing QR scan: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Failed to process QR scan\"}")
                    .build();
        }
    }

    /**
     * Track scan with additional data (for mobile apps) POST
     * /api/scan/{qrCodeSlug}
     */
    @POST
    @Path("/scan/{qrCodeSlug}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response trackScan(@PathParam("qrCodeSlug") String qrCodeSlug,
                              jakarta.json.JsonObject requestBody,
                              @Context HttpServletRequest request) {
        try {
            QRCode qrCode = qrCodeEJB.findBySlug(qrCodeSlug);
            if (qrCode == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"QR Code not found\"}")
                        .build();
            }

            if (!"ACTIVE".equals(qrCode.getStatus())) {
                return Response.status(Response.Status.GONE)
                        .entity("{\"error\":\"QR Code is inactive\"}")
                        .build();
            }

            // Record scan with location data
            QRScan scan = new QRScan();
            scan.setQrCodeId(qrCode.getQrCodeId());
            scan.setScannedAt(LocalDateTime.now());
            scan.setIpAddress(getClientIpAddress(request));
            scan.setUserAgent(request.getHeader("User-Agent"));
            scan.setDeviceType(requestBody.getString("deviceType", detectDeviceType(request.getHeader("User-Agent"))));

            if (requestBody.containsKey("latitude") && !requestBody.isNull("latitude")) {
                scan.setScanLatitude(requestBody.getJsonNumber("latitude").bigDecimalValue());
            }
            if (requestBody.containsKey("longitude") && !requestBody.isNull("longitude")) {
                scan.setScanLongitude(requestBody.getJsonNumber("longitude").bigDecimalValue());
            }

            scanEJB.recordScan(scan);

            // Increment counters
            qrCodeEJB.incrementScanCount(qrCode.getQrCodeId());
            analyticsEJB.incrementScanCount(qrCode.getQrCodeId(), scan.getDeviceType());

            // Publish Kafka event
            kafkaProducer.publishQRScanned(
                    qrCode.getQrCodeId(),
                    qrCode.getQrCodeSlug(),
                    null,
                    scan.getIpAddress()
            );

            // Return redirect URL
            String redirectUrl = System.getenv("FEEDBACK_SERVICE_URL");
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                redirectUrl = "http://localhost:8088";
            }
            String formUrl = String.format("%s/form/%s", redirectUrl, qrCode.getFormId());

            jakarta.json.JsonObject response = jakarta.json.Json.createObjectBuilder()
                    .add("success", true)
                    .add("redirectUrl", formUrl)
                    .add("formId", qrCode.getFormId())
                    .build();

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("❌ Error tracking scan: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Failed to track scan\"}")
                    .build();
        }
    }

    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Detect device type from user agent
     */
    private String detectDeviceType(String userAgent) {
        if (userAgent == null) {
            return "UNKNOWN";
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "MOBILE";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "TABLET";
        } else {
            return "DESKTOP";
        }
    }
}
