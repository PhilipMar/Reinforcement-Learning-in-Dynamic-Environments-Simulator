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

import de.uni.ks.configuration.ConfigManager;
import de.uni.ks.configuration.WritableToConfig;
import de.uni.ks.configuration.handlers.HandlerUtils;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class that provides helper classes to build the ui of {@link ConfigurationUI}.
 */
public class UIFactory {

    /**
     * Interface for general input helper classes.
     */
    protected interface Input {

        /**
         * Validates the current value of the input field.
         *
         * @return True if the input is valid, false if not.
         */
        boolean validate();

        /**
         * Simulates building an ui element. The contract is that the {@link Node} returned by this method will never
         * change, only the content of it will be changed.
         *
         * @return A {@link Node} that can be added to a ui.
         */
        Node build();

        /**
         * The id can be used to identify an input field. This could be the name of a filed in
         * {@link de.uni.ks.configuration.Config} or a class name. It is not guaranteed that this value is actually
         * unique.
         *
         * @return The id of the input field.
         */
        String getId();
    }

    /**
     * Interface for all input fields that allow one (1) input.
     *
     * @param <T> The type of the input.
     */
    protected interface SingleInput<T> extends Input {

        /**
         * @return The current value of the input field
         */
        T getValue();

        /**
         * Sets the inputs field to a specific value.
         *
         * @param value The value to which the input field is set.
         */
        void update(T value);
    }

    /**
     * Interface for input fields that allow many inputs.
     *
     * @param <T> The type of this input.
     */
    protected interface MultiInput<T> extends Input {

        /**
         * @return A list of all configured input values.
         */
        List<T> getValues();

        /**
         * Update the ui with a list of values.
         *
         * @param values List of values which should be displayed.
         */
        void update(List<T> values);
    }

    /**
     * Helper class for building the ui. This class provides a selector for multiple selections.
     */
    protected static class MultiSelectorBox<T extends WritableToConfig> implements MultiInput<T> {

        private final String id;

        private final MenuButton menuButton = new MenuButton();
        private final VBox staticUI = new VBox(); // This is returned by build().
        private final VBox uiInputBox = new VBox(); // This holds all the class input fields.

        private final Label errorLabel = new Label();
        private final List<ClassInputField<T>> currentInputFields = new ArrayList<>();

