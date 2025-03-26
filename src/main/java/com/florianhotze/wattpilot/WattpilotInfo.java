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
package com.florianhotze.wattpilot;

/**
 * Record for Wattpilot information received on connection establishment.
 *
 * @param serial serial number of the wall box
 * @param hostname hostname of the wall box
 * @param friendlyName friendly name of the wall box, i.e. what is displayed in the app
 * @param firmwareVersion firmware version of the wall box
 * @param protocolVersion protocol version
 * @param secured whether the connection has to be authenticated
 * @author Florian Hotze - Initial contribution
 */
public record WattpilotInfo(
        String serial,
        String hostname,
        String friendlyName,
        String firmwareVersion,
        int protocolVersion,
        boolean secured) {}
