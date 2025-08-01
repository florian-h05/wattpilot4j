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
 * Auth Message. This message is sent from the client to Wattpilot to perform an authentication.
 *
 * @author Florian Hotze - Initial contribution
 */
public class AuthMessage extends OutgoingMessage {
    public final String token3;
    public final String hash;

    public AuthMessage(String token3, String hash) {
        super(MessageType.AUTH);
        this.token3 = token3;
        this.hash = hash;
    }
}
