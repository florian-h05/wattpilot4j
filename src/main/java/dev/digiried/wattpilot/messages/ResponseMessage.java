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

import dev.digiried.wattpilot.dto.PartialStatus;

/**
 * Response Message. Received after sending a {@link SetValueMessage} (or {@link SecuredMessage} and
 * contains the result of the operation.
 *
 * @author Florian Hotze - Initial contribution
 */
public class ResponseMessage extends IncomingMessage {
    /** Whether the previous request was successful. */
    public boolean success;

    /** Message what went wrong in case of failure. */
    public String message;

    /** Request id in case of success. */
    public String requestId;

    /** Partial status containing the changed key in case of success. */
    public PartialStatus status;

    ResponseMessage() {
        super(MessageType.RESPONSE);
    }
}
