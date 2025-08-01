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
package dev.digiried.wattpilot.dto;

/**
 * Enum for the enforced charging state configuration.
 *
 * @author Florian Hotze - Initial contribution
 */
public enum EnforcedChargingState {
    /** No enforced charging state, i.e. let the wallbox decide (default). */
    NEUTRAL(0),
    /** Disallow charging. */
    OFF(1),
    /** Force charging. */
    ON(2);

    private final int value;

    EnforcedChargingState(int value) {
        this.value = value;
    }

    /**
     * Get the API value of the enforced charging state.
     *
     * @return the API value
     */
    public int toValue() {
        return value;
    }

    /**
     * Get the enforced charging state from the API value.
     *
     * @param value the API value
     * @return the enforced charging state
     */
    public static EnforcedChargingState fromValue(int value) {
        for (EnforcedChargingState state : EnforcedChargingState.values()) {
            if (state.value == value) {
                return state;
            }
        }
        return null;
    }
}
