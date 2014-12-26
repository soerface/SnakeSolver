class GameWorld {

  GameTile[] gameTiles;

  GameWorld(int width, int height) {
    int worldArea = width * height;
    int tileArea = GameTile.TILE_SIZE * GameTile.TILE_SIZE;
    int numberOfTiles = worldArea / tileArea;
    this.gameTiles = new GameTile[numberOfTiles]; //<>//
    for (int i=0; i<numberOfTiles; i++) {
      int x = i % (width / GameTile.TILE_SIZE);
      int y = i / (height / GameTile.TILE_SIZE);
      this.gameTiles[i] = new GameTile(x, y);
    }
  }
  
  void draw(Snake mainClass) {
    for (GameTile tile : this.gameTiles) {
      tile.draw(mainClass); //<>//
    }
  }
  
}
