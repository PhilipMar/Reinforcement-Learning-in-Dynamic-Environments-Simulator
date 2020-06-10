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

import de.uni.ks.agent.QTable;
import de.uni.ks.configuration.Config;
import de.uni.ks.configuration.ConfigManager;
import de.uni.ks.criterion.Criterion;
import de.uni.ks.logging.data.EpisodeData;
import de.uni.ks.logging.data.LevelData;
import de.uni.ks.logging.data.TrainingData;
import de.uni.ks.logging.messages.GuiMessageLog;
import de.uni.ks.logging.messages.GuiMessageType;
import de.uni.ks.maze.Maze;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;

/**
 * <p> This class is the central logging class of the application. </p>
 * <p> With this class the regular logger data can be accessed through a static instance of (@link TrainingData}
 * Furthermore this class contains the data of the UI log which is stored in a static array of {@link GuiMessageLog} objects, which should be accessed
 * through the {@link #addTextToGuiLog(String content, GuiMessageType type)} method </p>
 * <p>  In Addition, this class contains the logic for writing the data stored in ({@link TrainingData} to disk. </p>
 */
public class Logger {

    // ------ log data during execution of training ------
    public static TrainingData trainingData;
    public static GuiMessageLog[] guiMessageLogs;

    public static boolean guiIsActive = false;
    private static boolean initWarningShowed = false;

    // Flag to show message that images are to big only one time.
    private static boolean ignoreImageCreation = false;

    /**
     * Initializes logger by initializing needed data structures to store log data and gui messages.
     *
     * @param logName       Name under which the log will be saved.
     * @param configData    Config object of training session.
     * @param guiWasStarted Describes if application was started with GUI
     */
    public static void initLogger(String logName, Config configData, boolean guiWasStarted) {

        guiIsActive = guiWasStarted;

        // init trainingData object which will store all training related data
        trainingData = new TrainingData(logName, configData);

        // init guiMessageLogs array by creating a guiMessageLog object for every value of the GuiMessageType enum
        if (guiWasStarted) {
            guiMessageLogs = new GuiMessageLog[GuiMessageType.values().length];
            for (GuiMessageType type : GuiMessageType.values()) {
                guiMessageLogs[type.ordinal()] = new GuiMessageLog();
            }
        }

        // Create level and episode data before training begins:
        LevelData levelData = new LevelData(1);
        Logger.addLevelData(levelData);
        EpisodeData episodeData = new EpisodeData(1);
        Logger.addEpisodeData(1, episodeData);
    }

    /**
     * Adds text to misc log of current training.
     *
     * @param content Text that will be added to misc log.
     */
    public static void addTextToMiscLogOfCurrentTraining(String content) {
        if (trainingData == null) {
            printLoggerInitWarning();
        } else {
            if (CurrentData.currentLevelData == null) {
                trainingData.addTextToMiscLog("Initialisation: " + content);
            } else {
                trainingData.addTextToMiscLog("Level " + CurrentData.currentLevelData.getLevelNr() + ": " + content);
            }
        }
    }

    /**
     * Adds text to misc log of current level.
     *
     * @param content Text that will be added to misc log.
     */
    public static void addTextToMiscLogOfCurrentLevel(String content) {
        if (trainingData == null) {
            printLoggerInitWarning();
        } else if (CurrentData.currentLevelData == null) {
            System.err.println("Can't add text <" + content + "> to misc log of current level. Current level is not set");
        } else if (CurrentData.currentEpisodeData == null) {
            CurrentData.currentLevelData.addTextToMiscLog("Initialisation: " + content);
        } else {
            CurrentData.currentLevelData.addTextToMiscLog("Episode " + CurrentData.currentEpisodeData.getEpisodeNr()
                    + ": " + content);
        }
    }

