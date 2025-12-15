package com.shakilpatel.qr.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.math.BigDecimal;

public class UpdateQRRequest implements Serializable {

    @JsonbProperty("qrCodeName")
    private String qrCodeName;

    @JsonbProperty("paymentAmount")
    private BigDecimal paymentAmount;

    @JsonbProperty("status")
    private String status; // ACTIVE, INACTIVE

    // GETTERS AND SETTERS
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
