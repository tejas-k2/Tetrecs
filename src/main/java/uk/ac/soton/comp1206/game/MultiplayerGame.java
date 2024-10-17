package uk.ac.soton.comp1206.game;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.GameEndedListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.network.Communicator;

public class MultiplayerGame extends Game {

  Queue<GamePiece> queueOfBlocks = new LinkedList<>();
  private Communicator communicator;
  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
  private Boolean gameStart = true;
  private Boolean gameEnd = false;
  private GameLoopListener gameLoopListener;
  private GameEndedListener gameEndedListener;

  //<Name <Score, Lives>>
  private ListProperty<Pair<String, Pair<Integer, String>>> multiplayerLeaderboard = new SimpleListProperty<>(FXCollections.observableArrayList());
  private Timer timer;

  private Timer timer2;
  private int timerDelayLength;

  //false = singleplayer. true = multiplayer
  private Boolean gameType = true;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public MultiplayerGame(int cols, int rows, Communicator communicator) {
    super(cols, rows);
    this.communicator = communicator;
  }

  /**
   * Initialise a new game and set up anything that needs to be done at the start
   */
  public void initialiseGame() {
    logger.info("Initialising game");

    //Creates a timer loop so that the scene keeps searching for new channels every 1 second
    timer2 = new Timer();
    timer2.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
//        logger.info("Reloading the scores to ensure they are always updated");
        updatingMultiplayerScores();
      }
    }, 0, 1000);

    requestNextPieces();

    playMultiplayerSetupScene();
  }

  /**
   * Sets up the multiplayer scene by playing music and starting the initial timer
   */
  public void playMultiplayerSetupScene(){
    logger.info("Setting up the scene with the music, top score and timer");
    //Play Background music
    multimedia.playBackgroundMusic("/music/game_start.wav");

    //Starts the timer
    startTimer();
  }

  /**
   * Updates the multiplayer scores by sending a communicator request for the scores then adding it to a leaderboard arraylist
   */
  private void updatingMultiplayerScores() {
    logger.info("Sending a communicator request for all player's scores");
    communicator.send("SCORES");
    communicator.addListener((listener) -> Platform.runLater(() -> {
      //If the listener starts with score so the right response is used
      if(listener.startsWith("SCORES")){
        multiplayerLeaderboard.clear();
        String response = listener.substring(7);
        String[] eachPlayer = response.split("\n");
        //splits each player into their name score and lives remaining
        for(String player : eachPlayer) {
          String[] eachPlayerStats = player.split(":");
          String name = eachPlayerStats[0];
          Integer score = Integer.valueOf(eachPlayerStats[1]);
          String lives = eachPlayerStats[2];
          multiplayerLeaderboard.add(new Pair<>(name, new Pair<>(score, lives)));
        }

        //Creating a comparator to sort the leaderboard in terms of the score
        Comparator<Pair<String, Pair<Integer, String>>> comparator = Comparator.comparingInt(e -> e.getValue().getKey());
        comparator = comparator.reversed();
        multiplayerLeaderboard.sort(comparator);
      }
    }));
  }

  /**
   * requests the next piece from the server
   */
  private void requestNextPieces() {
    logger.info("Requesting 10 pieces from the server");
    for(int i=0; i<10; i++){
      communicator.send("PIECE");
    }
    communicator.addListener((listener) -> Platform.runLater(() -> {
      if(listener.startsWith("PIECE")){
        String value = listener.substring(6);
        updatePiecesQueue(value);
      }
    }));
  }

  /**
   * updates the pieces queue using the response from the communicator
   * @param value the communicator's response
   */
  public void updatePiecesQueue(String value) {
    logger.info("Updating the piece queue for the incoming piece with value: {}", value);
    GamePiece gamePiece = GamePiece.createPiece(Integer.parseInt(value));
    queueOfBlocks.add(gamePiece);
    logger.info("value of the piece queue: {}", queueOfBlocks);

    //sets up the scene when the game first starts to ensure there are enough pieces
    if(gameStart){
      if(queueOfBlocks.size() > 9){
        gameStart=false;
        loadFollowingPiece();
        loadNextPiece();
      }
    }
  }

  /**
   * Loads the next piece and places it onto the game's pieceboard
   */
  public void loadNextPiece(){
    logger.info("Loading the next piece");
    currentPiece = followingPiece;
    logger.info("the next piece has color value: {}", currentPiece.getValue());
    super.pieceBoard.displayPiece(currentPiece);
  }

  /**
   * loads the following pieceboard and requests more pieces if run out
   */
  public void loadFollowingPiece(){
    logger.info("Loading the following piece");
    followingPiece = queueOfBlocks.poll();
    super.followingPieceBoard.displayPiece(followingPiece);
    if(queueOfBlocks.size() < 5){
      logger.info("Queue of blocks has fallen below size 5, requesting more pieces");
      requestNextPieces();
    }
  }

  /**
   * Swaps the current piece with the following piece (and vice versa)
   */
  public void swapCurrentPiece() {
    logger.info("Swapping the current piece and the following piece around");
    GamePiece temp = currentPiece;
    currentPiece = followingPiece;
    followingPiece = temp;
    super.pieceBoard.displayPiece(currentPiece);
    super.followingPieceBoard.displayPiece(followingPiece);
    super.multimedia.playAudioFile("/sounds/place.wav");
  }

  /**
   * Increments the score if blocks have been cleared
   * @param numberOfLines the number of lines cleared
   * @param numberofBlocks total number of blocks that have been cleared
   */
  public void score(int numberOfLines, int numberofBlocks){
    logger.info("Recalculating the score");
    if(numberOfLines!=0){
      int newScore = score.get() + (numberOfLines*numberofBlocks*10*(multiplier.get()));
      score.set(newScore);
      communicator.send("SCORE "+score.getValue());
    }
  }

  /**
   * getter method for the leaderboard
   * @return
   */
  public ObservableList<Pair<String, Pair<Integer, String>>> getMultiplayerLeaderboard(){
    return multiplayerLeaderboard.get();
  }

  public ListProperty<Pair<String, Pair<Integer, String>>> multiplayerLeaderboard() {
    return multiplayerLeaderboard;
  }

  public void setMultiplayerLeaderboard(ObservableList<Pair<String, Pair<Integer, String>>> leaderboard) {
    multiplayerLeaderboard.set(leaderboard);
  }

  /**
   * game loop listener for the timer
   * @param listener GameLoopListener
   */
  public void setOnGameLoop(GameLoopListener listener){
    this.gameLoopListener = listener;
  }

  public void setOnGameEnd(GameEndedListener listener){
    this.gameEndedListener = listener;
  }

  /**
   * runs when the timer expires. Removes a life and ends the game if lives have run out
   */
  public void gameLoop(){
    //Platform.runLater() used due to different thread issue
    Platform.runLater(() -> {
      logger.info("Timer Ended! Moving onto gameloop next piece");
      lives.set(lives.get()-1);
      multimedia.playAudioFile("/sounds/lifelose.wav");

      //If the lives have fallen below zero
      if(lives.getValue() < 0){
        communicator.send("DIE");
        logger.info("TRYING TO STOP THE GAME !!!!!!!!!!!!!!!!!!!!!!");

        if(gameEndedListener != null){
          gameEndedListener.gameEnded();
        }

        stopTimer();
        this.endGame();

      } else {
        communicator.send("LIVES "+lives.get());
        multiplier.set(1);
        prepareAfterPiece();
      }
    });
  }

  /**
   * Plays the steps after a click is made. It checks if the piece can be played. If it can, the piece
   * is played and the following pieces are called to be prepared
   * @param x x coord of block click
   * @param y y coord of block click
   */
  public void playPiecePostSteps(int x, int y){
    if(grid.canPlayPiece(currentPiece,x,y)){
      multimedia.playAudioFile("/sounds/pling.wav");
      grid.playPiece(currentPiece, x, y);

      //ensures that when the keys are used, the incorrect color isnt used
      int color = grid.get(super.controlsX, super.controlsY);
      this.grid.setPreviousColor(color);

      afterPiece();
      prepareAfterPiece();

    } else {
      //play something like a sound to denote you cannot play that piece
    }
  }

  /**
   * prepares the next piece by loading the next piece and the following piece and restarts the timer
   */
  public void prepareAfterPiece(){
    //Loads the current piece and the following piece (from the queue of blocks)
    loadNextPiece();
    loadFollowingPiece();
    logger.info("The next piece is: {}", currentPiece);
    logger.info("The following piece is: {}", followingPiece);
    pieceBoard.displayPiece(currentPiece);
    followingPieceBoard.displayPiece(followingPiece);

    restartTimer();

  }

  /**
   * starts the timer. Sets a length for timerDelayLength and if the timer isnt restarted, it calls gameLoop()
   */
  public void startTimer() {
    //Starting the game's timer
    logger.info("Starting the timer");
    timer = new Timer();
    TimerTask timerTask = new TimerTask() {

      @Override
      public void run() {
        gameLoop();
      }
    };

    this.timerDelayLength = getTimerDelay();

    //sets the timerbar
    if(gameLoopListener != null){
      gameLoopListener.gameLoopOccured(this.timerDelayLength);
    }

    //specifies the task and how long after it should run it
    this.timer.schedule(timerTask, this.timerDelayLength);
  }

  /**
   * restarts the timer by cancelling it then restarting it
   */
  public void restartTimer() {
    logger.info("Restarting the timer");
    this.timer.cancel(); // Cancel the existing timer task
    this.timerDelayLength = getTimerDelay(); // Update timer delay length
    startTimer(); // Start a new timer
  }

  /**
   * stops the timer by cancelling it
   */
  public void stopTimer() {
    this.timer.cancel();
  }

  /**
   * getter method for the delay length of the timer
   * @return
   */
  public int getTimerDelayLength(){
    return timerDelayLength;
  }


}