package com.shakilpatel.qr.util;

import java.security.SecureRandom;
import java.util.Random;

public class SlugGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SLUG_LENGTH = 8;
    private static final Random RANDOM = new SecureRandom();

    /**
     * Generate unique QR code slug: QR_ABC12345
     */
    public static String generateQRSlug() {
        StringBuilder sb = new StringBuilder("QR_");
        for (int i = 0; i < SLUG_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * Generate full QR URL
     */
    public static String generateQRUrl(String slug) {
        return "https://bolo.in/" + slug;
    }
}
