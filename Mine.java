import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.*;

//EXTRA CREDIT: Press r to restart the game after winning or losing
// Bottom right displays how many non-mine cells are left to win the game

//array utils class for working with arrays
class ArrayUtils {

  //function for making the grid
  ArrayList<ArrayList<Cell>> makeGrid(int columns, int rows) {
    ArrayList<ArrayList<Cell>> row = new ArrayList<ArrayList<Cell>>(); 

    for (int i = 0; i < columns; i++) {
      Cell currentCell = new Cell(false);
      row.add(currentCell.makeColumn(rows));
    }
    return row;

  }

  //function for getting the neighbors for the cells
  //EFFECT: links cells that are right next to each other by their neighbors
  void getAllNeighbors(int columns, int rows, ArrayList<ArrayList<Cell>> grid) {
    for (int i = 0; i < columns - 1; i++) {
      new ArrayUtils().getNeighbors(grid.get(i), grid.get(i + 1), rows);
    }

    for (int i = 0; i < rows; i++) {
      Cell currentCell = grid.get(columns - 1).get(i);
      if (i > 0) {
        currentCell.neighbors.add(grid.get(columns - 1).get(i - 1));
      }
      if (i < rows - 1) {
        currentCell.neighbors.add(grid.get(columns - 1).get(i + 1));
      }
    }
  }

  // helper for getALLNeighbors
  // EFFECT: links the left and right boxes
  public void getNeighbors(ArrayList<Cell> currentColumn, ArrayList<Cell> nextColumn, int n) {
    for (int i = 0; i < n; i++) {
      Cell leftCell = currentColumn.get(i);
      leftCell.neighbors.add(nextColumn.get(i));
      if (i < n - 1) {
        leftCell.neighbors.add(nextColumn.get(i + 1));
        leftCell.neighbors.add(currentColumn.get(i + 1));
      }
      if (i != 0) {
        leftCell.neighbors.add(nextColumn.get(i - 1));
        leftCell.neighbors.add(currentColumn.get(i - 1));
      }
      Cell rightCell = nextColumn.get(i);
      rightCell.neighbors.add(currentColumn.get(i));
      if (i < n - 1) {
        rightCell.neighbors.add(currentColumn.get(i + 1));
      }

      if (i != 0) {
        rightCell.neighbors.add(currentColumn.get(i - 1));
      }
    }
  }
}

//represents a cell in the game Mine Sweeper
class Cell {
  Boolean bomb;
  Boolean flagged;
  ArrayList<Cell> neighbors;
  Boolean visited;
  WorldImage image;

  //initializes a cell
  Cell(Boolean bomb) {
    this.bomb = bomb;
    this.flagged = false;
    this.neighbors = new ArrayList<Cell>();
    this.image = new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray);
    this.visited = false;
  }

  //counts the number of neighbors that are mines
  int countMines() {

    int mines = 0;

    for (Cell c : neighbors) {
      if (c.bomb) {
        mines = mines + 1;
      }
    }
    return mines;
  }

  //enacts the flood fill effect on this mine
  //EFFECT: the image is changed if this cell hasn't been visited and this cell is visited
  //function is also called on potential neighbors as well
  int floodFill() {
    int counter = 0;
    if (!this.flagged) {

      counter += 1;
      this.visited = true;
      if (this.bomb) {
        this.drawMine();
        return -1;
      }
      else if (this.countMines() == 0) {
        this.image = new RectangleImage(25, 25, OutlineMode.SOLID, 
            Color.DARK_GRAY);
        for (Cell c: this.neighbors) {
          if (!c.bomb && !c.visited) {
            counter += c.floodFill();
          }
        }
      }
      else if (this.countMines() > 0) {
        this.drawNumber();
      }
    }
    return counter;
  }


  //makes a column or cells that are n cells long
  ArrayList<Cell> makeColumn(int n) {
    ArrayList<Cell> column = new ArrayList<Cell>();
    column.add(this);

    for (int i = 0; i < n - 1; i++) {
      column.add(new Cell(false));
    }
    return column;
  }

  //draws the flag cell
  //EFFECT: changes the image in the cell
  void drawFlag() {
    WorldImage flag = new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.YELLOW);

    WorldImage flagcell = new OverlayImage(flag,  
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    if (!this.flagged) {
      this.flagged = true;
      this.image = flagcell;
    }
    else if (this.flagged) {
      this.flagged = false;
      this.image = new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray);
    }
  }

  // draws the mine cell
  //EFFECT: changes the image in the cell
  void drawMine() {
    WorldImage mine = new CircleImage(10, OutlineMode.SOLID, Color.RED);
    WorldImage minecell = new OverlayImage(mine, this.image);
    this.image = minecell;
  }

  // draws the number cells
  //EFFECT: changes the image in the cell
  void drawNumber() {

    Color color = Color.GRAY;
    int number = this.countMines();

    if (number == 1) {
      color = Color.BLUE;
    }
    else if (number == 2) {
      color = Color.GREEN;
    }
    else if (number == 3) {
      color = Color.RED;
    }
    else if (number == 4) {
      color = Color.CYAN;
    }
    else if (number == 5) {
      color = Color.MAGENTA;
    }
    else if (number == 6) {
      color = Color.PINK;
    }
    else if (number == 7) {
      color = Color.ORANGE;
    }
    else if (number == 8) {
      color = Color.WHITE;
    }

    WorldImage numberdisplay = new TextImage(Integer.toString(number), 15, FontStyle.BOLD, color);
    WorldImage numbercell = new OverlayImage(numberdisplay, this.image);
    this.image = numbercell;     
  }
}


