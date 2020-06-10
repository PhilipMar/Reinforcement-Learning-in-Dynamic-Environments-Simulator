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

class DecreasingEpsilonPolicyTest {

    // tests the development of epsilon.
    @Test
    void testBehaviourChanges() {

        Maze myMaze = getDefaultMaze();
        NodeFactory.Node center = myMaze.getStartNode();

        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;
        Double highestQValue = 1.0;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, highestQValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        // add entry to q-table
        qTable.addEntry(center, actions);

        // Setup policy
        double epsilon_0 = 1.0d;
        double reducingFactor = 0.98d;
        DecreasingEpsilonPolicy decreasingEpsilonPolicy = new DecreasingEpsilonPolicy(epsilon_0, reducingFactor, 1234);

        // assert values of epsilon
        int numberOfActions = 20;
        double desiredEpsilon = epsilon_0;
        for (int i = 1; i < numberOfActions + 1; i++) {
            // choose action
            decreasingEpsilonPolicy.chooseAction(center, qTable);
            // check if epsilon develops as expected
            desiredEpsilon = desiredEpsilon * reducingFactor;
            Assertions.assertEquals(desiredEpsilon, decreasingEpsilonPolicy.getEpsilon());
        }
    }
}
