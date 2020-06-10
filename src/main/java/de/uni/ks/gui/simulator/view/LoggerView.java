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
import de.uni.ks.logging.messages.GuiMessageLog;
import de.uni.ks.logging.messages.GuiMessageType;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * This class creates the {@link TabPane} Container that displays the UI log.
 * For each value of the enum {@link de.uni.ks.logging.messages.GuiMessageType} a separate tab will be created.
 */
public class LoggerView {

    private final TabPane loggerTabPane;
    private static final int MIN_HEIGHT = 200;
    private static final int MAX_HEIGHT = 325;

    public LoggerView() {
        loggerTabPane = createLoggerTabPane();
    }

    private TabPane createLoggerTabPane() {
        if (Logger.trainingData == null)
            throw new RuntimeException("Can't create GUI Log TabPane. Logger needs to be initialised");
        TabPane loggerTabPane = new TabPane();
        loggerTabPane.setMinHeight(MIN_HEIGHT);
        loggerTabPane.setMaxHeight(MAX_HEIGHT);
        loggerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // create tab for every value in enum 'GuiMessageType'
        for (GuiMessageType messageType : GuiMessageType.values()) {
            ListView<String> listView = createListView(Logger.guiMessageLogs[messageType.ordinal()]);

            // create new tab and assign list view to it
            Tab newTab = new Tab(messageType.name());
            newTab.setContent(listView);

            // add new tab to tabPane
            loggerTabPane.getTabs().add(newTab);
        }

        return loggerTabPane;
    }

    private ListView<String> createListView(GuiMessageLog guiMessageLog) {
        // create ListView for new tab
        ListView<String> listView = new ListView<>();
        listView.setItems(guiMessageLog.getMessages());

        // set default setting of list view: always scroll to bottom of list view
        ListChangeListener<String> scrollListener = c -> listView.scrollTo(c.getList().size() - 1);
        listView.getItems().addListener(scrollListener);

        // stop scrolling if user clicks on log
        listView.setOnMouseClicked(event -> listView.getItems().removeListener(scrollListener));
        return listView;
    }

    public TabPane getLoggerTabPane() {
        return loggerTabPane;
    }
}
