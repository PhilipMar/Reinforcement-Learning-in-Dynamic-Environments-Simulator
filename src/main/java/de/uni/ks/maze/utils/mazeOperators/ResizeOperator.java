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

import java.util.Objects;
import java.util.Random;

/**
 * This operator enlarges the maze, whereby the maze can only be enlarged to the right or downwards.
 * After enlarging the maze, the new end node is always at the bottom right of the changed maze.
 * This new end node is always connected to the old end node by a passable path.
 */
public class ResizeOperator implements MazeOperator {

    private double costsPerDimension;
    private int xIncreasementValue;
    private int yIncreasementValue;
    private final Random random;
    private final int seed;

    public ResizeOperator(double costsPerDimension, int seed) {

        if (costsPerDimension <= 0) {
            throw new IllegalArgumentException("Value for parameter [costPerDimension] must be greater than 0.");
        }

        this.seed = seed;
        this.random = new Random(seed);
        this.costsPerDimension = costsPerDimension;
    }

    @Override
    public boolean changeMaze(Maze maze) {
        // return false if no change will be made
        if (xIncreasementValue == 0 && yIncreasementValue == 0) return false;

        Logger.addTextToGuiLog("Apply resize operator (x += " + xIncreasementValue
                + ", y += " + yIncreasementValue + ")", GuiMessageType.Maze);
        Logger.addTextToMiscLogOfCurrentTraining("Apply resize operator (x += " + xIncreasementValue
                + ", y += " + yIncreasementValue + ")");
        Logger.addTextToMiscLogOfCurrentLevel("Apply resize operator (x += " + xIncreasementValue
                + ", y += " + yIncreasementValue + ")");

        // resize maze and return true otherwise
        enlargeMaze(maze, xIncreasementValue, yIncreasementValue);

        // reset increasement values
        xIncreasementValue = 0;
        yIncreasementValue = 0;
        return true;
    }

    @Override
    public double estimateCost(Maze maze, double allowedCost) {

        // calculate the maximum value by which the dimensions can be increased at all
        int maxIncreasementValue = (int) (allowedCost / costsPerDimension);

        // return zero costs if no increase is possible
        if (maxIncreasementValue == 0) {
            xIncreasementValue = 0;
            yIncreasementValue = 0;
            return 0;
        }

        // determine the value by which the dimensions will be actually increased
        int totalIncreasementValue = 1 + random.nextInt(maxIncreasementValue);

        // randomly determine which dimension increasement value will be calculated first
        int dimensionChoice = random.nextInt(2);

        // calculate x increasement value first
        if (dimensionChoice == 0) {
            xIncreasementValue = 1 + random.nextInt(totalIncreasementValue);
            if (totalIncreasementValue - xIncreasementValue == 0) {
                yIncreasementValue = 0;
            } else {
                yIncreasementValue = 1 + random.nextInt(totalIncreasementValue - xIncreasementValue);
            }
        }
        // calculate y increasement value first
        else {
            yIncreasementValue = 1 + random.nextInt(totalIncreasementValue);
            if (totalIncreasementValue - yIncreasementValue == 0) {
                xIncreasementValue = 0;
            } else {
                xIncreasementValue = 1 + random.nextInt(totalIncreasementValue - yIncreasementValue);
            }
        }

        // return costs
        return totalIncreasementValue * costsPerDimension;
    }

    /**
     * The method will enlarge the Node array of the maze. It will increase the x dimension by
     * {@code xIncreasementValue} and the y dimension by {@code yIncreasementValue}.
     * Furthermore the method will create new nodes to update the maze. As a result the path is extended and new wall
     * nodes are created.
     *
     * @param maze      The maze that will be enlarged
     * @param xIncrease The value by which x dimension will be increased
     * @param yIncrease The value the y dimension will be increased
     * @see de.uni.ks.maze.utils.mazeOperators.ResizeOperator#enlargeNodeArray(Maze, int, int)
     * @see de.uni.ks.maze.utils.mazeOperators.ResizeOperator#enlargeInXDimension(Maze) (Maze, int, int)
     * @see de.uni.ks.maze.utils.mazeOperators.ResizeOperator#enlargeInYDimension(Maze) (Maze, int, int)
     */
    private void enlargeMaze(Maze maze, int xIncrease, int yIncrease) {
        // resize maze
        enlargeNodeArray(maze, xIncrease, yIncrease);
        enlargeInYDimension(maze);
        enlargeInXDimension(maze);
    }

    /**
     * The method will enlarge the Node array of the passed {@code maze}. This will be accomplished by replacing the
     * old Node array with a new one.
     * All already existing nodes of the passed {@code maze} object will be also used in the new array.
     * The new x dimension will be the x dimension of the passed {@code maze} increased by {@code xIncreasementValue}.
     * The new y dimension will be the y dimension of the passed {@code maze} increased by {@code yIncreasementValue}.
     *
     * @param maze      The maze that will be enlarged
     * @param xIncrease The value by which the x dimension will be increased
     * @param yIncrease The value by which the y dimension will be increased
     */
    private void enlargeNodeArray(Maze maze, int xIncrease, int yIncrease) {

        NodeFactory.Node[][] newNodes = new NodeFactory.Node[maze.getXDim() + xIncrease][maze.getYDim() + yIncrease];

        // use already existing nodes in new Node array
        for (int x = 0; x < maze.getXDim(); x++) {
            for (int y = 0; y < maze.getYDim(); y++) {
                newNodes[x][y] = maze.getMaze()[x][y];
            }
        }

        maze.setMaze(newNodes);
    }

