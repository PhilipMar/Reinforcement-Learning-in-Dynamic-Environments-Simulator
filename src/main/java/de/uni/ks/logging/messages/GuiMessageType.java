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
package de.uni.ks.logging.messages;

/**
 * This enum determines which tabs are displayed in the TabPane Controller of {@link de.uni.ks.gui.simulator.view.LoggerView}.
 * For each value of the enum a separate tab will be created.
 */
public enum GuiMessageType {
    All,
    Policy,
    Maze,
    Criteria,
}
