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
package de.uni.ks.criterion.stopEpisode;

import de.uni.ks.Training;
import de.uni.ks.agent.Agent;
import de.uni.ks.criterion.Criterion;
import de.uni.ks.maze.Maze;

import java.util.Objects;

public class AgentExceedsOptimalPathPercentage implements Criterion {

    private double percentageOfExtraActions;

    /**
     * This criterion checks if the agent has reached a maximum number of actions. The number of actions the agent is
     * allowed to take is calculated dynamically based on the optimal number of steps necessary to traverse the current
     * {@link de.uni.ks.maze.Maze}.
     * <p>
     * This criterion allows the agent to take a variable amount of extra actions, where the total number of extra
     * actions is defined as a percentage of the optimal number of actions. The threshold is calculated by
     * max = {@link Maze#getLengthOfShortestPath()}
     * + ({@link Maze#getLengthOfShortestPath()} * {@code percentageOfExtraActions})
     *
     * @param percentageOfExtraActions The percentage of extra actions the agent is allowed to do.
     */
    public AgentExceedsOptimalPathPercentage(double percentageOfExtraActions) {
        if (0 <= percentageOfExtraActions) {
            this.percentageOfExtraActions = percentageOfExtraActions;
        } else {
            throw new IllegalArgumentException("Parameter [percentageOfExtraActions] = " + percentageOfExtraActions
                    + " has to be greater equal than 0");
        }
    }

    @java.lang.Override
    public boolean isMet(Training training) {
        int lengthOfShortestPath = training.getMaze().getLengthOfShortestPath();

        Agent agent = training.getAgent();

        return agent.getNumberOfActionsTaken() > lengthOfShortestPath
                + (lengthOfShortestPath * percentageOfExtraActions);
    }

    @java.lang.Override
    public String myConfigString() {
        return getClass().getSimpleName() + "("
                + "percentageOfExtraActions = " + percentageOfExtraActions
                + ")";
    }

    @Override
    public String getLoggerString() {
        return "Exceeded Optimal Path (" + percentageOfExtraActions * 100 + "%)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentExceedsOptimalPathPercentage that = (AgentExceedsOptimalPathPercentage) o;
        return Double.compare(that.percentageOfExtraActions, percentageOfExtraActions) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(percentageOfExtraActions);
    }
}
