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
  ArrayList<Node> finalPath;
  boolean pathFound;
  int targetX;
  int targetY;
  Node nextNode;

  AutoSolver(Snake mainClass, GameWorld gameWorld) {
    this.gameWorld = gameWorld;
    this.mainClass = mainClass;
    this.openList = new ArrayList<Node>();
    this.closedList = new ArrayList<Node>();
    this.finalPath = new ArrayList<Node>();
    this.pathFound = false;
  }

  void calculatePath() {
    if (this.nextNode != null) {
      return;
    }
    this.openList = new ArrayList<Node>();
    this.closedList = new ArrayList<Node>();
    this.finalPath = new ArrayList<Node>();
    int x = this.gameWorld.snakeX;
    int y = this.gameWorld.snakeY;
    int startTileId = this.getTileId(x, y);
    // search for tile with food to set it as target
    for (GameTile tile : this.gameWorld.gameTiles) {
      if (tile.hasFood) {
        this.targetX = tile.x;
        this.targetY = tile.y;
        break;
      }
    }
    Node startNode = new Node(startTileId);
    this.openList.add(startNode);
    this.nextNode = startNode;
  }

  void checkNode(Node startNode) {
    int[] coordinates = getTileCoordinates(startNode.tileId);
    int x = coordinates[0];
    int y = coordinates[1];
    for (int tileId : this.getNeighbourTileIds (x, y)) {
      // check if neighbour tile is already in the open list
      boolean alreadyInOpenList = false;
      for (Node node : this.openList) {
        if (node.tileId == tileId) {
          alreadyInOpenList = true;
          int previousGCost = node.getGCost();
          Node previousParent = node.parent;
          node.parent = startNode;
          int newGCost = node.getGCost();
          node.parent = newGCost < previousGCost ? startNode : previousParent;
        }
      }
      boolean alreadyInClosedList = false;
      for (Node node : this.closedList) {
        if (node.tileId == tileId) {
          alreadyInClosedList = true;
        }
      }
      if (!alreadyInOpenList && !alreadyInClosedList) {
        Node newNode = new Node(tileId);
        this.openList.add(newNode);
        newNode.parent = startNode;
        newNode.hCost = this.calcHCost(x, y);
      }
    }
    // move node from the open list to the closed list
    for (int i = this.openList.size () - 1; i>=0; i--) {
      Node node = this.openList.get(i);
      if (node == startNode) {
        this.openList.remove(i);
      }
    }
    this.closedList.add(startNode);
    // check if this is the target node
    if (x == targetX && y == targetY) {
      this.nextNode = null;
      this.gameWorld.gamePaused = false;
      this.generateFinalPath(startNode);
      this.pathFound = true;
      return;
    }
    // else, continue with the node with the least F cost
    if (this.openList.size() > 0) {
      this.nextNode = this.openList.get(0);
      for (Node node : this.openList) {
        this.nextNode = node.getFCost() < nextNode.getFCost() ? node : nextNode;
      }
      // usually, this is recursive. For visualization purposes, we just remember the next node.
      //this.checkNode(nextNode);
    }
  }

  int getTileId(int x, int y) {
    return x + y * this.gameWorld.width;
  }

  int[] getTileCoordinates(int id) {
    int x = id % this.gameWorld.width;
    int y = id / this.gameWorld.width;
    return new int[] {
      x, y
    };
  }

  int[] getNeighbourTileIds(int x, int y) {
    int[] potentialNeighbours = new int[] {
      -1, -1, -1, -1
    };
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
  
  void generateFinalPath(Node node) {
    this.finalPath.add(node);
    if (node.parent != null) {
      generateFinalPath(node.parent);
    }
  }

  int calcHCost(int x, int y) {
    int distance = this.mainClass.abs(this.targetX - x) + this.mainClass.abs(this.targetY - y);
    return distance;
  }

  void draw() {
    if (nextNode != null) {
      this.checkNode(nextNode);
    }
    
    this.mainClass.strokeWeight(0);
    this.mainClass.fill(0xaa00ff00);
    for (Node node : this.openList) {
      int[] coordinates = this.getTileCoordinates(node.tileId);
      int x = coordinates[0] * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
      int y = coordinates[1] * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
      this.mainClass.ellipse(x, y, 5, 5);
    }
    this.mainClass.fill(0xaaff0000);
    for (Node node : this.closedList) {
      int[] coordinates = this.getTileCoordinates(node.tileId);
      int x = coordinates[0] * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
      int y = coordinates[1] * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
      this.mainClass.ellipse(x, y, 5, 5);
    }
    this.mainClass.fill(0xffffff00);
    for (Node node : this.finalPath) {
      int[] coordinates = this.getTileCoordinates(node.tileId);
      int x = coordinates[0] * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
      int y = coordinates[1] * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
      this.mainClass.ellipse(x, y, 5, 5);
    }
  }

  void tick() {
    if (!this.pathFound) {
      this.gameWorld.gamePaused = true;
      this.calculatePath();
    } else {
      this.gameWorld.gamePaused = false;
    }
  }
}
