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

import de.uni.ks.TestUtils;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.NodeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResizeOperatorTest {

    @Test
    void testIncreaseXDimensionByOne() {
        // create simple 3x5 maze
        Maze maze = TestUtils.getSimpleHorizontalMaze();

        // save nodes of the 0th row
        NodeFactory.Node oldNodeX0Y0 = maze.getNodeAt(0, 0);
        NodeFactory.Node oldNodeX0Y1 = maze.getNodeAt(0, 1);
        NodeFactory.Node oldNodeX0Y2 = maze.getNodeAt(0, 2);
        NodeFactory.Node oldNodeX0Y3 = maze.getNodeAt(0, 3);
        NodeFactory.Node oldNodeX0Y4 = maze.getNodeAt(0, 4);

        // save nodes of the 1th row
        NodeFactory.Node oldNodeX1Y0 = maze.getNodeAt(1, 0);
        NodeFactory.Node oldNodeX1Y1 = maze.getNodeAt(1, 1); // start node
        NodeFactory.Node oldNodeX1Y2 = maze.getNodeAt(1, 2);
        NodeFactory.Node oldNodeX1Y3 = maze.getNodeAt(1, 3); // old end node
        NodeFactory.Node oldNodeX1Y4 = maze.getNodeAt(1, 4);

        // save nodes of the 2th row
        NodeFactory.Node oldNodeX2Y0 = maze.getNodeAt(2, 0);
        NodeFactory.Node oldNodeX2Y1 = maze.getNodeAt(2, 1);
        NodeFactory.Node oldNodeX2Y2 = maze.getNodeAt(2, 2);
        NodeFactory.Node oldNodeX2Y3 = maze.getNodeAt(2, 3); // new end node
        NodeFactory.Node oldNodeX2Y4 = maze.getNodeAt(2, 4);

        // use resize Operator
        ResizeOperator resizeOperator = new ResizeOperator(100, 123);
        resizeOperator.setxIncreasementValue(1);
        resizeOperator.setyIncreasementValue(0);
        resizeOperator.changeMaze(maze);

        // check if maze x dimension has been Increased by one
        Assertions.assertEquals(4, maze.getMaze().length);

        // check if maze y dimension is still the same
        Assertions.assertEquals(5, maze.getMaze()[0].length);

        // check if nodes of old maze haven't been replaced by new ones
        assertNodesAreEqual(maze, oldNodeX0Y0, oldNodeX0Y1, oldNodeX0Y2, oldNodeX0Y3,
                oldNodeX0Y4, oldNodeX1Y0, oldNodeX1Y1, oldNodeX1Y2, oldNodeX1Y3,
                oldNodeX1Y4, oldNodeX2Y0, oldNodeX2Y1, oldNodeX2Y2, oldNodeX2Y3, oldNodeX2Y4);

        // check if former wall node below old end node is now the new end node
        assertNodeIsEndNode(maze, oldNodeX2Y3);

        // check if remaining nodes below the old end node are still wall nodes
        assertNodeIsWallNode(maze, oldNodeX2Y0);
        assertNodeIsWallNode(maze, oldNodeX2Y2);
        assertNodeIsWallNode(maze, oldNodeX2Y2);
        assertNodeIsWallNode(maze, oldNodeX2Y4);

        // save new nodes of new row (3th)
        NodeFactory.Node newNodeX3Y0 = maze.getNodeAt(3, 0);
        NodeFactory.Node newNodeX3Y1 = maze.getNodeAt(3, 1);
        NodeFactory.Node newNodeX3Y2 = maze.getNodeAt(3, 2);
        NodeFactory.Node newNodeX3Y3 = maze.getNodeAt(3, 3);
        NodeFactory.Node newNodeX3Y4 = maze.getNodeAt(3, 4);

        // check if all new nodes are wall nodes
        assertNodeIsWallNode(maze, newNodeX3Y0);
        assertNodeIsWallNode(maze, newNodeX3Y1);
        assertNodeIsWallNode(maze, newNodeX3Y2);
        assertNodeIsWallNode(maze, newNodeX3Y3);
        assertNodeIsWallNode(maze, newNodeX3Y4);
    }

    @Test
    void testIncreaseXDimensionByTwo() {
        // create simple 3x5 maze
        Maze maze = TestUtils.getSimpleHorizontalMaze();

        // save nodes of the 0th row
        NodeFactory.Node oldNodeX0Y0 = maze.getNodeAt(0, 0);
        NodeFactory.Node oldNodeX0Y1 = maze.getNodeAt(0, 1);
        NodeFactory.Node oldNodeX0Y2 = maze.getNodeAt(0, 2);
        NodeFactory.Node oldNodeX0Y3 = maze.getNodeAt(0, 3);
        NodeFactory.Node oldNodeX0Y4 = maze.getNodeAt(0, 4);

        // save nodes of the 1th row
        NodeFactory.Node oldNodeX1Y0 = maze.getNodeAt(1, 0);
        NodeFactory.Node oldNodeX1Y1 = maze.getNodeAt(1, 1); // start node
        NodeFactory.Node oldNodeX1Y2 = maze.getNodeAt(1, 2);
        NodeFactory.Node oldNodeX1Y3 = maze.getNodeAt(1, 3); // old end node
        NodeFactory.Node oldNodeX1Y4 = maze.getNodeAt(1, 4);

        // save nodes of the 2th row
        NodeFactory.Node oldNodeX2Y0 = maze.getNodeAt(2, 0);
        NodeFactory.Node oldNodeX2Y1 = maze.getNodeAt(2, 1);
        NodeFactory.Node oldNodeX2Y2 = maze.getNodeAt(2, 2);
        NodeFactory.Node oldNodeX2Y3 = maze.getNodeAt(2, 3);
        NodeFactory.Node oldNodeX2Y4 = maze.getNodeAt(2, 4);

        // use resize Operator
        ResizeOperator resizeOperator = new ResizeOperator(100, 123);
        resizeOperator.setxIncreasementValue(2);
        resizeOperator.setyIncreasementValue(0);
        resizeOperator.changeMaze(maze);

        // check if maze x dimension has been Increased by two
        Assertions.assertEquals(5, maze.getMaze().length);

        // check if maze y dimension is still the same
        Assertions.assertEquals(5, maze.getMaze()[0].length);

        // check if nodes of old maze haven't been replaced by new ones
        assertNodesAreEqual(maze, oldNodeX0Y0, oldNodeX0Y1, oldNodeX0Y2, oldNodeX0Y3,
                oldNodeX0Y4, oldNodeX1Y0, oldNodeX1Y1, oldNodeX1Y2, oldNodeX1Y3,
                oldNodeX1Y4, oldNodeX2Y0, oldNodeX2Y1, oldNodeX2Y2, oldNodeX2Y3, oldNodeX2Y4);

        // check if former wall node below old end node is now a way node
        assertNodeIsWayNode(maze, oldNodeX2Y3);

        // check if other nodes in this row are still wall nodes
        assertNodeIsWallNode(maze, oldNodeX2Y0);
        assertNodeIsWallNode(maze, oldNodeX2Y1);
        assertNodeIsWallNode(maze, oldNodeX2Y2);
        assertNodeIsWallNode(maze, oldNodeX2Y4);

        // save new nodes of new row (3th)
        NodeFactory.Node newNodeX3Y0 = maze.getNodeAt(3, 0);
        NodeFactory.Node newNodeX3Y1 = maze.getNodeAt(3, 1);
        NodeFactory.Node newNodeX3Y2 = maze.getNodeAt(3, 2);
        NodeFactory.Node newNodeX3Y3 = maze.getNodeAt(3, 3); // new end node
        NodeFactory.Node newNodeX3Y4 = maze.getNodeAt(3, 4);

        // check if newly created node two nodes below old end nod is now the new end node
        assertNodeIsEndNode(maze, newNodeX3Y3);

        // check if the remaining nodes left and right to the new end node are walls
        assertNodeIsWallNode(maze, newNodeX3Y0);
        assertNodeIsWallNode(maze, newNodeX3Y1);
        assertNodeIsWallNode(maze, newNodeX3Y2);
        assertNodeIsWallNode(maze, newNodeX3Y4);

        // save new nodes of new row (4th)
        NodeFactory.Node newNodeX4Y0 = maze.getNodeAt(4, 0);
        NodeFactory.Node newNodeX4Y1 = maze.getNodeAt(4, 1);
        NodeFactory.Node newNodeX4Y2 = maze.getNodeAt(4, 2);
        NodeFactory.Node newNodeX4Y3 = maze.getNodeAt(4, 3);
        NodeFactory.Node newNodeX4Y4 = maze.getNodeAt(4, 4);

        // check if the remaining nodes on the bottom of the maze are wall nodes
        assertNodeIsWallNode(maze, newNodeX4Y0);
        assertNodeIsWallNode(maze, newNodeX4Y1);
        assertNodeIsWallNode(maze, newNodeX4Y2);
        assertNodeIsWallNode(maze, newNodeX4Y3);
        assertNodeIsWallNode(maze, newNodeX4Y4);
    }

    @Test
    void testIncreaseYDimensionByOne() {
        // create simple 3x5 maze
        Maze maze = TestUtils.getSimpleHorizontalMaze();

        // save nodes of the 0th row
        NodeFactory.Node oldNodeX0Y0 = maze.getNodeAt(0, 0);
        NodeFactory.Node oldNodeX0Y1 = maze.getNodeAt(0, 1);
        NodeFactory.Node oldNodeX0Y2 = maze.getNodeAt(0, 2);
        NodeFactory.Node oldNodeX0Y3 = maze.getNodeAt(0, 3);
        NodeFactory.Node oldNodeX0Y4 = maze.getNodeAt(0, 4);

        // save nodes of the 1th row
        NodeFactory.Node oldNodeX1Y0 = maze.getNodeAt(1, 0);
        NodeFactory.Node oldNodeX1Y1 = maze.getNodeAt(1, 1); // start node
        NodeFactory.Node oldNodeX1Y2 = maze.getNodeAt(1, 2);
        NodeFactory.Node oldNodeX1Y3 = maze.getNodeAt(1, 3); // old end node
        NodeFactory.Node oldNodeX1Y4 = maze.getNodeAt(1, 4); // new end node

        // save nodes of the 2th row
        NodeFactory.Node oldNodeX2Y0 = maze.getNodeAt(2, 0);
        NodeFactory.Node oldNodeX2Y1 = maze.getNodeAt(2, 1);
        NodeFactory.Node oldNodeX2Y2 = maze.getNodeAt(2, 2);
        NodeFactory.Node oldNodeX2Y3 = maze.getNodeAt(2, 3);
        NodeFactory.Node oldNodeX2Y4 = maze.getNodeAt(2, 4);

        // use resize Operator
        ResizeOperator resizeOperator = new ResizeOperator(100, 123);
        resizeOperator.setxIncreasementValue(0);
        resizeOperator.setyIncreasementValue(1);
        resizeOperator.changeMaze(maze);

        // check if maze x dimension is still the same
        Assertions.assertEquals(3, maze.getMaze().length);

        // check if maze y dimension has been Increased by one
        Assertions.assertEquals(6, maze.getMaze()[0].length);

        // check if nodes of old maze haven't been replaced by new ones
        assertNodesAreEqual(maze, oldNodeX0Y0, oldNodeX0Y1, oldNodeX0Y2, oldNodeX0Y3,
                oldNodeX0Y4, oldNodeX1Y0, oldNodeX1Y1, oldNodeX1Y2, oldNodeX1Y3,
                oldNodeX1Y4, oldNodeX2Y0, oldNodeX2Y1, oldNodeX2Y2, oldNodeX2Y3, oldNodeX2Y4);

        // check if former wall node right to the old end node (4th column) is now the new end node
        assertNodeIsEndNode(maze, oldNodeX1Y4);

        // check if remaining nodes of the 4th column are still wall nodes
        assertNodeIsWallNode(maze, oldNodeX0Y4);
        assertNodeIsWallNode(maze, oldNodeX2Y4);

        // save new nodes of new column (5th)
        NodeFactory.Node newNodeX0Y5 = maze.getNodeAt(0, 5);
        NodeFactory.Node newNodeX1Y5 = maze.getNodeAt(1, 5);
        NodeFactory.Node newNodeX2Y5 = maze.getNodeAt(2, 5);

        // check if all new nodes are wall nodes
        assertNodeIsWallNode(maze, newNodeX0Y5);
        assertNodeIsWallNode(maze, newNodeX1Y5);
        assertNodeIsWallNode(maze, newNodeX2Y5);
    }

    @Test
    void testIncreaseYDimensionByTwo() {
        // create simple 3x5 maze
        Maze maze = TestUtils.getSimpleHorizontalMaze();

        // save nodes of the 0th row
        NodeFactory.Node oldNodeX0Y0 = maze.getNodeAt(0, 0);
        NodeFactory.Node oldNodeX0Y1 = maze.getNodeAt(0, 1);
        NodeFactory.Node oldNodeX0Y2 = maze.getNodeAt(0, 2);
        NodeFactory.Node oldNodeX0Y3 = maze.getNodeAt(0, 3);
        NodeFactory.Node oldNodeX0Y4 = maze.getNodeAt(0, 4);

        // save nodes of the 1th row
        NodeFactory.Node oldNodeX1Y0 = maze.getNodeAt(1, 0);
        NodeFactory.Node oldNodeX1Y1 = maze.getNodeAt(1, 1); // start node
        NodeFactory.Node oldNodeX1Y2 = maze.getNodeAt(1, 2);
        NodeFactory.Node oldNodeX1Y3 = maze.getNodeAt(1, 3); // old end node
        NodeFactory.Node oldNodeX1Y4 = maze.getNodeAt(1, 4);

        // save nodes of the 2th row
        NodeFactory.Node oldNodeX2Y0 = maze.getNodeAt(2, 0);
        NodeFactory.Node oldNodeX2Y1 = maze.getNodeAt(2, 1);
        NodeFactory.Node oldNodeX2Y2 = maze.getNodeAt(2, 2);
        NodeFactory.Node oldNodeX2Y3 = maze.getNodeAt(2, 3);
        NodeFactory.Node oldNodeX2Y4 = maze.getNodeAt(2, 4);

        // use resize Operator
        ResizeOperator resizeOperator = new ResizeOperator(100, 123);
        resizeOperator.setxIncreasementValue(0);
        resizeOperator.setyIncreasementValue(2);
        resizeOperator.changeMaze(maze);

        // check if maze x dimension is still the same
        Assertions.assertEquals(3, maze.getMaze().length);

        // check if maze y dimension has been Increased by two
        Assertions.assertEquals(7, maze.getMaze()[0].length);

        // check if nodes of old maze haven't been replaced by new ones
        assertNodesAreEqual(maze, oldNodeX0Y0, oldNodeX0Y1, oldNodeX0Y2, oldNodeX0Y3,
                oldNodeX0Y4, oldNodeX1Y0, oldNodeX1Y1, oldNodeX1Y2, oldNodeX1Y3,
                oldNodeX1Y4, oldNodeX2Y0, oldNodeX2Y1, oldNodeX2Y2, oldNodeX2Y3, oldNodeX2Y4);

        // check if former wall node right to the old end node has changed to way node
        assertNodeIsWayNode(maze, oldNodeX1Y4);

        // check if remaining nodes of 4th column are still wall nodes
        assertNodeIsWallNode(maze, oldNodeX0Y4);
        assertNodeIsWallNode(maze, oldNodeX2Y4);

        // save new nodes of new column (5th)
        NodeFactory.Node newNodeX0Y5 = maze.getNodeAt(0, 5);
        NodeFactory.Node newNodeX1Y5 = maze.getNodeAt(1, 5); // new end node
        NodeFactory.Node newNodeX2Y5 = maze.getNodeAt(2, 5);

        // check if new end node is in 5th column
        assertNodeIsEndNode(maze, newNodeX1Y5);

        // check if remaining new nodes in 5th column are walls
        assertNodeIsWallNode(maze, newNodeX0Y5);
        assertNodeIsWallNode(maze, newNodeX2Y5);

        // save new nodes of new column (6th)
        NodeFactory.Node newNodeX0Y6 = maze.getNodeAt(0, 6);
        NodeFactory.Node newNodeX1Y6 = maze.getNodeAt(1, 6);
        NodeFactory.Node newNodeX2Y6 = maze.getNodeAt(2, 6);

        // check if all new nodes of the 6th column are wall nodes
        assertNodeIsWallNode(maze, newNodeX0Y6);
        assertNodeIsWallNode(maze, newNodeX1Y6);
        assertNodeIsWallNode(maze, newNodeX2Y6);
    }

    @Test
    void testIncreaseXDimensionByOneAndYDimensionByOne() {
        // create simple 3x5 maze
        Maze maze = TestUtils.getSimpleHorizontalMaze();

        // save old nodes of the 0th row
        NodeFactory.Node oldNodeX0Y0 = maze.getNodeAt(0, 0);
        NodeFactory.Node oldNodeX0Y1 = maze.getNodeAt(0, 1);
        NodeFactory.Node oldNodeX0Y2 = maze.getNodeAt(0, 2);
        NodeFactory.Node oldNodeX0Y3 = maze.getNodeAt(0, 3);
        NodeFactory.Node oldNodeX0Y4 = maze.getNodeAt(0, 4);

        // save old nodes of the 1th row
        NodeFactory.Node oldNodeX1Y0 = maze.getNodeAt(1, 0);
        NodeFactory.Node oldNodeX1Y1 = maze.getNodeAt(1, 1); // start node
        NodeFactory.Node oldNodeX1Y2 = maze.getNodeAt(1, 2);
        NodeFactory.Node oldNodeX1Y3 = maze.getNodeAt(1, 3); // old end node
        NodeFactory.Node oldNodeX1Y4 = maze.getNodeAt(1, 4);

        // save old nodes of the 2th row
        NodeFactory.Node oldNodeX2Y0 = maze.getNodeAt(2, 0);
        NodeFactory.Node oldNodeX2Y1 = maze.getNodeAt(2, 1);
        NodeFactory.Node oldNodeX2Y2 = maze.getNodeAt(2, 2);
        NodeFactory.Node oldNodeX2Y3 = maze.getNodeAt(2, 3);
        NodeFactory.Node oldNodeX2Y4 = maze.getNodeAt(2, 4); // new end node

        // use resize Operator
        ResizeOperator resizeOperator = new ResizeOperator(100, 123);
        resizeOperator.setxIncreasementValue(1);
        resizeOperator.setyIncreasementValue(1);
        resizeOperator.changeMaze(maze);

        // check if maze x dimension has been Increased by one
        Assertions.assertEquals(4, maze.getMaze().length);

        // check if maze y dimension has been Increased by one
        Assertions.assertEquals(6, maze.getMaze()[0].length);

        // check if nodes of old maze haven't been replaced by new ones
        assertNodesAreEqual(maze, oldNodeX0Y0, oldNodeX0Y1, oldNodeX0Y2, oldNodeX0Y3,
                oldNodeX0Y4, oldNodeX1Y0, oldNodeX1Y1, oldNodeX1Y2, oldNodeX1Y3,
                oldNodeX1Y4, oldNodeX2Y0, oldNodeX2Y1, oldNodeX2Y2, oldNodeX2Y3, oldNodeX2Y4);

        // check if former wall node right to the old end node has changed to way node
        assertNodeIsWayNode(maze, oldNodeX1Y4);

        // check if node below the new way node (4th column 2th row) is now the new end node
        assertNodeIsEndNode(maze, oldNodeX2Y4);

        // check if the remaining already existing node in the 4th column is still a wall node
        assertNodeIsWallNode(maze, oldNodeX0Y4);

        // save new nodes of new row (3th)
        NodeFactory.Node newNodeX3Y0 = maze.getNodeAt(3, 0);
        NodeFactory.Node newNodeX3Y1 = maze.getNodeAt(3, 1);
        NodeFactory.Node newNodeX3Y2 = maze.getNodeAt(3, 2);
        NodeFactory.Node newNodeX3Y3 = maze.getNodeAt(3, 3);
        NodeFactory.Node newNodeX3Y4 = maze.getNodeAt(3, 4);
        NodeFactory.Node newNodeX3Y5 = maze.getNodeAt(3, 5);

        // save new nodes of new column (5th)
        NodeFactory.Node newNodeX0Y5 = maze.getNodeAt(0, 5);
        NodeFactory.Node newNodeX1Y5 = maze.getNodeAt(1, 5);
        NodeFactory.Node newNodeX2Y5 = maze.getNodeAt(2, 5);
        // NodeFactory.Node newNodeX3Y5 = maze.getNodeAt(3, 5); // already declared

        // check if all nodes in 3th row are wall nodes
        assertNodeIsWallNode(maze, newNodeX3Y0);
        assertNodeIsWallNode(maze, newNodeX3Y1);
        assertNodeIsWallNode(maze, newNodeX3Y2);
        assertNodeIsWallNode(maze, newNodeX3Y3);
        assertNodeIsWallNode(maze, newNodeX3Y4);
        assertNodeIsWallNode(maze, newNodeX3Y5);

        // check if all nodes in 5th column are wall nodes
        assertNodeIsWallNode(maze, newNodeX0Y5);
        assertNodeIsWallNode(maze, newNodeX1Y5);
        assertNodeIsWallNode(maze, newNodeX2Y5);
        // assertNodeIsWallNode(maze,newNodeX3Y5);//already checked
    }

    @Test
    void testIncreaseXDimensionByOneAndYDimensionByTwo() {
        // create simple 3x5 maze
        Maze maze = TestUtils.getSimpleHorizontalMaze();

        // save old nodes of the 0th row
        NodeFactory.Node oldNodeX0Y0 = maze.getNodeAt(0, 0);
        NodeFactory.Node oldNodeX0Y1 = maze.getNodeAt(0, 1);
        NodeFactory.Node oldNodeX0Y2 = maze.getNodeAt(0, 2);
        NodeFactory.Node oldNodeX0Y3 = maze.getNodeAt(0, 3);
        NodeFactory.Node oldNodeX0Y4 = maze.getNodeAt(0, 4);

        // save old nodes of the 1th row
        NodeFactory.Node oldNodeX1Y0 = maze.getNodeAt(1, 0);
        NodeFactory.Node oldNodeX1Y1 = maze.getNodeAt(1, 1); // start node
        NodeFactory.Node oldNodeX1Y2 = maze.getNodeAt(1, 2);
        NodeFactory.Node oldNodeX1Y3 = maze.getNodeAt(1, 3); // old end node
        NodeFactory.Node oldNodeX1Y4 = maze.getNodeAt(1, 4);

        // save old nodes of the 2th row
        NodeFactory.Node oldNodeX2Y0 = maze.getNodeAt(2, 0);
        NodeFactory.Node oldNodeX2Y1 = maze.getNodeAt(2, 1);
        NodeFactory.Node oldNodeX2Y2 = maze.getNodeAt(2, 2);
        NodeFactory.Node oldNodeX2Y3 = maze.getNodeAt(2, 3);
        NodeFactory.Node oldNodeX2Y4 = maze.getNodeAt(2, 4);

        // use resize Operator
        ResizeOperator resizeOperator = new ResizeOperator(100, 123);
        resizeOperator.setxIncreasementValue(1);
        resizeOperator.setyIncreasementValue(2);
        resizeOperator.changeMaze(maze);

        // check if maze x dimension has been Increased by one
        Assertions.assertEquals(4, maze.getMaze().length);

        // check if maze y dimension has been Increased by two
        Assertions.assertEquals(7, maze.getMaze()[0].length);

        // check if nodes of old maze haven't been replaced by new ones
        assertNodesAreEqual(maze, oldNodeX0Y0, oldNodeX0Y1, oldNodeX0Y2, oldNodeX0Y3,
                oldNodeX0Y4, oldNodeX1Y0, oldNodeX1Y1, oldNodeX1Y2, oldNodeX1Y3,
                oldNodeX1Y4, oldNodeX2Y0, oldNodeX2Y1, oldNodeX2Y2, oldNodeX2Y3, oldNodeX2Y4);

        // check if former wall node right to the old end node has changed to way node
        assertNodeIsWayNode(maze, oldNodeX1Y4);

        // check if the remaining already existing nodes of the 4th column are still wall nodes
        assertNodeIsWallNode(maze, oldNodeX0Y4);
        assertNodeIsWallNode(maze, oldNodeX2Y4);

        // save new nodes of new row (3th)
        NodeFactory.Node newNodeX3Y0 = maze.getNodeAt(3, 0);
        NodeFactory.Node newNodeX3Y1 = maze.getNodeAt(3, 1);
        NodeFactory.Node newNodeX3Y2 = maze.getNodeAt(3, 2);
        NodeFactory.Node newNodeX3Y3 = maze.getNodeAt(3, 3);
        NodeFactory.Node newNodeX3Y4 = maze.getNodeAt(3, 4);
        NodeFactory.Node newNodeX3Y5 = maze.getNodeAt(3, 5);
        NodeFactory.Node newNodeX3Y6 = maze.getNodeAt(3, 6);

        // save new nodes of new column (5th)
        NodeFactory.Node newNodeX0Y5 = maze.getNodeAt(0, 5);
        NodeFactory.Node newNodeX1Y5 = maze.getNodeAt(1, 5);
        NodeFactory.Node newNodeX2Y5 = maze.getNodeAt(2, 5); // new end node
        // NodeFactory.Node newNodeX3Y5 = maze.getNodeAt(3, 5); // already declared

        // save new nodes of new column (6th)
        NodeFactory.Node newNodeX0Y6 = maze.getNodeAt(0, 6);
        NodeFactory.Node newNodeX1Y6 = maze.getNodeAt(1, 6);
        NodeFactory.Node newNodeX2Y6 = maze.getNodeAt(2, 6);
        // NodeFactory.Node newNodeX3Y6 = maze.getNodeAt(3, 6); // already declared

        // check if node right to the new way node is also a way node
        assertNodeIsWayNode(maze, newNodeX1Y5);

        // check if the node two nodes on the right and one node below the old end node is the new end node (5th column 2th row)
        assertNodeIsEndNode(maze, newNodeX2Y5);

        // check if the remaining nodes of the 5th column are wall nodes
        assertNodeIsWallNode(maze, newNodeX0Y5);
        assertNodeIsWallNode(maze, newNodeX3Y5);

        // check if all nodes of the 6th column are wall node
        assertNodeIsWallNode(maze, newNodeX0Y6);
        assertNodeIsWallNode(maze, newNodeX1Y6);
        assertNodeIsWallNode(maze, newNodeX2Y6);

        // check if all nodes of the 3th row are wall node
        assertNodeIsWallNode(maze, newNodeX3Y0);
        assertNodeIsWallNode(maze, newNodeX3Y1);
        assertNodeIsWallNode(maze, newNodeX3Y2);
        assertNodeIsWallNode(maze, newNodeX3Y3);
        assertNodeIsWallNode(maze, newNodeX3Y4);
        // assertNodeIsWallNode(maze,newNodeX3Y5); //already checked
        assertNodeIsWallNode(maze, newNodeX3Y6);
    }

    @Test
    void testIncreaseXDimensionByTwoAndYDimensionByOne() {
        // create simple 3x5 maze
        Maze maze = TestUtils.getSimpleHorizontalMaze();

        // save old nodes of the 0th row
        NodeFactory.Node oldNodeX0Y0 = maze.getNodeAt(0, 0);
        NodeFactory.Node oldNodeX0Y1 = maze.getNodeAt(0, 1);
        NodeFactory.Node oldNodeX0Y2 = maze.getNodeAt(0, 2);
        NodeFactory.Node oldNodeX0Y3 = maze.getNodeAt(0, 3);
        NodeFactory.Node oldNodeX0Y4 = maze.getNodeAt(0, 4);

        // save old nodes of the 1th row
        NodeFactory.Node oldNodeX1Y0 = maze.getNodeAt(1, 0);
        NodeFactory.Node oldNodeX1Y1 = maze.getNodeAt(1, 1); // start node
        NodeFactory.Node oldNodeX1Y2 = maze.getNodeAt(1, 2);
        NodeFactory.Node oldNodeX1Y3 = maze.getNodeAt(1, 3); // old end node
        NodeFactory.Node oldNodeX1Y4 = maze.getNodeAt(1, 4);

        // save old nodes of the 2th row
        NodeFactory.Node oldNodeX2Y0 = maze.getNodeAt(2, 0);
        NodeFactory.Node oldNodeX2Y1 = maze.getNodeAt(2, 1);
        NodeFactory.Node oldNodeX2Y2 = maze.getNodeAt(2, 2);
        NodeFactory.Node oldNodeX2Y3 = maze.getNodeAt(2, 3);
        NodeFactory.Node oldNodeX2Y4 = maze.getNodeAt(2, 4);

        // use resize Operator
        ResizeOperator resizeOperator = new ResizeOperator(100, 123);
        resizeOperator.setxIncreasementValue(2);
        resizeOperator.setyIncreasementValue(1);
        resizeOperator.changeMaze(maze);

        // check if maze x dimension has been Increased by one
        Assertions.assertEquals(5, maze.getMaze().length);

        // check if maze y dimension has been Increased by two
        Assertions.assertEquals(6, maze.getMaze()[0].length);

        // check if nodes of old maze haven't been replaced by new ones
        assertNodesAreEqual(maze, oldNodeX0Y0, oldNodeX0Y1, oldNodeX0Y2, oldNodeX0Y3,
                oldNodeX0Y4, oldNodeX1Y0, oldNodeX1Y1, oldNodeX1Y2, oldNodeX1Y3,
                oldNodeX1Y4, oldNodeX2Y0, oldNodeX2Y1, oldNodeX2Y2, oldNodeX2Y3, oldNodeX2Y4);

        // check if former wall node right to the old end node has changed to way node;
        assertNodeIsWayNode(maze, oldNodeX1Y4);

        // check if node below new way is also a way node;
        assertNodeIsWayNode(maze, oldNodeX2Y4);

        // check if the remaining already existing node of the 4th column is still a wall nodes
        assertNodeIsWallNode(maze, oldNodeX0Y4);

        // save new nodes of new row (3th)
        NodeFactory.Node newNodeX3Y0 = maze.getNodeAt(3, 0);
        NodeFactory.Node newNodeX3Y1 = maze.getNodeAt(3, 1);
        NodeFactory.Node newNodeX3Y2 = maze.getNodeAt(3, 2);
        NodeFactory.Node newNodeX3Y3 = maze.getNodeAt(3, 3);
        NodeFactory.Node newNodeX3Y4 = maze.getNodeAt(3, 4); // new end node
        NodeFactory.Node newNodeX3Y5 = maze.getNodeAt(3, 5);

        // save new nodes of new row (4th)
        NodeFactory.Node newNodeX4Y0 = maze.getNodeAt(4, 0);
        NodeFactory.Node newNodeX4Y1 = maze.getNodeAt(4, 1);
        NodeFactory.Node newNodeX4Y2 = maze.getNodeAt(4, 2);
        NodeFactory.Node newNodeX4Y3 = maze.getNodeAt(4, 3);
        NodeFactory.Node newNodeX4Y4 = maze.getNodeAt(4, 4);
        NodeFactory.Node newNodeX4Y5 = maze.getNodeAt(4, 5);

        // save new nodes of new column (5th)
        NodeFactory.Node newNodeX0Y5 = maze.getNodeAt(0, 5);
        NodeFactory.Node newNodeX1Y5 = maze.getNodeAt(1, 5);
        NodeFactory.Node newNodeX2Y5 = maze.getNodeAt(2, 5);
        // NodeFactory.Node newNodeX3Y5 = maze.getNodeAt(2, 5);  // already declared
        // NodeFactory.Node newNodeX4Y5 = maze.getNodeAt(2, 5);  // already declared

        // check if the node one node to the right and two nodes below the old end node is the new end node (4th column 3th row)
        assertNodeIsEndNode(maze, newNodeX3Y4);

        // check if the remaining nodes of the 3th row are wall nodes
        assertNodeIsWallNode(maze, newNodeX3Y0);
        assertNodeIsWallNode(maze, newNodeX3Y1);
        assertNodeIsWallNode(maze, newNodeX3Y2);
        assertNodeIsWallNode(maze, newNodeX3Y3);
        assertNodeIsWallNode(maze, newNodeX3Y5);

        // check if all nodes of the 4th row are walls
        assertNodeIsWallNode(maze, newNodeX4Y0);
        assertNodeIsWallNode(maze, newNodeX4Y1);
        assertNodeIsWallNode(maze, newNodeX4Y2);
        assertNodeIsWallNode(maze, newNodeX4Y3);
        assertNodeIsWallNode(maze, newNodeX4Y4);
        assertNodeIsWallNode(maze, newNodeX4Y5);

        // check if the remaining nodes of the 5th column are wall nodes
        assertNodeIsWallNode(maze, newNodeX0Y5);
        assertNodeIsWallNode(maze, newNodeX1Y5);
        assertNodeIsWallNode(maze, newNodeX2Y5);
        // assertNodeIsWallNode(maze,newNodeX3Y5); //already checked
        // assertNodeIsWallNode(maze,newNodeX4Y5); //already checked

    }

    @Test
    void testIncreaseXDimensionByTwoAndYDimensionByTwo() {
        // create simple 3x5 maze
        Maze maze = TestUtils.getSimpleHorizontalMaze();

        // save old nodes of the 0th row
        NodeFactory.Node oldNodeX0Y0 = maze.getNodeAt(0, 0);
        NodeFactory.Node oldNodeX0Y1 = maze.getNodeAt(0, 1);
        NodeFactory.Node oldNodeX0Y2 = maze.getNodeAt(0, 2);
        NodeFactory.Node oldNodeX0Y3 = maze.getNodeAt(0, 3);
        NodeFactory.Node oldNodeX0Y4 = maze.getNodeAt(0, 4);

        // save old nodes of the 1th row
        NodeFactory.Node oldNodeX1Y0 = maze.getNodeAt(1, 0);
        NodeFactory.Node oldNodeX1Y1 = maze.getNodeAt(1, 1); // start node
        NodeFactory.Node oldNodeX1Y2 = maze.getNodeAt(1, 2);
        NodeFactory.Node oldNodeX1Y3 = maze.getNodeAt(1, 3); // old end node
        NodeFactory.Node oldNodeX1Y4 = maze.getNodeAt(1, 4);

        // save old nodes of the 2th row
        NodeFactory.Node oldNodeX2Y0 = maze.getNodeAt(2, 0);
        NodeFactory.Node oldNodeX2Y1 = maze.getNodeAt(2, 1);
        NodeFactory.Node oldNodeX2Y2 = maze.getNodeAt(2, 2);
        NodeFactory.Node oldNodeX2Y3 = maze.getNodeAt(2, 3);
        NodeFactory.Node oldNodeX2Y4 = maze.getNodeAt(2, 4);

        // use resize Operator
        ResizeOperator resizeOperator = new ResizeOperator(100, 123);
        resizeOperator.setxIncreasementValue(2);
        resizeOperator.setyIncreasementValue(2);
        resizeOperator.changeMaze(maze);

        // check if maze x dimension has been increased by one
        Assertions.assertEquals(5, maze.getMaze().length);

        // check if maze y dimension has been increased by two
        Assertions.assertEquals(7, maze.getMaze()[0].length);

        // check if nodes of old maze haven't been replaced by new ones
        assertNodesAreEqual(maze, oldNodeX0Y0, oldNodeX0Y1, oldNodeX0Y2, oldNodeX0Y3,
                oldNodeX0Y4, oldNodeX1Y0, oldNodeX1Y1, oldNodeX1Y2, oldNodeX1Y3,
                oldNodeX1Y4, oldNodeX2Y0, oldNodeX2Y1, oldNodeX2Y2, oldNodeX2Y3, oldNodeX2Y4);

        // check if former wall node right to the old end node has changed to way node
        assertNodeIsWayNode(maze, oldNodeX1Y4);

        // check if remaining already existing nodes in 4th column are still wall nodes
        assertNodeIsWallNode(maze, oldNodeX0Y4);
        assertNodeIsWallNode(maze, oldNodeX2Y4);

        // save new nodes of new row (3th)
        NodeFactory.Node newNodeX3Y0 = maze.getNodeAt(3, 0);
        NodeFactory.Node newNodeX3Y1 = maze.getNodeAt(3, 1);
        NodeFactory.Node newNodeX3Y2 = maze.getNodeAt(3, 2);
        NodeFactory.Node newNodeX3Y3 = maze.getNodeAt(3, 3);
        NodeFactory.Node newNodeX3Y4 = maze.getNodeAt(3, 4);
        NodeFactory.Node newNodeX3Y5 = maze.getNodeAt(3, 5); // new end node
        NodeFactory.Node newNodeX3Y6 = maze.getNodeAt(3, 6);

        // save new nodes of new row (4th)
        NodeFactory.Node newNodeX4Y0 = maze.getNodeAt(4, 0);
        NodeFactory.Node newNodeX4Y1 = maze.getNodeAt(4, 1);
        NodeFactory.Node newNodeX4Y2 = maze.getNodeAt(4, 2);
        NodeFactory.Node newNodeX4Y3 = maze.getNodeAt(4, 3);
        NodeFactory.Node newNodeX4Y4 = maze.getNodeAt(4, 4);
        NodeFactory.Node newNodeX4Y5 = maze.getNodeAt(4, 5);
        NodeFactory.Node newNodeX4Y6 = maze.getNodeAt(4, 6);

        // save new nodes of new column (5th)
        NodeFactory.Node newNodeX0Y5 = maze.getNodeAt(0, 5);
        NodeFactory.Node newNodeX1Y5 = maze.getNodeAt(1, 5);
        NodeFactory.Node newNodeX2Y5 = maze.getNodeAt(2, 5);
        // NodeFactory.Node newNodeX3Y5 = maze.getNodeAt(3, 5);  // already declared
        // NodeFactory.Node newNodeX4Y5 = maze.getNodeAt(4, 5);  // already declared

        // save new nodes of new column (6th)
        NodeFactory.Node newNodeX0Y6 = maze.getNodeAt(0, 6);
        NodeFactory.Node newNodeX1Y6 = maze.getNodeAt(1, 6);
        NodeFactory.Node newNodeX2Y6 = maze.getNodeAt(2, 6);
        // NodeFactory.Node newNodeX3Y6 = maze.getNodeAt(3, 6);  // already declared
        // NodeFactory.Node newNodeX4Y6 = maze.getNodeAt(4, 6);  // already declared

        // check if node right to new way node is also a way node
        assertNodeIsWayNode(maze, newNodeX1Y5);

        // check if node below the last node is also a way node
        assertNodeIsWayNode(maze, newNodeX2Y5);

        // check if the node two node to the right and two nodes below the old end node is the new end node (5th column 3th row)
        assertNodeIsEndNode(maze, newNodeX3Y5);

        // check if the remaining nodes of the 3th row are wall nodes
        assertNodeIsWallNode(maze, newNodeX3Y0);
        assertNodeIsWallNode(maze, newNodeX3Y1);
        assertNodeIsWallNode(maze, newNodeX3Y2);
        assertNodeIsWallNode(maze, newNodeX3Y3);
        assertNodeIsWallNode(maze, newNodeX3Y4);
        assertNodeIsWallNode(maze, newNodeX3Y6);

        // check if the nodes of the 4th row are wall nodes
        assertNodeIsWallNode(maze, newNodeX4Y0);
        assertNodeIsWallNode(maze, newNodeX4Y1);
        assertNodeIsWallNode(maze, newNodeX4Y2);
        assertNodeIsWallNode(maze, newNodeX4Y3);
        assertNodeIsWallNode(maze, newNodeX4Y4);
        assertNodeIsWallNode(maze, newNodeX4Y5);
        assertNodeIsWallNode(maze, newNodeX4Y6);

        // check if the remaining nodes of the 5th column are wall nodes
        assertNodeIsWallNode(maze, newNodeX0Y5);
        // assertNodeIsWallNode(maze,newNodeX1Y5); //already checked
        // assertNodeIsWallNode(maze,newNodeX2Y5); //already checked
        // assertNodeIsWallNode(maze,newNodeX3Y5); //already checked
        assertNodeIsWallNode(maze, newNodeX4Y5);

        // check if the remaining nodes of the 6th column are wall nodes
        assertNodeIsWallNode(maze, newNodeX0Y6);
        assertNodeIsWallNode(maze, newNodeX1Y6);
        assertNodeIsWallNode(maze, newNodeX2Y6);
        // assertNodeIsWallNode(maze,newNodeX3Y6); //already checked
        // assertNodeIsWallNode(maze,newNodeX4Y6); //already checked

    }

    private void assertNodesAreEqual(Maze maze, NodeFactory.Node oldNodeX0Y0,
                                     NodeFactory.Node oldNodeX0Y1, NodeFactory.Node oldNodeX0Y2,
                                     NodeFactory.Node oldNodeX0Y3, NodeFactory.Node oldNodeX0Y4,
                                     NodeFactory.Node oldNodeX1Y0, NodeFactory.Node oldNodeX1Y1,
                                     NodeFactory.Node oldNodeX1Y2, NodeFactory.Node oldNodeX1Y3,
                                     NodeFactory.Node oldNodeX1Y4, NodeFactory.Node oldNodeX2Y0,
                                     NodeFactory.Node oldNodeX2Y1, NodeFactory.Node oldNodeX2Y2,
                                     NodeFactory.Node oldNodeX2Y3, NodeFactory.Node oldNodeX2Y4) {
        // check if nodes of the 0th row are still the same objects
        Assertions.assertEquals(oldNodeX0Y0, maze.getMaze()[0][0]);
        Assertions.assertEquals(oldNodeX0Y1, maze.getMaze()[0][1]);
        Assertions.assertEquals(oldNodeX0Y2, maze.getMaze()[0][2]);
        Assertions.assertEquals(oldNodeX0Y3, maze.getMaze()[0][3]);
        Assertions.assertEquals(oldNodeX0Y4, maze.getMaze()[0][4]);

        // check if nodes of the 1th row are still the same objects
        Assertions.assertEquals(oldNodeX1Y0, maze.getMaze()[1][0]);
        Assertions.assertEquals(oldNodeX1Y1, maze.getMaze()[1][1]);
        Assertions.assertEquals(oldNodeX1Y2, maze.getMaze()[1][2]);
        Assertions.assertEquals(oldNodeX1Y3, maze.getMaze()[1][3]);
        Assertions.assertEquals(oldNodeX1Y4, maze.getMaze()[1][4]);

        // check if nodes of the 2th row are still the same objects
        Assertions.assertEquals(oldNodeX2Y0, maze.getMaze()[2][0]);
        Assertions.assertEquals(oldNodeX2Y1, maze.getMaze()[2][1]);
        Assertions.assertEquals(oldNodeX2Y2, maze.getMaze()[2][2]);
        Assertions.assertEquals(oldNodeX2Y3, maze.getMaze()[2][3]);
        Assertions.assertEquals(oldNodeX2Y4, maze.getMaze()[2][4]);
    }

    private void assertNodeIsWallNode(Maze maze, NodeFactory.Node node) {
        Assertions.assertTrue(maze.getNodeFactory().nodeLooksLikeWallNode(node));
        Assertions.assertSame(node.getNodeType(), NodeType.IMPASSABLE);
    }

    private void assertNodeIsWayNode(Maze maze, NodeFactory.Node node) {
        Assertions.assertTrue(maze.getNodeFactory().nodeLooksLikeWayNode(node));
        Assertions.assertSame(node.getNodeType(), NodeType.PASSABLE);
    }

    private void assertNodeIsEndNode(Maze maze, NodeFactory.Node node) {
        assertNodeIsWayNode(maze, node);
        Assertions.assertSame(node, maze.getEndNode());
    }

    @Test
    void testEstimateCostX0Y0() {
        // create simple 3x5 maze
        Maze maze = TestUtils.getSimpleHorizontalMaze();

        // try to use resize Operator if allowed cost is lower than the costs per dimension
        ResizeOperator resizeOperator = new ResizeOperator(100, 123);

        // check if method returns zero costs
        double resultingCosts = resizeOperator.estimateCost(maze, 50);
        Assertions.assertEquals(0.0d, resultingCosts);

        // no change can happen. Therefore, check if changeMaze method returns false
        boolean changeMazeSuccess = resizeOperator.changeMaze(maze);
        Assertions.assertFalse(changeMazeSuccess);
    }


    @Test
    void testEstimateCostX2Y2() {
        // create simple 3x5 maze
        Maze maze = TestUtils.getSimpleHorizontalMaze();

        // try to use resize Operator if a total dimension increase of 4 is allowed
        ResizeOperator resizeOperator = new ResizeOperator(25, 265);

        // estimateCost method will return the cost for a total increase of 4.
        // Therefore, check if costs are costsPerDimension * 4
        double resultingCosts = resizeOperator.estimateCost(maze, 100);
        Assertions.assertEquals(25 * 4, resultingCosts);

        // change is allowed. Therefore check if changeMaze method returns true
        boolean changeMazeSuccess = resizeOperator.changeMaze(maze);
        Assertions.assertTrue(changeMazeSuccess);
    }

}
