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
 * Command to set the charging current of the wallbox.
 *
 * @author Florian Hotze - Initial contribution
 */
public class SetChargingCurrentCommand extends Command {
    private final CommandValue<Integer> value;

    /**
     * Create a new charging current command.
     *
     * @param current the charging current in A between 6 and 32
     * @throws IllegalArgumentException if the current is not in the valid range
     */
    public SetChargingCurrentCommand(int current) throws IllegalArgumentException {
        super(PropertyKeys.CHARGING_CURRENT);
        if (current < 6 || current > 32) {
            throw new IllegalArgumentException("Charging current must be between 6 and 32 A");
        }
        this.value = new CommandValue<>(current);
    }

    @Override
    public CommandValue<Integer> getValue() {
        return value;
    }
}
