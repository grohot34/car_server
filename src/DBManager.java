import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DBManager {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB_NAME = "car_dealing";
    private static final String LOGIN = "root";
    private static final String PASSWORD = "root";

    // Метод для получения соединения с базой данных
    public Connection getDbConnection() throws SQLException {
        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME;
        return DriverManager.getConnection(url, LOGIN, PASSWORD);
    }

    // Метод для хеширования пароля
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            System.out.println(digest);
            byte[] hashedBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean checkPassword(String inputPassword, String storedHash) {
        String hashedInputPassword = hashPassword(inputPassword);
        return hashedInputPassword != null && hashedInputPassword.equals(storedHash);
    }

    // Метод для вставки пользователя в базу данных
    public void insertUser(String login, String password) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            System.out.println("Ошибка хеширования пароля.");
            return;
        }

        String sql = "INSERT INTO `users` (login, password_hash) VALUES (?, ?)";
        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(sql)) {

            prSt.setString(1, login);
            prSt.setString(2, hashedPassword);

            prSt.executeUpdate();
            System.out.println("Пользователь успешно добавлен: " + login);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для получения всех пользователей из базы данных
    public ArrayList<String> getUsers() {
        ArrayList<String> users = new ArrayList<>();
        String sql = "SELECT login, password_hash FROM `users`";

        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(sql);
             ResultSet resultSet = prSt.executeQuery()) {

            while (resultSet.next()) {
                String user = "Login: " + resultSet.getString("login") + ", Password: " + resultSet.getString("password_hash");
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    // Метод для вывода всех пользователей
    public void displayUsers() {
        ArrayList<String> users = getUsers();
        if (users.isEmpty()) {
            System.out.println("Нет пользователей для отображения.");
        } else {
            System.out.println("Список пользователей:");
            for (String user : users) {
                System.out.println("- " + user);
            }
        }
    }

    public String getUserRoleByLogin(String login) {
        String role = "CLIENT";
        String query = "SELECT role FROM users WHERE login = ?";


        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Устанавливаем параметр в запросе
            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    role = rs.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return role;
    }
}