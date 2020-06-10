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
package de.uni.ks.gui.simulator.view;

import de.uni.ks.logging.Logger;
import de.uni.ks.logging.data.LevelData;
import javafx.application.Platform;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyCode;

/**
 * This class creates and coordinates a JavaFX spinner that can select one of the already existing {@link LevelData} objects
 */
public class LevelSpinnerView {
    private final Spinner<LevelData> levelSelectionSpinner;

    public LevelSpinnerView() {
        levelSelectionSpinner = createLevelDataSelectionSpinner();
    }

    // create spinner for level selection with various listeners
    private Spinner<LevelData> createLevelDataSelectionSpinner() {

        // create editable spinner (levelNr can also be entered through the text field of the spinner)
        Spinner<LevelData> levelSelectionSpinner = new Spinner<>();
        levelSelectionSpinner.setEditable(true);

        // change level data if value of spinner changed
        SpinnerValueFactory<LevelData> valueFactory = //
                new SpinnerValueFactory<>() {

                    @Override
                    public void decrement(int steps) {
                        LevelData current = this.getValue();
                        int idx = Logger.trainingData.getLevels().indexOf(current);
                        if (idx == -1) return;
                        int newIdx = idx - steps;
                        if (newIdx < 0) {
                            newIdx = 0;
                        }
                        LevelData newLevel = Logger.trainingData.getLevels().get(newIdx);
                        this.setValue(newLevel);
                    }

                    @Override
                    public void increment(int steps) {
                        LevelData current = this.getValue();
                        int idx = Logger.trainingData.getLevels().indexOf(current);
                        if (idx == -1) return;
                        int length = Logger.trainingData.getLevels().size();
                        int newIdx = idx + steps;
                        if (newIdx > length - 1) {
                            newIdx = length - 1;
                        }
                        LevelData newLevel = Logger.trainingData.getLevels().get(newIdx);
                        this.setValue(newLevel);
                    }

                };
        levelSelectionSpinner.setValueFactory(valueFactory);

        // update value if user entered level number correctly into textfield and pressed enter
        // setOnKeyPressed does not seem to work on Linux (?) -> use setOnKeyReleased
        levelSelectionSpinner.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String levelString = levelSelectionSpinner.getEditor().getText();

                // select entered level
                if (levelNumberStringIsValid(levelString)) {
                    int levelNumberInteger = Integer.parseInt(levelString);
                    levelSelectionSpinner.getValueFactory().setValue(Logger.trainingData.getLevelData(levelNumberInteger));
                }
                // select first or last level if value of level string was too high or too low
                else {
                    if (stringIsValidInt(levelString)) {
                        int levelNumberInteger = Integer.parseInt(levelString);
                        if (levelNumberInteger < 0) {
                            levelSelectionSpinner.getValueFactory().setValue(Logger.trainingData.getLevelData(1));
                            levelSelectionSpinner.getEditor().setText("1");
                        } else if (levelNumberInteger >= Logger.trainingData.getLevels().size()) {
                            levelSelectionSpinner.getValueFactory().setValue(Logger.CurrentData.currentLevelData);
                            levelSelectionSpinner.getEditor().setText(String.valueOf(Logger.CurrentData.currentLevelData.getLevelNr()));
                        }
                    }
                }
            }
        });

        levelSelectionSpinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            // select all characters if textfield is focused
            if (isNowFocused) {
                Platform.runLater(() -> levelSelectionSpinner.getEditor().selectAll());
            }
            // if focus was lost and input was invalid -> replace input with the level number of the currently viewed level
            if (wasFocused) {
                Platform.runLater(() -> {
                    if (!levelNumberStringIsValid(levelSelectionSpinner.getEditor().getText())) {
                        levelSelectionSpinner.getEditor().setText(String.valueOf(levelSelectionSpinner.getValue().getLevelNr()));
                    }
                });
            }
        });

        return levelSelectionSpinner;
    }

    // check if string is valid int and valid level number
    private boolean levelNumberStringIsValid(String levelString) {
        if (stringIsValidInt(levelString)) {
            int levelNumberInteger = Integer.parseInt((levelString));
            return levelNumberInteger > 0 && levelNumberInteger <= Logger.trainingData.getLevels().size();
        }
        return false;
    }

    // check if string is valid int
    private boolean stringIsValidInt(String levelString) {
        try {
            Integer.parseInt((levelString));
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public LevelData getValue() {
        return levelSelectionSpinner.getValue();
    }

    public void setValue(LevelData levelData) {
        levelSelectionSpinner.getValueFactory().setValue(levelData);
    }

    public Spinner<LevelData> getLevelSelectionSpinner() {
        return levelSelectionSpinner;
    }

}
