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
    int neighborTileIds[] = this.getNeighbourTileIds(x, y);
  }

  int getTileId(int x, int y) {
    return x + y * this.gameWorld.width;
  }
  
  int[] getNeighbourTileIds(int x, int y) {
    int[] potentialNeighbours = new int[]{-1, -1, -1, -1};
    if (x > 0) {
      potentialNeighbours[0] = getTileId(x-1, y);
    }
    if (y > 0) {
      potentialNeighbours[1] = getTileId(x, y-1);
    }
    if (x < this.gameWorld.width) {
      potentialNeighbours[2] = getTileId(x+1, y);
    }
    if (y < this.gameWorld.height) {
      potentialNeighbours[3] = getTileId(x, y+1);
    }
    int totalNeighbours = 0;
    for (int n : potentialNeighbours) {
      if (n > -1 && !this.gameWorld.gameTiles[n].occupied) {
        totalNeighbours++;
      }
    }
    int[] neighbourTiles = new int[totalNeighbours];
    int i = 0;
    for (int n : potentialNeighbours) {
      if (n > -1 && !this.gameWorld.gameTiles[n].occupied) {
        neighbourTiles[i] = n;
        i++;
      }
    }
    return neighbourTiles;
  }

  void draw() {
  }

  void tick() {
    this.calculatePath();
  }
}
