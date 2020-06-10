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
package de.uni.ks.logging;

import de.uni.ks.Training;
import de.uni.ks.agent.Action;
import de.uni.ks.agent.QTable;
import de.uni.ks.agent.explorationPolicies.GreedyPolicy;
import de.uni.ks.configuration.Config;
import de.uni.ks.configuration.ConfigManager;
import de.uni.ks.criterion.changeLevel.MaxEpisodesReached;
import de.uni.ks.criterion.stopEpisode.EndStateReached;
import de.uni.ks.logging.data.EpisodeData;
import de.uni.ks.logging.data.LevelData;
import de.uni.ks.logging.data.TrainingData;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.complexityFunction.DefaultComplexityFunction;
import de.uni.ks.maze.utils.mazeOperators.DeadEndOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

class LoggerTest {

    // tests if all files have been created on hard drive (if there is no misc log data)
    @Test
    void testExistenceOfLogFilesNoMiscLogData() {
        // init training data
        TrainingData trainingData = getTestTrainingData();
        Logger.trainingData = trainingData;

        // create directory for test
        deleteDirectory(trainingData.getLogName());
        Logger.createDirectory(trainingData.getLogName());

        // create log in new directory
        boolean success = Logger.writeTrainingLog(trainingData.getLogName());
        Assertions.assertTrue(success, "Complete log was created correctly");

        // ------ check if all "training files" exist ------

        // check for config file
        Assertions.assertTrue(Files.exists(Paths.get(trainingData.getLogName() + "/MyTrainingName.cfg")));

        // check for summary file
        Assertions.assertTrue(Files.exists(Paths.get(trainingData.getLogName() + "/summary.csv")));

        // --- check if all files for level 1 exist ---
        String level1Path = trainingData.getLogName() + "/Level 1";

        // check for level evaluation file
        Assertions.assertTrue(Files.exists(Paths.get(level1Path + "/evaluation.csv")));

        // check for maze file
        Assertions.assertTrue(Files.exists(Paths.get(level1Path + "/maze.png")));

        // - check if all files for episode 1 of level 1 exist -
        String level1Episode1Path = level1Path + "/Episode 1";

        // check for q-table file
        Assertions.assertTrue(Files.exists(Paths.get(level1Episode1Path + "/qtable.csv")));

        // - check if all files for episode 2 of level 1 exist -
        String level1Episode2Path = level1Path + "/Episode 2";

        // check for q-table file
        Assertions.assertTrue(Files.exists(Paths.get(level1Episode2Path + "/qtable.csv")));

        // --- check if all files for level 2 exist ---
        String level2Path = trainingData.getLogName() + "/Level 2";

        // check for level evaluation file
        Assertions.assertTrue(Files.exists(Paths.get(level2Path + "/evaluation.csv")));

        // check for maze file
        Assertions.assertTrue(Files.exists(Paths.get(level2Path + "/maze.png")));

        // - check if all files for episode 1 of level 1 exist -
        String level2Episode1Path = level1Path + "/Episode 1";

        // check for q-table file
        Assertions.assertTrue(Files.exists(Paths.get(level2Episode1Path + "/qtable.csv")));

        // delete log directory
        deleteDirectory(trainingData.getLogName());
    }

