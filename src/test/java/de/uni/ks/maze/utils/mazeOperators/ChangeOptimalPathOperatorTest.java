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

class ChangeOptimalPathOperatorTest {

    @Test
    void testEstimateCostNoBlockableNodesFound() {
        // create simple maze without parallel routes and dead ends
        //  | # | # | # | # | # |
        //  | # | P | P | P | # |
        //  | # | # | # | # | # |
        Maze maze = TestUtils.getSimpleHorizontalMaze();

        // try to use Operator if there are no nodes that can be blocked
        ChangeOptimalPathOperator changeOptimalPathOperator = new ChangeOptimalPathOperator(1, 123);

        // check if method returns zero costs
        double resultingCosts = changeOptimalPathOperator.estimateCost(maze, 1000);
        Assertions.assertEquals(0.0d, resultingCosts);

        // no change can happen. Therefore, check if changeMaze method returns false
        boolean changeMazeSuccess = changeOptimalPathOperator.changeMaze(maze);
        Assertions.assertFalse(changeMazeSuccess);
    }

    @Test
    void testEstimateCostNodeToBlockWouldBlockDeadEnds() {
        // create maze with one parallel route and no blockable nodes (maze always has to be connected)
        // | # | # | # | # | # | # | # |
        // | # | # | # | P | # | # | # |
        // | # | S | P | P | P | E | # |
        // | # | # | P | # | P | # | # |
        // | # | # | P | P | P | # | # |
        // | # | # | # | # | # | # | # |
        Maze maze = TestUtils.getMazeWithOneParallelRouteAndDeadEndOnOptimalPath();

        // try to use Operator if there are no nodes that can be blocked
        ChangeOptimalPathOperator changeOptimalPathOperator = new ChangeOptimalPathOperator(1, 123);

        // check if method returns zero costs
        double resultingCosts = changeOptimalPathOperator.estimateCost(maze, 1000);
        Assertions.assertEquals(0.0d, resultingCosts);

        // no change can happen. Therefore, check if changeMaze method returns false
        boolean changeMazeSuccess = changeOptimalPathOperator.changeMaze(maze);
        Assertions.assertFalse(changeMazeSuccess);
    }

    @Test
    void testEstimateCostCostsNotAllowed() {
        // create 6x6 maze with one parallel route inside
        // | # | # | # | # | # | # |
        // | # | S | # | # | # | # |
        // | # | P | P | P | E | # |
        // | # | P | # | P | # | # |
        // | # | P | P | P | # | # |
        // | # | # | # | # | # | # |
        Maze maze = TestUtils.getMazeWithOneParallelRoute();

        // try to use Operator if there is an node that can be blocked but the costs are not allowed
        ChangeOptimalPathOperator changeOptimalPathOperator = new ChangeOptimalPathOperator(1000, 123);

        // check if method returns zero costs
        double resultingCosts = changeOptimalPathOperator.estimateCost(maze, 1);
        Assertions.assertEquals(0.0d, resultingCosts);

        // no change can happen. Therefore, check if changeMaze method returns false
        boolean changeMazeSuccess = changeOptimalPathOperator.changeMaze(maze);
        Assertions.assertFalse(changeMazeSuccess);
    }

    @Test
    void testEstimateCostCostsAreAllowed() {
        // create 6x6 maze with one parallel route inside
        // | # | # | # | # | # | # |
        // | # | S | # | # | # | # |
        // | # | P | P | P | E | # |
        // | # | P | # | P | # | # |
        // | # | P | P | P | # | # |
        // | # | # | # | # | # | # |
        Maze maze = TestUtils.getMazeWithOneParallelRoute();

        // try to use Operator if there is an node that can be blocked and the resulting costs are allowed
        ChangeOptimalPathOperator changeOptimalPathOperator = new ChangeOptimalPathOperator(5, 123);

        // check if method returns correct costs
        double resultingCosts = changeOptimalPathOperator.estimateCost(maze, 25);
        Assertions.assertEquals(20.0d, resultingCosts);

        // change can happen. Therefore, check if changeMaze method returns true
        boolean changeMazeSuccess = changeOptimalPathOperator.changeMaze(maze);
        Assertions.assertTrue(changeMazeSuccess);
    }

    @Test
    void testChangeMaze() {
        // create 6x6 maze with one parallel route inside
        // | # | # | # | # | # | # |
        // | # | S | # | # | # | # |
        // | # | P | P | P | E | # |
        // | # | P | # | P | # | # |
        // | # | P | P | P | # | # |
        // | # | # | # | # | # | # |
        Maze maze = TestUtils.getMazeWithOneParallelRoute();
        ChangeOptimalPathOperator changeOptimalPathOperator = new ChangeOptimalPathOperator(5, 123);

        // set node to block
        NodeFactory.Node nodeToBlock = maze.getNodeAt(2, 2);
        changeOptimalPathOperator.setNodeToBlock(nodeToBlock);

        // change can happen. Therefore, check if changeMaze method returns true.
        boolean changeMazeSuccess = changeOptimalPathOperator.changeMaze(maze);
        Assertions.assertTrue(changeMazeSuccess);

        // check if node is blocked
        Assertions.assertTrue(nodeToBlock.getNodeType() == NodeType.IMPASSABLE);

    }

}
