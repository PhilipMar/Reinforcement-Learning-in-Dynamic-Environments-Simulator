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
package de.uni.ks.maze.utils;

import de.uni.ks.TestUtils;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.NodeFactory.Node;
import de.uni.ks.maze.NodeType;
import de.uni.ks.maze.utils.mazeOperators.MazeOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;


class MazeUtilsTest {

    @Test
    void testDijkstra() {

        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Maze maze = MazeUtils.getPlaceholderMaze(nodeFactory);
        int len = maze.getLengthOfShortestPath();

        Assertions.assertEquals(7, len);
    }

    @Test
    void testMovedEndNode() {
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Maze maze = MazeUtils.getPlaceholderMaze(nodeFactory);

        nodeFactory.changeNodeToType(maze.getEndNode(), NodeType.IMPASSABLE);

        Node node = maze.getNodeAt(3, 3);
        maze.setEndNode(node);

        Assertions.assertEquals(maze.getEndNode(), node);

        Assertions.assertEquals(3, maze.getLengthOfShortestPath());
    }

    @Test
    void testNoPathExists() {
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Maze maze = MazeUtils.getPlaceholderMaze(nodeFactory);

        nodeFactory.changeNodeToType(maze.getNodeAt(2, 3), NodeType.IMPASSABLE);

        Assertions.assertThrows(IllegalArgumentException.class, maze::getLengthOfShortestPath);
    }

    @Test
    void testChangeMazeAlgorithm() {

        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        Maze maze = MazeUtils.getPlaceholderMaze(nodeFactory);
        Random random = new Random(123);

        //   1. returns 0 when delta is 0
        Assertions.assertEquals(0.0,
                MazeUtils.changeMaze(maze, Arrays.asList(new FakeOperator(true, 2.0),
                        new FakeOperator(true, 3.0)), 0.0, random));
        //   2. --- when no operator is given
        Assertions.assertEquals(0.0,
                MazeUtils.changeMaze(maze, new ArrayList<>(), 50.0, random));
        //   3. does not make more changes than delta allows
        double delta1 = 55;
        Assertions.assertTrue(delta1 >= MazeUtils.changeMaze(maze,
                Arrays.asList(new FakeOperator(true, 2.0),
                        new FakeOperator(true, 3.0),
                        new FakeOperator(false, 55),
                        new FakeResizeOperator(true, 1),
                        new FakeOperator(true, 10)), delta1, random));
    }

    private static class FakeOperator implements MazeOperator {

        private boolean changeMaze;
        private double cost;

        private FakeOperator(boolean changeMaze, double cost) {
            this.changeMaze = changeMaze;
            this.cost = cost;
        }

        @Override
        public boolean changeMaze(Maze maze) {
            return changeMaze;
        }

        @Override
        public double estimateCost(Maze maze, double allowedCost) {
            return cost;
        }

        @Override
        public String myConfigString() {
            return null;
        }
    }

    private static class FakeResizeOperator implements MazeOperator {

        private boolean changeMaze;
        private double cost;

        private FakeResizeOperator(boolean changeMaze, double cost) {
            this.changeMaze = changeMaze;
            this.cost = cost;
        }

        @Override
        public boolean changeMaze(Maze maze) {
            return changeMaze;
        }

        @Override
        public double estimateCost(Maze maze, double allowedCost) {
            return cost;
        }

        @Override
        public String myConfigString() {
            return null;
        }
    }

