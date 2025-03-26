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
 * Record for charging metrics.
 *
 * @param power total power in kilowatts
 * @param voltage1 phase 1 voltage in volts
 * @param voltage2 phase 2 voltage in volts
 * @param voltage3 phase 3 voltage in volts
 * @param amperage1 phase 1 amperage in amperes
 * @param amperage2 phase 2 amperage in amperes
 * @param amperage3 phase 3 amperage in amperes
 * @param power1 power of phase 1 in kilowatts
 * @param power2 power of phase 2 in kilowatts
 * @param power3 power of phase 3 in kilowatts
 * @author Florian Hotze - Initial contribution
 */
public record ChargingMetrics(
        float power,
        int voltage1,
        int voltage2,
        int voltage3,
        int amperage1,
        int amperage2,
        int amperage3,
        float power1,
        float power2,
        float power3) {
    ChargingMetrics(Integer[] chargingEnergy) {
        this(
                chargingEnergy[11] * 0.001f,
                chargingEnergy[0],
                chargingEnergy[1],
                chargingEnergy[2],
                chargingEnergy[4],
                chargingEnergy[5],
                chargingEnergy[6],
                chargingEnergy[7] * 0.001f,
                chargingEnergy[8] * 0.001f,
                chargingEnergy[9] * 0.001f);
    }

    @Override
    public String toString() {
        return String.format(
                "ChargingMetrics[power=%.1f kW, voltage1=%d V, voltage2=%d V, voltage3=%d V,"
                    + " amperage1=%d A, amperage2=%d A, amperage3=%d A, power1=%.1f kW, power2=%.1f"
                    + " kW, power3=%.1f kW]",
                power, voltage1, voltage2, voltage3, amperage1, amperage2, amperage3, power1,
                power2, power3);
    }
}
