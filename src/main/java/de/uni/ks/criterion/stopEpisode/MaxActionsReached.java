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
import de.uni.ks.criterion.Criterion;

import java.util.Objects;

public class MaxActionsReached implements Criterion {

    private final int maxActions;

    /**
     * This criterion checks if the agent has reached a maximum number of actions.
     *
     * @param maxActions The number of actions the agent has to reach to trigger the criterion.
     */
    public MaxActionsReached(int maxActions) {
        if (0 < maxActions) {
            this.maxActions = maxActions;
        } else {
            throw new IllegalArgumentException("Parameter [maxActions] = " + maxActions + " has to be greater than 0");
        }
    }

    /**
     * Checks if agent reached the maximum number of actions
     *
     * @param training Training where the criterion will be checked
     * @return True if agent reached maximum number of actions. False if not.
     */
    @Override
    public boolean isMet(Training training) {
        return training.getAgent().getNumberOfActionsTaken() == this.maxActions;
    }

    public int getMaxActions() {
        return this.maxActions;
    }

    @Override
    public String toString() {
        return "MaxActionsReached{" +
                "maxActions=" + maxActions +
                '}';
    }

    @Override
    public String myConfigString() {
        return this.getClass().getSimpleName() + "("
                + "maxActions = " + maxActions
                + ")";
    }

    @Override
    public String getLoggerString() {
        return "Max Actions Reached (" + maxActions + " Actions)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaxActionsReached that = (MaxActionsReached) o;
        return getMaxActions() == that.getMaxActions();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMaxActions());
    }
}
