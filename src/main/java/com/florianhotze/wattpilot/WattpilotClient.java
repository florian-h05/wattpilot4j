/*
 * #%L
 * wattpilot4j
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
package com.florianhotze.wattpilot;

import com.florianhotze.wattpilot.dto.DeviceInfo;
import com.florianhotze.wattpilot.messages.AuthErrorMessage;
import com.florianhotze.wattpilot.messages.AuthMessage;
import com.florianhotze.wattpilot.messages.AuthRequiredMessage;
import com.florianhotze.wattpilot.messages.AuthSuccessMessage;
import com.florianhotze.wattpilot.messages.HelloMessage;
import com.florianhotze.wattpilot.messages.Message;
import com.florianhotze.wattpilot.messages.MessageDeserializer;
import com.florianhotze.wattpilot.messages.ResponseMessage;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for the Fronius Wattpilot wall box.
 *
 * @author Florian Hotze - Initial contribution
 */
public class WattpilotClient {
    private final Logger logger = LoggerFactory.getLogger(WattpilotClient.class);
    private final Gson gson =
            new GsonBuilder()
                    .registerTypeAdapter(Message.class, new MessageDeserializer())
                    .create();
    private final Set<WattpilotClientListener> listeners = new HashSet<>();
    private final WebSocketClient client;
    private Session session;
    private boolean isAuthenticated = false;
    private DeviceInfo deviceInfo;

    /**
     * Create a new Fronius Wattpilot client using the given {@link HttpClient}.
     * @param httpClient the HTTP client to use, allows configuring HTTP settings
     */
    public WattpilotClient(HttpClient httpClient) {
        this.client = new WebSocketClient(httpClient);
    }

    /**
     * Adds a {@link WattpilotClientListener} to the client.
     *
     * @param listener the listener to add
     */
    public void addListener(WattpilotClientListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Connect the client to the wallbox.
     *
     * @param host the hostname or IP address of the wallbox
     * @param password the password to authenticate with
     * @throws IOException if the connection fails
     */
    public void connect(String host, String password) throws IOException {
        if (session != null && session.isOpen()) {
            throw new IOException("Can not connect on already connected session");
        }
        connectWebsocket(host, password);
    }

    /** Disconnect the client from the wallbox. */
    public void disconnect() {
        if (isConnected()) {
            session.close();
        }
    }

    /**
     * Whether the client is connected to the wallbox.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return session != null && session.isOpen() && isAuthenticated;
    }

    /**
     * Get the {@link DeviceInfo} of the wall box.
     * @return the device info or <code>null</code> if not available yet
     */
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    /**
     * Established the WebSocket connection with the wall box.
     *
     * @param host the hostname or IP address of the wall box
     * @param password the password to authenticate with
     * @throws IOException if the connection fails
     */
    private void connectWebsocket(String host, String password) throws IOException {
        URI uri;
        try {
            uri = new URI(String.format("ws://%s/ws", host));
        } catch (URISyntaxException e) {
            throw new IOException("Invalid wall box host", e);
        }

        try {
            client.start();
        } catch (Exception e) {
            logger.error("Could not start websocket client", e);
            throw new IOException("Failed to start WebSocket client", e);
        }

        client.connect(new FroniusWebsocketListener(password), uri);
    }

    /** Handles incoming WebSocket messages from the wall box. */
    private class FroniusWebsocketListener implements WebSocketListener {
        private final String password;
        private byte[] hashedPassword;

        FroniusWebsocketListener(String password) {
            this.password = password;
        }

        @Override
        public void onWebSocketClose(int code, String reason) {
            logger.trace("onWebSocketClose {} {}", code, reason);
            onDisconnected(reason);
        }

        @Override
        public void onWebSocketConnect(Session wsSession) {
            logger.trace("onWebSocketConnect {}", wsSession);
            session = wsSession;
        }

        @Override
        public void onWebSocketError(Throwable error) {
            logger.trace("onWebSocketError", error);
            onDisconnected(error.getMessage());
        }

        @Override
        public void onWebSocketBinary(byte[] data, int offset, int len) {
            logger.trace("onWebSocketBinary {} {} {}", data, offset, len);
        }

        @Override
        public void onWebSocketText(String message) {
            logger.trace("onWebSocketText {}", message);
            Message m = gson.fromJson(message, Message.class);

            if (m == null) {
                return;
            }

            if (!(m instanceof ResponseMessage)) {
                logger.warn("onWebSocketText received unexpected message: {}", message);
            }

            if (m instanceof HelloMessage hm) {
                logger.trace("Received HelloMessage");
                logger.debug("Established WS connection to {}", hm.friendlyName);
                deviceInfo =
                        new DeviceInfo(
                                hm.serial,
                                hm.hostname,
                                hm.friendlyName,
                                hm.version,
                                hm.protocol,
                                hm.secured);
                if (!deviceInfo.secured()) {
                    isAuthenticated = true;
                    onConnected();
                }
            }

            if (m instanceof AuthRequiredMessage arm) {
                logger.trace("Received AuthRequiredMessage");
                try {
                    hashedPassword = hashPassword(deviceInfo.serial(), password);
                    AuthMessage authMessage =
                            createAuthMessage(hashedPassword, arm.token1, arm.token2);
                    String json = gson.toJson(authMessage);
                    logger.trace("Sending AuthMessage {}", json);
                    session.getRemote().sendString(json);
                } catch (NoSuchAlgorithmException | IOException e) {
                    logger.error("Could not send auth message", e);
                }
            }

            if (m instanceof AuthSuccessMessage) {
                logger.trace("Received AuthSuccessMessage");
                logger.debug("Authenticated successfully with {}", deviceInfo.friendlyName());
                isAuthenticated = true;
                onConnected();
            }

            if (m instanceof AuthErrorMessage rm) {
                logger.trace("Received AuthErrorMessage");
                logger.error("Authentication failed: {}", rm.message);
                onDisconnected(rm.message);
            }
        }
    }

    private void onConnected() {
        synchronized (listeners) {
            for (WattpilotClientListener listener : listeners) {
                if (listener != null) {
                    listener.connected();
                }
            }
        }
    }

    private void onDisconnected(String reason) {
        isAuthenticated = false;
        session.close();
        synchronized (listeners) {
            for (WattpilotClientListener listener : listeners) {
                if (listener != null) {
                    listener.disconnected(reason);
                }
            }
        }
    }

    /**
     * Hashes the password using PBKDF2 with HMAC-SHA512, encodes it with Bas64 and truncates to 32
     * bytes.
     *
     * @param serial the serial number of the device
     * @param password the password to hash
     * @return the hashed, truncated password
     * @throws NoSuchAlgorithmException if PBKDF2WithHmacSHA512 key algorithm is not available
     */
    private static byte[] hashPassword(String serial, String password)
            throws NoSuchAlgorithmException {
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
    private static AuthMessage createAuthMessage(
            byte[] hashedPassword, String token1, String token2) throws NoSuchAlgorithmException {
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