    // tests if all files have been created on hard drive (if there is misc log data)
    @Test
    void testExistenceOfLogFilesWithMiscLogData() {
        // init training data
        TrainingData trainingData = getTestTrainingData();
        Logger.trainingData = trainingData;
        String logName = trainingData.getLogName();

        // add misc "training" text log data
        trainingData.addTextToMiscLog("Misc Training Log Message");

        // add misc "level" text log data
        Logger.trainingData.getLevelData(1).addTextToMiscLog("Misc log message level 0");
        Logger.trainingData.getLevelData(2).addTextToMiscLog("Misc log message level 2");

        // add misc "episode" text log data
        Logger.trainingData.getLevelData(1).getEpisodeData(1).addTextToMiscLog("Misc log message level 0 episode 1");
        Logger.trainingData.getLevelData(1).getEpisodeData(2).addTextToMiscLog("Misc log message level 0 episode 2");

        Logger.trainingData.getLevelData(2).getEpisodeData(1).addTextToMiscLog("Misc log message level 0 episode 1");

        // create directory for test
        deleteDirectory(logName);
        Logger.createDirectory(logName);

        // create log in new directory
        boolean success = Logger.writeTrainingLog(trainingData.getLogName());
        Assertions.assertTrue(success, "Complete log with misc log files was created correctly");

        // check for config file
        Assertions.assertTrue(Files.exists(Paths.get(logName + "/MyTrainingName.cfg")));

        // check for summary file
        Assertions.assertTrue(Files.exists(Paths.get(logName + "/summary.csv")));

        // check if misc text log of "training" exists
        Assertions.assertTrue(Files.exists(Paths.get(logName + "/misc.txt")));

        // - check if all files of level 1 exist -
        String level1Path = logName + "/Level 1";

        // check if misc text log of level 1 exists
        Assertions.assertTrue(Files.exists(Paths.get(level1Path + "/misc.txt")));

        // check for level evaluation file
        Assertions.assertTrue(Files.exists(Paths.get(level1Path + "/evaluation.csv")));

        // check for maze file
        Assertions.assertTrue(Files.exists(Paths.get(level1Path + "/maze.png")));

        // - check if all files for episode 1 of level 1 exist -
        String level1Episode1Path = level1Path + "/Episode 1";

        // check if misc text log of episode 1 of level 1 exists
        Assertions.assertTrue(Files.exists(Paths.get(level1Episode1Path + "/misc.txt")));

        // check for q-table file
        Assertions.assertTrue(Files.exists(Paths.get(level1Episode1Path + "/qtable.csv")));

        // - check if all files for episode 2 of level 1 exist -
        String level1Episode2Path = level1Path + "/Episode 2";

        // check if misc text log of episode 2 of level 1 exists
        Assertions.assertTrue(Files.exists(Paths.get(level1Episode2Path + "/misc.txt")));

        // check for q-table file
        Assertions.assertTrue(Files.exists(Paths.get(level1Episode2Path + "/qtable.csv")));

        // - check if all files of level 2 exist -
        String level2Path = logName + "/Level 2";

        // check if misc text log of level 2 exists
        Assertions.assertTrue(Files.exists(Paths.get(level2Path + "/misc.txt")));

        // check for level evaluation file
        Assertions.assertTrue(Files.exists(Paths.get(level2Path + "/evaluation.csv")));

        // check for maze file
        Assertions.assertTrue(Files.exists(Paths.get(level2Path + "/maze.png")));

        String level2Episode1Path = level1Path + "/Episode 1";

        // check if misc text log of episode 1 of level 2 exists
        Assertions.assertTrue(Files.exists(Paths.get(level2Episode1Path + "/misc.txt")));

        // check if misc text log of episode 2 of level 1 exists
        Assertions.assertTrue(Files.exists(Paths.get(level1Episode2Path + "/misc.txt")));

        // check for q-table file
        Assertions.assertTrue(Files.exists(Paths.get(level1Episode2Path + "/qtable.csv")));

        // delete log directory
        deleteDirectory(logName);
    }

    // tests if the misc training text log file will be created correctly
    @Test
    void testMiscTrainingTextLog() {
        // init training data
        TrainingData trainingData = getTestTrainingData();
        Logger.trainingData = trainingData;

        String fileName = "misc.txt";
        String miscTextData = "Test123\nTest456";

        // add misc "training" text log data
        trainingData.addTextToMiscLog(miscTextData);

        // delete possibly existing file
        deleteDirectory(fileName);

        // create misc training text log file
        boolean success = Logger.writeMiscTrainingLog("misc.txt");
        Assertions.assertTrue(success, "Misc log file was created correctly");

        // check if text was written correctly to misc training text log file
        String fileContent = readFile("misc.txt");
        Assertions.assertEquals(miscTextData, fileContent);

        deleteDirectory(fileName);
    }

    // tests if the config file will be created correctly
    @Test
    void testConfigFile() {
        // init training data
        TrainingData trainingData = getTestTrainingData();
        Logger.trainingData = trainingData;

        String fileName = "MyTrainingName.cfg";
        Config trainingConfig = trainingData.getConfig();

        // delete possibly existing file
        deleteDirectory(fileName);

        // create config file
        boolean success = Logger.writeConfigFile(fileName);
        Assertions.assertTrue(success, "Config file was created correctly");

        // create config object from saved file
        Config savedConfig = ConfigManager.readConfig(new File(fileName));

        // check if config information are identical
        Assertions.assertEquals(trainingConfig, savedConfig);

        deleteDirectory(fileName);
    }

