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
package de.uni.ks.configuration;

import de.uni.ks.agent.explorationPolicies.ExplorationPolicy;
import de.uni.ks.criterion.Criterion;
import de.uni.ks.maze.complexityFunction.ComplexityFunction;
import de.uni.ks.maze.utils.mazeOperators.MazeOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents the configuration file that was used to start this program. It contains the value of each
 * configured key. When this class is updated, the {@link Config#toString()}, {@link Config#equals(Object)} and
 * {@link Config#hashCode()} methods should be updated too.
 */
public class Config {

    @Section(name = "Reinforcement Learning")
    @DoNotChange
    public Double initialQValue;
    @Section(name = "Reinforcement Learning")
    @DoNotChange
    public Double wayNodeReward;
    @Section(name = "Reinforcement Learning")
    @DoNotChange
    public Double endNodeReward;

    @Section(name = "Reinforcement Learning")
    public Double qLearningAlpha;
    @Section(name = "Reinforcement Learning")
    public Double qLearningGamma;
    @DoNotChange
    @Section(name = "Reinforcement Learning")
    public Boolean startEachLevelWithEmptyQTable;
    @Section(name = "Reinforcement Learning")
    public ExplorationPolicy explorationPolicy;
    @Section(name = "Reinforcement Learning")
    public List<Criterion> episodeStoppingCriteria = new ArrayList<>();

    @Section(name = "Maze Change")
    public List<Criterion> levelChangeCriteria = new ArrayList<>();
    @Section(name = "Maze Change")
    @DoNotChange
    public ComplexityFunction complexityFunction;
    @Section(name = "Maze Change")
    public Integer numberOfLevels;
    @Section(name = "Maze Change")
    public Double delta;
    @DoNotChange
    @Section(name = "Maze Change")
    public Integer changeMazeSeed;
    @Section(name = "Maze Change")
    @DoNotChange
    public List<MazeOperator> mazeOperators = new ArrayList<>();

    @Section(name = "Initial Maze")
    public Boolean horizontal;
    @Section(name = "Initial Maze")
    public Integer initialPathLength;
    @Section(name = "Initial Maze")
    public Integer numberOfWayColors;
    @Section(name = "Initial Maze")
    public Integer numberOfWallColors;
    @Section(name = "Initial Maze")
    @DoNotChange
    public Integer generatedWayColorsSeed;
    @Section(name = "Initial Maze")
    @DoNotChange
    public Integer generatedWallColorsSeed;
    @Section(name = "Initial Maze")
    @DoNotChange
    public Integer usedWayColorsSeed;
    @Section(name = "Initial Maze")
    @DoNotChange
    public Integer usedWallColorsSeed;
    @Section(name = "Initial Maze")
    @DoNotChange
    public Integer minWallWayBrightnessDifference;

    @Section(name = "Misc")
    public String trainingName;
    @Section(name = "Misc")
    @DoNotChange
    public Boolean restrictImageSize;
    @Section(name = "Misc")
    @DoNotChange
    public Boolean showProgressBarInConsole;

    @Override
    public String toString() {
        return "Config{" +
                "trainingName='" + trainingName + '\'' +
                ", initialQValue=" + initialQValue +
                ", wayNodeReward=" + wayNodeReward +
                ", endNodeReward=" + endNodeReward +
                ", qLearningAlpha=" + qLearningAlpha +
                ", qLearningGamma=" + qLearningGamma +
                ", startEachLevelWithEmptyQTable=" + startEachLevelWithEmptyQTable +
                ", explorationPolicy=" + explorationPolicy +
                ", episodeStoppingCriteria=" + episodeStoppingCriteria +
                ", levelChangeCriteria=" + levelChangeCriteria +
                ", complexityFunction=" + complexityFunction +
                ", horizontal=" + horizontal +
                ", initialPathLength=" + initialPathLength +
                ", numberOfWayColors=" + numberOfWayColors +
                ", numberOfWallColors=" + numberOfWallColors +
                ", generatedWayColorsSeed=" + generatedWayColorsSeed +
                ", generatedWallColorsSeed=" + generatedWallColorsSeed +
                ", usedWayColorsSeed=" + usedWayColorsSeed +
                ", usedWallColorsSeed=" + usedWallColorsSeed +
                ", minWallWayBrightnessDifference=" + minWallWayBrightnessDifference +
                ", numberOfLevels=" + numberOfLevels +
                ", delta=" + delta +
                ", changeMazeSeed=" + changeMazeSeed +
                ", mazeOperators=" + mazeOperators +
                ", restrictImageSize=" + restrictImageSize +
                ", showProgressBarInConsole=" + showProgressBarInConsole +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return Objects.equals(trainingName, config.trainingName) &&
                Objects.equals(initialQValue, config.initialQValue) &&
                Objects.equals(wayNodeReward, config.wayNodeReward) &&
                Objects.equals(endNodeReward, config.endNodeReward) &&
                Objects.equals(qLearningAlpha, config.qLearningAlpha) &&
                Objects.equals(qLearningGamma, config.qLearningGamma) &&
                Objects.equals(startEachLevelWithEmptyQTable, config.startEachLevelWithEmptyQTable) &&
                Objects.equals(explorationPolicy, config.explorationPolicy) &&
                Objects.equals(episodeStoppingCriteria, config.episodeStoppingCriteria) &&
                Objects.equals(levelChangeCriteria, config.levelChangeCriteria) &&
                Objects.equals(complexityFunction, config.complexityFunction) &&
                Objects.equals(horizontal, config.horizontal) &&
                Objects.equals(initialPathLength, config.initialPathLength) &&
                Objects.equals(numberOfWayColors, config.numberOfWayColors) &&
                Objects.equals(numberOfWallColors, config.numberOfWallColors) &&
                Objects.equals(generatedWayColorsSeed, config.generatedWayColorsSeed) &&
                Objects.equals(generatedWallColorsSeed, config.generatedWallColorsSeed) &&
                Objects.equals(usedWayColorsSeed, config.usedWayColorsSeed) &&
                Objects.equals(usedWallColorsSeed, config.usedWallColorsSeed) &&
                Objects.equals(minWallWayBrightnessDifference, config.minWallWayBrightnessDifference) &&
                Objects.equals(numberOfLevels, config.numberOfLevels) &&
                Objects.equals(delta, config.delta) &&
                Objects.equals(changeMazeSeed, config.changeMazeSeed) &&
                Objects.equals(mazeOperators, config.mazeOperators) &&
                Objects.equals(restrictImageSize, config.restrictImageSize) &&
                Objects.equals(showProgressBarInConsole, config.showProgressBarInConsole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingName, initialQValue, wayNodeReward, endNodeReward, qLearningAlpha, qLearningGamma,
                startEachLevelWithEmptyQTable, explorationPolicy, episodeStoppingCriteria, levelChangeCriteria,
                complexityFunction, horizontal, initialPathLength, numberOfWayColors, numberOfWallColors,
                generatedWayColorsSeed, generatedWallColorsSeed, usedWayColorsSeed, usedWallColorsSeed,
                minWallWayBrightnessDifference, numberOfLevels, delta, changeMazeSeed, mazeOperators,
                restrictImageSize, showProgressBarInConsole);
    }
}
