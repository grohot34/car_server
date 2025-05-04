import model.User;
import util.BackupManager;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class MainWindow extends JFrame {
    public MainWindow(String title) {
        setTitle(title);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
}

// Окно для клиента
class ClientWindow extends MainWindow {
    private DBManager dbManager;
    private User currentUser;

    public ClientWindow(User currentUser) {
        super("Окно клиента");
        this.currentUser = currentUser;
        dbManager = new DBManager();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        JButton viewCarsButton = new JButton("Просмотреть автомобили");
        JButton createOrderButton = new JButton("Создать заказ на покупку автомобиля");
        JButton viewOrderButton = new JButton("Мои заказы");
        JButton viewPurchasesButton = new JButton("Мои покупки");
        JButton createServiceRequestButton = new JButton("Создать запрос на техобслуживание");
        JButton changePasswordButton = new JButton("Сменить пароль");


        viewCarsButton.addActionListener(e -> {
            CarsWindow carsWindow = new CarsWindow();
            carsWindow.setVisible(true);
        });

        createOrderButton.addActionListener(e -> {
            CreateOrderWindow orderWindow = new CreateOrderWindow(currentUser.getId());
            orderWindow.setVisible(true);
        });

        viewOrderButton.addActionListener(e -> {
            MyOrdersWindow ordersWindow = new MyOrdersWindow(currentUser.getId());
            ordersWindow.setVisible(true);
        });

        viewPurchasesButton.addActionListener(e -> {
            MyPurchasesWindow purchasesWindow = new MyPurchasesWindow(currentUser.getId());
            purchasesWindow.setVisible(true);
        });

        createServiceRequestButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Окно запроса на техобслуживание ещё не реализовано.");
        });

        changePasswordButton.addActionListener(e -> {
            ChangePasswordDialog dialog = new ChangePasswordDialog(this, currentUser.getId(), dbManager);
            dialog.setVisible(true);

            if (dialog.isPasswordChanged()) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginWindow(dbManager).setVisible(true));
            }
        });

        JButton exitButton = new JButton("Выход");
        exitButton.addActionListener(e -> dispose());

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(viewCarsButton, gbc);

        gbc.gridy++;
        add(createOrderButton, gbc);

        gbc.gridy++;
        add(viewOrderButton, gbc);

        gbc.gridy++;
        add(viewPurchasesButton, gbc);

        gbc.gridy++;
        add(createServiceRequestButton, gbc);

        gbc.gridy++;
        add(changePasswordButton, gbc);

        gbc.gridy++;
        add(exitButton, gbc);
    }
}
// Окно для менеджера
class ManagerWindow extends MainWindow {
    private User currentUser;
    private DBManager dbManager;
    private SalesMonitoringWindow monitoringWindow;
    public ManagerWindow(User currentUser) {
        super("Окно менеджера");
        this.currentUser = currentUser;
        this.monitoringWindow = new SalesMonitoringWindow();
        this.dbManager = new DBManager();

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton openMonitoringButton = new JButton("Мониторинг продаж и сервисного обслуживания");
        openMonitoringButton.addActionListener(e -> {
            monitoringWindow.setVisible(true);
        });

        JButton processOrdersButton = new JButton("Заказы");
        processOrdersButton.addActionListener(e -> {
            ManageOrdersWindow manageOrdersWindow = new ManageOrdersWindow(monitoringWindow);
            manageOrdersWindow.setVisible(true);
        });

        JButton changePassword = new JButton("Сменить пароль");
        changePassword.addActionListener(e -> {
            ChangePasswordDialog dialog = new ChangePasswordDialog(this, currentUser.getId(), dbManager);
            dialog.setVisible(true);

            if (dialog.isPasswordChanged()) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginWindow(dbManager).setVisible(true));
            }
        });

        JButton exitButton = new JButton("Выход");
        exitButton.addActionListener(e -> dispose());

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(openMonitoringButton, gbc);

        gbc.gridy++;
        add(processOrdersButton, gbc);

        gbc.gridy++;
        add(changePassword, gbc);

        gbc.gridy++;
        add(exitButton, gbc);
    }
}

