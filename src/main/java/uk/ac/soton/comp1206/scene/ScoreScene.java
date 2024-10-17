package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.util.Comparator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class ScoreScene extends BaseScene {
  private static final Logger logger = LogManager.getLogger(ScoreScene.class);
  private Game finalGameState;
  private ObservableList<Pair<String, Integer>> observableListOfScores;
  private ListProperty<Pair<String, Integer>> localScoresProperty;
  private ArrayList<Pair<String, Integer>> listOfScores;
  private ObservableList<Pair<String, Integer>> remoteScores = FXCollections.observableArrayList();
  private Communicator communicator;
  private ScoresList scoresList;
  private VBox topScores;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed
   * @param gameWindow the game window
   */
  public ScoreScene(GameWindow gameWindow, Game game, Communicator communicator) {
    super(gameWindow);
    this.finalGameState = game;
    this.communicator = communicator;

    //Initialising the scores array list

    scoresList = new ScoresList();

  }

  /**
   * Initialise this scene. Called after creation
   */
  @Override
  public void initialise() {
    logger.info("Initialising" + this.getClass().getName());
    Multimedia multimedia = new Multimedia();
    multimedia.playBackgroundMusic("/music/end.wav");

    //Loads the online scores and adds them all to the remoteScores ArrayList
    loadOnlineScores();

  }

  /**
   * Build the layout of the scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

    listOfScores = new ArrayList<>();
    observableListOfScores = FXCollections.observableList(listOfScores);
    localScoresProperty = new SimpleListProperty<>(observableListOfScores);

    scoresList.getScoreProperty().bind(localScoresProperty);

    //Loads the local scores
    loadScores();

    var menuPane = new StackPane();
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);

    topScores = new VBox();

    root.getChildren().add(scoresList);
    root.getChildren().add(topScores);

  }

  /**
   * Loads the local scores by reading from the file at the already known path then adds it to the
   * list of scores array (each pair that is read)
   */
  public void loadScores(){
    logger.info("Attempting to load scores...");
    try{

      //Setting up the file reader
      String filePath = "src/main/resources/scoresCollection.txt";
      File file = new File(filePath);
      Reader reader = new FileReader(file);
      BufferedReader reader1 = new BufferedReader(reader);

      String line = "";
      listOfScores.clear();

      //reading the file and adding the new pairs into the listOfScores array
      for(int i=0; i<10; i++) {
        line = reader1.readLine();
        String[] divide = line.split(":");
        String name = divide[0];
        int score = Integer.parseInt(divide[1]);
        listOfScores.add(new Pair<String, Integer>(name, score));
      }

      //creates a comparator to sort the list of scores in descending order
      Comparator<Pair<String, Integer>> comparator = Comparator.comparingInt(Pair::getValue);
      comparator = comparator.reversed();
      listOfScores.sort(comparator);

      reader1.close();

      observableListOfScores = FXCollections.observableArrayList(listOfScores);
      localScoresProperty = new SimpleListProperty<Pair<String, Integer>>(observableListOfScores);


    } catch (IOException e) {
      logger.error("An error has occurred when loading the scores file {}", e.getMessage());
    }
  }

  /**
   * Writes the scores to the local scores file at the path already known. All elements are added
   * again even if the element is already in the scores file.
   * @param scores a list of the scores that will be written to the file
   */
  public void writeScore(List<Pair<String, Integer>> scores){
    logger.info("Attempting to write scores...");
    try {
        //Setting up the writer to write
        String filePath = "src/main/resources/scoresCollection.txt";
        FileWriter file = new FileWriter(filePath);
        BufferedWriter bufferedWriter = new BufferedWriter(file);

        //Writing each score to the local scores file
        for(Pair<String, Integer> score : scores) {
          bufferedWriter.write(score.getKey() + ":" + score.getValue() + "\n");
        }
        //closes the buffered writer
        bufferedWriter.flush();
        logger.info("Score write success!!!");
    } catch (IOException exception) {
      logger.error("An error has occured when writing scores to the score file {}", exception.getMessage());
    }
  }

  /**
   * Handles the response when the communicator replies with the String of remote high scores by
   * splitting it into the name and score and adding it to the remoteScores field list.
   * @param communicatorReply the communicator's response
   */
  public void recieveRemoteHighScore(String communicatorReply) {
    logger.info("Handling the communicator's reply with incoming top online high scores");
    String[] scores = communicatorReply.split("\n");
    logger.info("Value of scores STRING[] after splitting, about to join the remoteScores: {}",
        (Object) scores);
    for(String score : scores) {
      String[] subsplit = score.split(":");
      remoteScores.add(new Pair<>(subsplit[0],Integer.parseInt(subsplit[1])));
    }
    logger.info("Value of scores remoteScores ArrayList after all online scores are added {}", remoteScores);
    checkScoreBeaten();

  }

  /**
   * Loads the online scores by calling the send request function and assigning a listener to it.
   */
  public void loadOnlineScores() {
    logger.info("Sending a request to the server to send top online high scores");
    //requests the communicator to send the high scores
    communicator.send("HISCORES");
    //adds a listener so that when the high scores are received, the first HISCORE word is cut off and the rest is sent off.
    communicator.addListener((remoteScoreListener) -> Platform.runLater(() -> {
      if (remoteScoreListener.startsWith("HISCORES")) {
        String scores = remoteScoreListener.substring(9);
         recieveRemoteHighScore(scores);
      }
    }));

  }

  /**
   * displays the remote scores on screen
   */
  public void displayRemoteScores(){
    logger.info("Displaying the remote top scores on screen");

    if(topScores != null) {
      topScores.getChildren().clear();
    }

    if (!remoteScores.isEmpty()) {
      //creates a title for top online scores
      Text title = new Text("Top Global Scores!");
      title.getStyleClass().add("scoreitemTitle");
      topScores.getChildren().add(title);

      //adds all the remote scores on screen
      for(int i=0; i<remoteScores.size(); i++) {
        Text scoretext = new Text(String.format("%s: %d", remoteScores.get(i).getKey(), remoteScores.get(i).getValue() ));
        scoretext.getStyleClass().add("scoreitem");
        topScores.getChildren().add(scoretext);
      }
      topScores.setAlignment(Pos.TOP_RIGHT);
      topScores.maxWidth(100);
      topScores.getStyleClass().add("online-scorelist");
    }
  }

  /**
   * Used when a user's high score is larger than any of the remote ones to append the remote list.
   * @param newHS the new local high score
   */
  public void sendNewHighScore(Pair<String, Integer> newHS) {
    logger.info("New High Score message has been sent to the server via communicator");
    communicator.send("HISCORE " + newHS.getKey() + ":" + newHS.getValue());
  }


  /**
   * Checks if a local or online high score was beaten. If so, changes are made accordingly
   */
  public void checkScoreBeaten() {
    //Sets a comparator so the lists can be arranged in descending order
    Comparator<Pair<String, Integer>> comparator = Comparator.comparingInt(Pair::getValue);
    comparator = comparator.reversed();

    boolean beaten = false;
    int finalScore = Integer.parseInt(finalGameState.getScore().getValue());
    for (Pair<String, Integer> pair : listOfScores) {
      if(finalScore > pair.getValue()) {
        beaten = true;
      }
    }

    logger.info("Remote scores: {}", remoteScores);

    //Checks if the user has beaten a local score OR there arent enough local scores OR the final score is higher than the smallest remote score
    if(beaten || listOfScores.size() <9 || finalScore > (remoteScores.get(remoteScores.size()-1).getValue()) ){
      String name = "";

      //Creating a textinputdialogue to get the user's name
      TextInputDialog nameBox = new TextInputDialog();
      nameBox.setContentText("Enter your name: ");
      Optional<String> nameInput = nameBox.showAndWait();
      name = nameInput.orElse("");

      //If local score was beaten or there arent enough local scores
      if(beaten || listOfScores.size() < 9){
        //Adds the new score to the localscoresproperty
        listOfScores.add(new Pair<>(name, finalScore));

        listOfScores.sort(comparator);

        observableListOfScores.clear();
        observableListOfScores.addAll(listOfScores);

        localScoresProperty = new SimpleListProperty<>(observableListOfScores);
        writeScore(listOfScores);
      }

      //Checking if the final score is larger than the online leaderboard
      if(finalScore > (remoteScores.get(remoteScores.size()-1).getValue())) {
        logger.info("NEW REMOTE HIGH SCORE!!!");
        remoteScores.add(new Pair<>(name, finalScore));
        remoteScores.sort(comparator);
        remoteScores.remove(remoteScores.size()-1);
        sendNewHighScore(new Pair<>(name, finalScore));
      }

      scoresList.reveal();
      displayRemoteScores();

    } else{
      scoresList.reveal();
      displayRemoteScores();
    }

    //Writes the leaderboard from the online multiplayer game
    if (finalGameState instanceof MultiplayerGame) {
      logger.info("Displaying the leaderboard for the multiplayer game");
      MultiplayerGame multiplayerGame = (MultiplayerGame) finalGameState;
      // Access the multiplayer leaderboard
      ObservableList<Pair<String, Pair<Integer, String>>> leaderboard = multiplayerGame.getMultiplayerLeaderboard(); // You can access the method now
      // Display the scores accordingly

      HBox leaderBoardScoreWrapper = new HBox();
      leaderBoardScoreWrapper.setTranslateY(400);
      leaderBoardScoreWrapper.setMaxSize(400, 200);
      root.getChildren().add(leaderBoardScoreWrapper);
      VBox leaderBoardScores = new VBox();
      leaderBoardScoreWrapper.getChildren().add(leaderBoardScores);
      Text multiplayerTitle = new Text("Multiplayer Results!");
      leaderBoardScores.getChildren().add(multiplayerTitle);
      multiplayerTitle.getStyleClass().add("scoreitemTitle");

      // Define the comparator
      Comparator<Pair<String, Pair<Integer, String>>> ldbrComparator = (player1, player2) -> {

        int score1 = player1.getValue().getKey();
        int score2 = player2.getValue().getKey();

        // Compare scores in descending order
        return Integer.compare(score2, score1);
      };

      // Sort the leaderboard using ldbrComparator
      FXCollections.sort(leaderboard, ldbrComparator);

      for(Pair<String, Pair<Integer, String>> player : leaderboard) {
        String nameOfPlayer = player.getKey();
        int scoreOfPlayer = player.getValue().getKey();
        Text playerResults = new Text("Name: "+nameOfPlayer+ ", Score: "+ scoreOfPlayer);
        leaderBoardScores.getChildren().add(playerResults);
        playerResults.getStyleClass().add("scoreitem");
      }
    }


  }
}
