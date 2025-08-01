/*
 * #%L
 * Wattpilot4j
 * %%
 * Copyright (C) 2025 Florian Hotze
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package dev.digiried.wattpilot;

import dev.digiried.wattpilot.messages.AuthMessage;
import dev.digiried.wattpilot.messages.AuthRequiredMessage;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class implementing the authentication mechanism both for establishing the connection and
 * authenticating messages.
 *
 * @author Florian Hotze - Initial contribution
 */
final class AuthUtil {
    private AuthUtil() {}

    /**
     * Hashes the password using PBKDF2 with HMAC-SHA512, encodes it with Bas64 and truncates to 32
     * bytes.
     *
     * @param serial the serial number of the device
     * @param password the password to hash
     * @return the hashed, truncated password
     * @throws NoSuchAlgorithmException if PBKDF2WithHmacSHA512 key algorithm is not available
     */
    static byte[] hashPassword(String serial, String password) throws NoSuchAlgorithmException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec spec =
                new PBEKeySpec(
                        password.toCharArray(),
                        serial.getBytes(StandardCharsets.UTF_8),
                        100000,
                        256 * 8);
        byte[] hash = null;
        try {
            hash = factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e); // NOSONAR: this should never happen
        }
        String base64 = Base64.getEncoder().encodeToString(hash).substring(0, 32);
        return base64.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Creates an AuthMessage from the hashed password and the tokens provided by the wallbox.
     *
     * @param hashedPassword the hashed password, see {@link #hashPassword(String, String)}
     * @param token1 the first token provided by the wallbox, see {@link AuthRequiredMessage}
     * @param token2 the second token provided by the wallbox, see {@link AuthRequiredMessage}
     * @return the AuthMessage to send to the wallbox
     * @throws NoSuchAlgorithmException if SHA-256 hash algorithm is not available
     */
    static AuthMessage createAuthMessage(byte[] hashedPassword, String token1, String token2)
            throws NoSuchAlgorithmException {
        // Generate a random 80-digit number
        SecureRandom random = new SecureRandom();
        BigInteger bigRandom = new BigInteger(80 * 3, random); // 80 digits = ~265 bits

        // Format as a 64-char hex and truncate to 32 chars
        String token3 = String.format("%064x", bigRandom).substring(0, 32);

        // Create first hash: SHA-256 of token1 + hashedPassword
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(token1.getBytes(StandardCharsets.UTF_8));
        digest.update(hashedPassword);
        String hash1 = bytesToHex(digest.digest());

        // Create second hash: SHA-256 of token3 + token2 + hash1
        digest.reset();
        digest.update(token3.getBytes(StandardCharsets.UTF_8));
        digest.update(token2.getBytes(StandardCharsets.UTF_8));
        digest.update(hash1.getBytes(StandardCharsets.UTF_8));
        String hash = bytesToHex(digest.digest());

        return new AuthMessage(token3, hash);
    }

    /**
     * Creates a HMAC from the hashed password and the data to send.
     *
     * @param hashedPassword the hashed password, see {@link #hashPassword(String, String)}
     * @param data the data to send
     * @return the HMAC as a hex string
     * @throws NoSuchAlgorithmException ir HMAC-SHA256 algorithm is not available
     */
    static String createHmac(byte[] hashedPassword, String data) throws NoSuchAlgorithmException {
        SecretKeySpec keySpec = new SecretKeySpec(hashedPassword, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        try {
            mac.init(keySpec);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e); // NOSONAR: this should never happen
        }
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmacBytes);
    }

    /**
     * Converts a byte array to a hex string.
     *
     * @param bytes the byte array to convert
     * @return the hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
