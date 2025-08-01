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

import dev.digiried.wattpilot.messages.DeltaStatusMessage;
import dev.digiried.wattpilot.messages.FullStatusMessage;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for the (partial) status received from {@link FullStatusMessage} or {@link
 * DeltaStatusMessage}. The status may only contain a subset of the available properties, so <code>
 * null</code> must be allowed for all properties.
 *
 * @author Florian Hotze - Initial contribution
 */
public class PartialStatus {
    @SerializedName(PropertyKeys.ALLOW_CHARGING)
    Boolean allowCharging;

    @SerializedName(PropertyKeys.CHARGING_CURRENT)
    Integer chargingCurrent;

    @SerializedName(PropertyKeys.CAR_STATE)
    Integer carState;

    @SerializedName(PropertyKeys.ENERGY_COUNTER_TOTAL)
    Integer energyCounterTotal;

    @SerializedName("err")
    Integer errorState;

    @SerializedName(PropertyKeys.FORCE_STATE)
    Integer forceState;

    @SerializedName(PropertyKeys.FORCE_SINGLE_PHASE)
    Boolean forceSinglePhase;

    @SerializedName(PropertyKeys.STARTING_POWER)
    Float startingPower;

    @SerializedName(PropertyKeys.LOGIC_MODE)
    Integer logicMode;

    @SerializedName(PropertyKeys.CHARGING_ENERGY)
    Integer[] chargingEnergy;

    @SerializedName(PropertyKeys.ENERGY_COUNTER_SINCE_START)
    Double energyCounterSinceStart;

    /**
     * Check if charging is currently allowed.
     *
     * @return
     */
    public Boolean isChargingAllowed() {
        return allowCharging;
    }

    /**
     * The configured charging current in amperes.
     *
     * @return
     */
    public Integer getChargingCurrent() {
        return chargingCurrent;
    }

    /**
     * Get the charging state.
     *
     * @return
     */
    public ChargingState getChargingState() {
        if (carState == null) {
            return null;
        }
        return ChargingState.fromValue(carState);
    }

    /**
     * Get the total energy counter in watt-hours (Wh).
     *
     * @return
     */
    public Integer getEnergyCounterTotal() {
        return energyCounterTotal;
    }

    /**
     * Get the enforced charging state of the wallbox, i.e. whether charging is forcefully enabled,
     * disabled, or nothing is enforced.
     *
     * @return
     */
    public EnforcedChargingState getEnforcedChargingState() {
        if (forceState == null) {
            return null;
        }
        return EnforcedChargingState.fromValue(forceState);
    }

    /**
     * Whether single phase charging is currently used.
     *
     * @return
     */
    public Boolean isChargingSinglePhase() {
        return forceSinglePhase;
    }

    /**
     * PV surplus power threshold in watts (W). This is the minimum solar surplus power at which
     * o´solar surplus charging can be started.
     *
     * @return
     */
    public Float getSurplusPowerThreshold() {
        if (startingPower == null) {
            return null;
        }
        return startingPower;
    }

    /**
     * Get the configured charging mode.
     *
     * @return
     */
    public ChargingMode getChargingMode() {
        if (logicMode == null) {
            return null;
        }
        return ChargingMode.fromValue(logicMode);
    }

    /**
     * Get charging metrics like power, voltage and amperage.
     *
     * @return
     */
    public ChargingMetrics getChargingMetrics() {
        if (chargingEnergy == null || chargingEnergy.length != 16) {
            return null;
        }
        return new ChargingMetrics(chargingEnergy);
    }

    /**
     * Get the energy counter in watt-hours (Wh) since the start of the current charging session. If
     * no session is active, the counter since the start of the last charging session is returned.
     *
     * @return
     */
    public Double getEnergyCounterSinceStart() {
        return energyCounterSinceStart;
    }
}
