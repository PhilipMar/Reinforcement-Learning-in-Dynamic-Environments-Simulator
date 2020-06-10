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
package de.uni.ks;

import de.uni.ks.agent.Agent;
import de.uni.ks.configuration.Config;
import de.uni.ks.criterion.Criterion;
import de.uni.ks.logging.Logger;
import de.uni.ks.logging.data.EpisodeData;
import de.uni.ks.logging.data.LevelData;
import de.uni.ks.logging.messages.GuiMessageType;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.utils.MazeUtils;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

/**
 * The class represents and performs the actual training.
 */
public class Training {
    private int currentEpisodeNr = 1;
    private int currentLevelNr = 1;
    private boolean isFinished = false;

    private Maze maze;
    private Agent agent;

    private Random operatorRandom;

    private Config config;

    private Timer progressPrinter;
    private final Semaphore isProgressPrinterFinished = new Semaphore(1);
    private final LocalTime startTime = LocalTime.now(); // To show the total running time of the training.

    /**
     * Init the training with the passed config object.
     *
     * @param config The {@link Config} object that will be used in the training.
     */
    public Training(Config config) {
        this.config = config;

        this.operatorRandom = new Random(config.changeMazeSeed);
        if (config.showProgressBarInConsole) startProgressPrinter();
    }

    /**
     * Is used for testing.
     */
    public Training() {
        this.config = new Config();
    }

    /**
     * Perform the complete training.
     */
    public void doTraining() {
        initSimulation();

        while (true) {
            if (!doStep()) break;
        }
    }

    /**
     * Init agent and maze.
     */
    public void initSimulation() {
        if (Logger.trainingData == null)
            throw new RuntimeException("Can't init training. Logger needs to be initialised");
        NodeFactory nodeFactory = new NodeFactory(config.wayNodeReward, config.endNodeReward, config.numberOfWayColors,
                config.numberOfWallColors, config.generatedWayColorsSeed, config.generatedWallColorsSeed,
                config.usedWayColorsSeed, config.usedWallColorsSeed, config.minWallWayBrightnessDifference);
        this.maze = MazeUtils.buildMaze(config.initialPathLength, config.horizontal, nodeFactory);
        this.agent = new Agent(maze.getStartNode(), config.explorationPolicy, config.qLearningAlpha,
                config.qLearningGamma, config.initialQValue);
    }

