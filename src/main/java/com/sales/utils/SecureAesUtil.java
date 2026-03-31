package com.sales.utils;

import com.sales.exceptions.MyException;
import com.sales.global.ResponseMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureAesUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecureAesUtil.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;      // 96 bits is standard
    private static final int GCM_TAG_LENGTH = 128;    // 16 bytes
    private SecureAesUtil() {
    }

    public static String encrypt(String plaintext, String secretKey) {
        try {
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            validateKeyLength(keyBytes);

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext → [IV (12) | encrypted + tag]
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            // In real code: throw checked exception or use Result type
            logger.error("Exception during encrypt : {}", e.getMessage());
            throw new MyException(ResponseMessages.ENCRYPTION_FAILED);
        }
    }

    public static String decrypt(String base64Ciphertext, String secretKey) {
        try {
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            validateKeyLength(keyBytes);

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            byte[] decoded = Base64.getDecoder().decode(base64Ciphertext);

            // GCM_TAG_LENGTH is in bits (128), convert to bytes for length check
            if (decoded.length < GCM_IV_LENGTH + (GCM_TAG_LENGTH / 8)) {
                throw new IllegalArgumentException(ResponseMessages.CIPHERTEXT_TOO_SHORT);
            }

            // Extract IV (first 12 bytes)
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, GCM_IV_LENGTH);

            // Rest is ciphertext + tag
            byte[] cipherData = new byte[decoded.length - GCM_IV_LENGTH];
            System.arraycopy(decoded, GCM_IV_LENGTH, cipherData, 0, cipherData.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] decrypted = cipher.doFinal(cipherData);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            logger.error("Exception during decrypt : {}", e.getMessage());
            throw new MyException(ResponseMessages.DECRYPTION_FAILED);
        }
    }

    private static void validateKeyLength(byte[] keyBytes) {
        int len = keyBytes.length;
        if (len != 16 && len != 24 && len != 32) {
            throw new IllegalArgumentException(
                    "Invalid AES key length: " + len + " bytes. Must be 16, 24 or 32 bytes.");
        }
    }
}