// Окно для администратора
class AdminWindow extends MainWindow {
    private final User currentUser;
    public AdminWindow(User currentUser) {
        super("Окно администратора");
        this.currentUser = currentUser;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton manageUsersButton = new JButton("Управление пользователями");
        manageUsersButton.addActionListener(e -> {
            UserManagement userManagementWindow = new UserManagement();
            userManagementWindow.setVisible(true);
        });

        JButton backupButton = new JButton("Создать резервную копию базы данных");
        backupButton.addActionListener(e -> BackupManager.createBackupWithProgress(AdminWindow.this));

        JButton exitButton = new JButton("Выход");
        exitButton.addActionListener(e -> dispose());

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(manageUsersButton, gbc);

        gbc.gridy++;
        add(backupButton, gbc);

        gbc.gridy++;
        add(exitButton, gbc);
    }
}

class UserManagement extends JFrame {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JComboBox<String> roleFilterBox;

    public UserManagement() {
        setTitle("Управление пользователями");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUserManagementComponents();
    }

    private void initUserManagementComponents() {

        // Модель таблицы с колонками
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Поиск по логину:"));
        searchField = new JTextField(15);
        topPanel.add(searchField);

        topPanel.add(new JLabel("Фильтр по роли:"));
        String[] roles = {"ALL", "CLIENT", "ADMIN", "MANAGER"};
        roleFilterBox = new JComboBox<>(roles);
        topPanel.add(roleFilterBox);

        // Таблица
        String[] columnNames = {"ID", "Логин", "Роль", "Блокировка"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);
        JScrollPane scrollPane = new JScrollPane(userTable);

        // Кнопки действий
        JButton addButton = new JButton("Добавить");
        JButton changeRoleButton = new JButton("Изменить роль");
        JButton deleteButton = new JButton("Удалить");
        JButton refreshButton = new JButton("Обновить");
        JButton exitButton = new JButton("Назад");
        JButton blockButton = new JButton("Заблокировать");
        JButton unblockButton = new JButton("Разблокировать");

        addButton.addActionListener(e -> showAddUserDialog());
        changeRoleButton.addActionListener(e -> changeUserRole());
        deleteButton.addActionListener(e -> deleteUser());
        blockButton.addActionListener(e -> changeBlockStatus(1));
        unblockButton.addActionListener(e -> changeBlockStatus(0));
        refreshButton.addActionListener(e -> loadUsersFromDatabase());
        exitButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(changeRoleButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(blockButton);
        buttonPanel.add(unblockButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exitButton);

        // Основная компоновка
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().removeAll();
        add(mainPanel);
        revalidate();
        repaint();

        // Слушатели для фильтрации
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });
        roleFilterBox.addActionListener(e -> filter());

