package com.shakilpatel.qr.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QRCodeDTO implements Serializable {

    @JsonbProperty("qrCodeId")
    private Long qrCodeId;

    @JsonbProperty("qrCodeSlug")
    private String qrCodeSlug;

    @JsonbProperty("qrCodeUrl")
    private String qrCodeUrl;

    @JsonbProperty("qrCodeName")
    private String qrCodeName;

    @JsonbProperty("formId")
    private String formId;

    @JsonbProperty("serviceId")
    private Long serviceId;

    @JsonbProperty("orgId")
    private Long orgId;

    @JsonbProperty("createdBy")
    private Long createdBy;

    @JsonbProperty("paymentAmount")
    private BigDecimal paymentAmount;

    @JsonbProperty("assignedTo")
    private Long assignedTo;

    @JsonbProperty("assignmentStatus")
    private String assignmentStatus;

    @JsonbProperty("assignedAt")
    private LocalDateTime assignedAt;

    @JsonbProperty("acceptedAt")
    private LocalDateTime acceptedAt;

    @JsonbProperty("isDeployed")
    private Boolean isDeployed;

    @JsonbProperty("deploymentStatus")
    private String deploymentStatus;

    @JsonbProperty("deployedAt")
    private LocalDateTime deployedAt;

    @JsonbProperty("deploymentLocationName")
    private String deploymentLocationName;

    @JsonbProperty("deploymentLatitude")
    private BigDecimal deploymentLatitude;

    @JsonbProperty("deploymentLongitude")
    private BigDecimal deploymentLongitude;

    @JsonbProperty("deploymentPhotoUrl")
    private String deploymentPhotoUrl;

    @JsonbProperty("status")
    private String status;

    @JsonbProperty("totalScans")
    private Integer totalScans;

    @JsonbProperty("totalResponses")
    private Integer totalResponses;

    @JsonbProperty("lastScannedAt")
    private LocalDateTime lastScannedAt;

    @JsonbProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonbProperty("updatedAt")
    private LocalDateTime updatedAt;

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

    public String getQrCodeName() {
        return qrCodeName;
    }

    public void setQrCodeName(String qrCodeName) {
        this.qrCodeName = qrCodeName;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
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

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
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

    public String getDeploymentPhotoUrl() {
        return deploymentPhotoUrl;
    }

    public void setDeploymentPhotoUrl(String deploymentPhotoUrl) {
        this.deploymentPhotoUrl = deploymentPhotoUrl;
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
