package com.shakilpatel.qr.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public class CreateQRRequest implements Serializable {

    @NotBlank(message = "Form ID is required")
    @JsonbProperty("formId")
    private String formId;

    @NotNull(message = "Service ID is required")
    @JsonbProperty("serviceId")
    private Long serviceId;

    @NotNull(message = "Organization ID is required")
    @JsonbProperty("orgId")
    private Long orgId;

    @NotBlank(message = "QR Code name is required")
    @JsonbProperty("qrCodeName")
    private String qrCodeName;

    @JsonbProperty("paymentAmount")
    private BigDecimal paymentAmount = BigDecimal.ZERO;

    // GETTERS AND SETTERS
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
}