        // Первичная загрузка данных
        loadUsersFromDatabase();
    }

    // Метод для добавления пользователя в таблицу
    private void addUserToTable(int id, String username, String role) {
        tableModel.addRow(new Object[]{id, username, role});
    }

    private void loadUsersFromDatabase() {
        DBManager dbManager = new DBManager();
        ArrayList<User> users = dbManager.getUsers();

        tableModel.setRowCount(0); // очистка старых данных
        for (User user : users) {
            tableModel.addRow(new Object[]{
                    user.getId(), user.getLogin(), user.getRole(), user.isBlocked()
            });
        }
    }

    private void showAddUserDialog() {
        DBManager dbManager = new DBManager();
        String previousLogin = "";

        while (true) {
            JTextField loginField = new JTextField(previousLogin);
            JPasswordField passwordField = new JPasswordField();
            String[] roles = {"CLIENT", "ADMIN", "MANAGER"};
            JComboBox<String> roleBox = new JComboBox<>(roles);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Логин:"));
            panel.add(loginField);
            panel.add(new JLabel("Пароль:"));
            panel.add(passwordField);
            panel.add(new JLabel("Роль:"));
            panel.add(roleBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Добавить пользователя",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                break;
            }

            String login = loginField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleBox.getSelectedItem();

            previousLogin = login; // сохраняем введённый логин

            if (login.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Логин и пароль не должны быть пустыми.",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            if (dbManager.doesLoginExist(login)) {
                JOptionPane.showMessageDialog(this, "Пользователь с таким логином уже существует. Введите другой логин.",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            dbManager.insertUserWithRole(login, password, role);
            loadUsersFromDatabase();
            break;
        }
    }

    private void changeUserRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите пользователя из таблицы.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentRole = (String) tableModel.getValueAt(selectedRow, 2);

        String[] roles = {"CLIENT", "ADMIN", "MANAGER"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setSelectedItem(currentRole);

        int result = JOptionPane.showConfirmDialog(this, roleBox, "Выберите новую роль",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newRole = (String) roleBox.getSelectedItem();

            if (!newRole.equals(currentRole)) {
                DBManager dbManager = new DBManager();
                dbManager.updateUserRole(userId, newRole);
                loadUsersFromDatabase();
            } else {
                JOptionPane.showMessageDialog(this, "Роль не изменилась.", "Информация",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите пользователя для удаления.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить пользователя?",
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            DBManager dbManager = new DBManager();
            dbManager.deleteUserById(userId);
            loadUsersFromDatabase();
        }
    }

    private void filter() {
        RowFilter<DefaultTableModel, Object> loginFilter = null;
        RowFilter<DefaultTableModel, Object> roleFilter = null;

        String searchText = searchField.getText().trim();
        String selectedRole = (String) roleFilterBox.getSelectedItem();

        if (!searchText.isEmpty()) {
            loginFilter = RowFilter.regexFilter("(?i)" + searchText, 1);
        }

        if (!"ALL".equals(selectedRole)) {
            roleFilter = RowFilter.regexFilter("^" + selectedRole + "$", 2);
        }

        ArrayList<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
        if (loginFilter != null) filters.add(loginFilter);
        if (roleFilter != null) filters.add(roleFilter);

        RowFilter<DefaultTableModel, Object> combinedFilter = filters.isEmpty()
                ? null
                : RowFilter.andFilter(filters);

        sorter.setRowFilter(combinedFilter);
    }

    private void changeBlockStatus(int block) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            String login = (String) tableModel.getValueAt(selectedRow, 1);
            String role = (String) tableModel.getValueAt(selectedRow, 2);

            if (!role.equals("CLIENT") && !role.equals("MANAGER")) {
                JOptionPane.showMessageDialog(this, "Можно блокировать только пользователей с ролями CLIENT и MANAGER.");
                return;
            }

            DBManager db = new DBManager();
            db.setUserBlocked(login, block);
            loadUsersFromDatabase();
        } else {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите пользователя.");
        }
    }
}

class SalesMonitoringWindow extends JFrame {
    private JTable carsTable;
    private JTable clientsTable;
    private JTable salesTable;
    private JTable serviceRecordsTable;

    public SalesMonitoringWindow() {
        setTitle("Мониторинг продаж и сервисного обслуживания");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        DBManager dbManager = new DBManager();
        // Автомобили
        JPanel carsPanel = createTablePanel(
                new String[]{"ID", "VIN", "Бренд", "Модель", "Год", "Цена", "Гарантия (лет)", "В наличии", "Количество"},
                "Автомобили"
        );
        carsTable = (JTable) carsPanel.getClientProperty("table");
        dbManager.loadTableData(carsTable, "SELECT id, vin, brand, model, year, price, warranty_years, available, quantity FROM cars");
        tabbedPane.add("Автомобили", carsPanel);

// Клиенты
        JPanel clientsPanel = createTablePanel(
                new String[]{"ID", "ФИО", "Телефон", "Email", "Адрес"},
                "Клиенты"
        );
        clientsTable = (JTable) clientsPanel.getClientProperty("table");
        dbManager.loadTableData(clientsTable, "SELECT id, full_name, phone, email, address FROM clients");
        tabbedPane.add("Клиенты", clientsPanel);

// Продажи
        JPanel salesPanel = createTablePanel(
                new String[]{"ID", "ID Авто", "ID Клиента", "Дата продажи", "Цена продажи"},
                "Продажи"
        );
        salesTable = (JTable) salesPanel.getClientProperty("table");
        dbManager.loadTableData(salesTable, "SELECT id, car_id, client_id, sale_date, sale_price FROM sales");
        tabbedPane.add("Продажи", salesPanel);

// Сервисное обслуживание
        JPanel servicePanel = createTablePanel(
                new String[]{"ID", "ID Авто", "ID Клиента", "Дата сервиса", "Описание", "По гарантии"},
                "Сервисное обслуживание"
        );
        serviceRecordsTable = (JTable) servicePanel.getClientProperty("table");
        dbManager.loadTableData(serviceRecordsTable, "SELECT id, car_id, client_id, service_date, description, under_warranty FROM service_records");
        tabbedPane.add("Сервисное обслуживание", servicePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTablePanel(String[] columnNames, String entityName) {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Добавить");
        JButton editButton = new JButton("Редактировать");
        JButton deleteButton = new JButton("Удалить");
        JButton exitButton = new JButton("Назад");

        // Слушатели кнопок
        addButton.addActionListener(e -> addRow(model, entityName));
        editButton.addActionListener(e -> editRow(model, table, entityName));
        deleteButton.addActionListener(e -> deleteRow(model, table));
        exitButton.addActionListener(e -> dispose());

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(exitButton);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        // Запоминаем таблицу
        panel.putClientProperty("table", table);

        return panel;
    }

    private void addRow(DefaultTableModel model, String entityName) {
        int columns = model.getColumnCount();
        String[] inputData = new String[columns];

        for (int i = 0; i < columns; i++) {
            inputData[i] = JOptionPane.showInputDialog(this, "Введите " + model.getColumnName(i) + ":");
            if (inputData[i] == null) {
                // Отмена ввода
                return;
            }
        }
        model.addRow(inputData);
    }

    private void editRow(DefaultTableModel model, JTable table, String entityName) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите строку для редактирования", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (int i = 0; i < model.getColumnCount(); i++) {
            String currentValue = (String) model.getValueAt(selectedRow, i);
            String newValue = JOptionPane.showInputDialog(this, "Измените " + model.getColumnName(i) + ":", currentValue);
            if (newValue != null) {
                model.setValueAt(newValue, selectedRow, i);
            }
        }
    }

    private void deleteRow(DefaultTableModel model, JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите строку для удаления", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Удалить выбранную строку?", "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            model.removeRow(selectedRow);
        }
    }

    public void refreshSalesData() {
        DBManager dbManager = new DBManager();
        dbManager.loadTableData(salesTable, "SELECT id, car_id, client_id, sale_date, sale_price FROM sales");
    }
}

