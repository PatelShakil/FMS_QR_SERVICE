package com.shakilpatel.qr.rest;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.shakilpatel.qr.dto.AssignQRRequest;
import com.shakilpatel.qr.ejb.KafkaProducerEJB;
import com.shakilpatel.qr.ejb.QRCodeEJB;
import com.shakilpatel.qr.entity.QRCode;
import com.shakilpatel.qr.util.JWTUtil;
import com.shakilpatel.qr.util.ResponseUtil;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("/qr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QRAssignmentResource {

    private static final Logger logger = LoggerFactory.getLogger(QRAssignmentResource.class);

    @Inject
    private QRCodeEJB qrCodeEJB;

    @Inject
    private KafkaProducerEJB kafkaProducer;

    @Inject
    private JWTUtil jwtUtil;

    /**
     * Assign QR to distributor POST /api/qr/{qrCodeId}/assign
     */
    @POST
    @Path("/{qrCodeId}/assign")
    public Response assignQRCode(@PathParam("qrCodeId") Long qrCodeId,
            AssignQRRequest request,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Long adminId = jwtUtil.getUserId(jwt);

            if (!jwtUtil.isOrgAdmin(jwt)) {
                return ResponseUtil.forbidden("Only organization admins can assign QR codes");
            }

            if (request.getDistributorId() == null) {
                return ResponseUtil.badRequest("Distributor ID is required");
            }

            QRCode qrCode = qrCodeEJB.assignToDistributor(qrCodeId, request.getDistributorId());

            // Publish Kafka event
            kafkaProducer.publishQRAssigned(
                    qrCode.getQrCodeId(),
                    qrCode.getQrCodeSlug(),
                    request.getDistributorId(),
                    adminId,
                    request.getDistributorEmail()
            );

            JsonObject data = Json.createObjectBuilder()
                    .add("qrCodeId", qrCode.getQrCodeId())
                    .add("qrCodeSlug", qrCode.getQrCodeSlug())
                    .add("assignedTo", qrCode.getAssignedTo())
                    .add("assignmentStatus", qrCode.getAssignmentStatus())
                    .add("assignedAt", qrCode.getAssignedAt().toString())
                    .build();

            return ResponseUtil.success(data, "QR Code assigned successfully");

        } catch (Exception e) {
            logger.error("❌ Error assigning QR Code: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to assign QR Code: " + e.getMessage());
        }
    }

    /**
     * Get pending assignments for organization GET /api/qr/assignments/pending
     */
    @GET
    @Path("/assignments/pending")
    public Response getPendingAssignments(@HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);

            if (!jwtUtil.isOrgAdmin(jwt)) {
                return ResponseUtil.forbidden("Only organization admins can view pending assignments");
            }

            Long orgId = jwtUtil.getOrgId(jwt);
            if (orgId == null) {
                return ResponseUtil.badRequest("Organization ID not found in token");
            }

            List<QRCode> pendingQRs = qrCodeEJB.findPendingAssignments(orgId);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (QRCode qr : pendingQRs) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("qrCodeId", qr.getQrCodeId())
                        .add("qrCodeSlug", qr.getQrCodeSlug())
                        .add("qrCodeName", qr.getQrCodeName())
                        .add("assignedTo", qr.getAssignedTo())
                        .add("assignedAt", qr.getAssignedAt().toString())
                        .build());
            }

            JsonObject data = Json.createObjectBuilder()
                    .add("pendingAssignments", arrayBuilder.build())
                    .add("total", pendingQRs.size())
                    .build();

            return ResponseUtil.success(data);

        } catch (Exception e) {
            logger.error("❌ Error fetching pending assignments: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to fetch pending assignments: " + e.getMessage());
        }
    }

    /**
     * Approve deployment POST /api/qr/{qrCodeId}/approve-deployment
     */
    @POST
    @Path("/{qrCodeId}/approve-deployment")
    public Response approveDeployment(@PathParam("qrCodeId") Long qrCodeId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Long adminId = jwtUtil.getUserId(jwt);

            if (!jwtUtil.isOrgAdmin(jwt)) {
                return ResponseUtil.forbidden("Only organization admins can approve deployments");
            }

            QRCode qrCode = qrCodeEJB.approveDeployment(qrCodeId, adminId);

            // Publish Kafka event
            kafkaProducer.publishQRDeployed(
                    qrCode.getQrCodeId(),
                    qrCode.getQrCodeSlug(),
                    qrCode.getAssignedTo(),
                    adminId
            );

            JsonObject data = Json.createObjectBuilder()
                    .add("qrCodeId", qrCode.getQrCodeId())
                    .add("deploymentStatus", qrCode.getDeploymentStatus())
                    .add("finalizedAt", qrCode.getFinalizedAt().toString())
                    .build();

            return ResponseUtil.success(data, "Deployment approved successfully");

        } catch (Exception e) {
            logger.error("❌ Error approving deployment: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to approve deployment: " + e.getMessage());
        }
    }

    /**
     * Reject deployment POST /api/qr/{qrCodeId}/reject-deployment
     */
    @POST
    @Path("/{qrCodeId}/reject-deployment")
    public Response rejectDeployment(@PathParam("qrCodeId") Long qrCodeId,
            jakarta.json.JsonObject requestBody,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Long adminId = jwtUtil.getUserId(jwt);

            if (!jwtUtil.isOrgAdmin(jwt)) {
                return ResponseUtil.forbidden("Only organization admins can reject deployments");
            }

            String reason = requestBody.getString("reason", "No reason provided");

            QRCode qrCode = qrCodeEJB.rejectDeployment(qrCodeId, adminId, reason);

            // Publish Kafka event
            kafkaProducer.publishDeploymentRejected(
                    qrCode.getQrCodeId(),
                    qrCode.getQrCodeSlug(),
                    adminId,
                    reason
            );

            JsonObject data = Json.createObjectBuilder()
                    .add("qrCodeId", qrCode.getQrCodeId())
                    .add("deploymentStatus", qrCode.getDeploymentStatus())
                    .add("rejectionReason", reason)
                    .build();

            return ResponseUtil.success(data, "Deployment rejected");

        } catch (Exception e) {
            logger.error("❌ Error rejecting deployment: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to reject deployment: " + e.getMessage());
        }
    }
}
