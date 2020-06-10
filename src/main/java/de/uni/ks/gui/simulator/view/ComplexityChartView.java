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
 * which shows a level number on the X-axis and the complexity on the Y-axis.
 * This class is used to manage a graph that shows the complexity of the labyrinth for each level.
 */
public class ComplexityChartView extends LineChartView {

    private final XYChart.Series<Number, Number> complexitySeries;

    public ComplexityChartView() {
        // create line chart
        super("Level", "Complexity");

        // create complexity series
        complexitySeries = new XYChart.Series<>();
        complexitySeries.setName("Overall Complexity");

        // add series to line chart
        lineChart.getData().add(complexitySeries);
    }

    /**
     * Adds data (complexity per level) to the complexity series.
     *
     * @param levelNr    The number of the level whose complexity will be added.
     * @param complexity The complexity of the passed level.
     */
    public void addDataToSeries(int levelNr, Number complexity) {
        // add complexity to series
        XYChart.Data<Number, Number> newComplexityData = new XYChart.Data<>(levelNr, complexity);
        complexitySeries.getData().add(newComplexityData);

        // update axes of the chart
        updateAxes(complexitySeries);
    }

}
