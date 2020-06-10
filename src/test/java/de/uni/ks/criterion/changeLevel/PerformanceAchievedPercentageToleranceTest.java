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
import de.uni.ks.agent.Agent;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.utils.MazeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceAchievedPercentageToleranceTest {

    // tests if PerformanceAchieved criterion works correctly if agent performed well in enough episodes. Uses percentage value as tolerance.
    @Test
    void testPerformanceAchievedPercentageToleranceTrueAfterSixEpisodes() {
        // init place holder maze
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Maze maze = MazeUtils.getPlaceholderMaze(nodeFactory);

        // init Agent
        Agent agent = new Agent(maze.getStartNode(), null, 0.0d, 0.0d, 0.0d);

        // init Training
        Training training = new Training();
        training.setAgent(agent);
        training.setMaze(maze);

        // init criterion (tolerance = 15% = 1 action)
        int consideredEpisodes = 5;
        Double tolerancePercentage = 0.15d;
        PerformanceAchievedPercentageTolerance performanceAchieved = new PerformanceAchievedPercentageTolerance(consideredEpisodes, tolerancePercentage);

        // checks criterion after first episode (agent doesn't reached desired quality this episode)
        agent.setNumberOfActionsTaken(9);
        training.setCurrentEpisodeNr(0);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after first episode");

        // checks criterion after second episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(1);
        agent.setNumberOfActionsTaken(8);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after second episode");

        // checks criterion after third episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(2);
        agent.setNumberOfActionsTaken(7);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after third episode");

        // checks criterion after fourth episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(3);
        agent.setNumberOfActionsTaken(8);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after fourth episode");

        // checks criterion after fifth episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(4);
        agent.setNumberOfActionsTaken(7);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after fifth episode");

        // checks criterion after sixth episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(5);
        agent.setNumberOfActionsTaken(8);
        assertTrue(performanceAchieved.isMet(training), "Agent has finally reached desired performance after sixth episode");
    }

    // tests if PerformanceAchieved criterion works correctly if agent doesn't perform well enough. Uses dynamic percentage value as tolerance.
    @Test
    void testPerformanceAchievedPercentageToleranceFalseAfterSixEpisodes() {
        // init place holder maze
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Maze maze = MazeUtils.getPlaceholderMaze(nodeFactory);

        // init Agent
        Agent agent = new Agent(maze.getStartNode(), null, 0.0d, 0.0d, 0.0d);

        // init Training
        Training training = new Training();
        training.setAgent(agent);
        training.setMaze(maze);

        // init criterion (tolerance = 15% = 1 action)
        int consideredEpisodes = 5;
        Double tolerancePercentage = 0.15d;
        PerformanceAchievedPercentageTolerance performanceAchieved = new PerformanceAchievedPercentageTolerance(consideredEpisodes, tolerancePercentage);

        // checks criterion after first episode (agent didn't reach desired quality this episode)
        agent.setNumberOfActionsTaken(9);
        training.setCurrentEpisodeNr(0);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after first episode");

        // checks criterion after second episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(1);
        agent.setNumberOfActionsTaken(8);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after second episode");

        // checks criterion after third episode (agent didn't reach desired quality this episode)
        training.setCurrentEpisodeNr(2);
        agent.setNumberOfActionsTaken(10);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after third episode");

        // checks criterion after fourth episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(3);
        agent.setNumberOfActionsTaken(8);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after fourth episode");

        // checks criterion after fifth episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(4);
        agent.setNumberOfActionsTaken(7);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after fifth episode");

        // checks criterion after sixth episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(5);
        agent.setNumberOfActionsTaken(8);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached the desired performance after sixth episode");
    }

    // tests if criterion also works if the successful episodes are not achieved consecutively. Uses percentage value as tolerance.
    @Test
    void testPerformanceAchievedPercentageToleranceTrueNotConsecutive() {
        // init place holder maze
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Maze maze = MazeUtils.getPlaceholderMaze(nodeFactory);

        // init Agent
        Agent agent = new Agent(maze.getStartNode(), null, 0.0d, 0.0d, 0.0d);

        // init Training
        Training training = new Training();
        training.setAgent(agent);
        training.setMaze(maze);

        // init criterion (tolerance = 15% = 1 action)
        int consideredEpisodes = 5;
        Double tolerancePercentage = 0.15d;
        PerformanceAchievedPercentageTolerance performanceAchieved = new PerformanceAchievedPercentageTolerance(consideredEpisodes, tolerancePercentage);

        // checks criterion after first episode (agent reached desired quality this episode)
        agent.setNumberOfActionsTaken(8);
        training.setCurrentEpisodeNr(0);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after first episode");

        // checks criterion after second episode (agent didn't reach desired quality this episode)
        training.setCurrentEpisodeNr(1);
        agent.setNumberOfActionsTaken(10);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after second episode");

        // checks criterion after first episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(2);
        agent.setNumberOfActionsTaken(8);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after third episode");

        // checks criterion after fourth episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(3);
        agent.setNumberOfActionsTaken(8);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after fourth episode");

        // checks criterion after fifth episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(4);
        agent.setNumberOfActionsTaken(7);
        assertFalse(performanceAchieved.isMet(training), "Agent has not reached desired performance after fifth episode");

        // checks criterion after sixth episode (agent reached desired quality this episode)
        training.setCurrentEpisodeNr(5);
        agent.setNumberOfActionsTaken(8);
        assertTrue(performanceAchieved.isMet(training), "Agent has finally reached desired performance after sixth episode");
    }

}
