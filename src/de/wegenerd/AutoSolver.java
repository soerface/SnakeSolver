/**
 * This class is implementing a modified version of A* to play the game automatically.
 * It needs refactoring, got pretty confusing in the meanwhile :/
 *
 * It contains nothing of the snake gamelogic, so you dont need to read it
 * if you are just checking my homework.
 */
package de.wegenerd;

import java.util.ArrayList;

class AutoSolver {

  GameWorld gameWorld;
  Processing processing;
  ArrayList<Node> openList;
  ArrayList<Node> closedList;
  ArrayList<Node> finalPath;
  ArrayList<Node> potentialAlternativesList;
  ArrayList<Integer> potentialAlternativesBlackList; // just contains tile ids
  ArrayList<Integer> punishedTiles;
  boolean pathFound;
  boolean goodLuck;
  boolean visualizationPaused;
  boolean generateFinalPath;
  boolean continueGenerateAlternativePath;
  int targetX;
  int targetY;
  Node nextNode;
  Node nextPathNode;
  Node alternativeNode;
  boolean visualize;

  AutoSolver(Processing processing, GameWorld gameWorld) {
    this.gameWorld = gameWorld;
    this.processing = processing;
    this.openList = new ArrayList<Node>();
    this.closedList = new ArrayList<Node>();
    this.finalPath = new ArrayList<Node>();
    this.potentialAlternativesList = new ArrayList<Node>();
    this.potentialAlternativesBlackList = new ArrayList<Integer>();
    this.punishedTiles = new ArrayList<Integer>();
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
    Node startNode = new Node(this.processing, startTileId);
    this.openList.add(startNode);
    this.nextNode = startNode;
  }

  void checkNode(Node startNode) {
    checkNode(startNode, false);
  }

