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
package de.uni.ks.configuration.handlers;

import de.uni.ks.configuration.Config;

/**
 * KeyHandlers are used to parse complex key-value pairs from the config file.
 */
public interface KeyHandler {

    /**
     * For a given (key = value) pair in the config file, this method is called. The pattern for this is the chain of
     * responsibility. If this handler is responsible for handling the given key, it parses the input and writes the
     * class instances to the config instance.
     *
     * @param key    The key of the key-value pair.
     * @param value  The value of the key-value pair.
     * @param config The config in which the parsed values are saved.
     * @return True, if this handler is responsible for the key. False if not.
     */
    boolean handle(String key, String value, Config config);
}
