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
package de.uni.ks.maze.utils;

import de.uni.ks.logging.Logger;
import de.uni.ks.logging.messages.GuiMessageType;
import de.uni.ks.maze.Maze;
import de.uni.ks.maze.NodeFactory;
import de.uni.ks.maze.NodeFactory.Node;
import de.uni.ks.maze.complexityFunction.ComplexityFunction;
import de.uni.ks.maze.utils.mazeOperators.MazeOperator;
import de.uni.ks.maze.utils.mazeOperators.ResizeOperator;

import java.util.*;

import static de.uni.ks.maze.NodeType.IMPASSABLE;

/**
 * This class provides methods that can be used on or with a {@link Maze}. E.g. finding the shortest path between two
 * {@link Node} or printing the maze to the terminal.
 */
public class MazeUtils {

    // These can be used to color terminal outputs on Linux.
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    /**
     * Randomly generates a simple linear maze. The maze will only have one single path with an initialPathLength of
     * {@code initialPathLength}.
     * The initialPathLength must therefore be greater than or equal to two, since the start node and end node count
     * towards the path.
     * The generated path will be surrounded by wall nodes. Therefore the outer fields of the maze will never be
     * accessible to the agent.
     * Depending on the users choice {@code horizontal} the initial maze will either have a horizontal or a vertical path.
     * Necessary Information about the available colors and the rewards are taken from the {@code nodeFactory} object.
     *
     * @param initialPathLength The length of the generated linear path.
     * @param horizontal        Decides whether a horizontal or vertical path is generated.
     * @param nodeFactory       NodeFactory that is responsible for the Reward and the colors used.
     * @return The generated linear maze.
     */
    public static Maze buildMaze(int initialPathLength, boolean horizontal, NodeFactory nodeFactory) {

        validateMazeBuildParameters(initialPathLength);

        // build maze with horizontal path
        if (horizontal) {
            Logger.addTextToMiscLogOfCurrentLevel("Create initial maze (horizontal)");
            Logger.addTextToGuiLog("Create initial maze (horizontal)", GuiMessageType.Maze);
            Node[][] nodes = new Node[3][initialPathLength + 2];
            Node startNode = nodeFactory.buildStartNode();
            Node endNode = nodeFactory.buildEndNode();

            for (int y = 0; y < nodes.length; y++) {
                for (int x = 0; x < nodes[0].length; x++) {
                    // build start node on the left side of the maze
                    if (y == 1 && x == 1) {
                        nodes[y][x] = startNode;
                    }
                    // build end node on the right side of the maze
                    else if (y == 1 && x == nodes[0].length - 2) {
                        nodes[y][x] = endNode;
                    }
                    // build straight way in the middle of the maze
                    else if (y == 1 && x > 1 && x < nodes[0].length - 2) {
                        nodes[y][x] = nodeFactory.buildWayNode();
                    }
                    // use remaining nodes as walls
                    else {
                        nodes[y][x] = nodeFactory.buildWallNode();
                    }
                }
            }
            return new Maze(nodeFactory, nodes, startNode, endNode);
        }
        // build maze with vertical path
        else {
            Logger.addTextToMiscLogOfCurrentLevel("Create initial maze (vertical)");
            Logger.addTextToGuiLog("Create initial maze (vertical)", GuiMessageType.Maze);
            Node[][] nodes = new Node[initialPathLength + 2][3];
            Node startNode = nodeFactory.buildStartNode();
            Node endNode = nodeFactory.buildEndNode();

            for (int y = 0; y < nodes.length; y++) {
                for (int x = 0; x < nodes[0].length; x++) {
                    // build start node on the top of the maze
                    if (y == 1 && x == 1) {
                        nodes[y][x] = startNode;
                    }
                    // build end node on the bottom of the maze
                    else if (y == nodes.length - 2 && x == 1) {
                        nodes[y][x] = endNode;
                    }
                    // build straight way in the middle of the maze
                    else if (x == 1 && y > 1 && y < nodes.length - 2) {
                        nodes[y][x] = nodeFactory.buildWayNode();
                    }
                    // use remaining nodes as walls
                    else {
                        nodes[y][x] = nodeFactory.buildWallNode();
                    }
                }
            }
            return new Maze(nodeFactory, nodes, startNode, endNode);
        }
    }

