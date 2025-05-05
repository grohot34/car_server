import model.Car;
import model.Order;
import model.OrderInfo;
import model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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

    public String getUserRoleByLogin(String login) {
        String role = "CLIENT";
        String query = "SELECT id, role FROM users WHERE login = ?";

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

    public int getUserIdByLogin(String login) {
        int id = 0;
        String query = "SELECT id FROM users WHERE login = ?";


        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Устанавливаем параметр в запросе
            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
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

    public void loadTableData(JTable table, String sqlQuery) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // очищаем старые данные

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = stmt.executeQuery()) {

            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ошибка загрузки данных: " + e.getMessage());
        }
    }
    public int getCarIdByBrandAndModel(String brand, String model) {
        String query = "SELECT id FROM cars WHERE brand = ? AND model = ? LIMIT 1";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, brand);
            stmt.setString(2, model);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public boolean updatePassword(int userId, String oldPassword, String newPassword) {
        String checkSql = "SELECT password_hash FROM users WHERE id = ?";
        String updateSql = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (Connection conn = getDbConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String currentPass = rs.getString("password_hash");
                oldPassword = hashPassword(oldPassword);
                if (!currentPass.equals(oldPassword)) {
                    return false; // неверный старый пароль
                }

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    newPassword = hashPassword(newPassword);
                    updateStmt.setString(1, newPassword);
                    updateStmt.setInt(2, userId);
                    updateStmt.executeUpdate();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        try (Connection conn = getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cars")) {

            while (rs.next()) {
                cars.add(new Car(
                        rs.getInt("id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getDouble("price"),
                        rs.getInt("warranty_years"),
                        rs.getBoolean("available")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    public boolean createOrder(Order order) {
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO orders_to_sales (client_id, car_id, order_date, status, payment_method, total_price) " +
                             "VALUES (?, ?, ?, ?, ?, ?)")) {

            stmt.setInt(1, order.getClientId());
            stmt.setInt(2, order.getCarId());
            stmt.setDate(3, java.sql.Date.valueOf(order.getOrderDate()));
            stmt.setString(4, order.getStatus());
            stmt.setString(5, order.getPaymentMethod());
            stmt.setDouble(6, order.getTotalPrice());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();  // Можно также логировать
            return false;
        }
    }

    public List<OrderInfo> getOrdersForClient(int clientId) {
        // Ваш SQL-запрос, который возвращает информацию о заказах и автомобилях
        String query = """
                SELECT o.id, c.brand, c.model, c.year, o.total_price, o.payment_method, o.order_date, o.status
                FROM orders_to_sales o
                JOIN cars c ON o.car_id = c.id
                WHERE o.client_id = ?
                ORDER BY o.order_date DESC
                """;

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, clientId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<OrderInfo> orders = new ArrayList<>();
                while (rs.next()) {
                    // Создание объекта Order с данными из базы данных
                    OrderInfo order = new OrderInfo(
                            rs.getInt("id"),
                            rs.getString("brand"),
                            rs.getString("model"),
                            rs.getInt("year"),
                            rs.getDouble("total_price"),
                            rs.getString("payment_method"),
                            rs.getDate("order_date").toLocalDate(),
                            rs.getString("status")
                    );
                    // Добавление заказа в список
                    orders.add(order);
                }
                return orders;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>(); // Возвращаем пустой список в случае ошибки
        }
    }
    public boolean updateOrderStatus(int orderId) {
        String sql = "UPDATE orders_to_sales SET status = 'Отменён' WHERE id = ?";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0; // Возвращаем true, если заказ был успешно обновлён
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}




