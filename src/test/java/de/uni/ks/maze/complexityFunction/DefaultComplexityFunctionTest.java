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
package de.uni.ks.maze.complexityFunction;

import de.uni.ks.TestUtils;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.NodeType;
import de.uni.ks.maze.utils.MazeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static de.uni.ks.TestUtils.getPlainMaze;

class DefaultComplexityFunctionTest {

    // #############################################################################
    //                            Complexity of parallel routes
    // #############################################################################
    @Test
    void testComplexityOfParallelRoutes() {

        DefaultComplexityFunction complexityFunction = new DefaultComplexityFunction();
        double COMPLEXITY_PARALLEL_ROUTE_NODE = DefaultComplexityFunction.COMPLEXITY_PARALLEL_ROUTE_NODE;
        // test complexity of maze with one parallel route
        // | # | # | # | # | # | # |
        // | # | S | # | # | # | # |
        // | # | P | P | P | E | # |
        // | # | P | # | P | # | # |
        // | # | P | P | P | # | # |
        // | # | # | # | # | # | # |
        Maze maze0 = TestUtils.getMazeWithOneParallelRoute();
        MazeUtils.printMazeToTerminal(maze0);
        Assertions.assertEquals(5.0d * COMPLEXITY_PARALLEL_ROUTE_NODE, complexityFunction.calculateComplexityOfParallelRoutes(maze0));

        // test complexity of maze with two parallel routes
        // | # | # | # | # | # | # | # | # |
        // | # | S | # | P | P | P | # | # |
        // | # | P | # | P | # | P | # | # |
        // | # | P | P | P | P | P | E | # |
        // | # | P | # | P | # | # | # | # |
        // | # | P | P | P | # | # | # | # |
        // | # | # | # | # | # | # | # | # |
        Maze maze1 = TestUtils.getMazeWithTwoParallelRoutes();
        Assertions.assertEquals(10.0d * COMPLEXITY_PARALLEL_ROUTE_NODE, complexityFunction.calculateComplexityOfParallelRoutes(maze1));


        // Maze with a parallel route and dead ends:
        // | # | # | # | # | # | # | # | # | # |
        // | # | # | # | # | # | # | E | # | # |
        // | # | # | # | # | P | P | P | # | # |
        // | # | # | P | # | # | # | P | # | # |
        // | # | S | P | P | P | P | P | P | # |
        // | # | # | # | # | P | # | # | P | # |
        // | # | # | # | P | P | P | P | P | # |
        // | # | # | # | # | # | # | # | # | # |
        Maze maze2 = getPlainMaze(8, 9);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(1, 6), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(2, 4), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(2, 5), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(2, 6), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(3, 2), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(3, 6), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(4, 1), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(4, 2), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(4, 3), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(4, 4), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(4, 5), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(4, 6), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(4, 7), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(5, 7), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(5, 4), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(6, 3), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(6, 4), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(6, 5), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(6, 6), NodeType.PASSABLE);
        maze2.getNodeFactory().changeNodeToType(maze2.getNodeAt(6, 7), NodeType.PASSABLE);
        maze2.setStartNode(maze2.getNodeAt(4, 1));
        maze2.setEndNode(maze2.getNodeAt(1, 6));
        Assertions.assertEquals(7.0d * COMPLEXITY_PARALLEL_ROUTE_NODE, complexityFunction.calculateComplexityOfParallelRoutes(maze2));


