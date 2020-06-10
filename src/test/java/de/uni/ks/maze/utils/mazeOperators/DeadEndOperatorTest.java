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

import java.util.Stack;

import static de.uni.ks.TestUtils.getPlainMaze;
import static org.junit.jupiter.api.Assertions.*;

class DeadEndOperatorTest {

    @Test
    void testStartDepthFirstSearch() {
        //  1. Path has no nodes if no way can be found
        Maze m2 = getPlainMaze();
        NodeFactory.Node c2 = getCenter(m2);
        m2.getNodeFactory().changeNodeToType(c2.getLowerLeftNeighbor(), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(c2.getLowerNeighbor(), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(c2.getLowerRightNeighbor(), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(c2.getRightNeighbor(), NodeType.PASSABLE);

        DeadEndOperator d2 = new DeadEndOperator(1, 99, 40,
                0.5, 123);
        Stack<NodeFactory.Node> p2 = d2.startDepthFirstSearch(c2.getLowerNeighbor(), m2, 99);

        assertEquals(0, p2.size());

        //  2. Path has maxPahtLen as lenght
        Maze m4 = getPlainMaze();
        NodeFactory.Node c4 = getCenter(m4);
        m4.getNodeFactory().changeNodeToType(c4.getLowerLeftNeighbor(), NodeType.PASSABLE);
        m4.getNodeFactory().changeNodeToType(c4.getLowerNeighbor(), NodeType.PASSABLE);
        m4.getNodeFactory().changeNodeToType(c4.getLowerRightNeighbor(), NodeType.PASSABLE);

        DeadEndOperator d4 = new DeadEndOperator(1, 3, 40,
                0.5, 123);
        Stack<NodeFactory.Node> p4 = d4.startDepthFirstSearch(c4.getLowerLeftNeighbor(), m4, 3);

        assertEquals(3, p4.size());

        //  4. path is connected
        assertTrue(p4.get(0).getDirectNeighbors().contains(p4.get(1)));
        assertTrue(p4.get(1).getDirectNeighbors().contains(p4.get(2)));
        assertFalse(p4.get(0).getDirectNeighbors().contains(p4.get(2)));

        //  3. paht has at least min len
        Maze m3 = getPlainMaze();
        NodeFactory.Node c3 = getCenter(m3);
        m3.getNodeFactory().changeNodeToType(c3.getLowerLeftNeighbor(), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(c3.getLowerNeighbor(), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(c3.getLowerRightNeighbor(), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(c3.getUpperRightNeighbor(), NodeType.PASSABLE);

        DeadEndOperator d3 = new DeadEndOperator(2, 5, 40,
                0.5, 123);
        Stack<NodeFactory.Node> p3 = d3.startDepthFirstSearch(c3.getLowerLeftNeighbor(), m3, 5);

        assertEquals(2, p3.size());

        //  5. Finds path of max len that is possible in maze
        Maze m1 = getPlainMaze();
        NodeFactory.Node c1 = getCenter(m1);
        m1.getNodeFactory().changeNodeToType(c1.getLowerLeftNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1.getLowerNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1.getLowerRightNeighbor(), NodeType.PASSABLE);

        DeadEndOperator d1 = new DeadEndOperator(1, 99, 40,
                0.5, 123);
        Stack<NodeFactory.Node> nodes = d1.startDepthFirstSearch(c1.getLowerLeftNeighbor(), m1, 99);

        assertEquals(4, nodes.size());

        // 6. Path len is 0 if min len is to high
        Maze m5 = getPlainMaze();
        NodeFactory.Node c5 = getCenter(m5);
        m5.getNodeFactory().changeNodeToType(c5.getLowerLeftNeighbor(), NodeType.PASSABLE);
        m5.getNodeFactory().changeNodeToType(c5.getLowerNeighbor(), NodeType.PASSABLE);
        m5.getNodeFactory().changeNodeToType(c5.getLowerRightNeighbor(), NodeType.PASSABLE);

        DeadEndOperator d5 = new DeadEndOperator(55, 99, 40,
                0.5, 123);
        Stack<NodeFactory.Node> p5 = d5.startDepthFirstSearch(c5.getLowerLeftNeighbor(), m5, 99);

        assertEquals(0, p5.size());
    }

    @Test
    void testEstimateCost() {
        Maze m1 = getPlainMaze();
        NodeFactory.Node c1 = getCenter(m1);
        m1.getNodeFactory().changeNodeToType(c1.getLowerLeftNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1.getLowerNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1.getLowerRightNeighbor(), NodeType.PASSABLE);

        m1.setStartNode(c1.getLowerLeftNeighbor());
        m1.setEndNode(c1.getLowerRightNeighbor());

        //  1. returns 0 if allowedCost are to low
        DeadEndOperator d1 = new DeadEndOperator(2, 5, 40, 0.5, 123);
        assertEquals(0, d1.estimateCost(m1, 0));

        //  2. returns 0 if path is to small
        DeadEndOperator d2 = new DeadEndOperator(6, 9, 40, 0.5, 123);
        assertEquals(0, d2.estimateCost(m1, 0));

        //  3. returns right cost for path
        int min3 = 2;
        int max3 = 4;
        DeadEndOperator d3 = new DeadEndOperator(min3, max3, 40, 0.5, 123);

        double cost = d3.estimateCost(m1, Double.POSITIVE_INFINITY);
        assertTrue(cost >= d3.getCostPerNode() * min3);
        assertTrue(cost <= d3.getCostPerNode() * max3);
    }

    @Test
    void testChangeMaze() {

        Maze m1 = getPlainMaze();
        NodeFactory.Node c1 = getCenter(m1);
        m1.getNodeFactory().changeNodeToType(c1.getLowerLeftNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1.getLowerNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1.getLowerRightNeighbor(), NodeType.PASSABLE);
        m1.setStartNode(c1.getLowerLeftNeighbor());
        m1.setEndNode(c1.getLowerRightNeighbor());

        //  1. Does nothing if estimate was not called first
        DeadEndOperator d1 = new DeadEndOperator(2, 5, 40, 0.5, 123);
        assertFalse(d1.changeMaze(m1));

        //  2. Does nothing if estimate found no path
        DeadEndOperator d2 = new DeadEndOperator(8, 99, 40, 0.5, 123);
        d2.estimateCost(m1, Double.POSITIVE_INFINITY);
        assertFalse(d2.changeMaze(m1));

        //  3. Changed the maze accordingly if a path was found
        DeadEndOperator d3 = new DeadEndOperator(2, 5, 40, 0.5, 123);
        d3.estimateCost(m1, Double.POSITIVE_INFINITY);

        int numOfPassableNodes = m1.getAllPassableNodes().size();

        int deadEndSize = d3.getDeadEnd().size();

        assertTrue(deadEndSize >= d3.getMinPathLen());
        assertTrue(deadEndSize <= d3.getMaxPathLen());
        assertTrue(d3.changeMaze(m1));
        assertNull(d3.getDeadEnd());

        assertEquals(numOfPassableNodes + deadEndSize, m1.getAllPassableNodes().size());
    }


    private NodeFactory.Node getCenter(Maze maze) {
        return maze.getNodeAt(2, 2);
    }
}
