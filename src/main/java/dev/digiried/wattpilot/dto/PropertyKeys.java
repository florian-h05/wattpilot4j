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
 * Constants for property keys in the WebSocket API. See <a
 * href="https://github.com/joscha82/wattpilot/blob/main/API.md">joscha82/wattpilot: Wattpilot API
 * Description</a>.
 *
 * @author Florian Hotze - Initial contribution
 */
public final class PropertyKeys {
    private PropertyKeys() {}

    /** Charging allowed/possible (RO) */
    public static final String ALLOW_CHARGING = "alw";

    /** Charging boost enabled (boost is only available in eco or next trip mode) (RW) */
    public static final String BOOST_ENABLED = "ebe";

    /**
     * SoC to discharge the battery to when boost is enabled (boost is only available in eco or next
     * trip mode) (RW)
     */
    public static final String BOOST_BATTERY_SOC = "ebt";

    /** Allowed current for charging in ampere (RW) */
    public static final String CHARGING_CURRENT = "amp";

    /** State of the charging process (RO) */
    public static final String CAR_STATE = "car";

    /** Error state (RO) */
    public static final String ERROR_STATE = "err";

    /** Total energy counter in watt-hours (RO) */
    public static final String ENERGY_COUNTER_TOTAL = "eto";

    /** Enforce a specific charging state (RW) */
    public static final String FORCE_STATE = "frc";

    /** Force-single phase charging (RW?) */
    public static final String FORCE_SINGLE_PHASE = "fsp";

    /** Starting power in watt for solar charging (RW) */
    public static final String STARTING_POWER = "fst";

    /** Starting SoC of battery for solar charging (RW) */
    public static final String STARTING_SOC = "fam";

    /** Charging mode (RW) */
    public static final String LOGIC_MODE = "lmo";

    /** Charging metrics like power, voltage, amperage (RO) */
    public static final String CHARGING_ENERGY = "nrg";

    /** Energy counter since the start of the charging session in watt-hours (RO) */
    public static final String ENERGY_COUNTER_SINCE_START = "wh";
}
