package final_project;

// Contract between AppFrame (host) and each test panel (screen)
public interface TestHost {
    // Update the top area (stage label + progress bar)
    void setTopStatus(String stageText, int value, int max);

    // Show an information message dialog
    void showInfo(String message);

    // Show an error message dialog
    void showError(String message);

    // Current logged-in user id
    String getUserId();

    // Persist a test result
    void submitResult(TestResult result);
}
