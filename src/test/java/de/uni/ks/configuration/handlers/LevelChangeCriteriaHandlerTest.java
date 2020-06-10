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
import de.uni.ks.criterion.changeLevel.MaxEpisodesReached;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LevelChangeCriteriaHandlerTest {

    private final Identifiers identifiers = new Identifiers();

    @Test
    void testHandlerHandlesKey() throws NoSuchFieldException {
        KeyHandler handler = new LevelChangeCriteriaHandler();

        Assertions.assertTrue(handler.handle(identifiers.getLevelChangeCriteria(),
                new MaxEpisodesReached(30).myConfigString(), new Config()));
        Assertions.assertFalse(handler.handle("wrongKey", "", new Config()));
    }

    @Test
    void testHandlerThrowsExceptionIfClassIsUnknown() {
        KeyHandler handler = new LevelChangeCriteriaHandler();

        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class,
                () -> handler.handle(identifiers.getLevelChangeCriteria(),
                        "", new Config()));
    }

    @Test
    void testHandlerCreatesClasses() throws NoSuchFieldException {
        Config config = new Config();

        KeyHandler handler = new LevelChangeCriteriaHandler();

        MaxEpisodesReached first = new MaxEpisodesReached(21);
        MaxEpisodesReached second = new MaxEpisodesReached(42);

        handler.handle(identifiers.getLevelChangeCriteria(),
                first.myConfigString() + ", " + second.myConfigString(), config);

        Assertions.assertEquals(first, config.levelChangeCriteria.get(0));
        Assertions.assertEquals(second, config.levelChangeCriteria.get(1));

        Assertions.assertEquals(2, config.levelChangeCriteria.size());
    }
}
