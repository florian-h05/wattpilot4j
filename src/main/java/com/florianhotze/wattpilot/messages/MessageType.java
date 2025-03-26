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

import java.lang.reflect.Field;

import com.google.gson.annotations.SerializedName;

/**
 * Enum for available WebSocket {@link Message} types.
 *
 * @author Florian Hotze - Initial contribution
 */
public enum MessageType {
    @SerializedName("hello")
    HELLO,
    @SerializedName("authRequired")
    AUTH_REQUIRED,
    @SerializedName("auth")
    AUTH,
    @SerializedName("authSuccess")
    AUTH_SUCCESS,
    @SerializedName("authError")
    AUTH_ERROR,
    @SerializedName("fullStatus")
    FULL_STATUS,
    @SerializedName("deltaStatus")
    DELTA_STATUS,
    @SerializedName("clearInverters")
    CLEAR_INVERTERS,
    @SerializedName("updateInverter")
    UPDATE_INVERTER,
    @SerializedName("securedMsg")
    SECURED_MSG,
    @SerializedName("response")
    RESPONSE;

    public String getSerializedName() {
        try {
            Field field = this.getClass().getField(this.name());
            SerializedName annotation = field.getAnnotation(SerializedName.class);
            return annotation != null ? annotation.value() : this.name();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return this.name();
        }
    }
}
