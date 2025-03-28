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
    private boolean chargingAllowed;
    private int chargingCurrent;
    private CarState carState;
    private boolean chargingSinglePhase;
    private float startingPower;
    private ChargingMode chargingMode;
    private ChargingMetrics chargingMetrics;

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
     * Get the current state of the (connected) car.
     *
     * @return the current state of the car
     */
    public CarState getCarState() {
        return carState;
    }

    /**
     * Get the configured charging power threshold, i.e. the power at which charging starts.
     *
     * @return the configured charging power threshold in kW
     */
    public float getChargingPowerThreshold() {
        return startingPower;
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

    void setChargingAllowed(boolean chargingAllowed) {
        this.chargingAllowed = chargingAllowed;
    }

    void setChargingCurrent(int chargingCurrent) {
        this.chargingCurrent = chargingCurrent;
    }

    void setCarState(CarState carState) {
        this.carState = carState;
    }

    void setChargingSinglePhase(boolean chargingSinglePhase) {
        this.chargingSinglePhase = chargingSinglePhase;
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