//class to represent the Mine Sweeper game
class MineSweeper extends World {
  int rows;
  int columns;
  int mines;
  ArrayList<ArrayList<Cell>> grid;
  Random seed;
  boolean lost;
  int squaresLeft;
  boolean won;


  //initalizes minesweeper
  MineSweeper(int rows, int columns, int mines, Random seed) {
    if (mines >= rows * columns) {
      throw new IllegalArgumentException("Must have more squares than mines");
    }
    this.rows = rows;
    this.columns = columns;
    this.mines = mines;
    this.seed = seed;
    this.lost = false;
    this.won = false;
    this.squaresLeft = (rows * columns) - mines;
    this.grid = new ArrayUtils().makeGrid(columns, rows);
    new ArrayUtils().getAllNeighbors(columns, rows, this.grid);
    this.placeMines();

  }


  //function for randomly placing mines on the grid
  //EFFECT, adds mins to the grid
  void placeMines() {
    Random rand = this.seed;
    int minesLeft = this.mines;

    while (minesLeft > 0) {
      int randomRow = rand.nextInt(this.rows);
      int randomColumn = rand.nextInt(this.columns);
      ArrayList<Integer> keys = new ArrayList<Integer>();

      //randomColumn will never be greater than this.columns, thus the key will always be unique
      int key = randomRow * this.columns + randomColumn;

      if (!keys.contains(key)) {
        keys.add(key);
        this.grid.get(randomColumn).get(randomRow).bomb = true;
        minesLeft -= 1;
      }
    }
  }

  //draws the scene with the grid
  public WorldScene makeScene() {
    WorldScene start = this.getEmptyScene();
    for (ArrayList<Cell> column : grid) {
      int x = this.grid.indexOf(column) * 25 + (25 / 2);
      for (Cell cell : column) {
        int y = column.indexOf(cell) * 25 + (25 / 2);
        start.placeImageXY(cell.image, x, y); 
        start.placeImageXY(new RectangleImage(25, 25, OutlineMode.OUTLINE, Color.black), x, y);
      }
    }
    if (this.lost) {
      start.placeImageXY(new TextImage("Game Over! Press r to restart", 50, 
          FontStyle.BOLD, Color.red), 
          450, 200);
    }
    else if (this.won) {
      start.placeImageXY(new TextImage("You Won! Press r to restart", 50,
          FontStyle.BOLD, Color.blue), 
          450, 200);
    }
    start.placeImageXY(new TextImage(this.squaresLeft + " squares left", 20, 
        FontStyle.BOLD, Color.green), 
        800, 460);
    return start;
  }

