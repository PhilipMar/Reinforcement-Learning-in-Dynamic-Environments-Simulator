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

/**
 * The enum-value describes if a given {@link de.uni.ks.maze.NodeFactory.Node} in the {@link Maze} is accessible by
 * the {@link de.uni.ks.agent.Agent} or not.
 * The agent can not enter a node that is {@link NodeType#IMPASSABLE}, but it can enter nodes that are
 * {@link NodeType#PASSABLE}.
 */
public enum NodeType {
    IMPASSABLE,
    PASSABLE
}
