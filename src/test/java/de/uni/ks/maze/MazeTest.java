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
package de.uni.ks.maze;


import de.uni.ks.maze.NodeFactory.Node;
import org.junit.jupiter.api.Test;

import static de.uni.ks.TestUtils.createMaze;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MazeTest {

    @Test
    void testGetNodeAt() {
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
        Node downRight = nodeFactory.buildEndNode();

        // create Maze
        Maze myMaze = createMaze(nodeFactory, upperLeft, up, upperRight, center, left, right, downLeft, down, downRight,
                center, downRight);

        // Assert that all nodes are at the right position.
        assertEquals(upperLeft, myMaze.getNodeAt(0, 0));
        assertEquals(up, myMaze.getNodeAt(0, 1));
        assertEquals(upperRight, myMaze.getNodeAt(0, 2));
        assertEquals(left, myMaze.getNodeAt(1, 0));
        assertEquals(center, myMaze.getNodeAt(1, 1));
        assertEquals(right, myMaze.getNodeAt(1, 2));
        assertEquals(downLeft, myMaze.getNodeAt(2, 0));
        assertEquals(down, myMaze.getNodeAt(2, 1));
        assertEquals(downRight, myMaze.getNodeAt(2, 2));

        // Assert that null gets returned for indices outside of the dimensions.
        assertNull(myMaze.getNodeAt(-1, 0));
        assertNull(myMaze.getNodeAt(-1, -1));
        assertNull(myMaze.getNodeAt(0, -1));

        assertNull(myMaze.getNodeAt(3, 0));
        assertNull(myMaze.getNodeAt(3, 3));
        assertNull(myMaze.getNodeAt(3, 3));

    }

    @Test
    void testInitNodes() {

        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Node n1 = nodeFactory.buildWayNode();
        Node n2 = nodeFactory.buildWayNode();
        Node n3 = nodeFactory.buildWayNode();
        Node n4 = nodeFactory.buildWayNode();

        Node[][] mazeArr = new Node[2][2];
        mazeArr[0][0] = n1;
        mazeArr[1][0] = n2;
        mazeArr[0][1] = n3;
        mazeArr[1][1] = n4;

        Maze maze = new Maze(nodeFactory, mazeArr, null, null);

        // Assert Maze -- Node associations.
        for (int x = 0; x < mazeArr.length; x++) {
            for (int y = 0; y < mazeArr[0].length; y++) {
                assertEquals(maze, mazeArr[x][y].getMaze());
            }
        }

        // Assert Positions for Nodes.
        for (int x = 0; x < mazeArr.length; x++) {
            for (int y = 0; y < mazeArr[0].length; y++) {
                Node node = maze.getNodeAt(x, y);
                assertEquals(x, node.getXPos());
                assertEquals(y, node.getYPos());
            }
        }

    }

    @Test
    void testFindStartAndEndNodes() {

        // init Nodes
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Node upperLeft = nodeFactory.buildWayNode();
        Node up = nodeFactory.buildWayNode();
        Node upperRight = nodeFactory.buildEndNode();
        Node center = nodeFactory.buildWallNode();
        Node left = nodeFactory.buildWayNode();
        Node right = nodeFactory.buildStartNode();
        Node downLeft = nodeFactory.buildWayNode();
        Node down = nodeFactory.buildWallNode();
        Node downRight = nodeFactory.buildWayNode();

        // create Maze
        Maze myMaze = createMaze(nodeFactory, upperLeft, up, upperRight, center, left, right, downLeft, down, downRight,
                right, upperRight);

        // Assert end nodes and start nodes are found.
        assertEquals(myMaze.getStartNode(), right);
        assertEquals(myMaze.getEndNode(), upperRight);
    }

    @Test
    void testAutomaticUpdates() {

        double actionReward = -0.05;
        NodeFactory nodeFactory = new NodeFactory(actionReward, 0, 1, 1, 0, 0, 0, 0, 200);
        Node[][] mazeArr = new Node[2][2];
        mazeArr[0][0] = nodeFactory.buildWayNode();
        mazeArr[0][1] = nodeFactory.buildWayNode();
        Node endNode = nodeFactory.buildEndNode();
        mazeArr[1][0] = endNode;
        mazeArr[1][1] = nodeFactory.buildWallNode();

        Maze maze = new Maze(nodeFactory, mazeArr, null, endNode);

        assertNull(maze.getStartNode());
        maze.setStartNode(maze.getNodeAt(0, 0));

        Node shouldBeStart = maze.getNodeAt(0, 0);
        assertEquals(shouldBeStart, maze.getStartNode());
        assertEquals(NodeType.PASSABLE, shouldBeStart.getNodeType());
        assertEquals(actionReward, shouldBeStart.getReward());

        // Assert the reward and type get changed automatically:
        // Before
        Node n1 = maze.getNodeAt(1, 1);
        assertEquals(NodeType.IMPASSABLE, n1.getNodeType());
        assertEquals(NodeFactory.IMPASSABLE_REWARD, n1.getReward());

        // After
        maze.setStartNode(n1);

        assertEquals(NodeType.PASSABLE, n1.getNodeType());
        assertEquals(actionReward, n1.getReward());

        assertEquals(n1, maze.getStartNode());
    }
}
