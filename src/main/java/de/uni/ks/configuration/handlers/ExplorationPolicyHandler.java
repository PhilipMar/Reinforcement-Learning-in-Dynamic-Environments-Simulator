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

import de.uni.ks.agent.explorationPolicies.ExplorationPolicy;
import de.uni.ks.configuration.Config;
import de.uni.ks.configuration.Identifiers;

import java.util.Map;

/**
 * Parses the {@link ExplorationPolicy} string in the config file.
 */
public class ExplorationPolicyHandler implements KeyHandler {

    public static final String packagePath = ExplorationPolicy.class.getPackage().getName() + ".";
    public static final String interfaceName = ExplorationPolicy.class.getName();

    private static final Identifiers identifiers = new Identifiers();

    @Override
    public boolean handle(String key, String value, Config config) {

        if (identifiers.getExplorationPolicy().equals(key)) {

            String className = HandlerUtils.parseClassName(value);
            Map<String, String> parsedArgumentMap = HandlerUtils.parseParameters(value);

            try {
                config.explorationPolicy =
                        (ExplorationPolicy) HandlerUtils.createClass(packagePath, className, parsedArgumentMap, interfaceName);

                return true;
            } catch (ClassCastException e) {
                throw new HandlerUtils.ConfigurationReaderException("The class [" + className + "] does not implement " +
                        "the interface [" + interfaceName + "]." +
                        "\n This is required for use at this part in the program.");
            }
        }

        return false;
    }

}
