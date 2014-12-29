/**
 * This class is implementing A* to play the game automatically.
 * It is a bit helpless when it comes to save as much space as possible
 * to maneuver out a dead end. Any idea how this could be handled?
 */
import java.util.ArrayList;

class AutoSolver {

  GameWorld gameWorld;
  Snake mainClass;
  ArrayList<Node> openList;
  ArrayList<Node> closedList;
  ArrayList<Node> finalPath;
  boolean pathFound;
  boolean goodLuck;
  int targetX;
  int targetY;
  Node nextNode;
  boolean visualize;

  AutoSolver(Snake mainClass, GameWorld gameWorld) {
    this.gameWorld = gameWorld;
    this.mainClass = mainClass;
    this.openList = new ArrayList<Node>();
    this.closedList = new ArrayList<Node>();
    this.finalPath = new ArrayList<Node>();
    this.pathFound = false;
    this.visualize = false;
    // in case we cant find a path
    this.goodLuck = false;
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
    boolean foodFound = false;
    for (GameTile tile : this.gameWorld.gameTiles) {
      if (tile.hasFood) {
        foodFound = true;
        this.targetX = tile.x;
        this.targetY = tile.y;
        break;
      }
    }
    if (!foodFound) {
      return;
    }
    Node startNode = new Node(this.mainClass, startTileId);
    this.openList.add(startNode);
    this.nextNode = startNode;
  }

  void checkNode(Node startNode) {
    int[] coordinates = getTileCoordinates(startNode.tileId);
    int x = coordinates[0];
    int y = coordinates[1];
    for (Node neighbourNode : this.getNeighbourNodes (x, y, startNode)) {
      // check if the node is already in the open list
      boolean alreadyInOpenList = false;
      for (Node node : this.openList) {
        if (node.tileId == neighbourNode.tileId) {
          alreadyInOpenList = true;
          // if we get faster to this node using the new one, change the parent accordingly...
          int previousGCost = node.getGCost();
          Node previousParent = node.parent;
          node.parent = startNode;
          int newGCost = node.getGCost();
          // but keep in mind that some nodes are only reachable after walking a certain distance (disappearing snake)
          if (node.getNumberOfParents() > neighbourNode.minimumDistance) {
            node.parent = newGCost < previousGCost ? startNode : previousParent;
          } else {
            node.parent = previousParent;
          }
        }
      }
      boolean alreadyInClosedList = false;
      for (Node node : this.closedList) {
        if (node.tileId == neighbourNode.tileId) {
          alreadyInClosedList = true;
        }
      }
      if (!alreadyInOpenList && !alreadyInClosedList) {
        this.openList.add(neighbourNode);
        neighbourNode.parent = startNode;
        neighbourNode.hCost = this.calcHCost(x, y);
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
      // usually, this is recursive. For visualization purposes, we skip it and just remember the next node.
      if (!this.visualize) {
        this.checkNode(nextNode);
      }
    } else {
      // okay, we didn't find a path, but there is nothing in the open list
      // the food must be hidden behind us. Still calculate the path to the last node we found,
      // but immediately try to find a new path after going a step
      this.nextNode = null;
      this.generateFinalPath(startNode);
      this.goodLuck = true;
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

  Node[] getNeighbourNodes(int x, int y, Node startNode) {
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
      if (n > -1) {
        GameTile gameTile = this.gameWorld.gameTiles[n];
        boolean willBeOccupied = gameTile.occupied;
        if (gameTile.occupied) {
          if (gameTile.occupiedCounter != -1 && startNode.getNumberOfParents() > gameTile.occupiedCounter) {
            willBeOccupied = false;
          }
        }
        if (!willBeOccupied) {
          totalNeighbours++;
        }
      }
    }
    Node[] neighbourNodes = new Node[totalNeighbours];
    int i = 0;
    for (int n : potentialNeighbours) {
      if (n > -1) {
        GameTile gameTile = this.gameWorld.gameTiles[n];
        boolean willBeOccupied = gameTile.occupied;
        if (gameTile.occupied) {
          if (gameTile.occupiedCounter != -1) {
            // the field may be occupied at time checking, but since we need to move there,
            // it might be free until we get there
            if (startNode.getNumberOfParents() > gameTile.occupiedCounter) {
              willBeOccupied = false;
            } else {
              // it will not be free, but it might be free if we take a slightly longer path,
              // and still get a shorter total path to the food
            }
          }
        }
        if (!willBeOccupied) {
          neighbourNodes[i] = new Node(this.mainClass, n);
          i++;
        }
      }
    }
    return neighbourNodes;
  }

  void generateFinalPath(Node node) {
    this.finalPath.add(node);
    if (node.parent != null) {
      generateFinalPath(node.parent);
    }
  }

  float calcHCost(int x, int y) {
    int a = this.targetX - x;
    int b = this.targetY - y;
    float distance = this.mainClass.sqrt(a*a + b*b);
    return distance;
  }

  void draw() {
    if (nextNode != null) {
      this.checkNode(nextNode);
    }
    for (Node node : this.openList) {
      node.draw(0xaa00ff00);
    }
    for (Node node : this.closedList) {
      node.draw(0xaaff0000);
    }
    for (Node node : this.finalPath) {
      node.draw(0xffffff00);
    }
    this.mainClass.textAlign(this.mainClass.LEFT, this.mainClass.BOTTOM);
    this.mainClass.textSize(10);
    String text = "Press [v] to toggle visualization, [i] to toggle interactive mode";
    float width = this.mainClass.textWidth(text);
    this.mainClass.fill(0xffffffff);
    this.mainClass.rect(5, this.mainClass.height - GameTile.TILE_SIZE, width, GameTile.TILE_SIZE);
    this.mainClass.fill(0xff000000);
    this.mainClass.text(text, 5, this.mainClass.height);
  }

  void tick() {
    if (!this.pathFound && !this.goodLuck) {
      this.gameWorld.gamePaused = true;
      this.calculatePath();
    } else {
      this.gameWorld.gamePaused = false;
      int lastElement = this.finalPath.size() - 1;
      if (lastElement == 1) {
        this.pathFound = false;
      }
      if (this.finalPath.size() == 1) {
        // we end up in a dead end. Just give up, theres nothing more to do
        this.goodLuck = false;
        this.finalPath = new ArrayList<Node>();
        return;
      }
      Node fromNode = this.finalPath.get(lastElement);
      Node toNode = this.finalPath.get(lastElement-1);
      int[] coordinates = this.getTileCoordinates(fromNode.tileId);
      int fromX = coordinates[0];
      int fromY = coordinates[1];
      coordinates = this.getTileCoordinates(toNode.tileId);
      int toX = coordinates[0];
      int toY = coordinates[1];
      if (toX > fromX) {
        this.gameWorld.snakeDirection = this.gameWorld.RIGHT;
      } else if (toX < fromX) {
        this.gameWorld.snakeDirection = this.gameWorld.LEFT;
      } else if (toY > fromY) {
        this.gameWorld.snakeDirection = this.gameWorld.DOWN;
      } else if (toY < fromY) {
        this.gameWorld.snakeDirection = this.gameWorld.UP;
      }
      this.finalPath.remove(lastElement);
      if (this.goodLuck) {
        this.goodLuck = false;
        this.finalPath = new ArrayList<Node>();
      }
    }
  }
}
