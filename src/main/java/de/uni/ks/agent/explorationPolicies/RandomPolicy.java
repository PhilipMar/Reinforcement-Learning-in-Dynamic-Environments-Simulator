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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class RandomPolicy implements ExplorationPolicy {

    private final int seed;
    private final Random random;

    /**
     * This policy chooses actions randomly from all the available actions for the state.
     *
     * @param seed Determines which actions are selected when choosing randomly.
     */
    public RandomPolicy(int seed) {
        this.seed = seed;
        this.random = new Random(this.seed);
    }

    /**
     * @param currentNode The node whose neighborhood encodes the state.
     * @param qTable      QTable that stores the knowledge of the agent.
     * @return Action that determines the next movement of the agent.
     */
    @Override
    public Action chooseAction(Node currentNode, QTable qTable) {

        HashMap<Action, Double> map = qTable.getActions(currentNode);
        ArrayList<Action> actions =
                (ArrayList<Action>) getSortedListFromActionSet(map.keySet());

        int rand = random.nextInt(actions.size());

        return actions.get(rand);
    }

    @Override
    public String myConfigString() {
        return this.getClass().getSimpleName() + "("
                + "seed = " + seed
                + ")";
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{"
                + "seed = " + seed
                + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomPolicy that = (RandomPolicy) o;
        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seed);
    }
}