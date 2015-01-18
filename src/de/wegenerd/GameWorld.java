package de.wegenerd;

import processing.core.PApplet;
import processing.core.PConstants;

import java.util.ArrayList;

class GameWorld {

    GameTile[] gameTiles;
    static int width;
    static int height;
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
    static final int[] testFoodPositions = new int[]{231, 410, 169, 507, 95, 295, 214, 365, 298, 545, 191, 260, 172, 144, 433, 76, 184, 497, 514, 531, 446, 39, 311, 313, 253, 348, 353, 143, 137, 167, 170, 401, 130, 135, 469, 446, 235, 318, 408, 463, 74, 114, 382, 411, 416, 431, 220, 289, 511, 154, 33, 483, 565, 214, 217, 80, 530, 422, 413, 220, 77, 177, 567, 41, 184, 285, 436, 309, 301, 469, 44, 173, 567, 504, 503, 248, 69, 170, 378, 46, 327, 422, 62, 304, 155, 66, 109, 548, 499, 476, 343, 139, 454, 220, 415, 290, 76, 435, 467, 263, 558, 548, 538, 276, 57, 98, 32, 443, 85, 337, 530, 227, 543, 83, 260, 532, 361, 313, 466, 112, 280, 525, 175, 159, 335, 470, 385, 306, 484, 538, 53, 250, 64, 80, 107, 349, 37, 295, 66, 426, 393, 405, 176, 412, 151, 334, 492, 436, 85, 374, 568, 247, 498, 265, 87, 244, 392, 227, 189, 425, 387, 340, 31, 400, 478, 264, 108, 126, 243, 79, 350, 500, 143, 190, 542, 83, 568, 489, 305, 475, 199, 154, 278, 247, 438, 274, 166, 409, 152, 305, 83, 109, 199, 126, 112, 469, 68, 438, 517, 404, 466, 544, 454, 486, 284, 515, 77, 333, 122, 418, 96, 524, 464, 535, 529, 455, 568, 82, 462, 443, 134, 560, 323, 505, 151, 282, 104, 133, 436, 290, 316, 231, 482, 80, 549, 277, 85, 275, 246, 212, 190, 307, 355, 373, 118, 148, 487, 242, 305, 348, 265, 196, 52, 268, 244, 116, 142, 132, 224, 228, 425, 328, 506, 544, 426, 560, 357, 106, 296, 366, 107, 93, 486, 169, 517, 175, 436, 91, 285, 133, 161, 333, 100, 398, 568, 335, 410, 358, 166, 97, 426, 536, 234, 415, 222, 317, 475, 160, 519, 416, 198, 391, 301, 445, 33, 278, 86, 403, 174, 63, 362, 344, 98, 310, 403, 513, 106, 178, 454, 436, 85, 304, 483, 114, 348, 213, 79, 202, 105, 182, 50, 224, 292, 230, 462, 250, 428, 399, 220, 218, 161, 142, 133, 227, 77, 190};
    static int testFoodCounter;

    GameWorld(Processing processing) {
        this.processing = processing;
        this.gameStarted = false;
        this.gamePaused = false;
        this.spawnFood = true;
        this.foodPositions = new ArrayList<Integer>();
        int width = processing.width;
        int height = processing.height;
        GameWorld.width = width / GameTile.TILE_SIZE;
        GameWorld.height = height / GameTile.TILE_SIZE;
        int worldArea = width * height;
        int tileArea = GameTile.TILE_SIZE * GameTile.TILE_SIZE;
        int numberOfTiles = worldArea / tileArea;
        this.gameTiles = new GameTile[numberOfTiles];
        for (int i = 0; i < numberOfTiles; i++) {
            int x = i % GameWorld.width;
            int y = i / GameWorld.width;
            this.gameTiles[i] = new GameTile(x, y);
        }
        this.respawn();
    }

    void draw() {
        if (!this.gameStarted) {
            this.processing.textAlign(PConstants.CENTER);
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
        int prevSnakeX = this.snakeX;
        int prevSnakeY = this.snakeY;
        switch (this.snakeDirection) {
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
                int tileId = GameTile.getTileIdByCoordinates(prevSnakeX, prevSnakeY);
                tile.parent = this.gameTiles[tileId];
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

    static int getTileId(int x, int y) {
        if (x < 0 || y < 0 || x >= GameWorld.width || y >= GameWorld.height) {
            return -1;
        }
        return x + y * GameWorld.width;
    }

    void spawnFood(int x, int y) {
        int tileId = x % width + y * width;
        this.spawnFood(tileId);
    }

    void spawnFood(int tileId) {
        // do not spawn food if there is already some
        for (GameTile tile : this.gameTiles) {
            if (tile.hasFood) {
                return;
            }
        }
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
        if (testFoodCounter >= testFoodPositions.length) {
            index = (int) this.processing.random(0, this.gameTiles.length);
        } else {
            index = testFoodPositions[testFoodCounter];
            PApplet.print(testFoodCounter + "/" + testFoodPositions.length + "\n");
            testFoodCounter++;
            if (testFoodCounter == 350) {
                AutoSolver.ANIMATION_DELAY = 5;
                Processing.GAME_DELAY = 10;
            }
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
        PApplet.print(this.foodPositions + "\n");
        testFoodCounter = 0;
        this.snakeLength = 0;
        for (GameTile tile : this.gameTiles) {
            int rightBorder = width - 1;
            int bottomBorder = height - 1;
            // initialize all with free blocks
            tile.occupied = false;
            tile.hasFood = false;
            // setup walls
            if (tile.x == 0 || tile.y == 0 || tile.x == rightBorder || tile.y == bottomBorder) {
                tile.occupied = true;
            }
            // startposition of snake in the middle of the world
            if (tile.x > width / 2 - INITIAL_SNAKE_LENGTH - 1 && tile.x < width / 2 && tile.y == height / 2) {
                tile.occupied = true;
                tile.parent = this.gameTiles[tile.tileId - 1];
                this.snakeLength++;
                tile.occupiedCounter = snakeLength;
                // reached the head of the snake; save this position
                if (snakeLength == INITIAL_SNAKE_LENGTH) {
                    this.snakeX = tile.x;
                    this.snakeY = tile.y;
                    this.snakeDirection = RIGHT;
                }
            }
        }
        this.foodPositions = new ArrayList<Integer>();
        this.spawnFood();
    }
}
