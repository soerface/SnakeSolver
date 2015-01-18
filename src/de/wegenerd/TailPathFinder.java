package de.wegenerd;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Thread.sleep;

public class TailPathFinder {

    private Processing processing;
    private GameTile[] gameTiles;
    private GameTile startTile;
    private ArrayList<GameTile> snakeTiles;
    public AStarPathFinder aStarPathFinder;
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
                    path = PathUtils.increasePathLength(path, this.aStarPathFinder.closedList);
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

    public void draw() {
        this.draw(-1);
    }

    public void draw(int color) {
        if (this.aStarPathFinder != null) {
            this.aStarPathFinder.draw(color);
        }
    }
}