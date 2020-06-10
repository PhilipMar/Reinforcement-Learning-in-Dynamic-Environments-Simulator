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

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * This class is the base class of all line charts used in the UI.
 * This class exists primarily to do the axis scaling uniform for all graphs.
 */
class LineChartView {

    final LineChart<Number, Number> lineChart;

    private final int DEFAULT_LOWER_X_BOUND = 1;
    private final int DEFAULT_UPPER_X_BOUND = 10;
    private final int DEFAULT_X_Tick_UNIT = 1;

    private final int DEFAULT_LOWER_Y_BOUND = 1;
    private final int DEFAULT_UPPER_Y_BOUND = 10;
    private final double DEFAULT_Y_Tick_UNIT = 1;

    LineChartView(String xAxisLabel, String yAxisLabel) {
        lineChart = createLineChart(xAxisLabel, yAxisLabel);
    }

    private LineChart<Number, Number> createLineChart(String xAxisLabel, String yAxisLabel) {
        // create x axis
        NumberAxis xAxis = new NumberAxis(DEFAULT_LOWER_X_BOUND, DEFAULT_UPPER_X_BOUND, DEFAULT_X_Tick_UNIT);
        xAxis.setLabel(xAxisLabel);
        xAxis.setMinorTickVisible(false);

        // create y axis
        NumberAxis yAxis = new NumberAxis(DEFAULT_LOWER_Y_BOUND, DEFAULT_UPPER_Y_BOUND, DEFAULT_Y_Tick_UNIT);
        yAxis.setLabel(yAxisLabel);
        yAxis.setMinorTickVisible(false);

        // create chart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        // thread issues will occur otherwise
        lineChart.setAnimated(false);
        return lineChart;
    }

    /**
     * Determines the lower and upper bound as well as the tick unit for the X and Y axis of the chart.
     * The adjustment of the axes will only take place if the maximum x or the maximum y value of the passed series are
     * higher than {@link #DEFAULT_UPPER_X_BOUND} or {@link #DEFAULT_UPPER_Y_BOUND}.
     *
     * @param series A list of series to be drawn in the graph.
     */
    void updateAxes(XYChart.Series<Number, Number>... series) {
        // get axes
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();

        // determine maximum number of data points for each series
        int maximumNumberOfDataPoints = 0;
        for (XYChart.Series<Number, Number> tmpSeries : series) {
            maximumNumberOfDataPoints = Math.max(tmpSeries.getData().size(), maximumNumberOfDataPoints);
        }

        // use default axes if no data is available
        if (series.length == 0 || maximumNumberOfDataPoints == 0) {
            /// set x axis to default
            xAxis.setLowerBound(DEFAULT_LOWER_X_BOUND);
            xAxis.setUpperBound(DEFAULT_UPPER_X_BOUND);
            xAxis.setTickUnit(DEFAULT_X_Tick_UNIT);
            /// set y axis to default
            xAxis.setLowerBound(DEFAULT_LOWER_Y_BOUND);
            yAxis.setUpperBound(DEFAULT_UPPER_Y_BOUND);
            yAxis.setTickUnit(DEFAULT_Y_Tick_UNIT);
        }
        // otherwise update axes according to the existing data
        else {
            // determine highest x and the highest y value that will be displayed
            double highestXValue = -Double.MAX_VALUE;
            double highestYValue = -Double.MAX_VALUE;
            for (XYChart.Series<Number, Number> tmpSeries : series) {
                for (XYChart.Data<Number, Number> dataPoint : tmpSeries.getData()) {
                    if (dataPoint.getXValue().doubleValue() > highestXValue) {
                        highestXValue = dataPoint.getXValue().doubleValue();
                    }
                    if (dataPoint.getYValue().doubleValue() > highestYValue) {
                        highestYValue = dataPoint.getYValue().doubleValue();
                    }
                }
            }

            // update x axis regarding to the highest possible x value
            if (highestXValue >= DEFAULT_UPPER_X_BOUND) {
                xAxis.setUpperBound((int) highestXValue);
                int tickRateX = (int) (highestXValue / 10);
                if (tickRateX == 0) tickRateX = 1;
                xAxis.setTickUnit(tickRateX);
            } else {
                xAxis.setLowerBound(DEFAULT_LOWER_X_BOUND);
                xAxis.setUpperBound(DEFAULT_UPPER_X_BOUND);
                xAxis.setTickUnit(DEFAULT_X_Tick_UNIT);
            }

            // update y axis regarding to the highest possible y value
            if (highestYValue >= DEFAULT_UPPER_Y_BOUND) {
                yAxis.setUpperBound((int) (highestYValue + highestYValue / 10));
                int tickRateY = (int) (highestYValue / 10);
                if (tickRateY == 0) tickRateY = 1;
                yAxis.setTickUnit(tickRateY);
            } else {
                xAxis.setLowerBound(DEFAULT_LOWER_Y_BOUND);
                yAxis.setUpperBound(DEFAULT_UPPER_Y_BOUND);
                yAxis.setTickUnit(DEFAULT_Y_Tick_UNIT);
            }
        }
    }

    public LineChart<Number, Number> getLineChart() {
        return lineChart;
    }
}
