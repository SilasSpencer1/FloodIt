
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import tester.*;
import javalib.impworld.*;

import java.awt.Color;

import javalib.worldimages.*;

//Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the
  // screen
  int x;
  int y;
  Color color;
  boolean isFlooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean isFlooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.isFlooded = isFlooded;
    this.left = null;
    this.right = null;
    this.top = null;
    this.bottom = null;
  }

  public WorldImage place(Color c) {
    return new RectangleImage(Board.blockSize, Board.blockSize, "solid", c);
  }
}


// main world class
class Board extends World {
  ArrayList<Cell> cells;
  static final int BOARD_SIZE = 22;
  int amountOfColors;
  HashMap<Integer, Color> hashmap;
  static final int blockSize = 22;
  Random rand = new Random();
  int amountOfClicks;
  static final int boardTimesBlock = BOARD_SIZE * blockSize / 2;

  Board(ArrayList<Cell> cells, int amountOfColors, HashMap<Integer, Color> hashmap) {
    this.cells = cells;
    this.amountOfColors = amountOfColors;
    this.hashmap = hashmap;
    this.amountOfClicks = 50;
    this.createPosition();
    this.connect();
  }
  
  static final WorldImage bg = new RectangleImage(Board.BOARD_SIZE * Board.BOARD_SIZE,
      Board.BOARD_SIZE * Board.BOARD_SIZE, OutlineMode.SOLID, Color.WHITE);
  
  public int createField(int cellX) {
    int field = cellX * Board.blockSize + Board.blockSize / 2;
    return field;
  }

  public WorldScene makeScene() {
    WorldScene start = this.getEmptyScene();
    
    start.placeImageXY(bg, ((int) 
        Math.pow(BOARD_SIZE, 2)) / 2, ((int) Math.pow(BOARD_SIZE, 2)) / 2);
    for (Cell cell : cells) {
      start.placeImageXY(cell.place(cell.color), 
          createField(cell.x), cell.y
          * Board.blockSize + Board.blockSize / 2);
      
      start.placeImageXY((new TextImage(Integer.toString(this.sucessfulClick()), blockSize,
          Color.WHITE)), boardTimesBlock, blockSize / 2);
    }
    return start;
  }

  public void onKeyEvent(String key) {
    if (key.equals("x")) {
      this.makeFinalScene("Goodbye!");
      this.makeScene();
    }
    else {
      if (key.equals("r")) {
        new Board(this.cells, this.amountOfColors, this.hashmap);
        this.amountOfClicks = Board.BOARD_SIZE + (Math.floorDiv(Board.BOARD_SIZE, 2));
        this.makeScene();
      }
    }
  }

  // make posn for each cell
  public void createPosition() {
    for (int row = 0; row < ((int) Math.pow(BOARD_SIZE, 2)) / BOARD_SIZE; row++) {
      for (int column = 0; column < ((int) Math.pow(BOARD_SIZE, 2)) / BOARD_SIZE; column++) {
        this.cells.add(new Cell(column, row, this.hashmap.get(rand.nextInt(this.amountOfColors)),
            false));
      } 
    }
  }

  // the end of the world
  public WorldEnd worldEnds() {
    if (this.allSameColor() && (this.sucessfulClick() > 0)) {
      return new WorldEnd(true, this.makeFinalScene("You win!"));
    }
    else {
      new WorldEnd(false, this.makeScene());
    }
    if (this.sucessfulClick() == 0) {
      return new WorldEnd(true, this.makeFinalScene("you have lost"));
    }
    return new WorldEnd(false, this.makeScene());
  }
  
  // to make the end game scene
  public WorldScene makeFinalScene(String s) {
    int canvas = boardTimesBlock;
    WorldScene final1 = this.getEmptyScene();
    final1.placeImageXY(bg, canvas, canvas);
    final1.placeImageXY((new TextImage(s
        , blockSize, Color.BLACK)), boardTimesBlock, boardTimesBlock);
    return final1;
  }

  // registers a mouse click
  public void onMouseClicked(Posn pos) {
    this.mainClick(getClickedCell(pos));
    this.amountOfClicks -= 1;
  }
  
  // conditional for mainclick
  public boolean mainClickHelp(Cell pos) {
    return pos != null;
  }
  
  // second conditional for main click
  public boolean mainClickHelp2(Color col, Color click) {
    return col.equals(click);
  }
  
  public void mainClick(Cell clickl) {
    for (Cell c : cells) {
      if (c.isFlooded) {
        c.color = clickl.color;
        if (mainClickHelp(c.left) && mainClickHelp2(c.left.color, clickl.color)) {
          c.left.isFlooded = true;
        }
        if (mainClickHelp(c.right) && mainClickHelp2(c.right.color, clickl.color)) {
          c.right.isFlooded = true;
        }
        if (mainClickHelp(c.bottom) && mainClickHelp2(c.bottom.color, clickl.color)) {
          c.bottom.isFlooded = true;
        }
        if (mainClickHelp(c.top) && mainClickHelp2(c.top.color, clickl.color)) {
          c.top.isFlooded = true;
        }
      }
    }
  }

  // to help determine which block / cell was clicked
  public Cell getClickedCell(Posn loc) { // 22 89
    return this.cells.get((Math.floorDiv(loc.x, Board.blockSize))
        + (Math.floorDiv(loc.y, Board.blockSize) * BOARD_SIZE));
  }

