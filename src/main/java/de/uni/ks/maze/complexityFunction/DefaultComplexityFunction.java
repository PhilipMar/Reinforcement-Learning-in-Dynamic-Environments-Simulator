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

import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.utils.MazeUtils;

import java.util.*;

public class DefaultComplexityFunction implements ComplexityFunction {

    /**
     * The calculated complexity is based on the following summands:
     * <ul>
     * <li> The optimal number of actions needed for the passed maze (see {@link #calculateComplexityOfOptimalPath(Maze)})
     * <li> The complexity of all parallel route nodes (see {@link #calculateComplexityOfParallelRoutes(Maze)})
     * <li> The complexity of all dead end nodes (see {@link #calculateComplexityOfDeadEnds(Maze)})
     * </ul>
     */
    @Override
    public double calculateComplexity(Maze maze) {
        double weightedComplexityOfOptimalPath = calculateComplexityOfOptimalPath(maze);
        double weightedComplexityOfParallelRoutes = calculateComplexityOfParallelRoutes(maze);
        double weightedComplexityOfDeadEnds = calculateComplexityOfDeadEnds(maze);
        return weightedComplexityOfOptimalPath + weightedComplexityOfParallelRoutes + weightedComplexityOfDeadEnds;
    }

    // #############################################################################
    // Complexity of optimal path
    // #############################################################################

    protected static final double COMPLEXITY_PER_OPTIMAL_ACTION = 1.0d;

    protected double calculateComplexityOfOptimalPath(Maze maze) {
        return MazeUtils.getOptimalNumberOfActions(maze, maze.getStartNode(), maze.getEndNode())
                * COMPLEXITY_PER_OPTIMAL_ACTION;
    }

    // #############################################################################
    // Complexity of parallel routes
    // #############################################################################

    protected static final double COMPLEXITY_PARALLEL_ROUTE_NODE = 0.5d;

    /**
     * Calculates the complexity of the parallel routes of a maze.
     * The complexity is calculated based on the number of parallel route nodes and the associated constant
     * {@link #COMPLEXITY_PARALLEL_ROUTE_NODE}.
     *
     * @param maze The complexity of this maze is calculated.
     * @return The complexity of the parallel routes within the maze. The complexity is 0 if the maze has no parallel
     * routes.
     */
    protected double calculateComplexityOfParallelRoutes(Maze maze) {
        // get all parallel route nodes and count them
        Stack<NodeFactory.Node> allParallelRouteNodes = MazeUtils.getAllParallelRouteNodes(maze);
        allParallelRouteNodes.removeAll(maze.getShortestPath());
        return allParallelRouteNodes.size() * COMPLEXITY_PARALLEL_ROUTE_NODE;
    }

    // #############################################################################
    // Complexity of dead ends
    // #############################################################################

    protected static final double COMPLEXITY_DEAD_END_NODE = 1;
    protected static final double COMPLEXITY_THREE_WAY_JUNCTION = 3;
    protected static final double COMPLEXITY_FOUR_WAY_JUNCTION = 4;

    /**
     * Calculates the complexity of the dead ends of a maze. The complexity is the sum of the complexity of each single
     * dead end in the maze.
     *
     * @param maze The complexity of this maze is calculated.
     * @return The complexity of the dead ends within the maze. The complexity is 0 if the maze has no dead ends.
     */
    protected double calculateComplexityOfDeadEnds(Maze maze) {
        Set<NodeFactory.Node> nonDeadEndNodes = new HashSet<>();
        nonDeadEndNodes.addAll(maze.getShortestPath());
        nonDeadEndNodes.addAll(MazeUtils.getAllParallelRouteNodes(maze));

        double complexity = 0.0;

        for (NodeFactory.Node nonDeadEndNode : nonDeadEndNodes) {
            List<NodeFactory.Node> neighbors = nonDeadEndNode.getPassableNeighbors();
            neighbors.removeAll(nonDeadEndNodes);

            for (NodeFactory.Node neighbor : neighbors) {
                complexity += calculateComplexityOfBranch(neighbor, nonDeadEndNodes);
            }
        }

        return complexity;
    }

    /**
     * Calculates the complexity of a dead end branch in the maze. The complexity of a branch is the sum
     * over the complexity of all its nodes. Junctions get weighted more the deeper they are located in the branch.
     * <p>
     * The depth of a node depends on the number of junctions that were visited before reaching this node.
     * See {@link #calculateComplexityOfNode(NodeFactory.Node, int)}.
     * <p>
     * The method should be called with a node adjacent to a node of the {@link Maze#getShortestPath()}.
     * <p>
     * If the dead end contains a loop, the loop gets traversed as if it two different branches meeting each other.
     * The search stops in such a branch, if it meets a node that was already visited as a part of a different branch.
     *
     * @param start     The node at which the search begins.
     * @param ignoreSet {@link Set} of nodes that are ignored in the search. I.e. nodes in parallel routes and nodes on
     *                            the optimal path should get ignored.
     * @return The complexity of the dead end that starts with {@code node}.
     */
    protected double calculateComplexityOfBranch(NodeFactory.Node start, Set<NodeFactory.Node> ignoreSet) {

        double complexity = 0;

        // breadth first search:
        Queue<NodeFactory.Node> queue = new ArrayDeque<>();
        queue.add(start);
        List<NodeFactory.Node> visited = new ArrayList<>();

        // Maps to manage respective depth of nodes.
        Map<NodeFactory.Node, Integer> depthMap = new HashMap<>();
        Map<NodeFactory.Node, NodeFactory.Node> predecessorMap = new HashMap<>();

        while (!queue.isEmpty()) {
            NodeFactory.Node node = queue.poll();
            visited.add(node);

            // Depth of current node is at least depth of its predecessor, depth of first junction must
            // be 0 after update.
            int currentDepth = depthMap.getOrDefault(predecessorMap.get(node), -1);
            if (node.getPassableNeighbors().size() > 2) currentDepth++;
            depthMap.put(node, currentDepth);

            complexity += calculateComplexityOfNode(node, currentDepth);

            for (NodeFactory.Node v : node.getPassableNeighbors()) {
                if (visited.contains(v)) continue;
                if (ignoreSet.contains(v)) continue;
                queue.add(v);
                visited.add(v);
                predecessorMap.put(v, node);
            }
        }

        return complexity;
    }

    /**
     * Calculates the complexity of a node that is part of a dead end. The complexity depends on the number of passable
     * neighbors of the node, and the branch-depth of the node.
     *
     * @param node  Calculate the complexity of this.
     * @param depth The branch-depth of the node. I.e. the number of junction nodes that were encountered before.
     * @return The calculated complexity of the node.
     */
    protected double calculateComplexityOfNode(NodeFactory.Node node, int depth) {

        final double multiplicand = 1 + (Math.exp(depth * 0.25) - 1);

        switch (node.getPassableNeighbors().size()) {
            case 3: // three-way junction
                return COMPLEXITY_THREE_WAY_JUNCTION * multiplicand;
            case 4: // four-way junction
                return COMPLEXITY_FOUR_WAY_JUNCTION * multiplicand;
            default:
                return COMPLEXITY_DEAD_END_NODE; // normal node
        }
    }

    @Override
    public String myConfigString() {
        return this.getClass().getSimpleName() + "()";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DefaultComplexityFunction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(myConfigString());
    }
}