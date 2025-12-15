package com.shakilpatel.qr.ejb;

import com.shakilpatel.qr.entity.QRMessage;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class QRMessageEJB {

    private static final Logger logger = LoggerFactory.getLogger(QRMessageEJB.class);

    @PersistenceContext(unitName = "qrPU")
    private EntityManager em;

    /**
     * Send message
     */
    public QRMessage sendMessage(QRMessage message) {
        try {
            em.persist(message);
            em.flush();
            logger.info("✅ Message sent for QR Code ID: {}", message.getQrCodeId());
            return message;
        } catch (Exception e) {
            logger.error("❌ Failed to send message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Get all messages for QR code (conversation thread)
     */
    public List<QRMessage> getMessages(Long qrCodeId) {
        try {
            TypedQuery<QRMessage> query = em.createNamedQuery("QRMessage.findByQRCodeId", QRMessage.class);
            query.setParameter("qrCodeId", qrCodeId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("❌ Error fetching messages for QR {}: {}", qrCodeId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Mark message as read
     */
    public void markAsRead(Long messageId) {
        try {
            QRMessage message = em.find(QRMessage.class, messageId);
            if (message != null && !message.getIsRead()) {
                message.setIsRead(true);
                message.setReadAt(LocalDateTime.now());
                em.merge(message);
                em.flush();
                logger.info("✅ Message {} marked as read", messageId);
            }
        } catch (Exception e) {
            logger.error("❌ Failed to mark message as read {}: {}", messageId, e.getMessage());
        }
    }

    /**
     * Count unread messages
     */
    public Long countUnread(Long qrCodeId) {
        try {
            TypedQuery<Long> query = em.createNamedQuery("QRMessage.countUnread", Long.class);
            query.setParameter("qrCodeId", qrCodeId);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("❌ Error counting unread messages: {}", e.getMessage());
            return 0L;
        }
    }
}
