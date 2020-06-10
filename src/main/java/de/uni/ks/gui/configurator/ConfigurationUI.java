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
package de.uni.ks.gui.configurator;

import de.uni.ks.MazeSimulator;
import de.uni.ks.agent.explorationPolicies.ExplorationPolicy;
import de.uni.ks.configuration.Config;
import de.uni.ks.configuration.ConfigManager;
import de.uni.ks.configuration.Identifiers;
import de.uni.ks.configuration.WritableToConfig;
import de.uni.ks.configuration.handlers.*;
import de.uni.ks.criterion.Criterion;
import de.uni.ks.gui.simulator.presenter.SimulatorPresenter;
import de.uni.ks.maze.complexityFunction.ComplexityFunction;
import de.uni.ks.maze.utils.mazeOperators.MazeOperator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The configuration assistant of the simulator. The user can load and store a config fiele, that was configured with the
 * help of this ui. The user can also start the training directly from this ui. The ui consists of input fields for
 * all keys in {@link Config}, the layout and behaviour of these ui components is defined in {@link UIFactory}.
 */
public class ConfigurationUI extends Application {

    HashMap<String, UIFactory.SingleInput<?>> singleInputMap = new HashMap<>();
    HashMap<String, UIFactory.MultiInput<?>> multiInputMap = new HashMap<>();

    private Stage configStage;
    private final VBox root = new VBox();

    private static final String DEFAULT_STAGE_TITLE = "Configuration Assistant";

    private final Identifiers identifiers = new Identifiers();

    // If the training was started with the ui, this holds a reference to that ui instance.
    private SimulatorPresenter simulatorPresenter;

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        configStage = primaryStage;

        // Sort all field per section.
        Map<String, List<Field>> sectionMap = new HashMap<>();
        for (Field f : Config.class.getFields()) {
            String sectionName = ConfigManager.getSectionName(f);

            sectionMap.putIfAbsent(sectionName, new ArrayList<>());

            sectionMap.get(sectionName).add(f);
        }

        TabPane tabPane = new TabPane();

        for (String sectionName : sectionMap.keySet()) {
            Tab tab = buildTabForSection(sectionName, sectionMap.get(sectionName));
            if (tab != null) { // tab is null if it would be empty otherwise.
                tabPane.getTabs().add(tab);
            }
        }

