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
import de.uni.ks.configuration.WritableToConfig;
import de.uni.ks.maze.NodeFactory.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public interface ExplorationPolicy extends WritableToConfig {

    /**
     * This method chooses a action that is executable from the given node.
     * The Selection is commonly based on the knowledge that is stored in the given Q-Table.
     *
     * @param currentNode The node whose neighborhood encodes the state.
     * @param qTable      QTable that stores the knowledge of the agent.
     * @return Action that determines the next movement of the agent.
     */
    Action chooseAction(Node currentNode, QTable qTable);

    /**
     * Method is executed after the execution of the last interaction. Implementation is optional.
     *
     * @param oldNode      The node agent was placed on before the interaction
     * @param chosenAction The Action the agent chose in the interaction
     * @param oldQValue    The Q Value of the state-action pair (oldNode, chosenAction) before the last interaction
     * @param newNode      The node the agent moved to after the interaction
     * @param qTable       QTable that stores the knowledge of the agent.
     */
    default void postProcessing(Node oldNode, Action chosenAction, double oldQValue, @SuppressWarnings("unused") Node newNode, QTable qTable) {

    }

    /**
     * Creates a sorted [List] from the provided [Set] of type [Action].
     * I.e. the order of the elements will be the same, every time the method gets
     * called with the same set.
     *
     * @param set The set that is transformed to an sorted list.
     * @return The sorted list.
     */
    default List<Action> getSortedListFromActionSet(Set<Action> set) {
        ArrayList<Action> actions = new ArrayList<>(set);
        actions.sort(Enum::compareTo);
        return actions;
    }
}
