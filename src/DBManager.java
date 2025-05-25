import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
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
        return false;
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
                    return false;
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

    public List<Car> getAllCarsDetailed() {
        List<Car> cars = new ArrayList<>();
        try (Connection conn = getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cars")) {

            while (rs.next()) {
                Car car = new Car(
                        rs.getInt("id"),
                        rs.getString("vin"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getDouble("price"),
                        rs.getInt("warranty_years"),
                        rs.getBoolean("available"),
                        rs.getInt("quantity")
                );
                cars.add(car);
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
            e.printStackTrace();
            return false;
        }
    }

    public List<OrderInfo> getOrdersForClient(int clientId) {
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
                    orders.add(order);
                }
                return orders;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public boolean updateOrderStatus(int orderId) {
        String sql = "UPDATE orders_to_sales SET status = 'Отменён' WHERE id = ?";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<OrderInfo> getAllOrders() {
        List<OrderInfo> orders = new ArrayList<>();
        String query = """
            SELECT o.id, o.client_id, c.brand, c.model, c.year,
                   o.total_price, o.payment_method, o.order_date, o.status
            FROM orders_to_sales o
            JOIN cars c ON o.car_id = c.id
            ORDER BY o.order_date DESC
            """;

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                OrderInfo order = new OrderInfo(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getDouble("total_price"),
                        rs.getString("payment_method"),
                        rs.getDate("order_date").toLocalDate(),
                        rs.getString("status")
                );
                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
    public boolean updateOrderStatusWithSales(int orderId, String newStatus) {
        try (Connection conn = getDbConnection()) {
            conn.setAutoCommit(false);

            String orderSql = """
            SELECT o.id, o.client_id, c.brand, c.model, c.year,
                   o.total_price, o.payment_method, o.order_date, o.status
            FROM orders_to_sales o
            JOIN cars c ON o.car_id = c.id
            WHERE o.id = ?
            """;
            int clientId;
            String brand, model;
            double price;
            String currentStatus;
            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                stmt.setInt(1, orderId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) return false;

                clientId = rs.getInt("client_id");
                brand = rs.getString("brand");
                model = rs.getString("model");
                price = rs.getDouble("total_price");
                currentStatus = rs.getString("status");

                if (!"Ожидает подтверждения".equals(currentStatus)) {
                    return false;
                }
            }

            int carId = getCarIdByBrandAndModel(brand, model);

            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT available FROM cars WHERE id = ?")) {
                checkStmt.setInt(1, carId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt("available") == 0) {
                    return false;
                }
            }

            try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE orders_to_sales SET status = ? WHERE id = ?")) {
                updateStmt.setString(1, newStatus);
                updateStmt.setInt(2, orderId);
                updateStmt.executeUpdate();
            }

            if ("Подтверждён".equals(newStatus)) {
                try (PreparedStatement saleStmt = conn.prepareStatement(
                        "INSERT INTO sales (car_id, client_id, sale_date, sale_price) VALUES (?, ?, CURRENT_DATE, ?)")) {
                    saleStmt.setInt(1, carId);
                    saleStmt.setInt(2, clientId);
                    saleStmt.setDouble(3, price);
                    saleStmt.executeUpdate();
                }

                try (PreparedStatement carStmt = conn.prepareStatement("UPDATE cars SET quantity = quantity - 1 WHERE id = ?")) {
                    carStmt.setInt(1, carId);
                    carStmt.executeUpdate();
                }

                try (PreparedStatement checkQtyStmt = conn.prepareStatement("SELECT quantity FROM cars WHERE id = ?")) {
                    checkQtyStmt.setInt(1, carId);
                    ResultSet rs = checkQtyStmt.executeQuery();
                    if (rs.next() && rs.getInt("quantity") == 0) {
                        try (PreparedStatement availableStmt = conn.prepareStatement("UPDATE cars SET available = 0 WHERE id = ?")) {
                            availableStmt.setInt(1, carId);
                            availableStmt.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT id, full_name, phone, email, address FROM clients";
        try (Connection conn = getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Client client = new Client(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                );
                clients.add(client);
                System.out.println(client.getFull_name());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT id, car_id, client_id, sale_date, sale_price, warranty_end FROM sales";
        try (Connection conn = getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Sale sale = new Sale(
                        rs.getInt("id"),
                        rs.getInt("car_id"),
                        rs.getInt("client_id"),
                        rs.getDate("sale_date").toLocalDate(),
                        rs.getDouble("sale_price"),
                        rs.getDate("warranty_end")
                );
                sales.add(sale);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    public List<ServiceRecord> getAllServiceRecords() {
        List<ServiceRecord> records = new ArrayList<>();
        String sql = "SELECT id, car_id, client_id, service_date, description, under_warranty FROM service_records";
        try (Connection conn = getDbConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ServiceRecord record = new ServiceRecord(
                        rs.getInt("id"),
                        rs.getInt("car_id"),
                        rs.getInt("client_id"),
                        rs.getDate("service_date").toLocalDate(),
                        rs.getString("description"),
                        rs.getBoolean("under_warranty")
                );
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public boolean insertCar(Car car) {
        String sql = "INSERT INTO cars (vin, brand, model, year, price, warranty_years, available, quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, car.getVIN());
            stmt.setString(2, car.getBrand());
            stmt.setString(3, car.getModel());
            stmt.setInt(4, car.getYear());
            stmt.setDouble(5, car.getPrice());
            stmt.setInt(6, car.getWarrantyYears());
            stmt.setBoolean(7, car.isAvailable());
            stmt.setInt(8, car.getQuantity());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCar(Car car) {
        String sql = "UPDATE cars SET vin = ?,brand = ?, model = ?, year = ?, price = ?, warranty_years = ?, available = ?, quantity = ? WHERE id = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, car.getVIN());
            stmt.setString(2, car.getBrand());
            stmt.setString(3, car.getModel());
            stmt.setInt(4, car.getYear());
            stmt.setDouble(5, car.getPrice());
            stmt.setInt(6, car.getWarrantyYears());
            stmt.setBoolean(7, car.isAvailable());
            stmt.setInt(8, car.getQuantity());
            stmt.setInt(9, car.getId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCar(int carId) {
        String sql = "DELETE FROM cars WHERE id = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean insertOrder(OrderInfo order) {
        String sql = "INSERT INTO orders_to_sales (client_id, car_id, order_date, status, payment_method, total_price) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, order.getClientId());
            stmt.setInt(2, order.getCarId());
            stmt.setDate(3, Date.valueOf(order.getOrderDate()));
            stmt.setString(4, order.getStatus());
            stmt.setString(5, order.getPaymentMethod());
            stmt.setDouble(6, order.getTotalPrice());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOrder(OrderInfo order) {
        String sql = "UPDATE orders_to_sales SET client_id = ?, car_id = ?, order_date = ?, status = ? payment_method = ? total_price = ? WHERE id = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, order.getClientId());
            stmt.setInt(2, order.getCarId());
            stmt.setDate(3, Date.valueOf(order.getOrderDate()));
            stmt.setString(4, order.getStatus());
            stmt.setString(5, order.getPaymentMethod());
            stmt.setDouble(6, order.getTotalPrice());
            stmt.setInt(7, order.getId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOrder(int orderId) {
        String sql = "DELETE FROM orders_to_sales WHERE id = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertClient(Client client) {
        String sql = "INSERT INTO clients (name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, client.getFull_name());
            stmt.setString(2, client.getPhone());
            stmt.setString(3, client.getEmail());
            stmt.setString(4, client.getAddress());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateClient(Client client) {
        String sql = "UPDATE clients SET full_name = ?, phone = ?, email = ?, address = ? WHERE id = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, client.getFull_name());
            stmt.setString(2, client.getPhone());
            stmt.setString(3, client.getEmail());
            stmt.setString(4, client.getAddress());
            stmt.setInt(5, client.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteClient(int clientId) {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertSale(Sale sale) {
        String sql = "INSERT INTO sales (car_id, client_id, sale_date, sale_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sale.getCar_id());
            stmt.setInt(2, sale.getClient_id());
            stmt.setDate(3, Date.valueOf(sale.getSale_date()));
            stmt.setDouble(4, sale.getSale_price());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSale(Sale sale) {
        String sql = "UPDATE sales SET car_id = ?, client_id = ?, sale_date = ?, sale_price = ? WHERE id = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sale.getCar_id());
            stmt.setInt(2, sale.getClient_id());
            stmt.setDate(3, Date.valueOf(sale.getSale_date()));
            stmt.setDouble(4, sale.getSale_price());
            stmt.setInt(5, sale.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSale(int saleId) {
        String sql = "DELETE FROM sales WHERE id = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertServiceRecord(ServiceRecord record) {
        String sql = "INSERT INTO service_records (car_id, client_id, service_date, description, under_warranty) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getCar_id());
            stmt.setInt(2, record.getClient_id());
            stmt.setDate(3, Date.valueOf(record.getService_date()));
            stmt.setString(4, record.getDescription());
            stmt.setBoolean(5, record.is_under_warranty());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateServiceRecord(ServiceRecord record) {
        String sql = "UPDATE service_records SET car_id = ?, client_id = ?,  service_date = ?, description = ?, under_warranty = ? WHERE id = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getCar_id());
            stmt.setInt(2, record.getClient_id());
            stmt.setDate(3, Date.valueOf(record.getService_date()));
            stmt.setString(4, record.getDescription());
            stmt.setBoolean(5, record.is_under_warranty());
            stmt.setInt(6, record.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteServiceRecord(int serviceRecordId) {
        String sql = "DELETE FROM service_records WHERE id = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, serviceRecordId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<PurchaseInfo> getClientPurchases(int clientId) {
        List<PurchaseInfo> list = new ArrayList<>();
        String query = """
        SELECT c.brand, c.model, c.year, o.total_price, o.payment_method, o.order_date
        FROM orders_to_sales o
        JOIN cars c ON o.car_id = c.id
        WHERE o.client_id = ? AND o.status = 'Подтверждён'
        ORDER BY o.order_date DESC
    """;

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PurchaseInfo purchase = new PurchaseInfo(
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getDouble("total_price"),
                        rs.getString("payment_method"),
                        rs.getDate("order_date").toLocalDate()
                );
                list.add(purchase);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public List<PurchaseInfo> getClientPurchasesWithCarId(int clientId) {
        List<PurchaseInfo> list = new ArrayList<>();
        String query = """
        SELECT c.id AS car_id, c.brand, c.model, c.year, 
               o.total_price, o.payment_method, o.order_date
        FROM orders_to_sales o
        JOIN cars c ON o.car_id = c.id
        WHERE o.client_id = ? AND o.status = 'Подтверждён'
        ORDER BY o.order_date DESC
    """;

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PurchaseInfo purchase = new PurchaseInfo(
                        rs.getInt("car_id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getDouble("total_price"),
                        rs.getString("payment_method"),
                        rs.getDate("order_date").toLocalDate()
                );

                list.add(purchase);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createBackup() {
        String mysqldumpPath = "\"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe\"";
        String dbUser = "root";
        String dbPassword = "root";
        String dbName = "car_dealing";

        // Путь для создания резервной копии — на стороне сервера, в папке backups
        String backupDirectory = "backups";
        new File(backupDirectory).mkdirs(); // создать папку, если нет

        String backupPath = backupDirectory + File.separator + dbName + "_backup.sql";
        String command = String.format("%s -u%s -p%s --add-drop-database --databases %s -r \"%s\"",
                mysqldumpPath, dbUser, dbPassword, dbName, backupPath);

        try {
            Process runtimeProcess = Runtime.getRuntime().exec(command);
            int processComplete = runtimeProcess.waitFor();
            return processComplete == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public File getLatestBackupFile() {
        File backupDir = new File("backups"); // или конкретный путь
        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));
        if (files == null || files.length == 0) return null;
        return Arrays.stream(files).max(Comparator.comparing(File::lastModified)).orElse(null);
    }

    public boolean createServiceRequest(ServiceRequest request) {
        String sql = "INSERT INTO service_requests (client_id, car_id, description, request_date, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, request.getClientId());

            if (request.getCarId() != null) {
                stmt.setInt(2, request.getCarId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setString(3, request.getDescription());
            stmt.setTimestamp(4, Timestamp.valueOf(request.getRequestDate()));
            stmt.setString(5, request.getStatus());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ServiceRequest> getAllServiceRequests() {
        List<ServiceRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM service_requests";

        try (Connection connection = getDbConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ServiceRequest request = new ServiceRequest(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getInt("car_id"),
                        rs.getString("description"),
                        rs.getTimestamp("request_date").toLocalDateTime(),
                        rs.getString("status")
                );
                requests.add(request);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public boolean updateServiceRequest(int requestId, String newStatus) {
        try (Connection conn = getDbConnection()) {
            conn.setAutoCommit(false);

            // Получение данных по заявке
            String selectSql = """
            SELECT sr.id, sr.car_id, sr.client_id,sr.description, sr.request_date, sr.status
            FROM service_requests sr
            WHERE sr.id = ?
        """;

            int carId, clientId;
            String description, currentStatus;
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setInt(1, requestId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) return false;

                carId = rs.getInt("car_id");
                clientId = rs.getInt("client_id");
                description = rs.getString("description");
                currentStatus = rs.getString("status");

                if (!"Ожидает подтверждения".equals(currentStatus)) {
                    return false;
                }
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE service_requests SET status = ? WHERE id = ?")) {
                updateStmt.setString(1, newStatus);
                updateStmt.setInt(2, requestId);
                updateStmt.executeUpdate();
            }

            if ("Подтверждён".equals(newStatus)) {
                int underWarranty = 0;

                String warrantySql = """
                     SELECT sr.request_date, c.warranty_years
                     FROM service_requests sr
                     JOIN cars c ON sr.car_id = c.id
                     WHERE sr.id = ?
                 """;

                try (PreparedStatement warrantyStmt = conn.prepareStatement(warrantySql)) {
                    warrantyStmt.setInt(1, requestId);
                    ResultSet rs = warrantyStmt.executeQuery();
                    if (rs.next()) {
                        Date requestDate = rs.getDate("request_date");
                        int warrantyYears = rs.getInt("warranty_years");

                        LocalDate warrantyEndDate = requestDate.toLocalDate().plusYears(warrantyYears);
                        LocalDate currentDate = LocalDate.now();

                        underWarranty = currentDate.isBefore(warrantyEndDate) || currentDate.isEqual(warrantyEndDate) ? 1 : 0;
                    }
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO service_records (car_id, client_id, service_date, description, under_warranty) " +
                                "VALUES (?, ?, CURRENT_DATE, ?, ?)")) {
                    insertStmt.setInt(1, carId);
                    insertStmt.setInt(2, clientId);
                    insertStmt.setString(3, description);
                    insertStmt.setInt(4, underWarranty);
                    insertStmt.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertReview(Review review) {
        String sql = "INSERT INTO reviews (client_id, description, rating, date_review) VALUES (?, ?, ?, ?)";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, review.getClientId());
            stmt.setString(2, review.getDescription());
            stmt.setInt(3, review.getRating());
            stmt.setDate(4, Date.valueOf(review.getDateReview()));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT id, client_id, description, rating, date_review FROM reviews ORDER BY date_review DESC";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reviews.add(new Review(
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getString("description"),
                        rs.getInt("rating"),
                        rs.getDate("date_review").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public Client getClientById(int clientId) {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Client client = new Client();
                client.setId(rs.getInt("id"));
                client.setFullName(rs.getString("full_name"));
                client.setPhone(rs.getString("phone"));
                client.setEmail(rs.getString("email"));
                client.setAddress(rs.getString("address"));
                return client;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertLogEntry(String userLogin, String action, String level) {
        String sql = "INSERT INTO logs (timestamp, user_login, action, level) VALUES (?, ?, ?, ?)";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, userLogin);
            stmt.setString(3, action);
            stmt.setString(4, level);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<LogEntry> getAllLogs() {
        List<LogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM logs ORDER BY timestamp DESC";
        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LogEntry log = new LogEntry();
                log.setId(rs.getInt("id"));
                log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                log.setUserLogin(rs.getString("user_login"));
                log.setAction(rs.getString("action"));
                log.setLevel(rs.getString("level"));
                logs.add(log);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public List<ServiceRequest> getServiceRequestsForClient(int clientId) {
        List<ServiceRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM service_requests WHERE client_id = ?";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ServiceRequest request = new ServiceRequest();
                request.setId(rs.getInt("id"));
                request.setClientId(rs.getInt("client_id"));
                request.setCarId(rs.getInt("car_id"));
                request.setDescription(rs.getString("description"));
                request.setRequestDate(rs.getTimestamp("request_date").toLocalDateTime());
                request.setStatus(rs.getString("status"));

                requests.add(request);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public boolean cancelServiceRequest(int requestId) {
        String query = "UPDATE service_requests SET status = 'Отменена' WHERE id = ? AND status = 'Ожидает подтверждения'";

        try (Connection conn = getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ServiceRecord> getServiceHistoryForClient(int clientId) {
        List<ServiceRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM service_records WHERE client_id = ?";

        try (Connection conn = getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServiceRecord record = new ServiceRecord();
                    record.setId(rs.getInt("id"));
                    record.setCarId(rs.getInt("car_id"));
                    record.setClientId(rs.getInt("client_id"));
                    record.setServiceDate(rs.getDate("service_date").toLocalDate());
                    record.setDescription(rs.getString("description"));
                    record.setUnderWarranty(rs.getInt("under_warranty") == 1);
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
}