        /**
         * This class is used to input multiple {@link Class} values. The user can choose from a set of classes
         * and can configure the the selected classes with a {@link ClassInputField}. Multiple classes from the
         * package can be configured at once.
         *
         * @param packageName   The package from which the classes can be selected.
         * @param interfaceName The interface that the classes must implement.
         * @param id            The identifier of this input field.
         */
        protected MultiSelectorBox(String packageName, String interfaceName, String id) {
            this.id = id;
            staticUI.autosize();

            errorLabel.setStyle("-fx-text-fill: red;");

            uiInputBox.setPadding(new Insets(0, 0, 0, 15));

            List<Class<T>> availableChoices = getAllValidClassesFromPackage(packageName, interfaceName);

            LinkedList<String> interfaceSplit = new LinkedList<>(Arrays.asList(interfaceName.split("\\.")));
            menuButton.setText("Please select a " + interfaceSplit.getLast() + " to add.");

            Label titleLabel = new Label(id);
            titleLabel.setPadding(new Insets(0, 0, 10, 0));

            Label placeholder = new Label(); // Padding on the menuButton does not work.
            placeholder.setVisible(false);

            staticUI.getChildren().addAll(titleLabel, menuButton, placeholder, uiInputBox);

            for (Class<T> clz : availableChoices) {
                MenuItem menuItem = new MenuItem(clz.getSimpleName());
                menuItem.setId(clz.getSimpleName());

                menuItem.addEventHandler(EventType.ROOT, actionEvent -> {

                    VBox vBox = new VBox();

                    ClassInputField<T> inputField = new ClassInputField<>(clz.getSimpleName(), clz, null);

                    currentInputFields.add(inputField);

                    Button removeButton = new Button("-");
                    removeButton.setOnMouseClicked(mouseEvent -> {
                        uiInputBox.getChildren().remove(vBox); // Remove from ui.
                        currentInputFields.remove(inputField); // Remove from this.
                    });

                    BorderPane header = new BorderPane();
                    Label label = new Label(clz.getSimpleName());
                    label.setStyle("-fx-font-weight: bold");
                    header.setLeft(label);
                    header.setRight(removeButton);

                    Separator separator = new Separator();
                    separator.setPadding(new Insets(5, 0, 5, 0));

                    vBox.getChildren().addAll(header, inputField.build(), separator);

                    uiInputBox.getChildren().add(vBox);
                });

                menuButton.getItems().add(menuItem);
            }

            if (availableChoices.isEmpty()) {
                staticUI.getChildren().add(new Label("There is no choice available!"));
            }
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Node build() {
            return staticUI;
        }

        @Override
        public boolean validate() {

            if (currentInputFields.isEmpty()) {
                showErrorLabel("The input on this field may not be empty. Please add an element here!");
                return false;
            }

            if (!currentInputFields.stream().allMatch(ClassInputField::validate)) return false;

            hideErrorLabel();
            return true;
        }

        private void showErrorLabel(String msg) {
            errorLabel.setText(msg);
            if (!staticUI.getChildren().contains(errorLabel)) {
                staticUI.getChildren().add(errorLabel);
            }
        }

        private void hideErrorLabel() {
            staticUI.getChildren().remove(errorLabel);
        }

        @Override
        public List<T> getValues() {
            return validate() ? currentInputFields.stream()
                    .map(ClassInputField::getValue).collect(Collectors.toList()) : null;
        }

        @Override
        public void update(List<T> values) {
            if (values == null) throw new IllegalArgumentException("Parameter [values] must not be null!");

            uiInputBox.getChildren().clear(); // Clear ui.
            currentInputFields.clear(); // Clear logic.

            List<Input> alreadyUpdatedInputs = new ArrayList<>();

            values.forEach(v -> {

                String name = v.getClass().getSimpleName();
                Optional<MenuItem> menuItem = menuButton.getItems().stream() // Get the menu item with the right id.
                        .filter(i -> i.getId().equals(name)).findFirst();

                if (menuItem.isPresent()) {
                    menuItem.get().fire();

                    Optional<ClassInputField<T>> inputField
                            = currentInputFields.stream()
                            .filter(i -> !alreadyUpdatedInputs.contains(i)) // Only update the newly added input field.
                            .filter(i -> i.getId().equals(name))
                            .findFirst();

                    if (inputField.isPresent()) {
                        ClassInputField<T> i = inputField.get();
                        alreadyUpdatedInputs.add(i);
                        i.update(v);
                    } else {
                        System.err.println("Updating " + getId() + " failed: The button for " + name + " was fired," +
                                " but no input field was found.");
                    }
                } else {
                    System.err.println("Updating " + getId() + " failed: No button for " + name + " was found.");
                }
            });

            validate();
        }
    }

    /**
     * Helper class for building the ui. This class provides a selector for a single selection.
     */
    public static class SingleSelectorBox<T extends WritableToConfig> implements SingleInput<T> {

        private final String id;
        private final MenuButton menuButton = new MenuButton();

        private ClassInputField<T> currentInputField;
        private final VBox currentUI = new VBox(); // This contains the current class input field.
        private final VBox staticUI; // This is returned by build().

