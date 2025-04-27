import model.User;
import util.BackupManager;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MainWindow extends JFrame {
    public MainWindow(String title, String message) {
        setTitle(title);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel(message, JLabel.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.PLAIN, 20));

        add(welcomeLabel);
    }
}

// Окно для клиента
class ClientWindow extends MainWindow {
    private DBManager dbManager;

    public ClientWindow() {
        super("Окно клиента", "Добро пожаловать, клиент!");
        dbManager = new DBManager();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Кнопки
        JButton viewCarsButton = new JButton("Просмотреть автомобили");
        JButton createOrderButton = new JButton("Создать заказ на покупку автомобиля");
        JButton viewPurchasesButton = new JButton("Просмотреть мои покупки");
        JButton createServiceRequestButton = new JButton("Создать запрос на техобслуживание");
        JButton changePasswordButton = new JButton("Сменить пароль");

        // Обработчики кнопок
        viewCarsButton.addActionListener(e -> {
            CarsWindow carsWindow = new CarsWindow();
            carsWindow.setVisible(true);
        });

        createOrderButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Окно создания заказа ещё не реализовано.");
        });

        viewPurchasesButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Окно просмотра покупок ещё не реализовано.");
        });

        createServiceRequestButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Окно запроса на техобслуживание ещё не реализовано.");
        });

        changePasswordButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Окно смены пароля ещё не реализовано.");
        });

        // Добавляем кнопки на форму
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(viewCarsButton, gbc);

        gbc.gridy++;
        add(createOrderButton, gbc);

        gbc.gridy++;
        add(viewPurchasesButton, gbc);

        gbc.gridy++;
        add(createServiceRequestButton, gbc);

        gbc.gridy++;
        add(changePasswordButton, gbc);
    }
}
// Окно для менеджера
class ManagerWindow extends MainWindow {
    public ManagerWindow() {
        super("Окно менеджера", "Добро пожаловать, Менеджер!");

        JButton openMonitoringButton = new JButton("Мониторинг продаж и сервиса");
        openMonitoringButton.setFont(new Font("Serif", Font.BOLD, 16));
        openMonitoringButton.addActionListener(e -> {
            SalesMonitoringWindow monitoringWindow = new SalesMonitoringWindow();
            monitoringWindow.setVisible(true);
        });

        JPanel panel = new JPanel();
        panel.add(openMonitoringButton);

        getContentPane().add(panel, BorderLayout.SOUTH);
    }
}

// Окно для администратора
class AdminWindow extends MainWindow {
    public AdminWindow() {
        super("Окно администратора", "Добро пожаловать, Администратор!");

        JButton manageUsersButton = new JButton("Управление пользователями");
        manageUsersButton.setFont(new Font("Serif", Font.PLAIN, 18));

        manageUsersButton.addActionListener(e -> {
            UserManagement userManagementWindow = new UserManagement();
            userManagementWindow.setVisible(true);
        });


        JButton backupButton = new JButton("Создать резервную копию базы данных");
        backupButton.setFont(new Font("Serif", Font.PLAIN, 18));
        backupButton.addActionListener(e -> BackupManager.createBackupWithProgress(AdminWindow.this));


        JPanel panel = new JPanel();
        panel.add(manageUsersButton);
        panel.add(backupButton);

        getContentPane().add(panel, BorderLayout.CENTER);
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
        JButton logoutButton = new JButton("Выход");
        JButton blockButton = new JButton("Заблокировать");
        JButton unblockButton = new JButton("Разблокировать");

        addButton.addActionListener(e -> showAddUserDialog());
        changeRoleButton.addActionListener(e -> changeUserRole());
        deleteButton.addActionListener(e -> deleteUser());
        blockButton.addActionListener(e -> changeBlockStatus(1));
        unblockButton.addActionListener(e -> changeBlockStatus(0));
        refreshButton.addActionListener(e -> loadUsersFromDatabase());
        logoutButton.addActionListener(e -> System.exit(0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(changeRoleButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(blockButton);
        buttonPanel.add(unblockButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);

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

        // Слушатели кнопок
        addButton.addActionListener(e -> addRow(model, entityName));
        editButton.addActionListener(e -> editRow(model, table, entityName));
        deleteButton.addActionListener(e -> deleteRow(model, table));

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);

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

    private void loadCarsData() {
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