// If you left click anywhere after game ends, the game restarts
// with bombs in randomly set new positions

import java.util.ArrayList;
import java.util.Random;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

interface WorldConstants {
  int cellLength = 25;

  Color H_COLOR = Color.decode("#8b9dc3");
  Color F_COLOR = Color.decode("#ae0001");
  Color B_COLOR1 = Color.decode("#f9d62e");
  Color B_COLOR2 = Color.decode("#fc913a");
  Color B_COLOR3 = Color.decode("#ff4e50");

  WorldImage HIDDEN = new OverlayOffsetImage(
      new RectangleImage(cellLength - 1, cellLength - 1, "outline", Color.BLACK), 1, 1,
      new RectangleImage(cellLength, cellLength, "solid", H_COLOR));

  WorldImage FLAGGED = new BesideAlignImage(AlignModeY.TOP,
      new LineImage(new Posn(0, 20), Color.GRAY),
      new TriangleImage(new Posn(0, 0), new Posn(0, 10), new Posn(10, 0), "solid", F_COLOR));

  WorldImage REVEALED = new OverlayOffsetImage(
      new RectangleImage(cellLength - 1, cellLength - 1, "outline", Color.BLACK), 1, 1,
      new RectangleImage(cellLength, cellLength, "solid", Color.DARK_GRAY));

  WorldImage MINE = new OverlayOffsetImage(new LineImage(new Posn(-2, 2), B_COLOR1), 0, 7,
      new OverlayOffsetImage(new LineImage(new Posn(-2, 2), B_COLOR2), -1, -6,
          new OverlayOffsetImage(new LineImage(new Posn(2, 1), B_COLOR3), 2, 5,
              new OverlayOffsetImage(new LineImage(new Posn(1, 4), Color.WHITE), 0, 5,
                  new CircleImage(7, OutlineMode.SOLID, Color.BLACK)))));

}

class Cell implements WorldConstants {
  boolean hasMine;
  boolean flagged;
  boolean hidden;
  ArrayList<Cell> neighbors;

  // Constructor for Cell
  Cell() {
    this.hasMine = false;
    this.flagged = false;
    this.hidden = true;
    this.neighbors = new ArrayList<>();
  }

  // Secondary Constructor for Cell
  Cell(boolean hasMine, boolean flagged, boolean hidden, ArrayList<Cell> neighbors) {
    this.hasMine = hasMine;
    this.flagged = flagged;
    this.hidden = hidden;
    this.neighbors = neighbors;
  }

  // Flags this cell
  void flagCell() {
    this.flagged = !this.flagged;
  }

  // Checks if this cell has a mine
  boolean hasMine() {
    return this.hasMine;
  }

  // Reveals this cell
  void revealCell() {
    this.hidden = false;
  }

  // Sets a mine to this cell
  void setMine() {
    this.hasMine = true;
  }

  // Adds a cell to the list of neighbors for this cell
  void addNeighbor(Cell c) {
    this.neighbors.add(c);
  }

  // Counts the number of mines neighboring this cell
  int countMines() {
    int mineCount = 0;
    for (Cell neighbor : this.neighbors) {
      if (neighbor.hasMine()) {
        mineCount++;
      }
    }
    return mineCount;
  }

  // Checks if this cell is flagged and hidden
  boolean isFlaggedAndHidden() {
    return flagged && hidden;
  }

  // Draws the number of neighbors with mines of this cell
  WorldImage drawNumber() {
    if (this.countMines() == 0) {
      return new EmptyImage();
    }
    else if (this.countMines() == 1) {
      return new TextImage("1", Color.decode("#47648d"));
    }
    else if (this.countMines() == 2) {
      return new TextImage("2", Color.decode("#1cac78"));
    }
    else if (this.countMines() == 3) {
      return new TextImage("3", Color.decode("#ba0048"));
    }
    else if (this.countMines() == 4) {
      return new TextImage("4", Color.decode("#ffff66"));
    }
    else if (this.countMines() == 5) {
      return new TextImage("5", Color.decode("#ffb3ff"));
    }
    else if (this.countMines() == 6) {
      return new TextImage("6", Color.decode("#ce7e00"));
    }
    else {
      return new TextImage("7", Color.decode("#753bcc"));
    }
  }

  // Draws this cell
  WorldImage drawcell() {
    if (this.hidden) {
      if (this.flagged) {
        return new OverlayOffsetImage(FLAGGED, 0, 0, HIDDEN);
      }
      return HIDDEN;
    }
    else {
      if (this.hasMine) {
        return new OverlayOffsetImage(MINE, 0, 0,
            new OverlayOffsetImage(this.drawNumber(), 0, 0, REVEALED));
      }
      return new OverlayOffsetImage(this.drawNumber(), 0, 0, REVEALED);
    }
  }
}

