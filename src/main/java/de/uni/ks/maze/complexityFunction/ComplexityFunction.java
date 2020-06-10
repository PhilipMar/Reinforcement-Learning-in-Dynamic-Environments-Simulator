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

import de.uni.ks.configuration.WritableToConfig;
import de.uni.ks.maze.Maze;

public interface ComplexityFunction extends WritableToConfig {

    /**
     * Calculates and returns the complexity of the given maze.
     *
     * @param maze the {@link de.uni.ks.maze.Maze} whose complexity will be calculated and returned
     * @return The calculated complexity of the passed maze.
     */
    double calculateComplexity(Maze maze);
}
