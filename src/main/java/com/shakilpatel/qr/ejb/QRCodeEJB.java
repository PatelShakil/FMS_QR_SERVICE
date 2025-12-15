package com.shakilpatel.qr.ejb;

import com.shakilpatel.qr.entity.QRCode;
import com.shakilpatel.qr.util.SlugGenerator;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class QRCodeEJB {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeEJB.class);

    @PersistenceContext(unitName = "qrPU")
    private EntityManager em;

    /**
     * Create new QR Code
     */
    public QRCode createQRCode(QRCode qrCode) {
        try {
            // Generate unique slug
            String slug = generateUniqueSlug();
            qrCode.setQrCodeSlug(slug);
            qrCode.setQrCodeUrl(SlugGenerator.generateQRUrl(slug));

            em.persist(qrCode);
            em.flush();

            logger.info("✅ QR Code created: {} (ID: {})", slug, qrCode.getQrCodeId());
            return qrCode;
        } catch (Exception e) {
            logger.error("❌ Failed to create QR Code: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create QR Code: " + e.getMessage());
        }
    }

    /**
     * Generate unique slug (check for collisions)
     */
    private String generateUniqueSlug() {
        String slug;
        int attempts = 0;
        do {
            slug = SlugGenerator.generateQRSlug();
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Failed to generate unique QR slug after 10 attempts");
            }
        } while (findBySlug(slug) != null);

        return slug;
    }

    /**
     * Find QR Code by slug
     */
    public QRCode findBySlug(String slug) {
        try {
            TypedQuery<QRCode> query = em.createNamedQuery("QRCode.findBySlug", QRCode.class);
            query.setParameter("slug", slug);
            List<QRCode> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logger.error("❌ Error finding QR by slug {}: {}", slug, e.getMessage());
            return null;
        }
    }

    /**
     * Find QR Code by ID
     */
    public QRCode findById(Long qrCodeId) {
        try {
            return em.find(QRCode.class, qrCodeId);
        } catch (Exception e) {
            logger.error("❌ Error finding QR by ID {}: {}", qrCodeId, e.getMessage());
            return null;
        }
    }

    /**
     * Find all QR Codes by Form ID
     */
    public List<QRCode> findByFormId(String formId) {
        try {
            TypedQuery<QRCode> query = em.createNamedQuery("QRCode.findByFormId", QRCode.class);
            query.setParameter("formId", formId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("❌ Error finding QRs by formId {}: {}", formId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Find all QR Codes by Organization ID
     */
    public List<QRCode> findByOrgId(Long orgId) {
        try {
            TypedQuery<QRCode> query = em.createNamedQuery("QRCode.findByOrgId", QRCode.class);
            query.setParameter("orgId", orgId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("❌ Error finding QRs by orgId {}: {}", orgId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Find all QR Codes assigned to distributor
     */
    public List<QRCode> findByDistributor(Long distributorId) {
        try {
            TypedQuery<QRCode> query = em.createNamedQuery("QRCode.findByAssignedTo", QRCode.class);
            query.setParameter("distributorId", distributorId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("❌ Error finding QRs by distributorId {}: {}", distributorId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Find pending assignments for org
     */
    public List<QRCode> findPendingAssignments(Long orgId) {
        try {
            TypedQuery<QRCode> query = em.createNamedQuery("QRCode.findPendingAssignments", QRCode.class);
            query.setParameter("orgId", orgId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("❌ Error finding pending assignments for orgId {}: {}", orgId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Update QR Code
     */
    public QRCode updateQRCode(QRCode qrCode) {
        try {
            QRCode updated = em.merge(qrCode);
            em.flush();
            logger.info("✅ QR Code updated: {}", qrCode.getQrCodeSlug());
            return updated;
        } catch (Exception e) {
            logger.error("❌ Failed to update QR Code {}: {}", qrCode.getQrCodeId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update QR Code: " + e.getMessage());
        }
    }

    /**
     * Assign QR to distributor
     */
    public QRCode assignToDistributor(Long qrCodeId, Long distributorId) {
        try {
            QRCode qrCode = findById(qrCodeId);
            if (qrCode == null) {
                throw new RuntimeException("QR Code not found");
            }

            qrCode.setAssignedTo(distributorId);
            qrCode.setAssignmentStatus("PENDING");
            qrCode.setAssignedAt(LocalDateTime.now());

            QRCode updated = em.merge(qrCode);
            em.flush();

            logger.info("✅ QR Code {} assigned to distributor {}", qrCode.getQrCodeSlug(), distributorId);
            return updated;
        } catch (Exception e) {
            logger.error("❌ Failed to assign QR {}: {}", qrCodeId, e.getMessage(), e);
            throw new RuntimeException("Failed to assign QR Code: " + e.getMessage());
        }
    }

    /**
     * Accept assignment
     */
    public QRCode acceptAssignment(Long qrCodeId, Long distributorId) {
        try {
            QRCode qrCode = findById(qrCodeId);
            if (qrCode == null) {
                throw new RuntimeException("QR Code not found");
            }

            if (!distributorId.equals(qrCode.getAssignedTo())) {
                throw new RuntimeException("QR Code not assigned to this distributor");
            }

            qrCode.setAssignmentStatus("ACCEPTED");
            qrCode.setAcceptedAt(LocalDateTime.now());

            QRCode updated = em.merge(qrCode);
            em.flush();

            logger.info("✅ QR Code {} accepted by distributor {}", qrCode.getQrCodeSlug(), distributorId);
            return updated;
        } catch (Exception e) {
            logger.error("❌ Failed to accept assignment {}: {}", qrCodeId, e.getMessage(), e);
            throw new RuntimeException("Failed to accept assignment: " + e.getMessage());
        }
    }

    /**
     * Reject assignment
     */
    public QRCode rejectAssignment(Long qrCodeId, Long distributorId, String reason) {
        try {
            QRCode qrCode = findById(qrCodeId);
            if (qrCode == null) {
                throw new RuntimeException("QR Code not found");
            }

            if (!distributorId.equals(qrCode.getAssignedTo())) {
                throw new RuntimeException("QR Code not assigned to this distributor");
            }

            qrCode.setAssignmentStatus("REJECTED");
            qrCode.setRejectedAt(LocalDateTime.now());
            qrCode.setRejectionReason(reason);

            QRCode updated = em.merge(qrCode);
            em.flush();

            logger.info("✅ QR Code {} rejected by distributor {}: {}", qrCode.getQrCodeSlug(), distributorId, reason);
            return updated;
        } catch (Exception e) {
            logger.error("❌ Failed to reject assignment {}: {}", qrCodeId, e.getMessage(), e);
            throw new RuntimeException("Failed to reject assignment: " + e.getMessage());
        }
    }

    /**
     * Submit deployment for approval
     */
    public QRCode submitDeployment(Long qrCodeId, Long distributorId, DeploymentData data) {
        try {
            QRCode qrCode = findById(qrCodeId);
            if (qrCode == null) {
                throw new RuntimeException("QR Code not found");
            }

            if (!distributorId.equals(qrCode.getAssignedTo())) {
                throw new RuntimeException("QR Code not assigned to this distributor");
            }

            if (!"ACCEPTED".equals(qrCode.getAssignmentStatus())) {
                throw new RuntimeException("Assignment not accepted");
            }

            qrCode.setDeploymentStatus("PENDING_APPROVAL");
            qrCode.setDeployedAt(LocalDateTime.now());
            qrCode.setDeploymentLocationName(data.locationName);
            qrCode.setDeploymentLatitude(data.latitude);
            qrCode.setDeploymentLongitude(data.longitude);
            qrCode.setDeploymentNotes(data.notes);
            qrCode.setDeploymentPhotoUrl(data.photoUrl);

            QRCode updated = em.merge(qrCode);
            em.flush();

            logger.info("✅ Deployment submitted for QR Code {}", qrCode.getQrCodeSlug());
            return updated;
        } catch (Exception e) {
            logger.error("❌ Failed to submit deployment {}: {}", qrCodeId, e.getMessage(), e);
            throw new RuntimeException("Failed to submit deployment: " + e.getMessage());
        }
    }

    /**
     * Approve deployment
     */
    public QRCode approveDeployment(Long qrCodeId, Long adminId) {
        try {
            QRCode qrCode = findById(qrCodeId);
            if (qrCode == null) {
                throw new RuntimeException("QR Code not found");
            }

            if (!"PENDING_APPROVAL".equals(qrCode.getDeploymentStatus())) {
                throw new RuntimeException("Deployment not pending approval");
            }

            qrCode.setDeploymentStatus("APPROVED");
            qrCode.setIsDeployed(true);
            qrCode.setFinalizedBy(adminId);
            qrCode.setFinalizedAt(LocalDateTime.now());

            QRCode updated = em.merge(qrCode);
            em.flush();

            logger.info("✅ Deployment approved for QR Code {} by admin {}", qrCode.getQrCodeSlug(), adminId);
            return updated;
        } catch (Exception e) {
            logger.error("❌ Failed to approve deployment {}: {}", qrCodeId, e.getMessage(), e);
            throw new RuntimeException("Failed to approve deployment: " + e.getMessage());
        }
    }

    /**
     * Reject deployment
     */
    public QRCode rejectDeployment(Long qrCodeId, Long adminId, String reason) {
        try {
            QRCode qrCode = findById(qrCodeId);
            if (qrCode == null) {
                throw new RuntimeException("QR Code not found");
            }

            if (!"PENDING_APPROVAL".equals(qrCode.getDeploymentStatus())) {
                throw new RuntimeException("Deployment not pending approval");
            }

            qrCode.setDeploymentStatus("REJECTED");
            qrCode.setRejectionReason(reason);

            QRCode updated = em.merge(qrCode);
            em.flush();

            logger.info("✅ Deployment rejected for QR Code {} by admin {}: {}", qrCode.getQrCodeSlug(), adminId, reason);
            return updated;
        } catch (Exception e) {
            logger.error("❌ Failed to reject deployment {}: {}", qrCodeId, e.getMessage(), e);
            throw new RuntimeException("Failed to reject deployment: " + e.getMessage());
        }
    }

    /**
     * Increment scan count
     */
    public void incrementScanCount(Long qrCodeId) {
        try {
            QRCode qrCode = findById(qrCodeId);
            if (qrCode != null) {
                qrCode.setTotalScans(qrCode.getTotalScans() + 1);
                qrCode.setLastScannedAt(LocalDateTime.now());
                em.merge(qrCode);
                em.flush();
            }
        } catch (Exception e) {
            logger.error("❌ Failed to increment scan count for QR {}: {}", qrCodeId, e.getMessage());
        }
    }

    /**
     * Increment response count
     */
    public void incrementResponseCount(Long qrCodeId) {
        try {
            QRCode qrCode = findById(qrCodeId);
            if (qrCode != null) {
                qrCode.setTotalResponses(qrCode.getTotalResponses() + 1);
                em.merge(qrCode);
                em.flush();
            }
        } catch (Exception e) {
            logger.error("❌ Failed to increment response count for QR {}: {}", qrCodeId, e.getMessage());
        }
    }

    /**
     * Update status (ACTIVE/INACTIVE)
     */
    public QRCode updateStatus(Long qrCodeId, String status) {
        try {
            QRCode qrCode = findById(qrCodeId);
            if (qrCode == null) {
                throw new RuntimeException("QR Code not found");
            }

            qrCode.setStatus(status);
            QRCode updated = em.merge(qrCode);
            em.flush();

            logger.info("✅ QR Code {} status changed to {}", qrCode.getQrCodeSlug(), status);
            return updated;
        } catch (Exception e) {
            logger.error("❌ Failed to update status for QR {}: {}", qrCodeId, e.getMessage(), e);
            throw new RuntimeException("Failed to update status: " + e.getMessage());
        }
    }

    // Inner class for deployment data
    public static class DeploymentData {

        public String locationName;
        public java.math.BigDecimal latitude;
        public java.math.BigDecimal longitude;
        public String notes;
        public String photoUrl;
    }
}
