package com.shakilpatel.qr.rest;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.shakilpatel.qr.ejb.QRAnalyticsEJB;
import com.shakilpatel.qr.ejb.QRCodeEJB;
import com.shakilpatel.qr.ejb.QRScanEJB;
import com.shakilpatel.qr.entity.QRAnalyticsDaily;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/qr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QRAnalyticsResource {

    private static final Logger logger = LoggerFactory.getLogger(QRAnalyticsResource.class);

    @Inject
    private QRCodeEJB qrCodeEJB;

    @Inject
    private QRScanEJB scanEJB;

    @Inject
    private QRAnalyticsEJB analyticsEJB;

    @Inject
    private JWTUtil jwtUtil;

    /**
     * Get analytics for single QR code GET /api/qr/{qrCodeId}/analytics
     */
    @GET
    @Path("/{qrCodeId}/analytics")
    public Response getQRAnalytics(@PathParam("qrCodeId") Long qrCodeId,
            @QueryParam("days") @DefaultValue("7") int days,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            jwtUtil.verifyToken(token);

            QRCode qrCode = qrCodeEJB.findById(qrCodeId);
            if (qrCode == null) {
                return ResponseUtil.notFound("QR Code not found");
            }

            // Get daily analytics
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);
            List<QRAnalyticsDaily> dailyAnalytics = analyticsEJB.getAnalyticsByDateRange(qrCodeId, startDate, endDate);

            // Calculate response rate
            double responseRate = 0.0;
            if (qrCode.getTotalScans() > 0) {
                responseRate = (qrCode.getTotalResponses().doubleValue() / qrCode.getTotalScans()) * 100;
            }

            // Build timeline
            JsonArrayBuilder timelineBuilder = Json.createArrayBuilder();
            for (QRAnalyticsDaily daily : dailyAnalytics) {
                timelineBuilder.add(Json.createObjectBuilder()
                        .add("date", daily.getDate().toString())
                        .add("scans", daily.getTotalScans())
                        .add("responses", daily.getTotalResponses())
                        .build());
            }

            // Build device breakdown
            int totalMobile = 0, totalTablet = 0, totalDesktop = 0;
            for (QRAnalyticsDaily daily : dailyAnalytics) {
                totalMobile += daily.getMobileScans();
                totalTablet += daily.getTabletScans();
                totalDesktop += daily.getDesktopScans();
            }

            JsonObject scansByDevice = Json.createObjectBuilder()
                    .add("mobile", totalMobile)
                    .add("tablet", totalTablet)
                    .add("desktop", totalDesktop)
                    .build();

            JsonObject data = Json.createObjectBuilder()
                    .add("qrCodeId", qrCode.getQrCodeId())
                    .add("qrCodeSlug", qrCode.getQrCodeSlug())
                    .add("totalScans", qrCode.getTotalScans())
                    .add("totalResponses", qrCode.getTotalResponses())
                    .add("responseRate", Math.round(responseRate * 100.0) / 100.0)
                    .add("lastScannedAt", qrCode.getLastScannedAt() != null ? qrCode.getLastScannedAt().toString() : "")
                    .add("scansByDevice", scansByDevice)
                    .add("scanTimeline", timelineBuilder.build())
                    .build();

            return ResponseUtil.success(data);

        } catch (Exception e) {
            logger.error("❌ Error fetching QR analytics: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to fetch analytics: " + e.getMessage());
        }
    }

    /**
     * Get organization-level analytics GET /api/qr/org/{orgId}/analytics
     */
    @GET
    @Path("/org/{orgId}/analytics")
    public Response getOrgAnalytics(@PathParam("orgId") Long orgId,
            @HeaderParam("Authorization") String authHeader) {
        try {
            // Verify JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.unauthorized("Missing or invalid authorization header");
            }

            String token = authHeader.substring(7);
            DecodedJWT jwt = jwtUtil.verifyToken(token);

            // Check authorization
            if (!jwtUtil.isPlatformAdmin(jwt)) {
                Long userOrgId = jwtUtil.getOrgId(jwt);
                if (userOrgId == null || !userOrgId.equals(orgId)) {
                    return ResponseUtil.forbidden("Access denied");
                }
            }

            List<QRCode> qrCodes = qrCodeEJB.findByOrgId(orgId);

            int totalQRs = qrCodes.size();
            int activeQRs = 0;
            int deployedQRs = 0;
            int totalScans = 0;
            int totalResponses = 0;

            for (QRCode qr : qrCodes) {
                if ("ACTIVE".equals(qr.getStatus())) {
                    activeQRs++;
                }
                if (qr.getIsDeployed()) {
                    deployedQRs++;
                }
                totalScans += qr.getTotalScans();
                totalResponses += qr.getTotalResponses();
            }

            double responseRate = totalScans > 0 ? (totalResponses * 100.0 / totalScans) : 0.0;

            // Top performing QRs
            qrCodes.sort((a, b) -> Integer.compare(b.getTotalScans(), a.getTotalScans()));
            JsonArrayBuilder topQRsBuilder = Json.createArrayBuilder();
            for (int i = 0; i < Math.min(5, qrCodes.size()); i++) {
                QRCode qr = qrCodes.get(i);
                topQRsBuilder.add(Json.createObjectBuilder()
                        .add("qrCodeId", qr.getQrCodeId())
                        .add("qrCodeName", qr.getQrCodeName())
                        .add("qrCodeSlug", qr.getQrCodeSlug())
                        .add("scans", qr.getTotalScans())
                        .add("responses", qr.getTotalResponses())
                        .build());
            }

            JsonObject data = Json.createObjectBuilder()
                    .add("totalQRs", totalQRs)
                    .add("activeQRs", activeQRs)
                    .add("deployedQRs", deployedQRs)
                    .add("totalScans", totalScans)
                    .add("totalResponses", totalResponses)
                    .add("responseRate", Math.round(responseRate * 100.0) / 100.0)
                    .add("topPerformingQRs", topQRsBuilder.build())
                    .build();

            return ResponseUtil.success(data);

        } catch (Exception e) {
            logger.error("❌ Error fetching org analytics: {}", e.getMessage(), e);
            return ResponseUtil.internalError("Failed to fetch analytics: " + e.getMessage());
        }
    }
}