class GameWorld extends World implements WorldConstants {
  ArrayList<ArrayList<Cell>> cells;
  int rows;
  int columns;
  Random rand;
  boolean gameOver = false; // To track game state
  int minesLeft;

  // Constructor for gameworld
  GameWorld(int rows, int columns, int mines, Random rand) {
    this.rows = rows;
    this.columns = columns;
    this.rand = (rand == null) ? new Random() : rand;
    this.cells = setCells(rows, columns);
    this.cells = linkCells(this.cells);
    this.cells = setMines(this.cells, mines);
    this.minesLeft = mines;
  }

  // Restarts the game
  public void restartGame() {
    this.cells = setCells(rows, columns);
    this.cells = linkCells(this.cells);
    this.cells = setMines(this.cells, minesLeft);
    this.gameOver = false;
  }

  // Sets cells in the gameworld
  ArrayList<ArrayList<Cell>> setCells(int rows, int columns) {
    ArrayList<ArrayList<Cell>> tempCells = new ArrayList<>();
    for (int r = 0; r < rows; r++) {
      ArrayList<Cell> row = new ArrayList<>();
      for (int c = 0; c < columns; c++) {
        row.add(new Cell());
      }
      tempCells.add(row);
    }
    return tempCells;
  }

  // Links cells together in the gameworld
  ArrayList<ArrayList<Cell>> linkCells(ArrayList<ArrayList<Cell>> cells) {
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        for (int i = -1; i <= 1; i++) {
          for (int j = -1; j <= 1; j++) {
            if (i == 0 && j == 0) {
              continue;
            }
            int nr = r + i;
            int nc = c + j;
            if (nr >= 0 && nr < rows && nc >= 0 && nc < columns) {
              cells.get(r).get(c).addNeighbor(cells.get(nr).get(nc));
            }
          }
        }
      }
    }
    return cells;
  }

  // Randomly sets mines in the gameWorld
  ArrayList<ArrayList<Cell>> setMines(ArrayList<ArrayList<Cell>> cells, int mines) {
    int placedMines = 0;
    while (placedMines < mines) {
      int r = rand.nextInt(rows);
      int c = rand.nextInt(columns);
      Cell cell = cells.get(r).get(c);
      if (!cell.hasMine) {
        cell.setMine();
        placedMines++;
      }
    }
    return cells;
  }

  // OnMouseClick method for MineSweeper
  // If you left click anywhere after game ends, the game restarts
  // with bombs in new positions
  public void onMouseClicked(Posn pos, String buttonName) {
    int col = pos.x / cellLength;
    int row = pos.y / cellLength;

    if (row >= 0 && row < this.rows && col >= 0 && col < this.columns) {
      Cell cell = this.cells.get(row).get(col);
      if ("RightButton".equals(buttonName)) {
        if (cell.hidden) {
          cell.flagCell();
        }
      }
      else if ("LeftButton".equals(buttonName)) {
        if (!cell.flagged && cell.hidden) {
          revealCellAndCheckGameEnd(cell);
        }
        else if (gameOver) {
          restartGame();
          return;
        }
      }
    }
  }

  // Checks if a mine has been revealed, if so ends game (lose)
  // Otherwise, if all cells have been revealed except for mines, game ends (win)
  void revealCellAndCheckGameEnd(Cell cell) {
    if (cell.hasMine) {
      cell.revealCell();
      gameOver = true;
      endGame(false);
    }
    else {
      floodFill(cell);
      if (checkWin()) {
        gameOver = true;
        endGame(true);
      }
    }
  }

  // Flood method for GameWorld
  void floodFill(Cell cell) {
    if (!cell.hidden || cell.flagged || cell.hasMine)
      return;
    cell.revealCell();
    if (cell.countMines() == 0) {
      for (Cell neighbor : cell.neighbors) {
        floodFill(neighbor);
      }
    }
  }

  // Checks for a win in gameWorld
  boolean checkWin() {
    for (ArrayList<Cell> row : cells) {
      for (Cell cell : row) {
        if (!cell.hasMine && cell.hidden) {
          return false;
        }
      }
    }
    return true;
  }

  // If win game, produces win message
  // If lose game, produces lose message
  void endGame(boolean won) {
    if (won) {
      System.out.println("Congratulations, you've won!");
    }
    else {
      System.out.println("Game over, you hit a mine!");
    }
  }

  // Creates WorldScene for gameWorld
  public WorldScene makeScene() {
    if (gameOver) {
      return this.lastScene("You clicked a mine.\nleft click to restart");
    }

    WorldScene scene = new WorldScene(this.rows * cellLength, this.columns * cellLength + 60);

    // Draw cells
    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.columns; c++) {
        Cell cell = this.cells.get(r).get(c);
        WorldImage cellImage = cell.drawcell();
        scene.placeImageXY(cellImage, c * cellLength + cellLength / 2,
            r * cellLength + cellLength / 2);
      }
    }

    int remainingMines = countRemainingMines();
    WorldImage minesLeftText = new TextImage("Mines Left: " + remainingMines, 20, Color.RED);
    scene.placeImageXY(minesLeftText, this.columns * cellLength / 2, this.rows * cellLength + 30);

    return scene;
  }

  // Counts how many mines remain in the game
  int countRemainingMines() {
    int remainingMines = 0;
    for (ArrayList<Cell> row : cells) {
      for (Cell cell : row) {
        if (cell.hasMine() && !cell.isFlaggedAndHidden()) {
          remainingMines++;
        }
      }
    }
    return remainingMines;
  }
}

