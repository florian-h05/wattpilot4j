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
package com.florianhotze.wattpilot;

import com.florianhotze.wattpilot.dto.CarState;
import com.florianhotze.wattpilot.dto.ChargingMetrics;
import com.florianhotze.wattpilot.dto.ChargingMode;

/**
 * Class providing the current configuration & status of the Wattpilot.
 *
 * @author Florian Hotze - Initial contribution
 */
public class WattpilotStatus {
    private Boolean chargingAllowed;
    private Integer chargingCurrent;
    private CarState carState;
    private Boolean forceSinglePhase;
    private Float startingPower;
    private ChargingMode chargingMode;
    private ChargingMetrics chargingMetrics;

    /**
     * Whether charging is currently allowed.
     *
     * @return whether charging is allowed or <code>null</code> if unknown
     */
    public Boolean isChargingAllowed() {
        return chargingAllowed;
    }

    /**
     * Get the configured charging current.
     *
     * @return the configured charging current in amperes or <code>null</code> if unknown
     */
    public Integer getChargingCurrent() {
        return chargingCurrent;
    }

    /**
     * Get the current state of the (connected) car.
     *
     * @return the current state of the car or <code>null</code> if unknown
     */
    public CarState getCarState() {
        return carState;
    }

    /**
     * Get the configured starting power, i.e. the power at which charging starts.
     *
     * @return the configured starting power in kW or <code>null</code> if unknown
     */
    public Float getStartingPower() {
        return startingPower;
    }

    /**
     * Whether single phase charging is currently enforced.
     *
     * @return
     */
    public Boolean isSinglePhaseEnforced() {
        return forceSinglePhase;
    }

    /**
     * Get the configured charging mode.
     *
     * @return the configured charge mode or <code>null</code> if unknown
     */
    public ChargingMode getChargingMode() {
        return chargingMode;
    }

    /**
     * Get the current charging metrics.
     *
     * @return the current charging metrics or <code>null</code> if unknown
     */
    public ChargingMetrics getChargingMetrics() {
        return chargingMetrics;
    }

    void setChargingAllowed(boolean chargingAllowed) {
        this.chargingAllowed = chargingAllowed;
    }

    void setChargingCurrent(int chargingCurrent) {
        this.chargingCurrent = chargingCurrent;
    }

    void setCarState(CarState carState) {
        this.carState = carState;
    }

    void setForceSinglePhase(boolean forceSinglePhase) {
        this.forceSinglePhase = forceSinglePhase;
    }

    void setStartingPower(float startingPower) {
        this.startingPower = startingPower;
    }

    void setChargingMode(ChargingMode chargingMode) {
        this.chargingMode = chargingMode;
    }

    void setChargingMetrics(ChargingMetrics chargingMetrics) {
        this.chargingMetrics = chargingMetrics;
    }
}
