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
import java.util.stream.Collectors;

import static de.uni.ks.maze.utils.mazeOperators.OperatorUtils.isValidNewWay;

/**
 * This operator creates dead ends in the maze. A dead end can begin at any {@link de.uni.ks.maze.NodeFactory.Node} in
 * the {@link Maze} except for the end node.
 */
public class DeadEndOperator implements MazeOperator {

    private final static int NUM_OF_DEAD_ENDS_TO_FIND = 20;

    private double optimalPathAndParallelRoutesPreferencePercentage;
    private int minPathLen;
    private int maxPathLen;
    private final Random random;
    private final int seed;

    private double costPerNode;

    Stack<NodeFactory.Node> deadEnd;

    /**
     * Constructor of this operator.
     *
     * @param minPathLen                                       The minimal length of a path that can be constructed by this
     *                                                         operator. Must be greater than 0.
     * @param maxPathLen                                       The maximal length of a path that can be constructed by this
     *                                                         operator. Must be greater than 0
     *                                                         and greater than {@code minPathLen}.
     * @param costPerNode                                      The cost of a single dead end node.
     * @param optimalPathAndParallelRoutesPreferencePercentage The higher the value, the more the operator prefers to choose
     *                                                         dead ends.
     *                                                         starting at the optimal path of a maze.
     * @param seed                                             Used for random decisions.
     */
    public DeadEndOperator(int minPathLen, int maxPathLen, double costPerNode,
                           double optimalPathAndParallelRoutesPreferencePercentage, int seed) {

        if (costPerNode <= 0) {
            throw new IllegalArgumentException("Value for parameter [costPerNode] must be greater than 0.");
        }

        if (0 > optimalPathAndParallelRoutesPreferencePercentage || optimalPathAndParallelRoutesPreferencePercentage > 1) {
            System.err.println(optimalPathAndParallelRoutesPreferencePercentage);
            throw new IllegalArgumentException("Value for parameter [optimalPathAndParallelRoutesPreferencePercentage] must be in [0,1]!");
        }

        if (minPathLen < 0
                || maxPathLen < minPathLen) {
            throw new IllegalArgumentException("Parameters minPathLen and maxPathLen are not greater than 0, or" +
                    "minPathLen is greater than maxPathLen.");
        }

        this.seed = seed;
        this.optimalPathAndParallelRoutesPreferencePercentage = optimalPathAndParallelRoutesPreferencePercentage;
        this.random = new Random(seed);
        this.costPerNode = costPerNode;
        this.minPathLen = minPathLen;
        this.maxPathLen = maxPathLen;
    }

    @Override
    public boolean changeMaze(Maze maze) {

        if (deadEnd == null) {
            return false;
        }

        Logger.addTextToGuiLog("Apply dead end operator (length = " + deadEnd.size() + ")", GuiMessageType.Maze);
        Logger.addTextToMiscLogOfCurrentTraining("Apply dead end operator (length = " + deadEnd.size() + ")");
        Logger.addTextToMiscLogOfCurrentLevel("Apply dead end operator (length = " + deadEnd.size() + ")");

        deadEnd.forEach(node ->
                maze.getNodeFactory().changeNodeToType(node, NodeType.PASSABLE));

        deadEnd = null;

        return true;
    }

    /**
     * Estimates the cost using this operator, also finds a random dead end with that is at least {@link #minPathLen}
     * long but not longer than {@link #maxPathLen}. To reduce the computational cost of this method, the search for
     * dead ends will stop after finding {@link #NUM_OF_DEAD_ENDS_TO_FIND} valid candidates.
     *
     * @param maze The maze for which the cost is estimated.
     * @return The cost of the dead end this operator would build when {@link #changeMaze(Maze)} was called.
     */
    @Override
    public double estimateCost(Maze maze, double allowedCost) {

        // Reset dead end
        deadEnd = null;

        if (minPathLen * costPerNode > allowedCost) {
            return 0;
        }

        // Find the max length of the path that the costs allow.
        int thisMaxPathLen = Math.min((int) (allowedCost / costPerNode), maxPathLen);

        LinkedList<NodeFactory.Node> optimalPathAndParallelRouteNodes = new LinkedList<>(maze.getShortestPath());
        optimalPathAndParallelRouteNodes.addAll(MazeUtils.getAllParallelRouteNodes(maze));
        optimalPathAndParallelRouteNodes.remove(maze.getEndNode());
        LinkedList<NodeFactory.Node> passableNodesWithoutParallelRouteAndOptimalPathNodes
                = new LinkedList<>(maze.getAllPassableNodes());
        passableNodesWithoutParallelRouteAndOptimalPathNodes.remove(maze.getEndNode());
        passableNodesWithoutParallelRouteAndOptimalPathNodes
                = passableNodesWithoutParallelRouteAndOptimalPathNodes.stream()
                .filter(p -> !optimalPathAndParallelRouteNodes.contains(p))
                .collect(Collectors.toCollection(LinkedList::new));

        Collections.shuffle(passableNodesWithoutParallelRouteAndOptimalPathNodes, random);
        Collections.shuffle(optimalPathAndParallelRouteNodes, random);

        List<Stack<NodeFactory.Node>> possibleDeadEnds = new ArrayList<>();

        while ((!passableNodesWithoutParallelRouteAndOptimalPathNodes.isEmpty()
                || !optimalPathAndParallelRouteNodes.isEmpty())
                && possibleDeadEnds.size() < NUM_OF_DEAD_ENDS_TO_FIND) {

            NodeFactory.Node start;
            // FIRST:
            if (passableNodesWithoutParallelRouteAndOptimalPathNodes.isEmpty()) {
                start = optimalPathAndParallelRouteNodes.pop();
            } else if (optimalPathAndParallelRouteNodes.isEmpty()) {
                start = passableNodesWithoutParallelRouteAndOptimalPathNodes.pop();
            } else {
                start = random.nextDouble() >= this.optimalPathAndParallelRoutesPreferencePercentage
                        ? passableNodesWithoutParallelRouteAndOptimalPathNodes.pop()
                        : optimalPathAndParallelRouteNodes.pop();
            }

            Stack<NodeFactory.Node> currentDeadEnd = startDepthFirstSearch(start, maze, thisMaxPathLen);

            if (currentDeadEnd.size() >= minPathLen) {
                possibleDeadEnds.add(currentDeadEnd);
            }
        }

        if (possibleDeadEnds.isEmpty()) {
            return 0;
        }

        Stack<NodeFactory.Node> deadEnd = possibleDeadEnds.get(random.nextInt(possibleDeadEnds.size()));

        this.deadEnd = deadEnd;

        return deadEnd.size() * costPerNode;
    }

