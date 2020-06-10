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

class EpsilonFirstPolicyTest {

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
        int numberOfActions = 500;
        EpsilonFirstPolicy epsilonFirstPolicy = new EpsilonFirstPolicy(1, 0,
                numberOfActions, 42);

        // Policy behaves like random first
        RandomPolicy randomPolicy = new RandomPolicy(42);
        for (int i = 0; i < numberOfActions; i++) {
            Assertions.assertEquals(randomPolicy.chooseAction(center, qTable),
                    epsilonFirstPolicy.chooseAction(center, qTable));
        }


        // Policy then behaves like greedy
        GreedyPolicy greedyPolicy = new GreedyPolicy(42);
        for (int i = 0; i < numberOfActions; i++) {
            Assertions.assertEquals(greedyPolicy.chooseAction(center, qTable),
                    epsilonFirstPolicy.chooseAction(center, qTable));
        }
    }
}