class CarsWindow extends JFrame {
    private JTable carsTable;
    private DBManager dbManager;

    public CarsWindow() {
        dbManager = new DBManager();

        setTitle("Доступные автомобили");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Панель для поиска автомобилей
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Поиск по автомобилям:");
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);
        JButton exitButton = new JButton("Назад");
        exitButton.addActionListener(e -> dispose());
        add(exitButton, BorderLayout.SOUTH);
        // Таблица автомобилей
        String[] columnNames = {"Марка", "Модель", "Год", "Цена", "Гарантия на сервисное обслуживание(лет)", "Наличие"};
        carsTable = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollPane = new JScrollPane(carsTable);
        add(scrollPane, BorderLayout.CENTER);

        loadCarsData();

        // Реализация поиска по таблице
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }
            private void search() {
                String text = searchField.getText();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) carsTable.getModel());
                carsTable.setRowSorter(sorter);
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
    }

    public void loadCarsData() {
        try (Connection conn = dbManager.getDbConnection()) {
            String sql = "SELECT brand, model, year, price, warranty_years, available FROM cars";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                DefaultTableModel model = (DefaultTableModel) carsTable.getModel();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("brand"),
                            rs.getString("model"),
                            rs.getInt("year"),
                            rs.getDouble("price"),
                            rs.getInt("warranty_years"),
                            rs.getBoolean("available") ? "Да" : "Нет"
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class CreateOrderWindow extends JFrame {
    private JTable carsTable;
    private DBManager dbManager;
    private int clientId;


    public CreateOrderWindow(int clientId) {
        this.clientId = clientId;
        dbManager = new DBManager();

        setTitle("Создание заказа на покупку автомобиля");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Поиск по автомобилям:");
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Таблица с автомобилями
        String[] columnNames = {"ID", "Марка", "Модель", "Год", "Цена", "Гарантия(лет)", "Наличие"};
        carsTable = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollPane = new JScrollPane(carsTable);
        add(scrollPane, BorderLayout.CENTER);

        loadCarsData();


        // Кнопка оформления заказа
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 2)); // 5 — отступ между кнопками

        JButton orderButton = new JButton("Оформить заказ");
        JButton exitButton = new JButton("Назад");

        buttonPanel.add(orderButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);

// Обработка нажатия кнопок
        orderButton.addActionListener(this::handleCreateOrder);
        exitButton.addActionListener(e -> dispose());
        // Фильтр поиска
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = searchField.getText();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) carsTable.getModel());
                carsTable.setRowSorter(sorter);
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
    }

    private void loadCarsData() {
        try (Connection conn = dbManager.getDbConnection()) {
            String sql = "SELECT id, brand, model, year, price, warranty_years, available FROM cars WHERE available = true";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                DefaultTableModel model = (DefaultTableModel) carsTable.getModel();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("brand"),
                            rs.getString("model"),
                            rs.getInt("year"),
                            rs.getDouble("price"),
                            rs.getInt("warranty_years"),
                            rs.getBoolean("available") ? "Да" : "Нет"
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    private void handleCreateOrder(ActionEvent e) {
        int row = carsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите автомобиль.");
            return;
        }

        int modelRow = carsTable.convertRowIndexToModel(row);


        String brand = (String) carsTable.getValueAt(modelRow, 1);
        String modelName = (String) carsTable.getValueAt(modelRow, 2);
        double price = (double) carsTable.getValueAt(modelRow, 4);

        // Получим ID машины
        int carId = dbManager.getCarIdByBrandAndModel(brand, modelName);

        // Ввод способа оплаты
        String[] methods = {"Наличные", "Карта", "Кредит", "Другое"};
        String paymentMethod = showPaymentDialog(methods);

        if (paymentMethod == null) {
            return; // Пользователь отменил
        }

        try (Connection conn = dbManager.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO orders_to_sales (client_id, car_id, order_date, status, payment_method, total_price) " +
                             "VALUES (?, ?, ?, ?, ?, ?)")) {

            stmt.setInt(1, clientId);
            stmt.setInt(2, carId);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setString(4, "Ожидает подтверждения");
            stmt.setString(5, paymentMethod);
            stmt.setDouble(6, price);

            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Заказ успешно оформлен!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при оформлении заказа: " + ex.getMessage());
        }
    }
    private String showPaymentDialog(String[] methods) {
        String paymentMethod = (String) JOptionPane.showInputDialog(
                this,
                "Выберите способ оплаты:",
                "Способ оплаты",
                JOptionPane.QUESTION_MESSAGE,
                null,
                methods,
                methods[0]);

        return paymentMethod; // Возвращаем выбранный способ оплаты
    }
}

