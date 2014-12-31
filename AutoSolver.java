/**
 * This class is implementing a modified version of A* to play the game automatically.
 *
 * It contains nothing of the snake gamelogic, so you dont need to read it
 * if you are just checking my homework.
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
  boolean continueGenerateAlternativePath;
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
    this.continueGenerateAlternativePath = false;
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
    for (Node neighbourNode : this.getNeighbourNodes (startNode)) {
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
      //this.generateAlternativePath();
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
      this.generateAlternativePath();
    }
  }

  void generateAlternativePath() {
    // find the alternative node which will be the first one not beeing occupied
    if (this.potentialAlternativesList.size() > 0) {
      if (!this.continueGenerateAlternativePath) {
        Node alternativeNode = null;
        for (Node node : this.potentialAlternativesList) {
          if (checkPathLength(node) <= 3) {
            // if we are too close to this node, we dont have a chance to make our path larger
            // so skip it and try another one
            continue;
          }
          if (alternativeNode == null) {
            alternativeNode = node;
          } else {
            alternativeNode = node.minimumDistance < alternativeNode.minimumDistance ? node : alternativeNode;
          }
        }
        // find a path to the alternative node.
        this.generateFinalPath(alternativeNode);
        // member to make it work when not doing it recursivley:
        if (this.visualize) {
          this.continueGenerateAlternativePath = true;
          return;
        }
      }
      // we need to change the path so that it gets long enough when reaching this node
      this.increasePathLength();
      /*
      if (this.finalPath.size() >= alternativeNode.minimumDistance) {
       this.pathFound = true;
       }*/
    }
  }

  boolean changeNode(Node node, ArrayList<Node> alreadyChangedNodes, ArrayList<Node> originalParents) {
    // findAlternativeNodes is calling getNeighbourNodes which is using getNumberOfParents().
    // therefore, we may not produce an infinite path, so reset original parents
    int size = alreadyChangedNodes.size();
    Node[] modifiedParents = new Node[size];
    for (int i=0; i<size; i++) {
      modifiedParents[i] = alreadyChangedNodes.get(i).parent;
      alreadyChangedNodes.get(i).parent = originalParents.get(i);
    }
    ArrayList<Node> alternativeNodes = this.findAlternativeNodes(node);
    for (int i=0; i<size; i++) {
      alreadyChangedNodes.get(i).parent = modifiedParents[i];
    }
    alreadyChangedNodes.add(node);
    Node originalParent = node.parent;
    originalParents.add(node.parent);
    boolean skip = false;
    for (Node alternativeNode : alternativeNodes) {
      for (Node prevNodes : alreadyChangedNodes) {
        if (alternativeNode.tileId == prevNodes.tileId) {
          skip = true;
        }
      }
      if (skip) {
        skip = false;
        continue;
      }
      node.parent = alternativeNode;
      if (!checkPathValidity(this.finalPath.get(0))) {
        if (changeNode(alternativeNode, alreadyChangedNodes, originalParents)) {
          return true;
        }
      } else if (checkPathLength(this.finalPath.get(0)) > this.finalPath.size()) {
        return true;
      }
    }
    node.parent = originalParent;
    return false;
  }

  boolean changeNode(Node node) {
    ArrayList<Node> alreadyChangedNodes = new ArrayList<Node>();
    ArrayList<Node> originalParents = new ArrayList<Node>();
    alreadyChangedNodes.add(node);
    originalParents.add(node.parent);
    return changeNode(node, alreadyChangedNodes, originalParents);
  }

  void increasePathLength() {
    boolean pathChanged = false;
    if (this.finalPath.size() < 1) {
      // not enough space to navigate
      this.continueGenerateAlternativePath = false;
      return;
    }
    for (int nodeId = 1; nodeId<this.finalPath.size (); nodeId++) {
      Node node = this.finalPath.get(nodeId);
      pathChanged = changeNode(node);
      if (pathChanged) {
        break;
      }
    }
    Node targetNode = this.finalPath.get(0);
    this.finalPath = new ArrayList<Node>();
    this.pathFound = true;
    this.generateFinalPath(targetNode);
    if (pathChanged) {
      // our path is now longer. is it long enough?
      if (checkPathLength(targetNode) > targetNode.minimumDistance) {
        this.continueGenerateAlternativePath = false;
      } else {
        this.continueGenerateAlternativePath = true;
        if (!this.visualize) {
          this.increasePathLength();
        }
      }
    } else {
      this.continueGenerateAlternativePath = false;
    }
  }

  boolean checkPathValidity(Node node) {
    ArrayList<Node> nodeList = new ArrayList<Node>();
    nodeList.add(node);
    return checkPathValidity(nodeList);
  }

  boolean checkPathValidity(ArrayList<Node> nodeList) {
    Node lastNode = nodeList.get(nodeList.size()-1);
    if (lastNode.parent == null) {
      return true;
    }
    for (Node node : nodeList) {
      // this node is already in the list. This would lead to an infinite path, so it is invalid
      if (node.tileId == lastNode.parent.tileId) {
        return false;
      }
    }
    nodeList.add(lastNode.parent);
    return checkPathValidity(nodeList);
  }

  int checkPathLength(Node node) {
    if (node.parent == null) {
      return 1;
    }
    return checkPathLength(node.parent) + 1;
  }

  ArrayList<Node> findAlternativeNodes(Node targetNode) {
    ArrayList<Node> alternativeNodes = new ArrayList<Node>();
    if (targetNode.parent == null) {
      // doesnt make sense to change the starting node (snake head)
      return alternativeNodes;
    }
    // is there another node in the closed list around us besides the one that is the parent?
    Node[] neighbourNodes = this.getNeighbourNodes(targetNode);
    int i = 0;
    for (Node node : this.closedList) {
      for (Node neighbourNode : neighbourNodes) {
        if (node.tileId == neighbourNode.tileId && node != targetNode.parent) {
          alternativeNodes.add(node);
        }
      }
    }
    return alternativeNodes;
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

  Node[] getNeighbourNodes(Node startNode) {
    int[] coordinates = getTileCoordinates(startNode.tileId);
    int x = coordinates[0];
    int y = coordinates[1];
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
                potentialNode.minimumDistance = gameTile.occupiedCounter;
              }
            }
          }
        }
        if (!willBeOccupied) {
          neighbourNodes[i] = new Node(this.mainClass, n);
          neighbourNodes[i].minimumDistance = gameTile.occupiedCounter;
          i++;
        }
      }
    }
    return neighbourNodes;
  }

  void generateFinalPath(Node node) {
    for (Node previousNode : this.finalPath) {
      if (node.tileId == previousNode.tileId) {
        this.mainClass.print("Invalid path!\n");
        this.generateFinalPath = false;
        return;
      }
    }
    this.finalPath.add(node);
    if (node.parent != null) {
      // recursivly if no need of visualization, else, save the member and continue on next draw() call
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
    if (this.nextNode != null) {
      this.checkNode(this.nextNode);
    }
    if (this.generateFinalPath) {
      this.generateFinalPath(this.nextPathNode);
    } else if (this.continueGenerateAlternativePath) {
      this.generateAlternativePath();
    }
  }

  void draw() {
    if (!this.visualizationPaused) {
      if (this.generateFinalPath) {
        this.generateFinalPath(nextPathNode);
      } else if (this.continueGenerateAlternativePath) {
        this.generateAlternativePath();
      }
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
    this.mainClass.strokeWeight(0);
    this.mainClass.rect(1, this.mainClass.height - GameTile.TILE_SIZE, width, GameTile.TILE_SIZE);
    this.mainClass.fill(0xff000000);
    this.mainClass.text(text, 5, this.mainClass.height);
  }

  void tick() {
    if (!this.pathFound && !this.goodLuck && !this.continueGenerateAlternativePath && !this.generateFinalPath) {
      this.gameWorld.gamePaused = true;
      this.calculatePath();
    } else if (this.generateFinalPath || this.continueGenerateAlternativePath) {
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
