package com.shakilpatel.qr.rest;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.shakilpatel.qr.dto.*;
import com.shakilpatel.qr.ejb.KafkaProducerEJB;
import com.shakilpatel.qr.ejb.QRCodeEJB;
import com.shakilpatel.qr.entity.QRCode;
import com.shakilpatel.qr.util.JWTUtil;
import com.shakilpatel.qr.util.ResponseUtil;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("/qr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QRCodeResource {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeResource.class);

    @Inject
    private QRCodeEJB qrCodeEJB;

    @Inject
    private KafkaProducerEJB kafkaProducer;

    @Inject
    private JWTUtil jwtUtil;

    /**
     * Generate new QR Code POST /api/qr/generate
     */
    @POST
    @Path("/generate")
    public Response generateQRCode(CreateQRRequest request, @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Long userId = jwtUtil.getUserId(jwt);

            // Check if user is ORG_ADMIN
            if (!jwtUtil.isOrgAdmin(jwt)) {
                return ResponseUtil.forbidden("Only organization admins can create QR codes");
            }

            // Validate request
            if (request.getFormId() == null || request.getFormId().trim().isEmpty()) {
                return ResponseUtil.badRequest("Form ID is required");
            }
            if (request.getServiceId() == null) {
                return ResponseUtil.badRequest("Service ID is required");
            }
            if (request.getOrgId() == null) {
                return ResponseUtil.badRequest("Organization ID is required");
            }
            if (request.getQrCodeName() == null || request.getQrCodeName().trim().isEmpty()) {
                return ResponseUtil.badRequest("QR Code name is required");
            }

            // Create QR Code entity
            QRCode qrCode = new QRCode();
            qrCode.setFormId(request.getFormId());
            qrCode.setServiceId(request.getServiceId());
            qrCode.setOrgId(request.getOrgId());
            qrCode.setQrCodeName(request.getQrCodeName());
            qrCode.setPaymentAmount(request.getPaymentAmount());
            qrCode.setCreatedBy(userId);

            // Save to database
            QRCode created = qrCodeEJB.createQRCode(qrCode);

            // Publish Kafka event
            kafkaProducer.publishQRGenerated(
                    created.getQrCodeId(),
                    created.getQrCodeSlug(),
                    created.getFormId(),
                    created.getOrgId()
            );

            // Build response
            JsonObject data = Json.createObjectBuilder()
                    .add("qrCodeId", created.getQrCodeId())
                    .add("qrCodeSlug", created.getQrCodeSlug())
                    .add("qrCodeUrl", created.getQrCodeUrl())
                    .add("qrCodeName", created.getQrCodeName())
                    .add("formId", created.getFormId())
                    .add("paymentAmount", created.getPaymentAmount())
                    .add("status", created.getStatus())
                    .add("createdAt", created.getCreatedAt().toString())
                    .build();

            return ResponseUtil.created(data, "QR Code generated successfully");

        } catch (Exception e) {
            logger.error("❌ Error generating QR Code: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to generate QR Code: " + e.getMessage());
        }
    }

    /**
     * Get QR Code by ID GET /api/qr/{qrCodeId}
     */
    @GET
    @Path("/{qrCodeId}")
    public Response getQRCodeById(@PathParam("qrCodeId") Long qrCodeId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);

            QRCode qrCode = qrCodeEJB.findById(qrCodeId);
            if (qrCode == null) {
                return ResponseUtil.notFound("QR Code not found");
            }

            JsonObject data = buildQRCodeJson(qrCode);
            return ResponseUtil.success(data);

        } catch (Exception e) {
            logger.error("❌ Error fetching QR Code: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to fetch QR Code: " + e.getMessage());
        }
    }

    /**
     * Get all QR Codes by Form ID GET /api/qr/form/{formId}
     */
    @GET
    @Path("/form/{formId}")
    public Response getQRCodesByFormId(@PathParam("formId") String formId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            jwtUtil.verifyToken(token);

            List<QRCode> qrCodes = qrCodeEJB.findByFormId(formId);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (QRCode qr : qrCodes) {
                arrayBuilder.add(buildQRCodeJson(qr));
            }

            JsonObject data = Json.createObjectBuilder()
                    .add("qrCodes", arrayBuilder.build())
                    .add("total", qrCodes.size())
                    .build();

            return ResponseUtil.success(data);

        } catch (Exception e) {
            logger.error("❌ Error fetching QR Codes by form: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to fetch QR Codes: " + e.getMessage());
        }
    }

    /**
     * Get all QR Codes by Organization ID GET /api/qr/org/{orgId}
     */
    @GET
    @Path("/org/{orgId}")
    public Response getQRCodesByOrgId(@PathParam("orgId") Long orgId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);

            // Check authorization
            if (!jwtUtil.isOrgAdmin(jwt)) {
                Long userOrgId = jwtUtil.getOrgId(jwt);
                if (userOrgId == null || !userOrgId.equals(orgId)) {
                    return ResponseUtil.forbidden("Access denied");
                }
            }

            List<QRCode> qrCodes = qrCodeEJB.findByOrgId(orgId);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (QRCode qr : qrCodes) {
                arrayBuilder.add(buildQRCodeJson(qr));
            }

            JsonObject data = Json.createObjectBuilder()
                    .add("qrCodes", arrayBuilder.build())
                    .add("total", qrCodes.size())
                    .build();

            return ResponseUtil.success(data);

        } catch (Exception e) {
            logger.error("❌ Error fetching QR Codes by org: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to fetch QR Codes: " + e.getMessage());
        }
    }

    /**
     * Update QR Code PUT /api/qr/{qrCodeId}
     */
    @PUT
    @Path("/{qrCodeId}")
    public Response updateQRCode(@PathParam("qrCodeId") Long qrCodeId,
            UpdateQRRequest request,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);

            if (!jwtUtil.isOrgAdmin(jwt)) {
                return ResponseUtil.forbidden("Only organization admins can update QR codes");
            }

            QRCode qrCode = qrCodeEJB.findById(qrCodeId);
            if (qrCode == null) {
                return ResponseUtil.notFound("QR Code not found");
            }

            // Update editable fields only
            if (request.getQrCodeName() != null) {
                qrCode.setQrCodeName(request.getQrCodeName());
            }
            if (request.getPaymentAmount() != null) {
                qrCode.setPaymentAmount(request.getPaymentAmount());
            }
            if (request.getStatus() != null) {
                qrCode.setStatus(request.getStatus());
            }

            QRCode updated = qrCodeEJB.updateQRCode(qrCode);

            JsonObject data = buildQRCodeJson(updated);
            return ResponseUtil.success(data, "QR Code updated successfully");

        } catch (Exception e) {
            logger.error("❌ Error updating QR Code: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to update QR Code: " + e.getMessage());
        }
    }

    /**
     * Activate QR Code PUT /api/qr/{qrCodeId}/activate
     */
    @PUT
    @Path("/{qrCodeId}/activate")
    public Response activateQRCode(@PathParam("qrCodeId") Long qrCodeId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Long userId = jwtUtil.getUserId(jwt);

            if (!jwtUtil.isOrgAdmin(jwt)) {
                return ResponseUtil.forbidden("Only organization admins can activate QR codes");
            }

            QRCode qrCode = qrCodeEJB.updateStatus(qrCodeId, "ACTIVE");

            // Publish Kafka event
            kafkaProducer.publishQRActivated(qrCode.getQrCodeId(), qrCode.getQrCodeSlug(), userId);

            JsonObject data = buildQRCodeJson(qrCode);
            return ResponseUtil.success(data, "QR Code activated successfully");

        } catch (Exception e) {
            logger.error("❌ Error activating QR Code: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to activate QR Code: " + e.getMessage());
        }
    }

    /**
     * Deactivate QR Code PUT /api/qr/{qrCodeId}/deactivate
     */
    @PUT
    @Path("/{qrCodeId}/deactivate")
    public Response deactivateQRCode(@PathParam("qrCodeId") Long qrCodeId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Long userId = jwtUtil.getUserId(jwt);

            if (!jwtUtil.isOrgAdmin(jwt)) {
                return ResponseUtil.forbidden("Only organization admins can deactivate QR codes");
            }

            QRCode qrCode = qrCodeEJB.updateStatus(qrCodeId, "INACTIVE");

            // Publish Kafka event
            kafkaProducer.publishQRDeactivated(qrCode.getQrCodeId(), qrCode.getQrCodeSlug(), userId);

            JsonObject data = buildQRCodeJson(qrCode);
            return ResponseUtil.success(data, "QR Code deactivated successfully");

        } catch (Exception e) {
            logger.error("❌ Error deactivating QR Code: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to deactivate QR Code: " + e.getMessage());
        }
    }

    /**
     * Helper: Build QR Code JSON
     */
    private JsonObject buildQRCodeJson(QRCode qr) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("qrCodeId", qr.getQrCodeId())
                .add("qrCodeSlug", qr.getQrCodeSlug())
                .add("qrCodeUrl", qr.getQrCodeUrl())
                .add("qrCodeName", qr.getQrCodeName())
                .add("formId", qr.getFormId())
                .add("serviceId", qr.getServiceId())
                .add("orgId", qr.getOrgId())
                .add("createdBy", qr.getCreatedBy())
                .add("paymentAmount", qr.getPaymentAmount())
                .add("assignmentStatus", qr.getAssignmentStatus())
                .add("isDeployed", qr.getIsDeployed())
                .add("status", qr.getStatus())
                .add("totalScans", qr.getTotalScans())
                .add("totalResponses", qr.getTotalResponses())
                .add("createdAt", qr.getCreatedAt().toString())
                .add("updatedAt", qr.getUpdatedAt().toString());

        if (qr.getAssignedTo() != null) {
            builder.add("assignedTo", qr.getAssignedTo());
        }
        if (qr.getAssignedAt() != null) {
            builder.add("assignedAt", qr.getAssignedAt().toString());
        }
        if (qr.getDeploymentStatus() != null) {
            builder.add("deploymentStatus", qr.getDeploymentStatus());
        }
        if (qr.getDeploymentLocationName() != null) {
            builder.add("deploymentLocationName", qr.getDeploymentLocationName());
        }
        if (qr.getDeploymentPhotoUrl() != null) {
            builder.add("deploymentPhotoUrl", qr.getDeploymentPhotoUrl());
        }
        if (qr.getLastScannedAt() != null) {
            builder.add("lastScannedAt", qr.getLastScannedAt().toString());
        }
        if (qr.getDeploymentLongitude() != null) {
            builder.add("longitude", qr.getDeploymentLongitude().toString());
        }
        if (qr.getDeploymentLatitude() != null) {
            builder.add("latitude", qr.getDeploymentLatitude().toString());
        }
        if (qr.getDeployedAt() != null) {
            builder.add("deployedAt", qr.getDeployedAt().toString());
        }

        return builder.build();
    }
}
