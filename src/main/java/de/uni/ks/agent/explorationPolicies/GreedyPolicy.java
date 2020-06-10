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
package de.uni.ks.agent.explorationPolicies;

import de.uni.ks.agent.Action;
import de.uni.ks.agent.QTable;
import de.uni.ks.maze.NodeFactory.Node;

import java.util.*;

public class GreedyPolicy implements ExplorationPolicy {

    private final Random random;
    private final int seed;

    /**
     * The greedy-policy always selects the action that has the highest Q value.
     * If several actions have the same Q-value, the policy will choose one of them randomly.
     *
     * @param seed Determines which action will be selected if the policy has to choose between actions with the same Q-Value.
     */
    public GreedyPolicy(int seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    /**
     * Always chooses the action with the highest Q-Value.
     * If there are multiple actions that share the highest Q-Value one of them will be randomly (regarding to used seed) selected.
     *
     * @param currentNode The node whose neighborhood encodes the state.
     * @param qTable      QTable that stores the knowledge of the agent
     * @return Action that determines the next movement of the agent.
     */
    @Override
    public Action chooseAction(Node currentNode, QTable qTable) {

        // init needed data
        HashMap<Action, Double> actions = qTable.getActions(currentNode);

        // determine highest Q-Value of passed state
        Double highestValue = qTable.getHighestQValueOfState(currentNode);

        // get best actions
        ArrayList<Action> greedyActions = new ArrayList<>();
        for (Map.Entry<Action, Double> entry : actions.entrySet()) {
            if (entry.getValue().compareTo(highestValue) == 0) {
                greedyActions.add(entry.getKey());
            }
        }

        // needs to be done because iterating order of the dictionary is random
        Collections.sort(greedyActions);

        // choose random action from best actions regarding to seed
        int actionNumber = this.random.nextInt(greedyActions.size());
        return greedyActions.get(actionNumber);
    }

    public int getSeed() {
        return this.seed;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "seed = " + this.seed + "}";
    }

    @Override
    public String myConfigString() {
        return getClass().getSimpleName() + "(" + "seed = " + this.seed + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GreedyPolicy that = (GreedyPolicy) o;
        return getSeed() == that.getSeed();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSeed());
    }
}
