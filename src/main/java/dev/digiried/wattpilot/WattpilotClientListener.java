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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for listening to events from the {@link WattpilotClient}.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public interface WattpilotClientListener {
    /**
     * Called when the client successfully connected to the wallbox.
     *
     * @deprecated Use {@link #connected(WattpilotInfo)} instead.
     */
    @Deprecated(forRemoval = true)
    default void connected() {}

    /**
     * Called when the client successfully connected to the wallbox and received the initial device
     *
     * @param info the device info
     */
    default void connected(WattpilotInfo info) {
        connected();
    }

    /**
     * Called when the client disconnected from the wallbox.
     *
     * @param reason the reason for the disconnection
     * @param cause the throwable that caused ungraceful disconnect or <code>null</code> if
     *     disconnection was graceful
     */
    void disconnected(String reason, @Nullable Throwable cause);

    /**
     * Called when the client receives a status change from the wallbox.
     *
     * @param status the new status
     */
    default void statusChanged(WattpilotStatus status) {}
}
