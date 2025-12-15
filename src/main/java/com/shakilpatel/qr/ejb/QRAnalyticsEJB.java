package com.shakilpatel.qr.ejb;

import com.shakilpatel.qr.entity.QRAnalyticsDaily;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Stateless
public class QRAnalyticsEJB {

    private static final Logger logger = LoggerFactory.getLogger(QRAnalyticsEJB.class);

    @PersistenceContext(unitName = "qrPU")
    private EntityManager em;

    /**
     * Get or create daily analytics record
     */
    public QRAnalyticsDaily getOrCreateDailyAnalytics(Long qrCodeId, LocalDate date) {
        try {
            TypedQuery<QRAnalyticsDaily> query = em.createQuery(
                    "SELECT a FROM QRAnalyticsDaily a WHERE a.qrCodeId = :qrCodeId AND a.date = :date",
                    QRAnalyticsDaily.class
            );
            query.setParameter("qrCodeId", qrCodeId);
            query.setParameter("date", date);

            List<QRAnalyticsDaily> results = query.getResultList();

            if (!results.isEmpty()) {
                return results.get(0);
            }

            // Create new record
            QRAnalyticsDaily analytics = new QRAnalyticsDaily();
            analytics.setQrCodeId(qrCodeId);
            analytics.setDate(date);
            em.persist(analytics);
            em.flush();

            return analytics;
        } catch (Exception e) {
            logger.error("❌ Error getting daily analytics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get analytics: " + e.getMessage());
        }
    }

    /**
     * Increment scan count for today
     */
    public void incrementScanCount(Long qrCodeId, String deviceType) {
        try {
            LocalDate today = LocalDate.now();
            QRAnalyticsDaily analytics = getOrCreateDailyAnalytics(qrCodeId, today);

            analytics.setTotalScans(analytics.getTotalScans() + 1);

            // Update device breakdown
            if ("MOBILE".equalsIgnoreCase(deviceType)) {
                analytics.setMobileScans(analytics.getMobileScans() + 1);
            } else if ("TABLET".equalsIgnoreCase(deviceType)) {
                analytics.setTabletScans(analytics.getTabletScans() + 1);
            } else if ("DESKTOP".equalsIgnoreCase(deviceType)) {
                analytics.setDesktopScans(analytics.getDesktopScans() + 1);
            }

            em.merge(analytics);
            em.flush();
        } catch (Exception e) {
            logger.error("❌ Failed to increment scan count: {}", e.getMessage());
        }
    }

    /**
     * Increment response count for today
     */
    public void incrementResponseCount(Long qrCodeId) {
        try {
            LocalDate today = LocalDate.now();
            QRAnalyticsDaily analytics = getOrCreateDailyAnalytics(qrCodeId, today);

            analytics.setTotalResponses(analytics.getTotalResponses() + 1);

            em.merge(analytics);
            em.flush();
        } catch (Exception e) {
            logger.error("❌ Failed to increment response count: {}", e.getMessage());
        }
    }

    /**
     * Get analytics for date range
     */
    public List<QRAnalyticsDaily> getAnalyticsByDateRange(Long qrCodeId, LocalDate startDate, LocalDate endDate) {
        try {
            TypedQuery<QRAnalyticsDaily> query = em.createNamedQuery(
                    "QRAnalyticsDaily.findByQRCodeIdAndDateRange",
                    QRAnalyticsDaily.class
            );
            query.setParameter("qrCodeId", qrCodeId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);

            return query.getResultList();
        } catch (Exception e) {
            logger.error("❌ Error fetching analytics: {}", e.getMessage());
            return List.of();
        }
    }
}