        /**
         * This class is used to input a single {@link Class} value. The user can choose from a set of classes
         * and can configure the the selected class with a {@link ClassInputField}. Only one class at a time can
         * be configured with this.
         *
         * @param packageName   The package from which the classes can be selected.
         * @param interfaceName The interface that the classes must implement.
         * @param id            The identifier of this input field.
         */
        public SingleSelectorBox(String packageName, String interfaceName, String id) {
            this.id = id;
            staticUI = new VBox();

            currentUI.setPadding(new Insets(0, 0, 0, 15));

            Label placeholder = new Label(); // Padding on the menuButton does not work.
            placeholder.setVisible(false);

            Label titleLabel = new Label(id);
            titleLabel.setPadding(new Insets(0, 0, 10, 0));
            List<Class<T>> availableChoices = getAllValidClassesFromPackage(packageName, interfaceName);
            staticUI.getChildren().addAll(titleLabel, menuButton, placeholder, currentUI);

            menuButton.setText("Please select one of the items");

            for (Class<T> clz : availableChoices) {
                MenuItem menuItem = new MenuItem(clz.getSimpleName());
                menuItem.setId(clz.getSimpleName());
                menuItem.setOnAction(actionEvent -> menuButton.setText(menuItem.getText()));

                menuItem.addEventHandler(EventType.ROOT, actionEvent -> {

                    Label header = new Label(clz.getSimpleName());
                    header.setStyle("-fx-font-weight: bold");

                    // Update this instance.
                    currentInputField = new ClassInputField<>(clz.getSimpleName(), clz,
                            header);
                    currentUI.getChildren().clear();
                    currentUI.getChildren().add(currentInputField.build());

                    // Update the ui.
                    ObservableList<Node> children = staticUI.getChildren();
                    children.remove(children.size() - 1);
                    children.add(currentUI);
                });

                menuButton.getItems().add(menuItem);
            }

            if (availableChoices.isEmpty()) currentUI.getChildren().add(new Label("There is no choice available!"));
        }

        @Override
        public Node build() {
            return staticUI;
        }

        @Override
        public boolean validate() {
            return currentInputField != null && currentInputField.validate();
        }

        @Override
        public T getValue() {
            return validate() ? currentInputField.getValue() : null;
        }

        @Override
        public void update(T value) {
            String className = value.getClass().getSimpleName();

            for (MenuItem item : menuButton.getItems()) {
                if (item.getId().equals(className)) {
                    item.fire();
                    currentInputField.update(value);
                    validate();
                    return;
                }
            }

            System.err.println("The class " + value.getClass().getName() + " could not be used to update " + getId());
        }

        @Override
        public String getId() {
            return this.id;
        }
    }

    /**
     * Helper class for building the ui. This class handles classes.
     */
    protected static class ClassInputField<T extends WritableToConfig> implements SingleInput<T> {

        private final String id;
        private final Node header;

        private final Label errorLabel = new Label();

        private final VBox staticUI = new VBox();

        private Constructor<T> constructor;
        private List<SingleInput<?>> constructorParameters = new ArrayList<>();
        private T currentValue;

        /**
         * This class is used to input {@link Class} values. Currently supported types:
         * * everything that implements {@link WritableToConfig} (this is needed for updating).
         *
         * @param id     The identifier of this input field.
         * @param clz    The class which this input field should configure (this can be changed later by
         *               calling {@link #update(WritableToConfig)}.
         * @param header An optional header for the ui. If present this is placed above the input fields.
         */
        protected ClassInputField(String id, Class<T> clz, Node header) {
            this.id = id;
            this.header = header;

            staticUI.autosize();

            errorLabel.setStyle("-fx-text-fill: red;");

            constructor = getConstructor(clz);

            this.constructorParameters = buildInputFields(new HashMap<>());
        }

        /**
         * Gets the constructor of a class. Also validates, that the class has exactly one constructor.
         *
         * @param clz The class of which the constructor is returned.
         * @return The constructor of {@code clz}.
         */
        private Constructor<T> getConstructor(Class<T> clz) {
            Constructor<T>[] declaredConstructors = (Constructor<T>[]) clz.getDeclaredConstructors();

            if (declaredConstructors.length > 1) {
                throw new IllegalArgumentException(this.getClass().getSimpleName()
                        + " only supports classes with 0 or 1 constructors, ["
                        + clz.getSimpleName() + "] has " + declaredConstructors.length);
            } else {
                return declaredConstructors[0];
            }
        }

