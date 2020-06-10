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

import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

import static de.uni.ks.TestUtils.getPlainMaze;
import static de.uni.ks.maze.utils.mazeOperators.OperatorUtilsTest.getCenter;
import static org.junit.jupiter.api.Assertions.*;

class NewPathOperatorTest {

    @Test
    void testStartDepthFirstSearch() {
        // 1. path has len 0 if non can be found
        Maze m1 = getPlainMaze();
        NodeFactory.Node c1 = getCenter(m1);
        m1.getNodeFactory().changeNodeToType(c1.getUpperLeftNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1.getLeftNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1.getLowerLeftNeighbor(), NodeType.PASSABLE);
        m1.getNodeFactory().changeNodeToType(c1, NodeType.PASSABLE);

        NewPathOperator o1 = new NewPathOperator(4, 5, 5, 123);
        assertEquals(0,
                o1.startDepthFirstSearch(c1.getUpperLeftNeighbor(), m1, 5).size());

        //  2. path has maxLen as length
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        NodeFactory.Node[][] nodes = new NodeFactory.Node[5][6];

        for (int x = 0; x < nodes.length; x++) {
            for (int y = 0; y < nodes[0].length; y++) {
                nodes[x][y] =
                        x == 1 && 0 < y && y < nodes[0].length - 1
                                ? nodeFactory.buildWayNode() : nodeFactory.buildWallNode();
            }
        }

        Maze m2 = new Maze(nodeFactory, nodes, nodes[1][1], nodes[1][4]);

        NewPathOperator o2 = new NewPathOperator(5, 6, 5, 123);
        assertEquals(6, o2.startDepthFirstSearch(m2.getStartNode(), m2, 6).size());

        //  5. finds longest path possible in maze
        NewPathOperator o5 = new NewPathOperator(4, 99, 5, 123);
        assertEquals(6, o5.startDepthFirstSearch(nodes[1][1], m2, 6).size());

        //  6. path has len 0 if minLen is to high
        Maze m6 = getPlainMaze();
        NodeFactory.Node c6 = getCenter(m6);
        m6.getNodeFactory().changeNodeToType(c6.getUpperLeftNeighbor(), NodeType.PASSABLE);
        m6.getNodeFactory().changeNodeToType(c6.getLeftNeighbor(), NodeType.PASSABLE);
        m6.getNodeFactory().changeNodeToType(c6.getLowerLeftNeighbor(), NodeType.PASSABLE);

        NewPathOperator o6 = new NewPathOperator(20, 55, 5, 123);
        assertEquals(0,
                o6.startDepthFirstSearch(c6.getUpperLeftNeighbor(), m6, 5).size());

        //  7. finds path with maxLen and not any longer
        NewPathOperator o7 = new NewPathOperator(4, 5, 5, 123);
        assertEquals(5, o7.startDepthFirstSearch(nodes[1][1], m2, 5).size());

        //  3. path is connected
        Stack<NodeFactory.Node> path = o7.startDepthFirstSearch(nodes[1][1], m2, 5);

        assertTrue(nodes[1][1].getDirectNeighbors().contains(path.get(0))); // Beginning
        assertTrue(path.get(0).getDirectNeighbors().contains(path.get(1)));
        assertTrue(path.get(1).getDirectNeighbors().contains(path.get(2)));
        assertTrue(path.get(2).getDirectNeighbors().contains(path.get(3)));
        assertTrue(path.get(3).getDirectNeighbors().contains(path.get(4)));
        assertTrue(path.get(4).getDirectNeighbors().contains(nodes[1][3])); // End
    }

    @Test
    void testIsValidEndNode() {
        testValidConfigs();
        testInvalidConfigs();
    }

