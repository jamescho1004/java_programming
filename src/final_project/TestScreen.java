package final_project;

// CardLayout screen contract 
public interface TestScreen {
    // Ranking keys used in saved results
    String KEY_REACTION = "REACTION";
    String KEY_CLICK = "CLICK";

    // Unique CardLayout key for this screen
    String getKey();

    // Display name used in the menu
    String getDisplayName();

    // Called when this screen becomes visible
    void onShow();

    // Called when the user presses the top Reset button
    void resetTest();
}
