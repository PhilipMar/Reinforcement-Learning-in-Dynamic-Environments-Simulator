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
package de.uni.ks;

import de.uni.ks.agent.explorationPolicies.GreedyPolicy;
import de.uni.ks.configuration.Config;
import de.uni.ks.criterion.changeLevel.MaxEpisodesReached;
import de.uni.ks.criterion.changeLevel.PerformanceAchievedStaticTolerance;
import de.uni.ks.criterion.stopEpisode.EndStateReached;
import de.uni.ks.criterion.stopEpisode.MaxActionsReached;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.NodeType;
import de.uni.ks.maze.complexityFunction.DefaultComplexityFunction;
import de.uni.ks.maze.utils.mazeOperators.DeadEndOperator;
import de.uni.ks.maze.utils.mazeOperators.NewPathOperator;
import de.uni.ks.maze.utils.mazeOperators.ResizeOperator;

/**
 * This class is mainly used to generate test data.
 */
public class TestUtils {

    public static Config getTestConfig() {
        Config config = new Config();
        config.trainingName = "MyTestFileName";
        config.qLearningGamma = 1.1;
        config.qLearningAlpha = 2.2;

        config.showProgressBarInConsole = false;

        config.restrictImageSize = false;

        config.complexityFunction = new DefaultComplexityFunction();

        config.initialQValue = 3.3;
        config.wayNodeReward = 4.4;
        config.endNodeReward = 5.5;
        config.startEachLevelWithEmptyQTable = false;

        config.delta = 5.6;
        config.changeMazeSeed = 57;

        config.horizontal = true;
        config.initialPathLength = 5;
        config.numberOfWayColors = 2;
        config.numberOfWallColors = 2;
        config.generatedWayColorsSeed = 123;
        config.generatedWallColorsSeed = 345;
        config.usedWayColorsSeed = 678;
        config.usedWallColorsSeed = 91011;
        config.minWallWayBrightnessDifference = 200;

        config.numberOfLevels = 12;

        config.explorationPolicy = new GreedyPolicy(42);

        config.mazeOperators.add(new DeadEndOperator(3, 5, 40,
                0.5, 123));
        config.mazeOperators.add(new ResizeOperator(100, 123));
        config.mazeOperators.add(new NewPathOperator(4, 10, 40, 123));

        config.episodeStoppingCriteria.add(new EndStateReached());
        config.episodeStoppingCriteria.add(new MaxActionsReached(33));
        config.episodeStoppingCriteria.add(new MaxActionsReached(44));

        config.levelChangeCriteria.add(new MaxEpisodesReached(55));
        config.levelChangeCriteria.add(new MaxEpisodesReached(66));
        config.levelChangeCriteria.add(new PerformanceAchievedStaticTolerance(99, 10));

        return config;
    }

    public static Maze createMaze(NodeFactory nodeFactory,
                                  NodeFactory.Node upperLeft, NodeFactory.Node up, NodeFactory.Node upperRight,
                                  NodeFactory.Node center, NodeFactory.Node left, NodeFactory.Node right,
                                  NodeFactory.Node downLeft, NodeFactory.Node down, NodeFactory.Node downRight,
                                  NodeFactory.Node startNode, NodeFactory.Node endNode) {
        NodeFactory.Node[][] maze = new NodeFactory.Node[3][3];
        maze[0][0] = upperLeft;
        maze[0][1] = up;
        maze[0][2] = upperRight;
        maze[1][1] = center;
        maze[1][0] = left;
        maze[1][2] = right;
        maze[2][0] = downLeft;
        maze[2][1] = down;
        maze[2][2] = downRight;

        return new Maze(nodeFactory, maze, startNode, endNode);
    }

