package uk.ac.soton.comp1206.game;

import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {
    private static final Logger logger = LogManager.getLogger(Grid.class);

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    private int previousColor;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Checks whether a GamePiece can be played at a specific location.
     * @param gamePiece game piece input
     * @param x x-coordinate input
     * @param y y-coordinate input
     * @return true if the GamePiece can be played
     */
    public boolean canPlayPiece(GamePiece gamePiece, int x, int y) {
        logger.info("Checking if we can play piece {} at {}, {}", gamePiece,x,y);

        int tempX = x-1;
        int tempY = y-1;

        int[][] blocks = gamePiece.getBlocks();
        for(var blockX=0; blockX < blocks.length; blockX++){
            for(var blockY=0; blockY < blocks.length; blockY++){
                //blockX and blockY coordinate inside the blocks 3x3 array
                var blockValue = blocks[blockX][blockY];
                //only updates the model if there is a colour (value of 1 or more)
                if(blockValue>0){
                    //checks if we can place this block on our grid
                    var gridValue = get(tempX+blockX, tempY+blockY);
                    if(gridValue!=0){
                        //-1 returns out of bounds hence !=0 is used
                        logger.info("Unable to play piece at coordinate {}, {}", x,y);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * allows the user to play a piece by updating the grid with the piece
     * @param gamePiece the gamePiece they want to play
     * @param x x-coordinate
     * @param y y-coordinate
     */

    public void playPiece(GamePiece gamePiece, int x, int y) {
        logger.info("Playing the piece {} at {}, {}", gamePiece,x,y);

        int tempX = x-1;
        int tempY = y-1;

        int color = gamePiece.getValue();
        int[][] blocks = gamePiece.getBlocks();
        if(canPlayPiece(gamePiece,x,y)){
            for(var blockX=0; blockX < blocks.length; blockX++){
                for(var blockY=0; blockY < blocks.length; blockY++){
                    //blockX and blockY coordinate inside the blocks 3x3 array
                    var blockValue = blocks[blockX][blockY];
                    //only updates the model if there is a colour (value of 1 or more)
                    if(blockValue>0){
                        logger.info("succesfully played the piece");
                        set(tempX+blockX, tempY+blockY,color);
                        this.setPreviousColor(color);
                    }

                }
            }
        }
    }

    /**
     * Resets the entire grid back to value=0 all over in every square in the grid
     */
    public void resetGrid(){
        for(int i=0; i<rows; i++){
            for(int j=0; j<cols; j++){
                set(i,j,0);
            }
        }
    }

    /**
     * Adds a temporary box which should resemble the cursor when the user plays with their keyboard
     * @param x x coordinate
     * @param y y coordinate
     */
    public void addTemporaryBox(int x, int y) {
        this.previousColor = grid[x][y].getValue();
        set(x,y,16);
    }

    /**
     * Removes the temporary box which resembles the cursor when the user plays with their keyboard
     * @param x x coordinate
     * @param y y coordinate
     */
    public void removeTemporaryBox(int x, int y) {
        set(x,y,this.previousColor);
    }

    /**
     * A setter method for changing the field variable previousColor;
     * @param color the int color you want to change the field var to
     */
    public void setPreviousColor(int color) {
        this.previousColor= color;
    }

}
