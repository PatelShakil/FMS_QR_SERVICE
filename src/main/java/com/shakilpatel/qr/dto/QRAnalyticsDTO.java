package com.shakilpatel.qr.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class QRAnalyticsDTO implements Serializable {

    @JsonbProperty("qrCodeId")
    private Long qrCodeId;

    @JsonbProperty("totalScans")
    private Integer totalScans;

    @JsonbProperty("totalResponses")
    private Integer totalResponses;

    @JsonbProperty("responseRate")
    private Double responseRate;

    @JsonbProperty("lastScannedAt")
    private LocalDateTime lastScannedAt;

    @JsonbProperty("scansByDevice")
    private Map<String, Integer> scansByDevice;

    @JsonbProperty("scanTimeline")
    private List<DailyScanData> scanTimeline;

    // GETTERS AND SETTERS
    public Long getQrCodeId() {
        return qrCodeId;
    }

    public void setQrCodeId(Long qrCodeId) {
        this.qrCodeId = qrCodeId;
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

    public Double getResponseRate() {
        return responseRate;
    }

    public void setResponseRate(Double responseRate) {
        this.responseRate = responseRate;
    }

    public LocalDateTime getLastScannedAt() {
        return lastScannedAt;
    }

    public void setLastScannedAt(LocalDateTime lastScannedAt) {
        this.lastScannedAt = lastScannedAt;
    }

    public Map<String, Integer> getScansByDevice() {
        return scansByDevice;
    }

    public void setScansByDevice(Map<String, Integer> scansByDevice) {
        this.scansByDevice = scansByDevice;
    }

    public List<DailyScanData> getScanTimeline() {
        return scanTimeline;
    }

    public void setScanTimeline(List<DailyScanData> scanTimeline) {
        this.scanTimeline = scanTimeline;
    }

    // Inner class for timeline data
    public static class DailyScanData implements Serializable {

        @JsonbProperty("date")
        private String date;

        @JsonbProperty("scans")
        private Integer scans;

        @JsonbProperty("responses")
        private Integer responses;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Integer getScans() {
            return scans;
        }

        public void setScans(Integer scans) {
            this.scans = scans;
        }

        public Integer getResponses() {
            return responses;
        }

        public void setResponses(Integer responses) {
            this.responses = responses;
        }
    }
}
