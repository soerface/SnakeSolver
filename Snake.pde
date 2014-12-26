GameWorld gameWorld;

void setup() {
  size(500, 500);
  this.gameWorld = new GameWorld(width, height);
  setup_walls();
}

void setup_walls() {
  for (GameTile tile : this.gameWorld.gameTiles) {
    int rightBorder = width / tile.TILE_SIZE - 1;
    int bottomBorder = height / tile.TILE_SIZE - 1;
    if (tile.x == 0 || tile.y == 0 || tile.x == rightBorder || tile.y == bottomBorder) {
      tile.occupied = true;
    }
  }  
}

void draw() {
  fill(0xff000000);
  rect(0, 0, width, height);
  gameWorld.draw(this);
}
