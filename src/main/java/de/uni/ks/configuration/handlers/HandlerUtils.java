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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Contains methods that are used by all the {@link KeyHandler}.
 */
public class HandlerUtils {

    /**
     * Creates an instance for the class with name [className] in the package [packagePath]. Also asserts that the
     * class instance implements the required [interface].
     *
     * @param packagePath       The package of the class.
     * @param className         The name of the class.
     * @param parsedArgumentMap The map that contains the parsed arguments.
     * @param interfaceName     The name of the [interface] the class is supposed to implement.
     * @return A reference to the new class instance.
     * @throws ConfigurationReaderException If the class does not implement [interfaceName], or if the creation of the
     *                                      new instance fails otherwise.
     */
    public static Object createClass(String packagePath, String className,
                                     Map<String, String> parsedArgumentMap, String interfaceName)
            throws ConfigurationReaderException {

        try {
            Class<?> clz = Class.forName(packagePath + className);


            if (!implementsInterface(clz, interfaceName)) {
                throw new ConfigurationReaderException("The class [" + className + "] does not " +
                        "implement the required interface [" + interfaceName + "].");
            }

            Constructor<?>[] constructors = clz.getDeclaredConstructors();
            if (constructors.length != 1) {
                throw new ConfigurationReaderException("Currently exactly one constructor per class is supported.\n" +
                        "[" + className + "] has [" + constructors.length + "].");
            }

            Constructor<?> constructor = constructors[0];
            Parameter[] constructorParams = constructor.getParameters();

            Object[] arguments = createArgumentsForNewInstance(constructorParams, parsedArgumentMap);

            return constructor.newInstance(arguments);

        } catch (ClassNotFoundException e) {
            // Apparently the class does not exist.
            throw new ConfigurationReaderException("The class [" + className + "] was not found under [" + packagePath + "]" +
                    "\n Please make sure the file exists, and is in the right directory.");
        } catch (IllegalAccessException e) {
            throw new ConfigurationReaderException("The constructor of [" + className + "] is not accessible." +
                    "\nIt must be a [public] constructor.");
        } catch (InstantiationException e) {
            // What does this mean?
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            throw new ConfigurationReaderException("The constructor of [" + className + "] threw an exception:" +
                    "\n" + e.getCause());
        }
        return null;
    }

    /**
     * Determines if a given class implements an interface.
     *
     * @param clz           The class to check.
     * @param interfaceName The interface the class should implement.
     * @return True if {@code clz} implements the interface, false if not.
     */
    public static boolean implementsInterface(Class<?> clz, String interfaceName) {
        boolean implementsInterface = false;

        for (Class<?> inter : clz.getInterfaces()) {
            if (inter.getName().equals(interfaceName)) {
                implementsInterface = true;
                break;
            }
        }

        return implementsInterface;
    }

    /**
     * Parses the String [s] to its boolean representation.
     * In contrast to `Boolean.parse(String s)` this method will *not* return false in
     * every case that [s] is not a representation of "true".
     *
     * @param s The String to parse.
     * @return [true] if [s] is a representation of true, [false] if [s] is a representation
     * of false.
     * @throws NumberFormatException if [s] is not  a valid boolean representation.
     */
    public static boolean parseBool(String s) throws NumberFormatException {
        if ("true".equalsIgnoreCase(s)) {
            return true;
        } else if ("false".equalsIgnoreCase(s)) {
            return false;
        }

        throw new NumberFormatException("[" + s + "] is not a valid boolean value.");
    }

    /**
     * Attempts to parse [encodedValue] to the required [type].
     *
     * @param encodedValue The encoded value to be parsed.
     * @param type         The primitive data type [encodedValue] should be parsed to.
     * @return An Object holding a reference to the parsed value.
     * @throws ConfigurationReaderException If parsing [encodedValue] fails or if [type] is not supported.
     */
    public static Object parseStringToType(String encodedValue, String type) throws ConfigurationReaderException {

        type = type.toLowerCase();

        try {
            Object returnValue;

            switch (type) {
                case "integer":
                case "int":
                    returnValue = Integer.parseInt(encodedValue);
                    break;
                case "double":
                    returnValue = Double.parseDouble(encodedValue);
                    break;
                case "boolean":
                    returnValue = parseBool(encodedValue);
                    break;
                default:
                    throw new ConfigurationReaderException("Type [" + type + "] of parameter " +
                            "[" + encodedValue + "] is not supported.");
            }

            return returnValue;

        } catch (NumberFormatException e) {
            throw new ConfigurationReaderException("Parameter [" + encodedValue + "] " +
                    "is not of required type [" + type + "].");
        }

    }

    /**
     * Validates that the provided parameters fit the required parameters. If that is the case returns an Array of the
     * parsed values that can be used to call [constructor.newInstance()].
     *
     * @param constructorParams The required parameters of the constructor.
     * @param parameterMap      The map containing the key:value pairs of the parsed parameters.
     * @return Array of parsed values that can be used to call the constructor.
     * @throws ConfigurationReaderException If the provided parameters ([parameterMap]) do
     *                                      not match the required parameters ([constructorParameters]).
     *                                      Or if [parseToString()] fails.
     */
    public static Object[] createArgumentsForNewInstance(Parameter[] constructorParams,
                                                         Map<String, String> parameterMap) throws ConfigurationReaderException {

        for (Parameter p : constructorParams) {
            if (parameterMap.get(p.getName()) == null) {
                throw new ConfigurationReaderException("Parameter [" + p.getName() + "] of type [" + p.getType().getName() + "] is missing." +
                        "\nExpected parameters: [" + parameterArrayToString(constructorParams) + "]." +
                        "\nFound parameters   : [" + mapToString(parameterMap) + "].");
            }
        }

        for (String name : parameterMap.keySet()) {
            boolean isParam = false;
            for (Parameter p : constructorParams) {
                if (p.getName().equals(name)) {
                    isParam = true;
                    break;
                }
            }

            if (!isParam) {
                throw new ConfigurationReaderException(
                        "Parameter [" + name + "] is provided but not expected for this constructor." +
                                "\nExpected parameters: [" + parameterArrayToString(constructorParams) + "].");
            }
        }


        // If we are here everything should be fine so far.
        Object[] arguments = new Object[constructorParams.length];
        for (int i = 0; i < arguments.length; i++) {
            Parameter p = constructorParams[i];
            arguments[i] = parseStringToType(parameterMap.get(p.getName()), p.getType().getSimpleName());
        }

        return arguments;
    }

