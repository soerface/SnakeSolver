package de.wegenerd;

import processing.core.PConstants;

import java.util.Comparator;

class GameTile implements Comparable<GameTile> {

    static final int TILE_SIZE = 40;
    final int x;
    final int y;
    final int tileId;
    boolean hasFood;
    boolean occupied;
    // -1: indefinitely occupied; else, it will be decreased every tick by 1, giving the field free when reaching 0
    int occupiedCounter = -1;
    GameTile parent;

    GameTile(int x, int y) {
        this.x = x;
        this.y = y;
        this.tileId = y * GameWorld.width + x;
    }

    static int getTileIdByCoordinates(int x, int y) {
        return y * GameWorld.width + x;
    }

    int[] getNeighbourTileIds() {
        return new int[]{
                GameWorld.getTileId(this.x - 1, this.y),
                GameWorld.getTileId(this.x, this.y - 1),
                GameWorld.getTileId(this.x + 1, this.y),
                GameWorld.getTileId(this.x, this.y + 1)
        };
    }

    void draw(Processing processing) {
        this.draw(processing, 0xffffffff, 0xff000000, 0xffffffff);
    }

    void draw(Processing processing, int occupiedColor, int freeColor, int strokeColor) {
        processing.stroke(strokeColor);
        processing.strokeWeight(1);
        int x = this.x * TILE_SIZE;
        int y = this.y * TILE_SIZE;
        if (this.occupied && this.occupiedCounter < 0) {
            processing.fill(occupiedColor);
            processing.rect(x, y, TILE_SIZE, TILE_SIZE);
        } else if (this.occupied) {
            int innerSize = TILE_SIZE / 2;
            processing.fill(occupiedColor);
            processing.rect(x + innerSize / 2, y + innerSize / 2, innerSize, innerSize);
            if (this.parent != null && this.parent.occupied && this.parent.occupiedCounter == this.occupiedCounter - 1) {
                int parentX = parent.x * TILE_SIZE;
                int parentY = parent.y * TILE_SIZE;
                int deltaX = (parentX - x) / 2;
                int deltaY = (parentY - y) / 2;
                processing.rect(x + deltaX + innerSize / 2, y + deltaY + innerSize / 2, innerSize, innerSize);
            }
        }
        if (this.hasFood) {
            int margin = TILE_SIZE / 3;
            processing.stroke(0xffffffaa);
            processing.line(x + margin, y + margin, x + TILE_SIZE - margin, y + TILE_SIZE - margin);
            processing.line(x + margin, y + TILE_SIZE - margin, x + TILE_SIZE - margin, y + margin);
        }
        if (Processing.DEBUG) {
            int color = this.occupied ? freeColor : occupiedColor;
            processing.fill(color);
            processing.textAlign(PConstants.CENTER, PConstants.CENTER);
            processing.text(this.occupiedCounter, x + TILE_SIZE / 2, y + TILE_SIZE / 2);
        }
    }

    void tick() {
        if (this.occupiedCounter > 0) {
            occupiedCounter--;
        }
        if (this.occupiedCounter == 0) {
            this.occupied = false;
        }
    }

    @Override
    public int compareTo(GameTile gameTile) {
        return this.occupiedCounter - gameTile.occupiedCounter;
    }
}
