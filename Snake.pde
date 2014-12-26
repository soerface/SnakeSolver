GameWorld gameWorld;

void setup() {
  size(600, 500);
  this.gameWorld = new GameWorld(width, height);
}

void draw() {
  fill(0xff000000);
  rect(0, 0, width, height);
  gameWorld.draw(this);
}