  // helper to check the range of colors used
  public int goodNumOfColors() {
    if (this.amountOfColors <= 8 && this.amountOfColors >= 3) {
      return this.amountOfColors;
    }
    else {
      throw new RuntimeException("color selection outside of index");
    }
  }

  // helper to determine whether player has lost
  public int sucessfulClick() {
    if (this.amountOfClicks < 0) {
      return 0;
    }
    else {
      return this.amountOfClicks;
    }
  }

  // a helper to determine if all cells are of the same color
  public boolean allSameColor() {
    boolean flag = true;
    for (Cell c : cells) {
      if (!(c.isFlooded)) {
        flag = false;
      }
    }
    return flag;
  }
  
  // to simplify connect
  public void connectHelp(int a, int y, int index, Cell p) {
    if (a > y) {
      p = cells.get(index - 1);
    } else {
      p = null;
    }
  }
  
  // to simplify connect
  public void connectHelp1(int a, int y, int index, Cell p) {
    if (a > y) {
      p = cells.get(index - BOARD_SIZE);
    } else {
      p = null;
    }
  }

  
  // make top, left, right & bottom for the Cell3
  public void connect() {
    cells.get(0).isFlooded = true;
   
    for (int index = 0; index <= ((Board.BOARD_SIZE * Board.BOARD_SIZE) - 1); index++) {
      int cellIndex = cells.get(index).x;
      int cellIndexY = cells.get(index).y;
      connectHelp(cellIndex, 0, index, cells.get(index).right);
      connectHelp(cellIndex, 0, index, cells.get(index).left);
      connectHelp1(cells.get(index).y, 0, index, cells.get(index).top);    
      if (cells.get(index).x < BOARD_SIZE - 1) {
        cells.get(index).right = cells.get(index + 1);
      }
      else {
        cells.get(index).right = null;
      }
      if (cells.get(index).y < BOARD_SIZE - 1) {
        cells.get(index).bottom = (cells.get(index + BOARD_SIZE));
      }
      else {
        cells.get(index).bottom = null;
      }
    }
  }
}


class Examples2 {
  // some sample cells
  Cell maincell;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;

  ArrayList<Cell> testList = new ArrayList<Cell>();

  public void TestCellsVoid() {
    maincell = new Cell(9, 8, Color.CYAN, false);
    cell2 = new Cell(90, 90, Color.ORANGE, false);
    cell3 = new Cell(3, 1, Color.GREEN, false);
  }

  // an empty list
  ArrayList<Cell> mtlist = new ArrayList<Cell>();
  Board f2;
  HashMap<Integer, Color> colorArray = new HashMap<Integer, Color>();
  Random rand = new Random();

  void createColors() {
    // here is the list of all possible colors. You can only go up
    // to 8 colors, just like in the online game.
    colorArray.put(7, Color.WHITE);
    colorArray.put(6, Color.BLACK);
    colorArray.put(3, Color.BLUE);
    colorArray.put(1, Color.ORANGE);
    colorArray.put(2, Color.RED);
    colorArray.put(4, new Color(225, 120, 0));
    colorArray.put(5, Color.YELLOW);
    colorArray.put(0, Color.PINK);
  }

  // tests place
  public boolean testDrawCell(Tester t) {
    this.TestCellsVoid();
    return t.checkExpect(maincell.place(Color.BLUE), new RectangleImage(22, 22, "solid",
        Color.BLUE))
        && t.checkExpect(cell2.place(Color.GREEN), new RectangleImage(22, 22, "solid",
            Color.GREEN));
  }

  // test making a random color
  public boolean testRandColor(Tester t) {
    this.createColors();
    new Board(testList, 5, colorArray);
    Color cell = testList.get(0).color;
    Color c2 = testList.get(1).color;
    return t.checkNumRange(cell.getRed(), 0, 256) && t.checkNumRange(c2.getBlue(), 0, 256)
        && t.checkNumRange(c2.getGreen(), 0, 256);
  }

  // test connect
  public boolean testconnect(Tester t) {
    this.createColors();
    new Board(testList, 6, colorArray);
    return t.checkExpect(testList.get(0).x, 0)
        && t.checkExpect(testList.get(Board.BOARD_SIZE + 1).x, 1)
        && t.checkExpect(testList.get(Board.BOARD_SIZE + 3).y, 1);
  }

  // test if all colors being initialized
  public boolean testCreateColors(Tester t) {
    this.createColors();
    new Board(testList, 6, colorArray);
    return t.checkExpect(testList.get(0), testList.get(0));
  }



  // testing randomInt
  boolean testrandomInt(Tester t) {
    return t.checkOneOf("test randomInt", this.rand.nextInt(1), 0, 1, 2, 3, 4, 5, 6)
        && t.checkOneOf("test randomInt", this.rand.nextInt(1), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 11, 12, 13)
            && t.checkNoneOf("test randomInt", this.rand.nextInt(5), 11, 12, 13, 14, 15, 16);
  }
  
  // test succesfulClick
  boolean testSuccesfulClicks(Tester t) {
    this.createColors();
    Board f1 = new Board(testList, 5, colorArray);
    return t.checkExpect(f1.sucessfulClick() - 10, 23)
        && t.checkExpect(f1.sucessfulClick() - 30, 3);
  }

  void initData() {
    this.createColors();
    // 8 colors, no more than 8, no less than 3
    f2 = new Board(mtlist, 7, colorArray);
  }

  void testGame(Tester t) {
    this.initData();
    f2.bigBang((int) Math.pow(Board.BOARD_SIZE, 2), (int) Math.pow(Board.BOARD_SIZE, 2), 0.1);
  }
}