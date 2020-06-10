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
package de.uni.ks.agent;

import de.uni.ks.TestUtils;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory.Node;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class QTableTest {


    // tests adding of entry (as hashMap) to QTable with new state (no entry exists)
    @Test
    void testAddEntryWithHashMapIfNoEntryExists() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        // add entry to empty Q-Table
        boolean isCreatedCorrectly = qTable.addEntry(node, actions);

        // check if entry was created correctly
        assertTrue(isCreatedCorrectly, "entry was added");
        assertTrue(qTable.stateExists(node), "state was created correctly");
        assertEquals(4, qTable.getActions(node).size(), "4 actions were stored in q-table");
        assertTrue(qTable.actionExists(node, Action.UP), "action up was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.RIGHT), "action right was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.DOWN), "action down was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.LEFT), "action left was added successfully to state");

    }

    // tests adding of entry (as hashMap)  to QTable if entry for the passed state already exists
    @Test
    void testAddEntryWithHashMapIfEntryAlreadyExists() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();


        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        // add entry to empty Q-Table
        boolean firstEntryIsCreatedCorrectly = qTable.addEntry(node, actions);

        // check if first entry was created correctly
        assertTrue(firstEntryIsCreatedCorrectly, "entry was added");
        assertTrue(qTable.stateExists(node), "state was created correctly");
        assertEquals(1, qTable.qTable.size(), "only one state exists in q-table");
        assertEquals(4, qTable.getActions(node).size(), "4 actions were stored in q-table");
        assertTrue(qTable.actionExists(node, Action.UP), "action up was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.RIGHT), "action right was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.DOWN), "action down was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.LEFT), "action left was added successfully to state");

        // try to add same entry again
        boolean secondEntryIsCreatedCorrectly = qTable.addEntry(node, actions);

        // check if no further state was created + no more actions were created
        assertFalse(secondEntryIsCreatedCorrectly, "entry was not added because entry already exists");
        assertEquals(1, qTable.qTable.size(), "only one state exists in q-table");
        assertEquals(4, qTable.getActions(node).size(), "no further actions were added");
    }

    // tests adding of entry (as ArrayList) to QTable with new state (no entry exists)
    @Test
    void testAddEntryWithArrayListIfNoEntryExists() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

        // init actions
        ArrayList<Action> actions = new ArrayList<>();
        Double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.add(Action.UP);
        actions.add(Action.RIGHT);
        actions.add(Action.DOWN);
        actions.add(Action.LEFT);

        // add entry to empty Q-Table
        boolean isCreatedCorrectly = qTable.addEntry(node, actions);

        // check if entry was created correctly
        assertTrue(qTable.stateExists(node), "state was created correctly");
        assertTrue(isCreatedCorrectly, "entry was added");
        assertEquals(4, qTable.getActions(node).size(), "4 actions were stored in q-table");

        assertTrue(qTable.actionExists(node, Action.UP), "action up was added successfully to state");
        assertEquals(qTableInitValue, qTable.getActions(node).get(Action.UP), "action up was initialised correctly");

        assertTrue(qTable.actionExists(node, Action.RIGHT), "action right was added successfully to state");
        assertEquals(qTableInitValue, qTable.getActions(node).get(Action.RIGHT), "action right was initialised correctly");

        assertTrue(qTable.actionExists(node, Action.DOWN), "action down was added successfully to state");
        assertEquals(qTableInitValue, qTable.getActions(node).get(Action.DOWN), "action down was initialised correctly");

        assertTrue(qTable.actionExists(node, Action.LEFT), "action left was added successfully to state");
        assertEquals(qTableInitValue, qTable.getActions(node).get(Action.LEFT), "action left was initialised correctly");
    }

    // tests adding of entry (as ArrayList) to QTable if entry for the passed state already exists
    @Test
    void testAddEntryWithArrayListIfEntryAlreadyExists() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

        // init actions
        ArrayList<Action> actions = new ArrayList<>();
        Double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.add(Action.UP);
        actions.add(Action.RIGHT);
        actions.add(Action.DOWN);
        actions.add(Action.LEFT);

        // add entry to empty Q-Table
        boolean isCreatedCorrectly = qTable.addEntry(node, actions);

        // check if entry was created correctly
        assertTrue(qTable.stateExists(node), "state was created correctly");
        assertTrue(isCreatedCorrectly, "entry was added");
        assertEquals(4, qTable.getActions(node).size(), "4 actions were stored in q-table");

        assertTrue(qTable.actionExists(node, Action.UP), "action up was added successfully to state");
        assertEquals(qTableInitValue, qTable.getActions(node).get(Action.UP), "action up was initialised correctly");

        assertTrue(qTable.actionExists(node, Action.RIGHT), "action right was added successfully to state");
        assertEquals(qTableInitValue, qTable.getActions(node).get(Action.RIGHT), "action right was initialised correctly");

        assertTrue(qTable.actionExists(node, Action.DOWN), "action down was added successfully to state");
        assertEquals(qTableInitValue, qTable.getActions(node).get(Action.DOWN), "action down was initialised correctly");

        assertTrue(qTable.actionExists(node, Action.LEFT), "action left was added successfully to state");
        assertEquals(qTableInitValue, qTable.getActions(node).get(Action.LEFT), "action left was initialised correctly");

        // try to add same entry again
        boolean secondEntryIsCreatedCorrectly = qTable.addEntry(node, actions);

        // check if no further state was created + no more actions were created
        assertFalse(secondEntryIsCreatedCorrectly, "entry was not added because entry already exists");
        assertEquals(1, qTable.qTable.size(), "only one state exists in q-table");
        assertEquals(4, qTable.getActions(node).size(), "no further actions were added");
    }

    // tests updating of Q-Value if entry exists
    @Test
    void testSetQValueActionExists() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        // add entry to empty Q-Table
        boolean isCreatedCorrectly = qTable.addEntry(node, actions);

        // check if first entry was created correctly
        assertTrue(isCreatedCorrectly, "entry was added");
        assertTrue(qTable.stateExists(node), "state was created correctly");
        assertEquals(1, qTable.qTable.size(), "only one state exists in q-table");
        assertEquals(4, qTable.getActions(node).size(), "4 actions were stored in q-table");
        assertTrue(qTable.actionExists(node, Action.UP), "action up was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.RIGHT), "action right was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.DOWN), "action down was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.LEFT), "action left was added successfully to state");

        // set Q-Value of existing state action pair
        Double newQValue = 1.0;
        qTable.setQValue(node, Action.UP, newQValue);

        // check if q-value was updated correctly
        assertEquals(newQValue, qTable.qTable.get(node.getState()).get(Action.UP), "q-value was correctly updated");
    }

    // tests updating of Q-Value if state does not exists
    @Test
    void testSetQValueStateDoesNotExist() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

        // init Q-Table
        QTable qTable = new QTable(0.0d);

        // set Q-Value of non existing state action pair (state and action don't exist)
        Double newQValue = 1.0;

        // check if q-value was not updated
        assertThrows(RuntimeException.class, () -> qTable.setQValue(node, Action.UP, newQValue));
    }

    // tests updating of Q-Value if state exists but action doesn't exist
    @Test
    void testSetQValueActionDoesNotExist() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.RIGHT, qTableInitValue);

        // add entry to empty Q-Table
        boolean isCreatedCorrectly = qTable.addEntry(node, actions);

        // check if first entry was created correctly
        assertTrue(isCreatedCorrectly, "entry was added");
        assertTrue(qTable.stateExists(node), "state was created correctly");
        assertEquals(1, qTable.getActions(node).size(), "1 action was stored in q-table");
        assertTrue(qTable.actionExists(node, Action.RIGHT), "action right was added successfully to state");

        // set Q-Value of non existing state action pair (state exists but action doesn't exist)
        Double newQValue = 1.0;

        // check if q-value was not updated
        assertThrows(RuntimeException.class, () -> qTable.setQValue(node, Action.UP, newQValue));
    }

    // tests requesting of Q-Value if entry exists
    @Test
    void testGetQValueActionExists() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();


        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;
        actions.put(Action.UP, qTableInitValue);

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);

        // add entry to empty Q-Table
        boolean firstEntryIsCreatedCorrectly = qTable.addEntry(node, actions);

        // check if entry was created correctly
        assertTrue(firstEntryIsCreatedCorrectly, "entry was added");
        assertTrue(qTable.stateExists(node), "state was created correctly");
        assertEquals(1, qTable.qTable.size(), "only one state exists in q-table");
        assertEquals(1, qTable.getActions(node).size(), "1 actions was stored in q-table");
        assertTrue(qTable.actionExists(node, Action.UP), "action up was added successfully to state");

        // try to get Q-Value of existing state action pair
        Double valueOfStateActionPair = qTable.getQValue(node, Action.UP);

        // check if getQValue works correctly
        assertEquals(qTableInitValue, valueOfStateActionPair, "Q-Value was requested correctly");
    }

    // tests requesting of Q-Value if entry doesn't exist (state and action don't exist in Q-Table)
    @Test
    void testGetQValueStateDoesNotExist() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

        // init Q-Table
        QTable qTable = new QTable(0.0d);

        // try to get Q-Value of non existing state action pair
        assertThrows(RuntimeException.class, () -> qTable.getQValue(node, Action.UP));
    }

    // tests requesting of Q-Value if entry doesn't exist (state exists but action doesn't exist)
    @Test
    void testGetQValueActionDoesNotExist() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);

        // add entry to empty Q-Table
        boolean firstEntryIsCreatedCorrectly = qTable.addEntry(node, actions);

        // check if entry was created correctly
        assertTrue(firstEntryIsCreatedCorrectly, "entry was added");
        assertTrue(qTable.stateExists(node), "state was created correctly");
        assertEquals(1, qTable.qTable.size(), "only one state exists in q-table");
        assertEquals(1, qTable.getActions(node).size(), "1 actions was stored in q-table");
        assertTrue(qTable.actionExists(node, Action.UP), "action up was added successfully to state");

        // try to get Q-Value of non existing state action pair
        assertThrows(RuntimeException.class, () -> qTable.getQValue(node, Action.RIGHT));
    }

    // tests requesting of all actions if state exists
    @Test
    void testGetActionsStateExists() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();


        // init actions
        HashMap<Action, Double> actions = new HashMap<>();
        Double qTableInitValue = 0.5;

        // init Q-Table
        QTable qTable = new QTable(qTableInitValue);
        actions.put(Action.UP, qTableInitValue);
        actions.put(Action.RIGHT, qTableInitValue);
        actions.put(Action.DOWN, qTableInitValue);
        actions.put(Action.LEFT, qTableInitValue);

        // add entry to empty Q-Table
        boolean isCreatedCorrectly = qTable.addEntry(node, actions);

        // check if first entry was created correctly
        assertTrue(isCreatedCorrectly, "entry was added");
        assertTrue(qTable.stateExists(node), "state was created correctly");
        assertEquals(1, qTable.qTable.size(), "only one state exists in q-table");
        assertEquals(4, qTable.getActions(node).size(), "4 actions were stored in q-table");
        assertTrue(qTable.actionExists(node, Action.UP), "action up was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.RIGHT), "action right was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.DOWN), "action down was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.LEFT), "action left was added successfully to state");

        // get actions of node
        HashMap<Action, Double> savedActions = qTable.getActions(node);

        // check if getActions returned all actions + check if Q-Values are correct
        assertEquals(4, savedActions.size(), "4 actions were stored in q-table");
        assertTrue(savedActions.containsKey(Action.UP), "saved actions contain action up");
        assertEquals(qTableInitValue, savedActions.get(Action.UP), "action up was returned with correct Q-Value");

        assertTrue(savedActions.containsKey(Action.RIGHT), "saved actions contain action right");
        assertEquals(qTableInitValue, savedActions.get(Action.RIGHT), "action right was returned with correct Q-Value");

        assertTrue(savedActions.containsKey(Action.DOWN), "saved actions contain action down");
        assertEquals(qTableInitValue, savedActions.get(Action.DOWN), "action down was returned with correct Q-Value");

        assertTrue(savedActions.containsKey(Action.LEFT), "saved actions contain action left");
        assertEquals(qTableInitValue, savedActions.get(Action.LEFT), "action left was returned with correct Q-Value");
    }

    // tests requesting of all actions if state does not exist
    @Test
    void testGetActionsStateDoesNotExist() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

        // init Q-Table
        QTable qTable = new QTable(0.0d);

        // get actions of node
        HashMap<Action, Double> savedActions = qTable.getActions(node);

        assertTrue(savedActions.isEmpty(), "state not found. Therefore empty hashMap was returned");

    }

    // tests determination of the highest Q-Value if state exists and multiple actions are saved
    @Test
    void testGetHighestQValueStateExistsMultipleActionsExist() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

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

        // add entry to empty Q-Table
        boolean isCreatedCorrectly = qTable.addEntry(node, actions);

        // check if first entry was created correctly
        assertTrue(isCreatedCorrectly, "entry was added");
        assertTrue(qTable.stateExists(node), "state was created correctly");
        assertEquals(1, qTable.qTable.size(), "only one state exists in q-table");
        assertEquals(4, qTable.getActions(node).size(), "4 actions were stored in q-table");
        assertTrue(qTable.actionExists(node, Action.UP), "action up was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.RIGHT), "action right was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.DOWN), "action down was added successfully to state");
        assertTrue(qTable.actionExists(node, Action.LEFT), "action left was added successfully to state");

        // get highestQValue
        Double highestCalculatedQValue = qTable.getHighestQValueOfState(node);

        // check if getActions returned all actions + check if Q-Values are correct
        assertEquals(highestQValue, highestCalculatedQValue, "highest q value was correctly determined");

    }

    // tests determination of the highest Q-Value if state exists but no actions are saved
    @Test
    void testGetHighestQValueStateExistsNoActionsExist() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

        // init Q-Table
        QTable qTable = new QTable(0.0d);

        // init empty actions
        HashMap<Action, Double> actions = new HashMap<>();

        // add entry to empty Q-Table
        boolean isCreatedCorrectly = qTable.addEntry(node, actions);

        // check if first entry was created correctly
        assertTrue(isCreatedCorrectly, "entry was added");
        assertTrue(qTable.stateExists(node), "state was created correctly");

        // check if getHighestQValueOfState throws exception
        assertThrows(RuntimeException.class, () -> qTable.getHighestQValueOfState(node));

    }

    // tests determination of the highest Q-Value if state doesn't exist
    @Test
    void testGetHighestQValueStateDoesNotExists() {

        Maze myMaze = TestUtils.getDefaultMaze();
        Node node = myMaze.getStartNode();

        // init Q-Table
        QTable qTable = new QTable(0.0d);

        // check if getHighestQValueOfState throws exception
        assertThrows(RuntimeException.class, () -> qTable.getHighestQValueOfState(node));

    }
}
