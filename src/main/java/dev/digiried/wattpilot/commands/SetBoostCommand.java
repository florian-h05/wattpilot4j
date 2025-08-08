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

/**
 * Command to enable or disable the charging boost in {@link
 * dev.digiried.wattpilot.dto.ChargingMode#ECO} or {@link
 * dev.digiried.wattpilot.dto.ChargingMode#NEXT_TRIP}.
 *
 * @author Florian Hotze - Initial contribution
 */
public class SetBoostCommand extends Command {
    private final CommandValue<Boolean> value;

    public SetBoostCommand(boolean enabled) {
        super(PropertyKeys.BOOST_ENABLED);
        this.value = new CommandValue<>(enabled);
    }

    @Override
    public CommandValue<Boolean> getValue() {
        return value;
    }
}
