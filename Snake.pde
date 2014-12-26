GameWorld gameWorld;
int fames = 0;
static final int TICKS_PER_FRAME = 10; // decrease this number to speedup the game

void setup() {
  size(600, 500);
  this.gameWorld = new GameWorld(this);
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
  if (!gameWorld.gameStarted) {
    if (key == 's' || key == 'S') {
      gameWorld.gameStarted = true;
    }
    return;
  }

  int pressedKey = key;
  // support for arrow keys
  if (key == CODED) {
    pressedKey = keyCode;
  }
  switch(pressedKey) {
    // regular controls
  case 'w':
  case 'W':
  // vim style controls
  case 'k':
  case 'K':
  // arrow keys
  case UP:
    if (this.gameWorld.snakeDirection != this.gameWorld.DOWN) {
      this.gameWorld.snakeDirection = this.gameWorld.UP;
    }
    break;
  case 'a':
  case 'A':
  case 'h':
  case 'H':
  case LEFT:
    if (this.gameWorld.snakeDirection != this.gameWorld.RIGHT) {
      this.gameWorld.snakeDirection = this.gameWorld.LEFT;
    }
    break;
  case 's':
  case 'S':
  case 'j':
  case 'J':
  case DOWN:
    if (this.gameWorld.snakeDirection != this.gameWorld.UP) {
      this.gameWorld.snakeDirection = this.gameWorld.DOWN;
    }
    break;
  case 'd':
  case 'D':
  case 'l':
  case 'L':
  case RIGHT:
    if (this.gameWorld.snakeDirection != this.gameWorld.LEFT) {
      this.gameWorld.snakeDirection = this.gameWorld.RIGHT;
    }
    break;
  }
}
