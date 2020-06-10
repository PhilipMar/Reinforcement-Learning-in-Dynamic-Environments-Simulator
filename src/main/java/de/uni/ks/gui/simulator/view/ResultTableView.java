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

import de.uni.ks.logging.data.EpisodeData;
import de.uni.ks.logging.data.LevelData;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;

/**
 * This class is used to display the results of a certain level in a {@link TableView}.
 * For each episode of a level, the table shows how many actions the agent has performed and what reward he has achieved.
 * It also shows the criteria which led to the end of the viewed episode.
 * In order to manage the data more efficiently the {@link ResultTableDataController} class is used.
 */
public class ResultTableView {
    private final TableView<EpisodeData> resultsTableView;
    private final ResultTableDataController resultTableDataController;

    public ResultTableView() {
        resultsTableView = createResultsTableView();
        resultTableDataController = new ResultTableDataController();
    }

    private TableView<EpisodeData> createResultsTableView() {

        TableView<EpisodeData> resultsTableView = new TableView<>();
        resultsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<EpisodeData, Integer> episodeCol = new TableColumn<>("Episode");
        TableColumn<EpisodeData, Integer> numberOfActionCol = new TableColumn<>("Actions");
        TableColumn<EpisodeData, Double> totalRewardCol = new TableColumn<>("Reward");
        TableColumn<EpisodeData, String> episodeStoppingCriteriaCol = new TableColumn<>("Stop Criterion");

        episodeCol.setCellValueFactory(
                new PropertyValueFactory<>("episodeNr")
        );
        numberOfActionCol.setCellValueFactory(
                new PropertyValueFactory<>("numberOfActions")
        );
        totalRewardCol.setCellValueFactory(
                new PropertyValueFactory<>("totalReward")
        );
        episodeStoppingCriteriaCol.setCellValueFactory(param -> {
            EpisodeData episodeData = param.getValue();
            String criterionLoggerString = "";
            if (episodeData.getOccurredEpisodeStopCriterion() != null) {
                criterionLoggerString = episodeData.getOccurredEpisodeStopCriterion().getLoggerString();
            }
            return new SimpleStringProperty(criterionLoggerString);
        });

        resultsTableView.getColumns().add(episodeCol);
        resultsTableView.getColumns().add(numberOfActionCol);
        resultsTableView.getColumns().add(totalRewardCol);
        resultsTableView.getColumns().add(episodeStoppingCriteriaCol);
        return resultsTableView;
    }

    /**
     * Sets the level whose results will be displayed in the result TableView.
     *
     * @param levelData Stores the results that will be displayed.
     */
    public void setLevel(LevelData levelData) {
        resultTableDataController.updateResultsTable(levelData);
        ObservableList<EpisodeData> episodes = resultTableDataController.getEpisodesOfLevel(levelData.getLevelNr());
        resultsTableView.setItems(episodes);
        resultsTableView.refresh();
    }

    /**
     * Updates the internally stored results data and refreshes the UI.
     *
     * @param levelData The data with which the internally stored data will be synchronized.
     */
    public void refresh(LevelData levelData) {
        resultTableDataController.updateResultsTable(levelData);
        resultsTableView.refresh();
    }

    public TableView<EpisodeData> getResultsTableView() {
        return resultsTableView;
    }

    /**
     * Class that is necessary to display and manage the data in the table view reasonably and efficiently.
     */
    static class ResultTableDataController {

        // a list that contains an element for each level. The elements themselves are again lists, that store all episodeData objects of a level.
        private final ArrayList<ObservableList<EpisodeData>> levelEpisodeData;

        ResultTableDataController() {
            levelEpisodeData = new ArrayList<>();
        }

        /**
         * The method synchronizes the data of the passed LevelData Object with the data stored internally in
         * ({@link #levelEpisodeData}).
         *
         * @param levelData The data with which the internally stored data will be synchronized.
         */
        boolean updateResultsTable(LevelData levelData) {
            if (!episodesOfLevelAlreadyExist(levelData.getLevelNr())) {
                levelEpisodeData.add(FXCollections.observableArrayList());
            }

            for (EpisodeData episodeData : levelData.getEpisodes()) {
                if (episodeOfLevelAlreadyExists(levelData.getLevelNr(), episodeData.getEpisodeNr())) {
                    updateEpisodeData(episodeData, getEpisodeOfLevel(levelData.getLevelNr(), episodeData.getEpisodeNr()));
                } else {
                    if (!addEpisodeData(levelData.getLevelNr(), episodeData)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean addEpisodeData(int levelNr, EpisodeData episodeData) {
            if (episodeOfLevelAlreadyExists(levelNr, episodeData.getEpisodeNr())) return false;
            ObservableList<EpisodeData> episodesOfLevel = getEpisodesOfLevel(levelNr);
            episodesOfLevel.add(episodeData);
            return true;
        }

        private void updateEpisodeData(EpisodeData passedEpisodeData, EpisodeData savedEpisodeData) {
            if (passedEpisodeData.getNumberOfActions() != savedEpisodeData.getNumberOfActions()) {
                savedEpisodeData.setNumberOfActions(passedEpisodeData.getNumberOfActions());
            }
            if (passedEpisodeData.getTotalReward().compareTo(savedEpisodeData.getTotalReward()) != 0) {
                savedEpisodeData.setTotalReward(passedEpisodeData.getTotalReward());
            }
            if (passedEpisodeData.getOccurredEpisodeStopCriterion() != savedEpisodeData.getOccurredEpisodeStopCriterion()) {
                savedEpisodeData.setOccurredEpisodeStopCriterion(passedEpisodeData.getOccurredEpisodeStopCriterion());
            }
        }

        private boolean episodesOfLevelAlreadyExist(int levelNr) {
            return levelEpisodeData.size() >= levelNr;
        }

        private boolean episodeOfLevelAlreadyExists(int levelNr, int episodeNr) {
            return episodesOfLevelAlreadyExist(levelNr) && getEpisodesOfLevel(levelNr).size() >= episodeNr;
        }

        ObservableList<EpisodeData> getEpisodesOfLevel(int levelNr) {
            if (!episodesOfLevelAlreadyExist(levelNr)) return null;
            return levelEpisodeData.get(levelNr - 1);
        }

        EpisodeData getEpisodeOfLevel(int levelNr, int episodeNr) {
            if (!episodeOfLevelAlreadyExists(levelNr, episodeNr)) return null;
            return getEpisodesOfLevel(levelNr).get(episodeNr - 1);
        }
    }
}