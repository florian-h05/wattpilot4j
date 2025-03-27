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

import java.lang.reflect.Type;
import java.util.HashMap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON deserializer for {@link Message}s.
 *
 * @author Florian Hotze - Initial contribution
 */
public class MessageDeserializer implements JsonDeserializer<Message> {
    private final Logger logger = LoggerFactory.getLogger(MessageDeserializer.class);

    private static HashMap<String, Class<?>> messageHandlers = new HashMap<>();

    static {
        messageHandlers.put(MessageType.HELLO.getSerializedName(), HelloMessage.class);
        messageHandlers.put(
                MessageType.AUTH_REQUIRED.getSerializedName(), AuthRequiredMessage.class);
        messageHandlers.put(MessageType.AUTH_SUCCESS.getSerializedName(), AuthSuccessMessage.class);
        messageHandlers.put(MessageType.AUTH_ERROR.getSerializedName(), AuthErrorMessage.class);
        messageHandlers.put(MessageType.FULL_STATUS.getSerializedName(), FullStatusMessage.class);
        messageHandlers.put(MessageType.DELTA_STATUS.getSerializedName(), DeltaStatusMessage.class);
        messageHandlers.put(MessageType.RESPONSE.getSerializedName(), ResponseMessage.class);
    }

    @Override
    public Message deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement messageType = jsonObject.get("type");

        if (messageType != null) {
            Class<?> clazz = messageHandlers.get(messageType.getAsString());
            if (clazz != null) {
                logger.trace("Deserializing message of type {}: {}", messageType, json);
                return context.deserialize(jsonObject, clazz);
            }
        }
        logger.debug("Unknown message type {}", messageType);
        return null;
    }
}
