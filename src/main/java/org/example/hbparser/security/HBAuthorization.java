package org.example.hbparser.security;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@RequiredArgsConstructor
public class HBAuthorization {

    private final Dotenv dotenv;

    public String getXSignature() {
        String publicKey = dotenv.get("HOTELBEDS_API_KEY");
        String secretKey = dotenv.get("HOTELBEDS_SECRET_KEY");

        log.info(publicKey + " Public key " + secretKey + " Secret key");

        long utcDate = System.currentTimeMillis() / 1000;
        String assemble = publicKey + secretKey + utcDate;

        String xSignature = calculateSHA256(assemble);

        log.info("Generated X-Signature: {}", xSignature);

        return xSignature;
    }

    public String getApiKey() {
        String publicKey = dotenv.get("HOTELBEDS_API_KEY");
        log.info("Using API Key: {}", publicKey);
        return publicKey;
    }

    private String calculateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // Convert byte array to a string representation
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            return null;
        }
    }
}