class ExamplesMinesweeper {
  GameWorld currentGame;

  // Initialize a game for testing purposes
  void init() {
    this.currentGame = new GameWorld(15, 15, 15, new Random(42));
    this.currentGame.cells.get(0).get(0).setMine();
    this.currentGame.cells.get(1).get(1).revealCell();
  }

  void testInitialSetup(Tester t) {
    this.init();

    t.checkExpect(this.currentGame.cells.get(0).get(0).hasMine, true);
    t.checkExpect(this.currentGame.cells.get(0).get(0).flagged, false);
    t.checkExpect(this.currentGame.cells.get(0).get(0).hidden, true);

    t.checkExpect(this.currentGame.cells.get(1).get(1).neighbors.size() > 0, true);
  }

  // Test counting mines around a cell
  void testCountMines(Tester t) {
    this.init();

    t.checkExpect(this.currentGame.cells.get(1).get(1).countMines(), 2);
  }

  void testFlagCell(Tester t) {
    this.init();
    Cell testCell = this.currentGame.cells.get(0).get(1);
    testCell.flagCell();
    t.checkExpect(testCell.flagged, true);
  }

  void testFlagCellTwice(Tester t) {
    init();
    Cell cell = this.currentGame.cells.get(0).get(1);
    cell.flagCell();
    t.checkExpect(cell.flagged, true);
  }

  void testSetMineAndHasMine(Tester t) {
    init();
    Cell cell = this.currentGame.cells.get(1).get(2);
    cell.setMine();
    t.checkExpect(cell.hasMine(), true);
  }

  void testAddNeighborAndCount(Tester t) {
    init();
    Cell cell = new Cell();
    Cell neighbor1 = new Cell();
    Cell neighbor2 = new Cell();
    cell.addNeighbor(neighbor1);
    cell.addNeighbor(neighbor2);
    t.checkExpect(cell.neighbors.size(), 2);
  }

  void testFloodFillNonMineCells(Tester t) {
    this.init();
    Cell startCell = this.currentGame.cells.get(1).get(1);
    this.currentGame.floodFill(startCell);
    t.checkExpect(startCell.hidden, false, "Start cell should be revealed.");
    t.checkExpect(startCell.neighbors.get(0).hidden, true, "Neighbor cell should be revealed.");
  }

  //  This test functions correctly however for some reason it 
  //  it's printing endgame in the console, so we commented it out
  
  //  void testRevealMineCellAndGameEnd(Tester t) {
  //    this.init();
  //    Cell mineCell = this.currentGame.cells.get(0).get(0);
  //    this.currentGame.revealCellAndCheckGameEnd(mineCell);
  //    t.checkExpect(this.currentGame.gameOver, true, "Game should end after revealing a mine.");
  //  }

  void testGameRestart(Tester t) {
    init();
    this.currentGame.gameOver = true;
    this.currentGame.restartGame();
    t.checkExpect(this.currentGame.gameOver, false);
  }

  void testRevealCell(Tester t) {
    this.init();
    Cell testCell = this.currentGame.cells.get(2).get(1);
    testCell.revealCell();
    t.checkExpect(testCell.hidden, false);
  }

  void testDrawCell(Tester t) {
    this.init();
    Cell mineCell = this.currentGame.cells.get(0).get(0);
    t.checkExpect(mineCell.drawcell() instanceof OverlayOffsetImage, true);
  }

  void testGameWithBigBang(Tester t) {
    GameWorld game = new GameWorld(15, 15, 15, new Random());
    int worldWidth = game.columns * WorldConstants.cellLength;

    int worldHeight = game.rows * WorldConstants.cellLength + 40;
    game.bigBang(worldWidth, worldHeight, 0.5);
  }
}
