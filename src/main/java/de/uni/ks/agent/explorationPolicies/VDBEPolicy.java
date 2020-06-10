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

import ch.obermuhlner.math.big.BigDecimalMath;
import de.uni.ks.agent.Action;
import de.uni.ks.agent.QTable;
import de.uni.ks.logging.Logger;
import de.uni.ks.logging.messages.GuiMessageType;
import de.uni.ks.maze.NodeFactory.Node;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Objects;

public class VDBEPolicy implements ExplorationPolicy {

    private double inverseSensitivity;
    private double epsilon_0;
    private int seed;

    private MathContext mathContext;
    private HashMap<String, Double> epsilonValues;
    private EpsilonGreedyPolicy epsilonGreedyPolicy;

    /**
     * The implemented ''Value-Difference Based Exploration'' (VDBE)-policy is built on the epsilon-greedy-policy.
     * The main characteristic of a VDBE-policy is that every state has its own exploration rate (in this case epsilon)
     * which will be adapted to the learning process based on the value-difference.
     * The Value-difference is the resulting difference of a Q Value after an action was executed.
     *
     * @param inverseSensitivity Determines which impact the value differences have on the adaption of the exploration parameter
     * @param epsilon_0          Initial epsilon value of a state
     * @param seed               Determines which random action selection numbers will be generated.
     */
    public VDBEPolicy(double inverseSensitivity, double epsilon_0, int seed) {

        if (inverseSensitivity > 0) {
            this.inverseSensitivity = inverseSensitivity;
        } else {
            throw new IllegalArgumentException("Parameter [inverseSensitivity] = " + inverseSensitivity + " has to be greater than 0");
        }

        if (0 <= epsilon_0 && epsilon_0 <= 1) {
            this.epsilon_0 = epsilon_0;
            this.epsilonGreedyPolicy = new EpsilonGreedyPolicy(epsilon_0, seed);
        } else {
            throw new IllegalArgumentException("Parameter [epsilon_0] = " + epsilon_0 + " is not in [0, 1].");
        }

        int PRECISION = 100;
        this.mathContext = new MathContext(PRECISION, RoundingMode.DOWN);
        this.seed = seed;
        this.epsilonValues = new HashMap<>();
    }

    /**
     * Performs action selection according to the VDBE-policy.
     *
     * @param currentNode The node whose neighborhood encodes the state.
     * @param qTable      QTable that stores the knowledge of the agent.
     * @return Action that determines the next movement of the agent.
     */
    @Override
    public Action chooseAction(Node currentNode, QTable qTable) {

        // create new epsilon for passed state if no epsilon is saved
        if (!epsilonValues.containsKey(currentNode.getState())) {
            epsilonValues.put(currentNode.getState(), this.epsilon_0);
        }

        // choose epsilon greedy action with the epsilon of the considered state
        epsilonGreedyPolicy.setEpsilon(getEpsilon(currentNode));
        return epsilonGreedyPolicy.chooseAction(currentNode, qTable);
    }

    /**
     * Updates the epsilon value of the node the agent was placed on before the last interaction.
     *
     * @param oldNode      The node the agent was placed on before the interaction
     * @param chosenAction The Action the agent chose in the interaction
     * @param oldQValue    The Q Value of the state-action pair (oldNode, chosenAction) before the last interaction
     * @param newNode      The node the agent moved to after the interaction
     * @param qTable       QTable that stores the knowledge of the agent.
     */
    @Override
    public void postProcessing(Node oldNode, Action chosenAction, double oldQValue, Node newNode, QTable qTable) {
        // calculate new epsilon
        double activationValue = boltzmannActivation(oldQValue, qTable.getQValue(oldNode, chosenAction));
        double newEpsilon = getNewEpsilonValue(oldNode, activationValue);

        // mention change in gui log
        if (epsilonValues.get(oldNode.getState()) < newEpsilon) {
            Logger.addTextToGuiLog("Epsilon of state " + oldNode.getState() + " increased", GuiMessageType.Policy);
        } else if (epsilonValues.get(oldNode.getState()) > newEpsilon) {
            Logger.addTextToGuiLog("Epsilon of state " + oldNode.getState() + " decreased", GuiMessageType.Policy);
        } else {
            Logger.addTextToGuiLog("Epsilon of state " + oldNode.getState() + " stayed the same", GuiMessageType.Policy);
        }

        // update epsilon of previous state
        this.epsilonValues.put(oldNode.getState(), newEpsilon);
    }

