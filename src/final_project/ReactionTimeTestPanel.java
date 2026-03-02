package final_project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import java.util.concurrent.CancellationException;

// Reaction Time Test: 5 trials. Score = average reaction time in ms
public class ReactionTimeTestPanel extends AbilityTestPanel {
    private static final long serialVersionUID = 1L;

    // Panel state machine
    private enum State { IDLE_RED, WAITING_RED, GREEN_READY, DONE }

    // Test configuration
    private static final int TRIALS = 5;

    // Random delay generator for the red waiting time
    private final Random rnd = new Random();

    // Center instructions
    private JTextArea text;

    // Background worker that waits for a random delay, then switches to GREEN
    private SwingWorker<Void, Void> waitWorker;

    // Current state
    private State state = State.IDLE_RED;

    // Progress and score accumulation
    private int doneTrials = 0;
    // Sum of measured reaction times in ms
    private long sumMs = 0;
    // Timestamp (nanoTime) when the panel turns GREEN for the current trial
    private long greenNano = -1;

    // Create panel and initialize UI + initial state
    public ReactionTimeTestPanel(TestHost host) {
        super(host);
        buildUI();
        resetTest();
    }
    
    // WindowBuilder default constructor
    public ReactionTimeTestPanel() {
        this(null);
    }

    // Build Swing components (simple text + background color)
    private void buildUI() {
        setLayout(new BorderLayout());
        setOpaque(true);

        text = new JTextArea();
        text.setEditable(false);
        text.setFocusable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setFont(new Font("Arial", Font.BOLD, 20));

        add(text, BorderLayout.CENTER);

        // Handle clicks on the panel itself.
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                handlePanelClick();
            }
        };
        addMouseListener(ma);
        text.addMouseListener(ma); 
    }

    // Main click handler: routes behavior based on current state
    private void handlePanelClick() {
        // After finishing all trials, clicks do nothing (user should press Reset)
        if (state == State.DONE) {
            info("Finished 5 trials. Press Reset to try again.");
            return;
        }

        // In idle red state: click starts a new trial (random wait begins)
        if (state == State.IDLE_RED) {
            startTrial();
            return;
        }

        // In green state: click measures reaction time and records the trial
        if (state == State.GREEN_READY) {
            long now = System.nanoTime();
            long ms = (now - greenNano) / 1_000_000L;

            sumMs += ms;
            doneTrials++;

            // progress: doneTrials/5
            updateStatus("trial " + doneTrials + "/5", doneTrials, 5);

            setBackground(Color.RED);

            if (doneTrials >= TRIALS) {
            	// All trials done -> compute average and save result
                state = State.DONE;
                long avg = sumMs / TRIALS;

                text.setText("Reaction Time Test\n"
                           + "All trials finished.\n"
                           + "Average: " + avg + " ms\n"
                           + "\nPress Reset to try again.");

                // Save to ranking file via host
                if (host != null) {
                	host.submitResult(new TestResult(getKey(), uid(), (double) avg, TestResult.today()));
                }
                info("[" + uid() + "] Reaction AVG: " + avg + " ms");
            } 
            else {
            	// Show trial result and wait for next click to start next trial
                state = State.IDLE_RED;
                text.setText("Reaction Time Test\n"
                           + "Trial " + doneTrials + " result: " + ms + " ms\n"
                           + "\nClick to start Trial " + (doneTrials + 1) + "/5\n"
                           + "Wait until the panel turns GREEN.");
            }
            return;
        }
        
        // If user clicks while still RED waiting -> 'Too soon' (restart the same trial)
        if (state == State.WAITING_RED) {
        	// Cancel the current waiting worker (so it will not turn green later)
            stopWaitWorker();

            text.setText("Too soon!\nWait until the panel turns GREEN.\nRestarting this trial...");
            info("Too soon! Please wait for GREEN.");

            // Show the message for 1 second, then restart the same trial
            waitWorker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Thread.sleep(1000);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        startTrial(); // restart without increasing doneTrials
                    } catch (Exception ex) {
                    	// ignore
                    }
                }
            };
            waitWorker.execute();
            return;
        }


    }

    // Start one trial: set RED, wait random time in background, then switch to GREEN
    private void startTrial() {
        stopWaitWorker();

        state = State.WAITING_RED;
        setBackground(Color.RED);

        int next = doneTrials + 1;
        
        text.setText("Reaction Time Test\n"
                   + "Trial " + next + "/5\n"
                   + "Wait until the panel turns GREEN...\n"
                   + "Then click as fast as you can.");

        updateStatus("trial " + doneTrials + "/5", doneTrials, 5);

        // Random wait in milliseconds
        final int delay = 1000 + rnd.nextInt(2000); 

        // SwingWorker: sleeps in background so the UI stays responsive
        waitWorker = new SwingWorker<Void, Void>() {
        	// Background: wait, then exit (done() will run on the EDT)
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(delay);
                return null;
            }

            // EDT: after waiting finishes, switch to GREEN unless cancelled
            @Override
            protected void done() {
                try {
                    get(); // throws CancellationException if cancelled
                    // If not cancelled, change to green
                    state = State.GREEN_READY;
                    setBackground(Color.GREEN);
                    text.setText("CLICK NOW!");
                    greenNano = System.nanoTime();
                } catch (CancellationException ex) {
                	// Cancelled because user clicked too early or reset
                } catch (Exception ex) {
                    // If an exception occurs, return to the initial state
                    state = State.IDLE_RED;
                    setBackground(Color.RED);
                }
            }
        };
        waitWorker.execute();
    }

    // Cancel and clear the current waiting worker safely
    private void stopWaitWorker() {
        if (waitWorker != null) {
            waitWorker.cancel(true);
            waitWorker = null;
        }
    }

    // Get CardLayout key used by AppFrame
    @Override
    public String getKey() {
        return TestScreen.KEY_REACTION;
    }

    // Get name shown in the menu
    @Override
    public String getDisplayName() {
        return "Reaction Time Test";
    }

    // Called when the panel is shown -> keep the status bar in sync
    @Override
    public void onShow() {
        updateStatus("trial " + doneTrials + "/5", doneTrials, 5);
    }

    // Reset everything back to the initial red state
    @Override
    public void resetTest() {
        stopWaitWorker();

        state = State.IDLE_RED;
        doneTrials = 0;
        sumMs = 0;
        greenNano = -1;

        setBackground(Color.RED);
        text.setText("Reaction Time Test (5 trials)\n"
                   + "Click to start Trial 1/5\n"
                   + "Wait until the panel turns GREEN.");

        updateStatus("trial 0/5", 0, 5);
    }
}