    /**
     * Returns a String containing all return values of [Parameter.getName()]
     *
     * @param parameters Array of [Parameter] to get the names of.
     * @return String listing all parameter names.
     */
    private static String parameterArrayToString(Parameter[] parameters) {
        try {
            StringBuilder s = new StringBuilder();

            for (Parameter p : parameters) {

                s.append(p.getName())
                        .append(" : ")
                        .append(p.getType().getName())
                        .append(", ");
            }

            return s.substring(0, s.length() - 2);
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    /**
     * Returns a String representation of the maps keys.
     *
     * @param map The map to get the key names of.
     * @return String listing all key names.
     */
    private static String mapToString(Map<String, String> map) {
        try {
            StringBuilder s = new StringBuilder();

            for (String key : map.keySet()) {

                s.append(key)
                        .append(" : ")
                        .append(map.get(key))
                        .append(", ");
            }

            return s.substring(0, s.length() - 2);
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    /**
     * Parses the given String [value] of the format POLICY(p1=x,p2=y,...) and extracts the encoded parameters
     * to a map. The parameter name will be used as the key. Thus the map will contain p1: x, p2:y as its entries.
     *
     * @param input The input String from which the parameters will be extracted.
     * @return A map containing the parameters as key:value pairs. p1=v1 will be mapped to p1:v1.
     * @throws ConfigurationReaderException If parsing the parameters is not possible.
     */
    public static Map<String, String> parseParameters(String input) throws ConfigurationReaderException {
        // Parameters: (p1=v1,p2=v2,...)
        int startOfParameters = input.indexOf("(");
        int endOfParameters = input.indexOf(")");

        if (startOfParameters == -1 || endOfParameters == -1) {
            throw new ConfigurationReaderException("[" + input + "] does not contain" +
                    " the required syntax for parameters: (p1=v1, p2=v2, ...)");
        } else {

            String paramsAsString = input.substring(startOfParameters + 1, endOfParameters); // Remove '(' and ')'.

            Map<String, String> keyValueMap = new HashMap<>();

            // Stop if there are no parameters.
            if (endOfParameters == startOfParameters + 1) {
                return keyValueMap;
            }

            List<String> stringList = splitStringAt(paramsAsString, ",");
            for (String keyValuePair : stringList) {
                int idxOfEq = keyValuePair.indexOf("=");

                if (idxOfEq == -1) {
                    throw new ConfigurationReaderException("[" + keyValuePair + "] in [" + paramsAsString + "]" +
                            " does not match the required syntax of parameters: parameter=value.");
                } else {
                    String key = keyValuePair.substring(0, idxOfEq).trim();
                    String value = keyValuePair.substring(idxOfEq + 1).trim();

                    keyValueMap.put(key, value);
                }
            }

            return keyValueMap;

        }
    }

    /**
     * Parses the given String [s] of the format POLICY(p1=x,p2=y,...) and returns the POLICY part.
     *
     * @param s The [String] to parse.
     * @return The POLICY part of [s].
     * @throws ConfigurationReaderException If parsing the class name is not possible.
     */
    public static String parseClassName(String s) throws ConfigurationReaderException {
        int endIndexOfClassName = s.indexOf("(");

        if (endIndexOfClassName == -1) {
            throw new ConfigurationReaderException("Class name from [" + s + "] could not be extracted.");
        } else {
            return s.substring(0, endIndexOfClassName).trim();
        }
    }

    /**
     * Splits [str] at [delimiter] and returns a list containing all parts of [str].
     *
     * @param str       The String to split.
     * @param delimiter The delimiter to use for splitting.
     * @return List of substrings.
     */
    public static List<String> splitStringAt(String str, String delimiter) {
        Scanner scanner = new Scanner(str);
        scanner.useDelimiter(delimiter);

        ArrayList<String> list = new ArrayList<>();

        while (scanner.hasNext()) {
            list.add(scanner.next());
        }

        scanner.close();
        return list;
    }

    /**
     * Custom exception class for for use by the [ConfigurationReaderWriter] class for all reading operations.
     */
    public static class ConfigurationReaderException extends IllegalArgumentException {

        public ConfigurationReaderException(String msg) {
            super(msg);
        }
    }

    public static List<String> splitCriteriaString(String value, String key) {

        ArrayList<String> rawList = new ArrayList<>(Arrays.asList(value.split("\\)\\s*,")));

        List<String> parametersAsList = new ArrayList<>();
        for (String s : rawList) {
            if (!s.contains(")")) {
                s += ")";
            }

            parametersAsList.add(s);
        }

        if (parametersAsList.size() == 0) {
            throw new HandlerUtils.ConfigurationReaderException("The configuration does not contain any parameters" +
                    " for key [" + key + "].");
        }

        return parametersAsList;
    }

}
