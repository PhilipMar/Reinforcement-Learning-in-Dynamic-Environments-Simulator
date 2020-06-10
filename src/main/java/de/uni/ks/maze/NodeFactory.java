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

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Font;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for the node management of a maze.
 * This class can be used to create new nodes and modify existing nodes.
 * Furthermore, it offers some functions regarding the states the nodes provide.
 */
public class NodeFactory {

    private double endReward;
    private double actionReward;
    public static final double IMPASSABLE_REWARD = Double.NEGATIVE_INFINITY;

    // fields used for random color assignment
    private final Random usedWayColorsRandom;
    private final Random usedWallColorsRandom;
    private Color[] wallColors;
    private Color[] wayColors;

    // maximum allowed tries to generate a valid random color
    private static final int maxTries = 10000000;

    // minWallWayBrightnessDifference has to be in [0, 255]
    public NodeFactory(double actionReward, double endReward, int numberOfWayColors, int numberOfWallColors,
                       int generateWayColorsSeed, int generatedWallColorsSeed, int usedWayColorsSeed,
                       int usedWallColorsSeed, int minWallWayBrightnessDifference) {

        validateParameters(numberOfWayColors, numberOfWallColors, minWallWayBrightnessDifference);

        this.actionReward = actionReward;
        this.endReward = endReward;

        // init random number generators for color assignments
        this.usedWayColorsRandom = new Random(usedWayColorsSeed);
        this.usedWallColorsRandom = new Random(usedWallColorsSeed);

        // determine possible colors for way and wall nodes
        double brightnessRange = (255d - minWallWayBrightnessDifference) / 2d;
        this.wallColors = findColors(numberOfWallColors, 0, brightnessRange, generatedWallColorsSeed);
        this.wayColors = findColors(numberOfWayColors, 255 - brightnessRange, 255, generateWayColorsSeed);
    }

    public static void validateParameters(int numberOfWayColors, int numberOfWallColors, double minWallWayBrightnessDifference) {
        if (numberOfWayColors <= 0 || numberOfWallColors <= 0) {
            throw new IllegalArgumentException("Parameters [numberOfWayColors] and [numberOfWallColors] " +
                    "must be greater than 0.");
        }

        if (minWallWayBrightnessDifference < 0 || minWallWayBrightnessDifference > 255) {
            throw new IllegalArgumentException("Parameter [minWallWayBrightnessDifference] is not in [0, 255]");
        }
    }

    public NodeFactory(double actionReward, double endReward, Color[] wallColors, Random usedWallColorsRandom,
                       Color[] wayColors, Random usedWayColorsRandom) {
        this.actionReward = actionReward;
        this.endReward = endReward;
        this.wallColors = wallColors;
        this.usedWallColorsRandom = usedWallColorsRandom;
        this.wayColors = wayColors;
        this.usedWayColorsRandom = usedWayColorsRandom;
    }

    public NodeFactory(NodeFactory nodeFactory) {
        this(nodeFactory.actionReward, nodeFactory.endReward, nodeFactory.wallColors, nodeFactory.usedWallColorsRandom,
                nodeFactory.wayColors, nodeFactory.usedWayColorsRandom);
    }

    // ################################################################################################################
    // Build nodes
    // ################################################################################################################

    // Rewards shall only be negative, thus the reward of a wall node is infinitely negative.
    // Therefor this node should never be chosen by any policy to be a good alternative.
    public Node buildWallNode() {
        return buildWallNode(Double.NEGATIVE_INFINITY);
    }

    public Node buildWallNode(Double reward) {
        Node node = new Node(reward, NodeType.IMPASSABLE);
        node.setColor(this.wallColors[this.usedWallColorsRandom.nextInt(this.wallColors.length)]);
        return node;
    }

    public Node buildEndNode() {
        return buildEndNode(endReward);
    }

    public Node buildEndNode(Double reward) {
        Node node = new Node(reward, NodeType.PASSABLE);
        node.setColor(this.wayColors[this.usedWayColorsRandom.nextInt(this.wayColors.length)]);
        return node;
    }

    public Node buildStartNode() {
        return buildStartNode(actionReward);
    }

    public Node buildStartNode(Double reward) {
        Node node = new Node(reward, NodeType.PASSABLE);
        node.setColor(this.wayColors[this.usedWayColorsRandom.nextInt(this.wayColors.length)]);
        return node;
    }

