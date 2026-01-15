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

import dev.digiried.wattpilot.dto.PropertyKeys;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Command to set the battery SoC limit for the charging boost in {@link
 * dev.digiried.wattpilot.dto.ChargingMode#ECO} or {@link
 * dev.digiried.wattpilot.dto.ChargingMode#NEXT_TRIP}.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class SetBoostSoCLimitCommand extends Command {
    private final CommandValue<Integer> value;

    public SetBoostSoCLimitCommand(int limit) {
        super(PropertyKeys.BOOST_BATTERY_SOC);
        if (limit < 0 || limit > 100) {
            throw new IllegalArgumentException("Threshold must be between 0 and 100 %");
        }
        this.value = new CommandValue<>(limit);
    }

    @Override
    public CommandValue<Integer> getValue() {
        return value;
    }
}
