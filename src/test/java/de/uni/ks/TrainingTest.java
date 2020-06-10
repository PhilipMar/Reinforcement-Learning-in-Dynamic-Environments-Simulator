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
package de.uni.ks;

import de.uni.ks.agent.Agent;
import de.uni.ks.criterion.Criterion;
import de.uni.ks.criterion.changeLevel.MaxEpisodesReached;
import de.uni.ks.criterion.stopEpisode.EndStateReached;
import de.uni.ks.criterion.stopEpisode.MaxActionsReached;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory.Node;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingTest {


    // tests checkForEpisodeStopCriterion() if no criterion is true
    @Test
    void testCheckForEpisodeStopCriterionIfNoCriterionIsMet() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node center = myMaze.getStartNode();

        // init Agent
        Agent agent = new Agent(center, null, 0.0d, 0.0d, 0.0d);
        agent.setNumberOfActionsTaken(2);

        // init Training
        Training training = new Training();
        training.setAgent(agent);
        training.setMaze(myMaze);

        // init episode stop criteria
        int maxActions = 10;
        Criterion[] episodeStopCriteria = {new MaxActionsReached(maxActions), new EndStateReached()};
        training.getConfig().episodeStoppingCriteria = Arrays.asList(episodeStopCriteria);

        // check if no criterion is true
        assertFalse(training.checkForEpisodeStopCriterion(), "no episode stop criterion is true");

    }

    //  tests checkForEpisodeStoppingCriteria() if one criterion is true and one is false
    @Test
    void testCheckForEpisodeStoppingCriteriaIfOneCriterionMet() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node center = myMaze.getStartNode();

        // init Agent
        Agent agent = new Agent(center, null, 0.0d, 0.0d, 0.0d);
        agent.setNumberOfActionsTaken(10);

        // init Training
        Training training = new Training();
        training.setAgent(agent);
        training.setMaze(myMaze);

        // init episode stop criteria
        int maxActions = 10;
        Criterion[] episodeStoppingCriteria = {new MaxActionsReached(maxActions), new EndStateReached()};
        training.getConfig().episodeStoppingCriteria = Arrays.asList(episodeStoppingCriteria);

        // check if no criterion is true
        assertTrue(training.checkForEpisodeStopCriterion(), "one episode stop criterion is true");

    }

    // tests checkForLevelChangeCriteria() if no criterion is true
    @Test
    void testCheckForLevelChangeCriteriaIfNoCriterionIsMet() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node center = myMaze.getStartNode();

        // init Agent
        Agent agent = new Agent(center, null, 0.0d, 0.0d, 0.0d);

        // init Training
        Training training = new Training();
        training.setAgent(agent);
        training.setMaze(myMaze);
        training.setCurrentEpisodeNr(0);

        // init episode stop criterion (makes no sense but needed two level change criteria)
        int maxEpisodes1 = 10;
        int maxEpisodes2 = 20;
        Criterion[] changeLevelCriteria = {new MaxEpisodesReached(maxEpisodes1), new MaxEpisodesReached(maxEpisodes2)};
        training.getConfig().levelChangeCriteria = Arrays.asList(changeLevelCriteria);

        // check if no criterion is true
        assertFalse(training.checkForLevelChangeCriteria(), "no level change criterion is true");

    }

    // tests checkForLevelChangeCriteria() if one criterion is true and one is false
    @Test
    void testCheckForLevelChangeCriteriaIfOneCriterionIsMet() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node center = myMaze.getStartNode();

        // init Agent
        Agent agent = new Agent(center, null, 0.0d, 0.0d, 0.0d);

        // init Training
        Training training = new Training();
        training.setAgent(agent);
        training.setMaze(myMaze);
        training.setCurrentEpisodeNr(10);

        // init episode stop criterion (makes no sense but needed two level change criteria)
        int maxEpisodes1 = 10;
        int maxEpisodes2 = 20;
        Criterion[] changeLevelCriteria = {new MaxEpisodesReached(maxEpisodes1), new MaxEpisodesReached(maxEpisodes2)};
        training.getConfig().levelChangeCriteria = Arrays.asList(changeLevelCriteria);

        // check if method returns true
        assertTrue(training.checkForLevelChangeCriteria(), "no level change criterion is true");

    }
}