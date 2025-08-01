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
package dev.digiried.wattpilot.commands;

import dev.digiried.wattpilot.dto.ChargingMode;
import dev.digiried.wattpilot.dto.PropertyKeys;

/**
 * Command to set the PV surplus power threshold of the wallbox for {@link ChargingMode#ECO} and
 * {@link ChargingMode#NEXT_TRIP}.
 *
 * @author Florian Hotze - Initial contribution
 */
public class SetSurplusPowerThresholdCommand extends Command {
    private final CommandValue<Float> value;

    /**
     * Create a new PV surplus power threshold command.
     *
     * @param threshold the PV surplus power threshold in watts (W) between 1400 W and 22000 W
     */
    public SetSurplusPowerThresholdCommand(float threshold) {
        super(PropertyKeys.STARTING_POWER);
        if (threshold < 1400 || threshold >= 22000) {
            throw new IllegalArgumentException("Threshold must be between 1400 and 22000 W");
        }
        this.value = new CommandValue<>(threshold);
    }

    @Override
    public CommandValue<?> getValue() {
        return value;
    }
}
