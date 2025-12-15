package com.shakilpatel.qr.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class AssignQRRequest implements Serializable {

    @NotNull(message = "Distributor ID is required")
    @JsonbProperty("distributorId")
    private Long distributorId;

    @JsonbProperty("distributorEmail")
    private String distributorEmail;

    @JsonbProperty("notes")
    private String notes;

    // GETTERS AND SETTERS
    public Long getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(Long distributorId) {
        this.distributorId = distributorId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDistributorEmail() {
        return distributorEmail;
    }

    public void setDistributorEmail(String distributorEmail) {
        this.distributorEmail = distributorEmail;
    }
}
