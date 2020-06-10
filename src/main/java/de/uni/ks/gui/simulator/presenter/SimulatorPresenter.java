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
package de.uni.ks.gui.simulator.presenter;

import de.uni.ks.MazeSimulator;
import de.uni.ks.Training;
import de.uni.ks.agent.Action;
import de.uni.ks.agent.QTable;
import de.uni.ks.configuration.Config;
import de.uni.ks.configuration.ConfigManager;
import de.uni.ks.gui.simulator.view.*;
import de.uni.ks.logging.Logger;
import de.uni.ks.logging.data.EpisodeData;
import de.uni.ks.logging.data.LevelData;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * This class creates the graphical user interface by assembling the components
 * implemented in {@link de.uni.ks.gui.simulator.view} into one interface.
 * Furthermore, this class is responsible for updating the UI components.
 * The actual training logic is executed in the {@link de.uni.ks.gui.simulator.presenter.GuiLogicTask} task.
 */
public class SimulatorPresenter extends Application {

    // graphical simulation view
    private Label trainingInfoLabel;
    private MazeCanvasView mazeCanvasView;
    SpeedSliderView speedSliderView;

    // logger view
    private LoggerView loggerView;

    // info view
    private QTableView qTableView;
    private LevelSpinnerView levelSelectionSpinnerResults;
    private ResultTableView resultTableView;
    private LevelSpinnerView levelSelectionSpinnerLiveBehaviour;
    private LiveBehaviourChartView liveBehaviourChartView;
    private ComplexityChartView complexityChartView;
    private SummaryChartView summaryChartView;

    // logical components
    private GuiLogicTask guiLogicTask;
    final Semaphore guiGetsUpdated = new Semaphore(0);
    Training training;

    private Config config;

    // Used by javafx.
    public SimulatorPresenter() {
        this(null);
    }

    public SimulatorPresenter(Config config) {
        this.config = config;
    }

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // The JavaFX CSS logger must be deactivated, otherwise it can happen
        // that strange CSS warnings get displayed after closing the application.
        // This is especially strange because the Simulator UI does not make any CSS changes at all.
        // Could be a bug of JavaFX:
        // https://stackoverflow.com/questions/48796581/how-to-turn-off-css-logging-whn-using-java-fx
        // https://www.eclipse.org/forums/index.php/t/988541/
        com.sun.javafx.util.Logging.getCSSLogger().disableLogging();

        // ---- create training ----
        if (this.config == null) {
            if (this.getParameters().getRaw().size() == 0) {
                System.err.println("User needs to pass config file path");
                System.exit(-1);
            }

            File file = new File(this.getParameters().getRaw().get(0));
            this.config = ConfigManager.readConfig(file);
        }
        training = MazeSimulator.createTraining(config, true);
        training.initSimulation();

        // ---- create left side of the scene ----

        // create info tab pane
        TabPane infoTabPane = new TabPane();
        infoTabPane.setMinWidth(400);
        infoTabPane.setMaxWidth(400);
        infoTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // create q table table view controller
        qTableView = new QTableView();

        // create all components needed for result table
        levelSelectionSpinnerResults = new LevelSpinnerView();
        resultTableView = new ResultTableView();
        VBox resultsVBox = createResultsVBox();

        // create all components needed for live behaviour line chart
        levelSelectionSpinnerLiveBehaviour = new LevelSpinnerView();
        liveBehaviourChartView = new LiveBehaviourChartView();
        VBox liveBehaviourBox = createLiveBehaviourVBox();

        // create all components needed for summary line chart
        summaryChartView = new SummaryChartView();
        VBox summaryChartVBox = createSummaryVBox();

        // create all components needed for complexity line chart
        complexityChartView = new ComplexityChartView();
        VBox complexityVBox = createComplexityVBox();

        // create tab for each info component
        Tab qTableTab = new Tab("Q Table", qTableView.getQTableView());
        Tab resultsTab = new Tab("Results", resultsVBox);
        Tab liveBehaviourTab = new Tab("Live Behaviour", liveBehaviourBox);
        Tab summaryTab = new Tab("Summary", summaryChartVBox);
        Tab complexityTab = new Tab("Complexity", complexityVBox);

        // add each tab to the tab pane
        infoTabPane.getTabs().add(qTableTab);
        infoTabPane.getTabs().add(resultsTab);
        infoTabPane.getTabs().add(liveBehaviourTab);
        infoTabPane.getTabs().add(summaryTab);
        infoTabPane.getTabs().add(complexityTab);

