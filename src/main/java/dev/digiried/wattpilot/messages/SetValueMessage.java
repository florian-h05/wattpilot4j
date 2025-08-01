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
package dev.digiried.wattpilot.messages;

import dev.digiried.wattpilot.commands.Command;
import dev.digiried.wattpilot.commands.CommandValue;

/**
 * Set Value Message. Is sent by the client to change a property value.
 *
 * @author Florian Hotze - Initial contribution
 */
public class SetValueMessage extends OutgoingMessage {
    public final int requestId;
    public final String key;
    public final CommandValue<?> value;

    /**
     * Creates a new SetValueMessage.
     *
     * @param requestId the request id, must start with 0 for a new client connection and increment
     *     by 1 for each message
     * @param key the key of the property to change
     * @param value the new value
     */
    SetValueMessage(int requestId, String key, CommandValue<?> value) {
        super(MessageType.SET_VALUE);
        this.requestId = requestId;
        this.key = key;
        this.value = value;
    }

    /**
     * Creates a new SetValueMessage from a command.
     *
     * @param requestId the request id, must start with 0 for a new client connection and increment
     *     by 1 for each message
     * @param command the command to create the message from
     * @return the new SetValueMessage
     */
    public static SetValueMessage fromCommand(int requestId, Command command) {
        return new SetValueMessage(requestId, command.getKey(), command.getValue());
    }
}
