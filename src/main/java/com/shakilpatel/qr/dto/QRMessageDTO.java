package com.shakilpatel.qr.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.time.LocalDateTime;

public class QRMessageDTO implements Serializable {

    @JsonbProperty("messageId")
    private Long messageId;

    @JsonbProperty("qrCodeId")
    private Long qrCodeId;

    @JsonbProperty("senderId")
    private Long senderId;

    @JsonbProperty("senderType")
    private String senderType;

    @JsonbProperty("messageText")
    private String messageText;

    @JsonbProperty("isRead")
    private Boolean isRead;

    @JsonbProperty("readAt")
    private LocalDateTime readAt;

    @JsonbProperty("createdAt")
    private LocalDateTime createdAt;

    // GETTERS AND SETTERS
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getQrCodeId() {
        return qrCodeId;
    }

    public void setQrCodeId(Long qrCodeId) {
        this.qrCodeId = qrCodeId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean read) {
        isRead = read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