    //    #############################################################################
    //    check if  MazeUtils can detect parallel routes in simple 6x6 maze with one parallel route
    //    #############################################################################
    @Test
    void testGetParallelRoutesMazeWithOneParallelRoute() {
        Maze maze = TestUtils.getMazeWithOneParallelRoute();

        // get all parallel routes of the given maze
        List<Stack<Node>> allParallelRoutes = MazeUtils.getAllParallelRoutes(maze);

        // maze has only one parallel route inside
        Assertions.assertSame(1, allParallelRoutes.size());

        // get all nodes of the parallel route
        Stack<NodeFactory.Node> parallelRouteNodes = allParallelRoutes.get(0);

        // check if parallel route consists of 8 nodes
        Assertions.assertSame(8, parallelRouteNodes.size());
        Assertions.assertTrue(parallelRouteNodes.contains(maze.getNodeAt(2, 1)));
        Assertions.assertTrue(parallelRouteNodes.contains(maze.getNodeAt(2, 2)));
        Assertions.assertTrue(parallelRouteNodes.contains(maze.getNodeAt(2, 3)));
        Assertions.assertTrue(parallelRouteNodes.contains(maze.getNodeAt(3, 1)));
        Assertions.assertTrue(parallelRouteNodes.contains(maze.getNodeAt(3, 3)));
        Assertions.assertTrue(parallelRouteNodes.contains(maze.getNodeAt(4, 1)));
        Assertions.assertTrue(parallelRouteNodes.contains(maze.getNodeAt(4, 2)));
        Assertions.assertTrue(parallelRouteNodes.contains(maze.getNodeAt(4, 3)));
    }

    @Test
    void testGetParallelRoutesMazeWithTwoParallelRoutes() {
        Maze maze = TestUtils.getMazeWithTwoParallelRoutes();

        // get all parallel routes of the given maze
        List<Stack<Node>> allParallelRoutes = MazeUtils.getAllParallelRoutes(maze);

        // maze has only one parallel route inside
        Assertions.assertSame(2, allParallelRoutes.size());

        // get all nodes of the first parallel route
        Stack<NodeFactory.Node> firstParallelRoute = allParallelRoutes.get(0);

        // check if first parallel route still consists of 8 nodes
        Assertions.assertSame(8, firstParallelRoute.size());
        Assertions.assertTrue(firstParallelRoute.contains(maze.getNodeAt(3, 1)));
        Assertions.assertTrue(firstParallelRoute.contains(maze.getNodeAt(3, 2)));
        Assertions.assertTrue(firstParallelRoute.contains(maze.getNodeAt(3, 3)));
        Assertions.assertTrue(firstParallelRoute.contains(maze.getNodeAt(4, 1)));
        Assertions.assertTrue(firstParallelRoute.contains(maze.getNodeAt(4, 3)));
        Assertions.assertTrue(firstParallelRoute.contains(maze.getNodeAt(5, 1)));
        Assertions.assertTrue(firstParallelRoute.contains(maze.getNodeAt(5, 2)));
        Assertions.assertTrue(firstParallelRoute.contains(maze.getNodeAt(5, 3)));

        // get all nodes of the second parallel route
        Stack<NodeFactory.Node> secondParallelRoute = allParallelRoutes.get(1);

        // check if second parallel route consists of 8 nodes
        Assertions.assertSame(8, secondParallelRoute.size());
        Assertions.assertTrue(secondParallelRoute.contains(maze.getNodeAt(1, 3)));
        Assertions.assertTrue(secondParallelRoute.contains(maze.getNodeAt(1, 4)));
        Assertions.assertTrue(secondParallelRoute.contains(maze.getNodeAt(1, 5)));
        Assertions.assertTrue(secondParallelRoute.contains(maze.getNodeAt(2, 3)));
        Assertions.assertTrue(secondParallelRoute.contains(maze.getNodeAt(2, 5)));
        Assertions.assertTrue(secondParallelRoute.contains(maze.getNodeAt(3, 3)));
        Assertions.assertTrue(secondParallelRoute.contains(maze.getNodeAt(3, 4)));
        Assertions.assertTrue(secondParallelRoute.contains(maze.getNodeAt(3, 5)));
    }

    @Test
    void testGetParallelRouteNodesMazeWithTwoParallelRoutes() {
        Maze maze = TestUtils.getMazeWithTwoParallelRoutes();

        // get all parallel routes of the given maze
        Stack<Node> allParallelRouteNodes = MazeUtils.getAllParallelRouteNodes(maze);

        // check if set contains 15 elements
        Assertions.assertSame(15, allParallelRouteNodes.size());

        // check if set contains all parallel route nodes
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(1, 3)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(1, 4)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(1, 5)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(2, 3)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(2, 5)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(3, 3)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(3, 4)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(3, 5)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(3, 1)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(3, 2)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(4, 1)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(4, 3)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(5, 1)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(5, 2)));
        Assertions.assertTrue(allParallelRouteNodes.contains(maze.getNodeAt(5, 3)));
    }
}
