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
import de.uni.ks.agent.Agent;
import de.uni.ks.criterion.Criterion;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.utils.MazeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AgentExceedsOptimalPathPercentageTest {

    @Test
    void testWithTolerance() {
        // init place holder maze
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Maze maze = MazeUtils.getPlaceholderMaze(nodeFactory);

        // init Agent
        Agent agent = new Agent(maze.getStartNode(), null, 0.0d, 0.0d, 0.0d);

        // init Training
        Training training = new Training();
        training.setAgent(agent);
        training.setMaze(maze);

        int shortestPath = maze.getLengthOfShortestPath();

        // Allow 50% more actions.
        Criterion criterion = new AgentExceedsOptimalPathPercentage(0.5);

        // Agent took way to much actions.
        agent.setNumberOfActionsTaken(Integer.MAX_VALUE);
        Assertions.assertTrue(criterion.isMet(training));

        // Agent found shortest path
        agent.setNumberOfActionsTaken(shortestPath);
        Assertions.assertFalse(criterion.isMet(training));

        // Agent is in tolerance.
        agent.setNumberOfActionsTaken(shortestPath + shortestPath / 2);
        Assertions.assertFalse(criterion.isMet(training));

        // Agent exceeds tolerance,
        agent.setNumberOfActionsTaken(shortestPath + shortestPath / 2 + 1);
        Assertions.assertTrue(criterion.isMet(training));
    }

    @Test
    void testWithoutTolerance() {

        // init place holder maze
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Maze maze = MazeUtils.getPlaceholderMaze(nodeFactory);

        // init Agent
        Agent agent = new Agent(maze.getStartNode(), null, 0.0d, 0.0d, 0.0d);

        // init Training
        Training training = new Training();
        training.setAgent(agent);
        training.setMaze(maze);

        int shortestPath = maze.getLengthOfShortestPath();

        Criterion criterion = new AgentExceedsOptimalPathPercentage(0.0);

        // Agent found shortest Path.
        agent.setNumberOfActionsTaken(shortestPath);
        Assertions.assertFalse(criterion.isMet(training));

        // Agent took to much actions.
        agent.setNumberOfActionsTaken(shortestPath + 1);
        Assertions.assertTrue(criterion.isMet(training));
    }
}
