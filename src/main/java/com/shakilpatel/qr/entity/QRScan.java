package com.shakilpatel.qr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "qr_scans")
@NamedQueries({
    @NamedQuery(name = "QRScan.findByQRCodeId",
            query = "SELECT s FROM QRScan s WHERE s.qrCodeId = :qrCodeId ORDER BY s.scannedAt DESC"),
    @NamedQuery(name = "QRScan.countByQRCodeId",
            query = "SELECT COUNT(s) FROM QRScan s WHERE s.qrCodeId = :qrCodeId"),
    @NamedQuery(name = "QRScan.findByDateRange",
            query = "SELECT s FROM QRScan s WHERE s.qrCodeId = :qrCodeId AND s.scannedAt BETWEEN :startDate AND :endDate ORDER BY s.scannedAt DESC")
})
public class QRScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scan_id")
    private Long scanId;

    @NotNull
    @Column(name = "qr_code_id", nullable = false)
    private Long qrCodeId;

    @NotNull
    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt;

    @Column(name = "scanned_by")
    private Long scannedBy; // NULL if anonymous

    // Device Info
    @Column(name = "device_type", length = 50)
    private String deviceType; // MOBILE, TABLET, DESKTOP

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // Location
    @Column(name = "scan_latitude", precision = 10, scale = 8)
    private BigDecimal scanLatitude;

    @Column(name = "scan_longitude", precision = 11, scale = 8)
    private BigDecimal scanLongitude;

    // Response Tracking
    @Column(name = "did_submit_response")
    private Boolean didSubmitResponse = false;

    @Column(name = "response_id", length = 50)
    private String responseId;

    @Column(name = "response_submitted_at")
    private LocalDateTime responseSubmittedAt;

    @PrePersist
    protected void onCreate() {
        if (scannedAt == null) {
            scannedAt = LocalDateTime.now();
        }
    }

    // GETTERS AND SETTERS
    public Long getScanId() {
        return scanId;
    }

    public void setScanId(Long scanId) {
        this.scanId = scanId;
    }

    public Long getQrCodeId() {
        return qrCodeId;
    }

    public void setQrCodeId(Long qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public Long getScannedBy() {
        return scannedBy;
    }

    public void setScannedBy(Long scannedBy) {
        this.scannedBy = scannedBy;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public BigDecimal getScanLatitude() {
        return scanLatitude;
    }

    public void setScanLatitude(BigDecimal scanLatitude) {
        this.scanLatitude = scanLatitude;
    }

    public BigDecimal getScanLongitude() {
        return scanLongitude;
    }

    public void setScanLongitude(BigDecimal scanLongitude) {
        this.scanLongitude = scanLongitude;
    }

    public Boolean getDidSubmitResponse() {
        return didSubmitResponse;
    }

    public void setDidSubmitResponse(Boolean didSubmitResponse) {
        this.didSubmitResponse = didSubmitResponse;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public LocalDateTime getResponseSubmittedAt() {
        return responseSubmittedAt;
    }

    public void setResponseSubmittedAt(LocalDateTime responseSubmittedAt) {
        this.responseSubmittedAt = responseSubmittedAt;
    }
}
