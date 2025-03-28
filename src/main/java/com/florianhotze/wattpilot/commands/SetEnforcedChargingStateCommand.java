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

import com.florianhotze.wattpilot.dto.EnforcedChargingState;
import com.florianhotze.wattpilot.dto.PropertyKeys;

/**
 * Command to set the enforced charging state of the wallbox.
 *
 * @author Florian Hotze - Initial contribution
 */
public class SetEnforcedChargingStateCommand extends Command {
    private final CommandValue<EnforcedChargingState> state;

    /**
     * Create a new enforced charging state command.
     *
     * @param state the enforced charging state
     */
    public SetEnforcedChargingStateCommand(EnforcedChargingState state) {
        super(PropertyKeys.FORCE_STATE);
        this.state = new CommandValue<>(state);
    }

    @Override
    public CommandValue<EnforcedChargingState> getValue() {
        return state;
    }
}
