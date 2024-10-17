package uk.ac.soton.comp1206.scene;

import java.util.Objects;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Multimedia {

  private MediaPlayer audioPlayer;
  private MediaPlayer musicPlayer;
  private static final Logger logger = LogManager.getLogger(Multimedia.class);

  /**
   * Plays a sound effect once
   * @param pathToAudio the path to the sound effect you wish to play
   */
  public void playAudioFile(String pathToAudio){
    Media sound = new Media(Objects.requireNonNull(getClass().getResource(pathToAudio)).toString());
    audioPlayer = new MediaPlayer(sound);
    audioPlayer.play();
  }

  /**
   * Plays Background music on loop
   * @param pathToAudio the path to the audio file you wish to play
   */
  public void playBackgroundMusic(String pathToAudio){
    try {
      Media sound = new Media(Objects.requireNonNull(getClass().getResource(pathToAudio)).toString());
      musicPlayer = new MediaPlayer(sound);
      musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      musicPlayer.play();
    } catch (NullPointerException e) {
      logger.error("The audio at path {} cannot be found", pathToAudio);
      e.printStackTrace();
    }
  }

  /**
   * stops the music
   */
  public void stopMusic(){
    if(musicPlayer!=null){
      musicPlayer.stop();;
    }
  }


}