    public static void validateMazeBuildParameters(int initialPathLength) {
        if (initialPathLength < 2) {
            throw new IllegalArgumentException("Parameter [initialPathLength] must be at least 2.");
        }
    }

    /**
     * Wraps {@link #changeMaze(Maze, ArrayList, List, double, Random)} with default values for {@code currentDiff}.
     *
     * @param maze      The instance of {@link de.uni.ks.maze.Maze} that should be changed.
     * @param operators The list of {@link de.uni.ks.maze.utils.mazeOperators.MazeOperator} that can be
     *                  used to change the maze.
     * @param delta     Quantifies how much the maze can be changed.
     * @param random    Random source to select operators.
     * @return A value quantifying the actual change the operations did,
     * this will always be smaller or equal to @code{delta}.
     */
    public static double changeMaze(Maze maze, List<MazeOperator> operators, double delta, Random random) {
        return changeMaze(maze, new ArrayList<>(operators), operators, delta, random);
    }

    /**
     * Changed the given maze with the available operators. While the current change is smaller than delta, a random
     * operator will be selected. If that operator is to expensive or cannot change the maze, it will be removed from
     * the list of available operators. If the maze is resized by a {@link ResizeOperator} all operators are available
     * again for changing the maze.
     *
     * @param maze         The instance of {@link de.uni.ks.maze.Maze} that should be changed.
     * @param operators    The list of {@link de.uni.ks.maze.utils.mazeOperators.MazeOperator} that can be
     *                     used to change the maze in the current recursive call of this method.
     * @param allOperators The list of all operators that this method was called with in the first place.
     * @param delta        Quantifies how much the maze can be changed.
     * @param random       Random source to select operators.
     * @return A value that quantifies the change that was made to the maze.
     */
    private static double changeMaze(Maze maze, ArrayList<MazeOperator> operators, List<MazeOperator> allOperators,
                                     double delta, Random random) {

        double currentDiff = 0.0;

        while (currentDiff < delta && !operators.isEmpty()) {
            int r = random.nextInt(operators.size());
            MazeOperator operator = operators.get(r);
            double cost = operator.estimateCost(maze, delta - currentDiff);

            if (currentDiff + cost <= delta
                    && operator.changeMaze(maze)) {
                currentDiff += cost;

                if (operator instanceof ResizeOperator) {
                    // Resizing the maze could allow other operators that could not be used before to be used again.
                    operators = new ArrayList<>(allOperators);
                }
            } else {
                operators.remove(operator); // This operator cannot be used on this maze.
            }
        }

        return currentDiff;
    }

    /**
     * Calculates and returns the complexityFunction of the given maze based on the passed complexity function.
     *
     * @param maze               The {@link de.uni.ks.maze.Maze} whose complexity will be calculated and returned.
     * @param complexityFunction The complexity function that will be used.
     * @return The calculated complexity of the maze.
     */
    public static double calculateComplexity(Maze maze, ComplexityFunction complexityFunction) {
        return complexityFunction.calculateComplexity(maze);
    }

