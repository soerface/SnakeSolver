package de.wegenerd;

class GameTile {
  
  static final int TILE_SIZE = 30; // I recommend a value of 20
  final int x;
  final int y;
  boolean hasFood;
  boolean occupied;
  // -1: indefinitely occupied; else, it will be decreased every tick by 1, giving the field free when reaching 0
  int occupiedCounter = -1;
  
  GameTile(int x, int y) {
    this.x = x;
    this.y = y;
  }

  void draw(Processing processing) {
    this.draw(processing, 0xffffffff, 0xff000000, 0xff333333);
  }

  void draw(Processing processing, int occupiedColor, int freeColor, int strokeColor) {
    int color = this.occupied ? occupiedColor : freeColor;
    processing.fill(color);
    processing.stroke(strokeColor);
    processing.strokeWeight(1);
    int x = this.x * TILE_SIZE;
    int y = this.y * TILE_SIZE;
    processing.rect(x, y, TILE_SIZE, TILE_SIZE);
    if (this.hasFood) {
      int margin = TILE_SIZE / 3;
      processing.stroke(0xffffffaa);
      processing.line(x + margin, y + margin, x + TILE_SIZE - margin, y + TILE_SIZE - margin);
      processing.line(x + margin, y + TILE_SIZE - margin, x + TILE_SIZE - margin, y + margin);
    }
    if (processing.DEBUG) {
      color = this.occupied ? freeColor : occupiedColor;
      processing.fill(color);
      processing.textAlign(processing.LEFT, processing.TOP);
      processing.text(this.occupiedCounter, x+1, y);
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
}
