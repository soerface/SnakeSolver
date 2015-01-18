package de.wegenerd;


import java.util.ArrayList;

import static java.lang.Thread.sleep;

public final class PathUtils {
    private PathUtils() {

    }

    public static ArrayList<Node> increasePathLength(ArrayList<Node> path, ArrayList<Node> nodeList) throws InterruptedException {
        sleep(AutoSolver.ANIMATION_DELAY);
        boolean pathChanged = false;
        for (Node node : path) {
            if (node.parent == null) {
                continue;
            }
            Node originalParent = node.parent;
            for (Node firstNeighbourNode : findNeighbourNodes(node, nodeList)) {
                // make sure we process a node which is not yet part of our path
                if (nodeInList(firstNeighbourNode, path)) {
                    continue;
                }
                for (Node secondNeighbourNode : findNeighbourNodes(firstNeighbourNode, nodeList)) {
                    if (nodeInList(secondNeighbourNode, path)) {
                        continue;
                    }
                    for (Node thirdNeighbourNode : findNeighbourNodes(secondNeighbourNode, nodeList)) {
                        if (thirdNeighbourNode == originalParent) {
                            node.parent = firstNeighbourNode;
                            firstNeighbourNode.parent = secondNeighbourNode;
                            secondNeighbourNode.parent = originalParent;
                            pathChanged = true;
                            break;
                        } else {
                            if (nodeInList(thirdNeighbourNode, path)) {
                                continue;
                            }
                            for (Node fourthNeighbourNode : findNeighbourNodes(thirdNeighbourNode, nodeList)) {
                                if (fourthNeighbourNode == originalParent) {
                                    node.parent = firstNeighbourNode;
                                    firstNeighbourNode.parent = secondNeighbourNode;
                                    secondNeighbourNode.parent = thirdNeighbourNode;
                                    thirdNeighbourNode.parent = originalParent;
                                    pathChanged = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (pathChanged) {
                        break;
                    }
                }
                if (pathChanged) {
                    break;
                }
            }
            if (pathChanged) {
                break;
            }
        }
        if (pathChanged) {
            return AStarPathFinder.generatePath(path.get(0));
        }
        return null;
    }

    public static ArrayList<Node> findNeighbourNodes(Node targetNode, ArrayList<Node> nodeList) {
        ArrayList<Node> neighbourNodes = new ArrayList<Node>();
        if (targetNode.parent == null) {
            // doesnt make sense to change the starting node (snake head)
            return neighbourNodes;
        }
        // add all nodes from the passed list to the result if it is a neighbour
        int[] neighbourTileIds = targetNode.tile.getNeighbourTileIds();
        for (Node node : nodeList) {
            for (int tileId : neighbourTileIds) {
                if (node.tile.tileId == tileId) {
                    neighbourNodes.add(node);
                }
            }
        }
        return neighbourNodes;
    }

    public static boolean nodeInList(Node node, ArrayList<Node> list) {
        for (Node listNode : list) {
            if (node == listNode) {
                return true;
            }
        }
        return false;
    }
}
