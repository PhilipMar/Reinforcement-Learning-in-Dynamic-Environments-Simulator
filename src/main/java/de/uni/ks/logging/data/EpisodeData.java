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
package de.uni.ks.logging.data;

import de.uni.ks.agent.QTable;
import de.uni.ks.criterion.Criterion;

/**
 * This class can be seen as a data class that stores all information about an episode.
 * Furthermore, certain episode related statistics can be calculated within this class.
 */
public class EpisodeData extends LogData {
    private final int episodeNr;
    private QTable qTable;
    private int numberOfActions;
    private Double totalReward;
    private Criterion occurredEpisodeStopCriterion;

    public EpisodeData(int episodeNr) {
        this.episodeNr = episodeNr;
        this.totalReward = 0.0d;
    }

    public void setQTable(QTable qTable) {
        this.qTable = new QTable(qTable);
    }

    public void setNumberOfActions(int numberOfActions) {
        this.numberOfActions = numberOfActions;
    }

    public void setTotalReward(Double totalReward) {
        this.totalReward = totalReward;
    }

    public int getEpisodeNr() {
        return episodeNr;
    }

    public QTable getQTable() {
        return qTable;
    }

    public int getNumberOfActions() {
        return numberOfActions;
    }

    public Double getTotalReward() {
        return totalReward;
    }

    public Criterion getOccurredEpisodeStopCriterion() {
        return occurredEpisodeStopCriterion;
    }

    public void setOccurredEpisodeStopCriterion(Criterion occurredEpisodeStopCriterion) {
        this.occurredEpisodeStopCriterion = occurredEpisodeStopCriterion;
    }
}