/**
 * This class is implementing a modified version of A* to play the game automatically.
 * It needs refactoring, got pretty confusing in the meanwhile :/
 *
 * It contains nothing of the snake gamelogic, so you dont need to read it
 * if you are just checking my homework.
 */
package de.wegenerd;

import processing.core.PApplet;
import processing.core.PConstants;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Thread.sleep;

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
    Node nextNode;
    static int ANIMATION_DELAY = 20;
    AStar aStar;
    final Object drawLock = new Object();

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
    }

    void calculatePath() throws InterruptedException {
        synchronized (this.drawLock) {
            this.openList = new ArrayList<Node>();
            this.closedList = new ArrayList<Node>();
            this.finalPath = new ArrayList<Node>();
            this.potentialAlternativesList = new ArrayList<Node>();
        }
        int x = this.gameWorld.snakeX;
        int y = this.gameWorld.snakeY;
        GameTile snakeHeadGameTile = this.gameWorld.gameTiles[GameTile.getTileIdByCoordinates(x, y)];
        // search for tile with food and generate a list of all snake tiles
        ArrayList<GameTile> snakeTiles = new ArrayList<GameTile>();
        GameTile foodGameTile = null;
        for (GameTile gameTile : this.gameWorld.gameTiles) {
            if (gameTile.hasFood) {
                foodGameTile = gameTile;
            }
            if (gameTile.occupied && gameTile.occupiedCounter > 0) {
                snakeTiles.add(gameTile);
            }
        }
        if (foodGameTile == null) {
            return;
        }
        Collections.sort(snakeTiles);
        ArrayList<Node> path;
        synchronized (drawLock) {
            this.aStar = new AStar(this.processing, this.gameWorld.gameTiles, snakeHeadGameTile, foodGameTile);
        }
        path = this.aStar.getPath();
        while (path == null) {
            // no direct path to food possible. Try to find a way to our tail
            if (snakeTiles.size() == 0) {
                PApplet.print("Couldn't find a way out\n");
                break;
            }
            synchronized (drawLock) {
                this.aStar = new AStar(this.processing, this.gameWorld.gameTiles, snakeHeadGameTile, snakeTiles.remove(0));
            }
            path = this.aStar.getPath();
            this.aStar.exploreAll();
            while (path != null) {
                Node targetNode = path.get(0);
                if (path.size() <= targetNode.minimumDistance) {
                    PApplet.print("Not long enough: " + path.size() + " (" + targetNode.minimumDistance + ")\n");
                    path = increasePathLength(path, aStar.closedList);
                    if (path != null) {
                        PApplet.print("Current size is: " + path.size() + "\n");
                    }
                }
            }
        }
        if (path != null) {
            this.finalPath = path;
            this.pathFound = true;
        }
    }