        // Maze with composed parallel routes and dead ends:
        // | # | # | # | # | # | # | # | # | # |
        // | # | # | P | P | P | # | E | # | # |
        // | # | # | P | # | P | P | P | # | # |
        // | # | # | P | # | # | # | P | # | # |
        // | # | S | P | P | P | P | P | P | # |
        // | # | # | # | # | P | # | # | P | # |
        // | # | # | # | P | P | P | P | P | # |
        // | # | # | # | # | # | # | # | # | # |
        Maze maze3 = getPlainMaze(8, 9);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(1, 2), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(1, 3), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(1, 4), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(1, 6), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(2, 2), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(2, 4), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(2, 5), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(2, 6), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(3, 2), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(3, 6), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(4, 1), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(4, 2), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(4, 3), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(4, 4), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(4, 5), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(4, 6), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(4, 7), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(5, 7), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(5, 4), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(6, 3), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(6, 4), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(6, 5), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(6, 6), NodeType.PASSABLE);
        maze3.getNodeFactory().changeNodeToType(maze3.getNodeAt(6, 7), NodeType.PASSABLE);
        maze3.setStartNode(maze3.getNodeAt(4, 1));
        maze3.setEndNode(maze3.getNodeAt(1, 6));
        Assertions.assertEquals(14.0d * COMPLEXITY_PARALLEL_ROUTE_NODE, complexityFunction.calculateComplexityOfParallelRoutes(maze3));
    }


    // #############################################################################
    //                            Complexity of dead ends
    // #############################################################################

    @Test
    void testComplexityOfNodes() {

        DefaultComplexityFunction complexityFunction
                = new DefaultComplexityFunction();

        //        # # #
        //        w w w
        //        # # #
        Maze m1 = getPlainMaze();
        NodeFactory.Node c1 = m1.getNodeAt(2, 2);
        m1.getNodeFactory().changeNodeToType(c1.getLeftNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1, NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1.getRightNeighbor(), NodeType.PASSABLE);

        Assertions.assertEquals(DefaultComplexityFunction.COMPLEXITY_DEAD_END_NODE,
                complexityFunction.calculateComplexityOfNode(c1, 0));
        Assertions.assertEquals(DefaultComplexityFunction.COMPLEXITY_DEAD_END_NODE,
                complexityFunction.calculateComplexityOfNode(c1, 1));
        Assertions.assertEquals(DefaultComplexityFunction.COMPLEXITY_DEAD_END_NODE,
                complexityFunction.calculateComplexityOfNode(c1, 2));

        //        # w #
        //        w w w
        //        # # #
        Maze m2 = getPlainMaze();
        NodeFactory.Node c2 = m2.getNodeAt(2, 2);
        m2.getNodeFactory().changeNodeToType(c2.getLeftNeighbor(), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(c2, NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(c2.getRightNeighbor(), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(c2.getUpperNeighbor(), NodeType.PASSABLE);


        Assertions.assertEquals(DefaultComplexityFunction.COMPLEXITY_THREE_WAY_JUNCTION
                * (1 + (Math.exp(0 * 0.25) - 1)), complexityFunction.calculateComplexityOfNode(c2, 0));
        Assertions.assertEquals(DefaultComplexityFunction.COMPLEXITY_THREE_WAY_JUNCTION
                * (1 + (Math.exp(1 * 0.25) - 1)), complexityFunction.calculateComplexityOfNode(c2, 1));
        Assertions.assertEquals(DefaultComplexityFunction.COMPLEXITY_THREE_WAY_JUNCTION
                * (1 + (Math.exp(2 * 0.25) - 1)), complexityFunction.calculateComplexityOfNode(c2, 2));

        //        # w #
        //        w w w
        //        # w #
        Maze m3 = getPlainMaze();
        NodeFactory.Node c3 = m3.getNodeAt(2, 2);
        m3.getNodeFactory().changeNodeToType(c3.getLeftNeighbor(), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(c3, NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(c3.getRightNeighbor(), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(c3.getUpperNeighbor(), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(c3.getLowerNeighbor(), NodeType.PASSABLE);

        Assertions.assertEquals(DefaultComplexityFunction.COMPLEXITY_FOUR_WAY_JUNCTION,
                complexityFunction.calculateComplexityOfNode(c3, 0));
        Assertions.assertEquals(DefaultComplexityFunction.COMPLEXITY_FOUR_WAY_JUNCTION
                * (1 + (Math.exp(1 * 0.25) - 1)), complexityFunction.calculateComplexityOfNode(c3, 1));
        Assertions.assertEquals(DefaultComplexityFunction.COMPLEXITY_FOUR_WAY_JUNCTION
                * (1 + (Math.exp(2 * 0.25) - 1)), complexityFunction.calculateComplexityOfNode(c3, 2));
    }

    @Test
    void testCalculateComplexityOfBranch() {

        DefaultComplexityFunction complexityFunction
                = new DefaultComplexityFunction();

        // | # | # | # | # | # |
        // | # | # | P | # | # |
        // | # | P | P | P | # |
        // | # | # | # | # | # |
        // | # | # | # | # | # |

        Maze m1 = getPlainMaze();
        NodeFactory.Node c1 = m1.getNodeAt(2, 2);
        m1.getNodeFactory().changeNodeToType(c1.getLeftNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1, NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1.getRightNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1.getUpperNeighbor(), NodeType.PASSABLE);

        Assertions.assertEquals(3 * complexityFunction.calculateComplexityOfNode(c1.getLeftNeighbor(), 0)
                        + complexityFunction.calculateComplexityOfNode(c1, 0),
                complexityFunction.calculateComplexityOfBranch(c1.getLeftNeighbor(), Collections.emptySet()));

        // | # | # | # | # | # | # | # | # | # | # | # |
        // | # | # | # | # | # | P | P | # | # | # | # |
        // | # | # | # | P | P | P | # | P | # | # | # |
        // | # | P | P | P | # | P | P | P | P | P | # |
        // | # | # | # | # | # | # | # | P | # | # | # |
        // | # | # | # | # | # | # | # | # | # | # | # |

        Maze m2 = getPlainMaze(6, 11);

        NodeFactory.Node startOfBranch = m2.getNodeAt(3, 1);

        m2.getNodeFactory().changeNodeToType(startOfBranch, NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(3, 2), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(3, 3), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(2, 3), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(2, 4), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(2, 5), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(1, 5), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(1, 6), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(3, 5), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(3, 6), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(3, 7), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(3, 8), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(3, 9), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(2, 7), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(4, 7), NodeType.PASSABLE);

        // 13 normal nodes
        // 1 three-way junction
        // 1 four-way junction

        Assertions.assertEquals(13 * complexityFunction.calculateComplexityOfNode(startOfBranch, 0)
                        + complexityFunction.calculateComplexityOfNode(m2.getNodeAt(2, 5), 0)
                        + complexityFunction.calculateComplexityOfNode(m2.getNodeAt(3, 7), 1),
                complexityFunction.calculateComplexityOfBranch(startOfBranch, Collections.emptySet()));

    }

    @Test
    void testCalculateComplexityOfDeadEnds() {

        DefaultComplexityFunction complexityFunction
                = new DefaultComplexityFunction();

        // Maze without a parallel route:
        // | # | # | # | # | # | # | # | # | # |
        // | # | # | # | # | # | # | E | # | # |
        // | # | # | # | # | P | P | P | # | # |
        // | # | # | P | # | # | # | P | # | # |
        // | # | S | P | P | P | P | P | P | # |
        // | # | # | # | # | P | # | # | P | # |
        // | # | # | # | P | P | P | # | # | # |
        // | # | # | # | # | # | # | # | # | # |

        Maze m1 = getPlainMaze(8, 9);

        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(4, 1), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(4, 2), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(4, 3), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(4, 4), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(4, 5), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(4, 6), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(4, 7), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(2, 4), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(2, 5), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(1, 6), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(2, 6), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(3, 6), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(5, 7), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(3, 2), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(5, 4), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(6, 3), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(6, 4), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(m1.getNodeAt(6, 5), NodeType.PASSABLE);

        m1.setStartNode(m1.getNodeAt(4, 1));
        m1.setEndNode(m1.getNodeAt(1, 6));

        // 8 normal nodes
        // 1 three-way junctions
        // 0 four-way junctions

        Assertions.assertEquals(8 * complexityFunction.calculateComplexityOfNode(m1.getNodeAt(3, 2), 0)
                        + complexityFunction.calculateComplexityOfNode(m1.getNodeAt(6, 4), 0),
                complexityFunction.calculateComplexityOfDeadEnds(m1));

        // Maze with a parallel route:
        // | # | # | # | # | # | # | # | # | # |
        // | # | # | # | # | # | # | E | # | # |
        // | # | # | # | # | P | P | P | # | # |
        // | # | # | P | # | # | # | P | # | # |
        // | # | S | P | P | P | P | P | P | # |
        // | # | # | # | # | P | # | # | P | # |
        // | # | # | # | P | P | P | P | P | # |
        // | # | # | # | # | # | # | # | # | # |

        Maze m2 = getPlainMaze(8, 9);

        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(4, 1), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(4, 2), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(4, 3), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(4, 4), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(4, 5), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(4, 6), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(4, 7), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(2, 4), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(2, 5), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(1, 6), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(2, 6), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(3, 6), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(5, 7), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(3, 2), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(5, 4), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(6, 3), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(6, 4), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(6, 5), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(6, 6), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(m2.getNodeAt(6, 7), NodeType.PASSABLE);

        m2.setStartNode(m2.getNodeAt(4, 1));
        m2.setEndNode(m2.getNodeAt(1, 6));

        // 4 normal nodes
        // 0 three-way junctions
        // 0 four-way junctions

        Assertions.assertEquals(4 * complexityFunction.calculateComplexityOfNode(m2.getNodeAt(3, 2), 0),
                complexityFunction.calculateComplexityOfDeadEnds(m2));

        // Maze with a parallel route:
        // | # | # | # | # | # | # | # | # | # |
        // | # | P | P | P | P | P | # | # | # |
        // | # | # | P | # | P | # | # | # | # |
        // | # | # | P | # | P | P | # | # | # |
        // | # | # | P | P | P | # | # | # | # |
        // | # | # | P | # | # | # | # | # | # |
        // | # | S | P | P | P | P | P | E | # |
        // | # | # | # | # | # | # | # | # | # |

        Maze m3 = getPlainMaze(8, 9);

        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(6, 2), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(6, 3), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(6, 4), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(6, 5), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(6, 6), NodeType.PASSABLE);

        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(5, 2), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(4, 2), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(3, 2), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(2, 2), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(1, 2), NodeType.PASSABLE);

        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(1, 1), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(1, 3), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(1, 4), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(1, 5), NodeType.PASSABLE);

        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(2, 4), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(3, 4), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(4, 4), NodeType.PASSABLE);

        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(4, 3), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(m3.getNodeAt(3, 5), NodeType.PASSABLE);

        m3.setStartNode(m3.getNodeAt(6, 1));
        m3.setEndNode(m3.getNodeAt(6, 7));

        MazeUtils.printMazeToTerminal(m3);

        // 10 normal nodes
        // 4 three-way junctions
        // 0 four-way junctions

        Assertions.assertEquals(10 * DefaultComplexityFunction.COMPLEXITY_DEAD_END_NODE
                        + 1 * DefaultComplexityFunction.COMPLEXITY_THREE_WAY_JUNCTION
                        + 2 * complexityFunction.calculateComplexityOfNode(m3.getNodeAt(1, 2), 1)
                        + 1 * complexityFunction.calculateComplexityOfNode(m3.getNodeAt(1, 2), 2),
                complexityFunction.calculateComplexityOfDeadEnds(m3));
    }

}