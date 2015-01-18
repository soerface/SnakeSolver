/**
 * This class is implementing a modified version of A* to play the game automatically.
 * It needs refactoring, got pretty confusing in the meanwhile :/
 *
 * It contains nothing of the snake gamelogic, so you dont need to read it
 * if you are just checking my homework.
 */
package de.wegenerd;

import processing.core.PConstants;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Thread.sleep;

class AutoSolver {

    GameWorld gameWorld;
    Processing processing;
    ArrayList<Node> openList;
    ArrayList<Node> closedList;
    ArrayList<Node> finalPath;
    ArrayList<Node> potentialAlternativesList;
    ArrayList<Integer> potentialAlternativesBlackList; // just contains tile ids
    ArrayList<Integer> punishedTiles;
    boolean pathFound;
    static int ANIMATION_DELAY = 10;
    AStarPathFinder aStarPathFinder;
    TailPathFinder tailPathFinder;
    DeadEndChecker deadEndChecker;
    final Object drawLock = new Object();

    AutoSolver(Processing processing, GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.processing = processing;
        this.openList = new ArrayList<Node>();
        this.closedList = new ArrayList<Node>();
        this.finalPath = new ArrayList<Node>();
        this.potentialAlternativesList = new ArrayList<Node>();
        this.potentialAlternativesBlackList = new ArrayList<Integer>();
        this.punishedTiles = new ArrayList<Integer>();
        this.pathFound = false;
    }

    void calculatePath() throws InterruptedException {
        synchronized (this.drawLock) {
            this.openList = new ArrayList<Node>();
            this.closedList = new ArrayList<Node>();
            this.finalPath = new ArrayList<Node>();
            this.potentialAlternativesList = new ArrayList<Node>();
            this.tailPathFinder = null;
        }
        int x = this.gameWorld.snakeX;
        int y = this.gameWorld.snakeY;
        GameTile snakeHeadGameTile = this.gameWorld.gameTiles[GameTile.getTileIdByCoordinates(x, y)];
        // search for tile with food
        GameTile foodGameTile = null;
        for (GameTile gameTile : this.gameWorld.gameTiles) {
            if (gameTile.hasFood) {
                foodGameTile = gameTile;
            }
        }
        if (foodGameTile == null) {
            return;
        }

        ArrayList<Node> path;
        synchronized (drawLock) {
            this.aStarPathFinder = new AStarPathFinder(this.processing, this.gameWorld.gameTiles, snakeHeadGameTile, foodGameTile);
        }
        path = this.aStarPathFinder.getPath();
        if (path == null) {
            synchronized (drawLock) {
                this.aStarPathFinder = null;
                this.tailPathFinder = new TailPathFinder(this.processing, this.gameWorld.gameTiles, snakeHeadGameTile);
            }
            path = this.tailPathFinder.getPath();
        }
        if (path != null) {
            this.deadEndChecker = new DeadEndChecker(this.processing, this.gameWorld.gameTiles, path);
            this.finalPath = path;
            if (!this.deadEndChecker.isDeadEnd()) {
                this.pathFound = true;
            }
        }
    }
/*
    void checkNode(Node startNode) throws InterruptedException {
        if (ANIMATION_DELAY > 0) {
            sleep(ANIMATION_DELAY);
        }
        if (startNode == null) {
            return;
        }
        int x = startNode.getX();
        int y = startNode.getY();
        for (Node neighbourNode : this.getNeighbourNodes(startNode, this.gameWorld.gameTiles, this.potentialAlternativesList, this.potentialAlternativesBlackList)) {
            // check if the node is already in the open list
            boolean alreadyInOpenList = false;
            for (Node node : this.openList) {
                if (node.tileId == neighbourNode.tileId) {
                    alreadyInOpenList = true;
                    // if we get faster to this node using the new one, change the parent accordingly...
                    int previousGCost = node.getGCost();
                    Node previousParent = node.parent;
                    node.parent = startNode;
                    int newGCost = node.getGCost();
                    // but keep in mind that some nodes are only reachable after walking a certain distance (disappearing snake)
                    if (node.getNumberOfParents() >= neighbourNode.minimumDistance) {
                        node.parent = newGCost < previousGCost ? startNode : previousParent;
                    } else {
                        node.parent = previousParent;
                    }
                }
            }
            boolean alreadyInClosedList = false;
            for (Node node : this.closedList) {
                if (node.tileId == neighbourNode.tileId) {
                    alreadyInClosedList = true;
                }
            }
            if (!alreadyInOpenList && !alreadyInClosedList) {
                neighbourNode.parent = startNode;
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
                if (node == startNode) {
                    this.openList.remove(i);
                }
            }
            this.closedList.add(startNode);
        }
        // check if this is the target node
        if (x == targetX && y == targetY) {
            boolean isDeadEnd = this.checkForDeadEnd(startNode);

            if (isDeadEnd) {
                // dead end. Punish nodes on path to try an alternative
                synchronized (this.drawLock) {
                    this.finalPath = new ArrayList<Node>();
                }
                this.generateFinalPath(startNode);
                for (Node node : this.finalPath) {
                    this.punishedTiles.add(node.tileId);
                }
                synchronized (this.drawLock) {
                    this.openList = new ArrayList<Node>();
                    this.closedList = new ArrayList<Node>();
                    this.finalPath = new ArrayList<Node>();
                    this.potentialAlternativesList = new ArrayList<Node>();
                }
                this.potentialAlternativesBlackList = new ArrayList<Integer>();
                this.pathFound = false;
                this.checkNode(this.nextNode);
                return;
            } else {
                // found a path which does not lead into a dead end
                this.nextNode = null;
                synchronized (this.drawLock) {
                    this.finalPath = new ArrayList<Node>();
                }
                this.generateFinalPath(startNode);
                this.pathFound = true;
                // we found a path, but we might be faster taking an alternative route
                this.potentialAlternativesBlackList = new ArrayList<Integer>();
                this.punishedTiles = new ArrayList<Integer>();
                //this.generateAlternativePath(); (not that important to find an even shorter path
                return;
            }
        }
        // else, continue with the node with the least F cost
        if (this.openList.size() > 0) {
            synchronized (this.drawLock) {
                this.nextNode = this.openList.get(0);
                for (Node node : this.openList) {
                    this.nextNode = node.getFCost() < nextNode.getFCost() ? node : nextNode;
                }
            }
            this.checkNode(nextNode);
        } else {
            // okay, we didn't find a path, but there is nothing in the open list
            // the food must be hidden behind us.
            // try to find an alternative path by making the path longer and therefore escape
            synchronized (this.drawLock) {
                this.nextNode = null;
            }
            this.generateAlternativePath();
        }
    }
    */

