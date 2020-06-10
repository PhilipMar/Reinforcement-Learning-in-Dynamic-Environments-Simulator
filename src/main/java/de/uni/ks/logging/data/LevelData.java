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

import de.uni.ks.criterion.Criterion;
import de.uni.ks.logging.Logger;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.complexityFunction.ComplexityFunction;
import de.uni.ks.maze.utils.MazeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * This class can be seen as a data class that stores all information about a level.
 * Furthermore, certain level related statistics can be calculated within this class.
 */
public class LevelData extends LogData {
    private final int levelNr;
    private final ArrayList<EpisodeData> episodes;
    private Maze maze;
    private Criterion occurredLevelAbortCriterion;

    private int optimalNumberOfActions;
    private Double averageNumberOfAction;
    private Double optimalReward;
    private Double averageReward;
    private Double complexity;
    private TreeMap<Criterion, Integer> episodeStopCriterionCounter;

    public LevelData(int levelNr) {
        this.levelNr = levelNr;
        this.episodes = new ArrayList<>();
    }

    public void setMaze(Maze maze) {
        this.maze = new Maze(maze);
        this.refreshOptimalNumberOfActions();
        this.refreshOptimalReward();
    }

    public void addEpisodeData(EpisodeData episodeData) {
        this.episodes.add(episodeData);
        Logger.CurrentData.currentEpisodeData = episodeData;
    }

    public EpisodeData getEpisodeData(int episodeNr) {
        for (EpisodeData episode : episodes) {
            if (episode.getEpisodeNr() == episodeNr) {
                return episode;
            }
        }
        return null;
    }

    public int getLevelNr() {
        return levelNr;
    }

    public ArrayList<EpisodeData> getEpisodes() {
        return episodes;
    }

    public Maze getMaze() {
        return maze;
    }

    public Criterion getOccurredLevelAbortCriterion() {
        return occurredLevelAbortCriterion;
    }

    public void setOccurredLevelAbortCriterion(Criterion occurredLevelAbortCriterion) {
        this.occurredLevelAbortCriterion = occurredLevelAbortCriterion;
    }

    private void refreshOptimalNumberOfActions() {
        optimalNumberOfActions = MazeUtils.getOptimalNumberOfActions(this.maze, this.maze.getStartNode(),
                this.maze.getEndNode());
    }

    public int getOptimalNumberOfActions() {
        return optimalNumberOfActions;
    }

    private void refreshOptimalReward() {
        optimalReward = MazeUtils.getOptimalReward(this.maze, this.maze.getStartNode(), this.maze.getEndNode());
    }

    public Double getOptimalReward() {
        return optimalReward;
    }

    public void refreshAverageNumberOfActions() {
        double totalActionsTaken = 0.0d;
        for (EpisodeData episodeData : this.episodes) {
            totalActionsTaken += episodeData.getNumberOfActions();
        }
        this.averageNumberOfAction = totalActionsTaken / this.episodes.size();
    }

    public Double getAverageNumberOfActions() {
        return averageNumberOfAction;
    }

    public void refreshAverageReward() {
        double totalLevelReward = 0.0d;
        for (EpisodeData episodeData : this.episodes) {
            totalLevelReward += episodeData.getTotalReward();
        }
        this.averageReward = totalLevelReward / this.episodes.size();
    }

    public Double getAverageReward() {
        return averageReward;
    }

    public void refreshComplexity(ComplexityFunction complexityFunction) {
        this.complexity = complexityFunction.calculateComplexity(maze);
    }

    public Double getComplexity() {
        return complexity;
    }

    public void refreshEpisodeStoppingCriteriaOccurrences(List<Criterion> usedEpisodeStoppingCriteria) {
        // add names of episode stopping criteria in a sorted order to the head line
        ArrayList<Criterion> episodeStoppingCriteria = new ArrayList<>(usedEpisodeStoppingCriteria);

        // create TreeMap that will count how often which criterion has occurred
        this.episodeStopCriterionCounter = createCriterionCounterTreeMap(episodeStoppingCriteria);

        for (EpisodeData episodeData : this.episodes) {
            // check which episode stopping criterion occurred and increase the counter of the criterion
            Criterion occurredEpisodeStoppingCriterion = episodeData.getOccurredEpisodeStopCriterion();
            if (episodeStopCriterionCounter.containsKey(occurredEpisodeStoppingCriterion)) {
                int oldCounterValue = episodeStopCriterionCounter.get(occurredEpisodeStoppingCriterion);
                episodeStopCriterionCounter.replace(occurredEpisodeStoppingCriterion, oldCounterValue + 1);
            }
        }
    }

    /**
     * Creates a new TreeMap that is supposed to count the occurrences of criteria.
     * Therefore all passed criteria will be used as keys and every criterion key will be initialized with a value of 0.
     *
     * @param criteria ArrayList with all criteria to be counted
     * @return The newly created TreeMap to count the occurrences of the criteria
     */
    private TreeMap<Criterion, Integer> createCriterionCounterTreeMap(ArrayList<Criterion> criteria) {
        TreeMap<Criterion, Integer> criterionCounterValues = new TreeMap<>(new Criterion.CriterionComparator());
        for (Criterion criterion : criteria) {
            criterionCounterValues.put(criterion, 0);
        }
        return criterionCounterValues;
    }

    public TreeMap<Criterion, Integer> getEpisodeStopCriterionCounter() {
        return episodeStopCriterionCounter;
    }


    @Override
    public String toString() {
        return Integer.toString(this.getLevelNr());
    }
}