  //EFFECT: implements the clicking functions in the MineSweeper game, 
  //if the left button is clicked, floodfill is enabled,
  //and if the user rightclicks they can flag and unflag cells. 
  public void onMouseClicked(Posn pos, String buttonName) {
    int x = (int)Math.floor(pos.x / 25);
    int y = (int)Math.floor(pos.y / 25);
    if (x < this.columns && y < this.rows) {

      Cell cell = this.grid.get(x).get(y);
      if (buttonName.equals("LeftButton") && !cell.flagged && !cell.visited 
          && !this.lost && !this.won) {
        int counter = cell.floodFill(); 
        if (counter == -1) {
          this.lost = true;
        }
        this.squaresLeft -= counter;
        if (this.squaresLeft <= 0) {
          this.won = true;
        }
      }
      if (buttonName.equals("RightButton") && !cell.visited
          && !this.lost && !this.won) {
        cell.drawFlag();
      }
    }
  }

  //EFFECT:restarts the game only if the the r key is pressed and if the game has ended. 
  public void onKeyEvent(String key) {
    if (key.equals("r") && (this.lost || this.won)) {
      this.seed = new Random();
      this.lost = false;
      this.won = false;
      this.squaresLeft = (rows * columns) - mines;
      this.grid = new ArrayUtils().makeGrid(columns, rows);
      new ArrayUtils().getAllNeighbors(columns, rows, grid);
      this.placeMines();
    }
  }

}

//class for holding the mine sweeper examples 
class MineSweeperExamples {

  Random random1;
  Random random2;
  Random random3;

  Cell cell1;
  Cell cell2;
  Cell cell3;
  ArrayList<Cell> column1;
  ArrayList<Cell> column11;
  ArrayList<Cell> column2;
  ArrayList<Cell> column3;
  ArrayList<Cell> column4;
  ArrayList<ArrayList<Cell>> grid1;
  ArrayList<ArrayList<Cell>> grid2;
  ArrayList<ArrayList<Cell>> grid3;
  ArrayList<ArrayList<Cell>> grid4;
  ArrayList<ArrayList<Cell>> grid5;
  ArrayList<ArrayList<Cell>> grid6;
  ArrayList<ArrayList<Cell>> grid7;

  MineSweeper game0;
  MineSweeper game1;
  MineSweeper game2;
  MineSweeper game3;
  MineSweeper game4;
  MineSweeper game5;
  ArrayUtils au;

  WorldImage cell;
  WorldImage flag;
  WorldImage flagcell;
  WorldImage mine;
  WorldImage minecell;
  WorldImage numberdisplay1;
  WorldImage numberdisplay2;
  WorldImage numberdisplay3;
  WorldImage numberdisplay0;
  WorldImage numbercell1;
  WorldImage numbercell2;
  WorldImage numbercell3;
  WorldImage numbercell0;

