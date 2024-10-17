package uk.ac.soton.comp1206.scene;

import java.security.Key;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.GameEndedListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements LineClearedListener, GameLoopListener,
    GameEndedListener {

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    protected Game game;
    private GameBoard board;
    private Rectangle timerBar;
    private Timeline timeline;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

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

        //Creates highscore text and highscore output
        Label highScoreLabel = new Label();
        highScoreLabel.textProperty().bind(game.getHighScoreProperty());
        highScoreLabel.getStyleClass().add("gameItems");
        highScoreLabel.setTranslateX(40);
        highScoreLabel.setTranslateY(420);

        Text highScoreText = new Text("High Score");
        highScoreText.getStyleClass().add("hiscore");
        highScoreText.setTranslateX(40);
        highScoreText.setTranslateY(390);

        root.getChildren().add(highScoreLabel);
        root.getChildren().add(highScoreText);

        //Creates Level text and Level output
        Label levelLabel = new Label();
        levelLabel.textProperty().bind(game.getLevel());
        levelLabel.getStyleClass().add("gameItems");
        levelLabel.setTranslateX(40);
        levelLabel.setTranslateY(260);

        Text levelText = new Text("Level");
        levelText.getStyleClass().add("level");
        levelText.setTranslateX(40);
        levelText.setTranslateY(220);

        root.getChildren().add(levelLabel);
        root.getChildren().add(levelText);

        //Creates Multiplier text and Multiplier output
        Label multiplierLabel = new Label();
        multiplierLabel.textProperty().bind(game.getMultiplier());
        multiplierLabel.getStyleClass().add("gameItems");
        multiplierLabel.setTranslateX(40);
        multiplierLabel.setTranslateY(340);

        Text multiplierText = new Text("Multiplier");
        multiplierText.getStyleClass().add("multiplier");
        multiplierText.setTranslateX(40);
        multiplierText.setTranslateY(310);

        root.getChildren().add(multiplierLabel);
        root.getChildren().add(multiplierText);

        //Creates Lives text and Lives output
        Label livesLabel = new Label();
        livesLabel.textProperty().bind(game.getLives());
        livesLabel.getStyleClass().add("gameItems");

        Text livesText = new Text("Lives");
        livesText.getStyleClass().add("lives");

        root.getChildren().add(livesLabel);
        root.getChildren().add(livesText);

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
        game = new Game(5, 5);
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
                game.gameClose();
                game = null;
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
     * sets up a timer bar at the top of the screen which is animated to start of green then fade yellow then red when the timer runs out
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

    /**
     * when the game ends the score screen will be displayed
     */
    @Override
    public void gameEnded() {

        gameWindow.displayScoreScreen(game);
    }
}
