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

import com.google.gson.annotations.SerializedName;

/**
 * Hello Message. Received upon connection before authentication.
 *
 * @author Florian Hotze - Initial contribution
 */
public class HelloMessage extends IncomingMessage {
    public String serial;
    public String hostname;

    @SerializedName("friendly_name")
    public String friendlyName;

    public String manufacturer;
    public String devicetype;
    public String version;
    public int protocol;
    public boolean secured;

    public HelloMessage() {
        super(MessageType.HELLO);
    }
}
