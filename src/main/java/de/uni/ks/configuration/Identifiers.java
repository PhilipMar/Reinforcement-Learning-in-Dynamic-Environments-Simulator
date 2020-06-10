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

/**
 * This class holds the names of some of the keys of {@link Config}. These names can not be obtained at runtime and must
 * be hard-coded for this reason. These values are used all around the program, e.g. by the
 * {@link de.uni.ks.gui.configurator.ConfigurationUI}. To simplify error-handling this class is used where ever the key
 * names are needed. If one of the names is not valid anymore (e.g. the name was changed by the programmer), this class
 * will throw an exception, informing the programmer of this problem.
 */
public class Identifiers {

    private final String explorationPolicy;
    private final String levelChangeCriteria;
    private final String episodeStoppingCriteria;
    private final String mazeOperators;
    private final String complexityFunction;
    private final String trainingName;

    public Identifiers() {

        Class<Config> clz = Config.class;

        try {
            this.explorationPolicy = clz.getField("explorationPolicy").getName();
            this.levelChangeCriteria = clz.getField("levelChangeCriteria").getName();
            this.episodeStoppingCriteria = clz.getField("episodeStoppingCriteria").getName();
            this.mazeOperators = clz.getField("mazeOperators").getName();
            this.trainingName = clz.getField("trainingName").getName();
            this.complexityFunction = clz.getField("complexityFunction").getName();
        } catch (NoSuchFieldException e) {
            // This informs the user / programmer that something does not work anymore, as the existence of
            // the fields cannot be checked by the compiler, this runtime exception forces the programmer
            // to handle the problem.

            e.printStackTrace();
            throw new RuntimeException("One of the fields in {episodeStoppingCriteria, mazeOperators," +
                    " levelChangeCriteria} does not exist in the config class, " +
                    "the field may have been renamed. To make the config ui work again this has" +
                    " to be fixed manually.");
        }
    }

    public String getExplorationPolicy() {
        return explorationPolicy;
    }

    public String getLevelChangeCriteria() {
        return levelChangeCriteria;
    }

    public String getEpisodeStoppingCriteria() {
        return episodeStoppingCriteria;
    }

    public String getMazeOperators() {
        return mazeOperators;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public String getComplexityFunction() {
        return complexityFunction;
    }
}
