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
package com.florianhotze.wattpilot.shell;

import com.florianhotze.wattpilot.WattpilotClient;
import com.florianhotze.wattpilot.WattpilotClientListener;
import com.florianhotze.wattpilot.WattpilotInfo;
import com.florianhotze.wattpilot.WattpilotStatus;
import com.florianhotze.wattpilot.commands.Command;
import com.florianhotze.wattpilot.commands.SetChargingCurrentCommand;
import com.florianhotze.wattpilot.commands.SetChargingModeCommand;
import com.florianhotze.wattpilot.dto.ChargingMode;

import java.io.IOException;
import java.util.Scanner;

import org.eclipse.jetty.client.HttpClient;

/**
 * Simple shell application to interact with a Wattpilot wallbox.
 *
 * @author Florian Hotze - Initial contribution
 */
public class App implements WattpilotClientListener {
    private WattpilotClient client;

    /**
     * Main method to start the shell application.
     *
     * <p>Two arguments are expected: the host and the password of the wallbox.
     *
     * @param args
     */
    public static void main(String[] args) {
        new App(args[0], args[1]);
    }

    App(String host, String password) {
        Scanner scanner = new Scanner(System.in);

        try {
            HttpClient httpClient = new HttpClient();
            client = new WattpilotClient(httpClient);
            client.addListener(this);
            client.connect(host, password);
        } catch (IOException e) {
            System.err.println("Failed to connect to wallbox: " + e.getMessage());
            e.printStackTrace();
            scanner.close();
            System.exit(-1);
        }

        String line;
        while (true) {
            line = scanner.nextLine();
            if (line == null || line.equals("q") || line.equals("quit")) {
                break;
            }
            if (line.equals("status")) {
                WattpilotInfo wattpilotInfo = client.getDeviceInfo();
                WattpilotStatus status = client.getStatus();
                System.out.println("Wall Box: " + wattpilotInfo.friendlyName());
                System.out.println("Serial Number: " + wattpilotInfo.serial());
                System.out.println("Firmware Version: " + wattpilotInfo.firmwareVersion());
                System.out.println("Car Status: " + status.getCarState());
                System.out.println("Charging Allowed: " + status.isChargingAllowed());
                System.out.println("Charging Mode: " + status.getChargingMode());
                System.out.println(
                        "Enforce Single Phase Charging: " + status.isSinglePhaseEnforced());
                System.out.println("Charging Current: " + status.getChargingCurrent() + " A");
                System.out.println("Charging Metrics: " + status.getChargingMetrics());
            }
            if (line.startsWith("set")) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    try {
                        Command command =
                                switch (parts[1]) {
                                    case "current" ->
                                            new SetChargingCurrentCommand(
                                                    Integer.parseInt(parts[2]));
                                    case "mode" ->
                                            new SetChargingModeCommand(
                                                    ChargingMode.valueOf(parts[2]));
                                    default -> null;
                                };
                        if (command != null) {
                            client.sendCommand(command);
                        } else {
                            System.err.println("Invalid command: " + line);
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to send command: " + e.getMessage());
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid argument: " + e.getMessage());
                    }
                } else {
                    System.err.println("Invalid command: " + line);
                }
            }
        }
        client.disconnect();
        scanner.close();
        System.exit(0);
    }

    @Override
    public void connected() {
        System.out.println("Wallbox connected");
    }

    @Override
    public void disconnected(String reason) {
        System.out.println("Wallbox disconnected: " + reason);
    }
}