    /**
     * The method will create new nodes and thereby enlarge the path.
     * The enlargement will only take place in direction of the x dimension (down).
     * The new nodes will be inserted into the Node array of the passed {@code maze} object.
     * Already existing nodes won't be overwritten.
     *
     * @param maze The maze whose x dimension will be updated if there're empty fields in the node array.
     */
    private void enlargeInXDimension(Maze maze) {
        NodeFactory nodeFactory = maze.getNodeFactory();
        NodeFactory.Node oldEndNode = maze.getEndNode();
        NodeFactory.Node[][] nodes = maze.getMaze();

        // if x dimension changed ...
        if (this.xIncreasementValue > 0) {

            // change old end node to way node
            nodeFactory.changeNodeToType(oldEndNode, NodeType.PASSABLE);

            // make node below the old end node passable
            NodeFactory.Node nodeBelowOldEndNode = maze.getNodeAt(oldEndNode.getXPos() + 1, oldEndNode.getYPos());
            nodeFactory.changeNodeToType(nodeBelowOldEndNode, NodeType.PASSABLE);

            // special case: x size was only increased by one
            if (this.xIncreasementValue == 1) {
                nodeFactory.changeNodeToEnd(nodeBelowOldEndNode);
                maze.setEndNode(nodeBelowOldEndNode);
            }

            // create new nodes
            for (int x = oldEndNode.getXPos() + 2; x < maze.getXDim(); x++) {
                for (int y = 0; y < oldEndNode.getYPos() + 2; y++) {
                    NodeFactory.Node newNode;

                    // build new end nod
                    if (x == maze.getXDim() - 2 && y == oldEndNode.getYPos()) {
                        newNode = nodeFactory.buildEndNode();
                        maze.setEndNode(newNode);
                    }
                    // build new way node
                    else if (x != maze.getXDim() - 1 && y == oldEndNode.getYPos()) {
                        newNode = nodeFactory.buildWayNode();
                    }
                    // build new wall node
                    else {
                        newNode = nodeFactory.buildWallNode();
                    }

                    newNode.setXPos(x);
                    newNode.setYPos(y);
                    newNode.setMaze(maze);
                    nodes[x][y] = newNode;
                }
            }
        }
    }

    /**
     * The method will create new nodes and thereby enlarge the path.
     * The enlargement will only take place in direction of the y dimension (right)
     * The new nodes will be inserted into the Node array of the passed {@code maze} object.
     * Already existing nodes won't be overwritten.
     *
     * @param maze The maze whose y dimension will be updated if there're empty fields in the node array.
     */
    private void enlargeInYDimension(Maze maze) {
        NodeFactory nodeFactory = maze.getNodeFactory();
        NodeFactory.Node oldEndNode = maze.getEndNode();
        NodeFactory.Node[][] nodes = maze.getMaze();

        // if y dimension changed ...
        if (this.yIncreasementValue > 0) {

            // change old end node to way node
            nodeFactory.changeNodeToType(oldEndNode, NodeType.PASSABLE);

            // make node right to the old end node passable
            NodeFactory.Node nodeRightToOldEndNode = maze.getNodeAt(oldEndNode.getXPos(), oldEndNode.getYPos() + 1);
            nodeFactory.changeNodeToType(nodeRightToOldEndNode, NodeType.PASSABLE);

            // special case: y size was only increased by one
            if (this.yIncreasementValue == 1) {
                nodeFactory.changeNodeToEnd(nodeRightToOldEndNode);
                maze.setEndNode(nodeRightToOldEndNode);
            }

            // create new nodes
            for (int x = 0; x < oldEndNode.getXPos() + 2; x++) {
                for (int y = oldEndNode.getYPos() + 2; y < maze.getYDim(); y++) {
                    NodeFactory.Node newNode;

                    // build new end node
                    if (x == oldEndNode.getXPos() && y == maze.getYDim() - 2) {
                        newNode = nodeFactory.buildEndNode();
                        maze.setEndNode(newNode);
                    }
                    // build way node
                    else if (x == oldEndNode.getXPos() && y != maze.getYDim() - 1) {
                        newNode = nodeFactory.buildWayNode();
                    }
                    // build wall node
                    else {
                        newNode = nodeFactory.buildWallNode();
                    }

                    newNode.setXPos(x);
                    newNode.setYPos(y);
                    newNode.setMaze(maze);
                    nodes[x][y] = newNode;
                }
            }
        }
    }

    public double getCostsPerDimension() {
        return costsPerDimension;
    }

    protected int getxIncreasementValue() {
        return xIncreasementValue;
    }

    protected void setxIncreasementValue(int xIncreasementValue) {
        this.xIncreasementValue = xIncreasementValue;
    }

    protected int getyIncreasementValue() {
        return yIncreasementValue;
    }

    protected void setyIncreasementValue(int yIncreasementValue) {
        this.yIncreasementValue = yIncreasementValue;
    }

    @Override
    public String myConfigString() {
        return this.getClass().getSimpleName() +
                "("
                + "costsPerDimension = " + costsPerDimension + ","
                + "seed = " + seed
                + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResizeOperator that = (ResizeOperator) o;
        return Double.compare(that.getCostsPerDimension(), getCostsPerDimension()) == 0 &&
                xIncreasementValue == that.xIncreasementValue &&
                yIncreasementValue == that.yIncreasementValue &&
                seed == that.seed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCostsPerDimension(), xIncreasementValue, yIncreasementValue, seed);
    }
}