    /**
     * Calculates activation value (double between 0 and 1) based on a Gibbs/Boltzmann distribution.
     *
     * @param oldQValue The old q value of the considered state action pair
     * @param newQValue The new q value of the considered state action pair
     * @return The activation value that controls the change of the exploration parameter
     */
    private double boltzmannActivation(double oldQValue, double newQValue) {
        // calculate e term of the old q value
        BigDecimal oldQValueFraction = BigDecimal.valueOf(oldQValue).divide(BigDecimal.valueOf(this.inverseSensitivity), this.mathContext);
        BigDecimal oldQValueETerm = BigDecimalMath.exp(oldQValueFraction, this.mathContext);

        // calculate e term of the new q value
        BigDecimal newQValueFraction = BigDecimal.valueOf(newQValue).divide(BigDecimal.valueOf(this.inverseSensitivity), this.mathContext);
        BigDecimal newQValueETerm = BigDecimalMath.exp(newQValueFraction, this.mathContext);

        // calculate first fraction
        BigDecimal firstFractionDenominator = oldQValueETerm.add(newQValueETerm);
        BigDecimal firstFraction = oldQValueETerm.divide(firstFractionDenominator, mathContext);

        // calculate second fraction
        BigDecimal secondFraction = newQValueETerm.divide(firstFractionDenominator, mathContext);

        // calculate activation value
        BigDecimal activationValue = firstFraction.subtract(secondFraction).abs();
        return activationValue.doubleValue();
    }

    /**
     * Calculates the new epsilon value of a passed node and an given activation value.
     *
     * @param node       The node whose new epsilon value will be calculated.
     * @param activation The previously calculated activation value that determines the change of the old epsilon.
     * @return The new epsilon value of the previous node.
     */
    private double getNewEpsilonValue(Node node, double activation) {
        // calculate learn rate as recommended by Michel Tokic
        double learnRate = 1 / (double) node.getPassableNeighbors().size();
        // calculate and return new epsilon value
        return learnRate * activation + (1 - learnRate) * getEpsilon(node);
    }

    /**
     * Returns the saved epsilon for the passed node.
     *
     * @param node The node whose epsilon value is requested.
     * @return The epsilon of the passed node. Returns negative Infinity if no epsilon for the given node exists.
     */
    public double getEpsilon(Node node) {
        if (this.epsilonValues.containsKey(node.getState())) {
            return epsilonValues.get(node.getState());
        } else {
            System.err.println("epsilon of state " + node.getState() + " does not exist");
            return -Double.POSITIVE_INFINITY;
        }
    }

    @Override
    public String myConfigString() {
        return getClass().getSimpleName() + "("
                + "inverseSensitivity = " + inverseSensitivity + ", "
                + "epsilon_0 = " + epsilon_0 + ", "
                + "seed = " + seed
                + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VDBEPolicy that = (VDBEPolicy) o;
        return Double.compare(that.inverseSensitivity, inverseSensitivity) == 0 &&
                Double.compare(that.epsilon_0, epsilon_0) == 0 &&
                seed == that.seed &&
                mathContext.equals(that.mathContext) &&
                epsilonValues.equals(that.epsilonValues) &&
                epsilonGreedyPolicy.equals(that.epsilonGreedyPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inverseSensitivity, epsilon_0, seed, mathContext, epsilonValues, epsilonGreedyPolicy);
    }
}