    public static Maze getPlaceholderMaze(NodeFactory nodeFactory) {
        Node[][] mazeData = new Node[7][5];

        Logger.addTextToMiscLogOfCurrentLevel("Create placeholder maze");

        Node startNode = nodeFactory.buildStartNode();
        Node endNode = nodeFactory.buildEndNode();

        // 1. Row
        mazeData[0][0] = nodeFactory.buildWallNode();
        mazeData[0][1] = nodeFactory.buildWallNode();
        mazeData[0][2] = nodeFactory.buildWallNode();
        mazeData[0][3] = nodeFactory.buildWallNode();
        mazeData[0][4] = nodeFactory.buildWallNode();
        // 2. Row
        mazeData[1][0] = nodeFactory.buildWallNode();
        mazeData[1][1] = nodeFactory.buildWayNode();
        mazeData[1][2] = startNode; // START
        mazeData[1][3] = nodeFactory.buildWayNode();
        mazeData[1][4] = nodeFactory.buildWallNode();

        // 3. Row
        mazeData[2][0] = nodeFactory.buildWallNode();
        mazeData[2][1] = nodeFactory.buildWallNode();
        mazeData[2][2] = nodeFactory.buildWallNode();
        mazeData[2][3] = nodeFactory.buildWayNode();
        mazeData[2][4] = nodeFactory.buildWallNode();

        // 4. Row
        mazeData[3][0] = nodeFactory.buildWallNode();
        mazeData[3][1] = nodeFactory.buildWayNode();
        mazeData[3][2] = nodeFactory.buildWayNode();
        mazeData[3][3] = nodeFactory.buildWayNode();
        mazeData[3][4] = nodeFactory.buildWallNode();

        // 5. Row
        mazeData[4][0] = nodeFactory.buildWallNode();
        mazeData[4][1] = nodeFactory.buildWayNode();
        mazeData[4][2] = nodeFactory.buildWallNode();
        mazeData[4][3] = nodeFactory.buildWayNode();
        mazeData[4][4] = nodeFactory.buildWallNode();

        // 6. Row
        mazeData[5][0] = nodeFactory.buildWallNode();
        mazeData[5][1] = endNode; // END
        mazeData[5][2] = nodeFactory.buildWallNode();
        mazeData[5][3] = nodeFactory.buildWallNode();
        mazeData[5][4] = nodeFactory.buildWallNode();

        // 7. Row
        mazeData[6][0] = nodeFactory.buildWallNode();
        mazeData[6][1] = nodeFactory.buildWallNode();
        mazeData[6][2] = nodeFactory.buildWallNode();
        mazeData[6][3] = nodeFactory.buildWallNode();
        mazeData[6][4] = nodeFactory.buildWallNode();

        return new Maze(nodeFactory, mazeData, startNode, endNode);
    }

    public static void printMazeToTerminal(Maze maze) {
        printMazeToTerminal("", maze, new ArrayList<>());
    }

    public static void printMazeToTerminal(String headline, Maze maze, List<Node> highlights) {

        Node[][] nodeGrid = maze.getMaze();

//        String line = new String(new char[nodeGrid[0].length * 4]).replace("\0", "-") + "-";

        System.out.println("\n" + headline + ":");
//        System.out.println(line);
        for (int x = 0; x < nodeGrid.length; x++) {
            System.out.print("| ");
            for (int y = 0; y < nodeGrid[0].length; y++) {
                Node node = nodeGrid[x][y];
                if (highlights != null && highlights.contains(node)) {
                    if (node.getNodeType() == IMPASSABLE) {
                        System.out.print(ANSI_RED + "#" + ANSI_RESET + " | ");
                    } else {
                        System.out.print(ANSI_RED + node.getNodeType().name().substring(0, 1) + ANSI_RESET + " | ");
                    }
                } else {
                    if (node == null) {
                        System.out.print("/" + " | ");
                    } else if (node.getNodeType() == IMPASSABLE) {
                        System.out.print("#" + " | ");
                    } else if (node.equals(maze.getStartNode())) {
                        System.out.print("S | ");
                    } else if (node.equals(maze.getEndNode())) {
                        System.out.print("E | ");
                    } else {
                        System.out.print(node.getNodeType().name().substring(0, 1) + " | ");
                    }
                }
            }
            System.out.println();
//            System.out.println(line);
        }
    }

    // Dijkstra-Algorithm for finding the shortest path.

    /**
     * Returns the number of actions that are needed to traverse the shortest path from [startNode]
     * to one of the [endNodes] in the given [maze]. I.e. the return value is the length of the
     * path to the nearest endNode.
     *
     * @param maze      The maze in which the path is searched.
     * @param startNode The start node of the path.
     * @param endNode   The node where the path could end.
     * @return The number of actions that are minimally needed to get from the [startNode] to one of the [endNodes].
     * @throws IllegalArgumentException If no path from the start node to any of the end nodes exists.
     */
    public static int getOptimalNumberOfActions(Maze maze, Node startNode, Node endNode) throws IllegalArgumentException {
        Map<Node, Node> predecessors = dijkstra(maze, startNode, endNode);
        Stack<Node> path = getShortestPath(endNode, predecessors);
        return path.size() - 1; // -1 Because the start node is part of the path, but does not count as a action.
    }

