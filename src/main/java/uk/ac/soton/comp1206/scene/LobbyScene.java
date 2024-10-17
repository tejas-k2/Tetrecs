package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class LobbyScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(LobbyScene.class);
  private Communicator communicator;
  private ArrayList<String> currentGames = new ArrayList<>();
  private String currentChannel = "";
  private boolean scrollToBottom = false;
  private ScrollPane scroller;
  private TextFlow messages;
  private boolean host;
  private VBox channelRoom;
  private TextField messageToSend = new TextField();
  private VBox currentGamesVBox = new VBox();
  private VBox textInput = new VBox();
  private Timer timer;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed
   * @param gameWindow the game window
   * @param communicator the communicator used to send/receive info from the server
   */
  public LobbyScene(GameWindow gameWindow, Communicator communicator) {
    super(gameWindow);
    this.communicator = communicator;
  }

  /**
   * Initialise this scene. Called after creation
   */
  @Override
  public void initialise() {
    logger.info("Initialising the LobbyScene");
    //Play Background music
    Multimedia multimedia = new Multimedia();
    multimedia.playBackgroundMusic("/music/menu.mp3");
  }

  /**
   * Build the layout of the scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

    //Creates a timer loop so that the scene keeps searching for new channels every 1 second
    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        logger.info("Reloading the current games Available");
        loadCurrentGames();
      }
    }, 0, 1000);

//    loadCurrentGames();

    //Creating the multiplayer pane and adding the background in
    var multiplayerPane = new StackPane();
    multiplayerPane.setMaxWidth(gameWindow.getWidth());
    multiplayerPane.setMaxHeight(gameWindow.getHeight());
    multiplayerPane.getStyleClass().add("menu-background");
    root.getChildren().add(multiplayerPane);

    //Creating a hbox to add the title "Multiplayer" to then adding it to the root
    HBox titleBox = new HBox();
    titleBox.setAlignment(Pos.TOP_CENTER);
    titleBox.setMaxHeight(50);
    Text title = new Text("Multiplayer");
    title.getStyleClass().add("multiplayerTitle");
    titleBox.getChildren().add(title);
    root.getChildren().add(titleBox);

    //Initialising the left VBox to store games
    VBox gamesBox = new VBox();
    gamesBox.setMaxWidth(gameWindow.getWidth()-500);
    gamesBox.setMaxHeight(gameWindow.getHeight()-100);
    gamesBox.setTranslateX(40);
    gamesBox.setTranslateY(70);
    gamesBox.getStyleClass().add("gamesBox");
    root.getChildren().add(gamesBox);

    //Initialising the host new game button
    Button hostGameBtn = new Button("Host New Game");
    hostGameBtn.getStyleClass().add("hostgameButton");
    hostGameBtn.setTranslateX(20);
    hostGameBtn.setTranslateY(20);
    gamesBox.getChildren().add(hostGameBtn);
    hostGameBtn.setOnAction(actionEvent -> hostNewGame());

    //Adds the current channels to the window
    root.getChildren().add(currentGamesVBox);
    currentGamesVBox.setMaxSize(250,500);
    currentGamesVBox.setTranslateX(60);
    currentGamesVBox.setTranslateY(250);
  }

  /**
   * Allows hosting a new game by prompting the user for a suitable channel name
   */
  public void hostNewGame() {
      logger.info("Hosting new Game");
      //Clearing from previous method call
      textInput.getChildren().clear();
      root.getChildren().remove(textInput);

      root.getChildren().add(textInput);

      //Setting up the onscreen visuals
      AtomicReference<String> channelName= new AtomicReference<>("");
      Label gameNameLabel = new Label("Enter Channel Name:");
      TextField channelNameTextField = new TextField();
      Button submitChannelName = new Button("Submit");

      //Collects all the title, textfield and button together into one vbox
      textInput.setMaxSize(250,100);
      textInput.getChildren().add(gameNameLabel);
      textInput.getChildren().add(channelNameTextField);
      textInput.getChildren().add(submitChannelName);
      textInput.setTranslateX(60);
      textInput.setTranslateY(140);
      gameNameLabel.getStyleClass().add("lobbyEnterChannelNameText");
      submitChannelName.getStyleClass().add("submitChannelName");

      //Sets the channelName to whatever the user had inputted if its suitable
      submitChannelName.setOnAction(actionEvent -> {
        channelName.set(channelNameTextField.getText());
        channelNameTextField.clear();
        //If the channel name the user chose doesnt already exist or the channel name isnt blank
        if(!currentGames.contains(channelName.get()) || !(channelName.get().isEmpty())){
          communicator.send("CREATE "+channelName.get());
          this.host = true;
          this.currentChannel = channelName.get();
          textInput.getChildren().clear();
          displayChatBox();
        }
      });

  }

  /**
   * Displays the current games on screen as buttons which can be clicked to join that specific game
   */
  public void displayCurrentGames() {
    logger.info("Displaying current games");

    //clears the currentGamesVbox so that when it is refreshed they dont stack up on each other.
    this.currentGamesVBox.getChildren().clear();

    //Creates a button for each channel
    Button channel;
    for(String games : this.currentGames){
      channel = new Button(games);
      channel.getStyleClass().add("channelButtons");
      currentGamesVBox.getChildren().add(channel);

      Button finalChannel = channel;
      channel.setOnAction(actionEvent -> {
        //When a button is clicked, its channel name is extracted and that channel is joined
        String channelName = finalChannel.getText();
        //Checks if your currently in a channel or not
        if(!this.currentChannel.isEmpty()){
          Alert error = new Alert(AlertType.ERROR);
          error.setTitle("Error");
          error.setContentText("You are already in a channel");
          error.showAndWait();
        } else {
          joinChannel(channelName);
        }
      });
    }
  }

  /**
   * Joins a channel by sending a communicator request to join the specific channel.
   * @param channelName the channel that the game wants to enter
   */
  public void joinChannel(String channelName){
    logger.info("Joining channel: {}", channelName);
    communicator.send("JOIN "+channelName);
    this.host = false;
    this.currentChannel = channelName;
    textInput.getChildren().clear();
    displayChatBox();
  }

  /**
   * Sends a request to the server via the communicator for a list of the current games/channels available
   */
  public void requestCurrentGames() {
    logger.info("Sending a request to the server to send current games");
    communicator.send("LIST");
  }

  /**
   * Loads the current channels available by setting a listener
   */
  public void loadCurrentGames(){
    logger.info("Setting a listener and calling requestCurrentGames() to access list of current games");
    requestCurrentGames();
    communicator.addListener((currentGamesListener) -> Platform.runLater(() -> {
      if(currentGamesListener.startsWith("CHANNELS")) {

        //Removes the first 9 characters of "Channels "
        String channels = currentGamesListener.substring(9);
        Platform.runLater(() -> recieveChannelNames(channels));

      }
    }));
  }

  /**
   * Adds all the channel names that the communicator responded with to currentGames arrayList
   * @param communicatorResponse the String reply that the listener picked up from the communicator
   */
  public void recieveChannelNames(String communicatorResponse) {
    logger.info("Adding the channel names to the currentGames ArrayList");

    //clears the current games, so it can be refreshed with all the new ones without any overlap
    currentGames.clear();
    String[] channels = communicatorResponse.split("\n");

    //Adds all the channels into the currentGames arrayList
    this.currentGames.addAll(Arrays.asList(channels));
    displayCurrentGames();
  }

  /**
   * Displays the channels lobby which includes players, chatbox and leave/start game
   */
  public void displayChatBox() {
    logger.info("Displaying the Channel {}'s chatbox", this.currentChannel);

    //clears the listeners so that there arent any duplicates when leaving and rejoining other games
    communicator.clearListeners();

    //initialises the entire channel room lobby portion of the screen
    this.channelRoom = new VBox();
    channelRoom.getStyleClass().add("gamesBox");
    channelRoom.setMaxHeight(gameWindow.getHeight()-200);
    channelRoom.setMaxWidth(gameWindow.getWidth()-400);
    channelRoom.setTranslateX(350);
    channelRoom.setTranslateY(70);
    root.getChildren().add(channelRoom);

    //Initialising the players in the channel vbox
    VBox playersInChannel = new VBox();
    playersInChannel.setMaxSize(450, 30);
    channelRoom.getChildren().add(playersInChannel);
    Text playersText = new Text("Current channel: "+this.currentChannel+ " Players: ");
    Text usersInChannel = new Text();
    playersText.getStyleClass().add("playersInChannel");
    usersInChannel.getStyleClass().add("playersInChannel");
    playersInChannel.getChildren().add(playersText);
    playersInChannel.getChildren().add(usersInChannel);

    //sends a communicator request for all the players in the current channel then updates the usersInChannel Text to reflect this
    communicator.send("USERS");
    communicator.addListener((listOfUsers) -> Platform.runLater(() -> {
      if(listOfUsers.startsWith("USERS")) {
        logger.info("RESPONSE FROM THE LISTENER FOR USERS: {}", listOfUsers);
        usersInChannel.setText("");
        String[] users = listOfUsers.substring(6).split("\n");
        for(String user : users){
          String currentText = usersInChannel.getText();
          usersInChannel.setText(currentText + " | "+ user);
        }
      }
    }));

    //Initialising the part allowing users to start the game for or leave a channel
    VBox channelCommandsVBox = new VBox();
    channelRoom.getChildren().add(channelCommandsVBox);
    channelCommandsVBox.setPrefSize(450,50);
    channelCommandsVBox.setMaxSize(450,50);
    channelCommandsVBox.setTranslateY(310);
    Button leaveButton = new Button("Leave Channel");
    channelCommandsVBox.getChildren().add(leaveButton);

    //Sets up the leave button
    leaveButton.setOnAction(actionEvent -> leaveChannel());

    //Creates the start button if the user is a host
    Button startButton;
    if(this.host){
      startButton = new Button("Start Game");
      channelCommandsVBox.getChildren().add(startButton);
      startButton.setOnAction(actionEvent -> startGame());
    }

    //Initialise the actual chat box
    VBox messagesBox = new VBox();
    messagesBox.setTranslateY(-40);
    messagesBox.setPrefSize(450,300);
    messagesBox.setMaxSize(450, 300);
    channelRoom.getChildren().add(messagesBox);

    this.messageToSend.clear();
    messageToSend.setMinWidth(channelRoom.getWidth()-50);
    messageToSend.setPromptText("Enter your message...");
    Button sendMessage = new Button("SEND");
    sendMessage.getStyleClass().add("sendMessageInLobby");
    HBox sendMessageBar = new HBox();
    sendMessageBar.getChildren().add(messageToSend);
    sendMessageBar.getChildren().add(sendMessage);
    HBox.setHgrow(messageToSend, Priority.ALWAYS);
    messagesBox.getChildren().add(sendMessageBar);

    //Adding a listener to ensure all incoming messages are outputted
    communicator.addListener((message) -> Platform.runLater(() -> {
      if(message.startsWith("MSG")) {
        this.recieveMessage(message.substring(4));
      }
    }));

    //Create a textflow to hold all the messages
    messages = new TextFlow();
    messages.getStyleClass().add("messagesTextFlow");

    //Adding a scrollpane to allow for scrolling
    scroller = new ScrollPane();
    scroller.setContent(messages);
    scroller.setFitToWidth(true);
    messagesBox.getChildren().add(scroller);

    //Clicking SEND will send the message to the communicator server
    sendMessage.setOnAction(actionEvent -> {
      this.sendCurrentMessage(messageToSend.getText());
      messageToSend.clear();
    });

    //Pressing ENTER on your keyboard will also send the message to the communicator server
    messageToSend.setOnKeyPressed(keyEvent -> {
      if(keyEvent.getCode() == KeyCode.ENTER) {
        sendCurrentMessage(messageToSend.getText());
        messageToSend.clear();
      }
    });

    //Setting a listener to ensure the scroller always scrolls to the bottom
    scene.addPostLayoutPulseListener(this::jumpToBottom);

    communicator.addListener((hostStartGame) -> Platform.runLater(() -> {
      if(hostStartGame.startsWith("START")){
        gameWindow.startGame();
      }
    }));

  }


  /**
   * Moves the scroller to the bottom of the page
   */
  private void jumpToBottom() {
    if (!scrollToBottom) return;
    scroller.setVvalue(1.0f);
    scrollToBottom = false;
  }

  private void recieveMessage(String message) {
    Text incomingMessage = new Text(message+"\n");
    messages.getChildren().add(incomingMessage);

    //Scrolls to the bottom after each message
    if(scroller.getVvalue() == 0.0f || scroller.getVvalue() > 0.0f) {
      scrollToBottom = true;
    }
  }

  /**
   * Sends the current message to the server via the communicator
   * @param text the String of text the user wants to send
   */
  private void sendCurrentMessage(String text) {
    logger.info("Sending the following text to the communicator server: {}", text);
    communicator.send("MSG "+text);
  }

  /**
   * Leaves the channel by sending a communicator request to PART.
   */
  public void leaveChannel() {
    logger.info("Sending request to communicator to leave the current channel");
    communicator.send("PART");
    this.channelRoom.getChildren().clear();
    //Makes the background transparent
    this.channelRoom.setStyle("-fx-background-color: transparent;");
    this.currentChannel = "";
  }

  /**
   * Starts the game by sending a communicator request to START
   */
  public void startGame() {
    logger.info("Sending request to communicator to start the game with the current channel");
    timer.cancel();
    communicator.send("START");
    gameWindow.startGame();
  }


}
