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

public class EndStateReached implements Criterion {

    /**
     * This criterion checks if the agent has reached the end state.
     */
    public EndStateReached() {

    }

    /**
     * Checks if an end state is reached.
     *
     * @param training Training where the Criterion will be checked
     * @return True if agent reached end state. False if not.
     */
    @Override
    public boolean isMet(Training training) {
        return training.getMaze().getEndNode().equals(training.getAgent().getCurrentPosition());
    }

    @Override
    public String myConfigString() {
        return this.getClass().getSimpleName() + "(" + ")";
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }

    @Override
    public String getLoggerString() {
        return "End State Reached";
    }

    @Override
    public int hashCode() {
        return Objects.hash(myConfigString());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EndStateReached;
    }
}
