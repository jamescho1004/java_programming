package final_project;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

public class AppFrame extends JFrame implements TestHost {

    private static final long serialVersionUID = 1L;

    // UI fields 
    private JPanel contentPane;

    private JMenuBar menuBar;
    private JMenu menuChange;
    private JMenuItem mntmReaction;

    private JMenu menuRank;
    private JMenuItem mntmReactionRanking;

    private JMenuItem mntmClick;
    private JMenuItem mntmClickRanking;

    private JMenu menuHelp;
    private JMenuItem mntmReactionHelp;

    private JPanel topPanel;
    private JLabel lblStage;
    private JProgressBar progressBar;
    private JButton btnReset;

    private JPanel centerPanel;
    private CardLayout cardLayout;

    // App state 
    // Logged-in user id (defaults to Guest) 
    private String userId = "Guest";

    // Card panels (CardLayout screens)
    private WelcomePanel welcomePanel;
    private ReactionTimeTestPanel reactionPanel;
    private ClickSpeedTestPanel clickPanel;

    // Currently visible test screen 
    private TestScreen currentScreen;

    private JMenuItem mntmClickHelp;

    // Default main
    public static void main(String[] args) {
        // Launch the frame on the Swing Event Dispatch Thread (EDT)
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                AppFrame frame = new AppFrame("TestUser");
                frame.setVisible(true);
            }
        });
    }

    // Default constructor 
    public AppFrame() {
        // Close the application when the frame is closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 698, 605);

        // Menu bar 
        // Create the top menu bar and attach it to this frame
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Test menu: switch between screens
        menuChange = new JMenu("Change");
        menuBar.add(menuChange);

        // Menu item: open Reaction Time Test
        mntmReaction = new JMenuItem("ReactionTimeTest");
        menuChange.add(mntmReaction);
        mntmReaction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Switch to the reaction test card
                showCard(TestScreen.KEY_REACTION);
            }
        });

        // Menu item: open Click Speed Test
        mntmClick = new JMenuItem("ClickSpeedTest");
        menuChange.add(mntmClick);
        mntmClick.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Switch to the click test card
                showCard(TestScreen.KEY_CLICK);
            }
        });

        // Rank menu: show Top10 results from ranking file 
        menuRank = new JMenu("Rank");
        menuBar.add(menuRank);

        // Ranking dialog for Reaction test (Top10)
        mntmReactionRanking = new JMenuItem("ReactionTimeTest Top10");
        menuRank.add(mntmReactionRanking);
        mntmReactionRanking.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Load + filter ranking file and show Top10 for REACTION
                showTop10Dialog(TestScreen.KEY_REACTION);
            }
        });

        // Ranking dialog for Click test (Top10)
        mntmClickRanking = new JMenuItem("ClickSpeedTest Top10");
        menuRank.add(mntmClickRanking);
        mntmClickRanking.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Load + filter ranking file and show Top10 for CLICK
                showTop10Dialog(TestScreen.KEY_CLICK);
            }
        });

        // Help menu
        menuHelp = new JMenu("Help");
        menuBar.add(menuHelp);

        // Help item: how to play Reaction Time Test
        mntmReactionHelp = new JMenuItem("ReactionTimeTest Guide");
        mntmReactionHelp.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		String guide =
        		        "Reaction Time Test Guide\n\n" +
        		        "1) Start the test and focus on the center of the screen.\n" +
        		        "2) The screen will first turn RED.\n" +
        		        "3) After a short random delay, the color will change to GREEN.\n" +
        		        "4) Click as quickly as possible when the screen turns GREEN.\n" +
        		        "5) Your reaction time (in milliseconds) will be recorded.\n\n" +
        		        "Notes:\n" +
        		        "- Clicking while the screen is RED may be treated as a false start.\n" +
        		        "- Stay focused and react only to the GREEN signal.";
                JOptionPane.showMessageDialog(AppFrame.this, guide, "Reaction Time Test Guide", JOptionPane.INFORMATION_MESSAGE);
        	}
        });
        menuHelp.add(mntmReactionHelp);

        // Help item: how to play Click Speed Test
        mntmClickHelp = new JMenuItem("ClickSpeedTest Guide");
        mntmClickHelp.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		String guide =
        		        "Click Speed Test Guide\n\n" +
        		        "1) Start the test and focus on the center of the screen.\n" +
        		        "2) During the trial, the panel becomes GREEN and counts your clicks.\n" +
        		        "2) Click as fast as you can while the test is running.\n" +
        		        "3) Keep clicking until the time limit ends.\n" +
        		        "4) Your result will be calculated based on your clicking speed (cps).\n\n" +
        		        "Tips:\n" +
        		        "- Maintain a steady clicking rhythm.\n" +
        		        "- Try not to move the cursor unnecessarily during the test.";
                JOptionPane.showMessageDialog(AppFrame.this, guide, "Click Speed Test Guide", JOptionPane.INFORMATION_MESSAGE);
        	}
        });
        menuHelp.add(mntmClickHelp);

        // Root container: top status bar + center screens
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        // BorderLayout: NORTH = topPanel, CENTER = centerPanel
        contentPane.setLayout(new BorderLayout(0, 0));

        // Top status bar (stage + progress + Reset) 
        // Top panel uses FlowLayout to keep label + progress + button in one row
        topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        contentPane.add(topPanel, BorderLayout.NORTH);

        // Stage label: shows current progress text 
        lblStage = new JLabel("trial 0/5");
        topPanel.add(lblStage);

        // Progress bar: shows progress for current test
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(5);
        progressBar.setValue(0);
        progressBar.setString("0/5");
        topPanel.add(progressBar);

        // Reset button: resets the currently active test screen
        btnReset = new JButton("Reset");
        topPanel.add(btnReset);
        btnReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Reset only works when a test screen is active (Welcome has no reset)
                if (currentScreen != null) currentScreen.resetTest();
            }
        });

        // Center area 
        // CardLayout container for switching screens.
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        contentPane.add(centerPanel, BorderLayout.CENTER);

        // Register cards (Welcome / Reaction / Click)
        // Create each screen panel
        welcomePanel = new WelcomePanel(userId);
        reactionPanel = new ReactionTimeTestPanel(this);
        clickPanel = new ClickSpeedTestPanel(this);

        // Add cards to CardLayout with fixed keys
        centerPanel.add(welcomePanel, "WELCOME");
        centerPanel.add(reactionPanel, reactionPanel.getKey()); // "REACTION"
        centerPanel.add(clickPanel, clickPanel.getKey());       // "CLICK"

        // Start on the Welcome screen
        setTopStatus("trial 0/5", 0, 5);
        showCard("WELCOME");
    }

    // Constructor that sets the logged-in user id and refreshes the welcome screen
    public AppFrame(String userId) {
        this();

        // Normalize the passed userId 
        if (userId != null && userId.trim().length() > 0) this.userId = userId.trim();
        else this.userId = "Guest";

        // Update frame title to include current user.
        setTitle("Ability Test - " + this.userId);

        // Replace only the WelcomePanel so it reflects the latest userId
        centerPanel.remove(welcomePanel);
        welcomePanel = new WelcomePanel(this.userId);
        centerPanel.add(welcomePanel, "WELCOME");

        // Refresh the container so the replaced panel is applied
        centerPanel.revalidate();
        centerPanel.repaint();

        // Show the welcome screen immediately after login
        showCard("WELCOME");
    }

    // Switch the visible CardLayout screen and update currentScreen reference
    private void showCard(String key) {
        // Leaving current test screen? stop its background work (timers/workers)
        TestScreen prev = currentScreen;
        if (prev != null && !prev.getKey().equals(key)) {
            prev.resetTest();
        }
        
        // Show requested card
        cardLayout.show(centerPanel, key);

        // Track which test screen is active 
        if (TestScreen.KEY_REACTION.equals(key)) currentScreen = reactionPanel;
        else if (TestScreen.KEY_CLICK.equals(key)) currentScreen = clickPanel;
        else currentScreen = null;

        // Call onShow() when a test screen becomes visible. otherwise reset the top bar
        if (currentScreen != null) currentScreen.onShow();
        else setTopStatus("trial 0/5", 0, 5);
    }

    // Ranking dialog (Top10) 
    private void showTop10Dialog(String testKey) {
        // Load all saved results from file
        List<TestResult> all = RankingManager.loadAll();
        
        // 1) Keep only Top10 using a PriorityQueue 
        PriorityQueue<TestResult> pq = new PriorityQueue<>(10, (a, b) -> {
            // Configure comparator so pq.peek() represents the current 'worst' within Top10
            if (TestScreen.KEY_REACTION.equals(testKey)) return Double.compare(b.value, a.value); // larger is worse (used to keep Top10 best)
            return Double.compare(a.value, b.value); // smaller is worse (used to keep Top10 best)
        });

        // Scan all results, keep only those that match the requested testKey
        for (TestResult r : all) {
            if (r == null || !testKey.equals(r.testKey)) continue;

            if (pq.size() < 10) {
            	// Fill until we have 10 results
                pq.offer(r);
            } 
            else {
            	// Replace current worst if the new record is better
                TestResult worst = pq.peek();

                boolean better =
                        TestScreen.KEY_REACTION.equals(testKey)
                                ? r.value < worst.value   // reaction: smaller is better
                                : r.value > worst.value;  // click: larger is better
                                
                if (better) {
                    pq.poll();
                    pq.offer(r);
                }
            }
        }

        // 2) Convert pq to a list, then sort in 'best first' order for display
        List<TestResult> filtered = new ArrayList<>(pq);

        // Sort results for ranking display:
        // Reaction test: smaller value ranks higher
        // Click test: larger value ranks higher
        filtered.sort((a, b) -> {
            if (TestScreen.KEY_REACTION.equals(testKey))
                return Double.compare(a.value, b.value);   // smaller is better
            return Double.compare(b.value, a.value);       // larger is better
        });

        // Number of ranking entries (up to Top 10)
        int n = filtered.size();

        // Unit string based on test type
        String unit = TestScreen.KEY_REACTION.equals(testKey) ? "ms" : "cps";

        // Fixed column width for user ID 
        int idWidth = 16;

        // Build ranking text using StringBuilder 
        StringBuilder sb = new StringBuilder();

        // Title line
        sb.append("[").append(testKey).append(" TOP 10]").append("\n\n");

        if (n == 0) {
            // Case: no ranking data available
            sb.append("There are no records yet.\n");
        } else {
            // Header row (ID / SCORE / DATE)
            String headerFmt = "    %-" + idWidth + "s  %10s  %s%n";
            sb.append(String.format(headerFmt, "ID", "SCORE(" + unit + ")", "DATE"));

            // Separator line (manually generated to avoid String.repeat())
            int lineLen = 6 + idWidth + 2 + 10 + 2 + 10;
            for (int i = 0; i < lineLen; i++) sb.append("-");
            sb.append("\n");

            // Body rows: one line per ranking entry
            String rowFmt = "%2d) %-" + idWidth + "s  %10.2f  %s%n";
            for (int i = 0; i < n; i++) {
                TestResult r = filtered.get(i);

                // Safely handle null userId
                String id = (r.userId == null) ? "" : r.userId;

                sb.append(String.format(rowFmt, (i + 1), id, r.value, r.date));
            }
        }

        // Wrap the plain text ranking in HTML <pre> for fixed-width alignment
        String html =
                "<html><pre style='font-family:monospace; font-size:12px;'>"
                        + escapeHtml(sb.toString())
                        + "</pre></html>";

        // Show ranking dialog
        JOptionPane.showMessageDialog(this, html, testKey + " Ranking", JOptionPane.INFORMATION_MESSAGE);
    }

    // Update the top label + progress bar
    @Override
    public void setTopStatus(String stageText, int value, int max) {
        lblStage.setText(stageText);
        progressBar.setMaximum(Math.max(0, max));
        progressBar.setValue(Math.max(0, value));
        progressBar.setString(value + "/" + max);
    }

    // Show an information dialog
    @Override
    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Result", JOptionPane.INFORMATION_MESSAGE);
    }

    // Show an error dialog
    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Current logged-in user id for saving results
    @Override
    public String getUserId() {
        return userId;
    }

    // Persist the result through RankingManager
    @Override
    public void submitResult(TestResult result) {
        RankingManager.saveResult(result);
    }

    // Escape special characters so strings are safe inside HTML dialogs
    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

}

