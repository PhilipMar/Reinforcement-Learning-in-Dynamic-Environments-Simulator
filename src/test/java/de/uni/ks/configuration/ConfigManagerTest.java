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
package de.uni.ks.configuration;

import de.uni.ks.agent.explorationPolicies.GreedyPolicy;
import de.uni.ks.criterion.changeLevel.MaxEpisodesReached;
import de.uni.ks.criterion.changeLevel.PerformanceAchievedPercentageTolerance;
import de.uni.ks.criterion.stopEpisode.EndStateReached;
import de.uni.ks.criterion.stopEpisode.MaxActionsReached;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.uni.ks.TestUtils.getTestConfig;
import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {

    private final Identifiers identifiers = new Identifiers();

    @Test
    void testParsingConfigFromFile() {
//        @SuppressWarnings("ConstantConditions") File file = new File(this.getClass().getClassLoader()
//                .getResource("testConfig.cfg").getFile());

        File file = new File("src/test/resources/testConfig.cfg");

        Config config = ConfigManager.readConfig(file);
        System.out.println("Config: " + config);

        // All primitive keys
        assertEquals("configTest", config.trainingName);

        // double keys
        assertEquals(0.0, config.initialQValue.doubleValue());
        assertEquals(-0.05, config.wayNodeReward.doubleValue());
        assertEquals(1, config.endNodeReward.doubleValue());
        assertEquals(1, config.qLearningAlpha.doubleValue());
        assertEquals(0.1, config.qLearningGamma.doubleValue());

        // integer keys
        assertEquals(5, config.initialPathLength.intValue());
        assertEquals(5, config.numberOfWayColors.intValue());
        assertEquals(5, config.numberOfWallColors.intValue());
        assertEquals(123, config.generatedWayColorsSeed.intValue());
        assertEquals(345, config.generatedWallColorsSeed.intValue());
        assertEquals(678, config.usedWayColorsSeed.intValue());
        assertEquals(91011, config.usedWallColorsSeed.intValue());

        assertEquals(3, config.numberOfLevels.intValue());

        // boolean keys
        assertTrue(config.horizontal);
        assertFalse(config.startEachLevelWithEmptyQTable);

        // exploration policy
        assertTrue(config.explorationPolicy instanceof GreedyPolicy);
        assertEquals(123456, ((GreedyPolicy) config.explorationPolicy).getSeed());

        // level change criteria
        assertTrue(config.levelChangeCriteria.get(0) instanceof MaxEpisodesReached);

        assertEquals(21, ((MaxEpisodesReached) config.levelChangeCriteria.get(0)).getNumberOfEpisodes());

        // episode stop criteria
        assertTrue(config.episodeStoppingCriteria.get(0) instanceof EndStateReached);
        assertTrue(config.episodeStoppingCriteria.get(1) instanceof MaxActionsReached);

        assertEquals(55, ((MaxActionsReached) config.episodeStoppingCriteria.get(1)).getMaxActions());
    }

    @Test
    void testParseField() throws NoSuchFieldException {
        Config config = getTestConfig();

        // Integer
        String generatedWallColorsSeed
                = ConfigManager.parseField(config.getClass().getField("generatedWallColorsSeed"), config);
        // Double
        String qLearningAlpha
                = ConfigManager.parseField(config.getClass().getField("qLearningAlpha"), config);
        // Boolean
        String horizontal
                = ConfigManager.parseField(config.getClass().getField("horizontal"), config);
        // String
        String configFileName
                = ConfigManager.parseField(config.getClass().getField(identifiers.getTrainingName()), config);
        // ExplorationPolicy
        String explorationPolicy
                = ConfigManager.parseField(config.getClass().getField(identifiers.getExplorationPolicy()), config);
        // LevelChangeCriteria
        String levelChangeCriteria
                = ConfigManager.parseField(config.getClass().getField(identifiers.getLevelChangeCriteria()), config);
        // EpisodeStoppingCriteria
        String episodeStoppingCriteria
                = ConfigManager.parseField(config.getClass().getField(identifiers.getEpisodeStoppingCriteria()), config);

        assertEquals(config.generatedWallColorsSeed.toString(), generatedWallColorsSeed);
        assertEquals(config.qLearningAlpha.toString(), qLearningAlpha);
        assertEquals(config.trainingName, configFileName);

        assertEquals(config.explorationPolicy.myConfigString(), explorationPolicy);

        assertEquals(config.episodeStoppingCriteria.get(0).myConfigString() + ", "
                + config.episodeStoppingCriteria.get(1).myConfigString() + ", "
                + config.episodeStoppingCriteria.get(2).myConfigString(), episodeStoppingCriteria);

        assertEquals(config.levelChangeCriteria.get(0).myConfigString() + ", "
                + config.levelChangeCriteria.get(1).myConfigString() + ", "
                + config.levelChangeCriteria.get(2).myConfigString(), levelChangeCriteria);
    }

    @Test
    void testParseConfigThrowsExceptions() {
        Config config = getTestConfig();

        Assertions.assertDoesNotThrow(() -> ConfigManager.serializeConfig(config));

        // Integer
        config.generatedWallColorsSeed = null;
        Assertions.assertThrows(ConfigManager.ConfigurationWriterException.class,
                () -> ConfigManager.serializeConfig(config));

        // Double
        Config config2 = getTestConfig();
        config2.qLearningAlpha = null;
        Assertions.assertThrows(ConfigManager.ConfigurationWriterException.class,
                () -> ConfigManager.serializeConfig(config2));

        // Boolean
        Config config3 = getTestConfig();
        config3.horizontal = null;
        Assertions.assertThrows(ConfigManager.ConfigurationWriterException.class,
                () -> ConfigManager.serializeConfig(config3));

        // String
        Config config4 = getTestConfig();
        config4.trainingName = null;
        Assertions.assertThrows(ConfigManager.ConfigurationWriterException.class,
                () -> ConfigManager.serializeConfig(config4));

        // ExplorationPolicy
        Config config5 = getTestConfig();
        config5.explorationPolicy = null;
        Assertions.assertThrows(ConfigManager.ConfigurationWriterException.class,
                () -> ConfigManager.serializeConfig(config5));

        // Level change
        Config config6 = getTestConfig();
        config6.levelChangeCriteria.clear();
        Assertions.assertThrows(ConfigManager.ConfigurationWriterException.class,
                () -> ConfigManager.serializeConfig(config6));

        // Episode stop
        Config config7 = getTestConfig();
        config7.episodeStoppingCriteria.clear();
        Assertions.assertThrows(ConfigManager.ConfigurationWriterException.class,
                () -> ConfigManager.serializeConfig(config7));
    }

    @Test
    void testWriteConfigToFile() {
        Config config = getTestConfig();

        File configFile = new File(config.trainingName + ".cfg");
        //noinspection ResultOfMethodCallIgnored
        configFile.delete();

        boolean wasCreated = ConfigManager.writeConfigToFile(config);
        Assertions.assertTrue(wasCreated);

        boolean wasCreatedAgain = ConfigManager.writeConfigToFile(config);
        assertFalse(wasCreatedAgain);

        Assertions.assertTrue(configFile.exists());

        // Delete file after test.
        boolean fileWasDeleted = configFile.delete();
        Assertions.assertTrue(fileWasDeleted);
    }

    @Test
    void testWrittenConfigCanBeRead() {
        Config config = getTestConfig();

        // Test if parameter of type "Double" is also working.
        config.levelChangeCriteria.add(new PerformanceAchievedPercentageTolerance(5,
                20d));

        File configFile = new File(config.trainingName + ".cfg");
        //noinspection ResultOfMethodCallIgnored
        configFile.delete();

        boolean wasWritten = ConfigManager.writeConfigToFile(config);
        Assertions.assertTrue(wasWritten);

        // This should not throw any exceptions!
        Config readConfig = ConfigManager.readConfig(configFile);

        Assertions.assertEquals(config, readConfig);

        // Delete file after test.
        //noinspection ResultOfMethodCallIgnored
        boolean fileWasDeleted = configFile.delete();
        Assertions.assertTrue(fileWasDeleted);
    }
}
