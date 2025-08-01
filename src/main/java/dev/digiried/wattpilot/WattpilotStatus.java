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
package dev.digiried.wattpilot;

import dev.digiried.wattpilot.dto.ChargingMetrics;
import dev.digiried.wattpilot.dto.ChargingMode;
import dev.digiried.wattpilot.dto.ChargingState;
import dev.digiried.wattpilot.dto.EnforcedChargingState;

/**
 * Class providing the current configuration and status of the Wattpilot.
 *
 * @author Florian Hotze - Initial contribution
 */
public class WattpilotStatus {
    private boolean chargingAllowed;
    private int chargingCurrent;
    private ChargingState chargingState;
    private EnforcedChargingState enforcedState;
    private boolean chargingSinglePhase;
    private float surplusPowerThreshold;
    private ChargingMode chargingMode;
    private ChargingMetrics chargingMetrics;
    private Double energyCounterSinceStart;
    private Integer energyCounterTotal;

    /** Create a new Wattpilot status. */
    protected WattpilotStatus() {}

    /**
     * Whether charging is currently allowed.
     *
     * @return whether charging is allowed
     */
    public boolean isChargingAllowed() {
        return chargingAllowed;
    }

    /**
     * Get the configured charging current.
     *
     * @return the configured charging current in amperes
     */
    public int getChargingCurrent() {
        return chargingCurrent;
    }

    /**
     * Get the current charging state.
     *
     * @return the current charging state
     */
    public ChargingState getChargingState() {
        return chargingState;
    }

    /**
     * Get the configured PV surplus power threshold in watts (W), i.e. the solar power surplus at
     * which solar surplus charging starts in {@link ChargingMode#ECO} and {@link
     * ChargingMode#NEXT_TRIP}.
     *
     * @return the configured PV surplus power threshold
     */
    public float getSurplusPowerThreshold() {
        return surplusPowerThreshold;
    }

    /**
     * Get the enforced charging state of the wallbox, i.e. whether charging forcefully enabled,
     * disabled, or the nothing is enforced.
     *
     * @return the enforced state
     */
    public EnforcedChargingState getEnforcedChargingState() {
        return enforcedState;
    }

    /**
     * Whether single phase charging is currently used.
     *
     * @return whether single phase charging is used
     */
    public boolean isChargingSinglePhase() {
        return chargingSinglePhase;
    }

    /**
     * Get the configured charging mode.
     *
     * @return the configured charge mode
     */
    public ChargingMode getChargingMode() {
        return chargingMode;
    }

    /**
     * Get the current charging metrics.
     *
     * @return the current charging metrics
     */
    public ChargingMetrics getChargingMetrics() {
        return chargingMetrics;
    }

    /**
     * Get the energy counter in watt-hours (Wh) since the start of the current charging session. If
     * no session is active, the counter since the start of the last session is returned.
     *
     * @return the energy counter of the current or the last charging session
     */
    public Double getEnergyCounterSinceStart() {
        return energyCounterSinceStart;
    }

    /**
     * Get the total energy counter in watt-hours (Wh).
     *
     * @return the total energy counter
     */
    public Integer getEnergyCounterTotal() {
        return energyCounterTotal;
    }

    void setChargingAllowed(boolean chargingAllowed) {
        this.chargingAllowed = chargingAllowed;
    }

    void setChargingCurrent(int chargingCurrent) {
        this.chargingCurrent = chargingCurrent;
    }

    void setChargingState(ChargingState chargingState) {
        this.chargingState = chargingState;
    }

    void setEnergyCounterSinceStart(Double energyCounterSinceStart) {
        this.energyCounterSinceStart = energyCounterSinceStart;
    }

    void setEnforcedState(EnforcedChargingState enforcedState) {
        this.enforcedState = enforcedState;
    }

    void setChargingSinglePhase(boolean chargingSinglePhase) {
        this.chargingSinglePhase = chargingSinglePhase;
    }

    void setSurplusPowerThreshold(float surplusPowerThreshold) {
        this.surplusPowerThreshold = surplusPowerThreshold;
    }

    void setChargingMode(ChargingMode chargingMode) {
        this.chargingMode = chargingMode;
    }

    void setChargingMetrics(ChargingMetrics chargingMetrics) {
        this.chargingMetrics = chargingMetrics;
    }

    void setEnergyCounterTotal(Integer energyCounterTotal) {
        this.energyCounterTotal = energyCounterTotal;
    }
}