    /**
     * Returns the maximal reward the agent can achieve when traveling from the start node to the end node
     *
     * @param maze      The maze in which the path is searched.
     * @param startNode The start node of the path.
     * @param endNode   The node where the path could end.
     * @return The highest reward the Agent can achieve when traveling from the [startNode] to the [endNodes].
     * @throws IllegalArgumentException If no path from the start node to any of the end nodes exists.
     */
    public static double getOptimalReward(Maze maze, Node startNode, Node endNode) throws IllegalArgumentException {

        Map<Node, Node> predecessors = dijkstra(maze, startNode, endNode);
        Stack<Node> path = getShortestPath(endNode, predecessors);

        // calculate optimum reward
        double reward = 0.0d;
        for (Node node : path) {
            if (node != maze.getStartNode()) {
                reward += node.getReward();
            }
        }

        return reward;
    }

    /**
     * Finds the shortest path in a maze from one node to another.
     *
     * @param maze      The maze in which the search is performed.
     * @param startNode The search starts here.
     * @param endNode   The search will end here.
     * @return Stack of all nodes that are part of the path, including {@code startNode} and {@code endNode}.
     */
    public static Stack<Node> getShortestPath(Maze maze, Node startNode, Node endNode) {
        Map<Node, Node> predecessors = dijkstra(maze, startNode, endNode);

        return getShortestPath(endNode, predecessors);
    }

    /**
     * Performs the dijkstra algorithm on the given [maze], i.e. the algorithm finds the best predecessor for every
     * node in the maze, that is the node that yields to the shortest path from [startNode] to that respective node.
     * To speed up the search the algorithm terminates if the shortest path to one of the end nodes is found.
     *
     * @param maze      The maze in which the algorithm is performed.
     * @param startNode The start node of the search.
     * @param endNode   The end node in the maze, the algorithm terminates if it is reached.
     * @return A map that maps all nodes in the maze to their best predecessor, the predecessor will be null
     * for all nodes that were not visited by the search.
     */
    private static Map<Node, Node> dijkstra(Maze maze, Node startNode, Node endNode) {

        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> predecessors = new HashMap<>();
        initDijkstra(maze, startNode, distances, predecessors);

        List<Node> allNodes = maze.getAllPassableNodes();
        while (allNodes.size() > 0) {
            Node u = null;
            double minDist = Double.POSITIVE_INFINITY;

            for (Node node : allNodes) {
                double dist = distances.get(node);
                if (dist < minDist) {
                    minDist = dist;
                    u = node;
                }
            }

            if (u == null) {
                // Apparently all nodes that were reachable from the start node were visited, and non of the end nodes
                // was visited.
                throw new IllegalArgumentException("There exists no path from the start node to any of the end nodes.");
            } else {

                allNodes.remove(u);

                // Iterate only over those nodes the agent can go to too.
                for (Node v : u.getPassableNeighbors()) {
                    if (allNodes.contains(v)) {
                        updateDistance(u, v, distances, predecessors);

                        if (v.equals(endNode)) { // Stop search if we reach an end node, this is automatically the
                            // shortest path.
                            return predecessors;
                        }
                    }
                }
            }
        }

        return predecessors;
    }

    /**
     * Initializes the distance and predecessor map for use in the dijkstra algorithm. Distances are initially
     * infinite for all nodes but the start node. Predecessors are initially null (i.e. there are no predecessors for
     * any node).
     *
     * @param maze         The maze in which the search is used.
     * @param startNode    The node where the search starts.
     * @param distances    Maps nodes to their respective distance [startNode].
     * @param predecessors Maps nodes to their respective predecessors.
     */
    private static void initDijkstra(Maze maze, Node startNode, Map<Node, Double> distances, Map<Node, Node> predecessors) {
        for (Node node : maze.getAllPassableNodes()) {
            distances.put(node, Double.POSITIVE_INFINITY); // Initial distance is infinite,
            // this represents that we have not found a way to that node yet.
            predecessors.put(node, null); // null means no predecessor.
        }

        distances.put(startNode, 0D); // Distance from start to start is 0.
    }

