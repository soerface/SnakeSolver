package de.wegenerd;

import java.util.ArrayList;

/**
 * Dead end checker
 * To check if we are running into a dead end, we just need to see if we could reach our tail after we arrive at the food
 * because if we can reach our tail we will be able to reach every other field
 */
public class DeadEndChecker {
    private Processing processing;
    private GameTile[] gameTiles;
    private ArrayList<Node> path;
    private int snakeLength;
    public TailPathFinder tailPathFinder;

    public DeadEndChecker(Processing processing, GameTile[] gameTiles, ArrayList<Node> path) {
        this.processing = processing;
        this.path = path;
        this.snakeLength = 0;
        for (GameTile gameTile : gameTiles) {
            this.snakeLength = gameTile.occupiedCounter > this.snakeLength ? gameTile.occupiedCounter : this.snakeLength;
        }
        this.gameTiles = this.simulatePath(gameTiles);
    }

    public void draw() {
        this.tailPathFinder.draw(0xff5C95FF);
        for (GameTile gameTile : this.gameTiles) {
            if (gameTile.occupiedCounter > -1) {
                gameTile.draw(this.processing, 0xdd5C95FF, 0xff000000, 0xff000000);
            }
        }
    }

    private GameTile[] simulatePath(GameTile[] gameTiles) {
        GameTile[] newTiles = new GameTile[gameTiles.length];
        int pathLength = this.path.size() - 1;
        int i = 0;
        for (GameTile tile : gameTiles) {
            newTiles[i] = new GameTile(tile.x, tile.y);
            if (tile.occupied && tile.occupiedCounter == -1) {
                // tile which are forever occupied
                newTiles[i].occupied = true;
            } else if (tile.occupied && tile.occupiedCounter - pathLength > 0) {
                // tiles which will still be occupied after moving
                newTiles[i].occupied = true;
                newTiles[i].occupiedCounter = tile.occupiedCounter - pathLength + 1; // + 1 since we will have collected food when arriving
            }
            i++;
        }
        i = 0;
        for (Node node : this.path) {
            GameTile tile = newTiles[node.tile.tileId];
            if (this.snakeLength - i > 0) {
                tile.occupied = true;
                tile.occupiedCounter = this.snakeLength - i + 1; // + 1 since we will have collected food when arriving
            } else {
                break;
            }
            i++;
        }
        return newTiles;
    }

    boolean isDeadEnd() throws InterruptedException {
        GameTile snakeHeadTile = null;
        for (GameTile tile : this.gameTiles) {
            if (!tile.occupied || tile.occupiedCounter < 0) {
                continue;
            }
            if (snakeHeadTile == null) {
                snakeHeadTile = tile;
            }
            else if (tile.occupiedCounter > snakeHeadTile.occupiedCounter) {
                snakeHeadTile = tile;
            }
        }
        this.tailPathFinder = new TailPathFinder(this.processing, this.gameTiles, snakeHeadTile);
        this.tailPathFinder.padding = 1;
        return this.tailPathFinder.getPath() == null;
    }
}
