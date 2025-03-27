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
package com.florianhotze.wattpilot.commands;

import com.florianhotze.wattpilot.dto.PropertyKeys;

/**
 * Command to set the charging power threshold of the wall box.
 *
 * @author Florian Hotze - Initial contribution
 */
public class SetChargingPowerThresholdCommand extends Command {
    private final CommandValue<Float> value;

    /**
     * Create a new charging power threshold command.
     *
     * @param threshold the charging power threshold in kW between 1.4 and 22.0
     */
    public SetChargingPowerThresholdCommand(float threshold) {
        super(PropertyKeys.STARTING_POWER);
        if (threshold <= 1.39 || threshold >= 22.0) {
            throw new IllegalArgumentException("Threshold must be between 1.4 and 22.0 kW");
        }
        this.value = new CommandValue<>(threshold * 1000);
    }

    @Override
    public CommandValue<?> getValue() {
        return value;
    }
}
