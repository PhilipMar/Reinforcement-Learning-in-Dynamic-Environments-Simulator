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
import de.uni.ks.maze.NodeFactory.Node;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class SoftmaxPolicy implements ExplorationPolicy {

    private double temperature;
    private int precision;
    private final int seed;

    private final Random random;
    private MathContext usedMathContext;

    /**
     * The softmax-policy consists of a random choice according to a Gibbs/Boltzmann distribution.
     * The greedy action is still given the highest selection probability, but all actions are ranked and weighted
     * according to their q values.
     * The temperature is a positive floating point number that controls the exploration behaviour of the agent. High
     * temperatures cause the actions to be all (nearly) equiprobable.
     * Low temperatures cause a greater difference in selection probability for actions that differ in their q values.
     * Therefore, the softmax-policy behaves similar to the random-policy if a very high temperature is used and similar
     * to the greedy-policy if the temperature is very low.
     * The precision determines how many digits of irrational and recurring numbers will be used.
     *
     * @param temperature Exploration parameter that controls the selection behaviour.
     * @param precision   Determines how many digits of irrational numbers will be used.
     * @param seed        Determines which random action selection numbers will be generated.
     */
    public SoftmaxPolicy(double temperature, int precision, int seed) {
        this.seed = seed;
        this.random = new Random(this.seed);

        if (temperature > 0) {
            this.temperature = temperature;
        } else {
            throw new IllegalArgumentException("Parameter [temperature] = " + temperature + " has to be greater than 0");
        }

        if (precision > 0) {
            this.precision = precision;
            this.usedMathContext = new MathContext(precision, RoundingMode.HALF_UP);
        } else {
            throw new IllegalArgumentException("Parameter [precision] = " + precision + " has to be greater than 0");
        }
    }

    /**
     * Performs action selection regarding to the softmax-policy.
     *
     * @param currentNode The node whose neighborhood encodes the state.
     * @param qTable      QTable that stores the knowledge of the agent
     * @return Action that determines the next movement of the agent.
     */
    @Override
    public Action chooseAction(Node currentNode, QTable qTable) {

        // init needed data
        ArrayList<Action> actions = (ArrayList<Action>) getSortedListFromActionSet(qTable.getActions(currentNode).keySet());
        BigDecimal[] probabilities = new BigDecimal[actions.size()];
        BigDecimal[] weightedQValues = new BigDecimal[actions.size()];

        // calculate weighted q values and denominator value once
        BigDecimal denominator = new BigDecimal(0.0d);
        for (int i = 0; i < actions.size(); i++) {
            // calculate weighted Q Value of the currently viewed action
            weightedQValues[i] = getWeightedQValue(qTable, currentNode, actions.get(i));
            // calculate denominator [sum of all weighted Q Values]
            denominator = denominator.add(weightedQValues[i], usedMathContext);
        }

        // calculate probability of each action
        for (int i = 0; i < probabilities.length; i++) {
            // get weighted Q Value exp(Q(s,a)/temperature)
            BigDecimal weightedQValue = weightedQValues[i];
            // calculate probability of current action
            probabilities[i] = weightedQValue.divide(denominator, usedMathContext);
        }

        // --- start of action selection begins ---

        // The sum of all probabilities should actually always be 1, but due to the fact that the precision cannot be infinite
        // the sum will only be a value that goes against 1 but does not reach 1
        // Therefore the absolute probability has to be the sum of all partial probabilities instead of 1
        BigDecimal sumOfProbabilities = Arrays.stream(probabilities).reduce(BigDecimal::add).orElseThrow(RuntimeException::new);

        // generate random number between 0 and sumOfProbabilities
        double randomDouble = sumOfProbabilities.doubleValue() * this.random.nextDouble();
        BigDecimal randomBigDecimal = new BigDecimal(randomDouble);

        // choose action
        int choiceIndex = -1;
        BigDecimal lowerBound = new BigDecimal(0.0d);
        for (int i = 0; i < probabilities.length; i++) {
            BigDecimal upperBound = lowerBound.add(probabilities[i]);

            // Check whether the generated number is within the bounds of the currently considered action (lowerBound <= randomDouble <= upperBound)
            if (lowerBound.compareTo(randomBigDecimal) <= 0 && upperBound.compareTo(randomBigDecimal) >= 0) {
                choiceIndex = i;
                break;
            }

            lowerBound = upperBound;
        }

        return actions.get(choiceIndex);
    }

    /**
     * Calculates weighted Q Value of a state action pair [exp(Q(s,a)/temperature)].
     * The probability calculation is based on the used Gibbs/Boltzmann distribution.
     *
     * @param qTable QTable that stores the knowledge of the agent
     * @param node   The considered node whose neighborhood encodes the state.
     * @param action The considered action whose q values will be weighted
     * @return The weighted q Values of the passed state action pair.
     */
    private BigDecimal getWeightedQValue(QTable qTable, Node node, Action action) {
        BigDecimal qValue = new BigDecimal(qTable.getQValue(node, action));
        BigDecimal tau = new BigDecimal(this.temperature);
        BigDecimal fraction = qValue.divide(tau, usedMathContext);
        return BigDecimalMath.exp(fraction, usedMathContext);
    }

    @Override
    public String myConfigString() {
        return getClass().getSimpleName() + "("
                + "temperature = " + this.temperature + ", "
                + "precision = " + this.precision + ", "
                + "seed = " + this.seed
                + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SoftmaxPolicy that = (SoftmaxPolicy) o;
        return Double.compare(that.temperature, temperature) == 0 &&
                precision == that.precision &&
                seed == that.seed &&
                usedMathContext.equals(that.usedMathContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, precision, seed, usedMathContext);
    }
}