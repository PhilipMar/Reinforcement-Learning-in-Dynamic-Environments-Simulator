/*
    Copyright (C) 2020 Philip Martin and Timo Sturm

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, see <http://www.gnu.org/licenses/>.
*/
package de.uni.ks;

import de.uni.ks.configuration.Config;
import de.uni.ks.configuration.ConfigManager;
import de.uni.ks.gui.configurator.ConfigurationUI;
import de.uni.ks.gui.simulator.presenter.SimulatorPresenter;
import de.uni.ks.logging.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The main class of this program. Handles the start of the program, parsing console arguments and starting the simulation
 * or the configuration assistant.
 */
public class MazeSimulator {

    public static final String CONFIG_UI_ARG = "--configure";
    public static final String START_WITH_UI_ARG = "--showUI";
    public static final String SET_CONFIG_ARG = "--config";
    public static final String HELP_ARG = "-h";
    public static final String HELP_LONG_ARG = "--help";

    public static void main(String... args) {

        boolean showConfigUI = false;
        boolean showUI = false;
        String configPath = "";

        List<String> arguments = new ArrayList<>(Arrays.asList(args));

        if (arguments.contains(HELP_ARG) || arguments.contains(HELP_LONG_ARG)) {
            showHelp();
            return;
        }

        if (arguments.contains(CONFIG_UI_ARG)) showConfigUI = true;
        if (arguments.contains(START_WITH_UI_ARG)) showUI = true;

        for (String arg : arguments) {
            if (arg.startsWith(SET_CONFIG_ARG + "=") && !arg.equals(CONFIG_UI_ARG)) {
                if (showConfigUI) {
                    System.out.println("Starting the configuration assistant " +
                            "and starting the simulation is mutually exclusive!");
                    return;
                }

                configPath = arg.substring(arg.indexOf('=') + 1);
            }

            if (!arg.equals(CONFIG_UI_ARG)
                    && !arg.equals(START_WITH_UI_ARG)
                    && !arg.equals(HELP_ARG)
                    && !arg.equals(HELP_LONG_ARG)
                    && !arg.startsWith(SET_CONFIG_ARG)) {
                System.out.println("Unknown argument [" + arg + "]. Type " + HELP_ARG
                        + " to show all valid arguments.");
                return;
            }
        }

        if (showConfigUI) {
            javafx.application.Application.launch(ConfigurationUI.class);
        } else if (!configPath.isEmpty()) {
            try {
                File file = new File(configPath);
                Config config = ConfigManager.readConfig(file);
                ConfigManager.validateConfig(config);

                if (showUI) {
                    javafx.application.Application.launch(SimulatorPresenter.class, configPath);
                } else {
                    Training training = createTraining(config, false);
                    Logger.addTextToMiscLogOfCurrentTraining("Start training");
                    training.doTraining();
                    Logger.addTextToMiscLogOfCurrentTraining("Finished training");
                    Logger.writeLog();
                }
            } catch (IllegalArgumentException e) {
                System.out.println("An error occurred while running the program: " + e.getMessage());
            }
        } else {
            System.out.println("An error occurred while running the program, no configuration was provided:" +
                    " Type " + HELP_ARG + " to show all valid arguments for the program.");
        }
    }

    private static void showHelp() {
        System.out.println(
                "usage: MazeSimulator.jar [arguments]\n" +
                        "\n" +
                        "Start the simulator: (starting the simulation and starting the configuration assistant is" +
                        " mutually exclusive)\n" +
                        "\tMazeSimulator.jar " + SET_CONFIG_ARG + "=<path/to/config.cfg> (without user interface)\n" +
                        "\tMazeSimulator.jar " + SET_CONFIG_ARG + "=<path/to/config.cfg> " + START_WITH_UI_ARG +
                        " (with user interface)\n" +
                        "\n" +
                        "Start the configuration assistant:\n" +
                        "\tMazeSimulator.jar " + CONFIG_UI_ARG + "\n" +
                        "\n" +
                        "Show this help dialog:\n" +
                        "\tMazeSimulator.jar --help / -h (these arguments ignore all other arguments)");
    }

    public static Training createTraining(Config config, boolean guiWasStarted) {
        Training training = new Training(config);
        Logger.initLogger(config.trainingName, config, guiWasStarted);
        Logger.addTextToMiscLogOfCurrentTraining("Initialised training with config file");
        return training;
    }
}
