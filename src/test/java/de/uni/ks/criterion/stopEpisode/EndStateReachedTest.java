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

import de.uni.ks.TestUtils;
import de.uni.ks.Training;
import de.uni.ks.agent.Agent;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndStateReachedTest {


    // tests if EndStateReached is true when the agent has reached the end state
    @Test
    void testEndStateReachedAgentReachedEndState() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node downRight = myMaze.getEndNode();

        // init Agent
        Agent agent = new Agent(downRight, null, 0.0d, 0.0d, 0.0d);

        // init Training
        Training training = new Training();
        training.setAgent(agent);
        training.setMaze(myMaze);

        // check if end state was reached
        assertTrue(new EndStateReached().isMet(training), "Agent has Exceeded end state");

    }

    // tests if EndStateReached is false when the agent hasn't reached the end state yet
    @Test
    void testEndStateReachedAgentHasNotReachedEndState() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node center = myMaze.getStartNode();

        // init Agent
        Agent agent = new Agent(center, null, 0.0d, 0.0d, 0.0d);

        // init Training
        Training training = new Training();
        training.setAgent(agent);
        training.setMaze(myMaze);

        // check if end state was not reached
        assertFalse(new EndStateReached().isMet(training), "agent has not Exceeded end state");
    }
}