    public Node buildWayNode() {
        return buildWayNode(actionReward);
    }

    public Node buildWayNode(Double reward) {
        Node node = new Node(reward, NodeType.PASSABLE);
        node.setColor(this.wayColors[this.usedWayColorsRandom.nextInt(this.wayColors.length)]);
        return node;
    }

    // ################################################################################################################
    // Method to encapsulate changing node type
    // ################################################################################################################

    public void changeNodeToType(Node node, NodeType type) {
        switch (type) {
            case PASSABLE:
                node.setNodeType(type, actionReward);
                if (!nodeLooksLikeWayNode(node))
                    node.setColor(this.wayColors[this.usedWayColorsRandom.nextInt(this.wayColors.length)]);
                break;
            case IMPASSABLE:
                node.setNodeType(type, IMPASSABLE_REWARD);
                if (!nodeLooksLikeWallNode(node))
                    node.setColor(this.wallColors[this.usedWallColorsRandom.nextInt(this.wallColors.length)]);
                break;
            default:
                node.setNodeType(NodeType.IMPASSABLE, IMPASSABLE_REWARD);
        }
    }

    public void changeNodeToStart(Node node) {
        changeNodeToType(node, NodeType.PASSABLE);
        if (!nodeLooksLikeWayNode(node))
            node.setColor(this.wayColors[this.usedWayColorsRandom.nextInt(this.wayColors.length)]);
    }

    public void changeNodeToEnd(Node node) {
        node.setNodeType(NodeType.PASSABLE, endReward);
        if (!nodeLooksLikeWayNode(node))
            node.setColor(this.wayColors[this.usedWayColorsRandom.nextInt(this.wayColors.length)]);
    }

    // ################################################################################################################
    // Build method for copy constructor
    // ################################################################################################################
    public static Node copyNode(Node nodeToCopy) {
        return new Node(nodeToCopy);
    }

    // ################################################################################################################
    // Color related methods
    // ################################################################################################################

    /**
     * Randomly generates {@code numberOfColors} colors whose brightness lies within an interval between {@code lowerBound} and {@code upperBound}.
     * {@code lowerBound}  and {@code upperBound} may only accept values between 0 and 255.
     * Brightness is calculated as described in {@link #getBrightness(Color)}
     *
     * @param numberOfColors Number of random colors that will be generated.
     * @param lowerBound     Lower bound for color brightness.
     * @param upperBound     Upper bound for color brightness.
     * @param seed           Random seed that is used to generate the colors.
     * @return An Array with the desired number of random colors whose brightness lies within the allowed interval.
     */
    public Color[] findColors(int numberOfColors, double lowerBound, double upperBound, int seed) {
        if (lowerBound < 0 || lowerBound > 255) {
            throw new IllegalArgumentException("parameter [lowerBound] = " + lowerBound + " is not in [0, 255]");
        }
        if (upperBound < 0 || upperBound > 255) {
            throw new IllegalArgumentException("parameter [upperBound] = " + lowerBound + " is not in [0, 255]");
        }
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("parameter [lowerBound] = " + lowerBound + "  has to be smaller than parameter [upperBound] = " + upperBound);
        }

        // init color array and random generator
        Color[] colors = new Color[numberOfColors];
        Random random = new Random(seed);

