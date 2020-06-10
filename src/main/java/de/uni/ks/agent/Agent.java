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

import de.uni.ks.agent.explorationPolicies.ExplorationPolicy;
import de.uni.ks.logging.Logger;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory.Node;

import java.util.ArrayList;

/**
 * This class represents the Reinforcement Learning agent that interacts with the {@link Maze}.
 */
public class Agent {

    private Node currentPosition;
    private QTable qTable;
    private final Double qTableInitValue;
    private final Double qLearningAlpha;
    private final Double qLearningGamma;
    private final ExplorationPolicy policy;
    private int numberOfActionsTaken;
    private Double totalReward;

    public Agent(Node currentPosition, ExplorationPolicy explorationPolicy, Double qLearningAlpha,
                 Double qLearningGamma, Double qTableInitValue) {
        this.currentPosition = currentPosition;
        this.numberOfActionsTaken = 0;
        this.totalReward = 0d;
        this.qTable = new QTable(qTableInitValue);
        this.qLearningAlpha = qLearningAlpha;
        this.qLearningGamma = qLearningGamma;
        this.qTableInitValue = qTableInitValue;
        this.policy = explorationPolicy;
    }

    /**
     * Calculates and returns an ArrayList that contains all accessible fields.
     *
     * @param node Node from where the accessible fields are calculated.
     * @return ArrayList with all accessible fields.
     */
    public ArrayList<Action> createActions(Node node) {

        ArrayList<Action> actions = new ArrayList<>();

        Node upperNeighbor = node.getUpperNeighbor();
        Node rightNeighbor = node.getRightNeighbor();
        Node lowerNeighbor = node.getLowerNeighbor();
        Node leftNeighbor = node.getLeftNeighbor();

        if (upperNeighbor != null) {
            if (upperNeighbor.isPassable()) {
                actions.add(Action.UP);
            }
        }
        if (rightNeighbor != null) {
            if (rightNeighbor.isPassable()) {
                actions.add(Action.RIGHT);
            }
        }
        if (lowerNeighbor != null) {
            if (lowerNeighbor.isPassable()) {
                actions.add(Action.DOWN);
            }
        }
        if (leftNeighbor != null) {
            if (leftNeighbor.isPassable()) {
                actions.add(Action.LEFT);
            }
        }
        return actions;
    }

    /**
     * Does agent environment interaction (do action, get reward, set new state and node) + updates knowledge and statistics.
     */
    public void doAction() {

        // check if entry for current state already exists in QTable.
        // if no entry exists: calculate possible actions and create new entry
        if (!this.qTable.stateExists(this.currentPosition)) {
            ArrayList<Action> actions = this.createActions(this.currentPosition);
            this.qTable.addEntry(this.currentPosition, actions);
        }

        // save old Node
        Node oldNode = this.currentPosition;

        // choose action according to policy
        Action action = this.policy.chooseAction(this.currentPosition, this.qTable);

        // do action
        moveAgent(action);

        // get reward for last action
        double reward = this.currentPosition.getReward();

        // update training statistics
        this.numberOfActionsTaken++;
        this.totalReward += reward;

        // write logger message
        Logger.CurrentData.currentActionNumber = this.numberOfActionsTaken;
        Logger.addTextToMiscLogOfCurrentEpisode("Agent moved " + action);

        // save old Q-Value
        double oldQValue = this.qTable.getQValue(oldNode, action);

        // calculate highest q value of current state. if state does not exist -> create entry
        double highestQValueCurrentState = this.qTableInitValue;
        if (this.qTable.stateExists(this.currentPosition)) {
            highestQValueCurrentState = this.qTable.getHighestQValueOfState(this.currentPosition);
        } else {
            ArrayList<Action> actions = this.createActions(this.currentPosition);
            this.qTable.addEntry(this.currentPosition, actions);
        }

        // update QTable
        double newQValue = oldQValue + this.qLearningAlpha * (reward + this.qLearningGamma * highestQValueCurrentState - oldQValue);
        this.qTable.setQValue(oldNode, action, newQValue);

        // do post processing
        this.policy.postProcessing(oldNode, action, oldQValue, this.currentPosition, this.qTable);
    }

    /**
     * Performs action by moving agent one field up, right, down or left.
     *
     * @param action Action that will be performed.
     */
    void moveAgent(Action action) {
        switch (action) {
            case UP: {
                this.currentPosition = this.currentPosition.getUpperNeighbor();
                break;
            }
            case RIGHT: {
                this.currentPosition = this.currentPosition.getRightNeighbor();
                break;
            }
            case DOWN: {
                this.currentPosition = this.currentPosition.getLowerNeighbor();
                break;
            }
            case LEFT: {
                this.currentPosition = this.currentPosition.getLeftNeighbor();
                break;
            }
        }
    }

    /**
     * Resets episode related statistics as well as the position of the agent for the new episode.
     *
     * @param maze The maze on whose start node the agent will be moved after the reset.
     */
    public void resetAgentForEpisode(Maze maze) {
        this.currentPosition = maze.getStartNode();
        this.numberOfActionsTaken = 0;
        this.totalReward = 0.0d;
    }

    /**
     * Resets Q-Table by deleting all entries.
     */
    public void resetQTable() {
        this.qTable.clear();
    }

    public QTable getQTable() {
        return this.qTable;
    }

    @SuppressWarnings("unused")
    public void setQTable(QTable Qtable) {
        this.qTable = Qtable;
    }

    public Node getCurrentPosition() {
        return currentPosition;
    }

    @SuppressWarnings("unused")
    private void setCurrentPosition(Node currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getNumberOfActionsTaken() {
        return numberOfActionsTaken;
    }

    public void setNumberOfActionsTaken(int numberOfActionsTaken) {
        this.numberOfActionsTaken = numberOfActionsTaken;
    }

    public double getTotalReward() {
        return totalReward;
    }

}
