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
package de.uni.ks.configuration;

import de.uni.ks.configuration.handlers.*;
import de.uni.ks.criterion.Criterion;
import de.uni.ks.criterion.stopEpisode.EndStateReached;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.utils.MazeUtils;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static de.uni.ks.configuration.handlers.HandlerUtils.implementsInterface;

/**
 * This class provides methods to parse a config file to a corresponding {@link Config} instance, and to convert an
 * instance of {@link Config} to its representation as a config file.
 */
public class ConfigManager {

    private static final Identifiers identifiers = new Identifiers();

    /**
     * Reads configuration for the simulator from a file and returns the [Config] object that hold the settings
     * at runtime.
     *
     * @param configFile The file that contains the configuration.
     * @return [Config] holding all settings at runtime.
     * @throws IllegalArgumentException If the file does not exist.
     */
    public static Config readConfig(File configFile) throws IllegalArgumentException {
        StringBuilder fileAsString = new StringBuilder();

        try (Scanner scanner = new Scanner(configFile)) {

            while (scanner.hasNextLine()) {
                fileAsString.append(scanner.nextLine())
                        .append("\n");
            }

            return readConfig(fileAsString.toString(), configFile.getName());
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("The configuration file at [" + configFile.getAbsolutePath() + "] does not exist.");
        }
    }

