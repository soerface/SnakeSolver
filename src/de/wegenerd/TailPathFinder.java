package de.wegenerd;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Thread.sleep;

public class TailPathFinder {

    private Processing processing;
    private GameTile[] gameTiles;
    private GameTile startTile;
    private ArrayList<GameTile> snakeTiles;
    private AStarPathFinder aStarPathFinder;

    TailPathFinder(Processing processing, GameTile[] gameTiles, GameTile startTile) {
        this.processing = processing;
        this.gameTiles = gameTiles;
        this.startTile = startTile;
        this.snakeTiles = new ArrayList<GameTile>();

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
            // no direct path to food possible. Try to find a way to our tail
            if (snakeTiles.size() == 0) {
                PApplet.print("Couldn't find a way out\n");
                break;
            }
            this.aStarPathFinder = new AStarPathFinder(this.processing, this.gameTiles, this.startTile, snakeTiles.remove(0));
            path = this.aStarPathFinder.getPath();
            this.aStarPathFinder.exploreAll();
            while (path != null) {
                Node targetNode = path.get(0);
                if (path.size() <= targetNode.minimumDistance) {
                    path = this.increasePathLength(path, this.aStarPathFinder.closedList);
                    if (path != null) {
                        if (path.size() == this.aStarPathFinder.closedList.size()) {
                            PApplet.print("unescapable\n");
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return path;
    }

    private ArrayList<Node> increasePathLength(ArrayList<Node> path, ArrayList<Node> nodeList) throws InterruptedException {
        sleep(AutoSolver.ANIMATION_DELAY * 10);
        boolean pathChanged = false;
        for (Node node : path) {
            if (node.parent == null) {
                continue;
            }
            Node originalParent = node.parent;
            for (Node firstNeighbourNode : this.findNeighbourNodes(node, nodeList)) {
                // make sure we process a node which is not yet part of our path
                if (this.nodeInList(firstNeighbourNode, path)) {
                    continue;
                }
                for (Node secondNeighbourNode : this.findNeighbourNodes(firstNeighbourNode, nodeList)) {
                    if (this.nodeInList(secondNeighbourNode, path)) {
                        continue;
                    }
                    for (Node thirdNeighbourNode : this.findNeighbourNodes(secondNeighbourNode, nodeList)) {
                        if (thirdNeighbourNode == originalParent) {
                            node.parent = firstNeighbourNode;
                            firstNeighbourNode.parent = secondNeighbourNode;
                            secondNeighbourNode.parent = originalParent;
                            pathChanged = true;
                            break;
                        } else {
                            if (this.nodeInList(thirdNeighbourNode, path)) {
                                continue;
                            }
                            for (Node fourthNeighbourNode : this.findNeighbourNodes(thirdNeighbourNode, nodeList)) {
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

    private ArrayList<Node> findNeighbourNodes(Node targetNode, ArrayList<Node> nodeList) {
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

    private boolean nodeInList(Node node, ArrayList<Node> list) {
        for (Node listNode : list) {
            if (node == listNode) {
                return true;
            }
        }
        return false;
    }

    public void draw() {
        this.aStarPathFinder.draw();
    }
}