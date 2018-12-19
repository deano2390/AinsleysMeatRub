package uk.co.deanwild.ainsleysmeatrub;

/**
 * Created by deanwild on 25/09/15.
 */
public interface GameStateListener {
    void onMeatRubbed(int score);
    void onAinsleyGettingExcited(boolean excited);
}