    private void testValidConfigs() {

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | P | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m1 = getPlainMaze();
        NodeFactory.Node c1 = getCenter(m1);
        m1.getNodeFactory().changeNodeToType(c1.getLeftNeighbor(), NodeType.PASSABLE);

        assertTrue(NewPathOperator.isValidEndOfPath(c1, c1.getRightNeighbor(),
                Collections.singletonList(c1.getRightNeighbor()), Collections.singletonList(c1.getLeftNeighbor()), m1));

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | P | # | P | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m2 = getPlainMaze();
        NodeFactory.Node c2 = getCenter(m2);
        m2.getNodeFactory().changeNodeToType(c2.getLeftNeighbor(), NodeType.PASSABLE);
        m2.getNodeFactory().changeNodeToType(c2.getRightNeighbor(), NodeType.PASSABLE);

        assertTrue(NewPathOperator.isValidEndOfPath(c2, c2.getRightNeighbor(),
                Collections.singletonList(c2.getRightNeighbor()), Collections.singletonList(c2.getLeftNeighbor()), m2));

        Maze m3 = getPlainMaze();
        NodeFactory.Node c3 = getCenter(m3);
        m3.getNodeFactory().changeNodeToType(c3.getLeftNeighbor(), NodeType.PASSABLE);
        m3.getNodeFactory().changeNodeToType(c3.getUpperLeftNeighbor(), NodeType.PASSABLE);

        assertTrue(NewPathOperator.isValidEndOfPath(c3, c3.getRightNeighbor(),
                Collections.singletonList(c3.getRightNeighbor()), Collections.singletonList(c3.getLeftNeighbor()), m3));

        Maze m4 = getPlainMaze();
        NodeFactory.Node c4 = getCenter(m4);
        m4.getNodeFactory().changeNodeToType(c4.getLeftNeighbor(), NodeType.PASSABLE);
        m4.getNodeFactory().changeNodeToType(c4.getUpperLeftNeighbor(), NodeType.PASSABLE);
        m4.getNodeFactory().changeNodeToType(c4.getRightNeighbor(), NodeType.PASSABLE);

        assertTrue(NewPathOperator.isValidEndOfPath(c4, c4.getRightNeighbor(),
                Collections.singletonList(c4.getRightNeighbor()), Collections.singletonList(c4.getLeftNeighbor()), m4));

        Maze m5 = getPlainMaze();
        NodeFactory.Node c5 = getCenter(m5);
        m5.getNodeFactory().changeNodeToType(c5.getLeftNeighbor(), NodeType.PASSABLE);

        m5.setEndNode(c5.getLeftNeighbor());

        assertTrue(NewPathOperator.isValidEndOfPath(c5, c5.getRightNeighbor(),
                Collections.singletonList(c5.getRightNeighbor()), Collections.singletonList(c5.getLeftNeighbor()), m5));

        Maze m6 = getPlainMaze();
        NodeFactory.Node c6 = getCenter(m6);
        m6.getNodeFactory().changeNodeToType(c6.getLeftNeighbor(), NodeType.PASSABLE);
        m6.getNodeFactory().changeNodeToType(c6.getRightNeighbor(), NodeType.PASSABLE);

        m6.setEndNode(c6.getLeftNeighbor());

        assertTrue(NewPathOperator.isValidEndOfPath(c6, c6.getRightNeighbor(),
                Collections.singletonList(c6.getRightNeighbor()), Collections.singletonList(c6.getLeftNeighbor()), m6));

    }

    private void testInvalidConfigs() {

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m1 = getPlainMaze();
        NodeFactory.Node c1 = getCenter(m1);

        assertFalse(NewPathOperator.isValidEndOfPath(c1, c1.getRightNeighbor(),
                Collections.singletonList(c1.getRightNeighbor()), Collections.singletonList(c1.getLeftNeighbor()), m1));

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | P | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m2 = getPlainMaze();
        NodeFactory.Node c2 = getCenter(m2);
        m2.getNodeFactory().changeNodeToType(c2.getRightNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c2, c2.getRightNeighbor(),
                Collections.singletonList(c2.getRightNeighbor()), Collections.singletonList(c2.getLeftNeighbor()), m2));

