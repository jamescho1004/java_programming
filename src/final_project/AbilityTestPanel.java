package final_project;

import javax.swing.JPanel;

// Base class for test panels, provides common helper methods via TestHost
public abstract class AbilityTestPanel extends JPanel implements TestScreen {
    private static final long serialVersionUID = 1L;

    // For top status updates, dialogs, and result submission
    protected final TestHost host;

    // Sub-panels must receive a host so they can report status/results consistently
    protected AbilityTestPanel(TestHost host) {
        this.host = host;
    }

    // Update the top status label, progress bar 
    protected void updateStatus(String stageText, int value, int max) {
        if (host != null) host.setTopStatus(stageText, value, max);
    }

    // Show an information dialog through the host 
    protected void info(String msg) {
        if (host != null) host.showInfo(msg);
    }

    // Show an error dialog 
    protected void error(String msg) {
        if (host != null) host.showError(msg);
    }

    // Current user id (falls back to Guest if host is missing)
    protected String uid() {
        return (host == null) ? "Guest" : host.getUserId();
    }
}