    void draw() {
        synchronized (this.drawLock) {
            if (this.tailPathFinder != null) {
                this.tailPathFinder.draw();
            }
            if (this.aStarPathFinder != null) {
                this.aStarPathFinder.draw();
            }
            for (Node node : this.finalPath) {
                node.draw(0xffffff00);
            }
            if (this.deadEndChecker != null) {
                this.deadEndChecker.draw();
            }
//            for (Node node : this.openList) {
//                node.draw(0xaa00ff00);
//            }
//            for (Node node : this.closedList) {
//                node.draw(0xaaff0000);
//            }
//            for (Node node : this.potentialAlternativesList) {
//                node.draw(0xaa0000ff);
//            }
//            for (Node node : this.finalPath) {
//                node.draw(0xffffff00);
//            }
//            if (this.nextNode != null) {
//                int x = this.nextNode.getX() * GameTile.TILE_SIZE;
//                int y = this.nextNode.getY() * GameTile.TILE_SIZE;
//                this.processing.fill(0xffffffff);
//                this.processing.rect(x, y, GameTile.TILE_SIZE, GameTile.TILE_SIZE);
//                this.nextNode.draw(0xff000000);
//            }
        }
        this.processing.textAlign(PConstants.LEFT, PConstants.BOTTOM);
        this.processing.textSize(10);
        String text = "Press [v] to toggle visualization, [i] to toggle interactive mode, [ ] to pause visualization, [n] to step through visualization";
        float width = this.processing.textWidth(text);
        this.processing.fill(0xffffffff);
        this.processing.strokeWeight(0);
        this.processing.rect(1, this.processing.height - GameTile.TILE_SIZE, width, GameTile.TILE_SIZE);
        this.processing.fill(0xff000000);
        this.processing.text(text, 5, this.processing.height);
    }

    void tick() throws InterruptedException {
        if (!this.pathFound) {
            this.gameWorld.gamePaused = true;
            this.calculatePath();
        } else {
            this.gameWorld.gamePaused = false;
            int lastElement = this.finalPath.size() - 1;
            if (lastElement == 1) {
                this.pathFound = false;
            }
            if (this.finalPath.size() <= 1) {
                synchronized (this.drawLock) {
                    this.finalPath = new ArrayList<Node>();
                }
                return;
            }
            Node fromNode = this.finalPath.get(lastElement);
            Node toNode = this.finalPath.get(lastElement - 1);
            int fromX = fromNode.getX();
            int fromY = fromNode.getY();
            int toX = toNode.getX();
            int toY = toNode.getY();
            if (toX > fromX) {
                this.gameWorld.snakeDirection = GameWorld.RIGHT;
            } else if (toX < fromX) {
                this.gameWorld.snakeDirection = GameWorld.LEFT;
            } else if (toY > fromY) {
                this.gameWorld.snakeDirection = GameWorld.DOWN;
            } else if (toY < fromY) {
                this.gameWorld.snakeDirection = GameWorld.UP;
            }
            synchronized (this.drawLock) {
                this.finalPath.remove(lastElement);
            }
        }
    }
}
