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
package de.uni.ks.agent;

import de.uni.ks.agent.explorationPolicies.GreedyPolicy;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.NodeFactory.Node;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static de.uni.ks.TestUtils.createMaze;
import static org.junit.jupiter.api.Assertions.*;


class AgentTest {

    // tests creation of actions when agent is surrounded by passable fields
    @Test
    void testCreateActionsOnlyPassableFields() {
        // init Nodes
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Node upperLeft = nodeFactory.buildWayNode();
        Node up = nodeFactory.buildWayNode();
        Node upperRight = nodeFactory.buildWayNode();
        Node center = nodeFactory.buildStartNode();
        Node left = nodeFactory.buildWayNode();
        Node right = nodeFactory.buildWayNode();
        Node downLeft = nodeFactory.buildWayNode();
        Node down = nodeFactory.buildWayNode();
        Node downRight = nodeFactory.buildWayNode();

        // create Maze
        createMaze(nodeFactory, upperLeft, up, upperRight, center, left, right, downLeft, down, downRight,
                center, null);

        // init Agent
        Double qTableInitValue = 0.5d;
        Agent agent = new Agent(center, null, 0.0d, 0.0d, qTableInitValue);

        // create actions
        ArrayList<Action> actions = agent.createActions(center);

        // check if actions were created correctly
        assertTrue(actions.contains(Action.UP), "actions contain up");
        assertTrue(actions.contains(Action.RIGHT), "actions contain right");
        assertTrue(actions.contains(Action.DOWN), "actions contain down");
        assertTrue(actions.contains(Action.LEFT), "actions contain left");
    }

    // tests creation of actions when agent is surrounded by passable and impassable fields
    @Test
    void testCreateActionsPassableAndNonPassableFields() {
        // init Nodes
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Node upperLeft = nodeFactory.buildWayNode();
        Node up = nodeFactory.buildWayNode();
        Node upperRight = nodeFactory.buildWayNode();
        Node center = nodeFactory.buildStartNode();
        Node left = nodeFactory.buildWallNode();
        Node right = nodeFactory.buildWallNode();
        Node downLeft = nodeFactory.buildWayNode();
        Node down = nodeFactory.buildWayNode();
        Node downRight = nodeFactory.buildWayNode();

        // create Maze
        createMaze(nodeFactory, upperLeft, up, upperRight, center, left, right, downLeft, down, downRight,
                center, null);

        // init Agent
        Double qTableInitValue = 0.5;
        Agent agent = new Agent(center, null, 0.0d, 0.0d, qTableInitValue);

        // create actions
        ArrayList<Action> actions = agent.createActions(center);// check if actions were created correctly
        assertTrue(actions.contains(Action.UP), "actions contain up");
        assertFalse(actions.contains(Action.RIGHT), "actions don't contain right");
        assertTrue(actions.contains(Action.DOWN), "actions contain down");
        assertFalse(actions.contains(Action.LEFT), "actions don't contain left");

    }

    // tests creation of actions when agent is in a corner of the maze
    @Test
    void testCreateActionsCorner() {
        // init Nodes
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Node upperLeft = nodeFactory.buildWayNode();
        Node up = nodeFactory.buildWayNode();
        Node upperRight = nodeFactory.buildStartNode();
        Node center = nodeFactory.buildWayNode();
        Node left = nodeFactory.buildWayNode();
        Node right = nodeFactory.buildWayNode();
        Node downLeft = nodeFactory.buildWayNode();
        Node down = nodeFactory.buildWayNode();
        Node downRight = nodeFactory.buildWayNode();

        // create Maze
        createMaze(nodeFactory, upperLeft, up, upperRight, center, left, right, downLeft, down, downRight,
                center, null);

        // init Agent
        Double qTableInitValue = 0.5;
        Agent agent = new Agent(upperRight, null, 0.0d, 0.0d, qTableInitValue);

        // create actions
        ArrayList<Action> actions = agent.createActions(upperRight);// check if actions were created correctly
        assertFalse(actions.contains(Action.UP), "actions don't contain up");
        assertFalse(actions.contains(Action.RIGHT), "actions don't contain right");
        assertTrue(actions.contains(Action.DOWN), "actions contain down");
        assertTrue(actions.contains(Action.LEFT), "actions contain left");
    }

