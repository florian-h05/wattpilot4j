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
 * Constants for property keys in the WebSocket API. See <a
 * href="https://github.com/joscha82/wattpilot/blob/main/API.md">joscha82/wattpilot: Wattpilot API
 * Description</a>.
 *
 * @author Florian Hotze - Initial contribution
 */
public final class PropertyKeys {
    private PropertyKeys() {}

    public static final String ALLOW_CHARGING = "alw";
    public static final String CHARGING_CURRENT = "amp";
    public static final String CAR_STATE = "car";
    public static final String FORCE_STATE = "frc";
    public static final String FORCE_SINGLE_PHASE = "fsp";
    public static final String STARTING_POWER = "fst";
    public static final String LOGIC_MODE = "lmo";
    public static final String CHARGING_ENERGY = "nrg";
    public static final String ENERGY_COUNTER_SINCE_START = "wh";
}
