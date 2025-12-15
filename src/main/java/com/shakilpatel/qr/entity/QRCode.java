package com.shakilpatel.qr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "qr_codes")
@NamedQueries({
    @NamedQuery(name = "QRCode.findBySlug",
            query = "SELECT q FROM QRCode q WHERE q.qrCodeSlug = :slug"),
    @NamedQuery(name = "QRCode.findByFormId",
            query = "SELECT q FROM QRCode q WHERE q.formId = :formId ORDER BY q.createdAt DESC"),
    @NamedQuery(name = "QRCode.findByOrgId",
            query = "SELECT q FROM QRCode q WHERE q.orgId = :orgId ORDER BY q.createdAt DESC"),
    @NamedQuery(name = "QRCode.findByAssignedTo",
            query = "SELECT q FROM QRCode q WHERE q.assignedTo = :distributorId ORDER BY q.createdAt DESC"),
    @NamedQuery(name = "QRCode.findPendingAssignments",
            query = "SELECT q FROM QRCode q WHERE q.orgId = :orgId AND q.assignmentStatus = 'PENDING' ORDER BY q.assignedAt DESC")
})
public class QRCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qr_code_id")
    private Long qrCodeId;

    // IMMUTABLE FIELDS
    @NotNull
    @Column(name = "qr_code_slug", unique = true, nullable = false, length = 50, updatable = false)
    private String qrCodeSlug;

    @NotNull
    @Column(name = "qr_code_url", nullable = false, updatable = false)
    private String qrCodeUrl;

    @NotNull
    @Column(name = "form_id", nullable = false, length = 50, updatable = false)
    private String formId;

    // EDITABLE FIELDS
    @NotNull
    @Column(name = "qr_code_name", nullable = false)
    private String qrCodeName;

    @Column(name = "payment_amount", precision = 10, scale = 2)
    private BigDecimal paymentAmount = BigDecimal.ZERO;

    // RELATIONSHIPS (IDs only - MSA pattern)
    @NotNull
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @NotNull
    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    // ASSIGNMENT
    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "assignment_status", length = 20)
    private String assignmentStatus = "UNASSIGNED"; // UNASSIGNED, PENDING, ACCEPTED, REJECTED

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    // DEPLOYMENT
    @Column(name = "is_deployed")
    private Boolean isDeployed = false;

    @Column(name = "deployment_status", length = 20)
    private String deploymentStatus; // NULL, PENDING_APPROVAL, APPROVED, REJECTED

    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;

    @Column(name = "deployment_location_name")
    private String deploymentLocationName;

    @Column(name = "deployment_latitude", precision = 10, scale = 8)
    private BigDecimal deploymentLatitude;

    @Column(name = "deployment_longitude", precision = 11, scale = 8)
    private BigDecimal deploymentLongitude;

    @Column(name = "deployment_notes")
    private String deploymentNotes;

    @Column(name = "deployment_photo_url")
    private String deploymentPhotoUrl;

    @Column(name = "finalized_by")
    private Long finalizedBy;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    // STATUS
    @NotNull
    @Column(name = "status", length = 20, nullable = false)
    private String status = "GENERATED"; //GENERATED, DEPLOYED, ACTIVE, INACTIVE, ARCHIVED

    // ANALYTICS
    @Column(name = "total_scans")
    private Integer totalScans = 0;

    @Column(name = "total_responses")
    private Integer totalResponses = 0;

    @Column(name = "last_scanned_at")
    private LocalDateTime lastScannedAt;

    // TIMESTAMPS
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // GETTERS AND SETTERS
    public Long getQrCodeId() {
        return qrCodeId;
    }

    public void setQrCodeId(Long qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    public String getQrCodeSlug() {
        return qrCodeSlug;
    }

    public void setQrCodeSlug(String qrCodeSlug) {
        this.qrCodeSlug = qrCodeSlug;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getQrCodeName() {
        return qrCodeName;
    }

    public void setQrCodeName(String qrCodeName) {
        this.qrCodeName = qrCodeName;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignmentStatus() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(String assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Boolean getIsDeployed() {
        return isDeployed;
    }

    public void setIsDeployed(Boolean deployed) {
        isDeployed = deployed;
    }

    public String getDeploymentStatus() {
        return deploymentStatus;
    }

    public void setDeploymentStatus(String deploymentStatus) {
        this.deploymentStatus = deploymentStatus;
    }

    public LocalDateTime getDeployedAt() {
        return deployedAt;
    }

    public void setDeployedAt(LocalDateTime deployedAt) {
        this.deployedAt = deployedAt;
    }

    public String getDeploymentLocationName() {
        return deploymentLocationName;
    }

    public void setDeploymentLocationName(String deploymentLocationName) {
        this.deploymentLocationName = deploymentLocationName;
    }

    public BigDecimal getDeploymentLatitude() {
        return deploymentLatitude;
    }

    public void setDeploymentLatitude(BigDecimal deploymentLatitude) {
        this.deploymentLatitude = deploymentLatitude;
    }

    public BigDecimal getDeploymentLongitude() {
        return deploymentLongitude;
    }

    public void setDeploymentLongitude(BigDecimal deploymentLongitude) {
        this.deploymentLongitude = deploymentLongitude;
    }

    public String getDeploymentNotes() {
        return deploymentNotes;
    }

    public void setDeploymentNotes(String deploymentNotes) {
        this.deploymentNotes = deploymentNotes;
    }

    public String getDeploymentPhotoUrl() {
        return deploymentPhotoUrl;
    }

    public void setDeploymentPhotoUrl(String deploymentPhotoUrl) {
        this.deploymentPhotoUrl = deploymentPhotoUrl;
    }

    public Long getFinalizedBy() {
        return finalizedBy;
    }

    public void setFinalizedBy(Long finalizedBy) {
        this.finalizedBy = finalizedBy;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalScans() {
        return totalScans;
    }

    public void setTotalScans(Integer totalScans) {
        this.totalScans = totalScans;
    }

    public Integer getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(Integer totalResponses) {
        this.totalResponses = totalResponses;
    }

    public LocalDateTime getLastScannedAt() {
        return lastScannedAt;
    }

    public void setLastScannedAt(LocalDateTime lastScannedAt) {
        this.lastScannedAt = lastScannedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
