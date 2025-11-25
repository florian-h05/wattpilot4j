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
package dev.digiried.wattpilot;

import dev.digiried.wattpilot.commands.Command;
import dev.digiried.wattpilot.commands.CommandResponse;
import dev.digiried.wattpilot.commands.CommandValue;
import dev.digiried.wattpilot.commands.CommandValueSerializer;
import dev.digiried.wattpilot.dto.PartialStatus;
import dev.digiried.wattpilot.messages.AuthErrorMessage;
import dev.digiried.wattpilot.messages.AuthMessage;
import dev.digiried.wattpilot.messages.AuthRequiredMessage;
import dev.digiried.wattpilot.messages.AuthSuccessMessage;
import dev.digiried.wattpilot.messages.DeltaStatusMessage;
import dev.digiried.wattpilot.messages.FullStatusMessage;
import dev.digiried.wattpilot.messages.HelloMessage;
import dev.digiried.wattpilot.messages.IncomingMessage;
import dev.digiried.wattpilot.messages.Message;
import dev.digiried.wattpilot.messages.MessageDeserializer;
import dev.digiried.wattpilot.messages.OutgoingMessage;
import dev.digiried.wattpilot.messages.ResponseMessage;
import dev.digiried.wattpilot.messages.SecuredMessage;
import dev.digiried.wattpilot.messages.SetValueMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for the Fronius Wattpilot wallbox.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class WattpilotClient {
    private static final String PING_MESSAGE = "{\"type\":\"PING\"}";
    private static final String PONG_RESPONSE_MESSAGE = "unknown message type=\"PING\"";

    private final Logger logger = LoggerFactory.getLogger(WattpilotClient.class);
    private final Gson gson =
            new GsonBuilder()
                    .registerTypeAdapter(Message.class, new MessageDeserializer())
                    .registerTypeAdapter(CommandValue.class, new CommandValueSerializer())
                    .create();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Set<WattpilotClientListener> listeners = new HashSet<>();
    private final WebSocketClient client;
    private final WattpilotStatus wattpilotStatus = new WattpilotStatus();
    private final Map<String, CompletableFuture<CommandResponse>> responseFutures =
            new ConcurrentHashMap<>();

    private @Nullable CompletableFuture<@Nullable Void> connectedFuture = null;
    private @Nullable CompletableFuture<@Nullable Void> disconnectFuture = null;

    private final long pingInterval;
    private final long pingTimeout;
    private @Nullable ScheduledFuture<?> pingTask;
    private @Nullable ScheduledFuture<?> timeoutTask;

    private @Nullable Session session;
    private boolean isAuthenticated = false;
    private boolean isInitialized = false;
    private byte[] hashedPassword = new byte[0];
    private @Nullable WattpilotInfo wattpilotInfo;
    private int requestCounter = 0;

    /**
     * Create a new Fronius Wattpilot client using the given {@link HttpClient}.
     *
     * @param httpClient the HTTP client to use, allows configuring HTTP settings
     */
    public WattpilotClient(HttpClient httpClient) {
        this.client = new WebSocketClient(httpClient);
        pingInterval = 30;
        pingTimeout = 3;
    }

    /**
     * Creates a new Fronius Wattpilot client using the given {@link HttpClient} and the provided
     * ping interval and timeout.
     *
     * @param httpClient the HTTP client to use, allows configuring HTTP settings
     * @param pingInterval the ping interval
     * @param pingTimeout the ping timeout; must be less than <code>pingInterval</code>
     */
    public WattpilotClient(HttpClient httpClient, int pingInterval, int pingTimeout) {
        this.client = new WebSocketClient(httpClient);
        if (pingTimeout >= pingInterval) {
            throw new IllegalArgumentException("pingTimeout must be less than pingInterval");
        }
        this.pingInterval = pingInterval;
        this.pingTimeout = pingTimeout;
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
     * Removes a {@link WattpilotClientListener} from the client.
     *
     * @param listener the listener to remove
     */
    public void removeListener(WattpilotClientListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Connect the client to the wallbox.
     *
     * <p>Connection is established asynchronously. Either use the returned {@link
     * CompletableFuture} or implement {@link WattpilotClientListener#connected()} and {@link
     * WattpilotClientListener#disconnected} to get notified about connection establishment or
     * failure.
     *
     * @param host the hostname or IP address of the wallbox
     * @param password the password to authenticate with
     * @return future that completes once the client has successfully connected
     * @throws IOException if the synchronous preparations for the connection establishment fail
     */
    public CompletableFuture<@Nullable Void> connect(String host, String password)
            throws IOException {
        var session = this.session;
        if (session != null && session.isOpen()) {
            throw new IOException("Can not connect on already connected session");
        }
        logger.debug("Connecting to wallbox at {}", host);
        return connectWebsocket(host, password);
    }

    /**
     * Disconnect the client from the wallbox.
     *
     * <p>This operation is idempotent, meaning it can be called multiple times without side
     * effects.
     *
     * @return future that completes once the client has successfully disconnected
     */
    public CompletableFuture<@Nullable Void> disconnect() {
        var session = this.session;
        if (session != null && session.isOpen()) {
            logger.debug("Disconnecting from wallbox at {}", session.getRemoteAddress());
            session.close();
            // onDisconnected will be called by the WebsocketListener and perform the clean-up
            CompletableFuture<@Nullable Void> disconnectFuture =
                    this.disconnectFuture = new CompletableFuture<>();
            return disconnectFuture;
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Whether the client is connected to the wallbox.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        var session = this.session;
        return session != null && session.isOpen() && isAuthenticated;
    }

    /**
     * Get the {@link WattpilotInfo} of the wallbox.
     *
     * @return the device info or <code>null</code> if not available yet
     */
    public @Nullable WattpilotInfo getDeviceInfo() {
        return wattpilotInfo;
    }

    /**
     * Get the current status of the wallbox.
     *
     * @return the current status or <code>null</code> if not available yet
     */
    public @Nullable WattpilotStatus getStatus() {
        if (!isInitialized) {
            return null;
        }
        return wattpilotStatus;
    }

    /**
     * Send a {@link Command} to the wallbox and return a {@link CompletableFuture} that will be
     * completed when the response is received.
     *
     * @param command the command to send
     * @return a {@link CompletableFuture} that will be completed when the response is received, or
     *     completed exceptionally with an {@link IOException} if the command could not be sent
     */
    public CompletableFuture<CommandResponse> sendCommand(Command command) {
        if (!isConnected()) {
            throw new IllegalStateException("Client is not connected");
        }

        SetValueMessage setValueMessage = SetValueMessage.fromCommand(requestCounter, command);
        var wattpilotInfo = this.wattpilotInfo;
        if (wattpilotInfo != null && !wattpilotInfo.secured()) {
            logger.trace("Sending SetValueMessage");
            return sendOutgoingMessage(String.valueOf(setValueMessage.requestId), setValueMessage);
        }

        String data = gson.toJson(setValueMessage);
        String hmac;
        try {
            hmac = AuthUtil.createHmac(hashedPassword, data);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could not send command: Failed to create HMAC", e);
            CompletableFuture<CommandResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new IOException("Failed to create HMAC", e));
            return future;
        }
        SecuredMessage securedMessage = new SecuredMessage(data, requestCounter + "sm", hmac);
        requestCounter++;
        logger.trace("Sending SecuredMessage");
        return sendOutgoingMessage(String.valueOf(setValueMessage.requestId), securedMessage);
    }

    /**
     * Establishes the WebSocket connection with the wallbox.
     *
     * @param host the hostname or IP address of the wallbox
     * @param password the password to authenticate with
     * @throws IOException if the connection fails
     */
    private CompletableFuture<@Nullable Void> connectWebsocket(String host, String password)
            throws IOException {
        URI uri;
        try {
            uri = new URI(String.format("ws://%s/ws", host));
        } catch (URISyntaxException e) {
            throw new IOException("Invalid wallbox host", e);
        }

        try {
            client.start();
        } catch (Exception e) {
            logger.error("Could not start websocket client", e);
            throw new IOException("Failed to start WebSocket client", e);
        }

        CompletableFuture<@Nullable Void> connectedFuture =
                this.connectedFuture = new CompletableFuture<>();
        client.connect(new FroniusWebsocketListener(password), uri);
        return connectedFuture;
    }

    private void cancelPingTask() {
        var pingTask = this.pingTask;
        if (pingTask != null) {
            pingTask.cancel(false);
            this.pingTask = null;
        }
        cancelTimeoutTask();
    }

    private void schedulePingTask() {
        cancelPingTask();
        pingTask =
                scheduler.scheduleAtFixedRate(
                        () -> {
                            try {
                                logger.debug("Sending PING message");
                                var session = this.session;
                                if (session == null) {
                                    throw new IllegalStateException(
                                            "No WebSocket session available");
                                }
                                session.getRemote().sendString(PING_MESSAGE);
                                scheduleTimeoutTask();
                            } catch (IOException e) {
                                logger.error("Failed to send ping message", e);
                                onDisconnected("Failed to send ping message", e);
                            }
                        },
                        pingInterval,
                        pingInterval,
                        TimeUnit.SECONDS);
    }

    private void cancelTimeoutTask() {
        var timeoutTask = this.timeoutTask;
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
            this.timeoutTask = null;
        }
    }

    @SuppressWarnings("null")
    private void scheduleTimeoutTask() {
        cancelTimeoutTask();
        timeoutTask =
                scheduler.schedule(
                        () -> {
                            logger.warn("Ping to {} timed out", session.getRemoteAddress());
                            onDisconnected(
                                    "Ping timed out",
                                    new TimeoutException("No pong received before ping timed out"));
                        },
                        pingTimeout,
                        TimeUnit.SECONDS);
    }

    /**
     * Sends an outgoing message to the wallbox and returns a {@link CompletableFuture} that will be
     * completed when the response is received.
     *
     * @param messageId the message ID expected of that message as expected in the response
     * @param message the message to send
     * @return a {@link CompletableFuture} that will be completed when the response is received, or
     *     completed exceptionally with an {@link IOException} if the message could not be sent
     */
    private CompletableFuture<CommandResponse> sendOutgoingMessage(
            final String messageId, OutgoingMessage message) {
        final CompletableFuture<CommandResponse> future = new CompletableFuture<>();
        if (!isConnected()) {
            future.completeExceptionally(new IOException("Client is not connected"));
            return future;
        }
        String json = gson.toJson(message);

        logger.debug("Writing message {}", json);
        var session = this.session;
        if (session == null) {
            throw new IllegalStateException("No WebSocket session available");
        }
        session.getRemote()
                .sendString(
                        json,
                        new WriteCallback() {
                            @Override
                            public void writeSuccess() {
                                logger.trace("writeSuccess for messageId {}", messageId);
                                responseFutures.put(messageId, future);
                            }

                            @NonNullByDefault({})
                            @Override
                            public void writeFailed(Throwable t) {
                                future.completeExceptionally(t);
                            }
                        });
        return future;
    }

    /** Handles incoming WebSocket messages from the wallbox. */
    @NonNullByDefault({})
    @WebSocket
    private class FroniusWebsocketListener implements WebSocketListener {
        private final String password;

        FroniusWebsocketListener(String password) {
            this.password = password;
        }

        @Override
        public void onWebSocketClose(int code, String reason) {
            logger.trace("onWebSocketClose {} {}", code, reason);
            onDisconnected(reason, null);
        }

        @Override
        public void onWebSocketConnect(Session wsSession) {
            logger.trace("onWebSocketConnect {}", wsSession);
            session = wsSession;
        }

        @Override
        public void onWebSocketError(Throwable error) {
            logger.debug("onWebSocketError", error);
            onDisconnected("WebSocket error", error);
        }

        @Override
        public void onWebSocketBinary(byte[] data, int offset, int len) {
            logger.trace("onWebSocketBinary {} {} {}", data, offset, len);
        }

        @SuppressWarnings("null")
        @Override
        public void onWebSocketText(String message) {
            logger.trace("onWebSocketText {}", message);
            Message m;
            try {
                m = gson.fromJson(message, Message.class);
            } catch (JsonSyntaxException e) {
                logger.warn("Could not parse message {} to JSON", message);
                return;
            }

            if (m == null) {
                return;
            }

            if (!(m instanceof IncomingMessage)) {
                logger.warn("onWebSocketText received unexpected message: {}", message);
            }

            if (m instanceof HelloMessage hm) {
                logger.trace("Received HelloMessage");
                logger.debug("Established WS connection to {}", hm.friendlyName);
                wattpilotInfo =
                        new WattpilotInfo(
                                hm.serial,
                                hm.hostname,
                                hm.friendlyName,
                                hm.version,
                                hm.protocol,
                                hm.secured);
                if (logger.isDebugEnabled()) {
                    logger.debug(wattpilotInfo.toString());
                }
                if (!wattpilotInfo.secured()) {
                    isAuthenticated = true;
                    onConnected();
                }
            }

            if (m instanceof AuthRequiredMessage arm) {
                logger.trace("Received AuthRequiredMessage");
                try {
                    hashedPassword = AuthUtil.hashPassword(wattpilotInfo.serial(), password);
                    AuthMessage authMessage =
                            AuthUtil.createAuthMessage(hashedPassword, arm.token1, arm.token2);
                    String json = gson.toJson(authMessage);
                    logger.trace("Sending AuthMessage {}", json);
                    session.getRemote().sendString(json);
                } catch (NoSuchAlgorithmException | IOException e) {
                    logger.error("Could not send auth message", e);
                }
            }

            if (m instanceof AuthSuccessMessage) {
                logger.trace("Received AuthSuccessMessage");
                logger.debug("Authenticated successfully with {}", wattpilotInfo.friendlyName());
                isAuthenticated = true;
                onConnected();
            }

            if (m instanceof AuthErrorMessage rm) {
                logger.trace("Received AuthErrorMessage");
                logger.error("Authentication failed: {}", rm.message);
                onDisconnected(
                        "Authentication failed",
                        new IOException("Authentication failed: " + rm.message));
            }

            if (m instanceof FullStatusMessage fsm) {
                logger.trace("Received FullStatusMessage");
                onStatus(fsm.status);
            }

            if (m instanceof DeltaStatusMessage dsm) {
                logger.trace("Received DeltaStatusMessage");
                if (!isInitialized) {
                    isInitialized = true;
                    logger.debug("Received (all parts of) full status, status is initialized now");
                    notifyListenersAboutStatusChange();
                }
                onStatus(dsm.status);
            }

            if (m instanceof ResponseMessage rm) {
                logger.trace("Received ResponseMessage");
                if (!rm.success && rm.message.equals(PONG_RESPONSE_MESSAGE)) {
                    logger.debug("Received PONG response");
                    cancelTimeoutTask();
                    return;
                }

                CompletableFuture<@NonNull CommandResponse> future =
                        responseFutures.remove(rm.requestId);
                if (future != null) {
                    future.complete(new CommandResponse(rm.success, rm.status));
                }
            }
        }
    }

    private void onConnected() { // NOSONAR: we want to keep this method here
        schedulePingTask();
        var connectedFuture = this.connectedFuture;
        if (connectedFuture != null && !connectedFuture.isDone()) {
            connectedFuture.complete(null);
            this.connectedFuture = null;
        }
        synchronized (listeners) {
            for (WattpilotClientListener listener : listeners) {
                listener.connected();
            }
        }
    }

    private void onDisconnected(
            String reason, @Nullable Throwable cause) { // NOSONAR: we want to keep this method here
        isAuthenticated = false;
        cancelPingTask();
        var session = this.session;
        if (session != null && session.isOpen()) {
            session.close();
        }
        this.session = null; // make sure to always destroy the session, even if already closed
        // complete connection future exceptionally
        var connectedFuture = this.connectedFuture;
        if (connectedFuture != null && !connectedFuture.isDone()) {
            connectedFuture.completeExceptionally(cause != null ? cause : new IOException(reason));
            this.connectedFuture = null;
        }
        // complete all pending futures exceptionally
        responseFutures.forEach(
                (key, future) -> {
                    future.completeExceptionally(new IOException("Client disconnected"));
                    responseFutures.remove(key);
                });
        // notify listeners
        synchronized (listeners) {
            for (WattpilotClientListener listener : listeners) {
                listener.disconnected(reason, cause);
            }
        }
        var disconnectFuture = this.disconnectFuture;
        if (disconnectFuture != null && !disconnectFuture.isDone()) {
            if (cause != null) {
                disconnectFuture.completeExceptionally(cause);
            } else {
                disconnectFuture.complete(null);
            }
            this.disconnectFuture = null;
        }
    }

    private void onStatus(PartialStatus status) { // NOSONAR: we want to keep this method here
        boolean hasChanged =
                false; // as a field is only not-null if it is present in a (fragment of a) full
        // message or a delta message, we can assume that it has changed then
        synchronized (wattpilotStatus) {
            if (status.isChargingAllowed() != null) {
                wattpilotStatus.setChargingAllowed(status.isChargingAllowed());
                hasChanged = true;
            }
            if (status.isBoostEnabled() != null) {
                wattpilotStatus.setBoostEnabled(status.isBoostEnabled());
                hasChanged = true;
            }
            if (status.getBoostSoCLimit() != null) {
                wattpilotStatus.setBoostSoCLimit(status.getBoostSoCLimit());
                hasChanged = true;
            }
            if (status.getChargingCurrent() != null) {
                wattpilotStatus.setChargingCurrent(status.getChargingCurrent());
                hasChanged = true;
            }
            if (status.getChargingState() != null) {
                wattpilotStatus.setChargingState(status.getChargingState());
                hasChanged = true;
            }
            if (status.getSurplusPowerThreshold() != null) {
                wattpilotStatus.setSurplusPowerThreshold(status.getSurplusPowerThreshold());
                hasChanged = true;
            }
            if (status.getSurplusSoCThreshold() != null) {
                wattpilotStatus.setSurplusSoCThreshold(status.getSurplusSoCThreshold());
                hasChanged = true;
            }
            if (status.getEnforcedChargingState() != null) {
                wattpilotStatus.setEnforcedState(status.getEnforcedChargingState());
                hasChanged = true;
            }
            if (status.isChargingSinglePhase() != null) {
                wattpilotStatus.setChargingSinglePhase(status.isChargingSinglePhase());
                hasChanged = true;
            }
            if (status.getChargingMode() != null) {
                wattpilotStatus.setChargingMode(status.getChargingMode());
                hasChanged = true;
            }
            if (status.getChargingMetrics() != null) {
                wattpilotStatus.setChargingMetrics(status.getChargingMetrics());
                hasChanged = true;
            }
            if (status.getEnergyCounterSinceStart() != null) {
                wattpilotStatus.setEnergyCounterSinceStart(status.getEnergyCounterSinceStart());
                hasChanged = true;
            }
            if (status.getEnergyCounterTotal() != null) {
                wattpilotStatus.setEnergyCounterTotal(status.getEnergyCounterTotal());
                hasChanged = true;
            }
        }
        if (isInitialized
                && hasChanged) { // only notify if status has been updated by a delta message, i.e.
            // after state initialization
            notifyListenersAboutStatusChange();
        }
    }

    private void notifyListenersAboutStatusChange() {
        synchronized (listeners) {
            for (WattpilotClientListener listener : listeners) {
                listener.statusChanged(wattpilotStatus);
            }
        }
    }
}
