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
 * Auth Required Message. Received after hello to ask for authentication.
 *
 * @author Florian Hotze - Initial contribution
 */
public class AuthRequiredMessage extends IncomingMessage {
    public String token1;
    public String token2;
    public String hash;

    public AuthRequiredMessage() {
        super(MessageType.AUTH_REQUIRED);
    }
}
