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
package com.florianhotze.wattpilot.commands;

import com.florianhotze.wattpilot.dto.ChargingMode;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * JSON serializer for {@link CommandValue}.
 *
 * @author Florian Hotze - Initial contribution
 */
public class CommandValueSerializer implements JsonSerializer<CommandValue<?>> {
    @Override
    public JsonElement serialize(
            CommandValue<?> commandValue, Type type, JsonSerializationContext context) {
        if (commandValue.value() instanceof ChargingMode cm) {
            return new JsonPrimitive(cm.toValue());
        } else {
            return context.serialize(commandValue.value());
        }
    }
}
