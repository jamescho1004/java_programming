package final_project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// File I/O helper for ranking.txt (append save + full load)
public class RankingManager {
	

    // Save next to the program working directory
    private static final String FILE_PATH = "ranking.txt";

    // Save one result 
    public static void saveResult(TestResult r) {
        if (r == null) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(r.toFileString());   // ex: userId TESTKEY 123.0 2025-12-18
            bw.newLine();
        } catch (IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Failed to save ranking file.", "File Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    //Load and parse all saved results from the file
    public static List<TestResult> loadAll() {
        List<TestResult> list = new ArrayList<>();

        File file = new File(FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                	// Parse one saved line into a TestResult object and add it to the list
                	TestResult tr = TestResult.fromFileString(line);
                	if (tr != null) list.add(tr);
                } catch (Exception parseError) {
                	// If a line is malformed, skip it and continue loading the rest
                }
            }
        } catch (IOException e) {
        	javax.swing.JOptionPane.showMessageDialog(null, "Failed to load ranking file.", "File Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }

        return list;
    }
}
