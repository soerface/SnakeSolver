GameWorld gameWorld;

void setup() {
  size(500, 500);
  this.gameWorld = new GameWorld(width, height);
  for (GameTile tile : gameWorld.gameTiles) {
    tile.occupied = random(0, 1) > 0.5;
  }
}

void draw() {
  fill(0xff000000);
  rect(0, 0, width, height);
  gameWorld.draw(this);
}
