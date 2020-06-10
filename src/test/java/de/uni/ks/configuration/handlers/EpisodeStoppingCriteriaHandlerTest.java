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
import de.uni.ks.criterion.stopEpisode.EndStateReached;
import de.uni.ks.criterion.stopEpisode.MaxActionsReached;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EpisodeStoppingCriteriaHandlerTest {

    private final Identifiers identifiers = new Identifiers();

    @Test
    void testHandlerHandlesKey() throws NoSuchFieldException {
        KeyHandler handler = new EpisodeStoppingCriteriaHandler();

        Assertions.assertTrue(handler.handle(identifiers.getEpisodeStoppingCriteria(),
                new EndStateReached().myConfigString(), new Config()));

        Assertions.assertFalse(handler.handle("wrongKey", "", new Config()));
    }

    @Test
    void testHandlerThrowsExceptionIfClassIsUnknown() {
        KeyHandler handler = new EpisodeStoppingCriteriaHandler();

        Assertions.assertThrows(HandlerUtils.ConfigurationReaderException.class,
                () -> handler.handle(identifiers.getEpisodeStoppingCriteria(),
                        "", new Config()));
    }

    @Test
    void testHandlerCreatesClasses() throws NoSuchFieldException {
        Config config = new Config();

        KeyHandler handler = new EpisodeStoppingCriteriaHandler();

        EndStateReached first = new EndStateReached();
        MaxActionsReached second = new MaxActionsReached(42);

        handler.handle(identifiers.getEpisodeStoppingCriteria(),
                first.myConfigString() + ", " + second.myConfigString(), config);

        Assertions.assertEquals(first, config.episodeStoppingCriteria.get(0));
        Assertions.assertEquals(second, config.episodeStoppingCriteria.get(1));

        Assertions.assertEquals(2, config.episodeStoppingCriteria.size());

    }
}