    public static Maze getDefaultMaze() {
        // init Nodes
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        NodeFactory.Node upperLeft = nodeFactory.buildWayNode();
        NodeFactory.Node up = nodeFactory.buildWayNode();
        NodeFactory.Node upperRight = nodeFactory.buildWayNode();
        NodeFactory.Node center = nodeFactory.buildStartNode();
        NodeFactory.Node left = nodeFactory.buildWayNode();
        NodeFactory.Node right = nodeFactory.buildWayNode();
        NodeFactory.Node downLeft = nodeFactory.buildWayNode();
        NodeFactory.Node down = nodeFactory.buildWayNode();
        NodeFactory.Node downRight = nodeFactory.buildEndNode();

        // create Maze
        return createMaze(nodeFactory, upperLeft, up, upperRight, center, left, right, downLeft, down, downRight,
                center, downRight); // last two are START | END
    }

    //  simple maze without parallel routes and dead ends
    //  | # | # | # | # | # |
    //  | # | P | P | P | # |
    //  | # | # | # | # | # |
    public static Maze getSimpleHorizontalMaze() {
        // init Nodes
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);

        // build upper wall nodes
        NodeFactory.Node x0y0 = nodeFactory.buildWallNode();
        NodeFactory.Node x0y1 = nodeFactory.buildWallNode();
        NodeFactory.Node x0y2 = nodeFactory.buildWallNode();
        NodeFactory.Node x0y3 = nodeFactory.buildWallNode();
        NodeFactory.Node x0y4 = nodeFactory.buildWallNode();

        // build wall and way nodes in the middle of the maze
        NodeFactory.Node x1y0 = nodeFactory.buildWallNode();
        NodeFactory.Node x1y1 = nodeFactory.buildStartNode();
        NodeFactory.Node x1y2 = nodeFactory.buildWayNode();
        NodeFactory.Node x1y3 = nodeFactory.buildEndNode();
        NodeFactory.Node x1y4 = nodeFactory.buildWallNode();

        // build lower wall nodes
        NodeFactory.Node x2y0 = nodeFactory.buildWallNode();
        NodeFactory.Node x2y1 = nodeFactory.buildWallNode();
        NodeFactory.Node x2y2 = nodeFactory.buildWallNode();
        NodeFactory.Node x2y3 = nodeFactory.buildWallNode();
        NodeFactory.Node x2y4 = nodeFactory.buildWallNode();

        // create node array
        NodeFactory.Node[][] maze = new NodeFactory.Node[3][5];
        maze[0][0] = x0y0;
        maze[0][1] = x0y1;
        maze[0][2] = x0y2;
        maze[0][3] = x0y3;
        maze[0][4] = x0y4;
        maze[1][0] = x1y0;
        maze[1][1] = x1y1;
        maze[1][2] = x1y2;
        maze[1][3] = x1y3;
        maze[1][4] = x1y4;
        maze[2][0] = x2y0;
        maze[2][1] = x2y1;
        maze[2][2] = x2y2;
        maze[2][3] = x2y3;
        maze[2][4] = x2y4;