        // ---- create right side of the scene ----

        // create VBox that contains the simulation and the speed slider
        mazeCanvasView = new MazeCanvasView();
        speedSliderView = new SpeedSliderView();
        VBox simulatorVBox = createGraphicVBox();
        VBox.setVgrow(simulatorVBox, Priority.ALWAYS);

        // create TabPane that contains the log
        loggerView = new LoggerView();
        TabPane loggerTabPane = createLoggerContainer(loggerView);

        // create container for the graphical simulation and the logger
        VBox simulatorAndLogBox = new VBox();
        simulatorAndLogBox.getChildren().add(simulatorVBox);
        simulatorAndLogBox.getChildren().add(loggerView.getLoggerTabPane());
        HBox.setHgrow(simulatorAndLogBox, Priority.ALWAYS);

        // add everything to the final container
        HBox root = new HBox();
        root.getChildren().add(infoTabPane);
        root.getChildren().add(simulatorAndLogBox);

        // ---- init UI data ----

        // create entry for the start node before the training begins
        ArrayList<Action> actions = training.getAgent().createActions(training.getAgent().getCurrentPosition());
        training.getAgent().getQTable().addEntry(training.getAgent().getCurrentPosition(), actions);
        qTableView.refresh(training.getAgent().getQTable());

        // highlight start node
        qTableView.selectEntry(training.getAgent().getCurrentPosition().getState());

        // add complexity of first level to complexity chart
        complexityChartView.addDataToSeries(1, training.getConfig().complexityFunction.calculateComplexity(training.getMaze()));

        // ---- show stage ----

        primaryStage.setTitle(training.getConfig().trainingName);
        primaryStage.setMinHeight(700);
        primaryStage.setMinWidth(900);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // ---- start logic ----