    public Stack<NodeFactory.Node> startDepthFirstSearch(NodeFactory.Node start, Maze maze, int maxPathLen) {
        List<NodeFactory.Node> visited = new ArrayList<>();
        visited.add(start);
        Stack<NodeFactory.Node> deadEnd = depthFirstSearch(start, visited, maze, -1, maxPathLen);
        deadEnd.remove(start); // Because the start node is already passable.

        if (deadEnd.size() < minPathLen) {
            return new Stack<>();
        }

        return deadEnd;
    }

    // call this with -1 as counter if the start node is one of the passable nodes
    private Stack<NodeFactory.Node> depthFirstSearch(NodeFactory.Node start, List<NodeFactory.Node> visited,
                                                     Maze maze, int recursionCounter, int maxPathLen) {

        if (recursionCounter == maxPathLen) {
            // Stop DFS prematurely if the max path length is already reached
            return new Stack<>();
        }

        Stack<NodeFactory.Node> path = new Stack<>();
        path.add(start);

        visited.add(start);

        // Recursively call DFS on all neighbors
        List<Stack<NodeFactory.Node>> paths = new ArrayList<>();
        List<NodeFactory.Node> directNeighbors = start.getDirectNeighbors();
        Collections.shuffle(directNeighbors, random); // Shuffle the neighbors to go in a random direction.
        for (NodeFactory.Node node : directNeighbors) {
            if (!visited.contains(node) && isValidNewWay(node, start, visited, maze)) {

                Stack<NodeFactory.Node> currentDeadEnd = depthFirstSearch(node, visited, maze,
                        recursionCounter + 1, maxPathLen);

                paths.add(currentDeadEnd);

                if (currentDeadEnd.size() == maxPathLen) {
                    break;
                }
            }
        }

        if (paths.size() > 0) {
            // Find the longest path
            Stack<NodeFactory.Node> longestSubPath = null;
            for (Stack<NodeFactory.Node> p : paths) {
                if (longestSubPath == null || p.size() > longestSubPath.size()) {
                    longestSubPath = p;
                }
            }

            path.addAll(longestSubPath);
        }
        return path;
    }

    public Stack<NodeFactory.Node> getDeadEnd() {
        return deadEnd;
    }

    public int getMinPathLen() {
        return minPathLen;
    }

    public int getMaxPathLen() {
        return maxPathLen;
    }

    public double getCostPerNode() {
        return costPerNode;
    }

    @Override
    public String myConfigString() {
        return this.getClass().getSimpleName()
                + "("
                + "minPathLen = " + minPathLen + ", "
                + "maxPathLen = " + maxPathLen + ", "
                + "costPerNode = " + costPerNode + ", "
                + "optimalPathAndParallelRoutesPreferencePercentage = " + optimalPathAndParallelRoutesPreferencePercentage + ", "
                + "seed = " + seed
                + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeadEndOperator that = (DeadEndOperator) o;
        return Double.compare(that.optimalPathAndParallelRoutesPreferencePercentage,
                optimalPathAndParallelRoutesPreferencePercentage) == 0 &&
                getMinPathLen() == that.getMinPathLen() &&
                getMaxPathLen() == that.getMaxPathLen() &&
                seed == that.seed &&
                Double.compare(that.getCostPerNode(), getCostPerNode()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(optimalPathAndParallelRoutesPreferencePercentage, getMinPathLen(), getMaxPathLen(), seed,
                getCostPerNode());
    }
}
