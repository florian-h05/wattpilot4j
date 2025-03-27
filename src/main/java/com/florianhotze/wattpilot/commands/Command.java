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

/**
 * Base class for all commands that can be sent to the wallbox.
 *
 * @author Florian Hotze - Initial contribution
 */
public abstract class Command {
    private final String key;

    Command(String key) {
        this.key = key;
    }

    /**
     * Returns the key of the command. See {@link com.florianhotze.wattpilot.dto.PropertyKeys} for a
     * list of all common keys.
     *
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the value of the command wrapped in a {@link CommandValue}.
     *
     * @return
     */
    public abstract CommandValue<?> getValue();
}
