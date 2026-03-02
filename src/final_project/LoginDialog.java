package final_project;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

// login dialog (file-based id/password)
public class LoginDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    // Public flags read by Main after the dialog closes
    public boolean loginSuccess = false;
    public String loginId = null;

    // Root content panel for this dialog
    private final JPanel contentPanel = new JPanel();

    // Input fields
    private JTextField textFieldId;
    private JPasswordField textFieldPw;

    // User database file (one line per account: "id password")
    private static final String ACCOUNT_FILE = "accounts.txt";

    // Default main
    public static void main(String[] args) {
        try {
            LoginDialog dialog = new LoginDialog();
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null, "Failed to open Login dialog.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Build the dialog UI
    public LoginDialog() {
    	// Make this dialog modal
    	setModal(true);
    	
        // Set title
        setTitle("Login");
        
        // positioning 
        setBounds(100, 100, 450, 200);

        // Layout: content in center + button bar at the bottom.
        getContentPane().setLayout(new BorderLayout());

        // Set default close operation
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Center area (labels + text fields) 
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        // GridLayout keeps label/field alignment consistent
        contentPanel.setLayout(new GridLayout(2, 1, 0, 0));

        // ID input
        JPanel panelId = new JPanel();
        contentPanel.add(panelId);
        
        // Row1: ID
        JLabel lblUserID = new JLabel("User ID");
        panelId.add(lblUserID);
        
        // add ID to textFieldId
        textFieldId = new JTextField();
        textFieldId.setColumns(12);
        panelId.add(textFieldId);

        // Password input
        JPanel panelPw = new JPanel();
        contentPanel.add(panelPw);
        
        // Row2: Password
        JLabel lblPassword = new JLabel("Password");
        panelPw.add(lblPassword);
        
        // add Password to textFieldPw
        textFieldPw = new JPasswordField();
        textFieldPw.setColumns(12);
        panelPw.add(textFieldPw);

        // Bottom area (Login / Create)
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        // Login button: when pressed, attempt login
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        buttonPane.add(btnLogin);
        getRootPane().setDefaultButton(btnLogin);

        // Create button: adds a new account to the file
        JButton btnCreate = new JButton("Create");
        btnCreate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                create();
            }
        });
        buttonPane.add(btnCreate);
    }

    // Login flow:
    // 1) Read id/password input
    // 2) Scan accounts.txt for matching id
    // 3) If password matches -> set flags and close
    private void login() {
    	// Read and normalize inputs
        String id = textFieldId.getText().trim();
        String pw = new String(textFieldPw.getPassword()).trim();

        // Basic validation: empty input
        if (id.length() == 0 || pw.length() == 0) {
            JOptionPane.showMessageDialog(this, "Enter ID and Password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Basic validation: id and password's length must <= 16
        if (id.length() > 16 || pw.length() > 16) {
            JOptionPane.showMessageDialog(this, "ID and Password must be 16 characters or less.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // validation: id or password contains spaces
        if (id.contains(" ") || pw.contains(" ")) {
            JOptionPane.showMessageDialog(this, "ID and Password cannot contain spaces.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // validation: id and password must only English letters and numbers
        if (!id.matches("[A-Za-z0-9]+") || !pw.matches("[A-Za-z0-9]+")) {
            JOptionPane.showMessageDialog(this, "ID and Password must contain only English letters and numbers.", "Error", 
            		JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ensure the user file exists 
        File file = new File(ACCOUNT_FILE);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "No account file. Please Create.", "Login Failed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean found = false;

        // Scan each line: "id password"
        try (FileInputStream fis = new FileInputStream(file);
             Scanner sc = new Scanner(fis)) {

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.length() == 0)
                    continue;

                // Split by space
                String[] token = line.split("\\s+");
                if (token.length < 2)
                    continue;

                String fid = token[0];
                String fpw = token[1];

                // Found the id -> check password
                if (fid.equals(id)) {
                    found = true;
                    if (fpw.equals(pw)) {
                    	// Success: store the logged-in user and close the dialog
                        JOptionPane.showMessageDialog(this, "Login success!", "OK", JOptionPane.INFORMATION_MESSAGE);
                        loginSuccess = true;
                        loginId = id;
                        dispose();
                        return;
                    } else {
                    	// Id exists but password mismatch
                        JOptionPane.showMessageDialog(this, "Wrong password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        textFieldPw.setText("");
                        return;
                    }
                }
            }

        } catch (Exception e) {
        	// Cannot read file
            JOptionPane.showMessageDialog(this, "File read error.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // If the id never appeared in the file
        if (!found) {
            JOptionPane.showMessageDialog(this, "No such account. Please Create.", "Login Failed",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // Create flow:
    // 1) Validate inputs
    // 2) Ensure id is not duplicated
    // 3) Append "id password" line to users.txt
    private void create() {
    	// Read and normalize inputs
        String id = textFieldId.getText().trim();
        String pw = new String(textFieldPw.getPassword()).trim();

        // Basic validation: empty input
        if (id.length() == 0 || pw.length() == 0) {
            JOptionPane.showMessageDialog(this, "Enter ID and Password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Basic validation: id and password's length must <= 16
        if (id.length() > 16 || pw.length() > 16) {
            JOptionPane.showMessageDialog(this, "ID and Password must be 16 characters or less.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // validation: id or password contains spaces
        if (id.contains(" ") || pw.contains(" ")) {
            JOptionPane.showMessageDialog(this,
                "ID and Password cannot contain spaces.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // validation: id and password must only English letters and numbers
        if (!id.matches("[A-Za-z0-9]+") || !pw.matches("[A-Za-z0-9]+")) {
            JOptionPane.showMessageDialog(this, "ID and Password must contain only English letters and numbers.", "Error", 
            		JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ensure the user file exists 
        File file = new File(ACCOUNT_FILE);
        if (file.exists()) {
        	// Open the account file and scan line by line
            try (FileInputStream fis = new FileInputStream(file);
                 Scanner sc = new Scanner(fis)) {

                while (sc.hasNextLine()) {
                	// Read one account line and remove extra spaces
                    String line = sc.nextLine().trim();
                    if (line.length() == 0)
                        continue;

                    // Split the line into tokens: "id password"
                    String[] token = line.split(" ");
                    if (token.length < 1)
                        continue;

                    // If the same ID already exists, stop creation
                    if (token[0].equals(id)) {
                        JOptionPane.showMessageDialog(this, "ID already exists. Try Login.", "Create Failed",
                        JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            } catch (Exception e) {
            	// Cannot read file
                JOptionPane.showMessageDialog(this, "File read error.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Append the new account (id + password) to the account file
        try (PrintWriter out = new PrintWriter(new FileWriter(ACCOUNT_FILE, true))) {
            out.println(id + " " + pw);
        } catch (Exception e) {
        	// Failed to write the new account to the file
            JOptionPane.showMessageDialog(this, "File write error.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Account creation succeeded -> notify the user
        JOptionPane.showMessageDialog(this, "Account created!", "OK", JOptionPane.INFORMATION_MESSAGE);

        // Automatically log in with the newly created account
        loginSuccess = true;
        loginId = id;
        
        // Close the login dialog and return control to Main
        dispose();
    }
}


