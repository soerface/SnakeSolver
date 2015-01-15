package de.wegenerd;

import processing.core.PApplet;

public class Processing extends PApplet {
    final static boolean DEBUG = false;

    GameWorld gameWorld;
    AutoSolver autoSolver;
    int frames = 0;
    static final int TICKS_PER_FRAME = 1; // decrease this number to speedup the game
    static final int BOARD_HORIZONTAL_SIZE = 30;
    static final int BOARD_VERTICAL_SIZE = 20;

    @Override
    public void setup() {
        size(BOARD_HORIZONTAL_SIZE * GameTile.TILE_SIZE, BOARD_VERTICAL_SIZE * GameTile.TILE_SIZE);
        this.gameWorld = new GameWorld(this);
    }

    @Override
    public void draw() {
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

    @Override
    public void keyPressed() {
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
            if (key == 'v' || key == 'V') {
                this.autoSolver.visualize = !this.autoSolver.visualize;
            }
            if (key == 'i' || key == 'I') {
                this.gameWorld.spawnFood = !this.gameWorld.spawnFood;
                if (this.gameWorld.spawnFood) {
                    // disabling interactive mode; spawn food if there is none.
                    for (GameTile tile : this.gameWorld.gameTiles) {
                        if (tile.hasFood) {
                            return;
                        }
                    }
                    this.gameWorld.spawnFood();
                }
            }
            if (key == ' ') {
                this.autoSolver.visualizationPaused = !this.autoSolver.visualizationPaused;
            }
            if ((key == 'n' || key == 'N')) {
                this.autoSolver.nextVisualization();
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

    @Override
    public void mouseClicked() {
        if (!this.gameWorld.spawnFood) {
            int x = mouseX / GameTile.TILE_SIZE;
            int y = mouseY / GameTile.TILE_SIZE;
            this.gameWorld.spawnFood(x, y);
        }
    }

}