        /**
         * Builds an input field for each parameter of the current {@link #constructor}. Default values for the input
         * fields can be provided in a map. The input fields are stored in in {@link #constructorParameters} and replace
         * all curent fields in that list.
         *
         * @param argumentMap A map containing default values for the constructor parameters.
         * @return A list of single input fiels, one for each key in {@code argumentMap}.
         */
        private List<SingleInput<?>> buildInputFields(Map<String, String> argumentMap) {

            List<SingleInput<?>> inputFields = new ArrayList<>();

            for (Parameter p : constructor.getParameters()) {
                String name = p.getName();
                String type = p.getType().getSimpleName().toLowerCase();

                switch (type) {
                    case "integer":
                    case "int":
                        inputFields.add(new TextInputField<>(name,
                                (Integer) HandlerUtils.parseStringToType(argumentMap
                                        .getOrDefault(name, "0"), type)));
                        break;
                    case "double":
                        inputFields.add(new TextInputField<>(name,
                                (Double) HandlerUtils.parseStringToType(argumentMap
                                        .getOrDefault(name, "0.0"), type)));
                        break;
                    case "boolean":
                        inputFields.add(new BoolInputField(name,
                                (Boolean) HandlerUtils.parseStringToType(argumentMap
                                        .getOrDefault(name, "false"), type)));
                        break;
                    default:
                        throw new IllegalArgumentException("The constructor of " + this.getClass().getSimpleName()
                                + " has an invalid type as a parameter: " + name + " is " + type);
                }
            }

            return inputFields;
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean validate() {
            for (SingleInput<?> f : constructorParameters) {
                if (!f.validate()) {
                    return false;
                }
            }

            Map<String, String> parameterMap = new HashMap<>();

            for (SingleInput<?> f : constructorParameters) {
                parameterMap.put(f.getId(), f.getValue().toString());
            }

            try {
                Object[] arguments = HandlerUtils.createArgumentsForNewInstance(constructor.getParameters(), parameterMap);
                currentValue = (T) constructor.newInstance(arguments);
            } catch (InvocationTargetException e) {
                // This is caused by an IllegalArgumentException of the constructor.
                showErrorLabel(e.getCause().getMessage());
                return false;
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }

            hideErrorLabel();
            return true;
        }

        /**
         * Build an input box for the current {@link #constructorParameters}. This box will contain input fields
         * for all the parameters. This is used to update {@link #staticUI} to contain all the input fields.
         *
         * @return {@link VBox} containing the current {@link #header} (if it is not null) and all input fields in
         * {@link #constructorParameters}.
         */
        private VBox buildClassInputBox() {
            VBox vBox = new VBox();
            vBox.autosize();

            if (header != null) {
                vBox.getChildren().add(header);
            }

            for (SingleInput<?> f : constructorParameters) {
                vBox.getChildren().add(f.build());
            }

            return vBox;
        }

        @Override
        public Node build() {
            staticUI.getChildren().add(buildClassInputBox());

            return staticUI;
        }

        private void showErrorLabel(String msg) {
            errorLabel.setText(msg);
            if (!staticUI.getChildren().contains(errorLabel)) {
                staticUI.getChildren().add(errorLabel);
            }
        }

        private void hideErrorLabel() {
            staticUI.getChildren().remove(errorLabel);
        }

        @Override
        public T getValue() {
            return validate() ? currentValue : null;
        }

        @Override
        public void update(T value) {
            if (value == null) throw new IllegalArgumentException("Parameter [value] must not be null!");

            // Update logic.
            this.currentValue = value;

            // Update ui.
            String configStr = value.myConfigString();
            Map<String, String> parsedArgumentMap = HandlerUtils.parseParameters(configStr);

            this.constructor = getConstructor((Class<T>) value.getClass());
            this.constructorParameters = buildInputFields(parsedArgumentMap);
            staticUI.getChildren().clear();
            staticUI.getChildren().add(buildClassInputBox());

            validate();
        }
    }

