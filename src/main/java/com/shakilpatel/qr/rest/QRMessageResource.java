package com.shakilpatel.qr.rest;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.shakilpatel.qr.dto.SendMessageRequest;
import com.shakilpatel.qr.ejb.QRMessageEJB;
import com.shakilpatel.qr.entity.QRMessage;
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
public class QRMessageResource {

    private static final Logger logger = LoggerFactory.getLogger(QRMessageResource.class);

    @Inject
    private QRMessageEJB messageEJB;

    @Inject
    private JWTUtil jwtUtil;

    /**
     * Send message POST /api/qr/{qrCodeId}/message
     */
    @POST
    @Path("/{qrCodeId}/message")
    public Response sendMessage(@PathParam("qrCodeId") Long qrCodeId,
            SendMessageRequest request,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Long senderId = jwtUtil.getUserId(jwt);

            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseUtil.badRequest("Message text is required");
            }

            // Determine sender type
            String senderType = jwtUtil.isOrgAdmin(jwt) ? "ORG_ADMIN" : "DISTRIBUTOR";

            QRMessage message = new QRMessage();
            message.setQrCodeId(qrCodeId);
            message.setSenderId(senderId);
            message.setSenderType(senderType);
            message.setMessageText(request.getMessage());

            QRMessage saved = messageEJB.sendMessage(message);

            JsonObject data = Json.createObjectBuilder()
                    .add("messageId", saved.getMessageId())
                    .add("qrCodeId", saved.getQrCodeId())
                    .add("senderId", saved.getSenderId())
                    .add("senderType", saved.getSenderType())
                    .add("messageText", saved.getMessageText())
                    .add("createdAt", saved.getCreatedAt().toString())
                    .build();

            return ResponseUtil.success(data, "Message sent successfully");

        } catch (Exception e) {
            logger.error("❌ Error sending message: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Get messages (conversation thread) GET /api/qr/{qrCodeId}/messages
     */
    @GET
    @Path("/{qrCodeId}/messages")
    public Response getMessages(@PathParam("qrCodeId") Long qrCodeId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            jwtUtil.verifyToken(token);

            List<QRMessage> messages = messageEJB.getMessages(qrCodeId);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (QRMessage msg : messages) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("messageId", msg.getMessageId())
                        .add("senderId", msg.getSenderId())
                        .add("senderType", msg.getSenderType())
                        .add("messageText", msg.getMessageText())
                        .add("isRead", msg.getIsRead())
                        .add("createdAt", msg.getCreatedAt().toString())
                        .build());
            }

            JsonObject data = Json.createObjectBuilder()
                    .add("messages", arrayBuilder.build())
                    .add("total", messages.size())
                    .build();

            return ResponseUtil.success(data);

        } catch (Exception e) {
            logger.error("❌ Error fetching messages: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to fetch messages: " + e.getMessage());
        }
    }

    /**
     * Mark message as read PUT /api/qr/message/{messageId}/read
     */
    @PUT
    @Path("/message/{messageId}/read")
    public Response markMessageAsRead(@PathParam("messageId") Long messageId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            jwtUtil.verifyToken(token);

            messageEJB.markAsRead(messageId);

            return ResponseUtil.success(null, "Message marked as read");

        } catch (Exception e) {
            logger.error("❌ Error marking message as read: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to mark message as read: " + e.getMessage());
        }
    }

    /**
     * Get unread message count GET /api/qr/messages/unread-count
     */
    @GET
    @Path("/messages/unread-count")
    public Response getUnreadCount(@QueryParam("qrCodeId") Long qrCodeId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            jwtUtil.verifyToken(token);

            if (qrCodeId == null) {
                return ResponseUtil.badRequest("QR Code ID is required");
            }

            Long unreadCount = messageEJB.countUnread(qrCodeId);

            JsonObject data = Json.createObjectBuilder()
                    .add("unreadCount", unreadCount)
                    .build();

            return ResponseUtil.success(data);

        } catch (Exception e) {
            logger.error("❌ Error fetching unread count: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to fetch unread count: " + e.getMessage());
        }
    }
}
