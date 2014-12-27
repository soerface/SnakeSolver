GameWorld gameWorld;
AutoSolver autoSolver;
int frames = 0;
static final int TICKS_PER_FRAME = 10; // decrease this number to speedup the game

void setup() {
  size(600, 500);
  this.gameWorld = new GameWorld(this);
}

void draw() {
  fill(0xff000000);
  rect(0, 0, width, height);
  gameWorld.draw();
  frames++;
  if (frames % TICKS_PER_FRAME == 0) {
    if (this.autoSolver != null) {
      this.autoSolver.tick();
    }
    gameWorld.tick();
  }
  if (this.autoSolver != null) {
    this.autoSolver.draw();
  }
}

void keyPressed() {
  if (!this.gameWorld.gameStarted) {
    if (key == 's' || key == 'S') {
      this.gameWorld.gameStarted = true;
    }
    if (key == 'a' || key == 'A') {
      this.gameWorld.gameStarted = true;
      this.autoSolver = new AutoSolver(this, this.gameWorld);
    }
    if (key == 'v' || key == 'V') {
      this.gameWorld.gameStarted = true;
      this.autoSolver = new AutoSolver(this, this.gameWorld);
      this.autoSolver.visualize = true;
    }
    if (key == 'i' || key == 'I') {
      this.gameWorld.gameStarted = true;
      this.gameWorld.spawnFood = false;
      this.autoSolver = new AutoSolver(this, this.gameWorld);
      this.autoSolver.visualize = true;
    }
    return;
  }
  if (this.autoSolver != null) {
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

void mouseClicked() {
  if (!this.gameWorld.spawnFood) {
    int x = mouseX / GameTile.TILE_SIZE;
    int y = mouseY / GameTile.TILE_SIZE;
    this.gameWorld.spawnFood(x, y);
  }
}
