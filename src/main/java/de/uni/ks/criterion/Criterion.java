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
package de.uni.ks.criterion;

import de.uni.ks.Training;
import de.uni.ks.configuration.WritableToConfig;

import java.util.Comparator;
import java.util.List;

public interface Criterion extends WritableToConfig {

    /**
     * Determines if the criterion is met or not.
     *
     * @param training Training where the criterion will be checked
     * @return True if criterion is met, false otherwise.
     */
    boolean isMet(Training training);

    /**
     * Method to reset criterion after the end of an episode or an level.
     */
    default void reset() {
    }

    /**
     * Comparator class that compares the LoggerString of two criteria objects.
     * Comparator is necessary to use criteria as keys in a {@code TreeMap} data structure.
     * A corresponding data structure is currently used in  {@link de.uni.ks.logging.data.LevelData} to count the
     * occurrences of criteria.
     *
     * @see de.uni.ks.logging.data.LevelData#refreshEpisodeStoppingCriteriaOccurrences(List)
     */
    class CriterionComparator implements Comparator<Criterion> {
        @Override
        public int compare(Criterion criterion1, Criterion criterion2) {
            return criterion1.getLoggerString().compareTo(criterion2.getLoggerString());
        }
    }

    /**
     * Method returns a string that describes the criterion in the log.
     *
     * @return the string that will be used to describe the criterion in the log.
     */
    default String getLoggerString() {
        return this.getClass().getSimpleName();
    }

}
