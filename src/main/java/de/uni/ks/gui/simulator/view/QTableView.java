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

import de.uni.ks.agent.Action;
import de.uni.ks.agent.QTable;
import de.uni.ks.maze.NodeFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is used to display the data of the QTable {@link de.uni.ks.agent.QTable}.
 * For each entry of the QTable a row is created in a {@link TableView}.
 * Each row has an image that visualizes the state and the corresponding Q-values.
 * In order to display the Q-values in a {@link TableView} object, the data has to be processed and managed in a {@link QTableDataController}.
 */
public class QTableView {

    private final TableView<QTableDataController.Entry> qTableView;
    private final QTableDataController qTableDataController;

    public QTableView() {
        qTableView = createQTableView();
        qTableDataController = new QTableDataController();
        qTableView.setItems(qTableDataController.getEntries());
    }

    private TableView<QTableDataController.Entry> createQTableView() {
        TableView<QTableDataController.Entry> qTableView = new TableView<>();
        qTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<QTableDataController.Entry, Image> stateCol = new TableColumn<>("State");
        TableColumn<QTableDataController.Entry, Double> upCol = new TableColumn<>("Up");
        TableColumn<QTableDataController.Entry, Double> rightCol = new TableColumn<>("Right");
        TableColumn<QTableDataController.Entry, Double> downCol = new TableColumn<>("Down");
        TableColumn<QTableDataController.Entry, Double> leftCol = new TableColumn<>("Left");

        // uses image of state in state column
        stateCol.setCellValueFactory(new PropertyValueFactory<>("stateImage"));
        stateCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<QTableDataController.Entry, Image> call(TableColumn<QTableDataController.Entry, Image> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Image item, boolean empty) {
                        super.updateItem(item, empty);

                        HBox box = new HBox();
                        box.setSpacing(10);

                        ImageView imageview = new ImageView();
                        imageview.setImage(item);

                        box.getChildren().addAll(imageview);
                        box.setAlignment(Pos.CENTER);
                        setGraphic(box);
                    }
                };
            }
        });

        upCol.setCellValueFactory(
                new PropertyValueFactory<>("upValue")
        );
        rightCol.setCellValueFactory(
                new PropertyValueFactory<>("rightValue")
        );

        downCol.setCellValueFactory(
                new PropertyValueFactory<>("downValue")
        );
        leftCol.setCellValueFactory(
                new PropertyValueFactory<>("leftValue")
        );

        qTableView.getColumns().add(stateCol);
        qTableView.getColumns().add(upCol);
        qTableView.getColumns().add(rightCol);
        qTableView.getColumns().add(downCol);
        qTableView.getColumns().add(leftCol);
        return qTableView;
    }

    private int getRowIndexOfState(String state) {
        ObservableList<QTableDataController.Entry> items = qTableView.getItems();
        for (int i = 0; i < qTableView.getItems().size(); i++) {
            if (items.get(i).getState().equals(state)) {
                return i;
            }
        }
        System.err.println("state " + state + " could not be found in QTableTableView");
        return -1;
    }

    /**
     * Searches the passed state in the table view and selects the corresponding row.
     *
     * @param state The state that will be searched.
     */
    public void selectEntry(String state) {
        int rowIndexOfCurrentState = getRowIndexOfState(state);
        TableView.TableViewSelectionModel<QTableDataController.Entry> selectionModel = qTableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(rowIndexOfCurrentState);
    }

    /**
     * Updates the internally stored QTable data and refreshes the UI.
     *
     * @param qTable The data with which the internally stored data will be synchronized.
     */
    public void refresh(QTable qTable) {
        if (!qTableDataController.updateEntries(qTable))
            System.err.println("Could not update data of qTableDataController correctly");
        qTableView.refresh();
    }

    public TableView<QTableDataController.Entry> getQTableView() {
        return qTableView;
    }

    /**
     * Class that is necessary to display the data in the table view reasonably and efficiently.
     */
    static class QTableDataController {

        private final ObservableList<Entry> entries;

        QTableDataController() {
            entries = FXCollections.observableArrayList();
        }

        private boolean addEntry(QTable qTable, String state) {
            HashMap<String, HashMap<Action, Double>> qTableHashMap = qTable.getQTable();
            Double upQValue = null;
            Double rightQValue = null;
            Double downQValue = null;
            Double leftQValue = null;

            if (qTableHashMap.containsKey(state)) {
                HashMap<Action, Double> actionMap = qTableHashMap.get(state);
                if (actionMap.containsKey(Action.UP)) {
                    upQValue = actionMap.get(Action.UP);
                }
                if (actionMap.containsKey(Action.RIGHT)) {
                    rightQValue = actionMap.get(Action.RIGHT);
                }
                if (actionMap.containsKey(Action.DOWN)) {
                    downQValue = actionMap.get(Action.DOWN);
                }
                if (actionMap.containsKey(Action.LEFT)) {
                    leftQValue = actionMap.get(Action.LEFT);
                }

                QTableDataController.Entry newEntry = new Entry(state, upQValue, rightQValue,
                        downQValue, leftQValue);
                this.entries.add(newEntry);
                return true;
            }
            return false;
        }

        private boolean updateEntry(QTable qTable, String state) {
            HashMap<String, HashMap<Action, Double>> qTableHashMap = qTable.getQTable();
            QTableDataController.Entry entry = getEntry(state);

            if (entry == null) {
                System.err.println("entry of state " + state + " does not exist in qTableDataController");
                return false;
            }

            if (!qTableHashMap.containsKey(state)) {
                System.err.println("State " + state + " does not exist in qTable");
                return false;
            }

            HashMap<Action, Double> actionMap = qTableHashMap.get(state);
            if (actionMap.containsKey(Action.UP)) {
                entry.setUpValue(actionMap.get(Action.UP));
            }
            if (actionMap.containsKey(Action.RIGHT)) {
                entry.setRightValue(actionMap.get(Action.RIGHT));
            }
            if (actionMap.containsKey(Action.DOWN)) {
                entry.setDownValue(actionMap.get(Action.DOWN));
            }
            if (actionMap.containsKey(Action.LEFT)) {
                entry.setLeftValue(actionMap.get(Action.LEFT));
            }
            return true;
        }

        private QTableDataController.Entry getEntry(String state) {
            for (QTableDataController.Entry entry : this.entries) {
                if (entry.getState().equals(state)) {
                    return entry;
                }
            }
            return null;
        }

        boolean entryExists(String state) {
            for (QTableDataController.Entry entry : this.entries) {
                if (entry.getState().equals(state))
                    return true;
            }
            return false;
        }

        ObservableList<QTableDataController.Entry> getEntries() {
            return entries;
        }

        /**
         * The method synchronizes the data of the passed QTable with the data stored internally ({@link #entries}).
         *
         * @param qTable The data with which the internally stored data will be synchronized.
         */
        boolean updateEntries(QTable qTable) {
            HashMap<String, HashMap<Action, Double>> qTableHashMap = qTable.getQTable();
            // iterate through passed QTable
            for (Map.Entry<String, HashMap<Action, Double>> stateActions : qTableHashMap.entrySet()) {
                String state = stateActions.getKey();
                // update existing entry if entry is already saved in QTable TableView
                if (entryExists(state)) {
                    if (!updateEntry(qTable, state)) {
                        return false;
                    }
                }
                // add new entry if entry is not existing in QTable TableView
                else {
                    if (!addEntry(qTable, state)) {
                        return false;
                    }
                }
            }

            // remove deleted entries if necessary
            if (entries.size() > qTableHashMap.size()) {
                Iterator iterator = entries.iterator();
                while (iterator.hasNext()) {
                    QTableDataController.Entry entry = (QTableDataController.Entry) iterator.next();
                    if (!qTableHashMap.containsKey(entry.getState())) {
                        iterator.remove();
                    }
                }
            }

            return true;
        }

        /**
         * This Class is needed to display the data originally stored in a HashMap in ({@link QTable} in a
         * {@link TableView}.
         */
        public static class Entry {
            private String state;
            private Image stateImage;
            private Double upValue;
            private Double rightValue;
            private Double downValue;
            private Double leftValue;

            Entry(String state, Double upValue, Double rightValue, Double downValue, Double leftValue) {
                this.state = state;
                this.upValue = upValue;
                this.rightValue = rightValue;
                this.downValue = downValue;
                this.leftValue = leftValue;
                this.stateImage = NodeFactory.createImageOfState(state);
            }

            String getState() {
                return state;
            }

            @SuppressWarnings("unused")
            public void setState(String state) {
                this.state = state;
            }

            public Image getStateImage() {
                return stateImage;
            }

            public void setStateImage(Image stateImage) {
                this.stateImage = stateImage;
            }

            @SuppressWarnings("unused")
            public Double getUpValue() {
                return upValue;
            }

            void setUpValue(double upValue) {
                this.upValue = upValue;
            }

            @SuppressWarnings("unused")
            public Double getRightValue() {
                return rightValue;
            }

            void setRightValue(double rightValue) {
                this.rightValue = rightValue;
            }

            @SuppressWarnings("unused")
            public Double getDownValue() {
                return downValue;
            }

            void setDownValue(double downValue) {
                this.downValue = downValue;
            }

            @SuppressWarnings("unused")
            public Double getLeftValue() {
                return leftValue;
            }

            void setLeftValue(double leftValue) {
                this.leftValue = leftValue;
            }

        }
    }
}
