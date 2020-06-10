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

import javafx.scene.control.Slider;

/**
 * This class creates a Slider which determines the delay between two actions.
 */
public class SpeedSliderView {

    private final Slider speedSlider;
    private static final int MAX_DELAY = 10;
    private static final int MIN_DELAY = 0;
    private static final int DEFAULT_DELAY = 1;

    public SpeedSliderView() {
        speedSlider = createSpeedSlider();
    }

    private Slider createSpeedSlider() {
        Slider speedSlider = new Slider(MIN_DELAY, MAX_DELAY, DEFAULT_DELAY);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(0.5f);
        speedSlider.setBlockIncrement(0.5f);
        return speedSlider;
    }

    public Slider getSpeedSlider() {
        return speedSlider;
    }
}
