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
  ArrayList<Node> potentialAlternativesList;
  boolean pathFound;
  boolean goodLuck;
  boolean visualizationPaused;
  boolean generateFinalPath;
  int targetX;
  int targetY;
  Node nextNode;
  Node nextPathNode;
  boolean visualize;

  AutoSolver(Snake mainClass, GameWorld gameWorld) {
    this.gameWorld = gameWorld;
    this.mainClass = mainClass;
    this.openList = new ArrayList<Node>();
    this.closedList = new ArrayList<Node>();
    this.finalPath = new ArrayList<Node>();
    this.potentialAlternativesList = new ArrayList<Node>();
    this.pathFound = false;
    this.visualize = false;
    this.visualizationPaused = false;
    this.generateFinalPath = false;
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
    this.potentialAlternativesList = new ArrayList<Node>();
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
    if (startNode == null) {
      return;
    }
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
      // we found a path, but we might be faster taking an alternative route
      //this.checkAlternativePath();
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
      // the food must be hidden behind us.
      // try to find an alternative path by making the path longer and therefore escape
      this.nextNode = null;
      this.checkAlternativePath();
    }
  }

  void checkAlternativePath() {
    // find the alternative node which will be the first one not beeing occupied
    if (this.potentialAlternativesList.size() > 0) {
      Node alternativeNode = this.potentialAlternativesList.get(0);
      for (Node node : this.potentialAlternativesList) {
        alternativeNode = node.minimumDistance < alternativeNode.minimumDistance ? node : alternativeNode; 
      }
      // find a path to the alternative node.
      // we need to change the path so that it gets long enough when reaching this node
      this.generateFinalPath(alternativeNode);
      this.pathFound = true;
    }
    // seems we have no chance.
    // Still calculate the path to the last node we checked,
    // but immediately try to find a new path after going a step
    if (!this.pathFound) {
      Node luckyNode = this.closedList.get(this.closedList.size() - 1);
      this.generateFinalPath(luckyNode);
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
              Node potentialNode = new Node(this.mainClass, n);
              boolean alreadyInList = false;
              for (Node node : this.potentialAlternativesList) {
                if (node.tileId == potentialNode.tileId) {
                  alreadyInList = true;
                  break;
                }
              }
              if (!alreadyInList) {
                this.potentialAlternativesList.add(potentialNode);
                potentialNode.parent = startNode;
              }
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
      // recursivly if no need of visualization, else, save the member and continue next draw() call
      if (!this.visualize) {
        this.generateFinalPath = false;
        generateFinalPath(node.parent);
      } else {
        this.generateFinalPath = true;
        this.nextPathNode = node.parent;
      }
    } else {
      this.generateFinalPath = false;
    }
  }

  float calcHCost(int x, int y) {
    int a = this.targetX - x;
    int b = this.targetY - y;
    float distance = this.mainClass.sqrt(a*a + b*b);
    return distance;
  }
  
  void nextVisualization() {
    if (!this.visualizationPaused) {
      return;
    }
    if (!this.pathFound) {
      this.checkNode(this.nextNode);
    }
    if (this.generateFinalPath) {
      this.generateFinalPath(this.nextPathNode);
    }
  }

  void draw() {
    if (!this.visualizationPaused && this.generateFinalPath) {
      this.generateFinalPath(nextPathNode);
    }
    for (Node node : this.openList) {
      node.draw(0xaa00ff00);
    }
    for (Node node : this.closedList) {
      node.draw(0xaaff0000);
    }
    for (Node node : this.potentialAlternativesList) {
      node.draw(0xaa0000ff);
    }
    for (Node node : this.finalPath) {
      node.draw(0xffffff00);
    }
    if (nextNode != null) {
      int[] coordinates = getTileCoordinates(nextNode.tileId);
      int x = coordinates[0] * GameTile.TILE_SIZE;
      int y = coordinates[1] * GameTile.TILE_SIZE;
      this.mainClass.fill(0xffffffff);
      this.mainClass.rect(x, y, GameTile.TILE_SIZE, GameTile.TILE_SIZE);
      nextNode.draw(0xff000000);
      if (!this.visualize || !this.visualizationPaused) {
        this.checkNode(nextNode);
      }
    }
    this.mainClass.textAlign(this.mainClass.LEFT, this.mainClass.BOTTOM);
    this.mainClass.textSize(10);
    String text = "Press [v] to toggle visualization, [i] to toggle interactive mode, [ ] to pause visualization, [n] to step through visualization";
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
    } else if (this.generateFinalPath) {
      this.gameWorld.gamePaused = true;
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