  void checkNode(Node startNode, boolean forceRecursion) {
    if (startNode == null) {
      return;
    }
    int x = startNode.getX();
    int y = startNode.getY();
    for (Node neighbourNode : this.getNeighbourNodes (startNode, this.gameWorld.gameTiles, this.potentialAlternativesList, this.potentialAlternativesBlackList)) {
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
          if (node.getNumberOfParents() >= neighbourNode.minimumDistance) {
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
        neighbourNode.parent = startNode;
        neighbourNode.hCost = this.calcHCost(x, y);
        // do not add nodes which were punished too hard to avoid infinite searches.
        int maxGCost = this.processing.BOARD_HORIZONTAL_SIZE * this.processing.BOARD_VERTICAL_SIZE;
        if (neighbourNode.getGCost() < maxGCost) {
          this.openList.add(neighbourNode);
        }
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
      boolean isDeadEnd = this.checkForDeadEnd(startNode);

      if (isDeadEnd) {
        // dead end. Punish nodes on path to try an alternative
        this.finalPath = new ArrayList<Node>();
        this.generateFinalPath(startNode, true);
        for (Node node : this.finalPath) {
          this.punishedTiles.add(node.tileId);
        }
        this.closedList = new ArrayList<Node>();
        this.openList = new ArrayList<Node>();
        this.finalPath = new ArrayList<Node>();
        this.potentialAlternativesList = new ArrayList<Node>();
        this.potentialAlternativesBlackList = new ArrayList<Integer>();
        this.pathFound = false;
        this.checkNode(nextNode);
        return;
      } else {
        // found a path which does not lead into a dead end
        this.nextNode = null;
        this.finalPath = new ArrayList<Node>();
        this.generateFinalPath(startNode);
        this.pathFound = true;
        // we found a path, but we might be faster taking an alternative route
        this.potentialAlternativesBlackList = new ArrayList<Integer>();
        this.punishedTiles = new ArrayList<Integer>();
        //this.generateAlternativePath(); (not that important to find an even shorter path
        return;
      }
    }
    // else, continue with the node with the least F cost
    if (this.openList.size() > 0) {
      this.nextNode = this.openList.get(0);
      for (Node node : this.openList) {
        this.nextNode = node.getFCost() < nextNode.getFCost() ? node : nextNode;
      }
      // usually, this is recursive. For visualization purposes, we skip it and just remember the next node.
      if (!this.visualize || forceRecursion) {
        this.checkNode(nextNode, forceRecursion);
      }
    } else {
      // okay, we didn't find a path, but there is nothing in the open list
      // the food must be hidden behind us.
      // try to find an alternative path by making the path longer and therefore escape
      this.nextNode = null;
      this.generateAlternativePath();
    }
  }

  boolean checkForDeadEnd(Node startNode) {
    // we need to check if we are running into a dead end.
    // in order to do this, we just need to see if we could reach our tail after we arrive at the food
    // because if we can reach our tail we will be able to reach every other field, too
    // if we can not reach it, increase the length of our path, so the path before us becomes free.
    this.generateFinalPath(startNode, true); // force recursive calculation, we need the final path
    GameTile[] futureGameTiles = this.simulatePath(this.finalPath);
    int i = 0;
    int futureProcessingHeadTileId = 0;
    GameTile futureProcessingHeadTile = futureGameTiles[0];
    for (GameTile tile : futureGameTiles) {
      if (futureProcessingHeadTile.occupiedCounter < tile.occupiedCounter) {
        futureProcessingHeadTile =  tile;
        futureProcessingHeadTileId = i;
      }
      i++;
    }
    Node futureStartNode = new Node(this.processing, futureProcessingHeadTileId);
    ArrayList<Integer> blackList = new ArrayList<Integer>();
    ArrayList<Node> closedList = new ArrayList<Node>();
    ArrayList<Node> snakeNodes = this.findProcessingNodes(futureStartNode, futureGameTiles, blackList, closedList);
    // now that we have the nodes of the snake which are reachable, we just need to find a single
    // one which we will be able to reach when it becomes free. Because this is essentially our tail
    // its basically the same calculation as the generateAlternativePath() method does
    boolean isDeadEnd = true;
    while (isDeadEnd) {
      Node targetNode = null;
      for (Node node : snakeNodes) {
        if (targetNode == null) {
          targetNode = node;
        } else {
          targetNode = node.minimumDistance < targetNode.minimumDistance ? node : targetNode;
        }
      }
      if (targetNode == null) {
        // all nodes were checked, we cant find a path to the tail
        break;
      }
      for (i = snakeNodes.size () - 1; i>=0; i--) {
        Node node = snakeNodes.get(i);
        if (node == targetNode) {
          snakeNodes.remove(i);
        }
      }
      if (targetNode.getNumberOfParents() >= targetNode.minimumDistance) {
        isDeadEnd = false;
      } else {
        this.finalPath = new ArrayList<Node>();
        this.generateFinalPath(targetNode, true);
        if (this.increasePathLength(futureGameTiles, closedList, snakeNodes, blackList, true, 6)) {
          isDeadEnd = false;
        }
        blackList.add(targetNode.tileId);
        closedList = new ArrayList<Node>();
        snakeNodes = this.findProcessingNodes(futureStartNode, futureGameTiles, blackList, closedList);
      }
    }
    return isDeadEnd;
  }

  ArrayList<Node> findProcessingNodes(Node startNode, GameTile[] gameTiles, ArrayList<Integer> blackList, ArrayList<Node> closedList) {
    ArrayList<Node> openList = new ArrayList<Node>();
    ArrayList<Node> snakeNodes = new ArrayList<Node>();
    openList.add(startNode);
    this.findProcessingNodes(gameTiles, openList, closedList, blackList, snakeNodes);
    return snakeNodes;
  }

  void findProcessingNodes(GameTile[] gameTiles, ArrayList<Node> openList, ArrayList<Node> closedList, ArrayList<Integer> blackList, ArrayList<Node> snakeNodes) {
    nextNode = null;
    for (Node node : openList) {
      if (nextNode == null) {
        nextNode = node;
      };
      nextNode = node.getGCost() < nextNode.getGCost() ? node : nextNode;
    }
    if (nextNode == null) {
      return;
    }
    for (Node neighbourNode : getNeighbourNodes (nextNode, gameTiles, snakeNodes, blackList, true)) {
      boolean addToList = true;
      for (Node node : openList) {
        if (neighbourNode.tileId == node.tileId) {
          addToList = false;
          int previousGCost = node.getGCost();
          Node previousParent = node.parent;
          node.parent = nextNode;
          int newGCost = node.getGCost();
          if (node.getNumberOfParents() >= neighbourNode.minimumDistance + 1) {
            node.parent = newGCost < previousGCost ? nextNode : previousParent;
          } else {
            node.parent = previousParent;
          }
        }
      }
      for (Node node : closedList) {
        if (neighbourNode.tileId == node.tileId) {
          addToList = false;
        }
      }
      if (!addToList) {
        continue;
      }
      neighbourNode.parent = nextNode;
      openList.add(neighbourNode);
    }
    // move node from the open list to the closed list
    for (int i = openList.size () - 1; i>=0; i--) {
      Node node = openList.get(i);
      if (node == nextNode) {
        openList.remove(i);
      }
    }
    closedList.add(nextNode);

    this.findProcessingNodes(gameTiles, openList, closedList, blackList, snakeNodes);
  }

  GameTile[] simulatePath(ArrayList<Node> path) {
    GameTile[] newTiles = new GameTile[this.gameWorld.gameTiles.length];
    int pathLength = path.size() - 1;
    int i=0;
    for (GameTile tile : this.gameWorld.gameTiles) {
      newTiles[i] = new GameTile(tile.x, tile.y);
      if (tile.occupied && tile.occupiedCounter == -1) {
        // tile which are forever occupied
        newTiles[i].occupied = true;
      } else if (tile.occupied && tile.occupiedCounter - pathLength > 0) {
        // tiles which will still be occupied after moving
        newTiles[i].occupied = true;
        newTiles[i].occupiedCounter = tile.occupiedCounter - pathLength;
      }
      i++;
    }
    i = 0;
    for (Node node : path) {
      GameTile tile = newTiles[node.tileId];
      if (this.gameWorld.snakeLength - i > 0) {
        tile.occupied = true;
        tile.occupiedCounter = this.gameWorld.snakeLength - i + 1; // + 1 since we will have collected food when arriving
      } else {
        break;
      }
      i++;
    }
    return newTiles;
  }

  void generateAlternativePath() {
    // find the alternative node which will be the first one not beeing occupied
    if (this.potentialAlternativesList.size() > 0) {
      if (!this.continueGenerateAlternativePath) {
        // this is a member to make it also work when not doing it recursivley
        this.alternativeNode = null;
        for (Node node : this.potentialAlternativesList) {
          if (this.alternativeNode == null) {
            this.alternativeNode = node;
          } else {
            this.alternativeNode = node.minimumDistance < this.alternativeNode.minimumDistance ? node : this.alternativeNode;
          }
        }
        if (this.alternativeNode == null) {
          // we couldn't find an alternative node... just give up
          this.goodLuck = true;
          this.generateFinalPath(this.closedList.get(0));
          return;
        }
        // find a path to the alternative node.
        this.generateFinalPath(this.alternativeNode);
        // member to make it work when not doing it recursivley:
        if (this.visualize) {
          this.continueGenerateAlternativePath = true;
          return;
        }
      }
      // we need to change the path so that it gets long enough when reaching this node
      boolean ret;
      ret = this.increasePathLength();
      if (!ret) {
        // it was not possible to generate a longer path.
        // remove our alternative node from the alternative nodes list to try it again
        this.potentialAlternativesBlackList.add(this.alternativeNode.tileId);
        //this.generateAlternativePath();
        this.pathFound = false;
        this.continueGenerateAlternativePath = false;
        this.generateFinalPath = false;
        this.goodLuck = false;
        this.calculatePath();
        if (!this.visualize) {
          this.checkNode(this.nextNode);
        }
      } else if (!this.continueGenerateAlternativePath) {
        // theres a solution. Clear the blacklist and the punishlist
        this.potentialAlternativesBlackList = new ArrayList<Integer>();
        this.punishedTiles = new ArrayList<Integer>();
      }
    } else {
      // there are no alternatives. Give up.
      this.goodLuck = true;
      this.generateFinalPath(this.closedList.get(0));
    }
  }

  boolean increasePathLength() {
    return increasePathLength(this.gameWorld.gameTiles, this.closedList, this.potentialAlternativesList, this.potentialAlternativesBlackList, false, 0);
  }

  boolean increasePathLength(GameTile[] gameTiles, ArrayList<Node> closedList, ArrayList<Node> alternativesList, ArrayList<Integer> alternativesBlackList, boolean forceRecursive, int margin) {
    boolean pathChanged = false;
    if (this.finalPath.size() < 1) {
      // not enough space to navigate
      this.continueGenerateAlternativePath = false;
      return false;
    }
    for (Node node : this.finalPath) {
      if (node.parent == null) {
        continue;
      }
      Node originalParent = node.parent;
      for (Node firstNeighbourNode : this.findAlternativeNodes (node, gameTiles, closedList, alternativesList, alternativesBlackList)) {
        // make sure we process a node which is not yet part of our path
        if (this.nodeInList(firstNeighbourNode, this.finalPath)) {
          continue;
        }
        for (Node secondNeighbourNode : this.findAlternativeNodes (firstNeighbourNode, gameTiles, closedList, alternativesList, alternativesBlackList)) {
          if (this.nodeInList(secondNeighbourNode, this.finalPath)) {
            continue;
          }
          for (Node thirdNeighbourNode : this.findAlternativeNodes (secondNeighbourNode, gameTiles, closedList, alternativesList, alternativesBlackList)) {
            if (thirdNeighbourNode == originalParent) {
              node.parent = firstNeighbourNode;
              firstNeighbourNode.parent = secondNeighbourNode;
              secondNeighbourNode.parent = originalParent;
              pathChanged = true;
              break;
            } else {
              if (this.nodeInList(thirdNeighbourNode, this.finalPath)) {
                continue;
              }
              for (Node fourthNeighbourNode : this.findAlternativeNodes (thirdNeighbourNode, gameTiles, closedList, alternativesList, alternativesBlackList)) {
                if (fourthNeighbourNode == originalParent) {
                  node.parent = firstNeighbourNode;
                  firstNeighbourNode.parent = secondNeighbourNode;
                  secondNeighbourNode.parent = thirdNeighbourNode;
                  thirdNeighbourNode.parent = originalParent;
                  pathChanged = true;
                  break;
                }
              }
            }
          }
          if (pathChanged) {
            break;
          }
        }
        if (pathChanged) {
          break;
        }
      }
      if (pathChanged) {
        break;
      }
    }
    Node targetNode = this.finalPath.get(0);
    this.finalPath = new ArrayList<Node>();
    this.pathFound = true;
    this.generateFinalPath(targetNode, forceRecursive);
    if (pathChanged) {
      // our path is now longer. is it long enough?
      if (targetNode.getNumberOfParents() >= targetNode.minimumDistance + margin) {
        this.continueGenerateAlternativePath = false;
      } else {
        this.continueGenerateAlternativePath = true;
        if (!this.visualize || forceRecursive) {
          return this.increasePathLength(gameTiles, closedList, alternativesList, alternativesBlackList, forceRecursive, margin);
        }
      }
      return true;
    } else {
      this.continueGenerateAlternativePath = false;
      return false;
    }
  }

  boolean nodeInList(Node node, ArrayList<Node> list) {
    for (Node listNode : list) {
      if (node == listNode) {
        return true;
      }
    }
    return false;
  }

  ArrayList<Node> findAlternativeNodes(Node targetNode, GameTile[] gameTiles, ArrayList<Node> closedList, ArrayList<Node> alternativesList, ArrayList<Integer> alternativesBlackList) {
    ArrayList<Node> alternativeNodes = new ArrayList<Node>();
    if (targetNode.parent == null) {
      // doesnt make sense to change the starting node (snake head)
      return alternativeNodes;
    }
    // is there another node in the closed list around us besides the one that is the parent?
    int[] neighbourTileIds = this.getNeighbourTileIds(targetNode);
    int i = 0;
    for (Node node : closedList) {
      for (int tileId : neighbourTileIds) {
        if (node.tileId == tileId) {
          alternativeNodes.add(node);
        }
      }
    }
    return alternativeNodes;
  }

  int getTileId(int x, int y) {
    return x + y * this.gameWorld.width;
  }

  ArrayList<Node> getNeighbourNodes(Node startNode, GameTile[] gameTiles, ArrayList<Node> potentialAlternativesList, ArrayList<Integer> potentialAlternativesBlackList) {
    return getNeighbourNodes(startNode, gameTiles, potentialAlternativesList, potentialAlternativesBlackList, false);
  }

  int[] getNeighbourTileIds(Node node) {
    int x = node.getX();
    int y = node.getY();
    return this.getNeighbourTileIds(x, y);
  }

  int[] getNeighbourTileIds(int x, int y) {
    int[] tileIds = new int[] {
      -1, -1, -1, -1
    };
    if (x > 0) {
      tileIds[0] = this.getTileId(x-1, y);
    }
    if (y > 0) {
      tileIds[1] = this.getTileId(x, y-1);
    }
    if (x < this.gameWorld.width) {
      tileIds[2] = this.getTileId(x+1, y);
    }
    if (y < this.gameWorld.height) {
      tileIds[3] = this.getTileId(x, y+1);
    }
    return tileIds;
  }

  ArrayList<Node> getNeighbourNodes(Node startNode, GameTile[] gameTiles, ArrayList<Node> potentialAlternativesList, ArrayList<Integer> potentialAlternativesBlackList, boolean ignoreMoving) {

    int[] potentialNeighbours = this.getNeighbourTileIds(startNode);

    ArrayList<Node> neighbourNodes = new ArrayList<Node>(); //new Node[totalNeighbours];
    for (int n : potentialNeighbours) {
      if (n > -1) {
        GameTile gameTile = gameTiles[n];
        boolean willBeOccupied = gameTile.occupied;
        if (gameTile.occupied) {
          if (gameTile.occupiedCounter != -1) {
            // the field may be occupied at time checking, but since we need to move there,
            // it might be free until we get there
            if (!ignoreMoving && startNode.getNumberOfParents() >= gameTile.occupiedCounter) {
              willBeOccupied = false;
            } else {
              // it will not be free, but it might be free if we take a slightly longer path,
              // and still get a shorter total path to the food
              Node potentialNode = new Node(this.processing, n);
              boolean addToList = true;
              for (Node node : potentialAlternativesList) {
                if (node.tileId == potentialNode.tileId) {
                  addToList = false;
                  break;
                }
              }
              for (int tileId : potentialAlternativesBlackList) {
                if (tileId == potentialNode.tileId) {
                  addToList = false;
                  break;
                }
              }
              if (addToList) {
                potentialAlternativesList.add(potentialNode);
                potentialNode.parent = startNode;
                potentialNode.minimumDistance = gameTile.occupiedCounter;
              }
            }
          }
        }
        if (!willBeOccupied) {
          Node node = new Node(this.processing, n);
          node.minimumDistance = gameTile.occupiedCounter;
          neighbourNodes.add(node);
        }
      }
    }
    return neighbourNodes;
  }

  void generateFinalPath(Node node) {
    this.generateFinalPath(node, false);
  }

  void generateFinalPath(Node node, boolean forceRecursive) {
    for (Node previousNode : this.finalPath) {
      if (node.tileId == previousNode.tileId) {
        this.processing.print("Invalid path!\n");
        this.generateFinalPath = false;
        return;
      }
    }
    this.finalPath.add(node);
    if (node.parent != null) {
      // recursivly if no need of visualization, else, save the member and continue on next draw() call
      if (!this.visualize || forceRecursive) {
        this.generateFinalPath = false;
        this.generateFinalPath(node.parent, forceRecursive);
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
    float distance = this.processing.sqrt(a*a + b*b);
    //float distance = this.processing.abs(this.targetX - x) + this.processing.abs(this.targetY - y);
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
      int x = nextNode.getX() * GameTile.TILE_SIZE;
      int y = nextNode.getY() * GameTile.TILE_SIZE;
      this.processing.fill(0xffffffff);
      this.processing.rect(x, y, GameTile.TILE_SIZE, GameTile.TILE_SIZE);
      nextNode.draw(0xff000000);
      if (!this.visualize || !this.visualizationPaused) {
        this.checkNode(nextNode);
      }
    }
    this.processing.textAlign(this.processing.LEFT, this.processing.BOTTOM);
    this.processing.textSize(10);
    String text = "Press [v] to toggle visualization, [i] to toggle interactive mode, [ ] to pause visualization, [n] to step through visualization";
    float width = this.processing.textWidth(text);
    this.processing.fill(0xffffffff);
    this.processing.strokeWeight(0);
    this.processing.rect(1, this.processing.height - GameTile.TILE_SIZE, width, GameTile.TILE_SIZE);
    this.processing.fill(0xff000000);
    this.processing.text(text, 5, this.processing.height);
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
      if (this.finalPath.size() <= 1) {
        // we end up in a dead end. Just give up, theres nothing more to do
        this.goodLuck = false;
        this.finalPath = new ArrayList<Node>();
        return;
      }
      Node fromNode = this.finalPath.get(lastElement);
      Node toNode = this.finalPath.get(lastElement-1);
      int fromX = fromNode.getX();
      int fromY = fromNode.getY();
      int toX = toNode.getX();
      int toY = toNode.getY();
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
