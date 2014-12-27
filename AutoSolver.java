/**
 * This class is implementing A* to play the game automatically.
 * It is not taking into account that the snake is moving and food may be blocked for a while.
 * I would appreciate feedback for optimizing the algorithm for this usecase.
 */
import java.util.ArrayList;

class AutoSolver {
  
  GameWorld gameWorld;
  Snake mainClass;
  ArrayList<Node> openList;
  ArrayList<Node> closedList;
  int targetX;
  int targetY;

  AutoSolver(Snake mainClass, GameWorld gameWorld) {
    this.gameWorld = gameWorld;
    this.mainClass = mainClass;
  }
  
  void calculatePath() {
    this.openList = new ArrayList<Node>();
    this.closedList = new ArrayList<Node>();
    
    int x = this.gameWorld.snakeX;
    int y = this.gameWorld.snakeY;
    int startTileId = this.getTileId(x, y);
    Node startNode = new Node(startTileId);
    this.openList.add(startNode);
    for(int tileId : this.getNeighbourTileIds(x, y)) {
      Node node = new Node(tileId);
      this.openList.add(node);
      node.parent = startNode;
      GameTile tile = this.gameWorld.gameTiles[tileId];
      node.hCost = this.calcHCost(tile.x, tile.y);
    }
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

  int calcHCost(int x, int y) {
    int distance = this.mainClass.abs(this.targetX - x) + this.mainClass.abs(this.targetY - y);
    return distance;
  }

  void draw() {
  }

  void tick() {
    this.calculatePath();
  }
}