    /**
     * Reads configuration for the simulator from a [String] representation of the config file, and returns
     * the [Config] object that hold the settings at runtime.
     *
     * @param configString The serialized representation of a {@link Config}.
     * @param trainingName The value {@link Config#trainingName} should have. This value replaces the current value of
     *                     the serialized version.
     * @return [Config] holding all settings at runtime.
     */
    public static Config readConfig(String configString, String trainingName) {
        Config config = new Config();
        config.trainingName = trainingName;

        // Instantiate all handlers that are used for parsing the config file.
        List<KeyHandler> handlers = new ArrayList<>(Arrays.asList(
                new PrimitiveKeyHandler(),
                new ExplorationPolicyHandler(),
                new EpisodeStoppingCriteriaHandler(),
                new LevelChangeCriteriaHandler(),
                new MazeOperatorHandler(),
                new ComplexityFunctionHandler()
        ));

        int lineNumber = 0;

        try (Scanner scanner = new Scanner(configString)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineNumber++;

                if (line.isEmpty() || '#' == line.charAt(0)) {
                    // Skip empty lines and comments.
                    continue;
                }

                int idxOfEq = line.indexOf("=");

                if (idxOfEq == -1) {
                    throw new HandlerUtils.ConfigurationReaderException("Line is malformed: [" + line + "]" +
                            " does not match the syntax key=value.");
                }

                String key = line.substring(0, idxOfEq).trim();
                String value = line.substring(idxOfEq + 1).trim();

                boolean wasHandled = false;
                for (KeyHandler handler : handlers) {
                    wasHandled = handler.handle(key, value, config);
                    if (wasHandled) {
                        break;
                    }
                }

                if (!wasHandled) {
                    writeErrorMessage("Unknown key [" + key + "] is skipped.", lineNumber);
                }
            }
        } catch (HandlerUtils.ConfigurationReaderException e) {
            writeErrorMessage(e.getMessage(), lineNumber);
        }
        return config;
    }

    /**
     * Writes [config] to a file, if the file does not exist already.
     * The file path corresponds to [config.trainingName].
     *
     * @param config The instance of [Config] that should be written to a file.
     * @return [True] if writing [config] is successful, [False] if not.
     * @throws ConfigurationWriterException If {@link #serializeConfig(Config)} throws an exception.
     */
    public static boolean writeConfigToFile(Config config) throws ConfigurationWriterException {

        try {
            File file = new File(config.trainingName.endsWith(".cfg")
                    ? config.trainingName : config.trainingName + ".cfg");

            if (file.exists()) {
                return false;
            }

            boolean wasCreated = file.createNewFile();

            if (!wasCreated) {
                return false;
            }

            return writeConfigToFile(config, file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeConfigToFile(Config config, File file) throws ConfigurationWriterException {

        String text = serializeConfig(config);

        if (!file.getAbsolutePath().endsWith(".cfg")) file = new File(file.getAbsolutePath() + ".cfg");

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(text);
            writer.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Serializes a *valid* instance of [Config]. Valid means that every [Field] can be parsed by [parseField].
     *
     * @param config The instance of [Config] that should be parsed.
     * @return A [String] representing the content of the config file.
     * @throws ConfigurationWriterException If parsing a [Field] of [config] fails.
     */
    public static String serializeConfig(Config config) throws ConfigurationWriterException {
        Map<String, List<Field>> categoryMap = new HashMap<>();

        // Sort fields by category
        for (Field f : config.getClass().getFields()) {

            String sectionName = getSectionName(f);

            categoryMap.putIfAbsent(sectionName, new ArrayList<>());

            categoryMap.get(sectionName).add(f);
        }

        StringBuilder sb = new StringBuilder();

        writePart(categoryMap, sb, config, false);

        String line = "##########################################################################";
        sb.append("\n\n").append(line).append("\n")
                .append("# DO NOT CHANGE THE FOLLOWING PART IF YOU DO NOT KNOW WHAT YOU ARE DOING #")
                .append("\n").append(line).append("\n\n");

        writePart(categoryMap, sb, config, true);

        return sb.toString();
    }

    /**
     * Determines the value of {@link Section#name()} of a given field. If the field is missing that annotation,
     * "Various" is used as the default value.
     *
     * @param field The field to check.
     * @return The value of {@link Section#name()} if present, default value otherwise.
     */
    public static String getSectionName(Field field) {
        String sectionName = "Various"; // Default value for fields without explicit category.

        for (Annotation a : field.getAnnotations()) {
            if (a instanceof Section) {
                sectionName = ((Section) a).name();
            }
        }

        return sectionName;
    }

    /**
     * Writes The configuration for every field of {@link Config} to a file, categorized by their respective
     * {@link Section} annotation. If {@code writeDoNotChange} is true all field that have the {@link DoNotChange}
     * annotation are written to the given {@link StringBuilder}. If the parameter is false all fields that do not
     * have that annotation are written.
     *
     * @param categoryMap      A map that categorizes every field in {@link Config} basend on its {@link Section}
     *                         annotation.
     * @param sb               The {@link StringBuilder} to which is written.
     * @param config           The instance of {@link Config} that contains the actual values.
     * @param writeDoNotChange True if only fields with the {@link DoNotChange} annotation should be written,
     *                         false if all the other fields should be written.
     */
    private static void writePart(Map<String, List<Field>> categoryMap, StringBuilder sb, Config config,
                                  boolean writeDoNotChange) {

        for (String sectionName : categoryMap.keySet()) {
            if (categoryHasFieldsForThisAnnotation(categoryMap.get(sectionName), writeDoNotChange)) {

                // Do not start a new line at the top of the file.
                if (!sb.toString().isEmpty()) {
                    sb.append("\n");
                }

                sb.append("#").append(sectionName).append("\n");

                List<Field> fields = categoryMap.get(sectionName);

                for (Field f : fields) {

                    if ((isDoNotChangeField(f) && writeDoNotChange)
                            || (!isDoNotChangeField(f) && !writeDoNotChange)) {

                        String key = f.getName();
                        String value = parseField(f, config);

                        if (identifiers.getExplorationPolicy().equals(key)) {
                            sb.append(describeAllConstructors(ExplorationPolicyHandler.packagePath,
                                    ExplorationPolicyHandler.interfaceName));
                        } else if (identifiers.getEpisodeStoppingCriteria().equals(key)) {
                            sb.append("# These can be combined: CriterionA, CriterionB, ...")
                                    .append("\n")
                                    .append(describeAllConstructors(EpisodeStoppingCriteriaHandler.packagePath,
                                            EpisodeStoppingCriteriaHandler.interfaceName));
                        } else if (identifiers.getLevelChangeCriteria().equals(key)) {
                            sb.append("# These can be combined: CriterionA, CriterionB, ...")
                                    .append("\n")
                                    .append(describeAllConstructors(LevelChangeCriteriaHandler.packagePath,
                                            LevelChangeCriteriaHandler.interfaceName));
                        } else if (identifiers.getMazeOperators().equals(key)) {
                            sb.append("# These can be combined: OperatorA, OperatorB, ...")
                                    .append("\n")
                                    .append(describeAllConstructors(MazeOperatorHandler.packagePath,
                                            MazeOperatorHandler.interfaceName));
                        } else if (identifiers.getComplexityFunction().equals(key)) {
                            sb.append(describeAllConstructors(ComplexityFunctionHandler.packagePath,
                                    ComplexityFunctionHandler.interfaceName));
                        }


                        sb.append(key)
                                .append(" = ")
                                .append(value)
                                .append("\n");
                    }
                }
            }
        }
    }

    /**
     * Constructs a String that describes all constructors in a specific package. All constructors are included whose
     * class implements a given interface.
     *
     * @param packageName   The package where the classes are located.
     * @param interfaceName The full name of the interface that each class must implement.
     *                      E.g. 'de.uni.ks.package.MyInterface'
     * @return A String that describes all constructors.
     */
    private static String describeAllConstructors(String packageName, String interfaceName) {

        StringBuilder sb = new StringBuilder();

        for (Constructor<?> c : getAllConstructorsFromPackage(packageName, interfaceName)) {
            sb.append(describeConstructor(c)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Constructs a string representation of a constructor that is written to the config file as an explanation. The
     * String follows the following format:
     * <p>
     * ClassName ( <parameter type> <parameter name>, ... )
     *
     * @param constructor The constructor for which the String is created.
     * @return A String representing the constructor.
     */
    private static String describeConstructor(Constructor<?> constructor) {

        StringBuilder sb = new StringBuilder();

        // getSimpleName() does not exist, strip package name form name here.
        String[] split = constructor.getName().split("\\.");
        String name = split[split.length - 1];

        sb.append(Arrays.stream(constructor.getParameters())
                .map(p -> p.getName() + " = " + "<" + p.getType() + ">")
                .collect(Collectors.joining(", ",
                        "# " + name + "(", ")")));

        return sb.toString();
    }

    /**
     * Get the constructor of every class that is located in a specific package, that implements a certain interface.
     *
     * @param packageName   The package where the classes are located. E.g. 'de.uni.ks.agent.explorationPolicies.'.
     * @param interfaceName The full name of the interface that each class must implement.
     *                      E.g. 'de.uni.ks.package.MyInterface'
     * @return List of all the constructors.
     */
    private static List<Constructor<?>> getAllConstructorsFromPackage(String packageName, String interfaceName) {

        return getClassesFromPackage(packageName).stream()
                .filter(c -> !c.isInterface())
                .filter(c -> implementsInterface(c, interfaceName))
                .filter(c -> {
                    int numOfConstructors = c.getDeclaredConstructors().length;
                    if (numOfConstructors > 1) {
                        System.out.println("[getAllConstructorsFromPackage]: The class [" + c.getSimpleName() + "] " +
                                "must have 1 or 0 declared constructors, has " + numOfConstructors + ". Skipping class");
                        return false;
                    }
                    return true;
                })
                .map(c -> c.getDeclaredConstructors()[0])
                .collect(Collectors.toList());
    }

    /**
     * Returns all classes in a specific package.
     *
     * @param packageName The identifier of the package.
     * @return List of all classes in {@code packageName}.
     */
    public static List<Class<?>> getClassesFromPackage(String packageName) {
        packageName = packageName.substring(0, packageName.length() - 1); // Remove the last '.'

        List<Class<?>> classes = new ArrayList<>();

        try (ScanResult scanResult =                // Assign scanResult in try-with-resources
                     new ClassGraph()                    // Create a new ClassGraph instance
//                             .verbose()                      // If you want to enable logging to stderr
                             .enableAllInfo()                // Scan classes, methods, fields, annotations
                             .whitelistPackages(packageName)   // Scan com.xyz and subpackages
                             .scan()) {                      // Perform the scan and return a ScanResult
            ClassInfoList allClasses = scanResult.getAllClasses();

            for (ClassInfo classInfo : allClasses) {
                try {
                    classes.add(Class.forName(classInfo.getName()));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    System.err.println("Class [" + classInfo.getSimpleName() + "] not found, the file is " +
                            "ignored in package [" + packageName + "]");
                }
            }
        }

        return classes;
    }

    /**
     * Determines if a given list of fields is empty when filtered for the {@link DoNotChange} annotation.
     *
     * @param fields           The list of fields.
     * @param writeDoNotChange True if the list must contain a field with the annotation {@link DoNotChange},
     *                         false if it must not contain a field with that annotation.
     * @return If one of the lists entries meets the criterion.
     */
    private static boolean categoryHasFieldsForThisAnnotation(List<Field> fields, boolean writeDoNotChange) {

        if (writeDoNotChange) {
            for (Field f : fields) {
                if (isDoNotChangeField(f)) {
                    return true;
                }
            }
        } else {
            for (Field f : fields) {
                if (!isDoNotChangeField(f)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if a given field has the {@link DoNotChange} annotation.
     *
     * @param field The field to check.
     * @return True if field has annotation {@link DoNotChange}, false if not.
     */
    public static boolean isDoNotChangeField(Field field) {
        boolean doNotChange = false;
        for (Annotation a : field.getAnnotations()) {
            if (a instanceof DoNotChange) {
                doNotChange = true;
                break;
            }
        }
        return doNotChange;
    }

    /**
     * Serializes the values of fields of an instance of [Config] class
     * (i.e. it returns a string representing the field). [Integer], [Double], [Boolean], and [String] are serialized
     * by their respective [toString] method.
     * Other fields must implement the [Interface] [ToConfigWritable] to be serialized.
     *
     * @param field  The field of [config] that should be parsed.
     * @param config The instance of [Config] from which the value is parsed.
     * @return A [String] representing the value of [field].
     * @throws ConfigurationWriterException If parsing the value fails, this happens if the value is null, a List
     *                                      does not contain any values, or if the field does not
     *                                      represent a [ToConfigWritable].
     */
    public static String parseField(Field field, Config config)
            throws ConfigurationWriterException {

        Class<?> type = field.getType();

        try {
            Object fieldInstance = config.getClass().getField(field.getName()).get(config);

            if (fieldInstance == null) {
                throw new ConfigurationWriterException("Illegal state: value of field" +
                        " [" + field.getName() + "] is [null].");
            }

            if (type.equals(Integer.class)
                    || type.equals(Double.class)
                    || type.equals(Boolean.class)
                    || type.equals(String.class)) {
                return fieldInstance.toString();
            } else if (type.equals(List.class)) {
                // Iterate over all objects
                StringBuilder listDescription = new StringBuilder();
                //noinspection unchecked
                for (WritableToConfig item : (List<WritableToConfig>) fieldInstance) {
                    listDescription
                            .append(item.myConfigString())
                            .append(", ");
                }

                if (listDescription.toString().isEmpty()) {
                    // The list does not contain any items.
                    throw new ConfigurationWriterException("The [List] [" + field.getName() + "] does not contain any" +
                            " entries.");
                }

                // Return the list minus the last ", ".
                return listDescription.substring(0, listDescription.length() - 2);
            } else if (new ArrayList<Type>(Arrays.asList(type.getInterfaces()))
                    .contains(WritableToConfig.class)) {
                return ((WritableToConfig) fieldInstance).myConfigString();
            } else {
                throw new ConfigurationWriterException("Type [" + field.getType().getName()
                        + "] of field [" + field.getName() + "] not supported.");
            }
        } catch (NoSuchFieldException e) {
            // This should not happen as we iterate only over existing fields.
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            throw new ConfigurationWriterException("The field [" + field.getName() + "] is not accessible.");
        } catch (ClassCastException e) {
            throw new ConfigurationWriterException("The field [" + field.getName() + "] is not a [List] of type "
                    + WritableToConfig.class.getSimpleName() + ".");
        }
    }

    private static void writeErrorMessage(String msg, int lineNumber) {
        System.err.println("Error in line [" + lineNumber + "]: " + msg);
    }

    /**
     * Custom exception class use by the [ConfigurationReaderWriter] class for all writing operations.
     */
    public static class ConfigurationWriterException extends IllegalArgumentException {

        public ConfigurationWriterException(String msg) {
            super(msg);
        }
    }

    /**
     * Validates that every field in {@link Config} is populated. This means that
     * * Every primitive field must have a non-null value
     * * Every {@code List} must not be empty.
     *
     * @param config The instance of {@link Config} to validate.
     * @throws IllegalArgumentException If one of the fields is not populated. The exception contains an error message
     *                                  for every invalid field.
     */
    public static void validateConfig(Config config) {

        List<String> errorMessages = new ArrayList<>();

        for (Field f : config.getClass().getFields()) {
            try {
                if (f.get(config) == null)
                    errorMessages.add("Field [" + f.getName()
                            + "] of type [" + f.getType().getSimpleName() + "] in config must be set!");

                if (List.class.isAssignableFrom(f.getType())) {
                    if (((List<?>) f.get(config)).isEmpty()) errorMessages.add("Field [" + f.getName()
                            + "] of type [" + f.getType().getSimpleName() + "] in config must not be empty!");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        try {
            NodeFactory.validateParameters(config.numberOfWayColors, config.numberOfWallColors,
                    config.minWallWayBrightnessDifference);
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
        }

        try {
            MazeUtils.validateMazeBuildParameters(config.initialPathLength);
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
        }

        if (config.numberOfLevels <= 0)
            errorMessages.add("Value for parameter [numberOfLevels] must be greater than 0.");

        if (config.delta <= 0) errorMessages.add("Value for parameter [delta] must be greater than 0.");

        // Check if episode stopping criteria contain end state reached as a criterion.
        boolean containsEndStateReached = false;

        for (Criterion criterion : config.episodeStoppingCriteria) {
            if (criterion instanceof EndStateReached) {
                containsEndStateReached = true;
                break;
            }
        }

        if (!containsEndStateReached) System.err.println(EndStateReached.class.getSimpleName() +
                " should be part of the episodeStoppingCriteria.");

        if (errorMessages.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (String errorMessage : errorMessages) {
            sb.append("- ").append(errorMessage).append("\n");
        }
        throw new IllegalArgumentException(sb.toString());
    }
}