    /**
     * Updates the predecessor of [v] and the distance from the start node to [v], if [u] is a better
     * predecessor (the path to [v] gets shorter) for [v] than the current predecessor.
     *
     * @param u            The alternate predecessor.
     * @param v            The node whose predecessor should be updated.
     * @param distances    A map containing all current distances from the start node to all other nodes. This map
     *                     would get updated if [u] is a better predecessor.
     * @param predecessors A map containing all current predecessors of all nodes. This map would get updated if [u]
     *                     is a better predecessor.
     */
    private static void updateDistance(Node u, Node v, Map<Node, Double> distances, Map<Node, Node> predecessors) {
        double alternateDistance = distances.get(u) + -1 * v.getReward();
        if (alternateDistance < distances.get(v)) {
            distances.put(v, alternateDistance);
            predecessors.put(v, u);
        }
    }

    /**
     * The method finds the shortest path from the start node to [endNode], this is done by starting at the
     * [endNode] and adding predecessors to the path until the start node (that has no predecessor).
     *
     * @param endNode      The node to which the shortest path is requested.
     * @param predecessors Map containing the predecessor for every node in the maze.
     *                     The predecessor is null if non is known.
     * @return A Stack containing all nodes in the path in order.
     */
    private static Stack<Node> getShortestPath(Node endNode, Map<Node, Node> predecessors) {
        Stack<Node> path = new Stack<>();
        path.push(endNode);
        Node u = endNode;
        while (predecessors.getOrDefault(u, null) != null) {
            u = predecessors.get(u);
            path.push(u);
        }

        return path;
    }

    /**
     * The method will return all simple cycle ('loops') that can be found in the maze.
     * A circle is a path in which all nodes apart from the start and the end node are different.
     *
     * @param maze The maze in which the loops will be searched.
     * @return A List that contains all loops.
     */
    public static List<Stack<Node>> getAllLoops(Maze maze) {
        // get all nodes that are part of the loops
        Stack<Node> loopNodes = getAllLoopNodes(maze);

        // get all loops
        List<Stack<Node>> allLoops = new ArrayList<>();
        for (Node loopNode : loopNodes) {
            allLoops.addAll(depthFirstSearchLoops(loopNode, loopNodes, new Stack<>()));
        }

        // removed redundant loops
        List<Stack<Node>> cleanedLoopList = removedDuplicatedLoops(allLoops);
        return cleanedLoopList;
    }

    /**
     * The method removes redundant loops and returns a new list in which all loops are unique.
     *
     * @param loops The list of loops that will be filtered.
     * @return A filtered List where all loops are unique.
     */
    private static List<Stack<Node>> removedDuplicatedLoops(List<Stack<Node>> loops) {
        List<Stack<Node>> newLoops = new ArrayList<>();
        for (Stack<Node> loop1 : loops) {
            boolean loopIsNew = true;
            for (Stack<Node> savedLoop : newLoops) {
                if (loopsAreEqual(savedLoop, loop1)) {
                    loopIsNew = false;
                    break;
                }
            }
            if (loopIsNew) {
                newLoops.add(loop1);
            }
        }
        return newLoops;
    }

    /**
     * The method checks if two loops are equal by comparing their nodes.
     *
     * @param loop1 The first loop that will be compared.
     * @param loop2 The second loop that will be compared.
     * @return True if loops are equal. False otherwise.
     */
    private static boolean loopsAreEqual(Stack<Node> loop1, Stack<Node> loop2) {
        if (loop1.size() != loop2.size()) return false;
        int equalNodes = 0;
        for (Node node1 : loop1) {
            for (Node node2 : loop2) {
                if (node1.equals(node2)) {
                    equalNodes++;
                }
            }
        }
        return equalNodes == loop1.size();
    }

