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

/**
 * Interface for listening to events from the {@link WattpilotClient}.
 *
 * @author Florian Hotze - Initial contribution
 */
public interface WattpilotClientListener {
    /** Called when the client successfully connected to the wall box. */
    void connected();

    /**
     * Called when the client disconnected from the wall box.
     *
     * @param reason the reason for the disconnection
     */
    void disconnected(String reason);

    /**
     * Called when the client receives a status change from the wall box.
     *
     * @param status the new status
     */
    default void onStatusChange(WattpilotStatus status) {}
}
