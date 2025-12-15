package com.shakilpatel.qr.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public class DeployQRRequest implements Serializable {

    @NotNull(message = "Latitude is required")
    @JsonbProperty("latitude")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @JsonbProperty("longitude")
    private BigDecimal longitude;

    @NotBlank(message = "Location name is required")
    @JsonbProperty("locationName")
    private String locationName;

    @JsonbProperty("notes")
    private String notes;

    @JsonbProperty("deploymentPhotoUrl")
    private String deploymentPhotoUrl;

    // GETTERS AND SETTERS
    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDeploymentPhotoUrl() {
        return deploymentPhotoUrl;
    }

    public void setDeploymentPhotoUrl(String deploymentPhotoUrl) {
        this.deploymentPhotoUrl = deploymentPhotoUrl;
    }
}
