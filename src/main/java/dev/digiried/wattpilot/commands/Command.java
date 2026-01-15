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
 * Base class for all commands that can be sent to the wallbox.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public abstract class Command {
    private final String key;

    /**
     * Create a new command with the given key.
     *
     * @param key the key of the property to set with the command
     */
    Command(String key) {
        this.key = key;
    }

    /**
     * Returns the key of the command. See {@link PropertyKeys} for a list of all common keys.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the value of the command wrapped in a {@link CommandValue}.
     *
     * @return the value
     */
    public abstract CommandValue<?> getValue();
}