        //| # | # | # | # | # |
        //| # | P | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m3 = getPlainMaze();
        NodeFactory.Node c3 = getCenter(m3);
        m3.getNodeFactory().changeNodeToType(c3.getUpperLeftNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c3, c3.getRightNeighbor(),
                Collections.singletonList(c3.getRightNeighbor()), Collections.singletonList(c3.getUpperLeftNeighbor()),
                m3));

        //| # | # | # | # | # |
        //| # | P | # | # | # |
        //| # | # | # | P | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m4 = getPlainMaze();
        NodeFactory.Node c4 = getCenter(m4);
        m4.getNodeFactory().changeNodeToType(c4.getUpperLeftNeighbor(), NodeType.PASSABLE);
        m4.getNodeFactory().changeNodeToType(c4.getRightNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c4, c4.getRightNeighbor(),
                Collections.singletonList(c4.getRightNeighbor()), Collections.singletonList(c4.getUpperLeftNeighbor()),
                m4));

        //| # | # | # | # | # |
        //| # | # | P | # | # |
        //| # | P | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m6 = getPlainMaze();
        NodeFactory.Node c6 = getCenter(m6);
        m6.getNodeFactory().changeNodeToType(c6.getLeftNeighbor(), NodeType.PASSABLE);
        m6.getNodeFactory().changeNodeToType(c6.getUpperNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c6, c6.getRightNeighbor(),
                Collections.singletonList(c6.getRightNeighbor()), Collections.singletonList(c6.getLeftNeighbor()), m6));

        //| # | # | # | # | # |
        //| # | # | P | # | # |
        //| # | P | # | P | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m7 = getPlainMaze();
        NodeFactory.Node c7 = getCenter(m7);
        m7.getNodeFactory().changeNodeToType(c7.getLeftNeighbor(), NodeType.PASSABLE);
        m7.getNodeFactory().changeNodeToType(c7.getUpperNeighbor(), NodeType.PASSABLE);
        m7.getNodeFactory().changeNodeToType(c7.getRightNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c7, c7.getRightNeighbor(),
                Collections.singletonList(c7.getRightNeighbor()), Collections.singletonList(c7.getLeftNeighbor()), m7));

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | P | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m8 = getPlainMaze();
        NodeFactory.Node c8 = getCenter(m8);
        m8.getNodeFactory().changeNodeToType(c8.getLeftNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c8, c8.getUpperRightNeighbor(),
                Collections.singletonList(c8.getUpperRightNeighbor()), Collections.singletonList(c8.getLeftNeighbor()),
                m8));

        //| # | # | # | # | # |
        //| # | # | # | P | # |
        //| # | P | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m9 = getPlainMaze();
        NodeFactory.Node c9 = getCenter(m9);
        m9.getNodeFactory().changeNodeToType(c9.getLeftNeighbor(), NodeType.PASSABLE);
        m9.getNodeFactory().changeNodeToType(c9.getUpperRightNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c9, c9.getUpperRightNeighbor(),
                Collections.singletonList(c9.getUpperRightNeighbor()), Collections.singletonList(c9.getLeftNeighbor()),
                m9));

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | P | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m10 = getPlainMaze();
        NodeFactory.Node c10 = getCenter(m10);
        m10.getNodeFactory().changeNodeToType(c10.getLeftNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c10, c10.getRightNeighbor(),
                Arrays.asList(c10.getRightNeighbor(), c10.getUpperNeighbor()),
                Collections.singletonList(c10.getLeftNeighbor()), m10));

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | P | # | P | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m11 = getPlainMaze();
        NodeFactory.Node c11 = getCenter(m11);
        m11.getNodeFactory().changeNodeToType(c11.getLeftNeighbor(), NodeType.PASSABLE);
        m11.getNodeFactory().changeNodeToType(c11.getRightNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c11, c11.getRightNeighbor(),
                Arrays.asList(c11.getRightNeighbor(), c11.getUpperNeighbor()),
                Collections.singletonList(c11.getLeftNeighbor()), m11));

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | P | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m12 = getPlainMaze();
        NodeFactory.Node c12 = getCenter(m12);
        m12.getNodeFactory().changeNodeToType(c12.getLeftNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c12, c12.getRightNeighbor(),
                Arrays.asList(c12.getRightNeighbor(), c12), Collections.singletonList(c12.getLeftNeighbor()), m12));

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | P | # | P | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m13 = getPlainMaze();
        NodeFactory.Node c13 = getCenter(m13);
        m13.getNodeFactory().changeNodeToType(c13.getLeftNeighbor(), NodeType.PASSABLE);
        m13.getNodeFactory().changeNodeToType(c13.getRightNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c13, c13.getRightNeighbor(),
                Arrays.asList(c13.getRightNeighbor(), c13), Collections.singletonList(c13.getLeftNeighbor()), m13));

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | P | P | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m14 = getPlainMaze();
        NodeFactory.Node c14 = getCenter(m14);
        m14.getNodeFactory().changeNodeToType(c14.getLeftNeighbor(), NodeType.PASSABLE);
        m14.getNodeFactory().changeNodeToType(c14, NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c14, c14.getRightNeighbor(),
                Collections.singletonList(c14.getRightNeighbor()), Collections.singletonList(c14.getLeftNeighbor()),
                m14));

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | P | P | P | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m15 = getPlainMaze();
        NodeFactory.Node c15 = getCenter(m15);
        m15.getNodeFactory().changeNodeToType(c15.getLeftNeighbor(), NodeType.PASSABLE);
        m15.getNodeFactory().changeNodeToType(c15, NodeType.PASSABLE);
        m15.getNodeFactory().changeNodeToType(c15.getRightNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c15, c15.getRightNeighbor(),
                Collections.singletonList(c15.getRightNeighbor()), Collections.singletonList(c15.getLeftNeighbor()),
                m15));

        //| # | # | # | # | # |
        //| # | # | # | # | # |
        //| # | P | # | # | # |
        //| # | # | # | # | # |
        //| # | # | # | # | # |

        Maze m16 = getPlainMaze();
        NodeFactory.Node c16 = getCenter(m16);
        m16.getNodeFactory().changeNodeToType(c16.getLeftNeighbor(), NodeType.PASSABLE);

        assertFalse(NewPathOperator.isValidEndOfPath(c16, c16.getRightNeighbor(),
                Collections.singletonList(c16.getRightNeighbor()), Collections.emptyList(), m16));
    }

    @Test
    void testEstimateCost() {
        Maze maze = setupTestMaze();

        //  1. longest path
        NewPathOperator w2 = new NewPathOperator(7, 7, 5, 123);
        w2.estimateCost(maze, Double.POSITIVE_INFINITY);
        assertEquals(7, w2.getPath().size());
        //  2. longest path
        NewPathOperator w3 = new NewPathOperator(7, 99, 5, 123);
        w3.estimateCost(maze, Double.POSITIVE_INFINITY);
        assertEquals(7, w3.getPath().size());
        //  3. 0 when cost to low
        NewPathOperator w4 = new NewPathOperator(5, 7, 5, 123);
        assertEquals(0, w4.estimateCost(maze, 0));
    }

    @Test
    void testChangeMaze() {
        Maze maze = setupTestMaze();

        //  1. False when estimateCost was not called
        NewPathOperator o1 = new NewPathOperator(4, 9, 5, 123);
        assertFalse(o1.changeMaze(maze));

        //  2. False when no path is found
        o1.estimateCost(maze, Double.NEGATIVE_INFINITY);
        assertFalse(o1.changeMaze(maze));

        //  3. True when path was found
        o1.estimateCost(maze, Double.POSITIVE_INFINITY);
        assertTrue(o1.changeMaze(maze));

        //  4. False after second call
        assertFalse(o1.changeMaze(maze));

        //  5. shortest path in maze is still the same length / does not contain node of the parallel way
        Maze maze1 = setupTestMaze();
        NewPathOperator o2 = new NewPathOperator(4, 99, 5, 123);
        int len1 = maze1.getShortestPath().size();
        int num1 = maze1.getAllPassableNodes().size();
        o2.estimateCost(maze1, Double.POSITIVE_INFINITY);
        Stack<NodeFactory.Node> path = o2.getPath();
        o2.changeMaze(maze);
        assertEquals(len1, maze1.getShortestPath().size());

        // Path was changed in the maze.
        assertEquals(num1 + path.size(), maze1.getAllPassableNodes().size());
    }

    @Test
    void testOptimalPathDoesNotGetShorter() {
        Maze maze = getPlainMaze(8, 5);
        for (int y = 1; y < maze.getMaze()[0].length - 1; y++) {
            maze.getNodeFactory().changeNodeToType(maze.getNodeAt(1, y), NodeType.PASSABLE);
        }
        for (int y = 1; y < maze.getMaze()[0].length - 1; y++) {
            maze.getNodeFactory().changeNodeToType(maze.getNodeAt(maze.getMaze().length - 2, y), NodeType.PASSABLE);
        }
        for (int x = 1; x < maze.getMaze().length - 1; x++) {
            maze.getNodeFactory().changeNodeToType(maze.getNodeAt(x, maze.getMaze()[0].length - 2), NodeType.PASSABLE);
        }
        maze.setStartNode(maze.getNodeAt(1, 1));
        maze.setEndNode(maze.getNodeAt(maze.getMaze().length - 2, 1));

        int lenBefore1 = maze.getLengthOfShortestPath();

        NewPathOperator w1 = new NewPathOperator(4, 4, 1, 123);
        w1.estimateCost(maze, Double.POSITIVE_INFINITY);
        w1.changeMaze(maze);

        // No path can be build here because every possible path is shorter than the current optimal path.
        assertEquals(lenBefore1, maze.getLengthOfShortestPath());

        Maze maze2 = setupTestMaze();
        int lenBefore2 = maze2.getLengthOfShortestPath();
        w1.estimateCost(maze2, Double.POSITIVE_INFINITY);
        w1.changeMaze(maze2);
        assertEquals(lenBefore2, maze2.getLengthOfShortestPath());
    }

    private Maze setupTestMaze() {
        Maze plainMaze = getPlainMaze(5, 7);
        for (int y = 1; y < plainMaze.getMaze()[0].length - 1; y++) {
            plainMaze.getNodeFactory().changeNodeToType(plainMaze.getNodeAt(1, y), NodeType.PASSABLE);
        }
        plainMaze.setStartNode(plainMaze.getNodeAt(1, 1));
        plainMaze.setEndNode(plainMaze.getNodeAt(1, plainMaze.getMaze()[0].length - 2));

        return plainMaze;
    }
}
