package final_project;

import java.time.LocalDate;

// One saved test result line in ranking.txt
public class TestResult {
    // Test identifier: "REACTION" or "CLICK"
    public final String testKey;

    // User id (from LoginDialog)
    public final String userId;

    // Score value: Reaction uses average ms, Click uses average cps
    public final double value;

    // Saved date string (LocalDate.now().toString()).
    public final String date;

    // Constructor
    public TestResult(String testKey, String userId, double value, String date) {
        this.testKey = testKey;
        this.userId = userId;
        this.value = value;
        this.date = date;
    }

    // For file saving (space-separated)
    public String toFileString() {
        return userId + " " + testKey + " " + value + " " + date;
    }

    // For file loading (parse from a saved line)
    static TestResult fromFileString(String line) {
        if (line == null) return null;
        line = line.trim();
        if (line.isEmpty()) return null;

        String[] p = line.split("\\s+");
        if (p.length < 4) return null;   // Guard

        try {
            String userId = p[0];
            String testKey = p[1];
            double value = Double.parseDouble(p[2]);
            String date = p[3];
            return new TestResult(testKey, userId, value, date);
        } catch (Exception e) {
            return null; // Ignore parse errors
        }
    }

    // Today's date as string
    public static String today() {
        return LocalDate.now().toString();
    }
}
