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
package de.uni.ks.logging.messages;

import de.uni.ks.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class is the data class of the UI log, which only stores text messages.
 * The messages must be stored in a {@link FXThreadTransformationList}, otherwise there will be problems with updating the UI {@link de.uni.ks.gui.simulator.view.LoggerView}.
 */
public class GuiMessageLog {
    private ObservableList<String> messages;
    private FXThreadTransformationList<String> messagesFX;

    public GuiMessageLog() {
        messages = FXCollections.observableArrayList();
        messagesFX = new FXThreadTransformationList<>(messages);
    }

    public void addTextToLog(String text) {
        int levelNr = Logger.CurrentData.currentLevelData != null
                ? Logger.CurrentData.currentLevelData.getLevelNr() : 0;
        int episodeNr = Logger.CurrentData.currentEpisodeData != null
                ? Logger.CurrentData.currentEpisodeData.getEpisodeNr() : 0;
        int actionNr = Logger.CurrentData.currentActionNumber;
        this.messages.add("Level " + levelNr + " | Episode " + episodeNr + " | Action " + actionNr + ": " + text + "\n");
    }

    public void addTextToLogWithoutPrefix(String text) {
        this.messages.add(text + "\n");
    }

    public FXThreadTransformationList<String> getMessages() {
        return messagesFX;
    }
}