        // separate non-FX thread that does the actual training
        guiLogicTask = new GuiLogicTask(this);
        Thread thread = new Thread(guiLogicTask);
        thread.start();
        guiLogicTask.valueProperty().addListener((observable, oldValue, newValue) -> {
            // show log alert if training has been finished
            if (newValue) {
                showLogAlert(training.getConfig().trainingName, "Training has been finished");
            }
        });
    }

    @Override
    public void stop() {
        guiGetsUpdated.release();
        guiLogicTask.cancel();
        Logger.guiIsActive = false;

        if (!training.isFinished()) {
            training.handleRemainingThreads();
        }
    }

    /**
     * Creates and shows alarm that allows the user to decide if the log should be created.
     * If the user agrees, the log gets created. Otherwise the alarm gets closed.
     *
     * @param title      The title of the alarm that will be shown.
     * @param headerText The header text of the alarm that will be shown.
     */
    public void showLogAlert(String title, String headerText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText("Create Log?");

        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonYes) {
            Thread logThread = new Thread(Logger::writeLog);
            logThread.start();
        }
    }

    // ################################################################################################################
    // Graphical Simulation View
    // ################################################################################################################

    private VBox createGraphicVBox() {

        // create container that will contain the maze canvas and the slider
        VBox SimulatorVBox = new VBox();
        SimulatorVBox.setAlignment(Pos.CENTER);

        // create label for general training info
        trainingInfoLabel = new Label("Level: 1\t Episode 1\t Action: 0");

        // create container that will contain the canvas
        HBox simulatorGraphicBox = createCanvasContainer();

        // create container for slider
        HBox sliderContainer = createSpeedSliderContainer();

        // add the slider and canvas container to the main main container
        SimulatorVBox.getChildren().add(trainingInfoLabel);
        SimulatorVBox.getChildren().add(simulatorGraphicBox);
        SimulatorVBox.getChildren().add(sliderContainer);
        return SimulatorVBox;
    }

    private HBox createCanvasContainer() {
        HBox simulatorGraphicBox = new HBox();
        simulatorGraphicBox.setMinHeight(200);
        simulatorGraphicBox.setMinWidth(200);
        simulatorGraphicBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(simulatorGraphicBox, Priority.ALWAYS);
        HBox.setHgrow(simulatorGraphicBox, Priority.ALWAYS);

        // add maze canvas to previously created container
        Canvas mazeCanvas = mazeCanvasView.getMazeCanvas();
        simulatorGraphicBox.getChildren().add(mazeCanvas);

        mazeCanvas.widthProperty().bind(simulatorGraphicBox.widthProperty());
        mazeCanvas.heightProperty().bind(simulatorGraphicBox.heightProperty());
        mazeCanvas.widthProperty().addListener(evt -> mazeCanvasView.drawMaze(training.getMaze(), training.getAgent()));
        mazeCanvas.heightProperty().addListener(evt -> mazeCanvasView.drawMaze(training.getMaze(), training.getAgent()));
        return simulatorGraphicBox;
    }

    private HBox createSpeedSliderContainer() {
        HBox sliderContainer = new HBox();
        sliderContainer.setPadding(new Insets(5, 5, 5, 5));
        sliderContainer.setMinHeight(50);
        sliderContainer.setMaxHeight(100);
        sliderContainer.setAlignment(Pos.CENTER);

        // create descriptive label for slider
        Label sliderLabel = new Label("Delay (seconds)");
        sliderLabel.setMinWidth(85);

        // add slider
        Slider speedSlider = speedSliderView.getSpeedSlider();
        speedSlider.prefWidthProperty().bind(sliderContainer.widthProperty().subtract(sliderLabel.widthProperty()));

        // add slider and descriptive label to slider container
        sliderContainer.getChildren().add(sliderLabel);
        sliderContainer.getChildren().add(speedSlider);
        return sliderContainer;
    }

    // ################################################################################################################
    // Logger View
    // ################################################################################################################

    private TabPane createLoggerContainer(LoggerView loggerView) {
        return loggerView.getLoggerTabPane();
    }

    // ################################################################################################################
    // Info View
    // ################################################################################################################

    private VBox createResultsVBox() {
        VBox resultsVBox = new VBox();

        // create container for level selection spinner + descriptive label
        Spinner<LevelData> levelSelectionSpinner = levelSelectionSpinnerResults.getLevelSelectionSpinner();
        HBox spinnerHBox = createSpinnerHBox(levelSelectionSpinner);

        // change displayed data if spinner value changed
        levelSelectionSpinner.valueProperty().addListener((observable, oldValue, newValue) -> resultTableView.setLevel(newValue));

        // enable vertical resizing of result table view
        VBox.setVgrow(resultTableView.getResultsTableView(), Priority.ALWAYS);

        // add everything to the final container
        resultsVBox.setPadding(new Insets(20, 20, 20, 20));
        resultsVBox.getChildren().add(spinnerHBox);
        resultsVBox.getChildren().add(resultTableView.getResultsTableView());

        return resultsVBox;
    }

    private VBox createLiveBehaviourVBox() {
        // create root container of tab
        VBox liveBehaviourVBox = new VBox();

        // create container for level selection spinner + descriptive label
        Spinner<LevelData> levelSelectionSpinner = levelSelectionSpinnerLiveBehaviour.getLevelSelectionSpinner();
        HBox spinnerHBox = createSpinnerHBox(levelSelectionSpinner);

        // change displayed data if spinner value changed
        levelSelectionSpinner.valueProperty().addListener((observable, oldValue, newValue)
                -> liveBehaviourChartView.setLiveBehaviourSeries(newValue.getLevelNr()));

        // add everything to the final container
        liveBehaviourVBox.setPadding(new Insets(20, 20, 20, 20));
        liveBehaviourVBox.getChildren().add(spinnerHBox);
        liveBehaviourVBox.getChildren().add(liveBehaviourChartView.getLineChart());

        return liveBehaviourVBox;
    }

    private VBox createComplexityVBox() {
        // create root container of tab
        VBox complexityVBox = new VBox();

        // add everything to the final container
        complexityVBox.setPadding(new Insets(20, 20, 20, 20));
        complexityVBox.getChildren().add(complexityChartView.getLineChart());

        return complexityVBox;
    }

    private VBox createSummaryVBox() {
        // create root container of tab
        VBox summaryVBox = new VBox();

        // add everything to the final container
        summaryVBox.setPadding(new Insets(20, 20, 20, 20));
        summaryVBox.getChildren().add(summaryChartView.getLineChart());

        return summaryVBox;
    }


    private HBox createSpinnerHBox(Spinner<LevelData> spinner) {
        // create container for level selection spinner + descriptive label
        HBox spinnerHBox = new HBox();

        // create descriptive label for spinner
        spinnerHBox.setAlignment(Pos.CENTER_LEFT);
        Label levelDescriptionLabel = new Label("Level: ");
        levelDescriptionLabel.setMinWidth(60);

        // create level selection spinner
        levelDescriptionLabel.setPadding(new Insets(0, 20, 0, 0));
        spinner.prefWidthProperty().bind(spinnerHBox.widthProperty()
                .subtract(levelDescriptionLabel.widthProperty()));

        // add label and spinner to spinner container
        spinnerHBox.getChildren().add(levelDescriptionLabel);
        spinnerHBox.getChildren().add(spinner);
        return spinnerHBox;
    }

    // ################################################################################################################
    // GUI logic
    // ################################################################################################################

    /**
     * Updates the UI after a step of the simulation has been completed.
     *
     * @param oldState         The state the agent was in before the last step was executed.
     * @param oldLvlNr         The number of the level the agent was in before the last step was executed.
     * @param oldEpisodeNr     The number of the episode the agent was in before the last step was executed.
     * @param oldActionNr      The number of actions the agent had performed before the last step was executed.
     * @param newState         The state the agent is in after the last step
     * @param newLvlNr         The number of the level the agent is in after the last step.
     * @param newEpisodeNr     The number of the episode the agent is in after the last step.
     * @param newActionNr      The number of actions the agent had performed after the last step.
     * @param trainingFinished Boolean, which tells if the training has been completed.
     */
    void updateUi(String oldState, int oldLvlNr, int oldEpisodeNr, int oldActionNr, String newState, int newLvlNr,
                  int newEpisodeNr, int newActionNr, boolean trainingFinished) {
        // update maze visualisation
        mazeCanvasView.drawMaze(training.getMaze(), training.getAgent());

        // make sure that a list is created for the first level in the result controller, even if the level has only one action
        if (oldLvlNr == 1 || oldEpisodeNr == 1 || oldActionNr == 1) {
            resultTableView.refresh(Logger.trainingData.getLevelData(1));
        }

        // set level spinner data of results tab -> automatically select newest level if level changed and agent had
        // previously selected newest level
        if (levelSelectionSpinnerResults.getValue() == null || levelSelectionSpinnerResults.getValue()
                .getLevelNr() == oldLvlNr) {
            levelSelectionSpinnerResults.setValue(Logger.CurrentData.currentLevelData);
        }

        // set level spinner data of live behaviour tab -> automatically select newest level if level changed and agent
        // had previously selected newest level
        if (levelSelectionSpinnerLiveBehaviour.getValue() == null || levelSelectionSpinnerLiveBehaviour.getValue()
                .getLevelNr() == oldLvlNr) {
            levelSelectionSpinnerLiveBehaviour.setValue(Logger.CurrentData.currentLevelData);
        }

        // update training info label after every action
        trainingInfoLabel.setText("Level: " + newLvlNr + "\t Episode: "
                + newEpisodeNr + "\t Action: " + training.getAgent().getNumberOfActionsTaken());

        // update q table after every action
        QTable qTable = training.getAgent().getQTable();
        qTableView.refresh(qTable);

        // update result table after every action
        resultTableView.refresh(levelSelectionSpinnerResults.getValue());

        // highlight row of new state if level didn't changed
        if (oldLvlNr == newLvlNr) {
            qTableView.selectEntry(newState);
        }

        // update complexity data if level changed
        if (oldLvlNr != newLvlNr) {
            LevelData newLevelData = Logger.trainingData.getLevelData(newLvlNr);
            complexityChartView.addDataToSeries(newLvlNr, newLevelData.getComplexity());
        }

        // update summary data if a level was finished
        if (oldLvlNr != newLvlNr || trainingFinished) {
            LevelData oldLevelData = Logger.trainingData.getLevelData(oldLvlNr);
            summaryChartView.addDataToSeries(oldLvlNr, oldLevelData.getAverageNumberOfActions(),
                    oldLevelData.getOptimalNumberOfActions());
        }

        // update live behavior data if episode changed or training has been finished
        if (oldEpisodeNr != newEpisodeNr || oldLvlNr != newLvlNr || trainingFinished) {
            EpisodeData formerEpisodeData = Logger.trainingData.getLevelData(oldLvlNr).getEpisodeData(oldEpisodeNr);
            liveBehaviourChartView.addDataToSeries(oldLvlNr, oldEpisodeNr, formerEpisodeData.getNumberOfActions());
        }
    }
}
