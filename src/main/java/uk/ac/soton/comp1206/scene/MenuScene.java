package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Awful title
        var title = new Text("TetrECS");
        var title2 = new Text("Unleash your inner architect");

        //makes slogan flash
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), title2);
        fadeTransition.setFromValue(0.5);
        fadeTransition.setToValue(1);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE); // Repeat indefinitely
        fadeTransition.play();

        title.getStyleClass().add("title");
        title2.getStyleClass().add("menu-slogan");

        VBox titleScreen = new VBox();
        titleScreen.getStyleClass().add("menu-title-vbox");
        titleScreen.getChildren().add(title);
        titleScreen.getChildren().add(title2);
        titleScreen.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(titleScreen);

        //For now, let us just add a button that starts the game. I'm sure you'll do something way better.
        var playButton = new Button("Play");
        playButton.getStyleClass().add("menu-scene-items");
        var instructionsButton = new Button("Instructions");
        instructionsButton.getStyleClass().add("menu-scene-items");
        var multiplayerButton = new Button("Multiplayer");
        multiplayerButton.getStyleClass().add("menu-scene-items");
        var exitButton = new Button("Exit");
        exitButton.getStyleClass().add("menu-scene-items");

        //Arranging all the buttons on screen
        VBox buttonsVBox = new VBox();
        buttonsVBox.getStyleClass().add("menu-options-vbox");
        buttonsVBox.getChildren().add(playButton);
        buttonsVBox.getChildren().add(instructionsButton);
        buttonsVBox.getChildren().add(multiplayerButton);
        buttonsVBox.getChildren().add(exitButton);

        buttonsVBox.setAlignment(Pos.CENTER_RIGHT);
        root.getChildren().add(buttonsVBox);

        //Bind the button action to the startGame method in the menu
        playButton.setOnAction(this::startGame);

        instructionsButton.setOnAction(this::displayInstructions);

        multiplayerButton.setOnAction(this::displayMultiplayer);
    }

    private void displayInstructions(ActionEvent actionEvent) {
        gameWindow.displayInstructions();
    }

    private void displayMultiplayer(ActionEvent actionEvent){
        gameWindow.displayLobby();
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        //Play Background music
        Multimedia multimedia = new Multimedia();
        multimedia.playBackgroundMusic("/music/menu.mp3");
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

}