/*
    void checkNode(Node startNode) throws InterruptedException {
        if (ANIMATION_DELAY > 0) {
            sleep(ANIMATION_DELAY);
        }
        if (startNode == null) {
            return;
        }
        int x = startNode.getX();
        int y = startNode.getY();
        for (Node neighbourNode : this.getNeighbourNodes(startNode, this.gameWorld.gameTiles, this.potentialAlternativesList, this.potentialAlternativesBlackList)) {
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
                int maxGCost = Processing.BOARD_HORIZONTAL_SIZE * Processing.BOARD_VERTICAL_SIZE;
                if (neighbourNode.getGCost() < maxGCost) {
                    synchronized (this.drawLock) {
                        this.openList.add(neighbourNode);
                    }
                }
            }
        }
        // move node from the open list to the closed list
        synchronized (this.drawLock) {
            for (int i = this.openList.size() - 1; i >= 0; i--) {
                Node node = this.openList.get(i);
                if (node == startNode) {
                    this.openList.remove(i);
                }
            }
            this.closedList.add(startNode);
        }
        // check if this is the target node
        if (x == targetX && y == targetY) {
            boolean isDeadEnd = this.checkForDeadEnd(startNode);

            if (isDeadEnd) {
                // dead end. Punish nodes on path to try an alternative
                synchronized (this.drawLock) {
                    this.finalPath = new ArrayList<Node>();
                }
                this.generateFinalPath(startNode);
                for (Node node : this.finalPath) {
                    this.punishedTiles.add(node.tileId);
                }
                synchronized (this.drawLock) {
                    this.openList = new ArrayList<Node>();
                    this.closedList = new ArrayList<Node>();
                    this.finalPath = new ArrayList<Node>();
                    this.potentialAlternativesList = new ArrayList<Node>();
                }
                this.potentialAlternativesBlackList = new ArrayList<Integer>();
                this.pathFound = false;
                this.checkNode(this.nextNode);
                return;
            } else {
                // found a path which does not lead into a dead end
                this.nextNode = null;
                synchronized (this.drawLock) {
                    this.finalPath = new ArrayList<Node>();
                }
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
            synchronized (this.drawLock) {
                this.nextNode = this.openList.get(0);
                for (Node node : this.openList) {
                    this.nextNode = node.getFCost() < nextNode.getFCost() ? node : nextNode;
                }
            }
            this.checkNode(nextNode);
        } else {
            // okay, we didn't find a path, but there is nothing in the open list
            // the food must be hidden behind us.
            // try to find an alternative path by making the path longer and therefore escape
            synchronized (this.drawLock) {
                this.nextNode = null;
            }
            this.generateAlternativePath();
        }
    }
    */

    boolean checkForDeadEnd(Node startNode) throws InterruptedException {
        /*
        // we need to check if we are running into a dead end.
        // in order to do this, we just need to see if we could reach our tail after we arrive at the food
        // because if we can reach our tail we will be able to reach every other field, too
        // if we can not reach it, increase the length of our path, so the path before us becomes free.
        this.generateFinalPath(startNode); // force recursive calculation, we need the final path
        GameTile[] futureGameTiles = this.simulatePath(this.finalPath);
        int i = 0;
        int futureProcessingHeadTileId = 0;
        GameTile futureProcessingHeadTile = futureGameTiles[0];
        for (GameTile tile : futureGameTiles) {
            if (futureProcessingHeadTile.occupiedCounter < tile.occupiedCounter) {
                futureProcessingHeadTile = tile;
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
            for (i = snakeNodes.size() - 1; i >= 0; i--) {
                Node node = snakeNodes.get(i);
                if (node == targetNode) {
                    snakeNodes.remove(i);
                }
            }
            if (targetNode.getNumberOfParents() >= targetNode.minimumDistance) {
                isDeadEnd = false;
            } else {
                synchronized (this.drawLock) {
                    this.finalPath = new ArrayList<Node>();
                }
                this.generateFinalPath(targetNode);
                if (this.increasePathLength(futureGameTiles, closedList, snakeNodes, blackList, 6)) {
                    isDeadEnd = false;
                }
                blackList.add(targetNode.tileId);
                closedList = new ArrayList<Node>();
                snakeNodes = this.findProcessingNodes(futureStartNode, futureGameTiles, blackList, closedList);
            }
        }
        return isDeadEnd;*/
        return false;
    }

    ArrayList<Node> findProcessingNodes(Node startNode, GameTile[] gameTiles, ArrayList<Integer> blackList, ArrayList<Node> closedList) {
        ArrayList<Node> openList = new ArrayList<Node>();
        ArrayList<Node> snakeNodes = new ArrayList<Node>();
        openList.add(startNode);
        this.findSnakeNodes(gameTiles, openList, closedList, blackList, snakeNodes);
        return snakeNodes;
    }

    void findSnakeNodes(GameTile[] gameTiles, ArrayList<Node> openList, ArrayList<Node> closedList, ArrayList<Integer> blackList, ArrayList<Node> snakeNodes) {
        /*
        synchronized (this.drawLock) {
            this.nextNode = null;
            for (Node node : openList) {
                if (this.nextNode == null) {
                    this.nextNode = node;
                }
                this.nextNode = node.getGCost() < this.nextNode.getGCost() ? node : this.nextNode;
            }
            if (this.nextNode == null) {
                return;
            }
        }
        for (Node neighbourNode : getNeighbourNodes(this.nextNode, gameTiles, snakeNodes, blackList, true)) {
            boolean addToList = true;
            for (Node node : openList) {
                if (neighbourNode.tileId == node.tileId) {
                    addToList = false;
                    int previousGCost = node.getGCost();
                    Node previousParent = node.parent;
                    node.parent = this.nextNode;
                    int newGCost = node.getGCost();
                    if (node.getNumberOfParents() >= neighbourNode.minimumDistance + 1) {
                        node.parent = newGCost < previousGCost ? this.nextNode : previousParent;
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
            neighbourNode.parent = this.nextNode;
            openList.add(neighbourNode);
        }
        // move node from the open list to the closed list
        for (int i = openList.size() - 1; i >= 0; i--) {
            Node node = openList.get(i);
            if (node == this.nextNode) {
                openList.remove(i);
            }
        }
        closedList.add(this.nextNode);

        this.findSnakeNodes(gameTiles, openList, closedList, blackList, snakeNodes);*/
    }

    /*
        GameTile[] simulatePath(ArrayList<Node> path) {
            GameTile[] newTiles = new GameTile[this.gameWorld.gameTiles.length];
            int pathLength = path.size() - 1;
            int i = 0;
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

        void generateAlternativePath() throws InterruptedException {
            // find the alternative node which will be the first one not beeing occupied
            if (this.potentialAlternativesList.size() > 0) {

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
                    this.generateFinalPath(this.closedList.get(0));
                    return;
                }
                // find a path to the alternative node.
                this.generateFinalPath(this.alternativeNode);
                // we need to change the path so that it gets long enough when reaching this node
                boolean ret;
                ret = this.increasePathLength();
                if (!ret) {
                    // it was not possible to generate a longer path.
                    // remove our alternative node from the alternative nodes list to try it again
                    this.potentialAlternativesBlackList.add(this.alternativeNode.tileId);
                    //this.generateAlternativePath();
                    this.pathFound = false;
                    this.calculatePath();
                    this.checkNode(this.nextNode);
                } else {
                    // theres a solution. Clear the blacklist and the punishlist
                    this.potentialAlternativesBlackList = new ArrayList<Integer>();
                    this.punishedTiles = new ArrayList<Integer>();
                }
            } else {
                // there are no alternatives. Give up.
                this.generateFinalPath(this.closedList.get(0));
            }
        }

        boolean increasePathLength() throws InterruptedException {
            return increasePathLength(this.gameWorld.gameTiles, this.closedList, this.potentialAlternativesList, this.potentialAlternativesBlackList, 0);
        }
    */
    ArrayList<Node> increasePathLength(ArrayList<Node> path, ArrayList<Node> nodeList) throws InterruptedException {
        sleep(ANIMATION_DELAY);
        boolean pathChanged = false;
        for (Node node : path) {
            if (node.parent == null) {
                continue;
            }
            Node originalParent = node.parent;
            for (Node firstNeighbourNode : this.findNeighbourNodes(node, nodeList)) {
                // make sure we process a node which is not yet part of our path
                if (this.nodeInList(firstNeighbourNode, path)) {
                    continue;
                }
                for (Node secondNeighbourNode : this.findNeighbourNodes(firstNeighbourNode, nodeList)) {
                    if (this.nodeInList(secondNeighbourNode, path)) {
                        continue;
                    }
                    for (Node thirdNeighbourNode : this.findNeighbourNodes(secondNeighbourNode, nodeList)) {
                        if (thirdNeighbourNode == originalParent) {
                            node.parent = firstNeighbourNode;
                            firstNeighbourNode.parent = secondNeighbourNode;
                            secondNeighbourNode.parent = originalParent;
                            pathChanged = true;
                            break;
                        } else {
                            if (this.nodeInList(thirdNeighbourNode, path)) {
                                continue;
                            }
                            for (Node fourthNeighbourNode : this.findNeighbourNodes(thirdNeighbourNode, nodeList)) {
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
        if (pathChanged) {
            return AStar.generatePath(path.get(0));
        }
        return null;
//        Node targetNode = this.finalPath.get(0);
//        synchronized (this.drawLock) {
//            this.finalPath = new ArrayList<Node>();
//        }
//        this.pathFound = true;
//        this.generateFinalPath(targetNode);
//        if (pathChanged) {
//            // our path is now longer. is it long enough?
//            if (targetNode.getNumberOfParents() >= targetNode.minimumDistance + margin) {
//                //this.continueGenerateAlternativePath = false;
//            } else {
//                return this.increasePathLength(gameTiles, nodeList, alternativesList, alternativesBlackList, margin);
//            }
//            return true;
//        } else {
//            return false;
//        }
    }

    boolean nodeInList(Node node, ArrayList<Node> list) {
        for (Node listNode : list) {
            if (node == listNode) {
                return true;
            }
        }
        return false;
    }

    ArrayList<Node> findNeighbourNodes(Node targetNode, ArrayList<Node> nodeList) {
        ArrayList<Node> neighbourNodes = new ArrayList<Node>();
        if (targetNode.parent == null) {
            // doesnt make sense to change the starting node (snake head)
            return neighbourNodes;
        }
        // add all nodes from the passed list to the result if it is a neighbour
        int[] neighbourTileIds = targetNode.tile.getNeighbourTileIds();
        for (Node node : nodeList) {
            for (int tileId : neighbourTileIds) {
                if (node.tile.tileId == tileId) {
                    neighbourNodes.add(node);
                }
            }
        }
        return neighbourNodes;
    }
/*
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
        int[] tileIds = new int[]{
                -1, -1, -1, -1
        };
        if (x > 0) {
            tileIds[0] = this.getTileId(x - 1, y);
        }
        if (y > 0) {
            tileIds[1] = this.getTileId(x, y - 1);
        }
        if (x < this.gameWorld.width) {
            tileIds[2] = this.getTileId(x + 1, y);
        }
        if (y < this.gameWorld.height) {
            tileIds[3] = this.getTileId(x, y + 1);
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
                                synchronized (this.drawLock) {
                                    potentialAlternativesList.add(potentialNode);
                                    potentialNode.parent = startNode;
                                    potentialNode.minimumDistance = gameTile.occupiedCounter;
                                }
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

    void generateFinalPath(Node node) throws InterruptedException {
        if (ANIMATION_DELAY > 0) {
            sleep(ANIMATION_DELAY);
        }
        for (Node previousNode : this.finalPath) {
            if (node.tileId == previousNode.tileId) {
                PApplet.print("Invalid path!\n");
                return;
            }
        }
        synchronized (this.drawLock) {
            this.finalPath.add(node);
        }
        if (node.parent != null) {
            this.generateFinalPath(node.parent);
        }
    }
*/

    void draw() {
        synchronized (this.drawLock) {
            if (this.aStar != null) {
                this.aStar.draw();
            }
//            for (Node node : this.openList) {
//                node.draw(0xaa00ff00);
//            }
//            for (Node node : this.closedList) {
//                node.draw(0xaaff0000);
//            }
//            for (Node node : this.potentialAlternativesList) {
//                node.draw(0xaa0000ff);
//            }
//            for (Node node : this.finalPath) {
//                node.draw(0xffffff00);
//            }
//            if (this.nextNode != null) {
//                int x = this.nextNode.getX() * GameTile.TILE_SIZE;
//                int y = this.nextNode.getY() * GameTile.TILE_SIZE;
//                this.processing.fill(0xffffffff);
//                this.processing.rect(x, y, GameTile.TILE_SIZE, GameTile.TILE_SIZE);
//                this.nextNode.draw(0xff000000);
//            }
        }
        this.processing.textAlign(PConstants.LEFT, PConstants.BOTTOM);
        this.processing.textSize(10);
        String text = "Press [v] to toggle visualization, [i] to toggle interactive mode, [ ] to pause visualization, [n] to step through visualization";
        float width = this.processing.textWidth(text);
        this.processing.fill(0xffffffff);
        this.processing.strokeWeight(0);
        this.processing.rect(1, this.processing.height - GameTile.TILE_SIZE, width, GameTile.TILE_SIZE);
        this.processing.fill(0xff000000);
        this.processing.text(text, 5, this.processing.height);
    }

    void tick() throws InterruptedException {
        if (!this.pathFound) {
            this.gameWorld.gamePaused = true;
            this.calculatePath();
        } else {
            this.gameWorld.gamePaused = false;
            int lastElement = this.finalPath.size() - 1;
            if (lastElement == 1) {
                this.pathFound = false;
            }
            if (this.finalPath.size() <= 1) {
                synchronized (this.drawLock) {
                    this.finalPath = new ArrayList<Node>();
                }
                return;
            }
            Node fromNode = this.finalPath.get(lastElement);
            Node toNode = this.finalPath.get(lastElement - 1);
            int fromX = fromNode.getX();
            int fromY = fromNode.getY();
            int toX = toNode.getX();
            int toY = toNode.getY();
            if (toX > fromX) {
                this.gameWorld.snakeDirection = GameWorld.RIGHT;
            } else if (toX < fromX) {
                this.gameWorld.snakeDirection = GameWorld.LEFT;
            } else if (toY > fromY) {
                this.gameWorld.snakeDirection = GameWorld.DOWN;
            } else if (toY < fromY) {
                this.gameWorld.snakeDirection = GameWorld.UP;
            }
            synchronized (this.drawLock) {
                this.finalPath.remove(lastElement);
            }
        }
    }
}
