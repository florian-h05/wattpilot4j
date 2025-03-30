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
package com.florianhotze.wattpilot.dto;

/**
 * Enum for the charging state.
 *
 * @author Florian Hotze - Initial contribution
 */
public enum ChargingState {
    /** No car connected. */
    NO_CAR(1),
    /** Car connected and charging. */
    CHARGING(2),
    /** Car connected and ready to charge, but not charging at the moment. */
    READY(3),
    /** Car connected and charging completed. */
    COMPLETE(4);

    private final int value;

    ChargingState(int value) {
        this.value = value;
    }

    /**
     * Get the API value of the charging state.
     *
     * @return the API value
     */
    public int toValue() {
        return value;
    }

    /**
     * Get the charging state from the API value.
     *
     * @param value the API value
     * @return the charging state
     */
    static ChargingState fromValue(int value) {
        for (ChargingState state : ChargingState.values()) {
            if (state.value == value) {
                return state;
            }
        }
        return null;
    }
}
