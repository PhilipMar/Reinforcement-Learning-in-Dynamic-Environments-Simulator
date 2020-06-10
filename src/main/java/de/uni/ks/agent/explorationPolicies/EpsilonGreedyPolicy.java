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
import de.uni.ks.logging.Logger;
import de.uni.ks.logging.messages.GuiMessageType;
import de.uni.ks.maze.NodeFactory.Node;

import java.util.Objects;
import java.util.Random;

public class EpsilonGreedyPolicy implements ExplorationPolicy {

    private double epsilon;
    private final int seed;
    private final Random random;

    private RandomPolicy randomPolicy;
    private GreedyPolicy greedyPolicy;

    /**
     * This policy chooses actions based on the value of epsilon. The higher epsilon, the more it behaves like
     * the {@link RandomPolicy} and the lower epsilon the more it behaves like the {@link GreedyPolicy}. Epsilon acts
     * as a threshold in this strategy, for e.g. epsilon = 0.7, 70% of all actions are chosen by
     * the {@link RandomPolicy}.
     *
     * @param epsilon The threshold value.
     * @param seed    Determines which actions are selected when choosing randomly.
     */
    public EpsilonGreedyPolicy(double epsilon, int seed) {

        if (0 > epsilon || epsilon > 1) {
            throw new IllegalArgumentException("The value for parameter epsilon must be in [0,1].");
        }

        this.seed = seed;
        this.random = new Random(this.seed);

        randomPolicy = new RandomPolicy(this.seed);
        greedyPolicy = new GreedyPolicy(this.seed);

        setEpsilon(epsilon);
    }

    /**
     * @param currentNode The node whose neighborhood encodes the state.
     * @param qTable      QTable that stores the knowledge of the agent.
     * @return Action that determines the next movement of the agent.
     */
    @Override
    public Action chooseAction(Node currentNode, QTable qTable) {

        double rand = random.nextDouble(); // Value between 0.0 and 1.0.

        if (rand <= epsilon) {
            Logger.addTextToGuiLog("Agent will perform random action", GuiMessageType.Policy);
            Logger.addTextToMiscLogOfCurrentEpisode("Agent will perform random action");
            return randomPolicy.chooseAction(currentNode, qTable);
        } else {
            Logger.addTextToGuiLog("Agent will perform greedy action", GuiMessageType.Policy);
            Logger.addTextToMiscLogOfCurrentEpisode("Agent will perform greedy action");
            return greedyPolicy.chooseAction(currentNode, qTable);
        }
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        if (0 <= epsilon && epsilon <= 1) {
            this.epsilon = epsilon;
        } else {
            throw new IllegalArgumentException("Parameter [epsilon] = " + epsilon + " is not in [0, 1].");
        }
    }

    @Override
    public String toString() {
        return "EpsilonGreedyPolicy{" +
                "epsilon=" + epsilon +
                ", seed=" + seed +
                ", randomPolicy=" + randomPolicy +
                ", greedyPolicy=" + greedyPolicy +
                '}';
    }

    @Override
    public String myConfigString() {
        return getClass().getSimpleName() + "("
                + "epsilon = " + epsilon + ", "
                + "seed = " + seed
                + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EpsilonGreedyPolicy that = (EpsilonGreedyPolicy) o;
        return Double.compare(that.getEpsilon(), getEpsilon()) == 0 &&
                seed == that.seed &&
                randomPolicy.equals(that.randomPolicy) &&
                greedyPolicy.equals(that.greedyPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEpsilon(), seed, randomPolicy, greedyPolicy);
    }
}
