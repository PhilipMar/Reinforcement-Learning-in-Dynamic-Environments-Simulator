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

import de.uni.ks.maze.NodeFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * <p>This class performs the training while the simulator ui is running.
 * This is necessary because the JavaFx thread (which is responsible for the UI)
 * otherwise freezes if the training calculations take too long. </p>
 * <p>In addition to the actual training calculations, this class coordinates
 * the time delay between two simulation steps and triggers the UI updates.</p>
 */
class GuiLogicTask extends Task<Boolean> {

    private final SimulatorPresenter simulatorPresenter;

    public GuiLogicTask(SimulatorPresenter simulatorPresenter) {
        this.simulatorPresenter = simulatorPresenter;
    }

    @Override
    protected Boolean call() {
        boolean isNotFinished = true;
        try {
            while (isNotFinished && !isCancelled()) {
                // save some information before executing the next action
                NodeFactory.Node oldNode = simulatorPresenter.training.getAgent().getCurrentPosition();
                String oldState = oldNode.getState();
                int oldLvlNr = simulatorPresenter.training.getCurrentLevelNr();
                int oldEpisodeNr = simulatorPresenter.training.getCurrentEpisodeNr();
                int oldActionNr = simulatorPresenter.training.getAgent().getNumberOfActionsTaken();

                // do action
                isNotFinished = simulatorPresenter.training.doStep();

                // save some information after the execution of the action
                NodeFactory.Node newNode = simulatorPresenter.training.getAgent().getCurrentPosition();
                String newState = newNode.getState();
                int newLvlNr = simulatorPresenter.training.getCurrentLevelNr();
                int newEpisodeNr = simulatorPresenter.training.getCurrentEpisodeNr();
                int newActionNr = simulatorPresenter.training.getAgent().getNumberOfActionsTaken();

                // update UI
                boolean isFinished = !isNotFinished;
                Platform.runLater(() -> {

                    // abort if training has been aborted
                    if (isCancelled()) {
                        simulatorPresenter.guiGetsUpdated.release();
                        return;
                    }

                    // update ui and release semaphore
                    simulatorPresenter.updateUi(oldState, oldLvlNr, oldEpisodeNr, oldActionNr, newState, newLvlNr,
                            newEpisodeNr, newActionNr, isFinished);
                    simulatorPresenter.guiGetsUpdated.release();
                });

                // abort if training has been aborted
                if (isCancelled()) return !isNotFinished;

                // wait until gui update is finished and measure the time passed while doing so
                long startTime = System.nanoTime();
                try {
                    simulatorPresenter.guiGetsUpdated.acquire();
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
                long timeAlreadyWaited = System.nanoTime() - startTime;

                // calculate the remaining delay
                long speedSliderDelayInNanoSeconds = (long) (simulatorPresenter.speedSliderView.getSpeedSlider()
                        .getValue() * 1000000000);
                long remainingDelayInNanoSeconds = (speedSliderDelayInNanoSeconds - timeAlreadyWaited);
                long remainingDelayInMilliSeconds = remainingDelayInNanoSeconds / 1000000;

                // abort if training has been aborted
                if (isCancelled()) return !isNotFinished;

                // wait the possibly remaining delay
                if (remainingDelayInMilliSeconds > 0) {
                    Thread.sleep(remainingDelayInMilliSeconds);
                }
            }
        }
        // InterruptedExceptions can be ignored
        catch (InterruptedException e) {
            //  System.out.println("Gui logic task was interrupted");
        } catch (Exception e) {
            System.err.println("Gui logic task was interrupted due to exception :" + e.getMessage());
            e.printStackTrace();
        }
        return !isNotFinished;
    }
}