    // tests if the summary file will be created correctly
    @Test
    void testSummaryFile() {
        // init training data
        Logger.trainingData = getTestTrainingData();
        String fileName = "summary.csv";

        // delete possibly existing file
        deleteDirectory(fileName);

        // create log in new directory
        boolean success = Logger.writeSummaryFile(fileName);
        Assertions.assertTrue(success, "summary was created correctly");

        // read summary file
        String expectedContent = "Level;Complexity;Optimal Number Of Actions;Average Number Of Actions;Optimal Reward;Average Reward;End State Reached;Level Abort Criterion\n" +
                "1;5.0;2;4.5;-0.05;-3.5;2;Max Episodes Reached (55 Episodes)\n" +
                "2;4.5;1;1.0;0.0;-1.0;1;Max Episodes Reached (55 Episodes)";

        // check if content of summary file is correct
        String actualFileContent = readFile(fileName);
        Assertions.assertEquals(expectedContent, actualFileContent);

        deleteDirectory(fileName);
    }

    // tests if the total evaluation file will be created correctly
    @Test
    void testTotalEvaluationFile() {
        // init training data
        Logger.trainingData = getTestTrainingData();
        String fileName = "evaluation.csv";

        // delete possibly existing file
        deleteDirectory(fileName);

        // create log in new directory
        boolean success = Logger.writeTotalEvaluationFile(fileName);
        Assertions.assertTrue(success, "Total evaluation file was created correctly");

        // check if content of total evaluation file is correct
        String expectedContent = "Level;Episode;Number Of Actions;Reward;Episode Stop Criterion\n" +
                "1;1;5;-5.0;End State Reached\n" +
                "1;2;4;-2.0;End State Reached\n" +
                "2;1;1;-1.0;End State Reached";
        String actualFileContent = readFile(fileName);
        Assertions.assertEquals(expectedContent, actualFileContent);

        deleteDirectory(fileName);
    }

    // tests if the level evaluation file will be created correctly
    @Test
    void testLevelEvaluationFile() {
        // init training data
        Logger.trainingData = getTestTrainingData();
        String fileName = "evaluation.csv";

        // delete possibly existing file
        deleteDirectory(fileName);

        // create level evaluation file of level 1
        boolean success = Logger.writeLevelEvaluationFile(fileName, 1);
        Assertions.assertTrue(success, "level file was created correctly");

        // check if content of level 1 evaluation file is correct.
        String expectedContent = "Episode;Number Of Actions;Reward;Episode Stop Criterion\n" +
                "1;5;-5.0;End State Reached\n" +
                "2;4;-2.0;End State Reached";
        String actualFileContent = readFile(fileName);
        Assertions.assertEquals(expectedContent, actualFileContent);

        deleteDirectory(fileName);
    }

    // tests if the q-table file will be created correctly
    @Test
    void testQTableFile() {
        // init training data
        Logger.trainingData = getTestTrainingData();
        String fileName = "qtable.csv";

        // delete possibly existing file
        deleteDirectory(fileName);

        // create q table file of level 1 episode 1
        boolean success = Logger.writeQTableFile(fileName, 1, 1);
        Assertions.assertTrue(success, "Q Table file was created correctly");

        // check if q table was saved correctly
        String expectedContent = Logger.trainingData.getLevelData(1).getEpisodeData(1).getQTable().getCsvString();
        String actualFileContent = readFile(fileName);
        Assertions.assertEquals(expectedContent, actualFileContent);

        deleteDirectory(fileName);
    }

    // tests if logged maze objects of level 1 and level 2 are not equal
    @Test
    void testLoggingOfMazes() {
        // init training
        Training training = new Training(getTestConfig());
        training.initSimulation();

        // init log data of first level
        LevelData levelData0 = (new LevelData(1));
        Logger.addLevelData(levelData0);

        // log level data of first level
        levelData0.setMaze(training.getMaze());

        // init log data of second level
        LevelData levelData1 = (new LevelData(2));
        Logger.addLevelData(levelData1);

        // log level data of second level
        levelData1.setMaze(training.getMaze());

        // check if logged mazes are unique objects
        TrainingData trainingData = Logger.trainingData;
        Maze mazeLevel1 = trainingData.getLevelData(1).getMaze();
        Maze mazeLevel2 = trainingData.getLevelData(2).getMaze();
        Assertions.assertNotEquals(mazeLevel1, mazeLevel2);
    }

