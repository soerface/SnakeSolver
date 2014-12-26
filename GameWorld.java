class GameWorld { //<>// //<>//

  GameTile[] gameTiles;
  int width;
  int height;
  int snakeX;
  int snakeY;
  int snakeDirection;
  int snakeLength;
  static final int UP = 0;
  static final int RIGHT = 1;
  static final int DOWN = 2;
  static final int LEFT = 3;
  static final int INITIAL_SNAKE_LENGTH = 5;

  GameWorld(int width, int height) {
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

  void draw(Snake mainClass) {
    for (GameTile tile : this.gameTiles) {
      tile.draw(mainClass);
    }
  }

  void tick() {
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
      this.snakeY--;
      break;
    }
    for (GameTile tile : this.gameTiles) {
      tile.tick();
      if (tile.x == this.snakeX && tile.y == this.snakeY) {
        tile.occupied = true;
        tile.occupiedCounter = this.snakeLength;
      }
    }
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
  }
}
