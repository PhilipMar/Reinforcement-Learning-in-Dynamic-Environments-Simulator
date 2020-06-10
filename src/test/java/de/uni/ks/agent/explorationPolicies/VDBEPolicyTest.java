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
package de.uni.ks.agent.explorationPolicies;

import de.uni.ks.agent.Action;
import de.uni.ks.agent.QTable;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static de.uni.ks.TestUtils.getDefaultMaze;

class VDBEPolicyTest {

    // tests VDBE policy with high inverse sensitivity and an appearing value difference
    @Test
    void testWithHighInverseSensitivityAndAppearingValueDifference() {
        // create Maze
        Maze myMaze = getDefaultMaze();
        NodeFactory.Node center = myMaze.getStartNode();

        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        // add entry to q-table
        qTable.addEntry(center, actions);

        // create VDBE policy with high inverse sensitivity
        VDBEPolicy vdbePolicy = new VDBEPolicy(3, 0.5, 1234);

        // choose action
        Action action = vdbePolicy.chooseAction(center, qTable);
        qTable.setQValue(center, action, 5.d);

        // do post processing (updates epsilon)
        vdbePolicy.postProcessing(center, action, qTableInitValue, null, qTable);

        // check if epsilon of considered state raised as expected after action selection
        Assertions.assertEquals(0.5337872380968218, vdbePolicy.getEpsilon(center));
    }

    // tests VDBE policy with low inverse sensitivity and an appearing value difference
    @Test
    void testWithLowInverseSensitivityAndAppearingValueDifference() {
        // create Maze
        Maze myMaze = getDefaultMaze();
        NodeFactory.Node center = myMaze.getStartNode();

        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        // add entry to q-table
        qTable.addEntry(center, actions);

        // create VDBE policy with low inverse sensitivity
        VDBEPolicy vdbePolicy = new VDBEPolicy(0.001, 0.5, 1234);

        // choose action
        Action action = vdbePolicy.chooseAction(center, qTable);
        qTable.setQValue(center, action, 5.d);

        // do post processing (updates epsilon)
        vdbePolicy.postProcessing(center, action, qTableInitValue, null, qTable);

        // check if epsilon of considered state raised as expected after action selection
        Assertions.assertEquals(0.625, vdbePolicy.getEpsilon(center));
    }

    // tests VDBE policy with high inverse sensitivity and no value difference
    @Test
    void testWithHighInverseSensitivityAndNoValueDifference() {
        // create Maze
        Maze myMaze = getDefaultMaze();
        NodeFactory.Node center = myMaze.getStartNode();

        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        // add entry to q-table
        qTable.addEntry(center, actions);

        // create VDBE policy with high inverse sensitivity
        VDBEPolicy vdbePolicy = new VDBEPolicy(3, 0.5, 1234);

        // choose action
        Action action = vdbePolicy.chooseAction(center, qTable);
        qTable.setQValue(center, action, 0.5);

        // do post processing (updates epsilon)
        vdbePolicy.postProcessing(center, action, qTableInitValue, null, qTable);

        // check if epsilon of considered state decreased as expected after action selection
        Assertions.assertEquals(0.375, vdbePolicy.getEpsilon(center));
    }

    // tests VDBE policy with low inverse sensitivity and no value difference
    @Test
    void testWithLowInverseSensitivityAndNoValueDifference() {
        // create Maze
        Maze myMaze = getDefaultMaze();
        NodeFactory.Node center = myMaze.getStartNode();

        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        // add entry to q-table
        qTable.addEntry(center, actions);

        // create VDBE policy with low inverse sensitivity
        VDBEPolicy vdbePolicy = new VDBEPolicy(0.001, 0.5, 1234);

        // choose action
        Action action = vdbePolicy.chooseAction(center, qTable);
        qTable.setQValue(center, action, 0.5);

        // do post processing (updates epsilon)
        vdbePolicy.postProcessing(center, action, qTableInitValue, null, qTable);

        // check if epsilon of considered state decreased as expected after action selection
        Assertions.assertEquals(0.375, vdbePolicy.getEpsilon(center));
    }

}