    /**
     * This recursive method is used to determine cycles and its nodes.
     * The Method uses depth first search to walk through every valid path.
     * A valid path only consists of nodes that are part of any existing cycle.
     * If a node is going to appear twice in path a cycle is detected.
     * The nodes of the cycle will get reconstructed and the cycle itself will be added to @Code{loops}
     *
     * @param currentNode The node that is currently examined.
     * @param loopNodes,  A Stack that contains all nodes that are part of any loop.
     * @param currentPath A Stack that contains all nodes of the current path.
     * @return A List of Stacks that contains all loops and its nodes.
     */
    private static List<Stack<Node>> depthFirstSearchLoops(Node currentNode, Stack<Node> loopNodes, Stack<Node> currentPath) {
        ArrayList<Stack<Node>> loops = new ArrayList<>();

        // if loop has been detected -> trace nodes back and add loop to loops
        if (currentPath.contains(currentNode)) {
            loops.add(reconstructCycle(currentNode, currentPath));
            return loops;
        }

        // add current node to current path
        Node lastNode = currentPath.size() > 0 ? currentPath.lastElement() : null;
        currentPath.add(currentNode);

        // recursively call depthFirstSearchLoops on all neighbors except the last node
        List<NodeFactory.Node> directNeighbors = currentNode.getPassableNeighbors();
        for (NodeFactory.Node neighborNode : directNeighbors) {
            if (neighborNode != lastNode && loopNodes.contains(neighborNode)) {
                List<Stack<Node>> newLoops = depthFirstSearchLoops(neighborNode, loopNodes, (Stack<Node>) currentPath.clone());
                loops.addAll(newLoops);
            }
        }
        return loops;
    }

    /**
     * The method reconstructs a detected loop.
     * This is achieved by traversing the current path backwards until the redundant node is reached again.
     *
     * @param currentNode The Node where the cycle was detected.
     * @param currentPath A Stack that contains all nodes of the current path.
     * @return A Stack that contains all nodes of the detected cycle.
     */
    private static Stack<Node> reconstructCycle(Node currentNode, Stack<Node> currentPath) {
        Stack<Node> loop = new Stack<>();
        loop.add(currentNode);
        Node tmp = currentPath.lastElement();
        while (tmp != currentNode) {
            loop.add(currentPath.pop());
            tmp = currentPath.lastElement();
        }
        return loop;
    }

    /**
     * The method will return all nodes that are part of any existing cycle.
     *
     * @param maze The maze in which the loop nodes will be searched.
     * @return A Stack that contains all nodes that are part on any existing cycles
     */
    public static Stack<Node> getAllLoopNodes(Maze maze) {
        Stack<Node> loopNodes = depthFirstSearchLoopNodes(maze.getStartNode(), new Stack<>(), new Stack<>());
        Stack<Node> cleanedLoopNodes = removeDuplicatedNodes(loopNodes);
        return cleanedLoopNodes;
    }

    /**
     * This recursive method is used to determine cycles and its nodes.
     * The Method uses depth first search to walk through every valid path.
     * A valid path only consists of nodes that are part of any existing cycle.
     * If a node is going to appear twice in path a cycle is detected.
     * The nodes of the cycle will get reconstructed and the cycle itself will be added to @Code{loops}
     *
     * @param currentNode The node that is currently examined.
     * @param finished    A Stack that contains all nodes that are already fully discovered
     * @param currentPath A Stack that contains all nodes of the current path.
     * @return A Stack that contains all nodes that are part of any loop.
     */
    private static Stack<Node> depthFirstSearchLoopNodes(Node currentNode, Stack<Node> finished, Stack<Node> currentPath) {
        Stack<Node> loopNodes = new Stack<>();

        // abort search if all neighbors of current node have already been searched
        if (finished.contains(currentNode)) {
            return loopNodes;
        }

        // if loop has been detected -> trace nodes back and add new loop to loops
        if (currentPath.contains(currentNode)) {
            loopNodes.addAll(reconstructCycle(currentNode, currentPath));
            return loopNodes;
        }

        // add current node to current path
        Node lastNode = currentPath.size() > 0 ? currentPath.lastElement() : null;
        currentPath.add(currentNode);

        // recursively call depthFirstSearchLoops on all neighbors except the last node
        List<NodeFactory.Node> directNeighbors = currentNode.getPassableNeighbors();
        for (NodeFactory.Node neighborNode : directNeighbors) {
            if (neighborNode != lastNode) {
                loopNodes.addAll(depthFirstSearchLoopNodes(neighborNode, finished, (Stack<Node>) currentPath.clone()));
            }
        }
        finished.add(currentNode);
        return loopNodes;
    }

