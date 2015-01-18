package de.wegenerd;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Thread.sleep;

public class TailPathFinder {

    private Processing processing;
    private GameTile[] gameTiles;
    private GameTile startTile;
    private ArrayList<GameTile> snakeTiles;
    private AStarPathFinder aStarPathFinder;
    public int padding;

    TailPathFinder(Processing processing, GameTile[] gameTiles, GameTile startTile) {
        this.processing = processing;
        this.gameTiles = gameTiles;
        this.startTile = startTile;
        this.snakeTiles = new ArrayList<GameTile>();
        this.padding = 0;

        // generate a list of all snake tiles
        for (GameTile gameTile : this.gameTiles) {
            if (gameTile.occupied && gameTile.occupiedCounter > 0) {
                this.snakeTiles.add(gameTile);
            }
        }
        Collections.sort(snakeTiles);
    }

    public ArrayList<Node> getPath() throws InterruptedException {
        ArrayList<Node> path = null;
        while (path == null) {
            if (snakeTiles.size() == 0) {
                return null;
            }
            this.aStarPathFinder = new AStarPathFinder(this.processing, this.gameTiles, this.startTile, snakeTiles.remove(0));
            this.aStarPathFinder.ignoreMoving = true;
            path = this.aStarPathFinder.getPath();
            while (path != null) {
                Node targetNode = path.get(0);
                if (targetNode.getNumberOfParents() < targetNode.minimumDistance + this.padding) {
                    this.aStarPathFinder.exploreAll();
                    path = this.increasePathLength(path, this.aStarPathFinder.closedList);
                    if (path != null) {
                        // our alternative path used all nodes in the closed list.
                        // since we iterate through our snake sorted (first nodes are those which will disappear first),
                        // it will not be possible to get a better path if path.size() == closedList.size().
                        // therefore, return null to indicate that it is not possible to escape from this situation
                        if (path.size() == this.aStarPathFinder.closedList.size() && targetNode.getNumberOfParents() < targetNode.minimumDistance + this.padding) {
                            return null;
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return path;
    }

    static private ArrayList<Node> increasePathLength(ArrayList<Node> path, ArrayList<Node> nodeList) throws InterruptedException {
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

    static private ArrayList<Node> findNeighbourNodes(Node targetNode, ArrayList<Node> nodeList) {
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

    static private boolean nodeInList(Node node, ArrayList<Node> list) {
        for (Node listNode : list) {
            if (node == listNode) {
                return true;
            }
        }
        return false;
    }

    public void draw() {
        this.draw(-1);
    }

    public void draw(int color) {
        if (this.aStarPathFinder != null) {
            this.aStarPathFinder.draw(color);
        }
    }
}