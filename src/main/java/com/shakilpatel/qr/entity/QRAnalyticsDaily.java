package com.shakilpatel.qr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "qr_analytics_daily", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"qr_code_id", "date"})
})
@NamedQueries({
    @NamedQuery(name = "QRAnalyticsDaily.findByQRCodeIdAndDateRange",
            query = "SELECT a FROM QRAnalyticsDaily a WHERE a.qrCodeId = :qrCodeId AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date ASC")
})
public class QRAnalyticsDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analytics_id")
    private Long analyticsId;

    @NotNull
    @Column(name = "qr_code_id", nullable = false)
    private Long qrCodeId;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_scans")
    private Integer totalScans = 0;

    @Column(name = "total_responses")
    private Integer totalResponses = 0;

    @Column(name = "unique_users")
    private Integer uniqueUsers = 0;

    // Device Breakdown
    @Column(name = "mobile_scans")
    private Integer mobileScans = 0;

    @Column(name = "tablet_scans")
    private Integer tabletScans = 0;

    @Column(name = "desktop_scans")
    private Integer desktopScans = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // GETTERS AND SETTERS
    public Long getAnalyticsId() {
        return analyticsId;
    }

    public void setAnalyticsId(Long analyticsId) {
        this.analyticsId = analyticsId;
    }

    public Long getQrCodeId() {
        return qrCodeId;
    }

    public void setQrCodeId(Long qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public Integer getUniqueUsers() {
        return uniqueUsers;
    }

    public void setUniqueUsers(Integer uniqueUsers) {
        this.uniqueUsers = uniqueUsers;
    }

    public Integer getMobileScans() {
        return mobileScans;
    }

    public void setMobileScans(Integer mobileScans) {
        this.mobileScans = mobileScans;
    }

    public Integer getTabletScans() {
        return tabletScans;
    }

    public void setTabletScans(Integer tabletScans) {
        this.tabletScans = tabletScans;
    }

    public Integer getDesktopScans() {
        return desktopScans;
    }

    public void setDesktopScans(Integer desktopScans) {
        this.desktopScans = desktopScans;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
