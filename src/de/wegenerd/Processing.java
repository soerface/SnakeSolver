package de.wegenerd;

import processing.core.PApplet;

import static java.lang.Thread.sleep;

public class Processing extends PApplet {
    final static boolean DEBUG = false;

    public GameWorld gameWorld;
    AutoSolver autoSolver;
    static int GAME_DELAY = 50; // delay in milliseconds
    static final int BOARD_HORIZONTAL_SIZE = 30;
    static final int BOARD_VERTICAL_SIZE = 20;

    @Override
    public void setup() {
        size(BOARD_HORIZONTAL_SIZE * GameTile.TILE_SIZE, BOARD_VERTICAL_SIZE * GameTile.TILE_SIZE);
        this.gameWorld = new GameWorld(this);
        thread("tick");
    }

    public void tick() throws InterruptedException {
        while (true) {
            if (this.autoSolver != null) {
                this.autoSolver.tick();
            }
            this.gameWorld.tick();
            sleep(GAME_DELAY);
        }
    }

    @Override
    public void draw() {
        fill(0xff000000);
        rect(0, 0, width, height);
        this.gameWorld.draw();
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
            if (key == 'i' || key == 'I') {
                this.gameWorld.gameStarted = true;
                this.gameWorld.spawnFood = false;
                this.autoSolver = new AutoSolver(this, this.gameWorld);
            }
            return;
        }
        if (this.autoSolver != null) {
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
            return;
        }

        int pressedKey = key;
        // support for arrow keys
        if (key == CODED) {
            pressedKey = keyCode;
        }
        switch (pressedKey) {
            // regular controls
            case 'w':
            case 'W':
                // vim style controls
            case 'k':
            case 'K':
                // arrow keys
            case UP:
                if (this.gameWorld.snakeDirection != GameWorld.DOWN) {
                    this.gameWorld.snakeDirection = GameWorld.UP;
                }
                break;
            case 'a':
            case 'A':
            case 'h':
            case 'H':
            case LEFT:
                if (this.gameWorld.snakeDirection != GameWorld.RIGHT) {
                    this.gameWorld.snakeDirection = GameWorld.LEFT;
                }
                break;
            case 's':
            case 'S':
            case 'j':
            case 'J':
            case DOWN:
                if (this.gameWorld.snakeDirection != GameWorld.UP) {
                    this.gameWorld.snakeDirection = GameWorld.DOWN;
                }
                break;
            case 'd':
            case 'D':
            case 'l':
            case 'L':
            case RIGHT:
                if (this.gameWorld.snakeDirection != GameWorld.LEFT) {
                    this.gameWorld.snakeDirection = GameWorld.RIGHT;
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
