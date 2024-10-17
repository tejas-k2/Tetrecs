package uk.ac.soton.comp1206.scene;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.MultiplayerLeaderboardListener;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class MultiplayerScene extends ChallengeScene implements MultiplayerLeaderboardListener {

  private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
  protected MultiplayerGame game;
  private GameBoard board;
  private Rectangle timerBar;
  private Timeline timeline;
  private Communicator communicator;

  private ObservableList<Pair<String, Pair<Integer, String>>> multiplayerLeaderboard;
  private Timer timer;
  private VBox leaderboardVBox = new VBox();
  private VBox leaderboardScores = new VBox();

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow, Communicator communicator) {
    super(gameWindow);
    this.communicator = communicator;
  }

  /**
   * Build the Challenge window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    game = new MultiplayerGame(5,5, this.communicator);

    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("menu-background");
    root.getChildren().add(challengePane);

    var mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
    mainPane.setCenter(board);

    logger.info("Setting up title contents on game screen...");

    //Creates Score text and Score output
    Label scoreLabel = new Label();
    //binds the text label to the score
    scoreLabel.textProperty().bind(game.getScore());
    scoreLabel.getStyleClass().add("gameItems");
    scoreLabel.setTranslateX(40);
    scoreLabel.setTranslateY(170);

    var scoreText = new Text("Score");
    scoreText.getStyleClass().add("score");
    scoreText.setTranslateX(40);
    scoreText.setTranslateY(130);

    root.getChildren().add(scoreLabel);
    root.getChildren().add(scoreText);

    //Creates Lives text and Lives output
    Label livesLabel = new Label();
    livesLabel.textProperty().bind(game.getLives());
    livesLabel.getStyleClass().add("gameItems");

    Text livesText = new Text("Lives");
    livesText.getStyleClass().add("lives");

    root.getChildren().add(livesLabel);
    root.getChildren().add(livesText);

    //Adds the multiplayer leaderboard to the screen

    //Adds a listener so whenever the multiplayer board listener updates, the MultiplayerScene one does so too
    game.multiplayerLeaderboard().addListener((Observable observable) -> {
      updateLeaderboard(game.getMultiplayerLeaderboard());
    });

    Text leaderboardTitle = new Text("Leaderboard");
    leaderboardTitle.getStyleClass().add("gameItems");

    leaderboardVBox = new VBox();
    leaderboardVBox.setTranslateX(40);
    leaderboardVBox.setTranslateY(220);
    leaderboardVBox.setMaxSize(50, 200);
    leaderboardVBox.getChildren().add(leaderboardTitle);

    root.getChildren().add(leaderboardVBox);

    leaderboardVBox.getChildren().add(leaderboardScores);

    //Creates a timer loop so that the scene keeps searching for new channels every 1 second
    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        refreshLeaderBoard();
      }
    }, 0, 1000);


    //Adds the current and following pieceBoards to the scene
    PieceBoard pieceBoard = game.getPieceBoard();
    PieceBoard followingPieceBoard = game.getFollowingPieceBoard();

    root.getChildren().add(pieceBoard);
    root.getChildren().add(followingPieceBoard);

    VBox rightSideBox = new VBox();
    rightSideBox.setMaxWidth(180);
    rightSideBox.getStyleClass().add("game-right-box");

    VBox innerLivesBox = new VBox();
    innerLivesBox.getChildren().add(livesText);
    innerLivesBox.getChildren().add(livesLabel);

    VBox innerCurrentPieceBox = new VBox();
    Text currentPieceText = new Text("Current Piece");
    currentPieceText.getStyleClass().add("game-currentPieceText");
    innerCurrentPieceBox.getChildren().add(currentPieceText);
    innerCurrentPieceBox.getChildren().add(pieceBoard);

    VBox innerFollowingPieceBox = new VBox();
    Text followingPieceText = new Text("Upcoming Piece");
    followingPieceText.getStyleClass().add("game-currentPieceText");
    innerFollowingPieceBox.getChildren().add(followingPieceText);
    innerFollowingPieceBox.getChildren().add(followingPieceBoard);

    rightSideBox.getChildren().add(innerLivesBox);
    rightSideBox.getChildren().add(innerCurrentPieceBox);
    rightSideBox.getChildren().add(innerFollowingPieceBox);

    innerLivesBox.setAlignment(Pos.CENTER_RIGHT);
    innerCurrentPieceBox.setAlignment(Pos.CENTER_RIGHT);
    innerFollowingPieceBox.setAlignment(Pos.CENTER_RIGHT);

    StackPane.setAlignment(rightSideBox, Pos.CENTER_RIGHT);
    root.getChildren().add(rightSideBox);

    //Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);
    //Handle the following piece board being clicked
    followingPieceBoard.setOnMouseClicked(this::followingBoardClicked);
    //handles the current pieceboard being clicked
    pieceBoard.setOnMouseClicked(this::currentBoardClicked);

    game.setOnGameLoop(this);

    //Creating the timer bar
    timerBar = new Rectangle();
    timerBar.setWidth(gameWindow.getWidth());
    timerBar.setHeight(30);
    timerBar.setFill(Color.GREEN);

    HBox timerBarBox = new HBox();
    timerBarBox.getChildren().add(timerBar);
    root.getChildren().add(timerBarBox);
    timerBarBox.setAlignment(Pos.BASELINE_CENTER);
    timerBarBox.setMaxHeight(30);

    game.setOnGameEnd(this);

  }

  /**
   * refreshes the leaderboard on screen. Adds red text if the player has died. If not, the comparator is used to ensure it always stays in descending order
   */

  public void refreshLeaderBoard() {
    if(multiplayerLeaderboard != null){
      Platform.runLater(() -> {
        logger.info("Refreshing the multiplayer leaderboard");
        leaderboardScores.getChildren().clear();
        for (Pair<String, Pair<Integer, String>> value : multiplayerLeaderboard) {
          HBox playerField = new HBox();
          Text nameField = new Text(value.getKey() + " score:");
          Text scoreField = new Text(value.getValue().getKey() + " lives:");
          Text livesField = new Text(String.valueOf(value.getValue().getValue()));

          if((value.getValue().getValue()).equals("DEAD")) {
            nameField.getStyleClass().add("leaderboardTextPlayerDead");
            scoreField.getStyleClass().add("leaderboardTextPlayerDead");
            livesField.getStyleClass().add("leaderboardTextPlayerDead");
          } else {
            nameField.getStyleClass().add("leaderboardText");
            scoreField.getStyleClass().add("leaderboardText");
            livesField.getStyleClass().add("leaderboardText");
          }

          playerField.getChildren().addAll(nameField, scoreField, livesField);
          leaderboardScores.getChildren().add(playerField);
        }
        this.multiplayerLeaderboard.clear();
      });
    }
  }

  /**
   * Handles when the current piece board is clicked, so it can rotate the piece
   * @param mouseEvent the user click
   */
  void currentBoardClicked(MouseEvent mouseEvent) {
    logger.info("Attempting to rotate the current piece...");
    if (mouseEvent.getButton() == MouseButton.PRIMARY) {
      game.rotateCurrentPieceClockwise();
    }
  }

  /**
   * Handles when the following piece board is clicked so that they swap pieces
   * @param mouseEvent the user click
   */

  void followingBoardClicked(MouseEvent mouseEvent) {
    logger.info("Attempting to swap the current piece with the following piece...");
    if(mouseEvent.getButton() == MouseButton.SECONDARY){
      game.swapCurrentPiece();
    }
  }

  /**
   * Handle when a block is clicked
   * @param gameBlock the Game Block that was clocked
   */
  void blockClicked(GameBlock gameBlock) {
    game.blockClicked(gameBlock);
    Set<GameBlockCoordinate> coords = game.getCoords();
    whenLineCleared(coords);
    game.removeCursor();
  }

  /**
   * Setup the game object and model
   */
  public void setupGame() {
    logger.info("Starting a new challenge");

    //Start new game
    game = new MultiplayerGame(5, 5, communicator);
  }

  /**
   * Initialise the scene and start the game
   */
  @Override
  public void initialise() {
    logger.info("Initialising Challenge");
    game.start();

    (gameWindow.getScene()).setOnKeyPressed(keyEvent -> {
      if(keyEvent.getCode() == KeyCode.Q || keyEvent.getCode() == KeyCode.Z || keyEvent.getCode() == KeyCode.OPEN_BRACKET ) {
        game.rotateCurrentPieceClockwise();
      } else if(keyEvent.getCode() == KeyCode.E || keyEvent.getCode() == KeyCode.C || keyEvent.getCode() == KeyCode.CLOSE_BRACKET ) {
        game.rotateCurrentPieceAnticlockwise();
      } else if(keyEvent.getCode() == KeyCode.ESCAPE) {
        gameWindow.loadScene(new MenuScene(gameWindow));
      } else if(keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode() == KeyCode.R) {
        game.swapCurrentPiece();
      } else if (keyEvent.getCode() == KeyCode.LEFT) {
        game.moveLeft();
      } else if (keyEvent.getCode() == KeyCode.RIGHT) {
        game.moveRight();
      } else if (keyEvent.getCode() == KeyCode.UP) {
        game.moveUp();
      } else if (keyEvent.getCode() == KeyCode.DOWN) {
        game.moveDown();
      } else if (keyEvent.getCode() == KeyCode.ENTER) {
        game.playPieceUsingControl();
        Set<GameBlockCoordinate> coords = game.getCoords();
        whenLineCleared(coords);
      }
    });
  }

  /**
   * fades out all of the coordinates that need to be cleared
   * @param coords the coordinates that need to be cleared
   * @return the coords
   */
  @Override
  public Set<GameBlockCoordinate> whenLineCleared(Set<GameBlockCoordinate> coords) {
    for (GameBlockCoordinate coordinates : coords) {
      GameBlock block = board.getBlock(coordinates.getX(), coordinates.getY());
      if (block != null) {
        block.fadeOut();
      }
    }
    return coords;
  }

  /**
   * the timer bar is set up so that it appears top of screen, starts green then fades yellow then red when the timer runs out
   * @param timerDelay the length of the timer
   */

  @Override
  public void gameLoopOccured(int timerDelay) {
    logger.info("Timer should start decreasing...");
    int delayLength = game.getTimerDelayLength();

    timeline = new Timeline(
        new KeyFrame(Duration.ZERO, new KeyValue(timerBar.widthProperty(), gameWindow.getWidth())),
        new KeyFrame(Duration.millis(delayLength), new KeyValue(timerBar.widthProperty(), 0))
    );
    timeline.setCycleCount(Animation.INDEFINITE);

    Duration yellowColor = Duration.millis((double) delayLength /3);
    Duration redColor = Duration.millis((double) delayLength /2);
    timeline.getKeyFrames().addAll(
        new KeyFrame(yellowColor, new KeyValue(timerBar.fillProperty(), Color.YELLOW)),
        new KeyFrame(redColor, new KeyValue(timerBar.fillProperty(), Color.RED))
    );

    timeline.stop();
    timerBar.setWidth(gameWindow.getWidth()); // Reset width
    timerBar.setFill(Color.GREEN); // Reset color
    timeline.playFromStart();

  }

  @Override
  public void gameEnded() {
    gameWindow.displayScoreScreen(game);
  }


  @Override
  public void updateLeaderboard(ObservableList<Pair<String, Pair<Integer, String>>> leaderboard) {
    this.multiplayerLeaderboard = leaderboard;
  }
}
