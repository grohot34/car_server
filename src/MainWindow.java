import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    public MainWindow(String title, String message) {
        setTitle(title);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel(message, JLabel.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.PLAIN, 20));

        add(welcomeLabel);
    }
}

// Окно для клиента
class ClientWindow extends MainWindow {
    public ClientWindow() {
        super("Окно клиента", "Добро пожаловать, Клиент!");
        // можно добавить клиентские компоненты
    }
}
// Окно для клиента
class ManagerWindow extends MainWindow {
    public ManagerWindow() {
        super("Окно менеджера", "Добро пожаловать, Менеджер!");
        // можно добавить клиентские компоненты
    }
}

// Окно для администратора
class AdminWindow extends MainWindow {
    public AdminWindow() {
        super("Окно администратора", "Добро пожаловать, Администратор!");
        // можно добавить админские компоненты
    }
}
