class GameTile {
  
  static final int TILE_SIZE = 10;
  final int x;
  final int y;
  boolean occupied;
  // -1: indefinitely occupied; else, it will be decreased every tick by 1, giving the field free when reaching 0
  int occupiedCounter = -1;
  
  GameTile(int x, int y) {
    this.x = x;
    this.y = y;
  }

  void draw(Snake mainClass) {
    int color = this.occupied ? 0xffffffff : 0xff000000;
    mainClass.fill(color);
    mainClass.stroke(0xffaaaaaa);
    mainClass.rect(this.x * TILE_SIZE, this.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
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
