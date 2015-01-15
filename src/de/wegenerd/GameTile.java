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

  void draw(Processing mainClass) {
    this.draw(mainClass, 0xffffffff, 0xff000000, 0xff333333);
  }

  void draw(Processing mainClass, int occupiedColor, int freeColor, int strokeColor) {
    int color = this.occupied ? occupiedColor : freeColor;
    mainClass.fill(color);
    mainClass.stroke(strokeColor);
    mainClass.strokeWeight(1);
    int x = this.x * TILE_SIZE;
    int y = this.y * TILE_SIZE;
    mainClass.rect(x, y, TILE_SIZE, TILE_SIZE);
    if (this.hasFood) {
      int margin = TILE_SIZE / 3;
      mainClass.stroke(0xffffffaa);
      mainClass.line(x + margin, y + margin, x + TILE_SIZE - margin, y + TILE_SIZE - margin);
      mainClass.line(x + margin, y + TILE_SIZE - margin, x + TILE_SIZE - margin, y + margin);
    }
    if (mainClass.DEBUG) {
      color = this.occupied ? freeColor : occupiedColor;
      mainClass.fill(color);
      mainClass.textAlign(mainClass.LEFT, mainClass.TOP);
      mainClass.text(this.occupiedCounter, x+1, y);
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
