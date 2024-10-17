package uk.ac.soton.comp1206.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.GameEndedListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.scene.Multimedia;
import java.util.Timer;
import java.util.TimerTask;
import uk.ac.soton.comp1206.scene.ScoresList;


/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game implements LineClearedListener {
    private Random random = new Random();
    public GamePiece currentPiece;
    public GamePiece followingPiece;
    public PieceBoard pieceBoard = new PieceBoard(3,3,120,120);
    public PieceBoard followingPieceBoard = new PieceBoard(3,3,80,80);
    public IntegerProperty score = new SimpleIntegerProperty(0); //IntegerProperty makes it bindable
    private IntegerProperty level = new SimpleIntegerProperty(0);
    public IntegerProperty lives = new SimpleIntegerProperty(3);
    public IntegerProperty multiplier = new SimpleIntegerProperty(1);
    private static final Logger logger = LogManager.getLogger(Game.class);
    public Multimedia multimedia = new Multimedia();
    public Set<GameBlockCoordinate> coords = new HashSet<>();
    private Timer timer;
    private TimerTask timerTask;
    private GameLoopListener gameLoopListener;
    private GameEndedListener gameEndedListener;
    private int timerDelayLength;
    private Boolean gameEnd = Boolean.FALSE;
    private IntegerProperty highScore = new SimpleIntegerProperty(0);
    private int valueOfCenterBlock;

    //false = singleplayer. true = multiplayer
    private Boolean gameType = false;

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    public int controlsX= 2;
    public int controlsY = 2;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");

        //we want the game to start with a piece already - hence put in initialise game method.
        currentPiece = spawnPiece();
        followingPiece = spawnPiece();
        pieceBoard.displayPiece(currentPiece);
        followingPieceBoard.displayPiece(followingPiece);

        playSetupScene();

    }

    public void playSetupScene() {
        logger.info("Setting up the scene with the music, top score and timer");
        //Play Background music
        multimedia.playBackgroundMusic("/music/game_start.wav");

        //sets the highscore
        this.highScore.set(getHighScore());

        //Starts the initial timer
        startTimer();
    }

    public void gameClose(){
        multimedia.stopMusic();
    }

    public void startTimer() {
        //Starting the game's timer
        logger.info("Starting the timer");
        timer = new Timer();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                gameLoop();
            }
        };

        this.timerDelayLength = getTimerDelay();

        if(gameLoopListener != null){
            gameLoopListener.gameLoopOccured(this.timerDelayLength);
        }

        //specifies the task and how long after it should run it
        timer.schedule(timerTask, this.timerDelayLength);
    }

    /**
     * Restarts the timer by cancelling it then starting it again
     */

    public void restartTimer() {
        logger.info("Restarting the timer");
        timer.cancel();
        this.timerDelayLength = getTimerDelay(); // Update timer delay length
        startTimer();
    }

    /**
     * stops the timer
     */

    public void stopTimer() {
        timer.cancel();
    }

    /**
     * returns the delay length for the current block
     * @return int represents the time
     */
    public int getTimerDelayLength(){
        return timerDelayLength;
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        removeCursor();
        playPiecePostSteps(x, y);
    }

    /**
     * plays the steps after a block click. Plays the piece if it can then sounds an audio
     * @param x x coord of block click
     * @param y y coord of block click
     */

    public void playPiecePostSteps(int x, int y){
        logger.info("Playing the post piece steps");
        if(grid.canPlayPiece(currentPiece,x,y)){
            multimedia.playAudioFile("/sounds/pling.wav");
            grid.playPiece(currentPiece, x, y);
            //ensures that when the keys are used, the incorrect color isnt used
            int color = grid.get(controlsX, controlsY);
            this.grid.setPreviousColor(color);
            afterPiece();
            prepareAfterPiece();
        } else {
        //play something like a sound to denote you cannot play that piece
        }
    }

    /**
     * Prepares the after piece by loading next piece and restarting timer
     */
    public void prepareAfterPiece() {
        nextPiece();
        restartTimer();
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    //GETTER+SETTER METHODS FOR SCORE
    /**
     * Used to retrieve the score
     *
     * @return the int value of the score
     */
    public StringBinding getScore(){
        return score.asString();
    }

    //GETTER+SETTER METHODS FOR LEVEL
    /**
     * Retrieves the level the player is currently on
     * @return int value of current level
     */
    public StringBinding getLevel(){
        return level.asString();
    }

    //GETTER METHOD FOR LIVES

    /**
     * Retrieves the number of lives remaining
     * @return int value of lives
     */
    public StringBinding getLives(){
        return lives.asString();
    }

    //GETTER METHOD FOR MULTIPLIER

    /**
     * Retrieves the multiplier
     *
     * @return int value of multiplier
     */
    public StringBinding getMultiplier(){
        return multiplier.asString();
    }

    /**
     * Replaces the current piece with a new piece that has been spawned in
     * @return the new current piece
     */
    public GamePiece nextPiece(){
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        logger.info("The next piece is: {}", currentPiece);
        logger.info("The following piece is: {}", followingPiece);
        pieceBoard.displayPiece(currentPiece);
        followingPieceBoard.displayPiece(followingPiece);

        return currentPiece;
    }

    /**
     * Spawns a new piece at random in
     * @return the piece that is created.
     */
    public GamePiece spawnPiece(){
        //chooses at random out of total number of pieces
        int randnum = random.nextInt(GamePiece.PIECES);
        logger.info("Picking random piece: {}",randnum);
        var piece = GamePiece.createPiece(randnum);
        return piece;
    }

    /**
     * Checks the rows and columns to see if they can be cleared or not, then clears any if found
     */
    public void afterPiece(){
        logger.info("Running afterPiece(). Checking for rows and columns to clear... ");
        coords.clear();
        int gridXRow = grid.getRows();
        int gridYColumn = grid.getCols();

        //Checks all the x-rows for valid blocks to clear
        for(int j=0; j<gridYColumn; j++){
            for(int i=0; i<gridXRow; i++) {
                var gridBlock = grid.get(i,j);
                if(gridBlock <= 0){
                    break;
                } else {
                    if(i==(gridXRow-1)){
                        //add all the 5 last x coords with the y coords to the hashset
                        for(int k=0; k<gridXRow; k++){
                            GameBlockCoordinate gameBlockCoordinate = new GameBlockCoordinate(k,j);
                            coords.add(gameBlockCoordinate);
                        }
                    }
                }
            }
        }

        //Checks all the y-columns for valid squares to clear
        for(int i=0; i<gridXRow; i++){
            for(int j=0; j<gridYColumn; j++) {
                var gridBlock = grid.get(i,j);
                if(gridBlock <= 0){
                    break;
                } else {
                    if(j==(gridYColumn-1)){
                        //add all the 5 last y coords and the x coords to the hashset
                        for(int k=0; k<gridYColumn; k++){
                            GameBlockCoordinate gameBlockCoordinate = new GameBlockCoordinate(i,k);
                            coords.add(gameBlockCoordinate);
                        }
                    }
                }
            }
        }

        whenLineCleared(coords);

        //Clearing the squares on the screen back to white (0)
        for (GameBlockCoordinate coordinate : coords) {
            logger.info("Clearing any fully filled rows/columns process has begun.");
            int x = coordinate.getX(), y = coordinate.getY();
            grid.setPreviousColor(0);
            grid.set(x,y,0);
            grid.get(x,y);
        }

        //calculates the multiplier and new score
        int numberOfBlocks = coords.size();
        int numberOfLines = (int) Math.ceil(numberOfBlocks/5);
        if (numberOfLines>0){
            multiplier.set(multiplier.get() + 1);
        } else {
            multiplier.set(1);
        }
        score(numberOfLines, numberOfBlocks);
        if(score.getValue() >= highScore.getValue()){
            highScore.setValue(score.getValue());
        }

        restartTimer();

        //Sets the new level
        level.set((int) Math.floor((double) score.get()/1000));

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
        }
    }

    /**
     * Getter method for the pieceBoard for the next piece
     * @return the pieceBoard
     */
    public PieceBoard getPieceBoard(){
        return pieceBoard;
    }

    /**
     * Getter method for the following pieceboard for the following piece
     * @return the followingPieceBoard
     */
    public PieceBoard getFollowingPieceBoard() {return followingPieceBoard;}

    /**
     * Rotates the current piece the user is playing - CLOCKWISE
     */
    public void rotateCurrentPieceClockwise() {
        logger.info("Successfully rotated the current piece");
        currentPiece.rotate();
        pieceBoard.displayPiece(currentPiece);
        multimedia.playAudioFile("/sounds/rotate.wav");
    }

    /**
     * Rotates the current piece the user is playing - ANTICLOCKWISE
     */
    public void rotateCurrentPieceAnticlockwise() {
        logger.info("Successfully rotated the current piece");
        currentPiece.rotate();
        currentPiece.rotate();
        currentPiece.rotate();
        pieceBoard.displayPiece(currentPiece);
        multimedia.playAudioFile("/sounds/rotate.wav");
    }


    /**
     * Swaps the current piece with the following piece (and vice versa)
     */
    public void swapCurrentPiece() {
        logger.info("Swapping the current piece and the following piece around");
        GamePiece temp = currentPiece;
        currentPiece = followingPiece;
        followingPiece = temp;
        pieceBoard.displayPiece(currentPiece);
        followingPieceBoard.displayPiece(followingPiece);
        multimedia.playAudioFile("/sounds/place.wav");
    }

    /**
     * Moves the users cursor left when playing with keyboard
     */
    public void moveLeft() {
        removeCursor();
        logger.info("Moving the cursor left. New coordinate {}, {}", controlsX, controlsY);
        if(this.controlsX > 0){
            controlsX--;
        } else {
            this.controlsX = 4;
        }
        displayCursor();
    }

    /**
     * Moves the users cursor right when playing with keyboard
     */
    public void moveRight() {
        removeCursor();
        logger.info("Moving the cursor right. New coordinate {}, {}", controlsX, controlsY);
        if(this.controlsX >= 0 && this.controlsX < 4){
            controlsX++;
        } else {
            this.controlsX = 0;
        }
        displayCursor();
    }

    /**
     * Moves the users cursor up when playing with keyboard
     */
    public void moveUp() {
        removeCursor();
        logger.info("Moving the cursor up. New coordinate {}, {}", controlsX, controlsY);
        if(this.controlsY > 0){
            controlsY--;
        } else {
            this.controlsY = 4;
        }
        displayCursor();
    }

    /**
     * Moves the users cursor down when playing with keyboard
     */
    public void moveDown() {
        removeCursor();
        logger.info("Moving the cursor down. New coordinate {}, {}", controlsX, controlsY);
        if(this.controlsY >= 0 && this.controlsY < 4){
            controlsY++;
        } else {
            this.controlsY = 0;
        }
        displayCursor();
    }

    /**
     * Displays the cursor when the user is playing with the keyboard
     */
    public void displayCursor() {
        grid.addTemporaryBox(controlsX, controlsY);
    }

    /**
     * Removes the cursor when the user is no longer playing with the keyboard, or to initialise a key control
     */

    public void removeCursor() {
        grid.removeTemporaryBox(controlsX, controlsY);
    }

    /**
     * Plays a piece using the key controls
     */
    public void playPieceUsingControl() {
        removeCursor();
        playPiecePostSteps(controlsX, controlsY);
    }

    /**
     * line cleared listener function
     * @param coords the coordinates to be cleared
     * @return the coordinates to be cleared
     */
    @Override
    public Set<GameBlockCoordinate> whenLineCleared(Set<GameBlockCoordinate> coords) {
        return coords;
    }

    /**
     * getter method for coordinates
     * @return coordinates
     */
    public Set<GameBlockCoordinate> getCoords() {
        return coords;
    }

    /**
     * Calculates the timer delay
     * @return int of the time the player has to play each piece
     */
    public int getTimerDelay(){
        int timerDelay = 12000-(500*level.getValue());
        logger.info("Timer Delay is: {}", timerDelay);
        return timerDelay;
    }

    /**
     * loops the game
     */
    public void gameLoop(){
        //Platform.runLater() used due to different thread issue
        Platform.runLater(() -> {
            logger.info("Timer Ended! Moving onto gameloop next piece");
            lives.set(lives.get()-1);
            multimedia.playAudioFile("/sounds/lifelose.wav");
            multiplier.set(1);
            prepareAfterPiece();

            //If lives fall below 0, end the game. Or prepare the next piece
            if(this.lives.get() < 0){
                logger.info("TRYING TO STOP THE GAME !!!!!!!!!!!!!!!!!!!!!!");

                if(gameEndedListener != null){
                    gameEndedListener.gameEnded();
                }

                stopTimer();
                this.endGame();
            }

        });
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
     * Ends the game by ending the scene and opening the score scene
     */
    public void endGame(){
        logger.info("Game has ended... gameEnd is True");
        this.gameEnd = true;
    }

    /**
     * getter for gameEnd field var
     * @return Boolean if the game is ending or not
     */

    public Boolean getGameEnd() {
        return this.gameEnd;
    }

    /**
     * getter method for the high score property
     * @return Stringbinding string of high score
     */
    public StringBinding getHighScoreProperty() {
        return highScore.asString();
    }

    /**
     * Retrieves the high score from an external file
     * @return int value of the high score
     */
    public int getHighScore() {
        int maxScore = 0;
        try{
            logger.info("Extracting user's highest score");
            String filePath = "src/main/resources/scoresCollection.txt";
            File file = new File(filePath);
            Reader reader = new FileReader(file);
            BufferedReader reader1 = new BufferedReader(reader);

            ArrayList<Integer> scores = new ArrayList<>();

            String line = "";

            while((line=reader1.readLine()) != null){
                String[] divide = line.split(":");
                int score = Integer.parseInt(divide[1]);
                scores.add(score);
            }

            //Finds the highest score.
            for(int value : scores) {
                if(value>maxScore) {
                    maxScore = value;
                }
            }

            return maxScore;
        } catch (IOException e) {
            logger.error("An error has occurred when extracting the user's highest score {}", e.getMessage());
        }
        return maxScore;
    }

    public Boolean getGameType(){
        return gameType;
    }

}
