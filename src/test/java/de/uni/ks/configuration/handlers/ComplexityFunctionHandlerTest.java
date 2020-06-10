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
import de.uni.ks.maze.complexityFunction.DefaultComplexityFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComplexityFunctionHandlerTest {

    private final Identifiers identifiers = new Identifiers();

    @Test
    void testHandlerThrowsExceptionIfClassIsUnknown() {
        KeyHandler handler = new ComplexityFunctionHandler();

        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class,
                () -> handler.handle(identifiers.getComplexityFunction(),
                        "", new Config()));
    }

    @Test
    void testHandlerCreatesClass() throws NoSuchFieldException {
        KeyHandler handler = new ComplexityFunctionHandler();

        Config config = new Config();

        DefaultComplexityFunction complexityFunction = new DefaultComplexityFunction();

        handler.handle(identifiers.getComplexityFunction(),
                complexityFunction.myConfigString(), config);

        Assertions.assertEquals(complexityFunction, config.complexityFunction);
    }
}