  //function to initialize the data
  void initData() {
    cell1 = new Cell(false);
    cell2 = new Cell(true);
    cell3 = new Cell(false);
    column1 = new ArrayList<Cell>(Arrays.asList(new Cell(false), 
        new Cell(false), new Cell(false), new Cell(false)));
    column11 = new ArrayList<Cell>(Arrays.asList(new Cell(true), 
        new Cell(false), new Cell(true), new Cell(false)));
    column2 = new ArrayList<Cell>(Arrays.asList(new Cell(true), new Cell(false),
        new Cell(false), new Cell(false),
        new Cell(false), new Cell(false), new Cell(false), new Cell(false)));
    column3 = new ArrayList<Cell>(Arrays.asList(new Cell(false), new Cell(false), new Cell(false)));


    random1 = new Random(2);

    grid1 = new ArrayList<ArrayList<Cell>>(Arrays.asList(
        this.column1, 
        this.column1, 
        this.column1));
    grid2 = new ArrayList<ArrayList<Cell>>(Arrays.asList(
        this.column2, 
        this.column2, 
        this.column2,
        this.column2));
    grid3 = new ArrayList<ArrayList<Cell>>(Arrays.asList(
        this.column3, 
        this.column3, 
        this.column3));
    game0 = new MineSweeper(4, 5, 0, random1);
    game1 = new MineSweeper(16, 36, 5, random1);
    game2 = new MineSweeper(4, 4, 4, random1);
    game3 = new MineSweeper(4, 4, 2, random1);
    game4 = new MineSweeper(8, 8, 4, random1);
    game5 = new MineSweeper(16, 36, 50, new Random());
    game3.grid.get(0).get(0).bomb = false;
    game3.grid.get(2).get(3).bomb = false;
    game3.grid.get(2).get(2).bomb = true;
    game3.grid.get(3).get(1).bomb = true;
    au = new ArrayUtils();
    grid4 = au.makeGrid(4, 5);
    grid5 = au.makeGrid(5, 4);
    grid6 = au.makeGrid(4, 4);
    this.game3.grid.get(0).get(0).image = new RectangleImage(25, 25, OutlineMode.SOLID, 
        Color.DARK_GRAY);
    this.game3.grid.get(0).get(1).image = new RectangleImage(25, 25, OutlineMode.SOLID, 
        Color.DARK_GRAY);
    this.game3.grid.get(0).get(2).image = new RectangleImage(25, 25, OutlineMode.SOLID, 
        Color.DARK_GRAY);
    this.game3.grid.get(0).get(3).image = new RectangleImage(25, 25, OutlineMode.SOLID, 
        Color.DARK_GRAY);
    this.game3.grid.get(1).get(1).image = new OverlayImage(new TextImage("" 
        + 
        this.game3.grid.get(1).get(1).countMines() , 10, Color.BLUE),
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    this.game3.grid.get(1).get(2).image = new OverlayImage(new TextImage("" 
        + 
        this.game3.grid.get(1).get(2).countMines() , 10, Color.BLUE),
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    this.game3.grid.get(1).get(3).image = new OverlayImage(new TextImage("" 
        + 
        this.game3.grid.get(1).get(3).countMines() , 10, Color.BLUE),
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    this.game3.grid.get(2).get(3).image = new OverlayImage(new TextImage("" 
        + 
        this.game3.grid.get(2).get(3).countMines() , 10, Color.BLUE),
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    this.game3.grid.get(3).get(3).image = new OverlayImage(new TextImage("" 
        + 
        this.game3.grid.get(3).get(3).countMines() , 10, Color.BLUE),
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    this.game3.grid.get(3).get(2).image = new OverlayImage(new TextImage("" 
        + 
        this.game3.grid.get(3).get(2).countMines() , 10, Color.BLUE),
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    this.game3.grid.get(3).get(0).image = new OverlayImage(new TextImage("" 
        + 
        this.game3.grid.get(3).get(0).countMines() , 10, Color.BLUE),
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    this.game3.grid.get(2).get(0).image = new OverlayImage(new TextImage("" 
        + 
        this.game3.grid.get(2).get(0).countMines() , 10, Color.BLUE),
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    this.game3.grid.get(2).get(1).image = new OverlayImage(new TextImage("" 
        + 
        this.game3.grid.get(2).get(1).countMines() , 10, Color.BLUE),
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    this.game3.grid.get(1).get(0).image = new RectangleImage(25, 25, 
        OutlineMode.SOLID, Color.DARK_GRAY);

    cell = new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray);
    flag = new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.YELLOW);
    flagcell = new OverlayImage(flag, cell);

    mine = new CircleImage(10, OutlineMode.SOLID, Color.RED);
    minecell = new OverlayImage(mine, cell);

    numberdisplay1 = new TextImage("1", 15, Color.BLUE);
    numbercell1 = new OverlayImage(numberdisplay1, cell);

    numberdisplay2 = new TextImage("2", 15, Color.GREEN);
    numbercell1 = new OverlayImage(numberdisplay2, cell);

    numberdisplay3 = new TextImage("3", 15, Color.RED);
    numbercell1 = new OverlayImage(numberdisplay3, cell);

    numberdisplay0 = new TextImage("0", 15, Color.GRAY);
    numbercell1 = new OverlayImage(numberdisplay0, cell);
  }

  void testmakeColumn(Tester t) {
    this.initData();
    t.checkExpect(this.cell1.makeColumn(4), this.column1);
    t.checkExpect(this.cell2.makeColumn(8), this.column2);
    t.checkExpect(this.cell1.makeColumn(3), this.column3);

  }

  void testmakeGrid(Tester t) {
    this.initData();
    this.column2.get(0).bomb = false;
    t.checkExpect(this.au.makeGrid(3, 4), this.grid1);
    t.checkExpect(this.au.makeGrid(4, 8), this.grid2);
    t.checkExpect(this.au.makeGrid(3, 3), this.grid3);
  }

  void testGetNeighbors(Tester t) {
    this.initData();
    this.au.getNeighbors(this.column1, this.column11, 4);
    t.checkExpect(this.column1.get(0).neighbors.size(), 3);
    t.checkExpect(this.column1.get(2).neighbors.size(), 5);
    t.checkExpect(this.column1.get(2).neighbors.contains(this.column1.get(1))
        && this.column1.get(2).neighbors.contains(this.column1.get(3))
        && this.column1.get(2).neighbors.contains(this.column11.get(2))
        && this.column1.get(2).neighbors.contains(this.column11.get(1))
        && this.column1.get(2).neighbors.contains(this.column11.get(3)), true);
    t.checkExpect(this.column11.get(0).neighbors.size(), 2);
    t.checkExpect(this.column11.get(1).neighbors.size(), 3);
    t.checkExpect(this.column11.get(1).neighbors.contains(this.column1.get(0))
        && this.column11.get(1).neighbors.contains(this.column1.get(1))
        && this.column11.get(1).neighbors.contains(this.column1.get(2)), true);
  }

  void testPlaceMines(Tester t) {
    this.initData();
    this.au.getAllNeighbors(5, 4, this.grid5);
    this.au.getAllNeighbors(4, 4, this.grid6);
    t.checkExpect(this.game2.grid.get(1).get(3).bomb, true);
    t.checkExpect(this.game2.grid.get(2).get(0).bomb, true);
    t.checkExpect(this.game2.grid.get(3).get(0).bomb, true);
    t.checkExpect(this.game2.grid.get(0).get(2).bomb, true);
    this.grid6.get(1).get(3).bomb = true;
    this.grid6.get(2).get(0).bomb = true;
    this.grid6.get(3).get(0).bomb = true;
    this.grid6.get(0).get(2).bomb = true;
    t.checkExpect(this.game2.grid, this.grid6);
  }

  boolean tesCountMines(Tester t) {
    this.initData();
    return t.checkExpect(this.grid6.get(1).get(2).countMines(), 2)
        && t.checkExpect(this.grid6.get(0).get(1).countMines(), 1)
        && t.checkExpect(this.grid6.get(0).get(0).countMines(), 0)
        && t.checkExpect(this.grid6.get(1).get(3).countMines(), 2);
  }

  // test for drawFlag method
  void testdrawFlag(Tester t) {
    this.initData();
    this.game2.grid.get(0).get(3).drawFlag();
    t.checkExpect(this.game2.grid.get(0).get(2).flagged, false);
    t.checkExpect(this.game2.grid.get(0).get(3).image, new OverlayImage(
        new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.YELLOW), 
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray)));   
  }
  
  boolean testConstructorException(Tester t) {
    this.initData();
    return t.checkConstructorException(
        new IllegalArgumentException(
            "Must have more squares than mines"), "MineSweeper", 5, 5, 25, new Random())
    && t.checkConstructorException(
        new IllegalArgumentException(
            "Must have more squares than mines"), "MineSweeper", 5, 5, 30, new Random());
  }

  // test for drawMine method
  void testdrawMine(Tester t) {
    this.initData();
    this.cell2.drawMine();

    t.checkExpect(this.cell2.image, new OverlayImage(
        new CircleImage(10, OutlineMode.SOLID, Color.RED), 
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray)));
  }

  // test for drawNumber method
  void testdrawNumber(Tester t) {
    this.initData();
    this.game2.grid.get(1).get(0).drawNumber();
    this.game2.grid.get(3).get(1).drawNumber();
    this.game2.grid.get(1).get(2).bomb = true;
    this.game2.grid.get(0).get(3).drawNumber();


    t.checkExpect(this.game2.grid.get(1).get(0).image, new OverlayImage(
        new TextImage("1", 15, FontStyle.BOLD, Color.BLUE), 
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray)));

    t.checkExpect(this.game2.grid.get(3).get(1).image, new OverlayImage(
        new TextImage("2", 15, FontStyle.BOLD, Color.GREEN), 
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray)));

    t.checkExpect(this.game2.grid.get(0).get(3).image, new OverlayImage(
        new TextImage("3", 15, FontStyle.BOLD, Color.RED), 
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray)));
  }


  void testMakeScene(Tester t) {
    this.initData();


    WorldScene temp = new WorldScene(0, 0);

    temp.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.DARK_GRAY), 0, 0);
    temp.placeImageXY(this.numberdisplay1, 25, 0);
    temp.placeImageXY(this.cell, 50, 0);
    temp.placeImageXY(this.cell, 75, 0);
    temp.placeImageXY(this.numberdisplay1, 0, 25);
    temp.placeImageXY(this.numberdisplay2, 25, 25);
    temp.placeImageXY(this.numberdisplay2, 50, 25);
    temp.placeImageXY(this.numberdisplay2, 75, 25);
    temp.placeImageXY(this.cell, 0, 50);
    temp.placeImageXY(this.numberdisplay2, 25, 50);
    temp.placeImageXY(this.numberdisplay1, 50, 50);
    temp.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.DARK_GRAY), 75, 50);
    temp.placeImageXY(this.cell, 0, 75);
    temp.placeImageXY(this.flagcell, 25, 75);
    temp.placeImageXY(this.numberdisplay1, 50, 75);
    temp.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.DARK_GRAY), 75, 75);
    temp.placeImageXY(new TextImage(571 + " squares left", 20, FontStyle.BOLD, Color.green), 
        800, 460);

    t.checkExpect(this.game2.makeScene(), 
        temp);

    WorldScene temp1 = new WorldScene(0, 0);

    temp1.placeImageXY(this.cell, 25, 0);
    temp1.placeImageXY(this.cell, 25, 25);
    temp1.placeImageXY(this.cell, 25, 50);
    temp1.placeImageXY(this.cell, 25, 75);
    temp1.placeImageXY(this.cell, 50, 0);
    temp1.placeImageXY(this.cell, 50, 25);
    temp1.placeImageXY(this.cell, 50, 50);
    temp1.placeImageXY(this.cell, 50, 75);
    temp1.placeImageXY(this.cell, 75, 0);
    temp1.placeImageXY(this.cell, 75, 25);
    temp1.placeImageXY(this.cell, 75, 50);
    temp1.placeImageXY(this.cell, 75, 75);
    temp1.placeImageXY(this.cell, 100, 0);
    temp1.placeImageXY(this.cell, 100, 25);
    temp1.placeImageXY(this.cell, 100, 50);
    temp1.placeImageXY(this.cell, 100, 75);
    temp1.placeImageXY(new TextImage(20 + " squares left", 20, FontStyle.BOLD, Color.green), 
        800, 460);

    t.checkExpect(this.game0.makeScene(), 
        temp1);
  }



  void testGetAllNeighbors(Tester t) {
    this.initData();
    this.au.getAllNeighbors(4, 5, this.grid4);
    t.checkExpect(this.grid4.get(2).get(3).neighbors.size(), 8);
    Cell c23 = this.grid4.get(2).get(3);
    t.checkExpect(c23.neighbors.contains(this.grid4.get(2).get(2))
        && c23.neighbors.contains(this.grid4.get(2).get(4))
        && c23.neighbors.contains(this.grid4.get(3).get(2))
        && c23.neighbors.contains(this.grid4.get(3).get(3))
        && c23.neighbors.contains(this.grid4.get(3).get(4))
        && c23.neighbors.contains(this.grid4.get(1).get(2))
        && c23.neighbors.contains(this.grid4.get(1).get(3))
        && c23.neighbors.contains(this.grid4.get(1).get(4)), true);
    Cell c45 = this.grid4.get(3).get(4);
    t.checkExpect(c45.neighbors.size(), 3);
    t.checkExpect(c45.neighbors.contains(this.grid4.get(3).get(3))
        && c45.neighbors.contains(this.grid4.get(2).get(4))
        && c45.neighbors.contains(this.grid4.get(2).get(3)), true);
    Cell c00 = this.grid4.get(0).get(0);
    t.checkExpect(c00.neighbors.size(), 3);
    t.checkExpect(c00.neighbors.contains(this.grid4.get(0).get(1))
        && c00.neighbors.contains(this.grid4.get(1).get(0))
        && c00.neighbors.contains(this.grid4.get(1).get(1)), true);
  }

  void testFloodFill(Tester t) {
    this.initData();
    t.checkExpect(this.game2.grid.get(3).get(3).visited, false);
    t.checkExpect(this.game2.grid.get(3).get(3).image, 
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    //tests that the image, and visited field are mutated for cell
    //and that function is also called on 5 other cells
    t.checkExpect(this.game2.grid.get(3).get(3).floodFill(), 6);
    t.checkExpect(this.game2.grid.get(3).get(3).image, 
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.DARK_GRAY));
    t.checkExpect(this.game2.grid.get(3).get(3).visited, true);
    t.checkExpect(this.game2.grid.get(1).get(3).visited, false);
    t.checkExpect(this.game2.grid.get(1).get(3).image, 
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    t.checkExpect(this.game2.grid.get(1).get(3).floodFill(), -1);
    t.checkExpect(this.game2.grid.get(1).get(3).visited, true);
    t.checkExpect(this.game2.grid.get(1).get(3).image, 
        new OverlayImage(mine, cell));
  }

  void testOnMouse(Tester t) {
    this.initData();
    t.checkExpect(this.game2.squaresLeft, 12);
    this.game2.onMouseClicked(new Posn(600, 800), "LeftButton");
    t.checkExpect(this.game2.squaresLeft, 12);
    t.checkExpect(this.game2.grid.get(3).get(3).visited, false);
    t.checkExpect(this.game2.grid.get(3).get(3).image, 
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.gray));
    this.game2.onMouseClicked(new Posn(95, 95), "LeftButton");
    t.checkExpect(this.game2.squaresLeft, 6);
    t.checkExpect(this.game2.grid.get(3).get(3).image, 
        new RectangleImage(25, 25, OutlineMode.SOLID, Color.DARK_GRAY));
    t.checkExpect(this.game2.grid.get(3).get(3).visited, true);
    t.checkExpect(this.game2.grid.get(3).get(1).flagged, false);
    t.checkExpect(this.game2.lost && this.game2.won, false);
    this.game2.onMouseClicked(new Posn(30, 90), "RightButton");
    this.game2.onMouseClicked(new Posn(31, 98), "LeftButton");
    t.checkExpect(this.game2.squaresLeft, 6);
    t.checkExpect(this.game2.grid.get(1).get(3).image, 
        new OverlayImage(flag, cell));

    this.game1.onMouseClicked(new Posn(10,10), "LeftButton");

    WorldScene temp = new WorldScene(0, 0);

    temp.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.DARK_GRAY), 0, 0);
    temp.placeImageXY(this.cell, 25, 0);
    temp.placeImageXY(this.numberdisplay1, 50, 0);
    temp.placeImageXY(this.cell, 75, 0);
    temp.placeImageXY(this.cell, 0, 25);
    temp.placeImageXY(this.numberdisplay1, 25, 25);
    temp.placeImageXY(this.numberdisplay2, 50, 25);
    temp.placeImageXY(this.cell, 75, 25);
    temp.placeImageXY(this.cell, 0, 50);
    temp.placeImageXY(this.cell, 25, 50);
    temp.placeImageXY(this.cell, 50, 50);
    temp.placeImageXY(this.cell, 75, 50);
    temp.placeImageXY(this.cell, 0, 75);
    temp.placeImageXY(this.cell, 25, 75);
    temp.placeImageXY(this.cell, 50, 75);
    temp.placeImageXY(this.cell, 75, 75);

    t.checkExpect(this.game2.makeScene(), 
        temp);

    this.initData();

    this.game1.onMouseClicked(new Posn(100,10), "LeftButton");

    WorldScene temp2 = new WorldScene(0, 0);

    temp.placeImageXY(this.cell, 0, 0);
    temp.placeImageXY(this.cell, 25, 0);
    temp.placeImageXY(this.cell, 50, 0);
    temp.placeImageXY(this.minecell, 75, 0);
    temp.placeImageXY(this.cell, 0, 25);
    temp.placeImageXY(this.cell, 25, 25);
    temp.placeImageXY(this.cell, 50, 25);
    temp.placeImageXY(this.cell, 75, 25);
    temp.placeImageXY(this.cell, 0, 50);
    temp.placeImageXY(this.cell, 25, 50);
    temp.placeImageXY(this.cell, 50, 50);
    temp.placeImageXY(this.cell, 75, 50);
    temp.placeImageXY(this.cell, 0, 75);
    temp.placeImageXY(this.cell, 25, 75);
    temp.placeImageXY(this.cell, 50, 75);
    temp.placeImageXY(this.cell, 75, 75);

    t.checkExpect(this.game2.makeScene(), 
        temp2);

    this.initData();

    this.game1.onMouseClicked(new Posn(10,10), "RightButton");

    WorldScene temp3 = new WorldScene(0, 0);

    temp.placeImageXY(this.flagcell, 0, 0);
    temp.placeImageXY(this.cell, 25, 0);
    temp.placeImageXY(this.cell, 50, 0);
    temp.placeImageXY(this.cell, 75, 0);
    temp.placeImageXY(this.cell, 0, 25);
    temp.placeImageXY(this.cell, 25, 25);
    temp.placeImageXY(this.cell, 50, 25);
    temp.placeImageXY(this.cell, 75, 25);
    temp.placeImageXY(this.cell, 0, 50);
    temp.placeImageXY(this.cell, 25, 50);
    temp.placeImageXY(this.cell, 50, 50);
    temp.placeImageXY(this.cell, 75, 50);
    temp.placeImageXY(this.cell, 0, 75);
    temp.placeImageXY(this.cell, 25, 75);
    temp.placeImageXY(this.cell, 50, 75);
    temp.placeImageXY(this.cell, 75, 75);

    t.checkExpect(this.game2.makeScene(), 
        temp3); 
  }

  void testOnKey(Tester t) {
    this.initData();
    this.au.getAllNeighbors(4, 4, this.grid6);
    this.grid6.get(1).get(3).bomb = true;
    this.grid6.get(2).get(0).bomb = true;
    this.grid6.get(3).get(0).bomb = true;
    this.grid6.get(0).get(2).bomb = true;
    t.checkExpect(this.game2.grid, this.grid6);
    this.game2.onKeyEvent("r");
    t.checkExpect(this.game2.grid, this.grid6);
    this.game2.lost = true;
    this.game2.onKeyEvent("c");
    t.checkExpect(this.game2.grid, this.grid6);
    this.game2.onKeyEvent("r");
    t.checkExpect(this.game2.lost, false);
    this.game2.won = true;
    this.game2.onKeyEvent("r");
    t.checkExpect(this.game2.won, false);

  }


  void testBigBang(Tester t) {
    this.initData();
    this.game2.grid.get(3).get(3).image = new RectangleImage(25, 25, 
        OutlineMode.SOLID, Color.DARK_GRAY);
    this.game2.grid.get(3).get(2).image = new RectangleImage(25, 25, 
        OutlineMode.SOLID, Color.DARK_GRAY);
    this.game2.grid.get(2).get(3).drawNumber();
    this.game2.grid.get(2).get(2).drawNumber();
    this.game2.grid.get(2).get(1).drawNumber();
    this.game2.grid.get(3).get(1).drawNumber();
    this.game2.grid.get(1).get(2).drawNumber();
    this.game2.grid.get(1).get(1).drawNumber();
    this.game2.grid.get(0).get(1).drawNumber();
    this.game2.grid.get(1).get(0).drawNumber();
    this.game2.grid.get(1).get(3).flagged = true;
    this.game2.grid.get(1).get(3).drawFlag();
    this.game2.grid.get(0).get(0).image = new RectangleImage(25, 25,
        OutlineMode.SOLID, Color.DARK_GRAY);
    this.initData();
    this.game5.bigBang(900, 500);
  }

}
