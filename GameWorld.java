class GameWorld {

  GameTile[] gameTiles;

  GameWorld(int width, int height) {
    int worldArea = width * height;
    int tileArea = GameTile.TILE_SIZE * GameTile.TILE_SIZE;
    int numberOfTiles = worldArea / tileArea;
    this.gameTiles = new GameTile[numberOfTiles]; //<>//
  }
  
  void draw(Snake mainClass) {
    for (GameTile tile : this.gameTiles) {
      tile.draw(mainClass); //<>//
    }
  }
  
}
