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

import java.util.Objects;

public class MaxEpisodesReached implements Criterion {

    private final int numberOfEpisodes;

    /**
     * This criterion checks if the agent has reached a maximum number of actions.
     *
     * @param numberOfEpisodes The number of episode that needs to be absolved to trigger the criterion.
     */
    public MaxEpisodesReached(int numberOfEpisodes) {

        if (0 > numberOfEpisodes) {
            throw new IllegalArgumentException("Parameter [numberOfEpisodes] = "
                    + numberOfEpisodes + " has to be greater than 0");
        }

        this.numberOfEpisodes = numberOfEpisodes;
    }

    /**
     * Checks if agent reached the maximum number of episodes.
     *
     * @param training Training where the Criterion will be checked.
     * @return True if agent reached maximum number of episodes. False if not.
     */
    @Override
    public boolean isMet(Training training) {
        return training.getCurrentEpisodeNr() == this.numberOfEpisodes;
    }

    public int getNumberOfEpisodes() {
        return this.numberOfEpisodes;
    }

    @Override
    public String toString() {
        return "MaxEpisodesReached{" +
                "numberOfEpisodes=" + numberOfEpisodes +
                '}';
    }

    @Override
    public String myConfigString() {
        return this.getClass().getSimpleName() + "("
                + "numberOfEpisodes = " + numberOfEpisodes
                + ")";
    }

    @Override
    public String getLoggerString() {
        return "Max Episodes Reached (" + numberOfEpisodes + " Episodes)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaxEpisodesReached that = (MaxEpisodesReached) o;
        return getNumberOfEpisodes() == that.getNumberOfEpisodes();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumberOfEpisodes());
    }
}