package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import javafx.stage.Stage;

public class InstructionScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(InstructionScene.class);

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public InstructionScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  /**
   * Initialise this scene. Called after creation
   */
  @Override
  public void initialise() {
    logger.info("Initialising" + this.getClass().getName());
    Multimedia multimedia = new Multimedia();
    multimedia.playBackgroundMusic("/music/menu.mp3");

    (gameWindow.getScene()).setOnKeyPressed((KeyEvent event) -> {
      if (event.getCode() == KeyCode.ESCAPE){
        //closes the instructions scene and opens the menu screen again
        gameWindow.loadScene(new MenuScene(gameWindow));
      }
    });
  }

  /**
   * Build the layout of the scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
    var instructionsPane = new StackPane();
    instructionsPane.setMaxWidth(gameWindow.getWidth());
    instructionsPane.setMaxHeight(gameWindow.getHeight());

    instructionsPane.getStyleClass().add("instruction-background");
    root.getChildren().add(instructionsPane);

    //outputting all the gamePieces
    HBox gamePieceHBox = new HBox();
    root.getChildren().add(gamePieceHBox);
    gamePieceHBox.setAlignment(Pos.BOTTOM_CENTER);

    for(int i=0; i<15; i++){
      GamePiece gamePiece = GamePiece.createPiece(i);
      PieceBoard pieceBoard = new PieceBoard(3,3,30,30);
      pieceBoard.displayPiece(gamePiece);
      pieceBoard.getStyleClass().add("instructions-blocks");
      gamePieceHBox.getChildren().add(pieceBoard);
    }


  }
}
