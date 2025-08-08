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
 * Command to set the PV surplus battery SoC threshold of the wallbox for {@link ChargingMode#ECO}
 * and {@link ChargingMode#NEXT_TRIP}.
 *
 * @author Florian Hotze - Initial contribution
 */
public class SetSurplusSoCThresholdCommand extends Command {
    private final CommandValue<Integer> value;

    public SetSurplusSoCThresholdCommand(int threshold) {
        super(PropertyKeys.STARTING_SOC);
        if (threshold < 0 || threshold > 100) {
            throw new IllegalArgumentException("Threshold must be between 0 and 100 %");
        }
        this.value = new CommandValue<>(threshold);
    }

    @Override
    public CommandValue<Integer> getValue() {
        return value;
    }
}