    /**
     * Helper class for building the ui. This class handles Number values.
     */
    protected static class TextInputField<T extends Number> implements SingleInput<T> {

        private final String id;
        private final T defaultValue;

        private T currentValue;

        private final TextField inputField;
        private final Label errorLabel;

        /**
         * This class is used to input {@link Number} values. Currently supported types:
         * * {@link Integer}
         * * {@link Double}
         *
         * @param id           The identifier of this input field, this is also used as a label in the ui.
         * @param defaultValue The default value of this input field.
         */
        protected TextInputField(String id, T defaultValue) {
            this.id = id;
            this.defaultValue = defaultValue;
            currentValue = defaultValue;

            // Only Integer and Double are supported.
            if (!(defaultValue instanceof Integer)
                    && !(defaultValue instanceof Double)) {
                throw new IllegalArgumentException(this.getClass().getSimpleName()
                        + " does not support the input type [" + defaultValue.getClass().getSimpleName() + "] .");
            }

            inputField = new TextField(defaultValue.toString());
            inputField.setAlignment(Pos.CENTER_RIGHT);
            inputField.setOnMouseClicked(e -> inputField.selectAll());
            inputField.textProperty().addListener((observable, oldValue, newValue)
                    -> validate());

            errorLabel = new Label("Please enter a valid " + defaultValue.getClass()
                    .getSimpleName().toLowerCase());
            errorLabel.setVisible(false);
            errorLabel.setStyle("-fx-text-fill: red;");
        }

        public String getId() {
            return id;
        }

        @Override
        public T getValue() {
            return currentValue;
        }

        @Override
        public void update(T value) {
            if (value == null) throw new IllegalArgumentException("Parameter [value] must not be null!");
            this.inputField.setText(value.toString()); // The method should only accept values of the right type.
        }

        @Override
        public boolean validate() {
            boolean isValid;

            try {
                if (defaultValue instanceof Integer) {
                    currentValue = (T) Integer.valueOf(Integer.parseInt(inputField.getText()));
                } else if (defaultValue instanceof Double) {
                    currentValue = (T) Double.valueOf(Double.parseDouble(inputField.getText()));
                }

                isValid = true;
            } catch (NumberFormatException | ClassCastException e) {
                isValid = false;
            }

            if (isValid) {
                errorLabel.setVisible(false);
                inputField.setStyle(""); // Remove red border.
            } else {
                errorLabel.setVisible(true);
                inputField.setStyle(" -fx-text-box-border: red ; -fx-focus-color: red ;");
            }

            return isValid;
        }

        @Override
        public Node build() {
            VBox labelBox = new VBox();
            Label spacing = new Label();
            spacing.setVisible(false);
            labelBox.getChildren().addAll(spacing, new Label(id));

            VBox inputBox = new VBox();
            inputBox.getChildren().addAll(errorLabel, inputField);

            BorderPane pane = new BorderPane();
            pane.setLeft(labelBox);
            pane.setRight(inputBox);

            return pane;
        }
    }

    /**
     * Helper class for building the ui. This class handles boolean values.
     */
    protected static class BoolInputField implements SingleInput<Boolean> {

        private final String id;

        private final CheckBox inputField;

        /**
         * This class is used to input boolean values.
         *
         * @param id           The identifier of the instance, this is also used for labeling the input field in the ui.
         * @param defaultValue The default value of this input field.
         */
        protected BoolInputField(String id, Boolean defaultValue) {
            this.id = id;
            this.inputField = new CheckBox();
            inputField.setSelected(defaultValue);
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean validate() {
            return true; // Invalid inputs cannot be made here.
        }

        @Override
        public Node build() {
            BorderPane pane = new BorderPane();
            pane.setLeft(new Label(id));
            pane.setRight(inputField);
            pane.setPadding(new Insets(10, 0, 10, 0));

            return pane;
        }

        @Override
        public Boolean getValue() {
            return inputField.isSelected();
        }

        @Override
        public void update(Boolean value) {
            if (value == null) throw new IllegalArgumentException("Parameter [value] must not be null!");
            this.inputField.setSelected(value);
        }
    }

