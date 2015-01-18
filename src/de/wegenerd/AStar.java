package de.wegenerd;

import processing.core.PApplet;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class AStar {

    private final GameTile[] gameTiles;
    private final Processing processing;
    private final GameTile startTile;
    private final GameTile endTile;
    private ArrayList<Node> openList;
    private ArrayList<Node> closedList;
    private final Object drawLock = new Object();
    private Node currentNode; // just a member for drawing purposes

    AStar(Processing processing, GameTile[] gameTiles, GameTile startTile, GameTile endTile) {
        this.processing = processing;
        this.gameTiles = gameTiles;
        this.startTile = startTile;
        this.endTile = endTile;
        this.openList = new ArrayList<Node>();
        this.closedList = new ArrayList<Node>();
    }

    ArrayList<Node> getPath() throws InterruptedException {
        if (this.startTile == this.endTile) {
            return null;
        }
        Node node = new Node(this.processing, this.startTile);
        //finalPath.add(node);
        Node finalNode = checkNode(node);
        if (finalNode != null) {
            return generatePath(finalNode);
        }
        return null;
    }

    static ArrayList<Node> generatePath(Node node) {
        ArrayList<Node> path = new ArrayList<Node>();
        path.add(node);
        if (node.parent != null) {
            path.addAll(generatePath(node.parent));
        }
        return path;
    }

    private Node checkNode(Node currentNode) throws InterruptedException {
        this.currentNode = currentNode;
        sleep(AutoSolver.ANIMATION_DELAY);
        int x = currentNode.getX();
        int y = currentNode.getY();
        for (int tileId : currentNode.tile.getNeighbourTileIds()) {
            Node neighbourNode;
            if (tileId > -1) {
                neighbourNode = new Node(this.processing, this.gameTiles[tileId]);
                if (neighbourNode.minimumDistance == -1) {
                    continue;
                }
                if (currentNode.getNumberOfParents() + 1 < neighbourNode.minimumDistance) {
                    continue;
                }
            } else {
                continue;
            }
            // check if the node is already in the open list
            boolean alreadyInOpenList = false;
            for (Node node : this.openList) {
                if (node.tile == neighbourNode.tile) {
                    alreadyInOpenList = true;
                    // if we get faster to this node using the new one, change the parent accordingly...
                    int previousGCost = node.getGCost();
                    Node previousParent = node.parent;
                    node.parent = currentNode;
                    int newGCost = node.getGCost();
                    // but keep in mind that some nodes are only reachable after walking a certain distance (disappearing snake)
                    if (node.getNumberOfParents() >= neighbourNode.minimumDistance) {
                        node.parent = newGCost < previousGCost ? currentNode : previousParent;
                    } else {
                        node.parent = previousParent;
                    }
                }
            }
            boolean alreadyInClosedList = false;
            for (Node node : this.closedList) {
                if (node.tile == neighbourNode.tile) {
                    alreadyInClosedList = true;
                }
            }
            if (!alreadyInOpenList && !alreadyInClosedList) {
                neighbourNode.parent = currentNode;
                neighbourNode.hCost = this.calcHCost(x, y);
                // do not add nodes which were punished too hard to avoid infinite searches.
                int maxGCost = Processing.BOARD_HORIZONTAL_SIZE * Processing.BOARD_VERTICAL_SIZE;
                if (neighbourNode.getGCost() < maxGCost) {
                    synchronized (this.drawLock) {
                        this.openList.add(neighbourNode);
                    }
                }
            }
        }
        // move node from the open list to the closed list
        synchronized (this.drawLock) {
            for (int i = this.openList.size() - 1; i >= 0; i--) {
                Node node = this.openList.get(i);
                if (node == currentNode) {
                    this.openList.remove(i);
                }
            }
            this.closedList.add(currentNode);
        }
        // check if this is the target node
        if (x == this.endTile.x && y == this.endTile.y) {
            return currentNode;
        }
        // else, continue with the node with the least F cost
        if (this.openList.size() > 0) {
            Node nextNode;
            synchronized (this.drawLock) {
                nextNode = this.openList.get(0);
                for (Node node : this.openList) {
                    nextNode = node.getFCost() < nextNode.getFCost() ? node : nextNode;
                }
            }
            if (nextNode != null) {
                return this.checkNode(nextNode);
            }
        }
        return null;
    }

    float calcHCost(int x, int y) {
        int a = this.endTile.x - x;
        int b = this.endTile.y - y;
        float distance = PApplet.sqrt(a * a + b * b);
        //float distance = this.processing.abs(this.targetX - x) + this.processing.abs(this.targetY - y);
        return distance;
    }

    public void draw() {
        synchronized (this.drawLock) {
            for (Node node : this.openList) {
                node.draw(0xff00ff00);
            }
            for (Node node : this.closedList) {
                node.draw(0xffff0000);
            }
            processing.stroke(0xaa00ffff);
            processing.strokeWeight(5);
            int fromX = this.startTile.x * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
            int fromY = this.startTile.y * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
            int toX = this.endTile.x * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
            int toY = this.endTile.y * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
            this.processing.line(fromX, fromY, toX, toY);
            if (currentNode != null) {
                currentNode.draw(0xffdddddd);
            }
        }
        /*for (Node node : this.potentialAlternativesList) {
            node.draw(0xaa0000ff);
        }
        for (Node node : this.finalPath) {
            node.draw(0xffffff00);
        }*/
    }
}