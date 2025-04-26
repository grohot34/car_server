package util;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class BackupManager extends Component {

    public static void createBackupWithProgress(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите папку для сохранения резервной копии");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int userSelection = fileChooser.showSaveDialog(parent);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedDirectory = fileChooser.getSelectedFile();


        // Диалог с прогрессбаром
        JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Создание резервной копии", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("Создание резервной копии...");
        progressBar.setStringPainted(true);
        progressDialog.add(progressBar);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(parent);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String mysqldumpPath = "\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe\"";
                String dbUser = "root";
                String dbPassword = "root";
                String dbName = "car_dealing";
                String backupPath = selectedDirectory.getAbsolutePath() + File.separator + dbName + "_backup.sql";
                String command = String.format("%s -u%s -p%s --add-drop-database --databases %s -r \"%s\"",
                        mysqldumpPath, dbUser, dbPassword, dbName, backupPath);

                Process runtimeProcess = Runtime.getRuntime().exec(command);
                int processComplete = runtimeProcess.waitFor();

                if (processComplete == 0) {
                    JOptionPane.showMessageDialog(parent, "Резервная копия создана успешно!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parent, "Ошибка при создании резервной копии!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done() {
                progressDialog.dispose();
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }


}
