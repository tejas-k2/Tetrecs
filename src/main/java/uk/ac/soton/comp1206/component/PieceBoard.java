package uk.ac.soton.comp1206.component;

import javafx.scene.canvas.GraphicsContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;

public class PieceBoard extends GameBoard {

  private static final Logger logger = LogManager.getLogger(PieceBoard.class);

  /**
   * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows,
   * along with the visual width and height.
   *
   * @param cols   number of columns for internal grid
   * @param rows   number of rows for internal grid
   * @param width  the visual width
   * @param height the visual height
   */
  public PieceBoard(int cols, int rows, double width, double height) {
    super(cols, rows, width, height);
  }

  /**
   * Displays the piece that is passed in onto the pieceboard's grid
   * @param piece the game piece that you want to show on screen.
   */
  public void displayPiece(GamePiece piece) {
    grid.resetGrid();
    grid.playPiece(piece, 1, 1);

    GameBlock middleBlock = getBlock(1,1);
    middleBlock.showCircle();

  }

}
