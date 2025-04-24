import model.User;

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

    public boolean isUserBlocked(String login) {
        String sql = "SELECT is_blocked FROM users WHERE login = ?";
        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("is_blocked") == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // по умолчанию считаем, что не заблокирован
    }


    public boolean doesLoginExist(String login) {
        String sql = "SELECT COUNT(*) FROM users WHERE login = ?";
        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Метод для вставки пользователя в базу данных
    public void insertUserWithRole(String login, String password, String role) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            System.out.println("Ошибка хеширования пароля.");
            return;
        }

        String sql = "INSERT INTO `users` (login, password_hash, role, is_blocked) VALUES (?, ?, ?, ?)";
        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(sql)) {

            prSt.setString(1, login);
            prSt.setString(2, hashedPassword);
            prSt.setString(3, role);
            prSt.setInt(4, 0);

            prSt.executeUpdate();
            System.out.println("Пользователь успешно добавлен: " + login);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для получения всех пользователей из базы данных
    public ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();
        String sql = "SELECT id, login, password_hash, role, is_blocked FROM `users`";



        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(sql);
             ResultSet resultSet = prSt.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String login = resultSet.getString("login");
                String role = resultSet.getString("role");
                int isBlocked = resultSet.getInt("is_blocked");
                users.add(new User(id, login, role, isBlocked));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }


    // Метод для вывода всех пользователей
    public void displayUsers() {
        ArrayList<User> users = getUsers();
        if (users.isEmpty()) {
            System.out.println("Нет пользователей для отображения.");
        } else {
            System.out.println("Список пользователей:");
            for (User user : users) {
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

    public void updateUserRole(int userId, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(sql)) {

            prSt.setString(1, newRole);
            prSt.setInt(2, userId);
            prSt.executeUpdate();
            System.out.println("Роль пользователя обновлена. ID: " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUserById(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = getDbConnection();
             PreparedStatement prSt = connection.prepareStatement(sql)) {

            prSt.setInt(1, userId);
            prSt.executeUpdate();
            System.out.println("Пользователь удалён. ID: " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUserBlocked(String login, int blocked) {
        String sql = "UPDATE users SET is_blocked = ? WHERE login = ?";

        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, blocked);
            stmt.setString(2, login);
            stmt.executeUpdate();
            System.out.println("Статус блокировки изменён для пользователя: " + login);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}