    /**
     * Returns a list of all classes that are located directly in a specific package. A class is valid if it meets
     * the following conditions:
     * * It has exactly one (1) constructor
     * * It implements the specified interface
     * * It is not an interface
     *
     * @param packageName   The path of the package, where the classes are.
     * @param interfaceName The path of the interface that the classes must implement.
     * @param <T>           The type that the classes must be of, usually this is the interface with {@code interfaceName}.
     * @return A list of classes of type T.
     */
    private static <T> List<Class<T>> getAllValidClassesFromPackage(String packageName, String interfaceName) {

        return ConfigManager.getClassesFromPackage(packageName).stream()
                .filter(c -> !c.isInterface())
                .filter(c -> HandlerUtils.implementsInterface(c, interfaceName))
                .filter(c -> {
                    Constructor<?>[] declaredConstructors = c.getDeclaredConstructors();
                    if (declaredConstructors.length != 1) {
                        System.out.println("Currently, exactly one constructor per class is supported.\n" +
                                "[" + c.getSimpleName() + "] has [" + declaredConstructors.length + "].");
                        return false;
                    }
                    return true;
                })
                .map(c -> (Class<T>) c)
                .collect(Collectors.toList());
    }

    /**
     * A custom dialog to ask the user, how the training should be started. it contains a {@link TextField} to enter the
     * training name and two buttons. One button to start the training without the ui, and one to start the training with
     * the ui.
     * <p>
     * The decisions made in the dialog are returned wrapped in a {@link Payload}.
     */
    protected static class StartTrainingDialog extends Dialog<StartTrainingDialog.Payload> {

        /**
         * Used to wrap two return values in one class.
         */
        protected static class Payload {
            private final int selection;
            private final String trainingName;

            private Payload(int selection, String trainingName) {
                this.selection = selection;
                this.trainingName = trainingName;
            }

            public int getSelection() {
                return selection;
            }

            public String getTrainingName() {
                return trainingName;
            }
        }

        protected StartTrainingDialog(Stage primaryStage) {
            super();
            initOwner(primaryStage);

            BorderPane root = new BorderPane();
            Label titleLabel = new Label("Please enter a name for the training and" +
                    " choose how you would like to run it:");
            titleLabel.setPadding(new Insets(0, 0, 10, 0));
            root.setTop(titleLabel);

            VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);

            TextField textField = new TextField();
            textField.setPromptText("training name");

            root.setCenter(vbox);

            root.setBottom(textField);

            getDialogPane().setContent(root);

            ButtonType startWithUIType = new ButtonType("With ui", ButtonBar.ButtonData.APPLY);
            ButtonType startWithoutUIType = new ButtonType("Without ui", ButtonBar.ButtonData.APPLY);
            ButtonType closeBtnType = new ButtonType("If you see this, the ui is broken.",
                    ButtonBar.ButtonData.CANCEL_CLOSE);

            getDialogPane().getButtonTypes().addAll(startWithUIType, startWithoutUIType, closeBtnType);

            Node closeButton = getDialogPane().lookupButton(closeBtnType);
            closeButton.managedProperty().bind(closeButton.visibleProperty());
            closeButton.setVisible(false);

            setResultConverter((btnType) -> {
                if (btnType.equals(startWithUIType)) return new Payload(1, textField.getText());
                if (btnType.equals(startWithoutUIType)) return new Payload(0, textField.getText());
                return new Payload(-1, textField.getText());
            });
        }
    }
}
