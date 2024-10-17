package uk.ac.soton.comp1206.component;

import java.util.TimerTask;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Timer;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    private double opacity = 1;
    private double fadeSpeed = 0.01;

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.rgb(42, 42, 43, 0.35),
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE,
            Color.rgb(64,64,64,0.5),
            Color.WHITE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.rgb(42, 42, 43, 0.35));
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        // Clear the canvas
        gc.clearRect(0, 0, width, height);

        // Fill the entire canvas with the specified colour
        gc.setFill(colour);
        gc.fillRect(0, 0, width, height);

        // Create a darker version of the specified colour for the bottom-right portion
        Color darkerColour = ((Color) colour).darker();

        // Create a gradient for the diagonal line, with the top-left portion using the original colour and the bottom-right portion using the darker colour
        LinearGradient gradient = new LinearGradient(0, 0, width, height, false, CycleMethod.NO_CYCLE,
            new Stop(0, (Color) colour), new Stop(0.5, (Color) colour), new Stop(0.5, darkerColour), new Stop(1, darkerColour));

        // Fill the diagonal area with the gradient
        gc.setFill(gradient);
        gc.fillRect(0, 0, width, height);

        // Draw a black border around the block
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2); // Adjust the line width as needed
        gc.strokeRect(0, 0, width, height);

    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }


    /**
     * Used to display a circle on the game block to show an indicator on the middle square
     */
    public void showCircle() {
        var gc = getGraphicsContext2D();
        gc.setFill(Color.rgb(42, 42, 43, 0.55));
        gc.fillOval((width - 20) / 2, (height - 20) / 2, 20, 20);
    }

    /**
     * does a out animation which flashes white then slowly paints in a clear colour
     */
    public void fadeOut() {
        logger.info("Attempting to fade out");

        paintColor(Color.WHITE);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                double clampedOpacity = Math.max(0.0, Math.min(opacity, 1.0));
                Color color = Color.rgb(42, 42, 43, clampedOpacity);
                opacity = opacity-0.05;
                paintColor(color);

                if(opacity<0.35) {
                    stop();
                    paintEmpty();
                }

            }
        };

        timer.start();

    }

}
