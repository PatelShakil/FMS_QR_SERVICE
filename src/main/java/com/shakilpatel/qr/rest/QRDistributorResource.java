package com.shakilpatel.qr.rest;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.shakilpatel.qr.dto.DeployQRRequest;
import com.shakilpatel.qr.dto.RejectQRRequest;
import com.shakilpatel.qr.ejb.KafkaProducerEJB;
import com.shakilpatel.qr.ejb.MinIOEJB;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("/qr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QRDistributorResource {

    private static final Logger logger = LoggerFactory.getLogger(QRDistributorResource.class);

    @Inject
    private QRCodeEJB qrCodeEJB;

    @Inject
    private MinIOEJB minioEJB;

    @Inject
    private KafkaProducerEJB kafkaProducer;

    @Inject
    private JWTUtil jwtUtil;

    /**
     * Get distributor's assignments GET /api/qr/my-assignments
     */
    @GET
    @Path("/my-assignments")
    public Response getMyAssignments(@HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Long distributorId = jwtUtil.getUserId(jwt);

            if (!jwtUtil.isDistributor(jwt)) {
                return ResponseUtil.forbidden("Only distributors can view their assignments");
            }

            List<QRCode> assignments = qrCodeEJB.findByDistributor(distributorId);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (QRCode qr : assignments) {
                arrayBuilder.add(buildDistributorQRJson(qr));
            }

            JsonObject data = Json.createObjectBuilder()
                    .add("assignments", arrayBuilder.build())
                    .add("total", assignments.size())
                    .build();

            return ResponseUtil.success(data);

        } catch (Exception e) {
            logger.error("❌ Error fetching distributor assignments: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to fetch assignments: " + e.getMessage());
        }
    }

    /**
     * Accept QR assignment POST /api/qr/{qrCodeId}/accept
     */
    @POST
    @Path("/{qrCodeId}/accept")
    public Response acceptAssignment(@PathParam("qrCodeId") Long qrCodeId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Long distributorId = jwtUtil.getUserId(jwt);

            if (!jwtUtil.isDistributor(jwt)) {
                return ResponseUtil.forbidden("Only distributors can accept assignments");
            }

            QRCode qrCode = qrCodeEJB.acceptAssignment(qrCodeId, distributorId);

            // Publish Kafka event
            kafkaProducer.publishQRAccepted(
                    qrCode.getQrCodeId(),
                    qrCode.getQrCodeSlug(),
                    distributorId
            );

            JsonObject data = Json.createObjectBuilder()
                    .add("qrCodeId", qrCode.getQrCodeId())
                    .add("qrCodeSlug", qrCode.getQrCodeSlug())
                    .add("assignmentStatus", qrCode.getAssignmentStatus())
                    .add("acceptedAt", qrCode.getAcceptedAt().toString())
                    .build();

            return ResponseUtil.success(data, "Assignment accepted successfully");

        } catch (Exception e) {
            logger.error("❌ Error accepting assignment: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to accept assignment: " + e.getMessage());
        }
    }

    /**
     * Reject QR assignment POST /api/qr/{qrCodeId}/reject
     */
    @POST
    @Path("/{qrCodeId}/reject")
    public Response rejectAssignment(@PathParam("qrCodeId") Long qrCodeId,
            RejectQRRequest request,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Long distributorId = jwtUtil.getUserId(jwt);

            if (!jwtUtil.isDistributor(jwt)) {
                return ResponseUtil.forbidden("Only distributors can reject assignments");
            }

            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                return ResponseUtil.badRequest("Rejection reason is required");
            }

            QRCode qrCode = qrCodeEJB.rejectAssignment(qrCodeId, distributorId, request.getReason());

            // Publish Kafka event
            kafkaProducer.publishQRRejected(
                    qrCode.getQrCodeId(),
                    qrCode.getQrCodeSlug(),
                    distributorId,
                    request.getReason()
            );

            JsonObject data = Json.createObjectBuilder()
                    .add("qrCodeId", qrCode.getQrCodeId())
                    .add("qrCodeSlug", qrCode.getQrCodeSlug())
                    .add("assignmentStatus", qrCode.getAssignmentStatus())
                    .add("rejectionReason", request.getReason())
                    .build();

            return ResponseUtil.success(data, "Assignment rejected");

        } catch (Exception e) {
            logger.error("❌ Error rejecting assignment: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to reject assignment: " + e.getMessage());
        }
    }

    /**
     * Submit deployment POST /api/qr/{qrCodeId}/deploy
     */
    @POST
    @Path("/{qrCodeId}/deploy")
    public Response submitDeployment(@PathParam("qrCodeId") Long qrCodeId,
            DeployQRRequest request,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Long distributorId = jwtUtil.getUserId(jwt);

            if (!jwtUtil.isDistributor(jwt)) {
                return ResponseUtil.forbidden("Only distributors can submit deployments");
            }

            // Validate request
            if (request.getLatitude() == null || request.getLongitude() == null) {
                return ResponseUtil.badRequest("Location coordinates are required");
            }
            if (request.getLocationName() == null || request.getLocationName().trim().isEmpty()) {
                return ResponseUtil.badRequest("Location name is required");
            }

            // Create deployment data
            QRCodeEJB.DeploymentData deploymentData = new QRCodeEJB.DeploymentData();
            deploymentData.locationName = request.getLocationName();
            deploymentData.latitude = request.getLatitude();
            deploymentData.longitude = request.getLongitude();
            deploymentData.notes = request.getNotes();
            deploymentData.photoUrl = request.getDeploymentPhotoUrl();

            QRCode qrCode = qrCodeEJB.submitDeployment(qrCodeId, distributorId, deploymentData);

            // Publish Kafka event
            kafkaProducer.publishDeploymentSubmitted(
                    qrCode.getQrCodeId(),
                    qrCode.getQrCodeSlug(),
                    distributorId,
                    request.getLocationName()
            );

            JsonObject data = Json.createObjectBuilder()
                    .add("qrCodeId", qrCode.getQrCodeId())
                    .add("qrCodeSlug", qrCode.getQrCodeSlug())
                    .add("deploymentStatus", qrCode.getDeploymentStatus())
                    .add("deployedAt", qrCode.getDeployedAt().toString())
                    .add("message", "Deployment submitted for approval")
                    .build();

            return ResponseUtil.success(data, "Deployment submitted successfully");

        } catch (Exception e) {
            logger.error("❌ Error submitting deployment: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to submit deployment: " + e.getMessage());
        }
    }

    /**
     * Upload deployment photo POST /api/qr/{qrCodeId}/upload-photo
     */
    @POST
    @Path("/{qrCodeId}/upload-photo")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadDeploymentPhoto(@PathParam("qrCodeId") Long qrCodeId,
            @FormParam("file") byte[] fileData,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);

            if (!jwtUtil.isDistributor(jwt)) {
                return ResponseUtil.forbidden("Only distributors can upload deployment photos");
            }

            QRCode qrCode = qrCodeEJB.findById(qrCodeId);
            if (qrCode == null) {
                return ResponseUtil.notFound("QR Code not found");
            }

            if (fileData == null || fileData.length == 0) {
                return ResponseUtil.badRequest("File is required");
            }

            // Upload to MinIO
            String photoUrl = minioEJB.uploadDeploymentPhoto(fileData, qrCode.getQrCodeSlug());

            JsonObject data = Json.createObjectBuilder()
                    .add("photoUrl", photoUrl)
                    .add("message", "Photo uploaded successfully")
                    .build();

            return ResponseUtil.success(data);

        } catch (Exception e) {
            logger.error("❌ Error uploading photo: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to upload photo: " + e.getMessage());
        }
    }

    /**
     * Helper: Build distributor-view QR JSON
     */
    private JsonObject buildDistributorQRJson(QRCode qr) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("qrCodeId", qr.getQrCodeId())
                .add("qrCodeSlug", qr.getQrCodeSlug())
                .add("qrCodeUrl", qr.getQrCodeUrl())
                .add("qrCodeName", qr.getQrCodeName())
                .add("paymentAmount", qr.getPaymentAmount())
                .add("assignmentStatus", qr.getAssignmentStatus())
                .add("isDeployed", qr.getIsDeployed())
                .add("status", qr.getStatus());

        if (qr.getAssignedAt() != null) {
            builder.add("assignedAt", qr.getAssignedAt().toString());
        }
        if (qr.getAcceptedAt() != null) {
            builder.add("acceptedAt", qr.getAcceptedAt().toString());
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

        return builder.build();
    }
}
