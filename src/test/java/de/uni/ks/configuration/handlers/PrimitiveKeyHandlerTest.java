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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PrimitiveKeyHandlerTest {

    @Test
    void testHandlerHandlesKey() throws NoSuchFieldException {

        KeyHandler handler = new PrimitiveKeyHandler();

        Assertions.assertTrue(handler.handle(Config.class.getField("generatedWayColorsSeed").getName(),
                "99", new Config()));

        Assertions.assertFalse(handler.handle("wrongKey", "", new Config()));
    }

    @Test
    void testHandlerThrowsExceptionOnKeyValueMismatch() {
        Config config = new Config();

        KeyHandler handler = new PrimitiveKeyHandler();
        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class,
                () -> handler.handle(Config.class.getField("generatedWayColorsSeed").getName(),
                        "22.22", config));
    }

    @Test
    void testHandlerSetsFields() {
        Config config = new Config();

        KeyHandler handler = new PrimitiveKeyHandler();

        handler.handle("generatedWallColorsSeed", "11", config);
        handler.handle("qLearningAlpha", "22.22", config);
        handler.handle("horizontal", "True", config);

        Assertions.assertEquals(11, config.generatedWallColorsSeed.intValue());
        Assertions.assertEquals(22.22, config.qLearningAlpha.doubleValue());
        Assertions.assertTrue(config.horizontal);
    }
}