    /**
     * Adds text to misc log of current episode.
     *
     * @param content Text that will be added to misc log.
     */
    public static void addTextToMiscLogOfCurrentEpisode(String content) {
        if (trainingData == null) {
            printLoggerInitWarning();
        } else if (CurrentData.currentLevelData == null) {
            System.err.println("Can't add text <" + content + "> to misc log of current episode. Current level is not set");
        } else if (CurrentData.currentEpisodeData == null) {
            System.err.println("Can't add text <" + content + "> to misc log of current episode. Current episode is not set");
        } else {
            CurrentData.currentEpisodeData.addTextToMiscLog("Action " + CurrentData.currentActionNumber + ": " + content);
        }
    }

    /**
     * Adds message to the UI log which is displayed in {@link de.uni.ks.gui.simulator.view.LoggerView}.
     * The added message will be placed in the tab that matches the passed {@link GuiMessageType}.
     *
     * @param content Text that will be added to the log.
     * @param type    The type of the message.
     */
    public static void addTextToGuiLog(String content, GuiMessageType type) {
        if (guiIsActive) {
            if (guiMessageLogs == null) {
                printLoggerInitWarning();
            } else {
                guiMessageLogs[type.ordinal()].addTextToLog(content);
                if (type != GuiMessageType.All) {
                    guiMessageLogs[GuiMessageType.All.ordinal()].addTextToLog(content);
                }
            }
        }
    }

    /**
     * Adds separator line to log of all GuiMessageTypes
     */
    public static void addSeparatorLineToGuiLog() {
        if (guiIsActive) {
            if (guiMessageLogs == null) {
                printLoggerInitWarning();
            } else {
                for (GuiMessageType type : GuiMessageType.values()) {
                    guiMessageLogs[type.ordinal()].addTextToLogWithoutPrefix("-------------------------------------" +
                            "-------------------------------------------------------");
                }
            }
        }
    }

    /**
     * Adds level data to training data and updates CurrentData values.
     *
     * @param levelData Level data that will be added.
     */
    public static void addLevelData(LevelData levelData) {
        if (trainingData == null) {
            printLoggerInitWarning();
        } else {
            trainingData.addLevelData(levelData);
        }
    }

    /**
     * adds episode data to level data and updates CurrentData values.
     *
     * @param levelNr     Number of the level to which the episode data will be added.
     * @param episodeData Episode data that will be added.
     */
    public static void addEpisodeData(int levelNr, EpisodeData episodeData) {
        if (trainingData == null) {
            printLoggerInitWarning();
        } else {
            LevelData levelData = trainingData.getLevelData(levelNr);
            if (levelData != null && levelData.getEpisodes() != null) {
                levelData.addEpisodeData(episodeData);
            } else {
                System.err.println("episode data could not be added to level " + levelNr);
            }
        }
    }

    /**
     * This class stores the currently log data objects of the current level and the current episode.
     * Furthermore it contains the number of the last logged action.
     */
    public static class CurrentData {
        public static LevelData currentLevelData;
        public static EpisodeData currentEpisodeData;
        public static int currentActionNumber;
    }

    // ------ write log to hard drive ------
    private static int fileNamePostFix = 0;

    /**
     * Method writes logged data to hard drive. All logs are saved in a general log directory named "Logs".
     * The method will create the general log folder if none is existing yet.
     * If the general log directory is available, the method will create the log of the current training session.
     * The log will be stored in a sub directory of the general log directory. This sub directory will be named after
     * the log name of the training data object.
     * The method will add a number to the log name, if a log with the desired log name already exists. This case will
     * trigger a new recursive method call.
     */
    public static void writeLog() {
        if (trainingData == null) {
            printLoggerInitWarning();
        } else {
            String logName = trainingData.getLogName();

            // Create general log directory
            File generalLogDirectory = new File("Logs");
            if (fileNamePostFix == 0) {
                System.out.println("\nlocation of Logs: " + generalLogDirectory.getAbsolutePath());
                createDirectory("Logs");
            }
            // set new log name
            else {
                logName = logName + " (" + fileNamePostFix + ")";
            }

            // Create directory for current training session
            String trainingPath = generalLogDirectory.getAbsolutePath() + File.separator + logName;
            if (createDirectory(trainingPath)) {
                System.out.println("--------- start creating log '" + logName + "' ---------");
                Logger.addTextToGuiLog("Start creating log '" + logName + "'", GuiMessageType.All);
                // Create log for current training session
                if (writeTrainingLog(trainingPath)) {
                    System.out.println("--------- created log '" + logName + "' successfully ---------");
                    Logger.addTextToGuiLog("Created log '" + logName + "' successfully", GuiMessageType.All);
                } else {
                    System.err.println("--------- creation of log '" + logName + "' failed");
                    Logger.addTextToGuiLog("Creation of log '" + logName + "' failed", GuiMessageType.All);
                }
            } else {
                fileNamePostFix++;
                writeLog();
            }
        }
    }