    // tests movement of agent
    @Test
    void testMoveAgent() {
        // init Nodes
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Node upperLeft = nodeFactory.buildWayNode();
        Node up = nodeFactory.buildWayNode();
        Node upperRight = nodeFactory.buildWayNode();
        Node center = nodeFactory.buildStartNode();
        Node left = nodeFactory.buildWallNode();
        Node right = nodeFactory.buildWallNode();
        Node downLeft = nodeFactory.buildWayNode();
        Node down = nodeFactory.buildWayNode();
        Node downRight = nodeFactory.buildWayNode();

        // create Maze
        createMaze(nodeFactory, upperLeft, up, upperRight, center, left, right, downLeft, down, downRight,
                center, null);

        // init Agent
        Agent agent = new Agent(center, null, 0.0d, 0.0d, 0.0d);

        // check if agent moved correctly
        agent.moveAgent(Action.UP);
        assertEquals(center.getUpperNeighbor(), agent.getCurrentPosition(), "agent moved up");

        agent.moveAgent(Action.RIGHT);
        assertEquals(center.getUpperNeighbor().getRightNeighbor(), agent.getCurrentPosition(), "agent moved right");

        agent.moveAgent(Action.DOWN);
        assertEquals(center.getUpperNeighbor().getRightNeighbor().getLowerNeighbor(), agent.getCurrentPosition(), "agent moved down");

        agent.moveAgent(Action.LEFT);
        assertEquals(center.getUpperNeighbor().getRightNeighbor().getLowerNeighbor().getLeftNeighbor(), agent.getCurrentPosition(), "agent moved left");

    }

    // tests q-learning algorithm and do action() method by performing 3 actions in maze with 2 passable fields
    @Test
    void testDoAction() {

        // set rewards
        double rewardCenter = -4d;
        double rewardRight = -5d;

        // init Nodes
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Node upperLeft = nodeFactory.buildWallNode();
        Node up = nodeFactory.buildWallNode();
        Node upperRight = nodeFactory.buildWallNode();
        Node center = nodeFactory.buildStartNode(rewardCenter);
        Node left = nodeFactory.buildWallNode();
        Node right = nodeFactory.buildWayNode(rewardRight);
        Node downLeft = nodeFactory.buildWallNode();
        Node down = nodeFactory.buildWallNode();
        Node downRight = nodeFactory.buildWallNode();

        // create Maze
        createMaze(nodeFactory, upperLeft, up, upperRight, center, left, right, downLeft, down, downRight,
                center, null);

        // init Agent
        int greedySeed = 12345;
        double qTableInitValue = 0.0;
        double qLearningAlpha = 1.0;
        double qLearningGamma = 1.0;
        Agent agent = new Agent(center, new GreedyPolicy(greedySeed), qLearningAlpha, qLearningGamma, qTableInitValue);

        // do first action
        agent.doAction();

        // check values after first action
        assertEquals(right, agent.getCurrentPosition(), "agent moved right");
        double firstUpdatedQValue = (0 + qLearningAlpha * (rewardRight + qLearningGamma * 0 - 0));
        assertEquals(firstUpdatedQValue, agent.getQTable().getQValue(center, Action.RIGHT), "Q-Value was updated correctly after first action");
        assertEquals(1, agent.getNumberOfActionsTaken(), "num of actions was increased correctly after first move");
        assertEquals(rewardRight, agent.getTotalReward(), "first reward was added to sum reward");

        // do second action
        agent.doAction();

        // check values after second action
        assertEquals(center, agent.getCurrentPosition(), "agent moved left");
        double secondUpdatedQValue = 0 + qLearningAlpha * (rewardCenter + qLearningGamma * firstUpdatedQValue - 0);
        assertEquals(secondUpdatedQValue, agent.getQTable().getQValue(right, Action.LEFT), "Q-Value was updated correctly after second action");
        assertEquals(2, agent.getNumberOfActionsTaken(), "num of actions was increased correctly after second move");
        assertEquals(rewardRight + rewardCenter, agent.getTotalReward(), "second reward was added to sum reward");

        // do third action
        agent.doAction();

        // check values after third action
        assertEquals(right, agent.getCurrentPosition(), "agent moved right again");
        double thirdUpdatedQValue = firstUpdatedQValue + qLearningAlpha * (rewardRight + qLearningGamma * secondUpdatedQValue - firstUpdatedQValue);
        assertEquals(thirdUpdatedQValue, agent.getQTable().getQValue(center, Action.RIGHT), "Q-Value was updated correctly after third action");
        assertEquals(3, agent.getNumberOfActionsTaken(), "num of actions was increased correctly after third move");
        assertEquals(2 * rewardRight + rewardCenter, agent.getTotalReward(), "third reward was added to sum reward");
    }
}