class MyOrdersWindow extends JFrame {
    private JTable ordersTable;
    private DBManager dbManager;
    private DefaultTableModel model;
    private int clientId;

    public MyOrdersWindow(int clientId) {
        dbManager = new DBManager();
        this.clientId = clientId;
        setTitle("Мои заказы");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Марка", "Модель", "Год", "Цена", "Способ оплаты", "Дата заказа", "Статус"};

        ordersTable = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton updateButton = new JButton("Обновить");
        JButton cancelButton = new JButton("Отменить заказ");
        JButton exitButton = new JButton("Назад");
        updateButton.addActionListener(e -> loadOrdersData(clientId));
        cancelButton.addActionListener(e -> cancelSelectedOrder());
        exitButton.addActionListener(e -> dispose());
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadOrdersData(clientId);
    }

    private void loadOrdersData(int clientId) {
        String query = """
                SELECT o.id, c.brand, c.model, c.year, o.total_price, o.payment_method, o.order_date, o.status
                FROM orders_to_sales o
                JOIN cars c ON o.car_id = c.id
                WHERE o.client_id = ?
                ORDER BY o.order_date DESC
                """;

        try (Connection conn = dbManager.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, clientId);

            try (ResultSet rs = stmt.executeQuery()) {
                model = (DefaultTableModel) ordersTable.getModel();
                model.setRowCount(0);
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("brand"),
                            rs.getString("model"),
                            rs.getInt("year"),
                            rs.getDouble("total_price"),
                            rs.getString("payment_method"),
                            rs.getDate("order_date"),
                            rs.getString("status")
                    });
                }
                ordersTable.getColumnModel().getColumn(0).setMinWidth(0);
                ordersTable.getColumnModel().getColumn(0).setMaxWidth(0);
                ordersTable.getColumnModel().getColumn(0).setWidth(0);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки заказов: " + e.getMessage());
        }
    }
    private void cancelSelectedOrder() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите заказ.");
            return;
        }

        int modelRow = ordersTable.convertRowIndexToModel(selectedRow);
        String status = (String) model.getValueAt(modelRow, 7);

        if (!status.equals("Ожидает подтверждения")) {
            JOptionPane.showMessageDialog(this, "Заказ нельзя отменить (уже обработан).");
            return;
        }

        int orderId = (int) model.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите отменить заказ №" + orderId + "?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String updateSql = "UPDATE orders_to_sales SET status = 'Отменён' WHERE id = ?";

        try (Connection conn = dbManager.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {

            stmt.setInt(1, orderId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Заказ успешно отменён.");
            loadOrdersData(clientId); // обновляем таблицу
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при отмене заказа: " + e.getMessage());
        }
    }
}

