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
package de.uni.ks.configuration.handlers;

import de.uni.ks.configuration.Config;
import de.uni.ks.configuration.Identifiers;
import de.uni.ks.maze.utils.mazeOperators.DeadEndOperator;
import de.uni.ks.maze.utils.mazeOperators.NewPathOperator;
import de.uni.ks.maze.utils.mazeOperators.ResizeOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MazeOperatorHandlerTest {

    private final Identifiers identifiers = new Identifiers();

    @Test
    void testHandlerHandlesKey() throws NoSuchFieldException {
        KeyHandler handler = new MazeOperatorHandler();

        Assertions.assertTrue(handler.handle(identifiers.getMazeOperators(),
                new ResizeOperator(100, 123).myConfigString(), new Config()));

        Assertions.assertFalse(handler.handle("wrongKey", "", new Config()));
    }

    @Test
    void testHandlerThrowsExceptionIfClassIsUnknown() {
        KeyHandler handler = new MazeOperatorHandler();

        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class,
                () -> handler.handle(identifiers.getMazeOperators(), "", new Config()));
    }

    @Test
    void testHandlerCreatesClasses() throws NoSuchFieldException {
        Config config = new Config();

        KeyHandler handler = new MazeOperatorHandler();

        DeadEndOperator first = new DeadEndOperator(3, 5, 40,
                0.5, 123);
        ResizeOperator second = new ResizeOperator(100, 123);
        NewPathOperator third = new NewPathOperator(4, 10, 40, 123);

        handler.handle(identifiers.getMazeOperators(),
                first.myConfigString() + ", "
                        + second.myConfigString() + ", "
                        + third.myConfigString(), config);

        Assertions.assertEquals(first, config.mazeOperators.get(0));
        Assertions.assertEquals(second, config.mazeOperators.get(1));
        Assertions.assertEquals(third, config.mazeOperators.get(2));

        Assertions.assertEquals(3, config.mazeOperators.size());
    }
}
