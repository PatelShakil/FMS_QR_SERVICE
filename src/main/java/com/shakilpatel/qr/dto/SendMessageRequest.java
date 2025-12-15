package com.shakilpatel.qr.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class SendMessageRequest implements Serializable {

    @NotBlank(message = "Message text is required")
    @JsonbProperty("message")
    private String message;

    // GETTERS AND SETTERS
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
