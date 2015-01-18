package de.wegenerd;

import processing.core.PApplet;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class AStarPathFinder {

    private final GameTile[] gameTiles;
    private final Processing processing;
    private final GameTile startTile;
    private final GameTile endTile;
    private ArrayList<Node> openList;
    public ArrayList<Node> closedList;
    private final Object drawLock = new Object();
    private Node currentNode;
    private Node targetNode;
    public int alpha;

    AStarPathFinder(Processing processing, GameTile[] gameTiles, GameTile startTile, GameTile endTile) {
        this.processing = processing;
        this.gameTiles = gameTiles;
        this.startTile = startTile;
        this.endTile = endTile;
        this.openList = new ArrayList<Node>();
        this.closedList = new ArrayList<Node>();
        this.currentNode = null;
        this.targetNode = null;
        this.alpha = 0xff;
    }

    ArrayList<Node> getPath() throws InterruptedException {
        if (this.startTile == this.endTile) {
            return null;
        }
        if (this.targetNode == null) {
            Node node = new Node(this.processing, this.startTile);
            node.punish();
            this.checkNode(node, false);
        }
        if (this.targetNode != null) {
            return generatePath(this.targetNode);
        }
        return null;
    }

    void exploreAll() throws InterruptedException {
        if (this.openList.size() > 0) {
            this.checkNode(this.openList.get(0), true);
        } else if (this.currentNode == null) {
            Node node = new Node(this.processing, this.startTile);
            this.checkNode(node, false);
        }
    }

    static ArrayList<Node> generatePath(Node node) throws InterruptedException {
        ArrayList<Node> path = new ArrayList<Node>();
        path.add(node);
        if (node.parent != null) {
            path.addAll(generatePath(node.parent));
        }
        return path;
    }

    private Node checkNode(Node currentNode, boolean exploreAll) throws InterruptedException {
        this.currentNode = currentNode;
        sleep(AutoSolver.ANIMATION_DELAY);
        int x = currentNode.getX();
        int y = currentNode.getY();
        for (int tileId : currentNode.tile.getNeighbourTileIds()) {
            Node neighbourNode;
            if (tileId > -1) {
                neighbourNode = new Node(this.processing, this.gameTiles[tileId]);
                if (!exploreAll) {
                    // try to not punish the target node; except if it is directly besides us
                    //if (tileId != this.endTile.tileId || currentNode.getNumberOfParents() < 1) {
                        neighbourNode.punish();
                    //}
                }
                // do not walk into tiles which are occupied; except if it is the desired target
                if (neighbourNode.minimumDistance == -1) {
                    continue;
                }
                if (tileId != this.endTile.tileId && currentNode.getNumberOfParents() + 1 < neighbourNode.minimumDistance) {
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
            // we do not want neighbours of the target node in our open list, if the target node is occupied
            if (currentNode.tile != this.endTile || currentNode.minimumDistance <= currentNode.getNumberOfParents()) {
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
            this.targetNode = currentNode;
            if (!exploreAll) {
                return currentNode;
            }
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
                return this.checkNode(nextNode, exploreAll);
            }
        }
        return null;
    }

    float calcHCost(int x, int y) {
        int a = this.endTile.x - x;
        int b = this.endTile.y - y;
        //float distance = PApplet.sqrt(a * a + b * b);
        float distance = PApplet.abs(this.endTile.x - x) + PApplet.abs(this.endTile.y - y);
        return distance;
    }

    public void draw() {
        this.draw(-1);
    }

    public void draw(int color) {
        synchronized (this.drawLock) {
            for (Node node : this.openList) {
                node.draw(color != -1 ? color : 0xff00ff00, this.alpha);
            }
            for (Node node : this.closedList) {
                node.draw(color != -1 ? color : 0xffff0000, this.alpha);
            }
            processing.stroke(color != -1 ? color : 0xaaff00ff, this.alpha);
            processing.strokeWeight(5);
            int fromX = this.startTile.x * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
            int fromY = this.startTile.y * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
            int toX = this.endTile.x * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
            int toY = this.endTile.y * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
            this.processing.line(fromX, fromY, toX, toY);
            Node checkingNode = this.currentNode;
            while (checkingNode != null) {
                checkingNode.draw(0xffffff00, this.alpha);
                checkingNode = checkingNode.parent;
            }
        }
    }
}