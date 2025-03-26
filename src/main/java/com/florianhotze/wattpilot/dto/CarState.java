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
 * Enum for the state of the connected car.
 *
 * @author Florian Hotze - Initial contribution
 */
public enum CarState {
    NO_CAR(1),
    CHARGING(2),
    READY(3),
    COMPLETE(4);

    private final int value;

    CarState(int value) {
        this.value = value;
    }

    public int toValue() {
        return value;
    }

    static CarState fromValue(int value) {
        for (CarState state : CarState.values()) {
            if (state.value == value) {
                return state;
            }
        }
        return null;
    }
}