        Button loadButton = new Button("Load");
        loadButton.setOnMouseClicked((a) -> onLoadButtonClicked());
        Button storeButton = new Button("Save");
        storeButton.setOnMouseClicked((a) -> onStoreButtonClicked());
        Button startButton = new Button("Start training");
        startButton.setOnMouseClicked((a) -> onStartButtonClicked());

        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(2, 2, 2, 2));
        buttonBar.getChildren().addAll(loadButton, storeButton, startButton);

        root.getChildren().addAll(buttonBar, tabPane);
        root.heightProperty().addListener((observableValue, oldVal, newVal) ->
                tabPane.setPrefHeight(newVal.doubleValue()));

        // Load default config if possible.
        try (InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("defaultConfigUIConfig.cfg")) {
            if (inputStream != null) {
                StringBuilder sb = new StringBuilder();
                Reader reader = new BufferedReader(new InputStreamReader(inputStream,
                        Charset.forName(StandardCharsets.UTF_8.name())));
                int c;
                while ((c = reader.read()) != -1) {
                    sb.append((char) c);
                }

                Config config = ConfigManager.readConfig(sb.toString(), "Default Configuration");
                updateUI(config, true);

            } else {
                throw new Exception("Catch me, if you can!");
            }
        } catch (Exception e) {
            System.err.println("Getting default config file for config ui failed.");
        }

        configStage.setTitle(DEFAULT_STAGE_TITLE);
        Scene scene = new Scene(root, 800, 600);

        configStage.setMinHeight(400);
        configStage.setMinWidth(500);

        configStage.setScene(scene);
        configStage.show();
    }

    //    ############################################################################################################
    //                                                  Build tabs
    //    ############################################################################################################

    /**
     * Builds a {@link Tab} that can be added to a {@link TabPane}. The tab contains input fields for fields of
     * {@link Config}, that ui is split in a "simple configuration" and an "advanced configuration".
     *
     * @param title     The name of the tab.
     * @param fieldList A list containing all fields that should be added to this tab.
     * @return A {@link Tab} with some input fields, {@code null} if the tab would be empty.
     */
    private Tab buildTabForSection(String title, List<Field> fieldList) {

        List<Field> changeFields = fieldList.stream().filter(f -> !ConfigManager.isDoNotChangeField(f)).collect(Collectors.toList());
        List<Field> doNotChangeFields = fieldList.stream().filter(ConfigManager::isDoNotChangeField).collect(Collectors.toList());

        VBox inner = new VBox();
        inner.setPadding(new Insets(10, 10, 10, 10));

        root.heightProperty().addListener((observableValue, oldVal, newVal) ->
                inner.setPrefHeight(newVal.doubleValue() - 64));

        ScrollPane scrollPane = new ScrollPane(inner);
        scrollPane.setFitToWidth(true);

        if (!changeFields.isEmpty()) {
            Label doChangeTitle = new Label("Simple Configuration:\n");
            doChangeTitle.setStyle("-fx-font-weight: bold; -fx-underline: true;");
            doChangeTitle.setPadding(new Insets(0, 0, 10, 0));

            VBox changeBox = new VBox();
            changeBox.autosize();
            fillVBoxWithInputFields(changeBox, changeFields);

            Separator sep1 = new Separator();
            sep1.setPadding(new Insets(10, 0, 0, 0));
            Separator sep2 = new Separator();
            sep2.setPadding(new Insets(0, 0, 5, 0));

            changeBox.getChildren().addAll(sep1, sep2);
            if (changeBox.getChildren().size() > 0) inner.getChildren().addAll(doChangeTitle, changeBox);
        }
        if (!doNotChangeFields.isEmpty()) {
            Label doNotChangeTitle = new Label("Advanced Configuration:\n");
            doNotChangeTitle.setStyle("-fx-font-weight: bold; -fx-underline: true;");
            doNotChangeTitle.setPadding(new Insets(10, 0, 10, 0));

            VBox doNotChangeBox = new VBox(doNotChangeTitle);
            doNotChangeBox.autosize();
            fillVBoxWithInputFields(doNotChangeBox, doNotChangeFields);
            if (doNotChangeBox.getChildren().size() > 0) inner.getChildren().add(doNotChangeBox);
        }

        if (inner.getChildren().size() == 0) {
            return null;
        } else {
            Tab tab = new Tab(title, scrollPane);
            tab.setClosable(false);
            return tab;
        }
    }

    /**
     * Adds {@link UIFactory.Input} fields to a {@link VBox}.
     *
     * @param vBox      The {@link VBox} in which the input fields are placed.
     * @param fieldList A list containing all the fields that should be added to the box.
     */
    private void fillVBoxWithInputFields(VBox vBox, List<Field> fieldList) {
        for (Field field : fieldList) {
            String type = field.getType().getSimpleName().toLowerCase();

            if ("integer".equals(type) || "int".equals(type)) {
                vBox.getChildren().add(buildPrimitiveField(field, 0).build());
            } else if ("double".equals(type)) {
                vBox.getChildren().add(buildPrimitiveField(field, 0.0).build());
            } else if ("boolean".equals(type)) {
                vBox.getChildren().add(buildPrimitiveField(field, false).build());
            } else if (ExplorationPolicy.class.getSimpleName().toLowerCase().equals(type)) {
                vBox.getChildren().add(buildSingleSelectorBox(ExplorationPolicyHandler.packagePath,
                        ExplorationPolicyHandler.interfaceName,
                        identifiers.getExplorationPolicy()).build());
            } else if (ComplexityFunction.class.getSimpleName().toLowerCase().equals(type)) {
                vBox.getChildren().add(buildSingleSelectorBox(ComplexityFunctionHandler.packagePath,
                        ComplexityFunctionHandler.interfaceName,
                        identifiers.getComplexityFunction()).build());
            } else if (identifiers.getLevelChangeCriteria().equals(field.getName())) {
                vBox.getChildren().add(buildMultiSelectorBox(LevelChangeCriteriaHandler.packagePath,
                        LevelChangeCriteriaHandler.interfaceName,
                        identifiers.getLevelChangeCriteria()).build());
            } else if (identifiers.getEpisodeStoppingCriteria().equals(field.getName())) {
                vBox.getChildren().add(buildMultiSelectorBox(EpisodeStoppingCriteriaHandler.packagePath,
                        EpisodeStoppingCriteriaHandler.interfaceName,
                        identifiers.getEpisodeStoppingCriteria()).build());
            } else if (identifiers.getMazeOperators().equals(field.getName())) {
                vBox.getChildren().add(buildMultiSelectorBox(MazeOperatorHandler.packagePath,
                        MazeOperatorHandler.interfaceName,
                        identifiers.getMazeOperators()).build());
            } else if (!identifiers.getTrainingName().equals(field.getName())) {
                System.err.println("The field " + field.getName() + " of type " + type + " is not supported!");
                continue;
            }

            Separator sep = new Separator();
            sep.setPadding(new Insets(5, 0, 5, 0));
            vBox.getChildren().add(sep);
        }

        vBox.getChildren().remove(vBox.getChildren().size() - 1);
    }

    //    ############################################################################################################
    //                                                  Handle Buttons
    //    ############################################################################################################

    private final FileChooser.ExtensionFilter configExtensionFilter = new FileChooser.ExtensionFilter(
            "Config files", "*.cfg");
    private final FileChooser.ExtensionFilter allExtensionFilter = new FileChooser.ExtensionFilter(
            "All files", "*");

    /**
     * Starts a {@link FileChooser} dialog to load a {@link Config}-file and updates the ui to display the config.
     */
    private void onLoadButtonClicked() {
        FileChooser fc = new FileChooser();

        fc.getExtensionFilters().addAll(configExtensionFilter, allExtensionFilter);

        File file = fc.showOpenDialog(configStage);

        if (file != null) {

            try {
                Config config = ConfigManager.readConfig(file);
                configStage.setTitle(DEFAULT_STAGE_TITLE + " " + config.trainingName);

                ConfigManager.validateConfig(config);

                updateUI(config, false);
            } catch (Exception e) {
                e.printStackTrace();
                showConfigErrorDialog("An error occurred while loading the config file,\n" +
                                "please make sure the file is a valid configuration:",
                        e.getMessage());
            }
        }
    }

    /**
     * Updates the ui by updating every input field with the values from a {@link Config}.
     *
     * @param config The config from which the values are taken.
     */
    private void updateUI(Config config, boolean advancedOnly) {

        List<String> errors = new ArrayList<>();

        for (Field f : Config.class.getFields()) {
            String id = f.getName();
            String typeName = f.getType().getSimpleName().toLowerCase();

            if (advancedOnly && !ConfigManager.isDoNotChangeField(f)) continue;

            try {
                if (typeName.equals("list")) {

                    UIFactory.MultiInput<?> multiInputRaw = multiInputMap.get(id);

                    if (multiInputRaw == null) {
                        errors.add("No multi input field found for key [" + id + "]," +
                                " this could indicate that the configuration is incompatible " +
                                "with this version of the program.");
                        continue;
                    }

                    if (identifiers.getLevelChangeCriteria().equals(f.getName())) {
                        UIFactory.MultiInput<Criterion> multiInput
                                = (UIFactory.MultiInput<Criterion>) multiInputRaw;
                        multiInput.update(config.levelChangeCriteria);
                    } else if (identifiers.getEpisodeStoppingCriteria().equals(f.getName())) {
                        UIFactory.MultiInput<Criterion> multiInput
                                = (UIFactory.MultiInput<Criterion>) multiInputRaw;
                        multiInput.update(config.episodeStoppingCriteria);
                    } else if (identifiers.getMazeOperators().equals(f.getName())) {
                        UIFactory.MultiInput<MazeOperator> multiInput
                                = (UIFactory.MultiInput<MazeOperator>) multiInputRaw;
                        multiInput.update(config.mazeOperators);
                    } else {
                        System.err.println("An input field was found for key [" + id + "], but the type is not" +
                                " supported, this indicates an error in the code itself.");
                    }
                } else { // Handle all single inputs

                    UIFactory.SingleInput<?> singleInputRaw = singleInputMap.get(id);

                    if (singleInputRaw == null && !id.equals(identifiers.getTrainingName())) {
                        errors.add("No single input field found for key [" + id + "]," +
                                " this could indicate that the configuration is incompatible " +
                                "with this version of the program.");
                        continue;
                    }

                    if (typeName.equals(ExplorationPolicy.class.getSimpleName().toLowerCase())) {
                        UIFactory.SingleInput<ExplorationPolicy> singleInput
                                = (UIFactory.SingleInput<ExplorationPolicy>) singleInputRaw;
                        singleInput.update((ExplorationPolicy) f.get(config));
                    } else if (typeName.equals(ComplexityFunction.class.getSimpleName().toLowerCase())) {
                        UIFactory.SingleInput<ComplexityFunction> singleInput
                                = (UIFactory.SingleInput<ComplexityFunction>) singleInputRaw;
                        singleInput.update((ComplexityFunction) f.get(config));
                    } else if (typeName.equals("int")
                            || typeName.equals("integer")) {
                        UIFactory.SingleInput<Integer> singleInput = (UIFactory.SingleInput<Integer>) singleInputRaw;
                        singleInput.update((Integer) HandlerUtils.parseStringToType(f.get(config).toString(), typeName));
                    } else if (typeName.equals("double")) {
                        UIFactory.SingleInput<Double> singleInput = (UIFactory.SingleInput<Double>) singleInputRaw;
                        singleInput.update((Double) HandlerUtils.parseStringToType(f.get(config).toString(), typeName));
                    } else if (typeName.equals("boolean")) {
                        UIFactory.SingleInput<Boolean> singleInput = (UIFactory.SingleInput<Boolean>) singleInputRaw;
                        singleInput.update((Boolean) HandlerUtils.parseStringToType(f.get(config).toString(), typeName));
                    } else if (!identifiers.getTrainingName().equals(f.getName())) {
                        System.err.println("An input field was found for key [" + id + "], but the type is not" +
                                " supported, this indicates an error in the code itself.");
                    }
                }

            } catch (IllegalAccessException e) {
                // This should just not happen.
                e.printStackTrace();
            }
        }

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            for (String error : errors) {
                sb.append("- ").append(error).append("\n");
            }

            showConfigErrorDialog("Some errors occurred while reading the configuration:", sb.toString());
        }
    }

    /**
     * Checks if all the input fields in the ui contain valid values by calling {@link UIFactory.Input#validate()} on
     * every input field. This triggers displaying error messages on every invalid input field.
     *
     * @return True if one of the input fields is not valid, false if every input field is valid.
     */
    private boolean isCurrentInputInvalid() {

        boolean valid = true;

        List<String> listOfInvalids = new ArrayList<>();

        for (String key : singleInputMap.keySet()) {
            if (!singleInputMap.get(key).validate()) {
                valid = false;
                listOfInvalids.add(key);
            }
        }

        for (String key : multiInputMap.keySet()) {
            if (!multiInputMap.get(key).validate()) {
                valid = false;
                listOfInvalids.add(key);
            }
        }

        if (!valid) {
            StringBuilder sb = new StringBuilder();
            for (String s : listOfInvalids) {
                sb.append("- ").append(s).append("\n");
            }

            showConfigErrorDialog("Validating the input failed.",
                    "Please check the following input fields for error messages:"
                            + "\n" + sb.toString());
        }

        return !valid;
    }

    /**
     * Converts the configuration from the ui to an instance of {@link Config} if all input fields are valid.
     * Starts a {@link FileChooser} dialog to select the path where the config file should be saved.
     */
    private void onStoreButtonClicked() {

        if (isCurrentInputInvalid()) return;

        Config config = buildConfigFromUI();

        try {
            ConfigManager.validateConfig(config);
        } catch (IllegalArgumentException e) {
            showConfigErrorDialog("Validating the input failed.",
                    e.getMessage());
            return;
        }

        File file = new FileChooser().showSaveDialog(configStage);

        if (file != null) {

            config.trainingName = file.getName();

            if (!ConfigManager.writeConfigToFile(config, file)) {
                // Writing the file failed.
                showConfigErrorDialog("An error occurred while writing the config file:",
                        "Try saving under a different file name.");
            }
        }
    }

    /**
     * Gets the current value of every {@link UIFactory.Input} field of the ui and sets the
     * corresponding field in {@link Config}.
     *
     * @return An instance of {@link Config}.
     */
    private Config buildConfigFromUI() {
        Config config = new Config();
        config.trainingName = "PLACEHOLDER"; // This is needed to validate the config before showing the file picker.
        for (Field f : Config.class.getFields()) {
            String typeName = f.getType().getSimpleName().toLowerCase();

            try {
                if ("list".equals(typeName)) { // This is a multi input.
                    if (!multiInputMap.containsKey(f.getName()))
                        throw new ConfigManager.ConfigurationWriterException("The multi field " + f.getName()
                                + " has no corresponding input in ui.");
                    f.set(config, multiInputMap.get(f.getName()).getValues());
                } else { // This is a single input.
                    if (typeName.equals(ExplorationPolicy.class.getSimpleName().toLowerCase())) {
                        f.set(config, singleInputMap.get(identifiers.getExplorationPolicy()).getValue());
                    } else if (!singleInputMap.containsKey(f.getName()) && !f.getName()
                            .equals(identifiers.getTrainingName()))
                        throw new ConfigManager.ConfigurationWriterException("The single field " + f.getName()
                                + " has no corresponding input in ui.");
                    else if (!f.getName().equals(identifiers.getTrainingName())) {
                        f.set(config, singleInputMap.get(f.getName()).getValue());
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new ConfigManager.ConfigurationWriterException(e.getClass().getSimpleName()
                        + " was thrown, this should not happen!");
            }
        }

        return config;
    }

    /**
     * Starts the training with the configuration of the ui. The user can enter a {@link Config#trainingName}, and choose
     * whether to run the training with or without the {@link de.uni.ks.gui.simulator.presenter.SimulatorPresenter}.
     */
    private void onStartButtonClicked() {

        if (isCurrentInputInvalid()) return;

        Config config = buildConfigFromUI();

        try {
            ConfigManager.validateConfig(config);
        } catch (IllegalArgumentException e) {
            showConfigErrorDialog("Validating the input failed.",
                    e.getMessage());
            return;
        }

        try {
            File tempFile = File.createTempFile("MazeSimulator-", ".cfg");
            tempFile.deleteOnExit();

            UIFactory.StartTrainingDialog dialog = new UIFactory.StartTrainingDialog(configStage);
            Optional<UIFactory.StartTrainingDialog.Payload> startWithUI = dialog.showAndWait();

            if (startWithUI.isPresent()) {

                UIFactory.StartTrainingDialog.Payload payload = startWithUI.get();
                config.trainingName = payload.getTrainingName();

                ConfigManager.writeConfigToFile(config, tempFile);

                switch (payload.getSelection()) {
                    case 0:
                        configStage.close();
                        new Thread(() -> MazeSimulator.main(MazeSimulator.SET_CONFIG_ARG
                                + "=" + tempFile.getAbsolutePath())).start();
                        break;
                    case 1:
                        configStage.close();
                        this.simulatorPresenter = new SimulatorPresenter(config);
                        this.simulatorPresenter.start(configStage);
                        break;
                    default: // Dialog was closed without a selection.
                }

            } else {
                showConfigErrorDialog("Starting training failed.", "Unknown error.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (this.simulatorPresenter != null) simulatorPresenter.stop();
    }

    //    ############################################################################################################
    //                                                  Factory Methods
    //    ############################################################################################################

    /**
     * Builds an input field for a primitive value (e.g. Integer or Boolean) and adds the
     * {@link UIFactory.SingleInput} to {@link #singleInputMap}.
     * Currently the following types are supported:
     * * {@link Integer}
     * * {@link Double}
     * * {@link Boolean}
     *
     * @param id           The id is used as a key in the {@link #singleInputMap}.
     * @param defaultValue The default value of the input field.
     * @param <T>          The type of the input field.
     * @return An instance of {@link UIFactory.SingleInput}.
     */
    private <T> UIFactory.SingleInput<?> buildPrimitiveField(String id, T defaultValue) {
        if (singleInputMap.containsKey(id)) {
            throw new IllegalArgumentException("A primitive input field with id [" + id + "] already exists!");
        }

        UIFactory.SingleInput<?> inputField;

        if (defaultValue instanceof Number) {
            inputField = new UIFactory.TextInputField<>(id, (Number) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            inputField = new UIFactory.BoolInputField(id, (Boolean) defaultValue);
        } else {
            throw new IllegalArgumentException("Input field for " + id
                    + " cannot be created because the type is not supported.");
        }

        singleInputMap.put(id, inputField);
        return inputField;

    }

    /**
     * Wraps {@link #buildPrimitiveField(String, Object)} and uses the name of a {@link Field} as the id.
     *
     * @param field The name of this field is used as the id.
     */
    private <T> UIFactory.SingleInput<?> buildPrimitiveField(Field field, T defaultValue) {
        return buildPrimitiveField(field.getName(), defaultValue);
    }

    /**
     * Builds a {@link UIFactory.SingleSelectorBox} and adds it to {@link #singleInputMap}.
     *
     * @param packagePath   The package from which classes should be selected.
     * @param interfaceName The interface the classes must implement.
     * @param id            The id is used as a key in the {@link #singleInputMap}.
     * @param <T>           The type of the input field.
     * @return An instance of {@link UIFactory.SingleSelectorBox}.
     */
    private <T extends WritableToConfig> UIFactory.SingleSelectorBox<T> buildSingleSelectorBox(String packagePath,
                                                                                               String interfaceName,
                                                                                               String id) {

        if (singleInputMap.containsKey(id)) {
            throw new IllegalArgumentException("A primitive input field with id [" + id + "] already exists!");
        }

        UIFactory.SingleSelectorBox<T> singleSelectorBox
                = new UIFactory.SingleSelectorBox<>(packagePath, interfaceName, id);

        singleInputMap.put(id, singleSelectorBox);

        return singleSelectorBox;
    }

    /**
     * Builds a {@link UIFactory.MultiSelectorBox} and adds it to {@link #multiInputMap}.
     *
     * @param packagePath   The package from which classes should be selected.
     * @param interfaceName The interface the classes must implement.
     * @param id            The id is used as a key in the {@link #multiInputMap}.
     * @param <T>           The type of the input field.
     * @return An instance of {@link UIFactory.SingleSelectorBox}.
     */
    private <T extends WritableToConfig> UIFactory.MultiSelectorBox<T> buildMultiSelectorBox(String packagePath,
                                                                                             String interfaceName,
                                                                                             String id) {

        if (multiInputMap.containsKey(id)) {
            throw new IllegalArgumentException("A primitive input field with id [" + id + "] already exists!");
        }

        UIFactory.MultiSelectorBox<T> singleSelectorBox
                = new UIFactory.MultiSelectorBox<>(packagePath, interfaceName, id);

        multiInputMap.put(id, singleSelectorBox);

        return singleSelectorBox;
    }

    /**
     * Shows a dialog in the ui, the dialog resizes itself automatically to fit its content.
     *
     * @param headerText The headerText is shown under the title
     * @param error      This should contain the full error message.
     */
    private void showConfigErrorDialog(String headerText, String error) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Creating config failed");
        alert.setHeaderText(headerText);
        alert.setContentText(error);

        // This resizes the dialog to fit the message.
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }
}
