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

import java.lang.reflect.Field;

/**
 * Parses all primitive key-value pairs in the config file. It currently supports parsing {@link Integer}, {@link Double},
 * {@link Boolean} and {@link String} values.
 */
public class PrimitiveKeyHandler implements KeyHandler {

    public static void main(String[] args) {
        Field[] fields = Config.class.getFields();

        for (Field f : fields) {
            System.out.println("F: " + f.getName() + " : " + f.getType().getSimpleName());
        }
    }

    @Override
    public boolean handle(String key, String value, Config config) {

        Field[] fields = config.getClass().getFields();

        for (Field f : fields) {
            if (f.getName().equals(key)) {
                String type = f.getType().getSimpleName();

                try {

                    Field field = config.getClass().getField(key);

                    switch (type) {
                        case "Integer":
                            field.set(config, Integer.parseInt(value));
                            return true;
                        case "Double":
                            field.set(config, Double.parseDouble(value));
                            return true;
                        case "Boolean":
                            field.set(config, HandlerUtils.parseBool(value));
                            return true;
                        case "String":
                            field.set(config, value);
                            return true;
                        default:
                            // This is no primitive key.
                            return false;
                    }
                } catch (NumberFormatException e) {
                    throw new HandlerUtils.ConfigurationReaderException("[" + value + "] " +
                            "is not a valid value for [" + key + "].");
                } catch (IllegalAccessException e) {
                    // This should not happen, all fields in [Config] should be public.
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    // This should not happen, because we actively test if one of the fields matches [key].
                    e.printStackTrace();
                }
            }
        }

        return false;
    }
}
