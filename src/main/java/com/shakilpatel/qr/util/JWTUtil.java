package com.shakilpatel.qr.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@ApplicationScoped
public class JWTUtil {

    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    private Algorithm algorithm;
    private JWTVerifier verifier;

    @PostConstruct
    public void init() {
        try {
            logger.info("Loading JWT public key...");

            InputStream is = getClass().getClassLoader().getResourceAsStream("keys/publicKey.pem");
            if (is == null) {
                throw new RuntimeException("publicKey.pem not found in resources/keys/");
            }

            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(spec);

            this.algorithm = Algorithm.RSA256(publicKey, null);
            this.verifier = JWT.require(algorithm)
                    .withIssuer("auth-service")
                    .withAudience("boloindia-services")
                    .build();

            logger.info("✅ JWT public key loaded successfully");
        } catch (Exception e) {
            logger.error("❌ Failed to load JWT public key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize JWT", e);
        }
    }

    public DecodedJWT verifyToken(String token) throws Exception {
        try {
            return verifier.verify(token);
        } catch (Exception e) {
            logger.error("JWT verification failed: {}", e.getMessage());
            throw new Exception("Invalid or expired token");
        }
    }

    public Long getUserId(DecodedJWT jwt) {
        try {
            String sub = jwt.getSubject();
            return Long.parseLong(sub);
        } catch (Exception e) {
            logger.error("Failed to parse userId from JWT: {}", e.getMessage());
            return null;
        }
    }

    public String getEmail(DecodedJWT jwt) {
        return jwt.getClaim("email").asString();
    }

    public String getName(DecodedJWT jwt) {
        return jwt.getClaim("name").asString();
    }

    /**
     * NULL-SAFE: Returns null if orgId is empty string or null
     */
    public Long getOrgId(DecodedJWT jwt) {
        try {
            String orgIdStr = jwt.getClaim("orgId").asString();

            // Handle empty string (PLATFORM_ADMIN case)
            if (orgIdStr == null || orgIdStr.trim().isEmpty()) {
                return null;
            }

            return Long.parseLong(orgIdStr);
        } catch (Exception e) {
            logger.debug("orgId not present or invalid: {}", e.getMessage());
            return null;
        }
    }

    public List<String> getRoles(DecodedJWT jwt) {
        return jwt.getClaim("roles").asList(String.class);
    }

    public boolean hasRole(DecodedJWT jwt, String role) {
        List<String> roles = getRoles(jwt);
        return roles != null && roles.contains(role);
    }

    public boolean isPlatformAdmin(DecodedJWT jwt) {
        return hasRole(jwt, "PLATFORM_ADMIN");
    }

    public boolean isOrgAdmin(DecodedJWT jwt) {
        return hasRole(jwt, "ORG_ADMIN") || hasRole(jwt, "ORG_OWNER") || isPlatformAdmin(jwt);
    }

    public boolean isDistributor(DecodedJWT jwt) {
        return hasRole(jwt, "DISTRIBUTOR");
    }
}
