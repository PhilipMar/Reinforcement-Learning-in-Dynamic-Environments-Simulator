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

public class EpsilonFirstPolicy implements ExplorationPolicy {

    private double epsilonExplore;
    private double epsilonExploit;
    private int numberOfExploringActions;
    private final int seed;

    private EpsilonGreedyPolicy firstPolicy;
    private EpsilonGreedyPolicy secondPolicy;

    private int actionsTaken = 0;

    /**
     * Training is split in two phases by this policy. The first phase is the exploration phase and lasts for a user defined number
     * of selections, the second phase is the exploitation phase and lasts indefinitely. Actions are chosen by an
     * {@link EpsilonGreedyPolicy}. The actions in the exploration phase are chosen with a different value for epsilon
     * than the actions in the exploitation phase.
     * <p>
     * According to the definition of the epsilon-greedy-policy, the value of epsilon for the exploration phase should
     * be higher than the value for the exploitation phase.
     *
     * @param epsilonExplore           Epsilon value of the exploration phase.
     * @param epsilonExploit           Epsilon value of the exploitation phase.
     * @param numberOfExploringActions The number of actions the strategy is exploring.
     * @param seed                     Determines which actions are selected when choosing randomly.
     */
    public EpsilonFirstPolicy(double epsilonExplore, double epsilonExploit, int numberOfExploringActions, int seed) {

        if (0 > epsilonExploit || epsilonExploit > 1
                || 0 > epsilonExplore || epsilonExplore > 1) {
            throw new IllegalArgumentException("Values for parameter epsilonExplore and epsilonExploit must be in [0,1].");
        }

        if (1 > numberOfExploringActions) {
            throw new IllegalArgumentException("Value for parameter exploreUntilAction must be greater than 0");
        }

        this.epsilonExplore = epsilonExplore;
        this.epsilonExploit = epsilonExploit;
        this.numberOfExploringActions = numberOfExploringActions;
        this.seed = seed;

        firstPolicy = new EpsilonGreedyPolicy(this.epsilonExplore, this.seed);
        secondPolicy = new EpsilonGreedyPolicy(this.epsilonExploit, this.seed);
    }

    /**
     * @param currentNode The node whose neighborhood encodes the state.
     * @param qTable      QTable that stores the knowledge of the agent.
     * @return Action that determines the next movement of the agent.
     */
    @Override
    public Action chooseAction(Node currentNode, QTable qTable) {

        actionsTaken++;

        if (actionsTaken > numberOfExploringActions) {
            Logger.addTextToGuiLog("Changed epsilon from " + firstPolicy.getEpsilon() + " to "
                    + secondPolicy.getEpsilon(), GuiMessageType.Policy);
        }

        return actionsTaken <= numberOfExploringActions ? firstPolicy.chooseAction(currentNode, qTable)
                : secondPolicy.chooseAction(currentNode, qTable);
    }

    @Override
    public String myConfigString() {
        return getClass().getSimpleName() + "("
                + "epsilonExplore = " + epsilonExplore + ", "
                + "epsilonExploit = " + epsilonExploit + ", "
                + "numberOfExploringActions = " + numberOfExploringActions + ", "
                + "seed = " + seed
                + ")";
    }

    @Override
    public String toString() {
        return "EpsilonFirstPolicy{" +
                "epsilonExplore=" + epsilonExplore +
                ", epsilonExploit=" + epsilonExploit +
                ", numberOfExploringActions=" + numberOfExploringActions +
                ", seed=" + seed +
                ", firstPolicy=" + firstPolicy +
                ", secondPolicy=" + secondPolicy +
                ", actionsTaken=" + actionsTaken +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EpsilonFirstPolicy that = (EpsilonFirstPolicy) o;
        return Double.compare(that.epsilonExplore, epsilonExplore) == 0 &&
                Double.compare(that.epsilonExploit, epsilonExploit) == 0 &&
                numberOfExploringActions == that.numberOfExploringActions &&
                seed == that.seed &&
                actionsTaken == that.actionsTaken &&
                firstPolicy.equals(that.firstPolicy) &&
                secondPolicy.equals(that.secondPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(epsilonExplore, epsilonExploit, numberOfExploringActions, seed, firstPolicy,
                secondPolicy, actionsTaken);
    }
}