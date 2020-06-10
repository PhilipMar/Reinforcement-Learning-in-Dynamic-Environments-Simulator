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

/**
 * This class is the base class for all logger data classes.
 * Reason for this is that all logger data classes must have a mixed log in which text messages can be stored.
 */
class LogData {

    private String miscLog;

    LogData() {
        miscLog = "";
    }

    public String getMiscLog() {
        return miscLog;
    }

    public void addTextToMiscLog(String text) {
        this.miscLog += text + "\n";
    }
}
