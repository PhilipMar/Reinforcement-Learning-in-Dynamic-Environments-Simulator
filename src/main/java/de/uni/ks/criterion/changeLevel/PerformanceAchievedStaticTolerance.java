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

public class PerformanceAchievedStaticTolerance implements Criterion {

    private final int numberOfConsideredEpisodes;
    private final int numberOfToleranceActions;
    private int numberOfWellPerformedEpisodes;

    /**
     * <p> This Criteria checks if the agent performed well in enough episodes.
     * This means the criterion checks if the number of actions the agent needed in 'numberOfConsideredEpisodes' episodes are within the
     * defined tolerance range.</p>
     * <p> The constructor can be used to set the tolerance and the number of of considered episodes.
     * In this version of the criterion, the tolerance is specified as a static number of actions.
     * For example, a static tolerance value of 10 means that the agent is allowed to take 10 Steps more as would be necessary.
     *
     * @param numberOfConsideredEpisodes number of episodes in which the agent's performance must be rated as good </p>
     * @param numberOfToleranceActions   static number of actions the agent is allowed to exceed the optimal number of actions
     */
    public PerformanceAchievedStaticTolerance(int numberOfConsideredEpisodes, int numberOfToleranceActions) {
        if (0 < numberOfConsideredEpisodes) {
            this.numberOfConsideredEpisodes = numberOfConsideredEpisodes;
        } else {
            throw new IllegalArgumentException("Parameter [numberOfConsideredEpisodes] = " + numberOfConsideredEpisodes
                    + " has to be greater than 0");
        }
        if (0 <= numberOfToleranceActions) {
            this.numberOfToleranceActions = numberOfToleranceActions;
        } else {
            throw new IllegalArgumentException("Parameter [numberOfToleranceActions] = " + numberOfToleranceActions
                    + " has to be greater equal than 0");
        }
        this.numberOfWellPerformedEpisodes = 0;
    }

    public int getNumberOfConsideredEpisodes() {
        return this.numberOfConsideredEpisodes;
    }

    public int getNumberOfToleranceActions() {
        return this.numberOfToleranceActions;
    }

    /**
     * checks if the agent performed well in enough episodes.
     * I.e. checks if the number of actions the agent needed in 'numberOfConsideredEpisodes' episodes are within the
     * defined tolerance range.
     *
     * @param training Training where the Criterion will be checked.
     * @return true if agent performed well enough in all past considered episodes. false otherwise.
     */
    @Override
    public boolean isMet(Training training) {

        // calculate minimum number of actions needed
        int optimalNumberOfActions = training.getMaze().getLengthOfShortestPath();

        // calculate maximum number of actions the agent's performance is still rated as good (add static number of actions)
        int maximumActions = optimalNumberOfActions + this.numberOfToleranceActions;

        // check if agent needed less equal than 'maximumActions' actions and increase counter if true
        if ((training.getAgent().getNumberOfActionsTaken()) <= maximumActions) {
            this.numberOfWellPerformedEpisodes++;
        }

        Logger.addTextToGuiLog("Well performed episodes: " + numberOfWellPerformedEpisodes
                + "/" + this.numberOfConsideredEpisodes, GuiMessageType.Criteria);

        // if agent achieved desired quality in all past considered episodes -> reset counter and return true
        return this.numberOfWellPerformedEpisodes == numberOfConsideredEpisodes;

    }

    @Override
    public void reset() {
        this.numberOfWellPerformedEpisodes = 0;
    }

    @Override
    public String myConfigString() {
        return this.getClass().getSimpleName() + "("
                + "numberOfConsideredEpisodes = " + numberOfConsideredEpisodes + ", "
                + "numberOfToleranceActions = " + numberOfToleranceActions
                + ")";
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{"
                + "numberOfConsideredEpisodes = " + numberOfConsideredEpisodes + ", "
                + "numberOfToleranceActions = " + numberOfToleranceActions
                + "}";
    }

    @Override
    public String getLoggerString() {
        return "Performance Achieved (" + numberOfToleranceActions + " Actions, " + numberOfConsideredEpisodes + " Episodes)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformanceAchievedStaticTolerance that = (PerformanceAchievedStaticTolerance) o;
        return getNumberOfConsideredEpisodes() == that.getNumberOfConsideredEpisodes() &&
                getNumberOfToleranceActions() == that.getNumberOfToleranceActions() &&
                numberOfWellPerformedEpisodes == that.numberOfWellPerformedEpisodes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumberOfConsideredEpisodes(), getNumberOfToleranceActions(), numberOfWellPerformedEpisodes);
    }
}
