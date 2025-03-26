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
package com.florianhotze.wattpilot.messages;

import com.florianhotze.wattpilot.dto.PartialStatus;

/**
 * Full Status Message. Set of messages received after successful connection. These messages contain
 * all properties of Wattpilot.
 *
 * <p>The status might only be sent partial, in this case more {@link FullStatusMessage}s will be
 * sent with additional properties.
 *
 * @author Florian Hotze - Initial contribution
 */
public class FullStatusMessage extends ResponseMessage {
    public PartialStatus status;

    FullStatusMessage() {
        super(MessageType.FULL_STATUS);
    }
}
