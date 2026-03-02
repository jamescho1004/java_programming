package final_project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextArea;
import javax.swing.Timer;

// Click Speed Test: 5 trials, each trial is 5 seconds, Score = average clicks per second (cps)
public class ClickSpeedTestPanel extends AbilityTestPanel {
    private static final long serialVersionUID = 1L;

    // Panel state machine, prevents invalid clicks during transitions
    private enum State { IDLE_RED, RUNNING_GREEN, COOL_DOWN, DONE }

    // Test configuration
    private static final int TRIALS = 5;
    private static final int DURATION_SEC = 5;

    // Center instructions 
    private JTextArea text;

    // 1-second timer during a running trial
    private Timer tick;

    // Short cooldown between trials 
    private Timer coolTimer;

    // Current state
    private State state = State.IDLE_RED;

    // Progress counters and score accumulation
    private int doneTrials = 0;      // Number of completed trials 
    private int secondsLeft = DURATION_SEC;

    // Click count within the current trial
    private int clicks = 0;

    // Sum of cps over trials
    private double sumCps = 0.0;

    // Create panel and initialize UI + initial state
    public ClickSpeedTestPanel(TestHost host) {
        super(host);
        buildUI();
        resetTest();
    }

    // WindowBuilder default constructor
    public ClickSpeedTestPanel() {
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

        // Count mouse presses on the panel itself
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                handlePanelClick();
            }
        };
        addMouseListener(ma);
        text.addMouseListener(ma); 
    }

    // Main click handler
    private void handlePanelClick() {
        // During cooldown, ignore clicks to avoid double-counting
    	if (state == State.COOL_DOWN) return;

        // After finishing all trials, clicks do nothing 
        if (state == State.DONE) {
            info("Finished 5 trials. Press Reset to try again.");
            return;
        }

        // In idle red state: start a new trial
        if (state == State.IDLE_RED) {
            startTrial();
            return;
        }

        // While running: count clicks
        if (state == State.RUNNING_GREEN) {
            clicks++;
            updateRunningText();
        }
    }

    // Start one 5-second trial: turn GREEN, reset counters, and start the 1-second timer
    private void startTrial() {
        state = State.RUNNING_GREEN;
        clicks = 0;
        secondsLeft = DURATION_SEC;

        setBackground(Color.GREEN);
        updateRunningText();

        stopTick();

        // Every second: update countdown and end the trial at 0
        tick = new Timer(1000, e -> {
            secondsLeft--;
            updateRunningText();

            if (secondsLeft <= 0) {
                endTrial();
            }
        });
        tick.start();
    }

    // Update the center text while a trial is running
    private void updateRunningText() {
        int next = doneTrials + 1;
        text.setText("Click Speed Test\n"
                   + "Trial " + next + "/5\n"
                   + "Click as fast as you can!\n"
                   + "Time left: " + secondsLeft + "s\n"
                   + "Clicks: " + clicks);
    }

    // Finish one trial: compute cps, accumulate, update status, and either move to next trial or end
    private void endTrial() {
        stopTick();

        double cps = clicks / (double) DURATION_SEC;
        sumCps += cps;
        doneTrials++;

        updateStatus("trial " + doneTrials + "/5", doneTrials, 5);

        setBackground(Color.RED);

        if (doneTrials >= TRIALS) {
        	// All trials done -> compute average and save result
            state = State.DONE;
            double avgCps = sumCps / TRIALS;

            text.setText("Click Speed Test\n"
                       + "All trials finished.\n"
                       + "Average: " + String.format("%.2f", avgCps) + " clicks/sec\n"
                       + "\nPress Reset to try again.");

            // Save to ranking file via host
            if (host != null) {
            	host.submitResult(new TestResult(getKey(), uid(), avgCps, TestResult.today()));
            }
            info("[" + uid() + "] ClickSpeed AVG: " + String.format("%.2f", avgCps) + " cps");
        } 
        // Not finished -> show short cooldown message, then go back to red idle
        else {
            state = State.COOL_DOWN;

            text.setText("Trial " + doneTrials + " finished.\nRest 2 seconds...");
            
            stopCoolTimer();
            
            // Show results briefly, then return to red idle for the next trial
            coolTimer = new Timer(2000, new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    coolTimer.stop();
                    coolTimer = null;

                    state = State.IDLE_RED;
                    text.setText("Click to start Trial " + (doneTrials + 1) + "/5");
                }
            });
            coolTimer.setRepeats(false);
            coolTimer.start();
        }
    }

    // Stop and clear the running timer safely
    private void stopTick() {
        if (tick != null) {
            tick.stop();
            tick = null;
        }
    }

    // Stop and clear the cooldown timer safely
    private void stopCoolTimer() {
        if (coolTimer != null) {
            coolTimer.stop();
            coolTimer = null;
        }
    }

    // Get CardLayout key used by AppFrame
    @Override
    public String getKey() {
        return TestScreen.KEY_CLICK;
    }

    // Get name shown in the menu.
    @Override
    public String getDisplayName() {
        return "Click Speed Test";
    }

    // Called when the panel is shown, keep the status bar in sync
    @Override
    public void onShow() {
        updateStatus("trial " + doneTrials + "/5", doneTrials, 5);
    }

    // Reset everything back to the initial red state.
    @Override
    public void resetTest() {
        stopTick();
        stopCoolTimer();

        state = State.IDLE_RED;
        doneTrials = 0;
        secondsLeft = DURATION_SEC;
        clicks = 0;
        sumCps = 0.0;

        // Red background indicates 'waiting to start'.
        setBackground(Color.RED);
        text.setText("Click Speed Test (5 trials)\n"
                   + "Click to start Trial 1/5\n"
                   + "Each trial lasts 5 seconds.");

        updateStatus("trial 0/5", 0, 5);
    }
}
