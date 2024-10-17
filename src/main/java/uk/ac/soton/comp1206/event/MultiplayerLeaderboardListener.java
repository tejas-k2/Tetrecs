package uk.ac.soton.comp1206.event;

import javafx.collections.ObservableList;
import javafx.util.Pair;

public interface MultiplayerLeaderboardListener {

  void updateLeaderboard(ObservableList<Pair<String, Pair<Integer, String>>> leaderboard);

}
