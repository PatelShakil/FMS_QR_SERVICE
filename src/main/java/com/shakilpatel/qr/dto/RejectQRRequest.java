package com.shakilpatel.qr.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class RejectQRRequest implements Serializable {

    @NotBlank(message = "Rejection reason is required")
    @JsonbProperty("reason")
    private String reason;

    // GETTERS AND SETTERS
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
