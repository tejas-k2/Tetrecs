package uk.ac.soton.comp1206.scene;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoresList extends VBox {
  private ListProperty<Pair<String, Integer>> scoresProperty = new SimpleListProperty<>();
  private static final Logger logger = LogManager.getLogger(ScoresList.class);

  /**
   * Constructor for ScoresList.
   */
  public ScoresList() {
    scoresProperty.addListener((InvalidationListener)( e)->createScores());
    getStyleClass().add("scorelist");
  }

  /**
   * Updates the scores by calling reveal() to show on screen.
   */
  public void createScores() {
    logger.info("Updating the scores");
    for(Pair<String, Integer> p : scoresProperty) {
      logger.info(p.getValue() + ":" + p.getKey());
    }
    reveal();
  }

  /**
   * Animates the display of the scores on screen
   */
  public void reveal() {
    getChildren().clear();
    Text topScores = new Text("Top Local Scores!");
    topScores.getStyleClass().add("");
    getChildren().add(topScores);

    for(Pair<String, Integer> score : scoresProperty){
      logger.info(score.getValue());
      Text scoresText = new Text(String.format("%s: %d",score.getKey(), score.getValue()));
      scoresText.getStyleClass().add("scoreitem");
      getChildren().add(scoresText);
    }
  }

  /**
   * Getter method for scoreProperty
   * @return field variable scoreProperty
   */
  public ListProperty<Pair<String, Integer>> getScoreProperty() {
    return scoresProperty;
  }

}
