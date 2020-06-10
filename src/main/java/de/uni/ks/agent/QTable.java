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
package de.uni.ks.agent;

import de.uni.ks.maze.NodeFactory.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class represents a classic Q-Table.
 * Therefore it is used to manage the Q-values of all state action pairs the agent knows.
 * </p>
 * <p>
 * The Q-values are stored as double values in a nested {@link HashMap}.
 * These nested HashMaps have actions as keys and the Q-Values as values.
 * The nested HashMaps themselves are the values of a parent HashMap (qTable).
 * The parent HashMap (qTable) is using states as keys, over which the nested HashMaps can be accessed.
 * </p>
 */
public class QTable {

    final HashMap<String, HashMap<Action, Double>> qTable;
    private final double qTableInitValue;

    /**
     * Initializes the Q-Table by initializing the nested HashMap, that is used, to store the Q-Values.
     *
     * @param qTableInitValue Each new state-action pair is initialized with this value.
     */
    public QTable(double qTableInitValue) {
        // initialize nested HashMap to manage Q-Table
        this.qTable = new HashMap<>();
        this.qTableInitValue = qTableInitValue;
    }

    /**
     * Copy constructor, which creates a new Q-Table with identical content as the passed Q-Table.
     *
     * @param qTableToCopy Q-Table whose content will be copied.
     */
    public QTable(QTable qTableToCopy) {

        // create new HashMap
        this.qTable = new HashMap<>();
        this.qTableInitValue = qTableToCopy.qTableInitValue;

        // iterate through passed QTable
        for (Map.Entry<String, HashMap<Action, Double>> stateActions : qTableToCopy.getQTable().entrySet()) {

            // create HashMap for action value pairs
            HashMap<Action, Double> actionValuePair = new HashMap<>();
            for (Map.Entry<Action, Double> actionValue : stateActions.getValue().entrySet()) {
                actionValuePair.put(actionValue.getKey(), actionValue.getValue());
            }

            // add entry (state and action value pair) to new HashMap
            this.qTable.put(stateActions.getKey(), actionValuePair);
        }

    }

    /**
     * Add entry to QTable.
     *
     * @param node    The node whose neighborhood will encode the state.
     * @param actions The actions that will be added to the state of the passed node.
     * @return True if adding the entry was successful. False if an error occurred.
     */
    public boolean addEntry(Node node, ArrayList<Action> actions) {
        if (!this.stateExists(node)) {
            HashMap<Action, Double> actionValuePairs = new HashMap<>();
            for (Action action : actions) {
                actionValuePairs.put(action, this.qTableInitValue);
            }
            this.qTable.put(node.getState(), actionValuePairs);
            return true;
        } else {
            System.err.println("state <" + node.getState() + "> already exist. No Entry was added ");
            return false;
        }
    }

    /**
     * Add entry to QTable.
     *
     * @param node    The node whose neighborhood will encode the state.
     * @param actions The actions that will be added to the state of the passed node.
     * @return True if adding the entry was successful. False if an error occurred.
     */
    public boolean addEntry(Node node, HashMap<Action, Double> actions) {
        if (!this.stateExists(node)) {
            this.qTable.put(node.getState(), actions);
            return true;
        } else {
            System.err.println("state <" + node.getState() + "> already exists in Q-Table. No Entry was added ");
            return false;
        }
    }

    /**
     * Set Q-Value of state action pair.
     *
     * @param node      The node whose neighborhood encodes the state.
     * @param action    The action whose Q-Value has to be changed.
     * @param newQValue The new Q-Value.
     */
    public void setQValue(Node node, Action action, Double newQValue) {

        if (!this.stateExists(node)) {
            throw new RuntimeException("Can't set Q-Value of state action pair (" + node.getState() + ", " + action + ") since the state can't be found in the Q-Table");
        }

        if (!this.actionExists(node, action)) {
            throw new RuntimeException("Can't set Q-Value of state action pair (" + node.getState() + ", " + action + ") since the state action pair can't be found in the Q-Table");
        }

        // set new QValue
        this.qTable.get(node.getState()).replace(action, newQValue);
    }

