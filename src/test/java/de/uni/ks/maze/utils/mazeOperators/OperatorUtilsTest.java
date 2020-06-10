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
package de.uni.ks.maze.utils.mazeOperators;

import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.NodeType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static de.uni.ks.TestUtils.getPlainMaze;
import static de.uni.ks.maze.utils.mazeOperators.OperatorUtils.isValidNewWay;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperatorUtilsTest {

    @Test
    void testIsValidNewWay() {
        testValidConfigs();
        testPredecessorAtWrongPositon();
        testPredecessorMissing();
        testNeighborIsPassable();
        testNodeIsAlreadyPassable();
        testDirectNeighborIsVisited();
        testDirectNeighborIsEndNode();
        testBorderNodes();
    }

    void testBorderNodes() {
        Maze m1 = getPlainMaze();
        NodeFactory.Node c1 = getCenter(m1);
        assertFalse(isValidNewWay(c1.getRightNeighbor().getRightNeighbor(),
                c1.getRightNeighbor(), Arrays.asList(c1, c1.getRightNeighbor()), m1));
    }

    void testDirectNeighborIsEndNode() {
        Maze m1 = getPlainMaze();
        NodeFactory.Node c1 = getCenter(m1);
        m1.setEndNode(c1.getUpperNeighbor());

        assertFalse(isValidNewWay(c1, c1.getRightNeighbor(), Arrays.asList(c1.getRightNeighbor()), m1));

        Maze m2 = getPlainMaze();
        NodeFactory.Node c2 = getCenter(m2);
        m2.setEndNode(c2.getUpperNeighbor());
        m2.getNodeFactory().changeNodeToType(c2.getRightNeighbor(), NodeType.PASSABLE);

        assertFalse(isValidNewWay(c2, c2.getRightNeighbor(), Arrays.asList(c2.getRightNeighbor()), m2));
    }

    void testDirectNeighborIsVisited() {
        Maze m1 = getPlainMaze();

        assertFalse(isValidNewWay(getCenter(m1), getCenter(m1).getRightNeighbor(), Arrays.asList(getCenter(m1), getCenter(m1).getUpperNeighbor()), m1));

        Maze m2 = getPlainMaze();
        m2.getNodeFactory().changeNodeToType(getCenter(m2).getRightNeighbor(), NodeType.PASSABLE);

        assertFalse(isValidNewWay(getCenter(m2), getCenter(m2).getRightNeighbor(), Arrays.asList(getCenter(m2), getCenter(m2).getUpperNeighbor()), m2));
    }

    void testNodeIsAlreadyPassable() {
        Maze m1 = getPlainMaze();
        m1.getNodeFactory().changeNodeToType(getCenter(m1), NodeType.PASSABLE);

        assertFalse(isValidNewWay(getCenter(m1), getCenter(m1).getRightNeighbor(), Arrays.asList(getCenter(m1).getRightNeighbor()), m1));

        Maze m2 = getPlainMaze();
        m2.getNodeFactory().changeNodeToType(getCenter(m2), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(getCenter(m2).getRightNeighbor(), NodeType.PASSABLE);

        assertFalse(isValidNewWay(getCenter(m2), getCenter(m2).getRightNeighbor(), Arrays.asList(getCenter(m2).getRightNeighbor()), m2));
    }

    void testNeighborIsPassable() {

        // Predecessor is not passable
        Maze m1 = getPlainMaze();
        m1.getNodeFactory().changeNodeToType(getCenter(m1).getLeftNeighbor(), NodeType.PASSABLE);

        assertFalse(isValidNewWay(getCenter(m1), getCenter(m1).getRightNeighbor(), Arrays.asList(getCenter(m1).getRightNeighbor()), m1));

        // Predecessor is passable
        Maze m2 = getPlainMaze();
        m2.getNodeFactory().changeNodeToType(getCenter(m2).getLeftNeighbor(), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(getCenter(m2).getRightNeighbor(), NodeType.PASSABLE);

        assertFalse(isValidNewWay(getCenter(m2), getCenter(m2).getRightNeighbor(), Arrays.asList(getCenter(m2).getRightNeighbor()), m2));
    }

    void testPredecessorMissing() {
        Maze m1 = getPlainMaze();

        assertFalse(isValidNewWay(getCenter(m1), null, new ArrayList<>(), m1));

        Maze m2 = getPlainMaze();
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(0, 0), NodeType.PASSABLE);

        assertFalse(isValidNewWay(getCenter(m2), null, new ArrayList<>(), m2));

        Maze m3 = getPlainMaze();
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(0, 1), NodeType.PASSABLE);

        assertFalse(isValidNewWay(getCenter(m3), null, new ArrayList<>(), m3));
    }

    void testPredecessorAtWrongPositon() {
        NodeFactory.Node placeholder = new NodeFactory(-0.05, 0, 1,
                1, 0, 0, 0, 0, 200)
                .buildWallNode();

        Maze m1 = getPlainMaze();

        assertFalse(isValidNewWay(getCenter(m1), placeholder, Arrays.asList(placeholder), m1));

        // Predecessor in corner
        Maze m2 = getPlainMaze();

        assertFalse(isValidNewWay(getCenter(m2), m2.getNodeAt(0, 0), Arrays.asList(m2.getNodeAt(0, 0)), m2));
    }

    void testValidConfigs() {
        Maze m1 = getPlainMaze();

        assertTrue(isValidNewWay(getCenter(m1), getCenter(m1).getRightNeighbor(),
                Arrays.asList(getCenter(m1).getRightNeighbor()),
                m1));

        Maze m2 = getPlainMaze();
        m2.getNodeFactory().changeNodeToType(getCenter(m2).getRightNeighbor(),
                NodeType.PASSABLE);

        assertTrue(isValidNewWay(getCenter(m2), getCenter(m2).getRightNeighbor(),
                Arrays.asList(getCenter(m2).getRightNeighbor()),
                m2));

        Maze m3 = getPlainMaze();
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(0, 0), NodeType.PASSABLE);

        assertTrue(isValidNewWay(getCenter(m3), getCenter(m3).getRightNeighbor(),
                Arrays.asList(getCenter(m3).getRightNeighbor()),
                m3));

        Maze m4 = getPlainMaze();
        m4.getNodeFactory().changeNodeToType(m4.getNodeAt(0, 0), NodeType.PASSABLE);

        m4.getNodeFactory().changeNodeToType(getCenter(m4).getRightNeighbor(),
                NodeType.PASSABLE);

        assertTrue(isValidNewWay(getCenter(m4), getCenter(m4).getRightNeighbor(),
                Arrays.asList(getCenter(m4).getRightNeighbor()),
                m4));

        Maze m5 = getPlainMaze();
        m5.setEndNode(m5.getNodeAt(0, 0));

        assertTrue(isValidNewWay(getCenter(m5), getCenter(m5).getRightNeighbor(),
                Arrays.asList(getCenter(m5).getRightNeighbor()),
                m5));

        Maze m6 = getPlainMaze();
        m6.setEndNode(m6.getNodeAt(0, 0));

        m6.getNodeFactory().changeNodeToType(getCenter(m6).getRightNeighbor(),
                NodeType.PASSABLE);

        assertTrue(isValidNewWay(getCenter(m6), getCenter(m6).getRightNeighbor(),
                Arrays.asList(getCenter(m6).getRightNeighbor()),
                m6));

        Maze m7 = getPlainMaze();

        assertTrue(isValidNewWay(getCenter(m7), getCenter(m7).getRightNeighbor(),
                Arrays.asList(getCenter(m7).getRightNeighbor(), getCenter(m7).getUpperLeftNeighbor()),
                m7));

        Maze m8 = getPlainMaze();

        m8.getNodeFactory().changeNodeToType(getCenter(m8).getRightNeighbor(),
                NodeType.PASSABLE);

        assertTrue(isValidNewWay(getCenter(m8), getCenter(m8).getRightNeighbor(),
                Arrays.asList(getCenter(m8).getRightNeighbor(), getCenter(m7).getUpperLeftNeighbor()),
                m8));
    }

    static NodeFactory.Node getCenter(Maze maze) {
        return maze.getNodeAt(2, 2);
    }
}