    /**
     * The method performs a single step of the simulation.
     * This means the method does an agent environment interaction and manages the logging.
     * Furthermore the method checks if the episode or the level has to be changed.
     * If the current episode is finished the method will reset the agent and update all involved counter.
     * If a level abort criterion is true the old level will be replaced by the new one.
     *
     * @return True if simulation hasn't finished. False otherwise.
     */
    public boolean doStep() {
        if (currentLevelNr <= config.numberOfLevels) {

            // set current level log data
            LevelData levelData = Logger.trainingData.getLevelData(currentLevelNr);
            if (levelData == null) {
                levelData = new LevelData(currentLevelNr);
                Logger.addLevelData(levelData);
            }
            if (levelData.getMaze() == null) {
                levelData.setMaze(maze);
                levelData.refreshComplexity(config.complexityFunction);
            }

            // set current episode log data
            EpisodeData episodeData = Logger.trainingData.getLevelData(currentLevelNr).getEpisodeData(currentEpisodeNr);
            if (episodeData == null) {
                episodeData = new EpisodeData(currentEpisodeNr);
                Logger.addEpisodeData(currentLevelNr, episodeData);
            }

            // do action
            Logger.CurrentData.currentActionNumber = this.agent.getNumberOfActionsTaken();
            this.agent.doAction();
            Logger.CurrentData.currentActionNumber = this.agent.getNumberOfActionsTaken();

            // update stats of current episode
            episodeData.setNumberOfActions(this.agent.getNumberOfActionsTaken());
            episodeData.setTotalReward(this.agent.getTotalReward());

            // check if episode has finished
            if (checkForEpisodeStopCriterion()) {
                episodeData.setQTable(this.agent.getQTable());

                // check if level has finished
                if (checkForLevelChangeCriteria()) {

                    // refresh average values of current level data
                    levelData.refreshAverageNumberOfActions();
                    levelData.refreshAverageReward();
                    levelData.refreshEpisodeStoppingCriteriaOccurrences(config.episodeStoppingCriteria);

                    // stop training if last level has been finished
                    if (currentLevelNr == config.numberOfLevels) {
                        Logger.addTextToGuiLog("Training has been finished", GuiMessageType.All);
                        isFinished = true;
                        handleRemainingThreads();
                        System.out.println("\n--------- Finished Training ---------");
                        return false;
                    }

                    // separate new level from old level in GUI Log by adding separator line
                    Logger.addSeparatorLineToGuiLog();

                    // change level
                    currentLevelNr++;
                    currentEpisodeNr = 1;

                    // init new level data
                    LevelData newLevelData = new LevelData(currentLevelNr);
                    Logger.addLevelData(newLevelData);

                    // init new episode data
                    EpisodeData newEpisodeData = new EpisodeData(currentEpisodeNr);
                    Logger.addEpisodeData(currentLevelNr, newEpisodeData);

                    // reset values for new level
                    resetLevelChangeCriteria();
                    Logger.CurrentData.currentActionNumber = 0;
                    if (config.startEachLevelWithEmptyQTable) this.agent.resetQTable();

                    // change maze
                    if (MazeUtils.changeMaze(maze, config.mazeOperators, config.delta, operatorRandom) <= 0) {
                        // No changes could be made.
                        levelData.addTextToMiscLog("Training stopped because no operator " +
                                "could be used on the current maze.");
                        Logger.addTextToGuiLog("Training stopped because no operator " +
                                "could be used on the current maze.", GuiMessageType.All);
                        System.err.println("Training stopped because no operator could be used on the current maze");
                        handleRemainingThreads();
                        throw new RuntimeException("Training stopped because no maze operator could be used on the current maze");
                    }
                    newLevelData.setMaze(maze);
                    newLevelData.refreshComplexity(config.complexityFunction);
                    Logger.addTextToGuiLog("Complexity of new Maze: " + newLevelData.getComplexity(), GuiMessageType.Maze);

                } else {
                    // change episode
                    this.currentEpisodeNr++;
                    EpisodeData newEpisodeData = new EpisodeData(currentEpisodeNr);
                    Logger.addEpisodeData(currentLevelNr, newEpisodeData);
                }

                // reset all values for new episode
                this.agent.resetAgentForEpisode(maze);
                this.resetEpisodeStoppingCriteria();
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if a maze change criterion is true.
     *
     * @return True if level change criteria is met.  Otherwise false.
     */
    boolean checkForLevelChangeCriteria() {
        for (Criterion changeLevelCriterion : config.levelChangeCriteria) {
            if (changeLevelCriterion.isMet(this)) {
                Logger.addTextToGuiLog(changeLevelCriterion.getClass().getSimpleName() + " triggered", GuiMessageType.Criteria);
                Logger.addTextToMiscLogOfCurrentLevel(changeLevelCriterion.getClass().getSimpleName() + " triggered");
                if (Logger.CurrentData.currentLevelData != null) {
                    Logger.CurrentData.currentLevelData.setOccurredLevelAbortCriterion(changeLevelCriterion);
                }
                return true;
            }

        }
        return false;
    }

    /**
     * Resets all level change criteria.
     */
    private void resetLevelChangeCriteria() {
        for (Criterion changeLevelCriterion : config.levelChangeCriteria) {
            changeLevelCriterion.reset();
        }
    }

    /**
     * Checks if an episode stop criterion is true.
     *
     * @return True if episode stop criteria is met. Otherwise false.
     */
    boolean checkForEpisodeStopCriterion() {
        for (Criterion stopEpisodeCriterion : config.episodeStoppingCriteria) {
            if (stopEpisodeCriterion.isMet(this)) {
                Logger.addTextToGuiLog(stopEpisodeCriterion.getLoggerString() + " triggered", GuiMessageType.Criteria);
                Logger.addTextToMiscLogOfCurrentEpisode(stopEpisodeCriterion.getClass().getSimpleName() + " triggered");
                if (Logger.CurrentData.currentEpisodeData != null) {
                    Logger.CurrentData.currentEpisodeData.setOccurredEpisodeStopCriterion(stopEpisodeCriterion);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Resets all episode stopping criteria.
     */
    private void resetEpisodeStoppingCriteria() {
        for (Criterion stopEpisodeCriterion : config.episodeStoppingCriteria) {
            stopEpisodeCriterion.reset();
        }
    }

    /**
     * Terminates all training related threads.
     * Currently only manages progress bar thread after the training has been finished or aborted.
     */
    public void handleRemainingThreads() {
        if (isFinished) {
            try {
                isProgressPrinterFinished.acquire();
            } catch (InterruptedException e) {
                System.err.println("Acquiring semaphore at handleRemainingThreads() failed.");
                e.printStackTrace();
            }
        } else {
            isProgressPrinterFinished.release();
            if (progressPrinter != null) progressPrinter.cancel();

        }
    }

    /**
     * Starts {@link #progressPrinter}, to print the simulation progress to the console, repeated calls to this method
     * are ignored, only the first call starts the printing.
     */
    private void startProgressPrinter() {

        try {
            isProgressPrinterFinished.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Semaphore for progress printer is not available, printer is not started.");
            return;
        }

        // Shows a progress indicator in the console to indicate the current level the training is performing right now.
        // Also shows the percentage of levels that are finished at the moment and the current running time of the training.
        if (progressPrinter == null) {
            progressPrinter = new Timer();
            progressPrinter.schedule(new TimerTask() {
                @Override
                public void run() {
                    final double maxLen = 20;
                    final double numberOfCharsPerLevel = maxLen / config.numberOfLevels;

                    int totalLevels = Training.this.config.numberOfLevels;
                    int currentLevel = Training.this.currentLevelNr;
                    int percentage = isFinished ? 100 : ((currentLevel - 1) * 100) / totalLevels;

                    System.out.print("\r"
                            + "|" + "=".repeat((int) ((currentLevel - 1) * numberOfCharsPerLevel))
                            + ">"
                            + " ".repeat((int) ((totalLevels - currentLevel) * numberOfCharsPerLevel))
                            + "| " + percentage + "%"
                            + " | Running for "
                            + String.format("%02d:%02d:%02d",
                            Duration.between(startTime, LocalTime.now()).toHoursPart(),
                            Duration.between(startTime, LocalTime.now()).toMinutesPart(),
                            Duration.between(startTime, LocalTime.now()).toSecondsPart()));

                    if (currentLevel == totalLevels && isFinished) {
                        progressPrinter.cancel();
                        isProgressPrinterFinished.release();
                    }
                }
            }, 0, 10);
        }
    }

    public Maze getMaze() {
        return maze;
    }

    public void setMaze(Maze maze) {
        this.maze = maze;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Config getConfig() {
        return config;
    }

    public int getCurrentLevelNr() {
        return currentLevelNr;
    }

    public int getCurrentEpisodeNr() {
        return currentEpisodeNr;
    }

    public void setCurrentEpisodeNr(int currentEpisodeNr) {
        this.currentEpisodeNr = currentEpisodeNr;
    }

    public boolean isFinished() {
        return this.isFinished;
    }
}
