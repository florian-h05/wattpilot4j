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

import dev.digiried.wattpilot.dto.AuthorizationState;
import dev.digiried.wattpilot.dto.PropertyKeys;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Command to set the authorization state.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class SetAuthorizationStateCommand extends Command {
    private final CommandValue<AuthorizationState> value;

    /**
     * Create a new command to set the authorization state.
     *
     * @param state the authorization state
     */
    public SetAuthorizationStateCommand(AuthorizationState state) {
        super(PropertyKeys.AUTHORIZATION_STATE);
        this.value = new CommandValue<>(state);
    }

    @Override
    public CommandValue<AuthorizationState> getValue() {
        return value;
    }
}