        //try to find 'numberOfColors' colors.
        int index = 0;
        int tryNr = 1;
        while (index < numberOfColors) {

            // force color if no random color was found after 'maxTries' tries
            if (tryNr == maxTries) {
                throw new RuntimeException("Could not find enough colors. Reduce number of colors or reduce minWallWayBrightnessDifference in config");
            }

            // generate random color
            int red = random.nextInt(255);
            int green = random.nextInt(255);
            int blue = random.nextInt(255);
            Color color = new Color(red, green, blue);

            // check if color brightness is in allowed interval and is not already in use
            double brightness = getBrightness(color);
            if ((lowerBound <= brightness) && (brightness <= upperBound) && !containsColor(colors, color)) {
                colors[index] = color;
                index++;
                tryNr = 1;
            } else {
                tryNr++;
            }
        }
        return colors;
    }

    /**
     * Checks if {@code colors} contains a color with the same rgb values as {@code color}.
     *
     * @param colors The colors the passed color will be compared with.
     * @param color  Color object that will be compared with the color objects of the {@code colors} array.
     * @return True if {@code color} object is in {@code color} Array. False Otherwise.
     */
    private boolean containsColor(Color[] colors, Color color) {
        for (Color tmpColor : colors) {
            if (color.equals(tmpColor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates brightness according to a "HSP Color Model".
     *
     * @param color Color whose brightness will be calculated.
     * @return The brightness of the passed color according to a "HSP Color Model".
     * @see <a href="http://alienryderflex.com/hsp.html">http://alienryderflex.com/hsp.html</a>
     */
    public static double getBrightness(Color color) {
        return Math.sqrt(0.299 * Math.pow(color.getRed(), 2) + 0.587 * Math.pow(color.getGreen(), 2) + 0.114 * Math.pow(color.getBlue(), 2));
    }

    /**
     * Checks if {@code node} has a wall color.
     *
     * @param node The node that will be checked
     * @return True if {@code node} is wall node. False otherwise.
     */
    public boolean nodeLooksLikeWallNode(Node node) {
        return containsColor(this.wallColors, node.color);
    }

    /**
     * Checks if {@code node} has a way color.
     *
     * @param node The node that will be checked
     * @return True if {@code node} is way node. False otherwise.
     */
    public boolean nodeLooksLikeWayNode(Node node) {
        return containsColor(this.wayColors, node.color);
    }

    // ################################################################################################################
    // Node class
    // ################################################################################################################

    /**
     * A node represents a field in the {@link Maze}.
     */
    public static class Node {

        // declare properties
        private double reward;
        private NodeType nodeType;
        private Maze maze;
        private Color color;
        int xPos, yPos; // the coordinates of this node in the maze

        /**
         * @param reward   The reward the {@link de.uni.ks.agent.Agent} gets if it enters this field.
         * @param nodeType The initial {@link NodeType} of this node.
         */
        private Node(double reward, NodeType nodeType) {
            this(reward, nodeType, 0, 0);
        }

        // copy constructor (maze is not set)
        private Node(Node nodeToCopy) {
            this(nodeToCopy.getReward(), nodeToCopy.getNodeType(), nodeToCopy.getXPos(), nodeToCopy.getYPos());
            this.setColor(nodeToCopy.getColor());
            this.setMaze(nodeToCopy.getMaze());
        }

        private Node(double reward, NodeType nodetype, int xPos, int yPos) {
            this.reward = reward;
            this.nodeType = nodetype;
            this.xPos = xPos;
            this.yPos = yPos;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        // getter and setter of properties
        public double getReward() {
            return reward;
        }

        public void setReward(double reward) {
            this.reward = reward;
        }

        public int getXPos() {
            return this.xPos;
        }

        public int getYPos() {
            return this.yPos;
        }

        public Maze getMaze() {
            return this.maze;
        }

        public Node setXPos(int xPos) {
            this.xPos = xPos;
            return this;
        }

        public Node setYPos(int yPos) {
            this.yPos = yPos;
            return this;
        }

        public Node setMaze(Maze maze) {
            this.maze = maze;
            return this;
        }

        public NodeType getNodeType() {
            return nodeType;
        }

        private void setNodeType(NodeType newNodeType, double newReward) {

            // Handling start and end nodes here is not possible anymore.

            this.nodeType = newNodeType;
            this.reward = newReward;
        }

        // Moore neighborhood of the node 'S'
        //      y:0   y:1   y:2
        // x:0   ul   u    ur
        // x:1   l    S    r
        // x:2   dl   d    dr

        // getter and setter of neighbors
        public Node getUpperLeftNeighbor() {
            return maze.getNodeAt(xPos - 1, yPos - 1);
        }

        public Node getUpperNeighbor() {
            return maze.getNodeAt(xPos - 1, yPos);
        }

        public Node getUpperRightNeighbor() {
            return maze.getNodeAt(xPos - 1, yPos + 1);
        }

        public Node getRightNeighbor() {
            return maze.getNodeAt(xPos, yPos + 1);
        }

        public Node getLowerRightNeighbor() {
            return maze.getNodeAt(xPos + 1, yPos + 1);
        }

        public Node getLowerNeighbor() {
            return maze.getNodeAt(xPos + 1, yPos);
        }

        public Node getLowerLeftNeighbor() {
            return maze.getNodeAt(xPos + 1, yPos - 1);
        }

        public Node getLeftNeighbor() {
            return maze.getNodeAt(xPos, yPos - 1);
        }

        /**
         * Method to get all neighbors of this node that would also be accessible by the agent.
         * I.e. all neighbors the agent could move to from this node, including WALL-Nodes.
         *
         * @return List containing all nodes.
         */
        public List<Node> getPassableNeighbors() {

            ArrayList<Node> list = (ArrayList<Node>) getDirectNeighbors();

            list.removeIf(node -> node.getNodeType() == NodeType.IMPASSABLE);

            return list;
        }

        public List<Node> getDirectNeighbors() {

            ArrayList<Node> list = new ArrayList<>();
            if (getLeftNeighbor() != null) {
                list.add(getLeftNeighbor());
            }
            if (getUpperNeighbor() != null) {
                list.add(getUpperNeighbor());
            }
            if (getRightNeighbor() != null) {
                list.add(getRightNeighbor());
            }
            if (getLowerNeighbor() != null) {
                list.add(getLowerNeighbor());
            }

            return list;
        }

        /**
         * encode Node as String by encoding its neighborhood. Use enum values of all neighbors and mention them clockwise
         *
         * @return returns encoded state as string
         */
        public String getState() {

            Node upperNeighbor = this.getUpperNeighbor();
            Node upperRightNeighbor = this.getUpperRightNeighbor();
            Node rightNeighbor = this.getRightNeighbor();
            Node lowerRightNeighbor = this.getLowerRightNeighbor();
            Node lowerNeighbor = this.getLowerNeighbor();
            Node lowerLeftNeighbor = this.getLowerLeftNeighbor();
            Node leftNeighbor = this.getLeftNeighbor();
            Node upperLeftNeighbor = this.getUpperLeftNeighbor();

            return describeNode(upperNeighbor) + "|"
                    + describeNode(upperRightNeighbor) + "|"
                    + describeNode(rightNeighbor) + "|"
                    + describeNode(lowerRightNeighbor) + "|"
                    + describeNode(lowerNeighbor) + "|"
                    + describeNode(lowerLeftNeighbor) + "|"
                    + describeNode(leftNeighbor) + "|"
                    + describeNode(upperLeftNeighbor);
        }

        public boolean isPassable() {
            return this.getNodeType() == NodeType.PASSABLE;
        }

        @Override
        public String toString() {
            return "Node at [" + this.getXPos() + "][" + this.getYPos() + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return xPos == node.xPos && yPos == node.yPos;
        }

        @Override
        public int hashCode() {
            return Objects.hash(xPos, yPos);
        }
    }

    private static String describeNode(Node n) {
        return n == null ? NodeType.IMPASSABLE.toString().substring(0, 1)
                : n.getNodeType().toString().substring(0, 1)
                + "["
                + "r=" + n.getColor().getRed() + ","
                + "g=" + n.getColor().getGreen() + ","
                + "b=" + n.getColor().getBlue()
                + "]";
    }

    /**
     * The method returns an image that displays the neighbourhood of the passed state.
     *
     * @param state State to be drawn.
     * @return Image showing the neighbourhood.
     */
    public static Image createImageOfState(String state) {
        javafx.scene.paint.Color[] colors = getColorsOfState(state);
        return createStateImageFromString(colors[0], colors[1], colors[2], colors[3], colors[4],
                colors[5], colors[6], colors[7]);
    }

    /**
     * The method returns an Array that contains the colors of all neighbor nodes.
     * color[0] = color of upper neighbor.
     * color[1] = color of upper right neighbor.
     * color[2] = color of right neighbor.
     * color[3] = color of lower right neighbor.
     * color[4] = color of lower neighbor.
     * color[5] = color of lower left neighbor.
     * color[6] = color of left neighbor.
     * color[7] = color of upper left neighbor.
     *
     * @param state State whose neighborhood node colors will be extracted.
     * @return An Array that contains the colors of all neighbor nodes.
     */
    private static javafx.scene.paint.Color[] getColorsOfState(String state) {

        // get content of every [] bracket
        Matcher matcher = Pattern.compile("\\[([^\\]]+)").matcher(state);
        ArrayList<String> tags = new ArrayList<>();
        int pos = -1;
        while (matcher.find(pos + 1)) {
            pos = matcher.start();
            tags.add(matcher.group(1));
        }

        // create color for every rbg value tuple
        javafx.scene.paint.Color[] colors = new javafx.scene.paint.Color[8];
        for (int i = 0; i < 8; i++) {
            String[] currentStringRGBValues = tags.get(i).split(",");
            int rValue = Integer.parseInt(currentStringRGBValues[0].substring(2));
            int gValue = Integer.parseInt(currentStringRGBValues[1].substring(2));
            int bValue = Integer.parseInt(currentStringRGBValues[2].substring(2));

            java.awt.Color color = new java.awt.Color(rValue, gValue, bValue);
            colors[i] = transformColor(color);
        }

        return colors;
    }

    /**
     * The method returns an image that displays the neighbourhood of the passed state.
     *
     * @param colorUpperNeighbor      Color of the upper neighbor.
     * @param colorUpperRightNeighbor Color of upper right neighbor.
     * @param colorRightNeighbor      Color of right neighbor.
     * @param colorLowerRightNeighbor Color of lower right neighbor.
     * @param colorLowerNeighbor      Color of lower neighbor.
     * @param colorLowerLeftNeighbor  Color of lower left neighbor.
     * @param colorLeftNeighbor       Color of left neighbor.
     * @param colorUpperLeftNeighbor  Color upper left neighbor.
     * @return Image showing the neighbourhood.
     */
    public static Image createStateImageFromString(javafx.scene.paint.Color colorUpperNeighbor,
                                                   javafx.scene.paint.Color colorUpperRightNeighbor,
                                                   javafx.scene.paint.Color colorRightNeighbor,
                                                   javafx.scene.paint.Color colorLowerRightNeighbor,
                                                   javafx.scene.paint.Color colorLowerNeighbor,
                                                   javafx.scene.paint.Color colorLowerLeftNeighbor,
                                                   javafx.scene.paint.Color colorLeftNeighbor,
                                                   javafx.scene.paint.Color colorUpperLeftNeighbor) {
        // init needed attributes
        double cellSize = 20d;
        double rasterWidth = 0.02d;
        double lineWidth = cellSize * rasterWidth;

        // draw all nodes
        javafx.scene.canvas.Canvas canvas = new Canvas(cellSize * 3, cellSize * 3);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {

                // select color
                javafx.scene.paint.Color color = null;
                if (x == 0 & y == 0) {
                    color = colorUpperLeftNeighbor;
                } else if (x == 0 & y == 1) {
                    color = colorLeftNeighbor;
                } else if (x == 0 & y == 2) {
                    color = colorLowerLeftNeighbor;
                } else if (x == 1 & y == 0) {
                    color = colorUpperNeighbor;
                } else if (x == 1 & y == 1) {
                    // agent node
                    color = javafx.scene.paint.Color.WHITE;
                } else if (x == 1 & y == 2) {
                    color = colorLowerNeighbor;
                } else if (x == 2 & y == 0) {
                    color = colorUpperRightNeighbor;
                } else if (x == 2 & y == 1) {
                    color = colorRightNeighbor;
                } else if (x == 2 & y == 2) {
                    color = colorLowerRightNeighbor;
                }

                // draw border
                gc.setFill(javafx.scene.paint.Color.GRAY);
                gc.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                // draw node
                gc.setFill(color);
                gc.fillRect((int) (x * cellSize + lineWidth), (int) (y * cellSize + lineWidth),
                        (int) (cellSize - 2 * lineWidth), (int) (cellSize - 2 * lineWidth));

                // highlight agent position
                if (x == 1 && y == 1) {

                    // draw round rect on agent node
                    gc.setFill(javafx.scene.paint.Color.GREY);
                    gc.fillRoundRect(x * cellSize + 0.1 * cellSize, y * cellSize + 0.1 * cellSize,
                            0.8 * cellSize, 0.8 * cellSize, cellSize, cellSize);

                    // mark agent by drawing "A" on round rect
                    gc.setFill(javafx.scene.paint.Color.BLACK);
                    gc.setFont(new Font(0.7 * cellSize));
                    gc.fillText("A", x * cellSize + 0.25 * cellSize, y * cellSize + 0.75 * cellSize);
                }
            }
        }

        // create image from canvas
        final WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        final WritableImage snapshot = canvas.snapshot(new SnapshotParameters(), writableImage);

        // return image
        return writableImage;
    }

    private static javafx.scene.paint.Color transformColor(java.awt.Color input) {
        int alpha = input.getAlpha();
        int blue = input.getBlue();
        int red = input.getRed();
        int green = input.getGreen();

        double opacity = alpha / 255.0;

        return javafx.scene.paint.Color.rgb(red, green, blue, opacity);
    }
}
