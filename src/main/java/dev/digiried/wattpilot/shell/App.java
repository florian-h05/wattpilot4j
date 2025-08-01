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
package dev.digiried.wattpilot.shell;

import dev.digiried.wattpilot.WattpilotClient;
import dev.digiried.wattpilot.WattpilotClientListener;
import dev.digiried.wattpilot.WattpilotInfo;
import dev.digiried.wattpilot.WattpilotStatus;
import dev.digiried.wattpilot.commands.Command;
import dev.digiried.wattpilot.commands.CommandResponse;
import dev.digiried.wattpilot.commands.SetChargingCurrentCommand;
import dev.digiried.wattpilot.commands.SetChargingModeCommand;
import dev.digiried.wattpilot.commands.SetEnforcedChargingStateCommand;
import dev.digiried.wattpilot.commands.SetSurplusPowerThresholdCommand;
import dev.digiried.wattpilot.dto.ChargingMode;
import dev.digiried.wattpilot.dto.EnforcedChargingState;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;

/**
 * Simple shell application to interact with a Wattpilot wallbox.
 *
 * @author Florian Hotze - Initial contribution
 */
@SuppressWarnings({"squid:S106", "squid:S2142", "CallToPrintStackTrace"})
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
        if (args.length != 2) {
            System.err.println("Please provide host and password arguments.");
            System.exit(-1);
        }
        new App(args[0], args[1]);
    }

    App(String host, String password) {
        Scanner scanner = new Scanner(System.in);

        try {
            HttpClient httpClient = new HttpClient();
            client = new WattpilotClient(httpClient, 10, 2);
            client.addListener(this);
            client.connect(host, password).get(3, TimeUnit.SECONDS);
        } catch (IOException | TimeoutException | InterruptedException | ExecutionException e) {
            System.err.println("Failed to connect to wallbox: " + e.getMessage());
            e.printStackTrace();
            scanner.close();
            System.exit(-1);
        }

        String line;
        while (client.isConnected()) {
            line = scanner.nextLine();
            if (line == null || line.equals("h") || line.equals("help")) {
                printHelp();
            } else if (line.equals("q")
                    || line.equals("quit")
                    || line.equals("e")
                    || line.equals("exit")) {
                break;
            } else if (line.equals("status")) {
                printStatus(client.getDeviceInfo(), client.getStatus());
            } else if (line.startsWith("set")) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    handleCommand(line, client);
                } else {
                    System.err.println("Invalid command: " + line);
                }
            } else {
                System.err.println("Unknown command: " + line);
            }
        }
        try {
            client.disconnect().get(3, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            // no handling needed here
        }
        scanner.close();
        System.exit(0);
    }

    @Override
    public void connected() {
        System.out.println("Wallbox connected");
    }

    @Override
    public void disconnected(String reason, Throwable cause) {
        if (reason != null) {
            System.out.println("Wallbox disconnected: " + reason);
        } else {
            System.out.println("Wallbox disconnected");
        }
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  status - get the current status of the wallbox");
        System.out.println("  set current <current> - set the charging current in A [6-32]");
        System.out.println("  set force <state> - set the enforced charging state (ON, OFF, NONE)");
        System.out.println("  set mode <mode> - set the charging mode (DEFAULT, ECO, NEXT_TRIP)");
        System.out.println(
                "  set threshold <threshold> - set the charging power threshold in W [1400-22000]");
        System.out.println("  q, quit, e, exit - quit the shell");
    }

    private static void printStatus(WattpilotInfo info, WattpilotStatus status) {
        if (info == null || status == null) {
            System.err.println("Failed to get status");
            return;
        }

        System.out.println("Wattpilot Device Info:");
        System.out.println("  Wallbox: " + info.friendlyName());
        System.out.println("  Serial Number: " + info.serial());
        System.out.println("  Firmware Version: " + info.firmwareVersion());

        System.out.println("Configuration:");
        System.out.println("  Charging Enforced: " + status.getEnforcedChargingState());
        System.out.println("  Charging Mode: " + status.getChargingMode());
        System.out.println("  Charging Current: " + status.getChargingCurrent() + " A");
        System.out.printf(
                "  PV Surplus Power Threshold: %.0f W%n", status.getSurplusPowerThreshold());

        System.out.println("Status:");
        System.out.println("  Charging State: " + status.getChargingState());
        System.out.println("  Charging Allowed: " + status.isChargingAllowed());
        System.out.println("  Single Phase Charging: " + status.isChargingSinglePhase());
        System.out.println("  Charging Metrics: " + status.getChargingMetrics());
        System.out.printf("  Charged Energy: %.0f Wh%n", status.getEnergyCounterSinceStart());
        System.out.printf("  Total Charged Energy: %d Wh%n", status.getEnergyCounterTotal());
    }

    private static void handleCommand(String line, WattpilotClient client) {
        String[] parts = line.split(" ");
        try {
            Command command =
                    switch (parts[1]) {
                        case "current" -> new SetChargingCurrentCommand(Integer.parseInt(parts[2]));
                        case "force" ->
                                new SetEnforcedChargingStateCommand(
                                        EnforcedChargingState.valueOf(parts[2]));
                        case "mode" -> new SetChargingModeCommand(ChargingMode.valueOf(parts[2]));
                        case "threshold" ->
                                new SetSurplusPowerThresholdCommand(Float.parseFloat(parts[2]));
                        default -> null;
                    };
            if (command != null) {
                CommandResponse res = client.sendCommand(command).get(5, TimeUnit.SECONDS);
                if (res != null && res.success()) {
                    System.out.println("Command successful");
                } else {
                    System.err.println("Command failed");
                }
            } else {
                System.err.println("Invalid command: " + line);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println("Failed to send command: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid argument: " + e.getMessage());
        }
    }
}