    /**
     * Writes the whole training log to the logging sub directory.
     *
     * @param path String that represents the path where the log will be saved.
     * @return boolean, which indicates whether the complete log was created successfully or not.
     */
    static boolean writeTrainingLog(String path) {

        // create file for additional logged info of current training if they do exist
        if (!writeMiscTrainingLog(path + File.separator + "misc.txt")) return false;

        if (!writeConfigFile(path + File.separator + trainingData.getConfig().trainingName + ".cfg")) return false;

        if (!writeSummaryFile(path + File.separator + "summary.csv")) return false;

        for (int level = 1; level <= trainingData.getLevels().size(); level++) {

            // create directory for level
            String levelPath = path + File.separator + "Level " + level;
            createDirectory(levelPath);

            // create image file for maze used in this level
            if (!writeMazeImageFile(levelPath + File.separator + "maze.png", level, trainingData.getConfig()
                    .restrictImageSize))
                return false;

            // create file for additional logged info of current level if they do exist
            if (!writeMiscLevelLog(levelPath + File.separator + "misc.txt", level)) return false;

            // create episode related files
            for (int episode = 1; episode <= trainingData.getLevelData(level).getEpisodes().size(); episode++) {

                String episodePath = levelPath + File.separator + "Episode " + episode;
                createDirectory(episodePath);

                // create file that contains q table of current episode
                if (!writeQTableFile(episodePath + File.separator + "qtable.csv", level, episode))
                    return false;

                // create file for additional logged info of current episode if they do exist
                if (!writeMiscEpisodeLog(episodePath + File.separator + "misc.txt", level, episode)) return false;
            }

            // create evaluation file for current level
            String evaluationPath = levelPath + File.separator + "evaluation.csv";
            if (!writeLevelEvaluationFile(evaluationPath, level)) return false;

        }

        return true;
    }

    /**
     * Creates misc training log on hard drive. Does nothing if no misc training data was logged.
     *
     * @param path String that represents the path where the misc training log will be saved.
     * @return boolean, which indicates whether the misc training log was created successfully or not.
     */
    static boolean writeMiscTrainingLog(String path) {
        String fileContent = trainingData.getMiscLog();
        if (!fileContent.equals("")) {
            return createFile(path, fileContent);
        }
        return true;
    }

    /**
     * Creates config file on hard drive.
     *
     * @param path String that represents the path where the config file will be saved.
     * @return boolean, which indicates whether the config file was created successfully or not.
     */
    static boolean writeConfigFile(String path) {
        String fileContent = ConfigManager.serializeConfig(trainingData.getConfig());
        return createFile(path, fileContent);
    }

    /**
     * Creates csv. file with all stored evaluation data on the hard drive.
     * This means the created file contains the number of action and the total reward of all episodes of all levels.
     *
     * @param path String that represents the path where the total evaluation file will be saved.
     * @return boolean, which indicates whether the total evaluation file was created successfully or not.
     */
    static boolean writeTotalEvaluationFile(String path) {
        StringBuilder fileContent = new StringBuilder("Level;Episode;Number Of Actions;Reward;Episode Stop Criterion\n");
        for (LevelData level : trainingData.getLevels()) {
            for (EpisodeData episode : level.getEpisodes()) {
                fileContent
                        .append(level.getLevelNr())
                        .append(";")
                        .append(episode.getEpisodeNr())
                        .append(";")
                        .append(episode.getNumberOfActions())
                        .append(";")
                        .append(episode.getTotalReward())
                        .append(";")
                        .append(episode.getOccurredEpisodeStopCriterion().getLoggerString())
                        .append("\n");
            }
        }
        return createFile(path, fileContent.toString());
    }

