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

import javafx.scene.chart.XYChart;

/**
 * This class creates and coordinates the content of a line chart {link en.uni.ks.gui.simulator.view.LineChartView},
 * which shows a level number on the X-axis and number of actions on the Y-axis.
 * This class is used to manage and plot a graph that shows the number of actions for each level.
 * Furthermore the class manages and plots a graph, that shows the optimal number of actions for each level.
 */

public class SummaryChartView extends LineChartView {

    private final XYChart.Series<Number, Number> averageSeries;
    private final XYChart.Series<Number, Number> optimalSeries;

    public SummaryChartView() {
        // create line chart
        super("Level", "Actions");

        // create series for optimal number of actions
        averageSeries = new XYChart.Series<>();
        averageSeries.setName("Average Number of Actions");

        // create series for average number of actions
        optimalSeries = new XYChart.Series<>();
        optimalSeries.setName("Optimal Number of Actions");

        // add series to line chart
        lineChart.getData().add(averageSeries);
        lineChart.getData().add(optimalSeries);
    }

    /**
     * Adds data (average number of actions and  optimal number of actions per level) to the corresponding series.
     *
     * @param levelNr                The number of the level whose summary will be refreshed.
     * @param averageNumberOfActions The average number of actions the agent needed of the passed level.
     * @param optimalNumberOfActions The optimal number of actions for the passed level.
     */
    public void addDataToSeries(int levelNr, Number averageNumberOfActions, Number optimalNumberOfActions) {
        // add average number of actions to corresponding series
        XYChart.Data<Number, Number> newAverageData = new XYChart.Data<>(levelNr, averageNumberOfActions);
        averageSeries.getData().add(newAverageData);
        // add optimal number of actions to corresponding series
        XYChart.Data<Number, Number> newOptimalData = new XYChart.Data<>(levelNr, optimalNumberOfActions);
        optimalSeries.getData().add(newOptimalData);

        // update axes of the chart
        updateAxes(averageSeries, optimalSeries);
    }

}