        // create Maze object
        return new Maze(nodeFactory, maze, x1y1, x1y3);
    }

    // create 6x6 maze with one parallel route inside
    // | # | # | # | # | # | # |
    // | # | S | # | # | # | # |
    // | # | P | P | P | E | # |
    // | # | P | # | P | # | # |
    // | # | P | P | P | # | # |
    // | # | # | # | # | # | # |
    public static Maze getMazeWithOneParallelRoute() {
        // create node array
        NodeFactory.Node[][] maze = new NodeFactory.Node[6][6];
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);

        maze[0][0] = nodeFactory.buildWallNode();
        maze[0][1] = nodeFactory.buildWallNode();
        maze[0][2] = nodeFactory.buildWallNode();
        maze[0][3] = nodeFactory.buildWallNode();
        maze[0][4] = nodeFactory.buildWallNode();
        maze[0][5] = nodeFactory.buildWallNode();
        maze[1][0] = nodeFactory.buildWallNode();
        maze[1][1] = nodeFactory.buildStartNode();
        maze[1][2] = nodeFactory.buildWallNode();
        maze[1][3] = nodeFactory.buildWallNode();
        maze[1][4] = nodeFactory.buildWallNode();
        maze[1][5] = nodeFactory.buildWallNode();
        maze[2][0] = nodeFactory.buildWallNode();
        maze[2][1] = nodeFactory.buildWayNode();
        maze[2][2] = nodeFactory.buildWayNode();
        maze[2][3] = nodeFactory.buildWayNode();
        maze[2][4] = nodeFactory.buildEndNode();
        maze[2][5] = nodeFactory.buildWallNode();
        maze[3][0] = nodeFactory.buildWallNode();
        maze[3][1] = nodeFactory.buildWayNode();
        maze[3][2] = nodeFactory.buildWallNode();
        maze[3][3] = nodeFactory.buildWayNode();
        maze[3][4] = nodeFactory.buildWallNode();
        maze[3][5] = nodeFactory.buildWallNode();
        maze[4][0] = nodeFactory.buildWallNode();
        maze[4][1] = nodeFactory.buildWayNode();
        maze[4][2] = nodeFactory.buildWayNode();
        maze[4][3] = nodeFactory.buildWayNode();
        maze[4][4] = nodeFactory.buildWallNode();
        maze[4][5] = nodeFactory.buildWallNode();
        maze[5][0] = nodeFactory.buildWallNode();
        maze[5][1] = nodeFactory.buildWallNode();
        maze[5][2] = nodeFactory.buildWallNode();
        maze[5][3] = nodeFactory.buildWallNode();
        maze[5][4] = nodeFactory.buildWallNode();
        maze[5][5] = nodeFactory.buildWallNode();

        // create Maze object
        return new Maze(nodeFactory, maze, maze[1][1], maze[2][4]);
    }


    // | # | # | # | # | # | # | # |
    // | # | # | # | P | # | # | # |
    // | # | S | P | P | P | E | # |
    // | # | # | P | # | P | # | # |
    // | # | # | P | P | P | # | # |
    // | # | # | # | # | # | # | # |
    public static Maze getMazeWithOneParallelRouteAndDeadEndOnOptimalPath() {
        Maze maze = getPlainMaze(6, 7);
        NodeFactory nodeFactory = maze.getNodeFactory();

        nodeFactory.changeNodeToType(maze.getNodeAt(1, 3), NodeType.PASSABLE);
        nodeFactory.changeNodeToStart(maze.getNodeAt(2, 1));
        nodeFactory.changeNodeToType(maze.getNodeAt(2, 2), NodeType.PASSABLE);
        nodeFactory.changeNodeToType(maze.getNodeAt(2, 3), NodeType.PASSABLE);
        nodeFactory.changeNodeToType(maze.getNodeAt(2, 4), NodeType.PASSABLE);
        nodeFactory.changeNodeToEnd(maze.getNodeAt(2, 5));
        nodeFactory.changeNodeToType(maze.getNodeAt(3, 2), NodeType.PASSABLE);
        nodeFactory.changeNodeToType(maze.getNodeAt(3, 4), NodeType.PASSABLE);
        nodeFactory.changeNodeToType(maze.getNodeAt(4, 2), NodeType.PASSABLE);
        nodeFactory.changeNodeToType(maze.getNodeAt(4, 3), NodeType.PASSABLE);
        nodeFactory.changeNodeToType(maze.getNodeAt(4, 4), NodeType.PASSABLE);

        maze.setStartNode(maze.getNodeAt(2, 1));
        maze.setEndNode(maze.getNodeAt(2, 5));

        // create Maze object
        return maze;
    }

    // create 6x6 maze with two parallel routes inside
    // | # | # | # | # | # | # | # | # |
    // | # | S | # | P | P | P | # | # |
    // | # | P | # | P | # | P | # | # |
    // | # | P | P | P | P | P | E | # |
    // | # | P | # | P | # | # | # | # |
    // | # | P | P | P | # | # | # | # |
    // | # | # | # | # | # | # | # | # |
    public static Maze getMazeWithTwoParallelRoutes() {
        // create node array
        NodeFactory.Node[][] maze = new NodeFactory.Node[7][8];
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);

        // init nodes of 0th row of maze
        maze[0][0] = nodeFactory.buildWallNode();
        maze[0][1] = nodeFactory.buildWallNode();
        maze[0][2] = nodeFactory.buildWallNode();
        maze[0][3] = nodeFactory.buildWallNode();
        maze[0][4] = nodeFactory.buildWallNode();
        maze[0][5] = nodeFactory.buildWallNode();
        maze[0][6] = nodeFactory.buildWallNode();
        maze[0][7] = nodeFactory.buildWallNode();

        // init nodes of 1th row of maze
        maze[1][0] = nodeFactory.buildWallNode();
        maze[1][1] = nodeFactory.buildStartNode();
        maze[1][2] = nodeFactory.buildWallNode();
        maze[1][3] = nodeFactory.buildWayNode();
        maze[1][4] = nodeFactory.buildWayNode();
        maze[1][5] = nodeFactory.buildWayNode();
        maze[1][6] = nodeFactory.buildWallNode();
        maze[1][7] = nodeFactory.buildWallNode();

        // init nodes of 2th row of maze
        maze[2][0] = nodeFactory.buildWallNode();
        maze[2][1] = nodeFactory.buildWayNode();
        maze[2][2] = nodeFactory.buildWallNode();
        maze[2][3] = nodeFactory.buildWayNode();
        maze[2][4] = nodeFactory.buildWallNode();
        maze[2][5] = nodeFactory.buildWayNode();
        maze[2][6] = nodeFactory.buildWallNode();
        maze[2][7] = nodeFactory.buildWallNode();

        // init nodes of 3th row of maze
        maze[3][0] = nodeFactory.buildWallNode();
        maze[3][1] = nodeFactory.buildWayNode();
        maze[3][2] = nodeFactory.buildWayNode();
        maze[3][3] = nodeFactory.buildWayNode();
        maze[3][4] = nodeFactory.buildWayNode();
        maze[3][5] = nodeFactory.buildWayNode();
        maze[3][6] = nodeFactory.buildEndNode();
        maze[3][7] = nodeFactory.buildWallNode();

        // init nodes of 4th row of maze
        maze[4][0] = nodeFactory.buildWallNode();
        maze[4][1] = nodeFactory.buildWayNode();
        maze[4][2] = nodeFactory.buildWallNode();
        maze[4][3] = nodeFactory.buildWayNode();
        maze[4][4] = nodeFactory.buildWallNode();
        maze[4][5] = nodeFactory.buildWallNode();
        maze[4][6] = nodeFactory.buildWallNode();
        maze[4][7] = nodeFactory.buildWallNode();

        // init nodes of 5th row of maze
        maze[5][0] = nodeFactory.buildWallNode();
        maze[5][1] = nodeFactory.buildWayNode();
        maze[5][2] = nodeFactory.buildWayNode();
        maze[5][3] = nodeFactory.buildWayNode();
        maze[5][4] = nodeFactory.buildWallNode();
        maze[5][5] = nodeFactory.buildWallNode();
        maze[5][6] = nodeFactory.buildWallNode();
        maze[5][7] = nodeFactory.buildWallNode();

        // init nodes of 6th row of maze
        maze[6][0] = nodeFactory.buildWallNode();
        maze[6][1] = nodeFactory.buildWallNode();
        maze[6][2] = nodeFactory.buildWallNode();
        maze[6][3] = nodeFactory.buildWallNode();
        maze[6][4] = nodeFactory.buildWallNode();
        maze[6][5] = nodeFactory.buildWallNode();
        maze[6][6] = nodeFactory.buildWallNode();
        maze[6][7] = nodeFactory.buildWallNode();

        // create Maze object
        return new Maze(nodeFactory, maze, maze[1][1], maze[3][6]);
    }

    public static Maze getPlainMaze(int xDim, int yDim) {
        // init Nodes
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);

        NodeFactory.Node[][] maze = new NodeFactory.Node[xDim][yDim];

        for (int x = 0; x < maze.length; x++) {
            for (int y = 0; y < maze[0].length; y++) {
                maze[x][y] = nodeFactory.buildWallNode();
            }
        }

        // create Maze
        return new Maze(nodeFactory, maze, null, null);
    }

    public static Maze getPlainMaze() {
        return getPlainMaze(5, 5);
    }
}