    /**
     * Creates a .csv file that portrays a summary of the whole training.
     * This means the created file contains the minimum number of actions needed, the average number of actions taken,
     * the highest achievable reward and the average reward achieved of each level.
     * Furthermore it shows how often which episode stop criterion occurred and which criterion lead to the end of the level.
     *
     * @param path String that represents the path where the total evaluation file will be saved.
     * @return boolean, which indicates whether the summary file was created successfully or not.
     */
    static boolean writeSummaryFile(String path) {
        // init string that contains the future file content + add add some attributes to the headline
        StringBuilder fileContent = new StringBuilder("Level;Complexity;Optimal Number Of Actions;Average Number " +
                "Of Actions;Optimal Reward;Average Reward;");

        // add the names of the used episode abort conditions to the head line
        TreeMap<Criterion, Integer> episodeAbortConditionCounterExample = trainingData.getLevels().get(0)
                .getEpisodeStopCriterionCounter();
        for (Criterion criterion : episodeAbortConditionCounterExample.keySet()) {
            fileContent.append(criterion.getLoggerString()).append(";");
        }

        // add last column identifier
        fileContent.append("Level Abort Criterion\n");

        for (LevelData levelData : trainingData.getLevels()) {
            // add level number to the current row
            fileContent.append(levelData.getLevelNr()).append(";");

            // add complexity to the current row
            fileContent.append(levelData.getComplexity()).append(";");

            // add shortest path length to the current row
            fileContent.append(levelData.getOptimalNumberOfActions()).append(";");

            // add the average number of actions taken to the current row
            fileContent.append(levelData.getAverageNumberOfActions()).append(";");

            // write the optimal reward and the average reward achieved to the current row
            fileContent.append(levelData.getOptimalReward()).append(";");
            fileContent.append(levelData.getAverageReward()).append(";");

            // add counter values of the episode stop criteria to the current row
            TreeMap<Criterion, Integer> episodeStoppingCriterionCounterValues = levelData.getEpisodeStopCriterionCounter();
            for (Integer counterValue : episodeStoppingCriterionCounterValues.values()) {
                fileContent.append(counterValue).append(";");
            }

            // add level abort criterion the the current row
            fileContent.append(levelData.getOccurredLevelAbortCriterion().getLoggerString());

            // begin new row
            fileContent.append("\n");
        }
        return createFile(path, fileContent.toString());
    }

