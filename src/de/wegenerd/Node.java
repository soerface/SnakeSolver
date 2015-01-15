package de.wegenerd;

class Node {
  int tileId;
  float hCost;
  int minimumDistance;
  Node parent;
  Processing mainClass;
  static int DOT_SIZE = GameTile.TILE_SIZE / 4;
  int costs;

  Node(Processing mainClass, int tileId) {
    this.mainClass = mainClass;
    this.tileId = tileId;
    this.minimumDistance = 0;
    this.costs = 1;
    for (int tile : this.mainClass.autoSolver.punishedTiles) {
      if (tile == tileId) {
        this.costs*=2;
      }
    }
  }

  int getGCost() {
    if (this.parent != null) {
      return this.parent.getGCost() + this.costs;
    }
    return 0;
  }

  int getNumberOfParents() {
    if (this.parent != null) {
      return this.parent.getNumberOfParents() + 1;
    }
    return 0;
  }


  int getFCost() {
    return this.getGCost() + (int)this.hCost;
  }

  int getX() {
    return this.tileId % this.mainClass.gameWorld.width;
  }

  int getY() {
    return this.tileId / this.mainClass.gameWorld.width;
  }

  void draw(int color) {
    this.mainClass.strokeWeight(0);
    this.mainClass.fill(color);
    int x = this.getX() * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
    int y = this.getY() * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
    this.mainClass.strokeWeight(1);
    this.mainClass.stroke(color);
    if (this.parent != null) {
      int parentX = this.parent.getX() * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
      int parentY = this.parent.getY() * GameTile.TILE_SIZE + GameTile.TILE_SIZE / 2;
      float vectorX = parentX - x;
      float vectorY = parentY - y;
      vectorX *= 0.4;
      vectorY *= 0.4;
      this.mainClass.line(x, y, x+vectorX, y+vectorY);
    }
    this.mainClass.ellipse(x, y, DOT_SIZE, DOT_SIZE);
    if (this.mainClass.DEBUG) {
      mainClass.fill(color);
      mainClass.textAlign(mainClass.LEFT, mainClass.BOTTOM);
      x = this.getX() * GameTile.TILE_SIZE;
      y = this.getY() * GameTile.TILE_SIZE + GameTile.TILE_SIZE;
      mainClass.text(this.getGCost(), x+1, y);
      mainClass.textAlign(mainClass.RIGHT, mainClass.BOTTOM);
      mainClass.text(this.tileId, x+GameTile.TILE_SIZE-1, y);
    }
  }
}
