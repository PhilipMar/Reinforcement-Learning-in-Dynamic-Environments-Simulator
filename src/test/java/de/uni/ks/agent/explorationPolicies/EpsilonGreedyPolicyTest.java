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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.uni.ks.TestUtils.getDefaultMaze;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EpsilonGreedyPolicyTest {

    // With high epsilon it should behave like the random policy.
    @Test
    void testPolicyBehavesLikeRandom() {

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

        // Test that the order of actions the policy chooses depends on the seed only, and not on the q-Value.
        RandomPolicy randomPolicy = new RandomPolicy(42);
        EpsilonGreedyPolicy epsilonGreedyPolicy = new EpsilonGreedyPolicy(1, 42);

        Assertions.assertEquals(randomPolicy.chooseAction(center, qTable), epsilonGreedyPolicy.chooseAction(center, qTable));
        Assertions.assertEquals(randomPolicy.chooseAction(center, qTable), epsilonGreedyPolicy.chooseAction(center, qTable));
        Assertions.assertEquals(randomPolicy.chooseAction(center, qTable), epsilonGreedyPolicy.chooseAction(center, qTable));
        Assertions.assertEquals(randomPolicy.chooseAction(center, qTable), epsilonGreedyPolicy.chooseAction(center, qTable));

        randomPolicy = new RandomPolicy(33);
        epsilonGreedyPolicy = new EpsilonGreedyPolicy(1, 33);

        Assertions.assertEquals(randomPolicy.chooseAction(center, qTable), epsilonGreedyPolicy.chooseAction(center, qTable));
        Assertions.assertEquals(randomPolicy.chooseAction(center, qTable), epsilonGreedyPolicy.chooseAction(center, qTable));
        Assertions.assertEquals(randomPolicy.chooseAction(center, qTable), epsilonGreedyPolicy.chooseAction(center, qTable));
        Assertions.assertEquals(randomPolicy.chooseAction(center, qTable), epsilonGreedyPolicy.chooseAction(center, qTable));
    }

    // With low epsilon it should behave like greedy policy.
    @Test
    void behavesLikeGreedy() {

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

        // init greedy policy
        GreedyPolicy greedy = new GreedyPolicy(54);
        EpsilonGreedyPolicy epsilonGreedyPolicy = new EpsilonGreedyPolicy(0, 54);

        // check if greedy-policy selected greedy action UP
        assertEquals(greedy.chooseAction(center, qTable), epsilonGreedyPolicy.chooseAction(center, qTable));
    }

    // Behaves like greedy-policy when multiple high q-values are available
    @Test
    void testBehavesLikeGreedyWithMultipleBestActions() {

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
        GreedyPolicy greedy = new GreedyPolicy(1234);
        EpsilonGreedyPolicy epsilonGreedyPolicy = new EpsilonGreedyPolicy(0, 1234);

        // check if greedy-policy selected greedy action RIGHT
        assertEquals(greedy.chooseAction(center, qTable), epsilonGreedyPolicy.chooseAction(center, qTable));
    }

    // Test that the policy behaves like greedy policy most of the time.
    @Test
    void testPolicyBehavesLikeGreedyHalfOfTheTime() {

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

        // init greedy-policy
        GreedyPolicy greedy = new GreedyPolicy(54);
        EpsilonGreedyPolicy epsilonGreedyPolicy = new EpsilonGreedyPolicy(0.5, 54);

        // check if greedy policy selected greedy action UP
        assertEquals(greedy.chooseAction(center, qTable), epsilonGreedyPolicy.chooseAction(center, qTable));

        // Test that epsilon greedy behaves like greedy at least 50% of the time
        List<Boolean> areEqualList = new ArrayList<>();
        int tries = 100000;
        for (int i = 0; i < tries; i++) {
            areEqualList.add(greedy.chooseAction(center, qTable).
                    equals(epsilonGreedyPolicy.chooseAction(center, qTable)));
        }

        long count = areEqualList.stream().filter(b -> b).count();

        // It should be save to assume that the random-policy's selection is equal to the greedy's selection some times too.
        Assertions.assertTrue(count > ((tries / 2)));
        // But some of the time the policy chooses random and thus the selection is not equal
        Assertions.assertTrue(count < (tries - (tries / 5)));
    }

}
