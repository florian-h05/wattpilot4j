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
package com.florianhotze.wattpilot.messages;

import com.florianhotze.wattpilot.dto.PartialStatus;

/**
 * Response Message. Received after sending a {@link SetValueMessage} (or {@link SecuredMessage} and
 * contains the result of the operation.
 *
 * @author Florian Hotze - Initial contribution
 */
public class ResponseMessage extends IncomingMessage {
    public String requestId;
    public boolean success;
    public PartialStatus status;

    ResponseMessage() {
        super(MessageType.RESPONSE);
    }
}
