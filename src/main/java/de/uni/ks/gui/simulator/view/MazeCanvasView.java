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
package de.uni.ks.gui.simulator.view;

import de.uni.ks.agent.Agent;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * This class is used to display the {@link Agent} and the {@link Maze} in a {@link Canvas}
 */

public class MazeCanvasView {

    private final Canvas mazeCanvas;
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 500;

    public MazeCanvasView() {
        this.mazeCanvas = createMazeCanvas();
    }

    private Canvas createMazeCanvas() {
        return new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Same as {@link #drawMaze(double, Maze, Agent)} but without a raster width.
     *
     * @param maze  The maze to draw.
     * @param agent The agent to draw.
     */
    public void drawMaze(Maze maze, Agent agent) {
        // Disclaimer: Even when the width is set to 0 there will be a fine border.
        double rasterWidth = 0.02d;
        drawMaze(rasterWidth, maze, agent);
    }

    /**
     * Draws a maze on a canvas, each cell of the maze will be drawn as a rectangle where all sides have the same length.
     * The length of the sides is calculated by using the shortest dimension (i.e. width or height) of the canvas,
     * this way it is guarantied that the whole maze fits on the canvas.
     *
     * @param maze        The maze to draw.
     * @param agent       The agent to draw.
     * @param rasterWidth The width of the border of each rectangle, when <code>rasterWidth</code> is 0 there
     *                    will still be a fine border around each rectangle.
     */
    private void drawMaze(double rasterWidth, Maze maze, Agent agent) {

        NodeFactory.Node[][] nodes = maze.getMaze();

        double width = Math.min(mazeCanvas.getHeight() / maze.getXDim(),
                mazeCanvas.getWidth() / maze.getYDim());

        double offsetX = 0.5 * (mazeCanvas.getWidth() - (nodes[0].length * width));
        double offsetY = 0.5 * (mazeCanvas.getHeight() - nodes.length * width);

        GraphicsContext gc = mazeCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mazeCanvas.getWidth(), mazeCanvas.getHeight());

        // visualize canvas size
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, mazeCanvas.getWidth(), mazeCanvas.getHeight());

        for (int y = 0; y < nodes.length; y++) {
            for (int x = 0; x < nodes[0].length; x++) {
                NodeFactory.Node node = nodes[y][x];

                gc.setFill(Color.GRAY);

                gc.fillRect(offsetX + x * width, offsetY + y * width, width, width);

                gc.setFill(transformColor(node.getColor()));

                double lineWidth = width * rasterWidth;

                gc.fillRect(offsetX + x * width + lineWidth, offsetY + y * width + lineWidth,
                        width - 2 * lineWidth, width - 2 * lineWidth);

                if (maze.getStartNode().equals(node)) {
                    gc.setFill(Color.BLACK);
                    gc.setFont(new Font(width));
                    gc.fillText("S", offsetX + x * width + 0.2 * width, offsetY + y * width + 0.9 * width);
                } else if (maze.getEndNode().equals(node)) {
                    gc.setFill(Color.BLACK);
                    gc.setFont(new Font(width));
                    gc.fillText("E", offsetX + x * width + 0.2 * width, offsetY + y * width + 0.9 * width);
                }

                // Mark agent on the maze
                if (agent.getCurrentPosition() != null
                        && agent.getCurrentPosition().equals(node)) {
                    gc.setFill(Color.GRAY);
                    //noinspection SuspiciousNameCombination
                    gc.fillRoundRect(offsetX + x * width + 0.1 * width, offsetY + y * width + 0.1 * width,
                            0.8 * width, 0.8 * width, width, width);

                    gc.setFill(Color.BLACK);
                    gc.setFont(new Font(0.7 * width));
                    gc.fillText("A", offsetX + x * width + 0.25 * width, offsetY + y * width + 0.75 * width);
                }
            }
        }
    }

    /**
     * Transforms @link{java.awt.Color} to @link{javafx.scene.paint.Color}
     *
     * @param input The awt color to transform
     * @return The javafx color representation of <code>input</code>.
     */
    private Color transformColor(java.awt.Color input) {
        int alpha = input.getAlpha();
        int blue = input.getBlue();
        int red = input.getRed();
        int green = input.getGreen();

        double opacity = alpha / 255.0;

        return Color.rgb(red, green, blue, opacity);
    }

    public Canvas getMazeCanvas() {
        return mazeCanvas;
    }
}