    /**
     * The method removes redundant nodes and returns a new stack in which all nodes are unique.
     *
     * @param nodes The stack of nodes that will be filtered.
     * @return A filtered stack where all nodes are unique.
     */
    private static Stack<Node> removeDuplicatedNodes(Stack<Node> nodes) {
        Stack<Node> newNodes = new Stack<>();
        for (Node node : nodes) {
            boolean NodeIsNew = true;
            for (Node savedNode : newNodes) {
                if (node.equals(savedNode)) {
                    NodeIsNew = false;
                    break;
                }
            }
            if (NodeIsNew) {
                newNodes.add(node);
            }
        }
        return newNodes;
    }

    /**
     * The method will return all parallel routes that can be found in the maze.
     * A parallel route is a loop that was found with {@getAllLoops} and
     * has at least two different nodes connected to the optimal path.
     *
     * @param maze The maze in which the parallel routes will be searched.
     * @return A List that contains all parallel routes.
     */
    public static List<Stack<Node>> getAllParallelRoutes(Maze maze) {
        // get all loops of the passed maze
        List<Stack<Node>> allLoops = getAllLoops(maze);

        // get optimal path
        Stack<Node> optimalPath = MazeUtils.getShortestPath(maze, maze.getStartNode(), maze.getEndNode());

        // filter loops -> parallel route has to have two nodes that are connected to the optimal path
        List<Stack<Node>> allParallelRoutes = new ArrayList<>();
        for (Stack<Node> loop : allLoops) {
            int numberOfNodesConnectedToOptimalPath = 0;
            for (Node node : loop) {
                for (Node neighbor : node.getPassableNeighbors()) {
                    if (optimalPath.contains(neighbor)) {
                        if (++numberOfNodesConnectedToOptimalPath == 2) {
                            allParallelRoutes.add(loop);
                            break;
                        }
                    }
                }
            }
        }

        return allParallelRoutes;
    }

    /**
     * The method will return all nodes that are part of any existing parallel route.
     * A parallel route is a loop that was found with {@getAllLoops} and
     * has at least two different nodes connected to the optimal path.
     *
     * @param maze The maze in which the parallel route nodes will be searched.
     * @return A Stack that contains all nodes that are part on any existing parallel route
     */
    public static Stack<Node> getAllParallelRouteNodes(Maze maze) {
        // get all parallel routes of the passed maze
        List<Stack<Node>> allParallelRoutes = getAllParallelRoutes(maze);

        // add all parallel route nodes to a single stack
        Stack<Node> allParallelRouteNodes = new Stack<>();
        for (Stack<Node> parallelRoute : allParallelRoutes) {
            allParallelRouteNodes.addAll(parallelRoute);
        }
        // remove duplicates
        Stack<Node> cleanedParallelRouteNodes = removeDuplicatedNodes(allParallelRouteNodes);
        return cleanedParallelRouteNodes;
    }

    /*
    // TODO: method can be used to transform maze to an edge set. This representation of the maze can perhaps be
         used to use existing graph algorithms
    private static int[][] getEdgesOfMaze(Maze maze, HashMap<Node, Integer> numeratedNodes) {
        // give every passable node a unique number
        List<Node> allPassableNodes = maze.getAllPassableNodes();
        int number = 1;
        for (Node node : allPassableNodes) {
            numeratedNodes.put(node, number);
            number++;
        }

        // add all edges to int[][] array (symmetric edges included)
        // [maze.getAllPassableNodes().size() * 4] =  maximum number of edges possible
        // [2] = size of one edge for example (1,2)
        int[][] graph = new int[maze.getAllPassableNodes().size() * 4][2];
        int currentEdgeNumber = 0;
        for (Node node : allPassableNodes) {
            int currentNodeNumber = numeratedNodes.get(node);
            for (Node neighbor : node.getPassableNeighbors()) {
                int neighborNodeNumber = numeratedNodes.get(neighbor);
                graph[currentEdgeNumber][0] = currentNodeNumber;
                graph[currentEdgeNumber][1] = neighborNodeNumber;
                currentEdgeNumber++;
            }
        }

        return Arrays.copyOfRange(graph, 0, currentEdgeNumber);
    }

    // TODO: method can be used to transform a node number back to a node object
    private static Node getNodeFromNumber(Integer nodeNumberObject, HashMap<Node, Integer> numberedNodes) {
        return numberedNodes.entrySet()
                .stream()
                .filter(entry -> nodeNumberObject.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().get();
    }
    */

}
