import Request_Response.Request;
import Request_Response.Response;
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

        Request request = new Request(commandType, new String[]{login, password});
        Response response = UserSender.sendRequest(request);

        if (response.isSuccess()) {
            User user = (User) response.getData();
            JOptionPane.showMessageDialog(this, "Успешно!");

            switch (user.getRole()) {
                case "CLIENT" -> new ClientWindow(user).setVisible(true);
                case "ADMIN" -> new AdminWindow(user).setVisible(true);
                case "MANAGER" -> new ManagerWindow(user).setVisible(true);
                default -> JOptionPane.showMessageDialog(this, "Неизвестная роль: " + user.getRole());
            }

            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Ошибка: " + response.getMessage());
        }
    }

    public static void main(String[] args) {
        DBManager dbManager = new DBManager();
        SwingUtilities.invokeLater(() -> new LoginWindow(dbManager).setVisible(true));
    }
}
