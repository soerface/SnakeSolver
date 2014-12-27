class GameWorld { //<>// //<>//

  GameTile[] gameTiles;
  int width;
  int height;
  int snakeX;
  int snakeY;
  int snakeDirection;
  int snakeLength;
  boolean gameStarted;
  boolean gamePaused;
  Snake mainClass;
  static final int UP = 0;
  static final int RIGHT = 1;
  static final int DOWN = 2;
  static final int LEFT = 3;
  static final int INITIAL_SNAKE_LENGTH = 5;

  GameWorld(Snake mainClass) {
    this.mainClass = mainClass;
    this.gameStarted = false;
    this.gamePaused = false;
    int width = mainClass.width;
    int height = mainClass.height;
    this.width = width / GameTile.TILE_SIZE;
    this.height = height / GameTile.TILE_SIZE;
    int worldArea = width * height;
    int tileArea = GameTile.TILE_SIZE * GameTile.TILE_SIZE;
    int numberOfTiles = worldArea / tileArea;
    this.gameTiles = new GameTile[numberOfTiles];
    for (int i=0; i<numberOfTiles; i++) {
      int x = i % this.width;
      int y = i / this.width;
      this.gameTiles[i] = new GameTile(x, y);
    }
    this.respawn();
  }

  void draw() {
    if (!this.gameStarted) {
      this.mainClass.textAlign(this.mainClass.CENTER);
      int x = this.mainClass.width / 2;
      int y = this.mainClass.height / 2;
      this.mainClass.fill(0xffffffff);
      this.mainClass.textSize(32);
      this.mainClass.text("Press [s] to start the game\n", x, y);
      this.mainClass.textSize(24);
      this.mainClass.text(
      "\n[a] to let the computer play automatically" +
      "\n[v] to let the computer play with visualization", x, y);
      return;
    }
    for (GameTile tile : this.gameTiles) {
      tile.draw(mainClass);
    }
  }

  void tick() {
    if (!this.gameStarted || this.gamePaused) {
      return;
    }
    switch(this.snakeDirection) {
    case UP:
      this.snakeY--;
      break;
    case RIGHT:
      this.snakeX++;
      break;
    case DOWN:
      this.snakeY++;
      break;
    case LEFT:
      this.snakeX--;
      break;
    }
    boolean foodCollected = false;
    for (GameTile tile : this.gameTiles) {
      tile.tick();
      // check for collision
      if (tile.occupied && tile.x == this.snakeX && tile.y == this.snakeY) {
        this.respawn();
        return;
      }
      // and for collected food
      if (tile.hasFood && tile.x == this.snakeX && tile.y == this.snakeY) {
        tile.hasFood = false;
        foodCollected = true;
      }
      // and then set the new block to occupied
      if (tile.x == this.snakeX && tile.y == this.snakeY) {
        tile.occupied = true;
        tile.occupiedCounter = this.snakeLength;
      }
    }
    // we found some food! increase the occupiedCounter on each tile to increase the snakes length
    if (foodCollected) {
      for (GameTile tile : this.gameTiles) {
        // do not increase it for negative counters. They are staying forever anyway
        if (tile.occupiedCounter > 0) {
          tile.occupiedCounter++;
        }
      }
      this.snakeLength++;
      this.spawnFood();
    }
  }

  void spawnFood() {
    int index = (int)this.mainClass.random(0, this.gameTiles.length);
    GameTile tile = this.gameTiles[index];
    if (tile.occupied) {
      // darn! we got a tile which we will never be able to reach.
      // Better place no food and instead try it again
      this.spawnFood();
      return;
    }
    tile.hasFood = true;
  }

  void respawn() {
    this.respawn(true);
  }

  void respawn(boolean withWalls) {
    int i = 0;
    this.snakeLength = 0;
    for (GameTile tile : this.gameTiles) {
      int rightBorder = this.width - 1;
      int bottomBorder = this.height - 1;
      // initialize all with free blocks
      tile.occupied = false;
      tile.hasFood = false;
      // setup walls
      if (tile.x == 0 || tile.y == 0 || tile.x == rightBorder || tile.y == bottomBorder) {
        tile.occupied = true;
      }
      // startposition of snake in the middle of the world
      if (tile.x > this.width / 2 - INITIAL_SNAKE_LENGTH - 1 && tile.x < this.width / 2 && tile.y == this.height / 2) {
        tile.occupied = true;
        this.snakeLength++;
        tile.occupiedCounter = snakeLength;
        // reached the head of the snake; save this position
        if (snakeLength == INITIAL_SNAKE_LENGTH) {
          this.snakeX = tile.x;
          this.snakeY = tile.y;
          this.snakeDirection = RIGHT;
        }
        i++;
      }
    }
    this.spawnFood();
  }
}
