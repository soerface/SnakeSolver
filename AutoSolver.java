/**
 * This class is implementing A* to play the game automatically.
 * It is not taking into account that the snake is moving and food may be blocked for a while.
 * I would appreciate feedback for optimizing the algorithm for this usecase.
 */
import java.util.ArrayList;

class AutoSolver {
  
  GameWorld gameWorld;
  ArrayList<Node> openList;
  ArrayList<Node> closedList;

  AutoSolver(GameWorld gameWorld) {
    this.gameWorld = gameWorld;
  }
  
  void calculatePath() {
    this.openList = new ArrayList<Node>();
    this.closedList = new ArrayList<Node>();
    
    int x = this.gameWorld.snakeX;
    int y = this.gameWorld.snakeY;
    int tileId = this.getTileId(x, y);
    this.openList.add(new Node(tileId));
  }

  int getTileId(int x, int y) {
    return x + y * this.gameWorld.width;
  }
  
  void draw() {
  }

  void tick() {
    this.calculatePath();
  }
}
