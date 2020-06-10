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
package de.uni.ks.maze;

import de.uni.ks.maze.NodeFactory.Node;
import de.uni.ks.maze.utils.MazeUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the maze that the {@link de.uni.ks.agent.Agent} walks through.
 */
public class Maze {

    // declare start and end states
    private Node startNode;
    private Node endNode;
    private Node[][] maze;
    private NodeFactory nodeFactory;

    // If a maze is larger than this in either height or width, the resulting image is of reduced resolution.
    private static final int MAX_MAZE_PRINT_SIZE = 2000;

    public Maze(NodeFactory nodeFactory, Node[][] maze, Node startNode, Node endNode) {

        if (startNode != null && endNode != null) {
            this.startNode = startNode;
            this.endNode = endNode;
        } else {
            System.err.println("No start or end node provided for new maze.");
            // No start and end then..
        }

        this.nodeFactory = nodeFactory;

        initMaze(maze);
    }

    // copy constructor
    public Maze(Maze mazeToCopy) {
        Node[][] mazeToCopyArray = mazeToCopy.getMaze();
        this.maze = new Node[mazeToCopyArray.length][mazeToCopyArray[0].length];
        this.nodeFactory = new NodeFactory(mazeToCopy.getNodeFactory());
        for (int x = 0; x < mazeToCopy.getMaze().length; x++) {
            for (int y = 0; y < mazeToCopy.getMaze()[0].length; y++) {
                Node nodeToCopy = mazeToCopy.getNodeAt(x, y);
                Node newNode = NodeFactory.copyNode(nodeToCopy);
                if (nodeToCopy == mazeToCopy.getStartNode()) {
                    this.startNode = newNode;
                } else if (nodeToCopy == mazeToCopy.getEndNode()) {
                    this.endNode = newNode;
                }
                this.maze[x][y] = newNode;
            }
        }
        initMaze(maze);
    }

    private void initMaze(Node[][] maze) {
        if (maze != null && maze.length > 1 && maze[0].length > 1) {
            this.maze = maze;
            initNodes();
        } else {
            throw new IllegalArgumentException("The parameter [maze] is an invalid instance.");
        }
    }


    private void initNodes() {
        for (int x = 0; x < maze.length; x++) {
            for (int y = 0; y < maze[0].length; y++) {
                Node n = maze[x][y];
                n.setMaze(this)
                        .setXPos(x)
                        .setYPos(y);
            }
        }
    }

    /**
     * Returns the node that is at the given position (xPos, yPos) in the maze-array.
     *
     * @param xPos x position of requested node.
     * @param yPos y position of requested node.
     * @return The node at the given position or null if the position is outside of the maze.
     */
    public Node getNodeAt(int xPos, int yPos) {
        if (!(xPos < 0 || yPos < 0
                || xPos >= maze.length || yPos >= maze[0].length)) {
            return maze[xPos][yPos];
        } else {
            return null;
        }
    }

    public void setMaze(Node[][] maze) {
        this.maze = maze;
    }

    public Node[][] getMaze() {
        return this.maze;
    }

    public int getLengthOfShortestPath() throws IllegalArgumentException {
        return MazeUtils.getOptimalNumberOfActions(this, this.startNode, this.endNode);
    }

    public List<Node> getShortestPath() {
        return MazeUtils.getShortestPath(this, this.startNode, this.endNode);
    }

    public List<Node> getAllPassableNodes() {
        List<Node> allAccessibleNodes = new ArrayList<>();

        for (int x = 0; x < maze.length; x++) {
            for (int y = 0; y < maze[0].length; y++) {
                Node node = maze[x][y];
                if (node.getNodeType() != NodeType.IMPASSABLE) {
                    allAccessibleNodes.add(node);
                }
            }
        }


        return allAccessibleNodes;
    }

    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node startNode) {
        // Change the reward and type to be sure

        if (startNode != null) {
            nodeFactory.changeNodeToStart(startNode);
        }

        this.startNode = startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node endNode) {

        if (endNode != null) {
            nodeFactory.changeNodeToEnd(endNode);
        }

        this.endNode = endNode;
    }

    public int getXDim() {
        return getMaze().length;
    }

    public int getYDim() {
        return getMaze()[0].length;
    }

    /**
     * Creates an image of this {@link Maze} that can be stored in the file system. This is used by the
     * {@link de.uni.ks.logging.Logger}.
     *
     * @param reduceImageSize If true, the resolution of the maze is reduced to set an upper bound for the final image
     *                        (file) size.
     * @return A {@link BufferedImage} that shows this maze.
     */
    public BufferedImage getMazeAsBufferedImage(boolean reduceImageSize) {

        class MazeCanvas extends Canvas {
            private int cellSize = 50;

            MazeCanvas() {
                int numberOfColumns = maze[0].length;
                int numberOfRows = maze.length;

                int width = numberOfColumns * cellSize;
                int height = numberOfRows * cellSize;

                if (reduceImageSize && (height > MAX_MAZE_PRINT_SIZE || width > MAX_MAZE_PRINT_SIZE)) {
                    cellSize = 3;
                    width = numberOfColumns * cellSize;
                    height = numberOfRows * cellSize;
                }

                setSize(width, height);
            }

            public void paint(Graphics g) {
                NodeFactory.Node[][] nodes = getMaze();

                for (int x = 0; x < nodes.length; x++) {
                    for (int y = 0; y < nodes[0].length; y++) {
                        NodeFactory.Node node = nodes[x][y];

                        // draw border
                        g.setColor(java.awt.Color.gray);
                        g.fillRect(y * cellSize, x * cellSize, cellSize, cellSize);

                        // draw node
                        g.setColor(node.getColor());
                        double rasterWidth = 0.02;
                        double lineWidth = cellSize * rasterWidth;
                        g.fillRect((int) (y * cellSize + lineWidth), (int) (x * cellSize + lineWidth),
                                (int) (cellSize - 2 * lineWidth), (int) (cellSize - 2 * lineWidth));

                        // highlight start and end node
                        if (getStartNode().equals(node)) {
                            g.setColor(java.awt.Color.black);
                            g.setFont(new Font("SanSerif", Font.PLAIN, cellSize));
                            g.drawString("S", (int) (y * cellSize + 0.2 * cellSize), (int) (x * cellSize + 0.85
                                    * cellSize));
                        } else if (getEndNode().equals(node)) {
                            g.setColor(Color.black);
                            g.setFont(new Font("SanSerif", Font.PLAIN, cellSize));
                            g.drawString("E", (int) (y * cellSize + 0.2 * cellSize), (int) (x * cellSize + 0.85
                                    * cellSize));
                        }
                    }
                }
            }
        }

        try {
            Canvas canvas = new MazeCanvas();
            BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            canvas.paint(g2);

            return image;
        } catch (OutOfMemoryError e) {
            // The image is to large to save.
            return null;
        }
    }
}
