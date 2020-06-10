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

import java.util.List;

/**
 * The class implements methods that are used by different {@link MazeOperator}.
 */
public class OperatorUtils {

    public static boolean isValidNewWay(NodeFactory.Node node, NodeFactory.Node predecessor,
                                        List<NodeFactory.Node> visited, Maze maze) {
        return isValidNewWay(node, predecessor, visited, maze, false);
    }

    /**
     * A given <code>NodeFactory.Node</code> is a valid part for a new dead and if it fulfils
     * the following conditions:
     * - the predecessor is visited
     * - non of the direct neighbors is a passable node
     * (the predecessor can be passable if it is the "start" of the path)
     * - non of the direct neighbors is visited (except the predecessor)
     * - the current node is not passable
     * - non of the neighbors is an end node
     * - the node is not already visited
     * - the node is not at the border of the maze.
     *
     * @param node                   The <code>NodeFactory.Node</code> to evaluate.
     * @param visited                The list of nodes that were already visited.
     * @param predecessor            The predecessor of node.
     * @param maze                   The maze the new way is created in
     * @param allowPassableNeighbors If true, a node is still valid if it has other passable neighbors than
     *                               the predecessor.
     * @return true if node fulfils the above conditions, false if not
     */
    public static boolean isValidNewWay(NodeFactory.Node node, NodeFactory.Node predecessor,
                                        List<NodeFactory.Node> visited, Maze maze, boolean allowPassableNeighbors) {

        if (visited.contains(node)) {
            return false;
        }

        if (node.getNodeType().equals(NodeType.PASSABLE)) {
            return false;
        }

        if (!node.getDirectNeighbors().contains(predecessor)) {
            return false;
        }

        if (node.getXPos() == 0 || node.getYPos() == 0
                || node.getXPos() == maze.getMaze().length - 1
                || node.getYPos() == maze.getMaze()[0].length - 1) {
            return false;
        }

        for (NodeFactory.Node n : node.getDirectNeighbors()) {
            if ((n.getNodeType().equals(NodeType.PASSABLE) && !n.equals(predecessor) && !allowPassableNeighbors)
                    || (visited.contains(n) && !n.equals(predecessor))) {
                return false;
            }
        }

        return visited.contains(predecessor);
    }
}
