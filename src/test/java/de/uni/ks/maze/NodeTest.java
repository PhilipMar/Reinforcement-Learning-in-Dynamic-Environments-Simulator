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

class NodeTest {

    @Test
    void testGetXYNeighborMethods() {
        // init Nodes
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Node upperLeft = nodeFactory.buildWayNode();
        Node up = nodeFactory.buildWayNode();
        Node upperRight = nodeFactory.buildEndNode();
        Node center = nodeFactory.buildWayNode();
        Node left = nodeFactory.buildWayNode();
        Node right = nodeFactory.buildStartNode();
        Node downLeft = nodeFactory.buildWayNode();
        Node down = nodeFactory.buildWayNode();
        Node downRight = nodeFactory.buildWayNode();

        // create Maze
        createMaze(nodeFactory, upperLeft, up, upperRight, center, left, right, downLeft, down, downRight,
                right, upperRight);

        assertEquals(center.getUpperLeftNeighbor(), upperLeft);
        assertEquals(center.getUpperNeighbor(), up);
        assertEquals(center.getUpperRightNeighbor(), upperRight);
        assertEquals(center.getLeftNeighbor(), left);
        assertEquals(center.getRightNeighbor(), right);
        assertEquals(center.getLowerLeftNeighbor(), downLeft);
        assertEquals(center.getLowerNeighbor(), down);
        assertEquals(center.getLowerRightNeighbor(), downRight);

    }

}