class ManageOrdersWindow extends JFrame {
    private JTable ordersTable;
    private DBManager dbManager;
    private DefaultTableModel model;

    private SalesMonitoringWindow monitoringWindow;

    public ManageOrdersWindow(SalesMonitoringWindow monitoringWindow) {
        this.monitoringWindow = monitoringWindow;
        dbManager = new DBManager();

        setTitle("Обработка заказов");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Клиент ID", "Марка", "Модель", "Год", "Цена", "Способ оплаты", "Дата заказа", "Статус"};
        model = new DefaultTableModel(columnNames, 0);
        ordersTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);




        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton approveButton = new JButton("Подтвердить");
        JButton rejectButton = new JButton("Отклонить");
        JButton updateButton = new JButton("Обновить");
        JButton exitButton = new JButton("Назад");


        approveButton.addActionListener(e -> updateOrderStatus("Подтверждён"));
        rejectButton.addActionListener(e -> updateOrderStatus("Отклонён"));
        updateButton.addActionListener(e -> loadOrdersData());
        exitButton.addActionListener(e -> dispose());

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadOrdersData();
    }

    private void loadOrdersData() {
        String query = """
                SELECT o.id, o.client_id, c.brand, c.model, c.year,
                       o.total_price, o.payment_method, o.order_date, o.status
                FROM orders_to_sales o
                JOIN cars c ON o.car_id = c.id
                ORDER BY o.order_date DESC
                """;

        model.setRowCount(0); // очищаем перед загрузкой

        try (Connection conn = dbManager.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("client_id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getDouble("total_price"),
                        rs.getString("payment_method"),
                        rs.getDate("order_date"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки заказов: " + e.getMessage());
        }
    }

    private void updateOrderStatus(String newStatus) {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите заказ.");
            return;
        }

        int modelRow = ordersTable.convertRowIndexToModel(selectedRow);
        String currentStatus = (String) model.getValueAt(modelRow, 8);

        if (!"Ожидает подтверждения".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Можно изменить только заказы со статусом 'Ожидает подтверждения'.");
            return;
        }

        int orderId = (int) model.getValueAt(modelRow, 0);
        int clientId = (int) model.getValueAt(modelRow, 1);
        String brand = (String) model.getValueAt(modelRow, 2);
        String modelName = (String) model.getValueAt(modelRow, 3);
        double price = (double) model.getValueAt(modelRow, 5);

        // Получим ID машины
        int carId = dbManager.getCarIdByBrandAndModel(brand, modelName);


        String checkAvailability = "SELECT available FROM cars WHERE id = ?";
        try (Connection conn = dbManager.getDbConnection();
        PreparedStatement checkStmt = conn.prepareStatement(checkAvailability)) {
            checkStmt.setInt(1, carId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("available") == 0) {
                JOptionPane.showMessageDialog(this, "Автомобиль \"" + brand + " " + modelName + "\" больше не доступен.");
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String updateSql = "UPDATE orders_to_sales SET status = ? WHERE id = ?";
        try (Connection conn = dbManager.getDbConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            updateStmt.setString(1, newStatus);
            updateStmt.setInt(2, orderId);
            updateStmt.executeUpdate();



            // Если заказ подтвержден — вставим запись в таблицу sales
            if ("Подтверждён".equals(newStatus)) {
                String insertSale = "INSERT INTO sales (car_id, client_id, sale_date, sale_price) VALUES (?, ?, CURRENT_DATE, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSale)) {
                    insertStmt.setInt(1, carId);
                    insertStmt.setInt(2, clientId);
                    insertStmt.setDouble(3, price);
                    insertStmt.executeUpdate();
                }

                // Уменьшим количество машин на складе
                String updateCarQty = "UPDATE cars SET quantity = quantity - 1 WHERE id = ?";
                try (PreparedStatement carStmt = conn.prepareStatement(updateCarQty)) {
                    carStmt.setInt(1, carId);
                    carStmt.executeUpdate();
                }

                String checkQtySql = "SELECT quantity FROM cars WHERE id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQtySql)) {
                    checkStmt.setInt(1, carId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt("quantity") == 0) {
                        String updateAvailable = "UPDATE cars SET available = 0 WHERE id = ?";
                        try (PreparedStatement availableStmt = conn.prepareStatement(updateAvailable)) {
                            availableStmt.setInt(1, carId);
                            availableStmt.executeUpdate();
                        }
                    }
                }

                // Обновим мониторинг
                if (monitoringWindow != null) {
                    monitoringWindow.refreshSalesData();
                }
            }

            JOptionPane.showMessageDialog(this, "Статус заказа обновлён на: " + newStatus);
            loadOrdersData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка обновления статуса: " + e.getMessage());
        }
    }

}


