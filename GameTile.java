class GameTile {
  
  static final int TILE_SIZE = 10;
  final int x;
  final int y;
  
  GameTile(int x, int y) {
    this.x = x;
    this.y = y;
  }

  void draw(Snake mainClass) {
    float r = mainClass.random(0, 255);
    float g = mainClass.random(0, 255);
    float b = mainClass.random(0, 255);
    mainClass.fill(r, g, b);
    mainClass.stroke(0xffaaaaaa);
    mainClass.rect(this.x * TILE_SIZE, this.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
  }
}
