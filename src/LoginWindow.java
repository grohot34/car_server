import model.User;

import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JFrame {
    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;

    public LoginWindow(DBManager dbManager) {
        setTitle("Авторизация");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // по центру экрана
        setLayout(new GridLayout(4, 1));

        loginField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Войти");
        registerButton = new JButton("Зарегистрироваться");

        add(new JLabel("Логин:"));
        add(loginField);
        add(new JLabel("Пароль:"));
        add(passwordField);
        add(loginButton);
        add(registerButton);

        loginButton.addActionListener(e -> handleAuth("LOGIN", dbManager));
        registerButton.addActionListener(e -> handleAuth("REGISTER", dbManager));
    }

    private void handleAuth(String commandType, DBManager dbManager) {
        String login = loginField.getText();
        String password = new String(passwordField.getPassword());
        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Поля не должны быть пустыми!");
            return;
        }

        if (dbManager.isUserBlocked(login)) {
            JOptionPane.showMessageDialog(this, "Ваша учетная запись заблокирована. Обратитесь к администратору.");
            return;
        }

        String role = dbManager.getUserRoleByLogin(login);
        int id = dbManager.getUserIdByLogin(login);
        System.out.println(role);
        String command = commandType + ":" + login + ":" + password;
        String response = UserSender.sendCommand(command);

        User user = new User(id, login, role);
        if (response.startsWith("SUCCESS")) {
            JOptionPane.showMessageDialog(this, "Успешно!");
            switch (role) {
                case "CLIENT":
                    ClientWindow clientWindow = new ClientWindow(user);
                    clientWindow.setVisible(true);
                    break;
                case "ADMIN":
                    AdminWindow adminWindow = new AdminWindow(user);
                    adminWindow.setVisible(true);
                    break;
                case "MANAGER":
                    ManagerWindow managerWindow = new ManagerWindow(user);
                    managerWindow.setVisible(true);
                    break;
                    default: JOptionPane.showMessageDialog(this, "Неизвестная роль: " + role);
            }
            // Закрытие окна авторизации
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Ошибка: " + response);
        }
    }

    public static void main(String[] args) {
        DBManager dbManager = new DBManager();
        SwingUtilities.invokeLater(() -> new LoginWindow(dbManager).setVisible(true));
    }
}
