package final_project;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// Demo screen shown after login
public class WelcomePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Main constructor: greets the current user.
    public WelcomePanel(String userId) {
        setLayout(new BorderLayout());
        JLabel lbl = new JLabel("Welcome, " + userId + "!", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 28));
        add(lbl, BorderLayout.CENTER);
    }

    // WindowBuilder default constructor (shows Guest)
    public WelcomePanel() {
        this("Guest");
    }
}