    /**
     * Returns Q-Value of state action pair in QTable.
     *
     * @param node   The node whose neighborhood encodes the state.
     * @param action The action whose Q-Value is requested.
     * @return requested Q-Value if state and action exists.
     */
    public double getQValue(Node node, Action action) {

        if (!this.stateExists(node)) {
            throw new RuntimeException("Can't request Q-Value of state action pair (" + node.getState() + ", " + action + ") since the state can't be found in the Q-Table");
        }

        if (!this.actionExists(node, action)) {
            throw new RuntimeException("Can't request Q-Value of state action pair (" + node.getState() + ", " + action + ") since the state doesn't know about the action");
        }

        return this.qTable.get(node.getState()).get(action);
    }

    /**
     * Returns HashMap with Q-Values of all actions available. Creates empty HashMap, if state does not exist.
     *
     * @param node The node whose neighborhood encodes the state.
     * @return HashMap with all saved actions and their Q-Values.
     */
    public HashMap<Action, Double> getActions(Node node) {

        // if state does not exist -> create new state in QTable and create HashMap for actions
        if (!this.stateExists(node)) {
            HashMap<Action, Double> actions = new HashMap<>();
            this.qTable.put(node.getState(), actions);
            System.err.println("creation of state<" + node.getState() + "> has been forced. Empty HashMap with actions was added");
        }

        return this.qTable.get(node.getState());
    }

    /**
     * Determine and return highest Q-Value of passed state.
     *
     * @param node The node whose neighborhood encodes the state.
     * @return Highest Q-Value of passed state if state exists.
     */
    public Double getHighestQValueOfState(Node node) {
        // init needed data
        HashMap<Action, Double> actions = this.getActions(node);

        Double highestValue = Double.NEGATIVE_INFINITY;

        // determine highest Q-Value
        for (Map.Entry<Action, Double> entry : actions.entrySet()) {
            if (entry.getValue() > highestValue) {
                highestValue = entry.getValue();
            }
        }

        if (highestValue == Double.NEGATIVE_INFINITY) {
            throw new RuntimeException("can't get highest Q-Value of state <\" + node.getState() + \"> since it does not contain any actions");

        }

        return highestValue;
    }

    /**
     * Check if action exists in Q-Table.
     *
     * @param node   The node whose neighborhood encodes the state.
     * @param action Action whose existence will be checked.
     * @return True if action exists. Returns false If state or action doesn't exist.
     */
    public boolean actionExists(Node node, Action action) {
        if (!this.stateExists(node)) {
            return false;
        }
        return this.qTable.get(node.getState()).containsKey(action);
    }

    /**
     * Check if state exists in Q-Table.
     *
     * @param node The node whose neighborhood encodes the state.
     * @return True if state exists. Returns false otherwise.
     */
    public boolean stateExists(Node node) {
        return this.qTable.containsKey(node.getState());
    }

    /**
     * Returns the numbers of rows of the QTable.
     *
     * @return The numbers of rows of the QTable.
     */
    public int size() {
        return this.qTable.size();
    }

    /**
     * Removes all data of the qTable.
     */
    public void clear() {
        this.qTable.clear();
    }


    /**
     * Returns a string that represents the Q-Table in the .csv format.
     *
     * @return String that represents the Q-Table in the .csv format.
     */
    public String getCsvString() {

        int numberOfLines = qTable.entrySet().size() + 1;
        StringBuilder string = new StringBuilder("State;Up;Right;Down;Left\n");

        int currentLineNumber = 1;
        for (Map.Entry<String, HashMap<Action, Double>> stateActions : qTable.entrySet()) {

            string.append(stateActions.getKey()).append(";");

            String up = "NaN";
            String right = "NaN";
            String down = "NaN";
            String left = "NaN";

            for (Map.Entry<Action, Double> actionValue : stateActions.getValue().entrySet()) {
                switch (actionValue.getKey()) {
                    case UP:
                        up = actionValue.getValue().toString();
                        break;
                    case RIGHT:
                        right = actionValue.getValue().toString();
                        break;
                    case DOWN:
                        down = actionValue.getValue().toString();
                        break;
                    case LEFT:
                        left = actionValue.getValue().toString();
                        break;
                }
            }
            string.append(up).append(";").append(right).append(";").append(down).append(";").append(left);
            if (currentLineNumber < numberOfLines - 1) string.append("\n");
            currentLineNumber++;
        }
        return string.toString();
    }

    public HashMap<String, HashMap<Action, Double>> getQTable() {
        return qTable;
    }
}
