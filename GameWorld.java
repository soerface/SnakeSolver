class GameWorld {

  GameTile[] gameTiles;
  int width;
  int height;
  static final int INITIAL_SNAKE_LENGTH = 5;

  GameWorld(int width, int height) {
    this.width = width / GameTile.TILE_SIZE;
    this.height = height / GameTile.TILE_SIZE;
    int worldArea = width * height;
    int tileArea = GameTile.TILE_SIZE * GameTile.TILE_SIZE;
    int numberOfTiles = worldArea / tileArea;
    this.gameTiles = new GameTile[numberOfTiles]; //<>//
    for (int i=0; i<numberOfTiles; i++) {
      int x = i % this.width;
      int y = i / this.width;
      this.gameTiles[i] = new GameTile(x, y);
    }
    this.respawn();
  }
  
  void draw(Snake mainClass) {
    for (GameTile tile : this.gameTiles) {
      tile.draw(mainClass);
    }
  }
  
  void tick() {
    for (GameTile tile : this.gameTiles) {
      tile.tick();
    }
  } //<>//

  void respawn() {
    this.respawn(true);
  }

  void respawn(boolean withWalls) {
    int i = 0;
    int occupiedCounter = 1;
    for (GameTile tile : this.gameTiles) {
      int rightBorder = this.width - 1;
      int bottomBorder = this.height - 1;
      // setup walls
      if (tile.x == 0 || tile.y == 0 || tile.x == rightBorder || tile.y == bottomBorder) {
        tile.occupied = true;
      }
      // startposition of snake in the middle of the world
      if (tile.x > this.width / 2 - INITIAL_SNAKE_LENGTH - 1 && tile.x < this.width / 2 && tile.y == this.height / 2) {
        tile.occupied = true;
        tile.occupiedCounter = occupiedCounter;
        occupiedCounter++;
      }
      i++;
    }
  }
}