    // tests if logged q-table objects of level 1 episode 1 and level 1 episode 2 are not equal
    @Test
    void testLoggingOfQTables() {
        // init training
        Training training = new Training(getTestConfig());
        training.initSimulation();

        // init log data of first level
        LevelData levelData = (new LevelData(1));
        Logger.addLevelData(levelData);

        //  perform first episode of first level and log episode data
        EpisodeData episodeData0 = new EpisodeData(1);
        Logger.addEpisodeData(1, episodeData0);
        training.doStep();
        episodeData0.setQTable(training.getAgent().getQTable());

        //  perform second episode of first level and log episode data
        EpisodeData episodeData1 = new EpisodeData(2);
        Logger.addEpisodeData(1, episodeData1);
        training.doStep();
        episodeData1.setQTable(training.getAgent().getQTable());

        // check if logged q-tables are unique objects
        TrainingData trainingData = Logger.trainingData;
        QTable qTableLevel1Episode1 = trainingData.getLevelData(1).getEpisodeData(1).getQTable();
        QTable qTableLevel1Episode2 = trainingData.getLevelData(1).getEpisodeData(2).getQTable();
        Assertions.assertNotEquals(qTableLevel1Episode1, qTableLevel1Episode2);
    }

    private TrainingData getTestTrainingData() {
        Config config = getTestConfig();
        TrainingData trainingData = new TrainingData("TestLog", config);

        // create data for level 1
        LevelData level1 = new LevelData(1);
        Maze mazeLevel1 = getTestMazeLevel1();
        level1.setMaze(mazeLevel1);
        level1.refreshComplexity(config.complexityFunction);
        level1.setOccurredLevelAbortCriterion(config.levelChangeCriteria.get(0));
        trainingData.addLevelData(level1);

        // create data for episode 1 of level 1
        EpisodeData level1Episode1 = new EpisodeData(1);
        QTable level1Episode1QTable = getTestQTableLevel1Episode1(mazeLevel1);
        level1Episode1.setNumberOfActions(5);
        level1Episode1.setTotalReward(-5.0d);
        level1Episode1.setQTable(level1Episode1QTable);
        level1Episode1.setOccurredEpisodeStopCriterion(config.episodeStoppingCriteria.get(0));
        level1.addEpisodeData(level1Episode1);

        // create data for episode 2 of level 1
        EpisodeData level1Episode2 = new EpisodeData(2);
        QTable level1Episode2QTable = getTestQTableLevel1Episode2(mazeLevel1);
        level1Episode2.setNumberOfActions(4);
        level1Episode2.setTotalReward(-2.0d);
        level1Episode2.setQTable(level1Episode2QTable);
        level1Episode2.setOccurredEpisodeStopCriterion(config.episodeStoppingCriteria.get(0));
        level1.addEpisodeData(level1Episode2);

        level1.refreshAverageNumberOfActions();
        level1.refreshEpisodeStoppingCriteriaOccurrences(config.episodeStoppingCriteria);
        level1.refreshAverageReward();

        // create data for level 2
        LevelData level2 = new LevelData(2);
        Maze mazeLevel2 = getTestMazeLevel2();
        level2.setMaze(mazeLevel2);
        level2.refreshComplexity(config.complexityFunction);
        level2.setOccurredLevelAbortCriterion(config.levelChangeCriteria.get(0));
        trainingData.addLevelData(level2);

        // create data for episode 1 of level 2
        EpisodeData level2Episode1 = new EpisodeData(1);
        QTable level2Episode1QTable = getTestQTableLevel2Episode1(mazeLevel2);
        level2Episode1.setNumberOfActions(1);
        level2Episode1.setTotalReward(-1.0d);
        level2Episode1.setQTable(level2Episode1QTable);
        level2Episode1.setOccurredEpisodeStopCriterion(config.episodeStoppingCriteria.get(0));
        level2.addEpisodeData(level2Episode1);

        level2.refreshAverageNumberOfActions();
        level2.refreshEpisodeStoppingCriteriaOccurrences(config.episodeStoppingCriteria);
        level2.refreshAverageReward();

        return trainingData;
    }

    private Maze getTestMazeLevel1() {
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        NodeFactory.Node[][] mazeNodes = new NodeFactory.Node[3][3];

        mazeNodes[0][0] = nodeFactory.buildWayNode();
        mazeNodes[0][1] = nodeFactory.buildWayNode();
        mazeNodes[0][2] = nodeFactory.buildWayNode();
        NodeFactory.Node startNode = nodeFactory.buildStartNode();
        mazeNodes[1][1] = startNode;
        mazeNodes[1][0] = nodeFactory.buildWayNode();
        mazeNodes[1][2] = nodeFactory.buildWayNode();
        mazeNodes[2][0] = nodeFactory.buildWayNode();
        mazeNodes[2][1] = nodeFactory.buildWayNode();
        NodeFactory.Node endNode = nodeFactory.buildEndNode();
        mazeNodes[2][2] = endNode;
        return new Maze(nodeFactory, mazeNodes, startNode, endNode);
    }

