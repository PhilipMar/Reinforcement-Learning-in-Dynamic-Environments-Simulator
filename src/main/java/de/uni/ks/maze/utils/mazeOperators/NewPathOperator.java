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

import de.uni.ks.logging.Logger;
import de.uni.ks.logging.messages.GuiMessageType;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.NodeType;
import de.uni.ks.maze.utils.MazeUtils;

import java.util.*;

import static de.uni.ks.maze.utils.mazeOperators.OperatorUtils.isValidNewWay;

/**
 * Operator that builds parallel paths in the maze. Such a path connects two nodes of the {@link Maze#getShortestPath()}
 * of the maze. The new path will never make the optimal path of the maze shorter.
 */
public class NewPathOperator implements MazeOperator {

    private final static int NUM_OF_PATHS_TO_FIND = 20;

    private Stack<NodeFactory.Node> path;

    private int minPathLen;
    private int maxPathLen;
    private double costPerNode;
    private final Random random;
    private final int seed;

    public NewPathOperator(int minPathLen, int maxPathLen, double costPerNode, int seed) {

        if (costPerNode <= 0) {
            throw new IllegalArgumentException("Parameter [costPerNode] must be greater than 0.");
        }

        if (minPathLen <= 3) {
            throw new IllegalArgumentException("Parameter [minPathLen] must be greater than 3.");
        }

        if (maxPathLen < minPathLen) {
            throw new IllegalArgumentException("Parameter [maxPathLen] must be greater than, or equal " +
                    "to parameter [minPathLen].");
        }

        this.minPathLen = minPathLen;
        this.maxPathLen = maxPathLen;
        this.costPerNode = costPerNode;
        this.seed = seed;
        this.random = new Random(seed);
    }

    @Override
    public boolean changeMaze(Maze maze) {

        if (path == null) {
            return false;
        }

        Logger.addTextToGuiLog("Apply new path operator (length = " + path.size() + ")", GuiMessageType.Maze);
        Logger.addTextToMiscLogOfCurrentTraining("Apply new path operator (length = " + path.size() + ")");
        Logger.addTextToMiscLogOfCurrentLevel("Apply new path operator (length = " + path.size() + ")");

        path.forEach(node -> maze.getNodeFactory().changeNodeToType(node, NodeType.PASSABLE));

        // Reset path.
        path = null;

        return true;
    }

    /**
     * Evaluates if the operator can be used on the current maze. Finds a random path in the maze that
     * is at least {@link #minPathLen} long and not longer than {@link #maxPathLen}. Paths can start and end at any
     * node that is on the {@link Maze#getShortestPath()} of the {@code maze}. From all possible paths, one is randomly
     * selected. To reduce the computational cost of this method, the search for new paths will stop after finding
     * {@link #NUM_OF_PATHS_TO_FIND} valid candidates.
     *
     * @param maze        The maze for which the cost is estimated.
     * @param allowedCost The cost the operator is allowed to produce for its operation.
     * @return The cost of the path that would be build when calling {@link #changeMaze(Maze)}. 0 if no path is possible.
     */
    @Override
    public double estimateCost(Maze maze, double allowedCost) {

        // Reset path
        path = null;

        if (minPathLen * costPerNode > allowedCost) {
            return 0;
        }

        int thisMaxPathLen = Math.min((int) (allowedCost / costPerNode), maxPathLen);

        List<NodeFactory.Node> shortestPath = maze.getShortestPath();

        Collections.shuffle(shortestPath, random); // We do not want to have a bias for nodes at the beginning.

        List<Stack<NodeFactory.Node>> paths = new ArrayList<>();

        for (NodeFactory.Node node : shortestPath) {

            Stack<NodeFactory.Node> currentPath = startDepthFirstSearch(node, maze, thisMaxPathLen);

            if (minPathLen <= currentPath.size() && currentPath.size() <= maxPathLen) {

                // Verify that the new path does not make the current optimal path shorter.
                NodeFactory.Node start = currentPath.firstElement();
                NodeFactory.Node end = currentPath.lastElement();
                int shortestPathLen = MazeUtils.getShortestPath(maze, getConnector(start, maze),
                        getConnector(end, maze)).size();

                if (currentPath.size() > shortestPathLen) {
                    paths.add(currentPath);
                }
            }

            // Stop the search prematurely to reduce computational cost.
            if (paths.size() >= NUM_OF_PATHS_TO_FIND) {
                break;
            }
        }

        if (paths.isEmpty()) {
            return 0;
        }

        this.path = paths.get(random.nextInt(paths.size()));
        return path.size() * costPerNode;
    }

    /**
     * Wraps {@link #depthFirstSearch(NodeFactory.Node, List, Maze, int, int)} with default values for {@code visited}
     * and {@code recursionCounter}.
     *
     * @param start      The node to start from in the current recursive action.
     * @param maze       The maze in which the search is performed.
     * @param maxPathLen The number of recursive calls at which the search stops.
     * @return The longest path that can be found in the maze when starting at {@code start}. {@link Stack#empty()} if
     * non can be found.
     */
    public Stack<NodeFactory.Node> startDepthFirstSearch(NodeFactory.Node start, Maze maze, int maxPathLen) {
        Stack<NodeFactory.Node> path = depthFirstSearch(start, new ArrayList<>(), maze, 0, maxPathLen);
        path.remove(start); // Because the start node is already passable.

        if (path.size() < minPathLen) {
            return new Stack<>();
        }

        return path;
    }

