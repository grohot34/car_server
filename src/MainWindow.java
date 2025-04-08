import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    public MainWindow() {
        setTitle("Главное окно");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // по центру экрана

        JLabel welcomeLabel = new JLabel("Добро пожаловать в систему мониторинга!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.PLAIN, 20));

        add(welcomeLabel);
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
//    }
}
