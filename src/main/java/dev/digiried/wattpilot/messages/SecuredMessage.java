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

/**
 * Secured Message. If the connection is secured, this is sent by the client to send an
 * authenticated {@link SetValueMessage}.
 *
 * @author Florian Hotze - Initial contribution
 */
public class SecuredMessage extends OutgoingMessage {
    public final String data;
    public final String requestId;
    public final String hmac;

    /**
     * Creates a new SecuredMessage.
     *
     * @param data the serialized {@link SetValueMessage} to be sent
     * @param requestId the request id
     * @param hmac the HMAC to authenticate the message
     */
    public SecuredMessage(String data, String requestId, String hmac) {
        super(MessageType.SECURED_MSG);
        this.data = data;
        this.requestId = requestId;
        this.hmac = hmac;
    }
}