    /**
     * Performs depth first search in the given maze, by walking through the graph recursively.
     *
     * @param start          The node to start from in the current recursive action.
     * @param visited        The nodes that are already visited.
     * @param maze           The maze in which the search is performed.
     * @param currentPathLen The length of the currently viewed path respectively the current depth of the recursion.
     * @param maxPathLen     The number of recursive calls at which the search stops.
     * @return The longest path that can be found in the maze when starting at {@code start}. {@link Stack#empty()} if
     * non can be found.
     */
    private Stack<NodeFactory.Node> depthFirstSearch(NodeFactory.Node start, List<NodeFactory.Node> visited,
                                                     Maze maze, int currentPathLen, int maxPathLen) {

        Stack<NodeFactory.Node> path = new Stack<>();
        path.add(start);

        visited.add(start);

        // Recursively call DFS on all neighbors
        List<Stack<NodeFactory.Node>> possiblePaths = new ArrayList<>();
        for (NodeFactory.Node node : start.getDirectNeighbors()) {
            if (currentPathLen < maxPathLen - 1
                    && !visited.contains(node) && isValidNewWay(node, start, visited, maze)) {

                Stack<NodeFactory.Node> currentPath = depthFirstSearch(node, visited, maze,
                        currentPathLen + 1, maxPathLen);

                possiblePaths.add(currentPath);

                if (currentPath.size() == maxPathLen) {
                    break;
                }
            }
        }

        if (possiblePaths.isEmpty()
                && currentPathLen + 1 >= minPathLen) {

            // Try to find valid end point here.
            NodeFactory.Node endNode = null;
            for (NodeFactory.Node node : start.getDirectNeighbors()) {
                if (isValidEndOfPath(node, start, visited, maze)) {
                    endNode = node;
                    break;
                }
            }

            if (endNode != null) {
                path.add(endNode);
                return path;
            }

            return new Stack<>();

        } else if (possiblePaths.isEmpty()) { // && minPathLen was not reached.
            return new Stack<>();
        } else {
            // Find the longest path
            Stack<NodeFactory.Node> longestSubPath = null;
            for (Stack<NodeFactory.Node> p : possiblePaths) {
                if (longestSubPath == null || p.size() > longestSubPath.size()) {
                    longestSubPath = p;
                }
            }
            if (longestSubPath.isEmpty()) {// && currentPathLen < minPathLen
                return new Stack<>();
            }

            path.addAll(longestSubPath);
            return path;
        }
    }

    /**
     * Wraps {@link #isValidEndOfPath(NodeFactory.Node, NodeFactory.Node, List, List, Maze)} with a default value for
     * the parameter {@code List}.
     */
    private static boolean isValidEndOfPath(NodeFactory.Node node, NodeFactory.Node predecessor,
                                            List<NodeFactory.Node> visited,
                                            Maze maze) {
        return isValidEndOfPath(node, predecessor, visited, maze.getShortestPath(), maze);
    }

    /**
     * Evaluates if a given node is a valid end for a new way. That is the case if:
     * - The node is a valid path node {@link OperatorUtils#isValidNewWay
     * (NodeFactory.Node, NodeFactory.Node, List, Maze, boolean)}.
     * - The node has exactly one passable neighbor that is part of a given list.
     *
     * @param node        The node to evaluate.
     * @param predecessor The predecessor of this node in the current paht.
     * @param visited     List of visited nodes in the current search.
     * @param list        One of the node's neighbors must be in this list.
     * @param maze        The maze in which the node lies.
     * @return True if the node matches all of the above constraints, false if not.
     */
    protected static boolean isValidEndOfPath(NodeFactory.Node node, NodeFactory.Node predecessor,
                                              List<NodeFactory.Node> visited, List<NodeFactory.Node> list,
                                              Maze maze) {
        boolean isValidNewWay = isValidNewWay(node, predecessor, visited, maze, true);

        if (!isValidNewWay) {
            return false;
        }

        List<NodeFactory.Node> passableNeighbors = node.getPassableNeighbors();
        passableNeighbors.remove(predecessor);
        if (passableNeighbors.size() != 1) return false;
        return list.contains(passableNeighbors.get(0));
    }

    /**
     * Returns the neighbor node of the given node that is part of the optimal path in the maze.
     *
     * @param node The node whose neighbor should be determined.
     * @param maze The maze in which the optimal path lays.
     * @return The neighbor of {@code node} that is part of the optimal path, {@code null} if no such neighbor exists.
     */
    private NodeFactory.Node getConnector(NodeFactory.Node node, Maze maze) {

        NodeFactory.Node connector = null;

        List<NodeFactory.Node> shortestPath = maze.getShortestPath();
        for (NodeFactory.Node n : node.getDirectNeighbors()) {
            if (shortestPath.contains(n)) {
                connector = n;
                break;
            }
        }

        return connector;
    }

    public Stack<NodeFactory.Node> getPath() {
        return path;
    }

    public double getCostPerNode() {
        return costPerNode;
    }

    @Override
    public String myConfigString() {
        return this.getClass().getSimpleName() +
                "("
                + "minPathLen = " + minPathLen + ","
                + "maxPathLen = " + maxPathLen + ","
                + "costPerNode = " + costPerNode + ","
                + "seed = " + seed
                + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewPathOperator that = (NewPathOperator) o;
        return minPathLen == that.minPathLen &&
                maxPathLen == that.maxPathLen &&
                Double.compare(that.getCostPerNode(), getCostPerNode()) == 0 &&
                seed == that.seed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minPathLen, maxPathLen, getCostPerNode(), seed);
    }
}
