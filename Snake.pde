GameWorld gameWorld;
int fames = 0;
static final int TICKS_PER_FRAME = 25; // decrease this number to speedup the game

void setup() {
  size(600, 500);
  this.gameWorld = new GameWorld(width, height);
}

void draw() {
  fill(0xff000000);
  rect(0, 0, width, height);
  gameWorld.draw(this);
  fames++;
  if (fames % TICKS_PER_FRAME == 0) {
    gameWorld.tick();
  }
}
