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
package de.uni.ks.gui.simulator.view;

import de.uni.ks.logging.Logger;
import javafx.scene.chart.XYChart;

import java.util.HashMap;

/**
 * This class creates and manages the content of a line chart {link en.uni.ks.gui.simulator.view.LineChartView},
 * which shows a episode number on the X-axis and number of actions on the Y-axis.
 * This class is used to manage and plot a graph that shows the number of actions for each episode of a certain level.
 * Furthermore the class manages and plots a graph, that shows the optimal number of actions to solve the level's maze.
 */

public class LiveBehaviourChartView extends LineChartView {

    private final HashMap<Number, XYChart.Series<Number, Number>> resultSeries;
    private final HashMap<Number, XYChart.Series<Number, Number>> optimalSeries;

    public LiveBehaviourChartView() {
        // create line chart
        super("Episode", "Actions");

        // init data structures to save all series (each level has its own)
        resultSeries = new HashMap<>();
        optimalSeries = new HashMap<>();
    }

    /**
     * Gathers and displays the series of the passed level number in the LineChart.
     * Furthermore the corresponding optimal series will be added to the LineChart.
     *
     * @param levelNr The number of the level whose data should be set in the LineChart.
     */
    public void setLiveBehaviourSeries(int levelNr) {
        // get requested series
        XYChart.Series<Number, Number> resultSeries = getOrCreateResultSeries(levelNr);
        XYChart.Series<Number, Number> optimalSeries = getOrCreateOptimalSeries(levelNr);

        // update chart
        //noinspection unchecked
        lineChart.getData().setAll(resultSeries, optimalSeries);
        updateAxes(resultSeries, optimalSeries);
    }

    /**
     * Adds data (actions per episode) to the result series of the passed level number.
     * Furthermore the corresponding optimal series will get expanded.
     *
     * @param levelNr The number of the level whose data should be expanded.
     * @param episode The number of the episode.
     * @param actions The number of actions the agent needed in the episode.
     */
    public void addDataToSeries(int levelNr, Number episode, Number actions) {
        // create a new series or selects the already created result series and add the new data
        XYChart.Series<Number, Number> resultSeries = getOrCreateResultSeries(levelNr);
        XYChart.Data<Number, Number> newResultData = new XYChart.Data<>(episode, actions);
        resultSeries.getData().add(newResultData);

        // create a new series or selects the already created optimal series and add the new data
        XYChart.Series<Number, Number> optimalSeries = getOrCreateOptimalSeries(levelNr);
        XYChart.Data<Number, Number> newData = new XYChart.Data<>(episode, Logger.trainingData.getLevelData(levelNr)
                .getOptimalNumberOfActions());
        optimalSeries.getData().add(newData);

        // update axes of chart if data was added to the current series
        if (lineChart.getData().contains(resultSeries) && lineChart.getData().contains(optimalSeries)) {
            updateAxes(resultSeries, optimalSeries);
        }
    }

    /**
     * Gets or creates the result series of the passed level number.
     *
     * @param levelNr The number of the level whose result series is requested.
     * @return The result series of the passed level number.
     */
    private XYChart.Series<Number, Number> getOrCreateResultSeries(int levelNr) {
        XYChart.Series<Number, Number> resultSeries;
        if (!this.resultSeries.containsKey(levelNr)) {
            resultSeries = new XYChart.Series<>();
            resultSeries.setName("Result Level " + levelNr);
            this.resultSeries.put(levelNr, resultSeries);
        }
        return this.resultSeries.get(levelNr);
    }

    /**
     * Gets or creates the optimal series of the passed level number.
     *
     * @param levelNr The number of the level whose optimal series is requested.
     * @return The optimal series of the passed level number.
     */
    private XYChart.Series<Number, Number> getOrCreateOptimalSeries(int levelNr) {
        XYChart.Series<Number, Number> optimalSeries;
        if (!this.optimalSeries.containsKey(levelNr)) {
            optimalSeries = new XYChart.Series<>();
            optimalSeries.setName("Optimum Level " + levelNr);
            this.optimalSeries.put(levelNr, optimalSeries);
        }
        return this.optimalSeries.get(levelNr);
    }

}
