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

public class DecreasingEpsilonPolicy implements ExplorationPolicy {

    private double epsilon_0;
    private double reducingFactor;
    private final int seed;
    private EpsilonGreedyPolicy epsilonGreedyPolicy;

    /**
     * The decreasing-epsilon-policy basically portrays a epsilon-greedy-policy with a falling epsilon.
     * The first choice of the policy is a epsilon-greedy-action-selection with epsilon = epsilon_0.
     * The epsilon gets smaller after each action selection since it gets multiplied by the reducing factor.
     *
     * @param epsilon_0      The epsilon value the policy starts with
     * @param reducingFactor The factor which reduces the epsilon after every choice
     * @param seed           Determines which random action selection numbers will be generated.
     */
    public DecreasingEpsilonPolicy(double epsilon_0, double reducingFactor, int seed) {
        this.seed = seed;

        if (0 <= epsilon_0 && epsilon_0 <= 1) {
            this.epsilon_0 = epsilon_0;
            this.epsilonGreedyPolicy = new EpsilonGreedyPolicy(epsilon_0, seed);
        } else {
            throw new IllegalArgumentException("Parameter [epsilon_0] = " + epsilon_0 + " is not in [0, 1].");
        }

        if (0 < reducingFactor && reducingFactor < 1) {
            this.reducingFactor = reducingFactor;
        } else {
            throw new IllegalArgumentException("Parameter [reducingFactor] = " + reducingFactor + " is not in (0, 1).");
        }
    }


    public double getEpsilon() {
        return this.epsilonGreedyPolicy.getEpsilon();
    }

    /**
     * Performs action selection regarding to the decreasing-epsilon-policy.
     *
     * @param currentNode The node whose neighborhood encodes the state.
     * @param qTable      QTable that stores the knowledge of the agent.
     * @return Action that determines the next movement of the agent.
     */
    @Override
    public Action chooseAction(Node currentNode, QTable qTable) {

        // choose action
        Action chosenAction = this.epsilonGreedyPolicy.chooseAction(currentNode, qTable);

        // reduce epsilon
        double oldEpsilon = this.epsilonGreedyPolicy.getEpsilon();
        double newEpsilon = oldEpsilon * this.reducingFactor;
        this.epsilonGreedyPolicy.setEpsilon(newEpsilon);

        Logger.addTextToGuiLog("Decreased epsilon to " + newEpsilon, GuiMessageType.Policy);
        return chosenAction;
    }

    @Override
    public String myConfigString() {
        return getClass().getSimpleName() + "("
                + "epsilon_0 = " + epsilon_0 + ", "
                + "reducingFactor = " + reducingFactor + ", "
                + "seed = " + seed
                + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecreasingEpsilonPolicy that = (DecreasingEpsilonPolicy) o;
        return Double.compare(that.epsilon_0, epsilon_0) == 0 &&
                Double.compare(that.reducingFactor, reducingFactor) == 0 &&
                seed == that.seed &&
                epsilonGreedyPolicy.equals(that.epsilonGreedyPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(epsilon_0, reducingFactor, seed, epsilonGreedyPolicy);
    }
}
