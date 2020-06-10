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
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static de.uni.ks.TestUtils.getDefaultMaze;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GreedyPolicyTest {

    // tests greedy policy if an action with highest Q-Value exists
    @Test
    void testGreedyPolicyUniqueGreedyAction() {

        Maze myMaze = getDefaultMaze();

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
        qTable.addEntry(myMaze.getStartNode(), actions);

        // init greedy policy
        int seed = 54;
        GreedyPolicy greedy = new GreedyPolicy(seed);

        // select action
        Action action = greedy.chooseAction(myMaze.getStartNode(), qTable);

        // check if greedy policy selected greedy action UP
        assertEquals(Action.UP, action);
    }

    //  tests greedy action selection if multiple actions share the highest Q-Value
    @Test
    void testGreedyPolicyMultipleGreedyAction() {

        Maze myMaze = getDefaultMaze();
        NodeFactory.Node center = myMaze.getStartNode();

        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;
        Double highestQValue = 1.0;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, highestQValue);
        actions.put(Action.RIGHT, highestQValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        // add entry to q-table
        qTable.addEntry(center, actions);

        // init greedy policy
        int seed = 1234;
        GreedyPolicy greedy = new GreedyPolicy(seed);

        // select action
        Action action = greedy.chooseAction(center, qTable);

        // check if greedy policy selected greedy action RIGHT
        assertEquals(Action.RIGHT, action, "select greedy action regarding to seed");
    }
}
