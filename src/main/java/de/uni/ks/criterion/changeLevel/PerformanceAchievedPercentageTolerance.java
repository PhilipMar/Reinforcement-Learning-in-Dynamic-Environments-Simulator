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
package de.uni.ks.criterion.changeLevel;

import de.uni.ks.Training;
import de.uni.ks.criterion.Criterion;
import de.uni.ks.logging.Logger;
import de.uni.ks.logging.messages.GuiMessageType;

import java.util.Objects;

public class PerformanceAchievedPercentageTolerance implements Criterion {

    private final int numberOfConsideredEpisodes;
    private final double percentageTolerance;
    private int numberOfWellPerformedEpisodes;

    /**
     * <p> This Criteria checks if the agent performed well in enough episodes.
     * This means the criterion checks if the number of actions the agent needed
     * in 'numberOfConsideredEpisodes' episodes are within the defined tolerance range. </p>
     * <p> The constructor can be used to set the tolerance and the number of of considered episodes.
     * In this version of the criterion, the tolerance is specified in percent.
     * For example, a percentage value of 1 means that the agent has a 100% tolerance and
     * therefore is allowed to take twice as many actions as would be necessary. </p>
     *
     * @param numberOfConsideredEpisodes number of episodes in which the agents performance must be rated as good
     * @param percentageTolerance        percentage value the agent is allowed to exceed the optimal number of actions
     */
    public PerformanceAchievedPercentageTolerance(int numberOfConsideredEpisodes, double percentageTolerance) {
        if (0 < numberOfConsideredEpisodes) {
            this.numberOfConsideredEpisodes = numberOfConsideredEpisodes;
        } else {
            throw new IllegalArgumentException("Parameter [numberOfConsideredEpisodes] = " + numberOfConsideredEpisodes
                    + " has to be greater than 0");
        }
        if (0 <= percentageTolerance) {
            this.percentageTolerance = percentageTolerance;
        } else {
            throw new IllegalArgumentException("Parameter [percentageTolerance] = " + percentageTolerance
                    + " has to be greater equal than 0");
        }
        this.numberOfWellPerformedEpisodes = 0;
    }

    /**
     * Checks if the agent performed well in enough episodes.
     * I.e. checks if the number of actions the agent needed in 'numberOfConsideredEpisodes' episodes are within the
     * defined tolerance range.
     *
     * @param training Training where the criterion will be checked.
     * @return True if agent performed well enough in all past considered episodes. False otherwise.
     */
    @Override
    public boolean isMet(Training training) {

        // calculate minimum number of actions needed
        int optimalNumberOfActions = training.getMaze().getLengthOfShortestPath();

        // calculate maximum number of actions the agent's performance is still rated as good (add percentage value to
        // optimal number of actions)
        int maximumActions = optimalNumberOfActions + (int) (((double) optimalNumberOfActions) * this.percentageTolerance);

        // check if agent needed less equal than 'maximumActions' actions and increase counter if true
        if ((training.getAgent().getNumberOfActionsTaken()) <= maximumActions) {
            this.numberOfWellPerformedEpisodes++;
        }

        Logger.addTextToGuiLog("Well performed episodes: " + numberOfWellPerformedEpisodes + "/"
                + this.numberOfConsideredEpisodes, GuiMessageType.Criteria);

        // check if agent achieved desired quality in all past considered episodes
        return this.numberOfWellPerformedEpisodes == numberOfConsideredEpisodes;
    }

    @Override
    public void reset() {
        this.numberOfWellPerformedEpisodes = 0;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{"
                + "numberOfConsideredEpisodes = " + numberOfConsideredEpisodes + ", "
                + "percentageTolerance = " + percentageTolerance
                + "}";
    }

    @Override
    public String myConfigString() {
        return this.getClass().getSimpleName() + "("
                + "numberOfConsideredEpisodes = " + numberOfConsideredEpisodes + ", "
                + "percentageTolerance = " + percentageTolerance
                + ")";
    }

    @Override
    public String getLoggerString() {
        return "Performance Achieved Percentage (" + percentageTolerance + "%, " + numberOfConsideredEpisodes + " Episodes)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformanceAchievedPercentageTolerance that = (PerformanceAchievedPercentageTolerance) o;
        return numberOfConsideredEpisodes == that.numberOfConsideredEpisodes &&
                Double.compare(that.percentageTolerance, percentageTolerance) == 0 &&
                numberOfWellPerformedEpisodes == that.numberOfWellPerformedEpisodes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfConsideredEpisodes, percentageTolerance, numberOfWellPerformedEpisodes);
    }
}
