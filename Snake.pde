GameWorld gameWorld;
int fames = 0;
static final int TICKS_PER_FRAME = 10; // decrease this number to speedup the game

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

void keyPressed() {
  switch(key) {
  case 'w':
  case 'W':
    if (this.gameWorld.snakeDirection != this.gameWorld.DOWN) {
      this.gameWorld.snakeDirection = this.gameWorld.UP;
    }
    break;
  case 'a':
  case 'A':
    if (this.gameWorld.snakeDirection != this.gameWorld.RIGHT) {
      this.gameWorld.snakeDirection = this.gameWorld.LEFT;
    }
    break;
  case 's':
  case 'S':
    if (this.gameWorld.snakeDirection != this.gameWorld.UP) {
      this.gameWorld.snakeDirection = this.gameWorld.DOWN;
    }
    break;
  case 'd':
  case 'D':
    if (this.gameWorld.snakeDirection != this.gameWorld.LEFT) {
      this.gameWorld.snakeDirection = this.gameWorld.RIGHT;
    }
    break;
  }
}
