package com.shakilpatel.qr.ejb;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Stateless
public class MinIOEJB {

    private static final Logger logger = LoggerFactory.getLogger(MinIOEJB.class);

    private MinioClient minioClient;
    private String bucketName = "qr-deployments";
    private boolean minioEnabled = true;

    @PostConstruct
    public void init() {
        try {
            String minioEndpoint = System.getenv("MINIO_ENDPOINT");
            String minioAccessKey = System.getenv("MINIO_ACCESS_KEY");
            String minioSecretKey = System.getenv("MINIO_SECRET_KEY");

            if (minioEndpoint == null || minioEndpoint.isEmpty()) {
                minioEndpoint = "http://localhost:9000";
            }
            if (minioAccessKey == null || minioAccessKey.isEmpty()) {
                minioAccessKey = "boloindia";
            }
            if (minioSecretKey == null || minioSecretKey.isEmpty()) {
                minioSecretKey = "boloindiapass123";
            }

//            String minioEnabledEnv = System.getenv("MINIO_ENABLED");
//            String minioEnabledEnv = "true";
//            if ("false".equalsIgnoreCase(minioEnabledEnv)) {
//                minioEnabled = false;
//                logger.warn("⚠️ MinIO is DISABLED via environment variable");
//                return;
//            }

            this.minioClient = MinioClient.builder()
                    .endpoint(minioEndpoint)
                    .credentials(minioAccessKey, minioSecretKey)
                    .build();

            // Create bucket if not exists
            createBucketIfNotExists();

            logger.info("✅ MinIO client initialized: {}", minioEndpoint);
        } catch (Exception e) {
            logger.error("❌ Failed to initialize MinIO: {}", e.getMessage(), e);
            minioEnabled = false;
        }
    }

    /**
     * Create bucket if it doesn't exist
     */
    private void createBucketIfNotExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                logger.info("✅ Bucket created: {}", bucketName);
            }
        } catch (Exception e) {
            logger.error("❌ Error creating bucket: {}", e.getMessage());
        }
    }

    /**
     * Upload file to MinIO
     */
    public String uploadFile(byte[] fileData, String fileName, String contentType) {
        if (!minioEnabled || minioClient == null) {
            logger.warn("MinIO disabled, cannot upload file: {}", fileName);
            return null;
        }

        try {
            // Generate unique object name
            String objectName = generateObjectName(fileName);

            // Upload file
            InputStream inputStream = new ByteArrayInputStream(fileData);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, fileData.length, -1)
                            .contentType(contentType)
                            .build()
            );

            // Generate public URL (adjust based on your MinIO setup)
            String fileUrl = String.format("%s/%s/%s",
                    System.getenv("MINIO_ENDPOINT") != null ? System.getenv("MINIO_ENDPOINT") : "http://localhost:9000",
                    bucketName,
                    objectName);

            logger.info("✅ File uploaded to MinIO: {}", objectName);
            return fileUrl;

        } catch (Exception e) {
            logger.error("❌ Failed to upload file to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Upload deployment photo
     */
    public String uploadDeploymentPhoto(byte[] imageData, String qrCodeSlug) {
        String fileName = String.format("deployment_%s.jpg", qrCodeSlug);
        return uploadFile(imageData, fileName, "image/jpeg");
    }

    /**
     * Generate unique object name with timestamp
     */
    private String generateObjectName(String fileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileExtension = "";

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = fileName.substring(dotIndex);
            fileName = fileName.substring(0, dotIndex);
        }

        // Sanitize filename
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9_-]", "_");

        return String.format("%s_%s%s", sanitized, timestamp, fileExtension);
    }

    /**
     * Delete file from MinIO
     */
    public void deleteFile(String objectName) {
        if (!minioEnabled || minioClient == null) {
            return;
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            logger.info("✅ File deleted from MinIO: {}", objectName);
        } catch (Exception e) {
            logger.error("❌ Failed to delete file from MinIO: {}", e.getMessage());
        }
    }

    /**
     * Get file URL (for already uploaded files)
     */
    public String getFileUrl(String objectName) {
        return String.format("%s/%s/%s",
                System.getenv("MINIO_ENDPOINT") != null ? System.getenv("MINIO_ENDPOINT") : "http://localhost:9000",
                bucketName,
                objectName);
    }

    /**
     * Generate presigned URL (temporary access)
     */
    public String getPresignedUrl(String objectName, int expirySeconds) {
        if (!minioEnabled || minioClient == null) {
            return null;
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expirySeconds)
                            .build()
            );
        } catch (Exception e) {
            logger.error("❌ Failed to generate presigned URL: {}", e.getMessage());
            return null;
        }
    }
}
