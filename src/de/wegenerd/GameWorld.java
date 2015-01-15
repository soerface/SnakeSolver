package de.wegenerd;
import java.util.ArrayList;

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
  boolean spawnFood;
  Processing processing;
  static final int UP = 0;
  static final int RIGHT = 1;
  static final int DOWN = 2;
  static final int LEFT = 3;
  static final int INITIAL_SNAKE_LENGTH = 5;
  ArrayList<Integer> foodPositions;
  static final int[] testFoodPositions = new int[] {231, 410, 169, 507, 95, 295, 175, 214, 365, 213, 298, 545, 191, 260, 172, 144, 433, 76, 184, 497, 514, 531, 446, 39, 309, 311, 313, 253, 251, 348, 353, 143, 137, 167, 170, 401, 130, 135, 223, 469, 446, 235, 321, 318, 408, 463, 74, 114, 382, 411, 408, 416, 431, 220, 289, 511, 200, 154, 33, 483, 565, 214, 217, 80, 530, 216, 422, 413, 543, 220, 77, 177, 567, 41, 184, 285, 436, 88, 309, 301, 286, 469, 44, 435, 173, 567, 504, 503, 248, 69, 257, 170, 378, 46, 327, 422, 62, 304, 155, 66, 109, 548, 499, 296, 529, 410, 476, 343, 139, 454, 220, 415, 422, 290, 76, 435, 467, 263, 111, 376, 558, 548, 538, 276, 108, 398, 472, 358, 268, 57, 98, 32, 443, 85, 337, 530, 466, 227, 354, 543, 223, 83, 273, 260, 532, 167, 353, 361, 313, 466, 438, 195, 283, 112, 170, 280, 244, 399, 292, 162, 531, 280, 525, 175, 159, 549, 79, 502, 335, 163, 43, 47, 142, 470, 460, 183, 39, 385, 499, 156, 306, 484, 538, 53, 457, 121, 372, 250, 323, 393, 64, 80, 107, 349, 245, 37, 550, 295, 96, 72, 476, 51, 66, 351, 100, 426, 191, 68, 118, 506, 478, 393, 405, 176, 512, 412, 147, 167, 370, 151, 226, 334, 134, 382, 177, 492, 439, 306, 56, 52, 436, 85, 374, 545, 568, 285, 247, 489, 391, 498, 265, 380, 87, 334, 244, 251, 64, 392, 227, 189, 425, 58, 387, 340, 464, 456, 31, 395, 380, 400, 337, 478, 264, 397, 215, 108, 200, 310, 567, 227, 109, 126, 538, 503, 425, 61, 243, 115, 79, 71, 247, 129, 215, 79, 147, 122, 350, 500, 276, 70, 346, 143, 376, 190, 192, 272, 110, 128};
  int testFoodCounter;

  GameWorld(Processing processing) {
    this.processing = processing;
    this.gameStarted = false;
    this.gamePaused = false;
    this.spawnFood = true;
    this.foodPositions = new ArrayList<Integer>();
    int width = processing.width;
    int height = processing.height;
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
      this.processing.textAlign(this.processing.CENTER);
      int x = this.processing.width / 2;
      int y = this.processing.height / 2;
      this.processing.fill(0xffffffff);
      this.processing.textSize(18);
      this.processing.text("Press [s] to start the game\n", x, y);
      this.processing.textSize(14);
      this.processing.text(
              "\n[a] to let the computer play automatically" +
                      "\n[v] to let the computer play with visualization" +
                      "\n[i] computer, set food interactively with click", x, y);
      return;
    }
    for (GameTile tile : this.gameTiles) {
      tile.draw(processing);
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
  
  void spawnFood(int x, int y) {
    int tileId = x % this.width + y * this.width;
    this.spawnFood(tileId);
  }

  void spawnFood(int tileId) {
    GameTile tile = this.gameTiles[tileId];
    if (!tile.occupied) {
      tile.hasFood = true;
      this.foodPositions.add(tileId);
    }
  }

  void spawnFood() {
    if (!this.spawnFood) {
      return;
    }
    int index;
    if (this.testFoodCounter >= this.testFoodPositions.length) {
      index = (int)this.processing.random(0, this.gameTiles.length);
    } else {
      index = this.testFoodPositions[this.testFoodCounter];
      this.processing.print(this.testFoodCounter + "\n");
      this.testFoodCounter++;
    }
    GameTile tile = this.gameTiles[index];
    if (tile.occupied) {
      // darn! we got a tile which we will never be able to reach.
      // Better place no food and instead try it again
      this.spawnFood();
      return;
    }
    tile.hasFood = true;
    this.foodPositions.add(index);
  }

  void respawn() {
    this.respawn(true);
  }

  void respawn(boolean withWalls) {
    this.processing.print(this.foodPositions + "\n");
    this.testFoodCounter = 0;
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
    this.foodPositions = new ArrayList<Integer>();
    this.spawnFood();
  }
}
