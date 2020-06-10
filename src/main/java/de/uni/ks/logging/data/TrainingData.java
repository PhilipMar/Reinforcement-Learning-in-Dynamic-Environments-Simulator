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
package de.uni.ks.logging.data;

import de.uni.ks.configuration.Config;
import de.uni.ks.logging.Logger;

import java.util.ArrayList;

/**
 * This class can be seen as a data class for an compete training.
 * This class contains some general training related data and a list of {@link LevelData} objects in which all further data is stored.
 */
public class TrainingData extends LogData {
    private final String logName;
    private final Config config;
    private final ArrayList<LevelData> levels;

    public TrainingData(String logName, Config config) {
        this.logName = logName;
        this.config = config;
        levels = new ArrayList<>();
    }

    public void addLevelData(LevelData levelData) {
        this.levels.add(levelData);
        Logger.CurrentData.currentLevelData = levelData;
    }

    public LevelData getLevelData(int levelNr) {
        for (LevelData level : levels) {
            if (level.getLevelNr() == levelNr) {
                return level;
            }
        }
        return null;
    }

    public String getLogName() {
        return logName;
    }

    public Config getConfig() {
        return config;
    }

    public ArrayList<LevelData> getLevels() {
        return levels;
    }

}



