package final_project;

import java.awt.EventQueue;

import javax.swing.JDialog;

// Program launcher: shows LoginDialog first, then opens the main AppFrame
public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // 1) Show login dialog
                LoginDialog login = new LoginDialog();
                login.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                login.setVisible(true);

                // 2) If login failed or user closed the dialog, exit the program
                if (!login.loginSuccess) {
                    System.exit(0);
                }

                // 3) Login succeeded -> open the main window with the user id
                AppFrame frame = new AppFrame(login.loginId);
                frame.setVisible(true);
            }
        });
    }
}
