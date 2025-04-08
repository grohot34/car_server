import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginWindow extends JFrame {
    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;

    public LoginWindow() {
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

        loginButton.addActionListener(e -> handleAuth("LOGIN"));
        registerButton.addActionListener(e -> handleAuth("REGISTER"));
    }

    private void handleAuth(String commandType) {
        String login = loginField.getText();
        String password = new String(passwordField.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Поля не должны быть пустыми!");
            return;
        }

        String command = commandType + ":" + login + ":" + password;
        String response = ClientSender.sendCommand(command);
        System.out.println(command);
        System.out.println(response);
        if ("SUCCESS".equals(response)) {
            JOptionPane.showMessageDialog(this, "Успешно!");
            // тут можно открыть следующее окно (например, меню пользователя)
            new MainWindow().setVisible(true);

            // Закрытие окна авторизации
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Ошибка: " + response);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
    }
}