    /**
     * Creates a png file that contains a image of the maze that was used in the corresponding level.
     *
     * @param path            String that represents the path where the maze file will be saved.
     * @param levelNumber     Number of the corresponding level.
     * @param reduceImageSize If the images of the mazes are limited in size.
     * @return False, if writing the file failed. True if the file was written or if the image was to large to create.
     */
    private static boolean writeMazeImageFile(String path, int levelNumber, boolean reduceImageSize) {
        LevelData levelData = trainingData.getLevelData(levelNumber);
        Maze maze = levelData.getMaze();
        BufferedImage image = maze.getMazeAsBufferedImage(reduceImageSize);

        // If the image is to large to write
        if (image == null && !ignoreImageCreation) {
            System.err.println("Image for level " + levelNumber + " is to large, saving images from now on is omitted.");
            ignoreImageCreation = true;
            return true;
        }

        try {
            ImageIO.write(image, "png", new File(path));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates csv. file on hard drive, that contains a all stored result of the corresponding level.
     *
     * @param path        String that represents the path where the evaluation file will be saved.
     * @param levelNumber Number of the corresponding level.
     * @return boolean, which indicates whether the evaluation file was created successfully or not.
     */
    static boolean writeLevelEvaluationFile(String path, int levelNumber) {
        StringBuilder fileContent = new StringBuilder("Episode;Number Of Actions;Reward;Episode Stop Criterion\n");
        LevelData levelData = trainingData.getLevelData(levelNumber);
        for (EpisodeData episode : levelData.getEpisodes()) {
            fileContent.append(episode.getEpisodeNr())
                    .append(";")
                    .append(episode.getNumberOfActions())
                    .append(";")
                    .append(episode.getTotalReward())
                    .append(";")
                    .append(episode.getOccurredEpisodeStopCriterion().getLoggerString())
                    .append("\n");
        }
        return createFile(path, fileContent.toString());
    }

    /**
     * Creates misc level log on hard drive. Does nothing if no misc level data was logged in passed level.
     *
     * @param path        String that represents the path where the misc level log will be saved.
     * @param levelNumber Number of the corresponding level.
     * @return boolean, which indicates whether the misc level log was created successfully or not.
     */
    private static boolean writeMiscLevelLog(String path, int levelNumber) {
        LevelData levelData = trainingData.getLevelData(levelNumber);

        String fileContent = levelData.getMiscLog();
        if (!fileContent.equals("")) {
            return createFile(path, fileContent);
        }
        return true;
    }

    /**
     * Creates csv. file on hard drive, that contains the Q-Table of the corresponding episode of the corresponding level.
     *
     * @param path          String that represents the path where the Q-Table file will be saved.
     * @param levelNumber   Number of the corresponding level.
     * @param episodeNumber Number of the corresponding episode.
     * @return boolean, which indicates whether the Q-Table file was created successfully or not.
     */
    static boolean writeQTableFile(String path, int levelNumber, int episodeNumber) {
        LevelData levelData = trainingData.getLevelData(levelNumber);
        EpisodeData episodeData = levelData.getEpisodeData(episodeNumber);

        QTable qTable = episodeData.getQTable();
        String fileContent = qTable.getCsvString();
        return createFile(path, fileContent);
    }

    /**
     * Creates misc episode log on hard drive. Does nothing if no misc episode data was logged in passed episode.
     *
     * @param path          String that represents the path where the misc episode log will be saved.
     * @param levelNumber   Number of the corresponding level.
     * @param episodeNumber Number of the corresponding episode.
     * @return boolean, which indicates whether the misc episode log was created successfully or not.
     */
    private static boolean writeMiscEpisodeLog(String path, int levelNumber, int episodeNumber) {
        LevelData levelData = trainingData.getLevelData(levelNumber);
        EpisodeData episodeData = levelData.getEpisodeData(episodeNumber);

        String fileContent = episodeData.getMiscLog();
        if (!fileContent.equals("")) {
            return createFile(path, fileContent);
        }
        return true;
    }

    /**
     * Creates directory in passed path.
     *
     * @param path String that represents the path and the name of the directory.
     * @return boolean, which indicates whether the directory was created successfully or not.
     */
    static boolean createDirectory(String path) {
        File directory = new File(path);
        if (directory.exists()) {
            return false;
        } else {
            boolean success = directory.mkdir();
            if (success) {
                return true;
            } else {
                System.err.println("Directory '" + directory.getName() + "' could not be created");
                return false;
            }
        }
    }

    /**
     * Creates file in passed path that contains passed content.
     *
     * @param path String that represents the path and the name of the file.
     * @return boolean, which indicates whether the file was created successfully or not.
     */
    private static boolean createFile(String path, String content) {
        File file = new File(path);
        if (file.exists()) {
            System.err.println("File '" + file.getName() + "' already exists");
            return false;
        } else {
            boolean success = false;
            try {
                success = file.createNewFile();
                Files.write(Paths.get(path), content.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (success) {
                return true;
            } else {
                System.err.println("File '" + file.getName() + "' could not be created");
                return false;
            }
        }
    }

    /**
     * Displays an error message in console if error message has not already been displayed.
     */
    private static void printLoggerInitWarning() {
        if (!initWarningShowed) {
            System.err.println("Can't log data. Logger is not initialized");
            initWarningShowed = true;
        }
    }

}
