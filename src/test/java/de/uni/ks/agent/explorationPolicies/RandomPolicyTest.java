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

class RandomPolicyTest {

    @Test
    void testRandomPolicyChoosesRandomAction() {

        Maze myMaze = getDefaultMaze();
        NodeFactory.Node center = myMaze.getStartNode();

        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;
        Double highestQValue = 1.0;

        // init Q-Table
        QTable qTable = new QTable(0.0d);
        actions.put(Action.UP, highestQValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        // add entry to q-table
        qTable.addEntry(center, actions);

        // Test that the order of actions the policy chooses depends on the seed only, and not on the q-Value.
        RandomPolicy policy = new RandomPolicy(42);

        Action a1 = policy.chooseAction(center, qTable);
        Action a2 = policy.chooseAction(center, qTable);
        Action a3 = policy.chooseAction(center, qTable);
        Action a4 = policy.chooseAction(center, qTable);

        Assertions.assertEquals(Action.DOWN, a1);
        Assertions.assertEquals(Action.UP, a2);
        Assertions.assertEquals(Action.DOWN, a3);
        Assertions.assertEquals(Action.UP, a4);

        policy = new RandomPolicy(33);

        Action a5 = policy.chooseAction(center, qTable);
        Action a6 = policy.chooseAction(center, qTable);
        Action a7 = policy.chooseAction(center, qTable);
        Action a8 = policy.chooseAction(center, qTable);

        Assertions.assertEquals(Action.DOWN, a5);
        Assertions.assertEquals(Action.DOWN, a6);
        Assertions.assertEquals(Action.LEFT, a7);
        Assertions.assertEquals(Action.UP, a8);
    }
}


