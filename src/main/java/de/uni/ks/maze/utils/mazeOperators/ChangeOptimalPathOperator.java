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

import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.Stack;

/**
 * This operator changes the optimal path of a maze.
 * This is only possible if a parallel path exists, which was previously created with the {@link NewPathOperator}.
 * After using the operator, the agent is forced to use this parallel path
 * instead of the corresponding part of the previous optimal path, because it was blocked by the operator.
 */

public class ChangeOptimalPathOperator implements MazeOperator {

    private double costsPerOptimalPathLengthIncreasement;
    private NodeFactory.Node nodeToBlock;
    private final Random random;
    private final int seed;

    public ChangeOptimalPathOperator(double costsPerOptimalPathLengthIncreasement, int seed) {

        if (costsPerOptimalPathLengthIncreasement <= 0) {
            throw new IllegalArgumentException("Value for parameter [costsPerOptimalPathLengthIncreasement] must be greater than 0.");
        }

        this.seed = seed;
        this.random = new Random(seed);
        this.costsPerOptimalPathLengthIncreasement = costsPerOptimalPathLengthIncreasement;
    }

    @Override
    public boolean changeMaze(Maze maze) {
        // return false if no change will be made
        if (nodeToBlock == null) return false;

        Logger.addTextToGuiLog("Apply change optimal path operator (" + nodeToBlock.toString() + ")", GuiMessageType.Maze);

        // block previously chosen node on optimal path
        maze.getNodeFactory().changeNodeToType(nodeToBlock, NodeType.IMPASSABLE);

        // reset block node
        nodeToBlock = null;
        return true;
    }

    /**
     * This method tries to find a passable {@link de.uni.ks.maze.NodeFactory.Node} that can be blocked and calculates the costs that would be caused by this operation.
     * <p>
     * A passable node is blockable if the following conditions apply:
     * <ul>
     * <li> The node is on the optimal path.
     * <li> There is an alternative path (parallel path) that the agent can take instead if the node gets blocked.
     * <li> Blocking the node would extend the optimal path.
     * <li> After blocking the node the maze would still be connected.
     * <li> The node is neither the start nor the end node.
     * </ul>
     * <p>
     * The returned costs are the product of the resulting increasement of the optimal paths length
     * and the previously configured costs per increasement value {@link #costsPerOptimalPathLengthIncreasement}
     *
     * @param maze        The maze for which the cost is estimated.
     * @param allowedCost The cost the operator is allowed to produce for its operation.
     * @return The costs that will be produced by blocking the determined node. 0 if no node can be blocked
     */
    @Override
    public double estimateCost(Maze maze, double allowedCost) {
        // reset node to block
        nodeToBlock = null;

        // calculate all node that could be blocked
        Stack<NodeFactory.Node> optimalPathNodes = MazeUtils.getShortestPath(maze, maze.getStartNode(), maze.getEndNode());
        Stack<NodeFactory.Node> parallelRouteNodes = MazeUtils.getAllParallelRouteNodes(maze);
        Stack<NodeFactory.Node> blockableOptimalPathNodes = new Stack<>();
        for (NodeFactory.Node node : optimalPathNodes) {
            if (parallelRouteNodes.contains(node) && node.getPassableNeighbors().size() == 2) {
                blockableOptimalPathNodes.add(node);
            }
        }

        // abort if no node can be blocked
        if (blockableOptimalPathNodes.size() == 0) {
            return 0.0d;
        }

        // find a node that can be blocked AND whose costs will be allowed
        double resultingOptimalPathLengthDifference = 0;
        double resultingCosts = 0;
        Collections.shuffle(blockableOptimalPathNodes, random);
        for (NodeFactory.Node node : blockableOptimalPathNodes) {
            // select a blockable node randomly
            resultingOptimalPathLengthDifference = getOptimalPathLengthDifference(maze, node);
            resultingCosts = resultingOptimalPathLengthDifference * costsPerOptimalPathLengthIncreasement;

            // check if costs are allowed and new path would be longer than the old one and the node is neither the start nor the end node
            if (resultingCosts <= allowedCost && resultingOptimalPathLengthDifference > 0) {
                this.nodeToBlock = node;
                break;
            }
        }

        // abort if no node can be blocked (because the costs would be too high)
        if (this.nodeToBlock == null) {
            return 0.0d;
        }

        // return the costs the previously calculated difference of the optimal path length will produce
        return resultingCosts;
    }

    /**
     * Calculates and returns how much the length of the optimal path would increase if the passed {@link de.uni.ks.maze.NodeFactory.Node}  would be blocked.
     *
     * @param maze        The maze where the path lengths will be calculated.
     * @param nodeToBlock The node whose impact on the optimal paths length will be checked.
     * @return How much the optimal paths length would increase, if the passed node gets blocked.
     */
    private double getOptimalPathLengthDifference(Maze maze, NodeFactory.Node nodeToBlock) {

        if (nodeToBlock == maze.getStartNode() || nodeToBlock == maze.getEndNode()) return Double.POSITIVE_INFINITY;

        // calculate the length of the optimal path
        double optimalPathLengthBeforeChange = maze.getLengthOfShortestPath();

        // block passed node and calculate the length of the optimal path again
        maze.getNodeFactory().changeNodeToType(nodeToBlock, NodeType.IMPASSABLE);
        double optimalPathLengthAfterChange = maze.getLengthOfShortestPath();
        maze.getNodeFactory().changeNodeToType(nodeToBlock, NodeType.PASSABLE);

        // return how much the length would be increased
        return optimalPathLengthAfterChange - optimalPathLengthBeforeChange;
    }

    public double getCostsPerOptimalPathLengthIncreasement() {
        return costsPerOptimalPathLengthIncreasement;
    }

    protected NodeFactory.Node getNodeToBlock() {
        return nodeToBlock;
    }

    protected void setNodeToBlock(NodeFactory.Node nodeToBlock) {
        this.nodeToBlock = nodeToBlock;
    }

    @Override
    public String myConfigString() {
        return this.getClass().getSimpleName() +
                "("
                + "costsPerOptimalPathLengthIncreasement = " + costsPerOptimalPathLengthIncreasement + ", "
                + "seed = " + seed
                + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeOptimalPathOperator that = (ChangeOptimalPathOperator) o;
        return Double.compare(that.costsPerOptimalPathLengthIncreasement, costsPerOptimalPathLengthIncreasement) == 0 &&
                seed == that.seed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(costsPerOptimalPathLengthIncreasement, seed);
    }
}