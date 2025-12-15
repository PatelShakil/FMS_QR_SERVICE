package com.shakilpatel.qr.ejb;

import com.shakilpatel.qr.entity.QRScan;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class QRScanEJB {

    private static final Logger logger = LoggerFactory.getLogger(QRScanEJB.class);

    @PersistenceContext(unitName = "qrPU")
    private EntityManager em;

    /**
     * Record a QR scan
     */
    public QRScan recordScan(QRScan scan) {
        try {
            em.persist(scan);
            em.flush();
            logger.info("✅ QR Scan recorded for QR Code ID: {}", scan.getQrCodeId());
            return scan;
        } catch (Exception e) {
            logger.error("❌ Failed to record scan: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to record scan: " + e.getMessage());
        }
    }

    /**
     * Find all scans for a QR code
     */
    public List<QRScan> findByQRCodeId(Long qrCodeId) {
        try {
            TypedQuery<QRScan> query = em.createNamedQuery("QRScan.findByQRCodeId", QRScan.class);
            query.setParameter("qrCodeId", qrCodeId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("❌ Error finding scans for QR {}: {}", qrCodeId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Count scans for a QR code
     */
    public Long countByQRCodeId(Long qrCodeId) {
        try {
            TypedQuery<Long> query = em.createNamedQuery("QRScan.countByQRCodeId", Long.class);
            query.setParameter("qrCodeId", qrCodeId);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("❌ Error counting scans for QR {}: {}", qrCodeId, e.getMessage());
            return 0L;
        }
    }

    /**
     * Find scans in date range
     */
    public List<QRScan> findByDateRange(Long qrCodeId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            TypedQuery<QRScan> query = em.createNamedQuery("QRScan.findByDateRange", QRScan.class);
            query.setParameter("qrCodeId", qrCodeId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("❌ Error finding scans by date range: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Update scan with response info
     */
    public void updateScanWithResponse(Long scanId, String responseId) {
        try {
            QRScan scan = em.find(QRScan.class, scanId);
            if (scan != null) {
                scan.setDidSubmitResponse(true);
                scan.setResponseId(responseId);
                scan.setResponseSubmittedAt(LocalDateTime.now());
                em.merge(scan);
                em.flush();
                logger.info("✅ Scan {} updated with response {}", scanId, responseId);
            }
        } catch (Exception e) {
            logger.error("❌ Failed to update scan {}: {}", scanId, e.getMessage());
        }
    }
}