    private QTable getTestQTableLevel1Episode1(Maze maze) {
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;

        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        qTable.addEntry(maze.getNodeAt(1, 1), actions);

        return qTable;
    }

    private QTable getTestQTableLevel2Episode1(Maze maze) {
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 1.0d;

        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        qTable.addEntry(maze.getNodeAt(1, 1), actions);

        return qTable;
    }

    private QTable getTestQTableLevel1Episode2(Maze maze) {

        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;

        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        qTable.addEntry(maze.getNodeAt(1, 2), actions);

        return qTable;
    }

    private Maze getTestMazeLevel2() {
        NodeFactory nodeFactory = new NodeFactory(-0.05, 0, 1, 1, 0, 0, 0, 0, 200);
        NodeFactory.Node[][] mazeNodes = new NodeFactory.Node[3][3];

        mazeNodes[0][0] = nodeFactory.buildWayNode();
        mazeNodes[0][1] = nodeFactory.buildWayNode();
        mazeNodes[0][2] = nodeFactory.buildWayNode();
        NodeFactory.Node startNode = nodeFactory.buildStartNode();
        mazeNodes[1][1] = startNode;
        mazeNodes[1][0] = nodeFactory.buildWayNode();
        mazeNodes[1][2] = nodeFactory.buildWayNode();
        mazeNodes[2][0] = nodeFactory.buildWayNode();
        NodeFactory.Node endNode = nodeFactory.buildEndNode();
        mazeNodes[2][1] = endNode;
        mazeNodes[2][2] = nodeFactory.buildWayNode();
        return new Maze(nodeFactory, mazeNodes, startNode, endNode);
    }

    private String readFile(String path) {
        StringBuilder fileContent = new StringBuilder();
        File file = new File(path);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            // read file
            ArrayList<String> lines = new ArrayList<>();
            String st;
            while ((st = br.readLine()) != null) {
                lines.add(st);
            }
            // reconstruct file structure (new lines)
            for (int i = 0; i < lines.size(); i++) {
                fileContent.append(lines.get(i));
                if (i < lines.size() - 1) {
                    fileContent.append("\n");
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Assertions.fail(path + " could not be found");
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail(path + " could not be read");
        }
        return fileContent.toString();
    }

    private boolean deleteDirectory(String path) {

        File dir = new File(path);
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < Objects.requireNonNull(children).length; i++) {
                boolean success = deleteDirectory(children[i].getAbsolutePath());
                if (!success) {
                    System.err.println("Deleting " + path + " failed");
                    return false;
                }
            }
        } // either file or an empty directory
        // System.out.println("delete " + dir.getName());
        return dir.delete();
    }

    private Config getTestConfig() {
        Config config = new Config();
        config.trainingName = "MyTrainingName";
        config.qLearningGamma = 1.1;
        config.qLearningAlpha = 2.2;
        config.startEachLevelWithEmptyQTable = false;

        config.showProgressBarInConsole = false;

        config.restrictImageSize = false;

        config.complexityFunction = new DefaultComplexityFunction();

        config.initialQValue = 3.3;
        config.wayNodeReward = 4.4;
        config.endNodeReward = 5.5;

        config.delta = 5.6;
        config.changeMazeSeed = 57;

        config.horizontal = true;
        config.initialPathLength = 5;
        config.numberOfWayColors = 2;
        config.numberOfWallColors = 2;
        config.generatedWayColorsSeed = 123;
        config.generatedWallColorsSeed = 345;
        config.usedWayColorsSeed = 678;
        config.usedWallColorsSeed = 91011;
        config.minWallWayBrightnessDifference = 200;

        config.numberOfLevels = 12;

        config.mazeOperators.add(new DeadEndOperator(2, 5, 40,
                0.5, 123));

        config.explorationPolicy = new GreedyPolicy(42);

        config.episodeStoppingCriteria.add(new EndStateReached());
        config.levelChangeCriteria.add(new MaxEpisodesReached(55));

        return config;
    }
}