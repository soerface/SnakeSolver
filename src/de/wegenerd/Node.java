package de.wegenerd;

import processing.core.PConstants;

class Node {
    public GameTile tile;
    public float hCost;
    public int minimumDistance;
    public Node parent;
    private Processing processing;
    private static int DOT_SIZE = GameTile.TILE_SIZE / 4;
    private int costs;

    Node(Processing processing, GameTile tile) {
        this.processing = processing;
        this.tile = tile;
        this.minimumDistance = tile.occupied ? tile.occupiedCounter : 0;
        this.costs = 1;
    }

    void punish() {
        for (int tileId : this.processing.autoSolver.punishedTiles) {
            if (tileId == this.tile.tileId) {
                this.costs *= 2;
            }
        }
    }

    int getGCost() {
        if (this.parent != null) {
            return this.parent.getGCost() + this.costs;
        }
        return 0;
    }

    int getNumberOfParents() {
        if (this.parent != null) {
            return this.parent.getNumberOfParents() + 1;
        }
        return 0;
    }


    int getFCost() {
        return this.getGCost() + (int) this.hCost;
    }

    int getX() {
        return this.tile.x;
    }

    int getY() {
        return this.tile.y;
    }

    void draw(int color) {
        this.draw(color, 0xff);
    }

    void draw(int color, int alpha) {
        this.processing.strokeWeight(0);
        this.processing.fill(color, alpha);
        int x = this.getX() * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
        int y = this.getY() * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
        this.processing.strokeWeight(1);
        this.processing.stroke(color, alpha);
        if (this.parent != null) {
            int parentX = this.parent.getX() * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
            int parentY = this.parent.getY() * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
            float vectorX = parentX - x;
            float vectorY = parentY - y;
            vectorX *= 0.4;
            vectorY *= 0.4;
            this.processing.line(x, y, x + vectorX, y + vectorY);
        }
        this.processing.ellipse(x, y, DOT_SIZE, DOT_SIZE);
        if (Processing.DEBUG) {
            processing.fill(color, alpha);
            processing.textAlign(PConstants.LEFT, PConstants.BOTTOM);
            x = this.getX() * GameTile.TILE_SIZE;
            y = this.getY() * GameTile.TILE_SIZE + GameTile.TILE_SIZE;
            processing.text(this.getGCost(), x + 1, y);
            //processing.textAlign(PConstants.RIGHT, PConstants.BOTTOM);
            //processing.text(this.tile.tileId, x+GameTile.TILE_SIZE-1, y);
        }
    }
}
