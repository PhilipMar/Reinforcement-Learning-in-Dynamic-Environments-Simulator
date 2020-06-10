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

import de.uni.ks.configuration.WritableToConfig;
import de.uni.ks.maze.Maze;

/**
 * MazeOperators are used to change a maze, and make it more complex for the agent. The operators are used in
 * {@link de.uni.ks.maze.utils.MazeUtils}.
 */
public interface MazeOperator extends WritableToConfig {

    /**
     * Uses this operator to change the given maze. If the operator cannot be used on this maze,
     * the maze will not be changed.
     *
     * @param maze The maze that the operator will change.
     * @return True if the operation succeeded, false if not.
     */
    boolean changeMaze(Maze maze);

    /**
     * Estimates the cost that using this operator on the maze will produce. This is used to decide if an operation
     * should be used or not.
     *
     * @param maze        The maze for which the cost is estimated.
     * @param allowedCost The cost the operator is allowed to produce for its operation.
     * @return The cost of the operation the <code>MazeOperator</code> will do. This can be a static value or it can be
     * dynamically calculated based on how big the change of the <code>MazeOperator</code> can be.
     */
    double estimateCost(Maze maze, double allowedCost);
}