class ChangePasswordDialog extends JDialog {
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private final DBManager dbManager;
    private final int userId; // ID текущего пользователя
    private boolean passwordChanged;

    public ChangePasswordDialog(JFrame parent, int userId, DBManager dbManager) {
        super(parent, "Сменить пароль", true);
        this.userId = userId;
        this.dbManager = dbManager;

        setSize(350, 250);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Старый пароль:"));
        oldPasswordField = new JPasswordField();
        add(oldPasswordField);

        add(new JLabel("Новый пароль:"));
        newPasswordField = new JPasswordField();
        add(newPasswordField);

        add(new JLabel("Подтвердите новый:"));
        confirmPasswordField = new JPasswordField();
        add(confirmPasswordField);

        JButton changeButton = new JButton("Сменить");
        JButton cancelButton = new JButton("Отмена");

        changeButton.addActionListener(e -> changePassword());
        cancelButton.addActionListener(e -> dispose());

        add(changeButton);
        add(cancelButton);
    }

    private void changePassword() {
        String oldPass = new String(oldPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Новый пароль и подтверждение не совпадают.");
            return;
        }

        boolean success = dbManager.updatePassword(userId, oldPass, newPass);
        if (success) {
            passwordChanged = true;
            JOptionPane.showMessageDialog(this, "Пароль успешно изменён. Войдите заново.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Старый пароль неверен.");
        }
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }
}
class MyPurchasesWindow extends JFrame {
    private JTable purchasesTable;
    private DefaultTableModel model;
    private DBManager dbManager;
    private int clientId;

    public MyPurchasesWindow(int clientId) {
        this.clientId = clientId;
        dbManager = new DBManager();

        setTitle("Мои покупки");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        JButton exitButton = new JButton("Назад");
        exitButton.addActionListener(e -> dispose());
        add(exitButton, BorderLayout.SOUTH);

        String[] columnNames = {
                "Марка", "Модель", "Год", "Цена", "Способ оплаты", "Дата покупки"
        };
        model = new DefaultTableModel(columnNames, 0);
        purchasesTable = new JTable(model);
        add(new JScrollPane(purchasesTable), BorderLayout.CENTER);

        loadPurchases();
    }

    private void loadPurchases() {
        String query = """
            SELECT c.brand, c.model, c.year, o.total_price, o.payment_method, o.order_date
            FROM orders_to_sales o
            JOIN cars c ON o.car_id = c.id
            WHERE o.client_id = ? AND o.status = 'Подтверждён'
            ORDER BY o.order_date DESC
        """;

        try (Connection conn = dbManager.getDbConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            model.setRowCount(0); // очистить таблицу перед добавлением новых данных

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getDouble("total_price"),
                        rs.getString("payment_method"),
                        rs.getDate("order_date")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки покупок: " + e.getMessage());
        }
    }
}
