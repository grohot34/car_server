import java.sql.*;

public class AuthManager {

    // Метод для регистрации пользователя
    public static boolean register(DBManager dbManager, String login, String password) {
        String hash = DBManager.hashPassword(password);  // Хешируем пароль с солью
        String query = "INSERT INTO users (login, password_hash) VALUES (?, ?)";

        try (Connection conn = dbManager.getDbConnection(); // Используем переданный объект DBManager
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, login);
            ps.setString(2, hash);
            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("⚠️ Пользователь с таким логином уже существует.");
            return false;
        } catch (SQLException e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
            return false;
        }
    }

    // Метод для входа пользователя
    public static boolean login(DBManager dbManager, String login, String password) {
        System.out.println("Login");
        String query = "SELECT password_hash FROM users WHERE login = ?";  // Извлекаем только хеш пароля

        try (Connection conn = dbManager.getDbConnection(); // Используем переданный объект DBManager
             PreparedStatement ps = conn.prepareStatement(query)) {
            System.out.println("In try");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();

            // Если пользователь найден, сравниваем хеши паролей
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                System.out.println("Hash: " + storedHash);
                System.out.println(storedHash);
                return DBManager.checkPassword(password, storedHash);  // Проверяем пароль
            }

            return false;  // Пользователь не найден

        } catch (SQLException e) {
            System.out.println("Ошибка входа: " + e.getMessage());
            return false;
        }
    }
}


