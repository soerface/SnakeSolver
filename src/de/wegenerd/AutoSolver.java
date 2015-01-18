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
    static int ANIMATION_DELAY = 0;
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
            // no direct path to food possible. Find the tail of the snake
            synchronized (drawLock) {
                this.aStarPathFinder = null;
                this.tailPathFinder = new TailPathFinder(this.processing, this.gameWorld.gameTiles, snakeHeadGameTile);
            }
            path = this.tailPathFinder.getPath();
        }
        if (path != null) {
            // direct path possible; check if following the path would lead to a dead end
            synchronized (this.drawLock) {
                this.deadEndChecker = new DeadEndChecker(this.processing, this.gameWorld.gameTiles, path);
            }
            if (!this.deadEndChecker.isDeadEnd()) {
                // its save to take the path; go for it!
                this.finalPath = path;
                this.pathFound = true;
                this.punishedTiles = new ArrayList<Integer>();
                synchronized (this.drawLock) {
                    this.deadEndChecker = null;
                    this.tailPathFinder = null;
                }
            } else {
                // not a good path. Change the edge weights to get a new path in the next iteration
                this.pathFound = false;
                for (Node node : path) {
                    this.punishedTiles.add(node.tile.tileId);
                }
            }
        }
    }

    void draw() {
        synchronized (this.drawLock) {
            if (this.tailPathFinder != null) {
                this.tailPathFinder.draw();
            }
            if (this.aStarPathFinder != null) {
                if (this.deadEndChecker != null) {
                    this.aStarPathFinder.alpha = 0x33;
                } else {
                    this.aStarPathFinder.alpha = 0xff;
                }
                this.aStarPathFinder.draw();
            }
            for (Node node : this.finalPath) {
                node.draw(0xffff00);
            }
            if (this.deadEndChecker != null) {
                this.deadEndChecker.draw();
            }
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
