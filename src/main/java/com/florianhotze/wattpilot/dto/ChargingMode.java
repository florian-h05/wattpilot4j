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
 * Enum for the charging mode configuration.
 *
 * @author Florian Hotze - Initial contribution
 */
public enum ChargingMode {
    /** Default charging mode, i.e. charge with the configured current. */
    DEFAULT(3),
    /**
     * Eco charging mode, i.e. charge with solar power surplus or if the dynamic electricity tariff
     * is under the configured threshold.
     */
    ECO(4),
    /**
     * Similar to {@link #ECO}, but additionally ensures the car is charged with a specified amount
     * of energy by a set time by using grid power if necessary.
     */
    NEXT_TRIP(5);

    private final int value;

    ChargingMode(int value) {
        this.value = value;
    }

    /**
     * Get the API value of the charging mode.
     *
     * @return the API value
     */
    public int toValue() {
        return value;
    }

    /**
     * Get the charging mode from the API value.
     *
     * @param value the API value
     * @return the charging mode
     */
    static ChargingMode fromValue(int value) {
        for (ChargingMode mode : ChargingMode.values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        return null;
    }
}
