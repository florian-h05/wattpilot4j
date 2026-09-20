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
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
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

    private final Set<WattpilotClientListener> listeners = new CopyOnWriteArraySet<>();
    private final WebSocketClient client;
    private final long pingInterval;
    private final long pingTimeout;

    // connection handling:
    private final Object connectionLock = new Object();
    private volatile WattpilotClient.@Nullable WebSocketConnection connection = null;
    private volatile @Nullable CompletableFuture<@Nullable Void> connectFuture = null;
    private volatile @Nullable CompletableFuture<@Nullable Void> disconnectFuture = null;

    // command handling:
    private final Object sendCommandLock = new Object();

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
        listeners.add(listener);
    }

    /**
     * Removes a {@link WattpilotClientListener} from the client.
     *
     * @param listener the listener to remove
     */
    public void removeListener(WattpilotClientListener listener) {
        listeners.remove(listener);
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
        var connection = this.connection;
        if (connection == null) {
            return CompletableFuture.completedFuture(null);
        }
        var session = connection.getSession();
        if (session == null || !session.isOpen()) {
            return CompletableFuture.completedFuture(null);
        }

        // Atomically get-check-and-set disconnectFuture.
        // Note: AtomicReference only provides a checkAndSet, but no combination of get, check and
        // set, so we can't use it here.
        CompletableFuture<@Nullable Void> disconnectFuture;
        synchronized (connectionLock) {
            if (this.connection != connection) {
                return CompletableFuture.completedFuture(null);
            }
            disconnectFuture = this.disconnectFuture;
            if (disconnectFuture != null) {
                return disconnectFuture.copy();
            }

            logger.debug("Disconnecting from wallbox at {}", session.getRemoteSocketAddress());
            disconnectFuture = this.disconnectFuture = new CompletableFuture<@Nullable Void>();
        }
        session.close(); // onDisconnected will be called by the Session.Listener and perform the
        // cleanup
        return disconnectFuture.copy();
    }

    /**
     * Whether the client is connected to the wallbox.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        var connection = this.connection;
        if (connection == null) {
            return false;
        }
        return connection.isConnected();
    }

    /**
     * Get the {@link WattpilotInfo} of the wallbox.
     *
     * @return the device info or <code>null</code> if not available yet
     */
    public @Nullable WattpilotInfo getDeviceInfo() {
        var connection = this.connection;
        if (connection == null) {
            return null;
        }
        return connection.wattpilotInfo;
    }

    /**
     * Get the current status of the wallbox.
     *
     * @return the current status or <code>null</code> if not available yet
     */
    public @Nullable WattpilotStatus getStatus() {
        var connection = this.connection;
        if (connection == null || !connection.isInitialized()) {
            return null;
        }

        synchronized (connection.wattpilotStatus) {
            return new WattpilotStatus(connection.wattpilotStatus);
        }
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
        var connection = this.connection;
        if (connection == null || !connection.isConnected()) {
            throw new IllegalStateException("Client is not connected");
        }

        // Synchronize to guarantee strict ordering of counter-increment AND transmission
        synchronized (sendCommandLock) {
            int requestCounter = connection.requestCounter;

            SetValueMessage setValueMessage = SetValueMessage.fromCommand(requestCounter, command);
            var wattpilotInfo = connection.wattpilotInfo;
            if (wattpilotInfo != null && !wattpilotInfo.secured()) {
                logger.trace("Sending SetValueMessage");
                connection.requestCounter++;
                return connection.sendOutgoingMessage(
                        String.valueOf(setValueMessage.requestId), setValueMessage);
            }

            String data = gson.toJson(setValueMessage);
            String hmac;
            try {
                hmac = AuthUtil.createHmac(connection.hashedPassword, data);
            } catch (NoSuchAlgorithmException e) {
                logger.error("Could not send command: Failed to create HMAC", e);
                CompletableFuture<CommandResponse> future = new CompletableFuture<>();
                future.completeExceptionally(new IOException("Failed to create HMAC", e));
                return future;
            }
            SecuredMessage securedMessage = new SecuredMessage(data, requestCounter + "sm", hmac);
            logger.trace("Sending SecuredMessage");
            connection.requestCounter++;
            return connection.sendOutgoingMessage(
                    String.valueOf(setValueMessage.requestId), securedMessage);
        }
    }

    /**
     * Establishes the WebSocket connection with the wallbox.
     *
     * @param host the hostname or IP address of the wallbox
     * @param password the password to authenticate with
     * @throws IOException if the connection fails or the connection is already established
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

        CompletableFuture<@Nullable Void> connectedFuture;
        WebSocketConnection connection;
        synchronized (connectionLock) {
            if (this.connection != null) {
                var future = this.connectFuture;
                if (future != null) {
                    return future.copy();
                } else {
                    throw new IOException("Can not connect on already connected session");
                }
            }
            connection = this.connection = new WebSocketConnection(password);
            connectedFuture = this.connectFuture = new CompletableFuture<>();
        }
        try {
            client.connect(connection, uri);
        } catch (RuntimeException e) {
            onDisconnected(connection, "Failed to start connection", e);
            throw new IOException("Failed to connect", e);
        }
        return connectedFuture.copy();
    }

    private void schedulePingTask(WebSocketConnection origin) {
        var task =
                scheduler.scheduleAtFixedRate(
                        () -> {
                            if (connection != origin) {
                                return; // connection already torn down
                            }

                            logger.debug("Sending PING message");

                            try {
                                scheduleTimeoutTask(
                                        origin); // make sure to schedule the timeout task before
                                // sending PING, so a PONG response is guaranteed
                                // to find a scheduled timeout task
                                var session = origin.getSession();
                                if (session == null) {
                                    return;
                                }
                                session.sendText(
                                        PING_MESSAGE,
                                        new Callback() {
                                            @NonNullByDefault({})
                                            @Override
                                            public void fail(Throwable t) {
                                                logger.error("Failed to send ping message", t);
                                                onDisconnected(
                                                        origin, "Failed to send ping message", t);
                                            }
                                        });
                            } catch (RuntimeException e) {
                                logger.error(
                                        "Ping task failed", e); // never let the periodic task die
                            }
                        },
                        pingInterval,
                        pingInterval,
                        TimeUnit.SECONDS);
        origin.setPingTask(task);
    }

    private void scheduleTimeoutTask(WebSocketConnection origin) {
        var task =
                scheduler.schedule(
                        () -> {
                            if (connection != origin) {
                                return; // connection already torn down
                            }
                            var session = origin.getSession();
                            logger.warn(
                                    "Ping to {} timed out",
                                    session != null ? session.getRemoteSocketAddress() : null);
                            onDisconnected(
                                    origin,
                                    "Ping timed out",
                                    new IOException("No pong received before ping timed out"));
                        },
                        pingTimeout,
                        TimeUnit.SECONDS);
        origin.setTimeoutTask(task);
    }

    /**
     * {@link WebSocketConnection} holds connection-bound state and implements Jetty {@link
     * Session.Listener.AutoDemanding} to handle incoming WebSocket messages from the wallbox.
     *
     * <p>The connection is owned by the {@link WattpilotClient}.
     *
     * @implNote This class has to be public for Jetty.
     */
    @NonNullByDefault({})
    public class WebSocketConnection implements Session.Listener.AutoDemanding {
        // auth:
        private final String password;
        private volatile byte[] hashedPassword = new byte[0];

        // flags:
        private volatile boolean authenticated = false;
        private volatile boolean initialized = false;
        private volatile boolean tornDown = false;

        // command handling:
        private volatile int requestCounter = 0;
        private final Map<String, CompletableFuture<CommandResponse>> responseFutures =
                new ConcurrentHashMap<>();

        // heartbeat mechanism:
        private final AtomicReference<@Nullable ScheduledFuture<?>> pingTask =
                new AtomicReference<>(null);
        private final AtomicReference<@Nullable ScheduledFuture<?>> timeoutTask =
                new AtomicReference<>(null);

        // state:
        private volatile @Nullable Session session;
        private volatile @Nullable WattpilotInfo wattpilotInfo;
        private final WattpilotStatus wattpilotStatus = new WattpilotStatus();

        WebSocketConnection(String password) {
            this.password = password;
        }

        private @Nullable Session getSession() {
            return session;
        }

        private boolean isConnected() {
            var session = this.session;
            return session != null && session.isOpen() && authenticated;
        }

        private boolean isInitialized() {
            return initialized;
        }

        void setPingTask(ScheduledFuture<?> task) {
            var oldTask = pingTask.getAndSet(task);
            if (oldTask != null) {
                oldTask.cancel(false);
            }
            if (tornDown) {
                cancelHeartbeat();
            }
        }

        void setTimeoutTask(ScheduledFuture<?> task) {
            var oldTask = timeoutTask.getAndSet(task);
            if (oldTask != null) {
                oldTask.cancel(false);
            }
            if (tornDown) {
                cancelHeartbeat();
            }
        }

        void cancelTimeoutTask() {
            var oldTask = timeoutTask.getAndSet(null);
            if (oldTask != null) {
                oldTask.cancel(false);
            }
        }

        void cancelHeartbeat() {
            var oldTask = pingTask.getAndSet(null);
            if (oldTask != null) {
                oldTask.cancel(false);
            }
            cancelTimeoutTask();
        }

        private void teardown() {
            tornDown = true;
            cancelHeartbeat();

            // Close the session
            var session = this.session;
            if (session != null) {
                session.close();
            }

            // Complete all pending response futures exceptionally
            responseFutures.forEach(
                    (key, future) -> {
                        future.completeExceptionally(new IOException("Client disconnected"));
                        responseFutures.remove(key);
                    });
        }

        /**
         * Sends an outgoing message to the wallbox and returns a {@link CompletableFuture} that
         * will be completed when the response is received.
         *
         * @param messageId the message ID expected of that message as expected in the response
         * @param message the message to send
         * @return a {@link CompletableFuture} that will be completed when the response is received,
         *     or completed exceptionally with an {@link IOException} if the message could not be
         *     sent
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
                throw new IllegalStateException(
                        "No WebSocket session available, this should not happen");
            }
            responseFutures.put(messageId, future);
            if (tornDown) { // teardown may already have drained the map
                responseFutures.remove(messageId);
                future.completeExceptionally(new IOException("Client disconnected"));
                return future;
            }
            session.sendText(
                    json,
                    new Callback() {
                        @Override
                        public void succeed() {
                            logger.trace("writeSuccess for messageId {}", messageId);
                        }

                        @NonNullByDefault({})
                        @Override
                        public void fail(Throwable t) {
                            responseFutures.remove(messageId);
                            future.completeExceptionally(t);
                        }
                    });
            return future;
        }

        @Override
        public void onWebSocketClose(int code, String reason) {
            logger.trace("onWebSocketClose {} {}", code, reason);
            // see https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent/code for CloseEvent
            // codes
            if (code == 1000 || code == 1005) {
                onDisconnected(WebSocketConnection.this, "Connection was closed gracefully", null);
                return;
            }
            onDisconnected(
                    WebSocketConnection.this,
                    "Connection was closed unexpectedly",
                    new IOException(
                            "Connection was closed unexpectedly: code "
                                    + code
                                    + "; reason: "
                                    + reason));
        }

        @Override
        public void onWebSocketOpen(Session wsSession) {
            logger.trace("onWebSocketOpen {}", wsSession);
            this.session = wsSession;
            synchronized (connectionLock) {
                if (connection != WebSocketConnection.this) {
                    if (wsSession != null && wsSession.isOpen()) {
                        wsSession.close();
                    }
                    return;
                }
                session = wsSession;
            }
        }

        @Override
        public void onWebSocketError(Throwable error) {
            logger.debug("onWebSocketError", error);
            onDisconnected(WebSocketConnection.this, "Connection error", error);
        }

        @Override
        public void onWebSocketBinary(ByteBuffer data, Callback callback) {
            logger.trace("onWebSocketBinary {}", data);
            callback.succeed();
        }

        @SuppressWarnings("null")
        @Override
        public void onWebSocketText(String message) {
            if (connection != WebSocketConnection.this) {
                return;
            }
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
                var wi =
                        wattpilotInfo =
                                new WattpilotInfo(
                                        hm.serial,
                                        hm.hostname,
                                        hm.friendlyName,
                                        hm.deviceType,
                                        hm.version,
                                        hm.protocol,
                                        hm.secured);
                if (logger.isDebugEnabled()) {
                    logger.debug(wi.toString());
                }
                if (!wi.secured()) {
                    authenticated = true;
                    onConnected(WebSocketConnection.this);
                }
            }

            if (m instanceof AuthRequiredMessage arm) {
                logger.trace("Received AuthRequiredMessage");
                AuthUtil.HashAlgorithm hash = AuthUtil.HashAlgorithm.PBKDF2;
                if (arm.hash != null && !arm.hash.isBlank()) {
                    logger.debug("Wattpilot requested {} hash algorithm.", arm.hash);
                    AuthUtil.HashAlgorithm requestedHash =
                            AuthUtil.HashAlgorithm.fromString(arm.hash);
                    if (requestedHash != null) {
                        hash = requestedHash;
                    } else {
                        logger.warn(
                                "Wattpilot requested unknown hash algorithm {}, falling back to"
                                        + " {}.",
                                arm.hash,
                                hash);
                    }
                } else if ("wattpilot_flex".equals(wattpilotInfo.deviceType())) {
                    hash = AuthUtil.HashAlgorithm.BCRYPT;
                }
                try {
                    hashedPassword = AuthUtil.hashPassword(wattpilotInfo.serial(), password, hash);
                    AuthMessage authMessage =
                            AuthUtil.createAuthMessage(hashedPassword, arm.token1, arm.token2);
                    String json = gson.toJson(authMessage);
                    logger.trace("Sending AuthMessage {}", json);
                    var session = this.session;
                    if (session != null) {
                        session.sendText(
                                json,
                                new Callback() {
                                    @NonNullByDefault({})
                                    @Override
                                    public void fail(Throwable t) {
                                        logger.error("Could not send auth message", t);
                                    }
                                });
                    }
                } catch (NoSuchAlgorithmException e) {
                    logger.error("Could not send auth message", e);
                }
            }

            if (m instanceof AuthSuccessMessage) {
                logger.trace("Received AuthSuccessMessage");
                logger.debug("Authenticated successfully with {}", wattpilotInfo.friendlyName());
                authenticated = true;
                onConnected(WebSocketConnection.this);
            }

            if (m instanceof AuthErrorMessage rm) {
                logger.trace("Received AuthErrorMessage");
                logger.error("Authentication failed: {}", rm.message);
                onDisconnected(
                        WebSocketConnection.this,
                        "Authentication failed",
                        new IOException("Authentication failed: " + rm.message));
            }

            if (m instanceof FullStatusMessage fsm) {
                logger.trace("Received FullStatusMessage");
                onStatus(fsm.status);
            }

            if (m instanceof DeltaStatusMessage dsm) {
                logger.trace("Received DeltaStatusMessage");
                if (!initialized) {
                    initialized = true;
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

        private void onStatus(PartialStatus status) { // NOSONAR: we want to keep this method here
            boolean hasChanged =
                    false; // as a field is only not-null if it is present in a (fragment of a) full
            // message or a delta message, we can assume that it has changed then
            synchronized (wattpilotStatus) {
                if (status.isChargingAllowed() != null) {
                    wattpilotStatus.setChargingAllowed(status.isChargingAllowed());
                    hasChanged = true;
                }
                if (status.getAuthorizationState() != null) {
                    wattpilotStatus.setAuthorizationState(status.getAuthorizationState());
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
            if (initialized
                    && hasChanged) { // only notify if status has been updated by a delta message,
                // i.e. after state initialization
                notifyListenersAboutStatusChange();
            }
        }

        private void notifyListenersAboutStatusChange() {
            WattpilotStatus statusCopy;
            synchronized (wattpilotStatus) {
                statusCopy = new WattpilotStatus(wattpilotStatus);
            }
            for (WattpilotClientListener listener : listeners) {
                listener.statusChanged(statusCopy);
            }
        }
    }

    private void onConnected(
            WebSocketConnection origin) { // NOSONAR: we want to keep this method here
        if (connection != origin) {
            return;
        }
        schedulePingTask(origin);

        // Complete connection future
        CompletableFuture<@Nullable Void> connectedFuture;
        synchronized (connectionLock) { // atomic get-and-set
            connectedFuture = this.connectFuture;
            this.connectFuture = null;
        }
        if (connectedFuture != null && !connectedFuture.isDone()) {
            connectedFuture.complete(null);
        }

        // notify listeners
        var wattpilotInfo = origin.wattpilotInfo;
        if (wattpilotInfo == null) {
            throw new IllegalStateException("wattpilotInfo is null, this should not happen");
        }
        for (WattpilotClientListener listener : listeners) {
            listener.connected(wattpilotInfo);
        }
    }

    private void onDisconnected(
            WebSocketConnection origin,
            String reason,
            @Nullable Throwable cause) { // NOSONAR: we want to keep this method here
        CompletableFuture<@Nullable Void> connectedFuture;
        CompletableFuture<@Nullable Void> disconnectFuture;
        synchronized (connectionLock) {
            if (connection != origin) {
                return;
            }
            connection = null;

            connectedFuture = this.connectFuture;
            this.connectFuture = null;
            disconnectFuture = this.disconnectFuture;
            this.disconnectFuture = null;
        }

        // Complete connect future exceptionally
        if (connectedFuture != null && !connectedFuture.isDone()) {
            connectedFuture.completeExceptionally(cause != null ? cause : new IOException(reason));
        }

        origin.teardown();

        // Notify listeners
        for (WattpilotClientListener listener : listeners) {
            listener.disconnected(reason, cause);
        }

        // Complete disconnect future
        if (disconnectFuture != null && !disconnectFuture.isDone()) {
            if (cause != null) {
                disconnectFuture.completeExceptionally(cause);
            } else {
                disconnectFuture.complete(null);
            }
        }
    }
}
