import Request_Response.Request;
import Request_Response.Response;
import model.*;
import util.BackupManager;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;


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
            try {
                // Попробуем получить список автомобилей от сервера
                List<Car> cars = UserSender.getAllCars();
                System.out.println(cars);
                // Если запрос прошел успешно, откроем окно с автомобилями
                if (!cars.isEmpty()) {
                    CarsWindow carsWindow = new CarsWindow(cars);
                    carsWindow.setVisible(true);
                } else {
                    // Если список пустой, покажем сообщение
                    JOptionPane.showMessageDialog(this, "Нет доступных автомобилей для просмотра.");
                }
            } catch (Exception ex) {
                // Обработка исключений
                JOptionPane.showMessageDialog(this, "Ошибка при получении данных о автомобилях: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        createOrderButton.addActionListener(e -> {
            try {
                List<Car> cars = UserSender.getAllCars();
                if (cars.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Нет доступных автомобилей.");
                    return;
                }

                CreateOrderWindow orderWindow = new CreateOrderWindow(currentUser.getId(), cars);
                orderWindow.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Ошибка при загрузке автомобилей: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        viewOrderButton.addActionListener(e -> {
            try {
                // Попробуем получить список заказов для текущего пользователя с сервера
                List<OrderInfo> orders = UserSender.getOrdersForClient(currentUser.getId());

                // Если запрос прошел успешно и есть заказы
                if (orders != null && !orders.isEmpty()) {
                    // Создадим окно для отображения заказов
                    MyOrdersWindow ordersWindow = new MyOrdersWindow(currentUser.getId()); // передаем список заказов
                    ordersWindow.setVisible(true);
                } else {
                    // Если заказов нет, выводим сообщение
                    JOptionPane.showMessageDialog(this, "У вас нет заказов.");
                }
            } catch (Exception ex) {
                // Обработка исключений, если что-то пошло не так
                JOptionPane.showMessageDialog(this, "Ошибка при получении данных о заказах: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        viewPurchasesButton.addActionListener(e -> {
            MyPurchasesWindow purchasesWindow = new MyPurchasesWindow(currentUser.getId());
            purchasesWindow.setVisible(true);
        });

        createServiceRequestButton.addActionListener(e -> {
            CreateServiceRequestWindow createServiceRequestWindow = new CreateServiceRequestWindow(currentUser.getId());
            createServiceRequestWindow.setVisible(true);
        });

        changePasswordButton.addActionListener(e -> {
            ChangePasswordDialog dialog = new ChangePasswordDialog(this, currentUser.getId());
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
            try {
                List<OrderInfo> orders = UserSender.getAllOrders();
                if (!orders.isEmpty()) {
                    ManageOrdersWindow manageOrdersWindow = new ManageOrdersWindow(orders, monitoringWindow);
                    manageOrdersWindow.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Нет заказов для отображения.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при получении заказов: " + ex.getMessage());
                ex.printStackTrace();
            }
        });


        JButton changePassword = new JButton("Сменить пароль");
        changePassword.addActionListener(e -> {
            ChangePasswordDialog dialog = new ChangePasswordDialog(this, currentUser.getId());
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
        backupButton.addActionListener(e -> {
            // Диалог с прогрессбаром
            JDialog progressDialog = new JDialog(AdminWindow.this, "Создание резервной копии", true);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setString("Создание резервной копии...");
            progressBar.setStringPainted(true);
            progressDialog.add(progressBar);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(AdminWindow.this);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    Response response = UserSender.createBackup();
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(AdminWindow.this, response.getMessage(), "Успех", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(AdminWindow.this, response.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                }

            };

            worker.execute();
            progressDialog.setVisible(true);

        });
        JButton downloadBackupButton = new JButton("Скачать резервную копию");
        downloadBackupButton.addActionListener(e -> UserSender.downloadBackup(AdminWindow.this));



        JButton exitButton = new JButton("Выход");
        exitButton.addActionListener(e -> dispose());

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(manageUsersButton, gbc);

        gbc.gridy++;
        add(backupButton, gbc);

        gbc.gridy++;
        add(downloadBackupButton, gbc);

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
        refreshButton.addActionListener(e -> loadUsersFromServer());
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
        loadUsersFromServer();
    }

    private void loadUsersFromServer() {
        List<User> users = UserSender.getAllUsers();
        tableModel.setRowCount(0); // очистка
        for (User user : users) {
            tableModel.addRow(new Object[]{
                    user.getId(),
                    user.getLogin(),
                    user.getRole(),
                    user.isBlocked() ? "Да" : "Нет"
            });
        }
    }


    private void showAddUserDialog() {
        JTextField loginField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        String[] roles = {"client", "manager", "admin"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Логин:"));
        panel.add(loginField);
        panel.add(new JLabel("Пароль:"));
        panel.add(passwordField);
        panel.add(new JLabel("Роль:"));
        panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Добавить пользователя",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String login = loginField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = (String) roleBox.getSelectedItem();

            if (login.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Все поля должны быть заполнены.");
                return;
            }

            Response response = UserSender.insertUser(login, password, role);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Пользователь успешно добавлен.");
                loadUsersFromServer();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка: " + response.getMessage());
            }
        }
    }

    private void changeUserRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите пользователя.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentRole = (String) tableModel.getValueAt(selectedRow, 2);

        String[] roles = {"CLIENT", "MANAGER", "ADMIN"};
        String newRole = (String) JOptionPane.showInputDialog(this, "Выберите новую роль:",
                "Изменить роль", JOptionPane.PLAIN_MESSAGE, null, roles, currentRole);

        if (newRole != null && !newRole.equals(currentRole)) {
            Response response = UserSender.updateUserRole(userId, newRole);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Роль успешно обновлена.");
                loadUsersFromServer();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при обновлении роли: " + response.getMessage());
            }
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите пользователя.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Удалить пользователя?", "Подтверждение",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Response response = UserSender.deleteUser(userId);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Пользователь удален.");
                loadUsersFromServer();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка: " + response.getMessage());
            }
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

    private void changeBlockStatus(int newBlockValue) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите пользователя.");
            return;
        }

        String login = (String) tableModel.getValueAt(selectedRow, 1);
        String currentBlock = (String) tableModel.getValueAt(selectedRow, 3);
        boolean isBlocked = currentBlock.equals("Да");

        // Проверка: не блокируем уже заблокированного и не разблокируем уже разблокированного
        if ((newBlockValue == 1 && isBlocked) || (newBlockValue == 0 && !isBlocked)) {
            JOptionPane.showMessageDialog(this, "Статус пользователя уже установлен.");
            return;
        }

        Response response = UserSender.setUserBlocked(login, newBlockValue);
        if (response.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Статус блокировки обновлён.");
            loadUsersFromServer();
        } else {
            JOptionPane.showMessageDialog(this, "Ошибка: " + response.getMessage());
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
        // Автомобили
        JPanel carsPanel = createTablePanel(
                new String[]{"ID", "VIN", "Бренд", "Модель", "Год", "Цена", "Гарантия (лет)", "В наличии", "Количество"},
                "CARS"
        );
        carsTable = (JTable) carsPanel.getClientProperty("table");
        loadCars();
        tabbedPane.add("Автомобили", carsPanel);

        // Клиенты
        JPanel clientsPanel = createTablePanel(
                new String[]{"ID", "ФИО", "Телефон", "Email", "Адрес"},
                "CLIENTS"
        );
        clientsTable = (JTable) clientsPanel.getClientProperty("table");
        loadClients();
        tabbedPane.add("Клиенты", clientsPanel);

        // Продажи
        JPanel salesPanel = createTablePanel(
                new String[]{"ID", "ID Авто", "ID Клиента", "Дата продажи", "Цена продажи"},
                "SALES"
        );
        salesTable = (JTable) salesPanel.getClientProperty("table");
        loadSales();
        tabbedPane.add("Продажи", salesPanel);

        // Сервис
        JPanel servicePanel = createTablePanel(
                new String[]{"ID", "ID Авто", "ID Клиента", "Дата сервиса", "Описание", "По гарантии"},
                "SERVICE"
        );
        serviceRecordsTable = (JTable) servicePanel.getClientProperty("table");
        loadServiceRecords();
        tabbedPane.add("Сервисное обслуживание", servicePanel);

        add(tabbedPane, BorderLayout.CENTER);


    }

    private JPanel createTablePanel(String[] columnNames, String entityName) {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JTextField searchField = new JTextField(20);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Поиск:"));
        searchPanel.add(searchField);
        panel.add(searchPanel, BorderLayout.NORTH); // добавим панель поиска сверху

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void search() {
                String text = searchField.getText();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) carsTable.getModel());
                carsTable.setRowSorter(sorter);
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text))); // экранируем спецсимволы
            }

            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
        });


        // Панель кнопок
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Добавить");
        JButton editButton = new JButton("Редактировать");
        JButton deleteButton = new JButton("Удалить");
        JButton exitButton = new JButton("Назад");

        addButton.addActionListener(e -> handleAdd(entityName, table));
        editButton.addActionListener(e -> handleEdit(entityName, table));
        deleteButton.addActionListener(e -> handleDelete(entityName, table));


        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(exitButton);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

// Запоминаем таблицу
        panel.putClientProperty("table", table);

        return panel;
    }

    private void handleAdd(String entity, JTable table) {
        switch (entity) {
            case "CARS":
                String vin = JOptionPane.showInputDialog(this, "Введите VIN:");
                String brand = JOptionPane.showInputDialog(this, "Введите бренд:");
                String modelCar = JOptionPane.showInputDialog(this, "Введите модель:");
                int year = Integer.parseInt(JOptionPane.showInputDialog(this, "Введите год:"));
                double price = Double.parseDouble(JOptionPane.showInputDialog(this, "Введите цену:"));
                int warrantyYears = Integer.parseInt(JOptionPane.showInputDialog(this, "Гарантия (лет):"));
                boolean isAvailable = JOptionPane.showConfirmDialog(this, "В наличии?", "Выберите", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                int quantity = Integer.parseInt(JOptionPane.showInputDialog(this, "Количество:"));

                Car car = new Car(vin, brand, modelCar, year, price, warrantyYears, isAvailable, quantity);
                Response carResp = UserSender.insertCar(car);
                showMessage(carResp, "Автомобиль добавлен");
                loadCars();
                break;

            case "CLIENTS":
                String name = JOptionPane.showInputDialog(this, "ФИО:");
                String phone = JOptionPane.showInputDialog(this, "Телефон:");
                String email = JOptionPane.showInputDialog(this, "Email:");
                String address = JOptionPane.showInputDialog(this, "Адрес:");

                Client client = new Client(name, phone, email, address);
                Response clientResp = UserSender.insertClient(client);
                showMessage(clientResp, "Клиент добавлен");
                loadClients();
                break;

            case "SALES":
                int carId = Integer.parseInt(JOptionPane.showInputDialog(this, "ID Авто:"));
                int clientId = Integer.parseInt(JOptionPane.showInputDialog(this, "ID Клиента:"));
                LocalDate saleDate = LocalDate.parse(JOptionPane.showInputDialog(this, "Дата продажи (YYYY-MM-DD):"));
                double salePrice = Double.parseDouble(JOptionPane.showInputDialog(this, "Цена продажи:"));

                Sale sale = new Sale(carId, clientId, saleDate, salePrice);
                Response saleResp = UserSender.insertSale(sale);
                showMessage(saleResp, "Продажа добавлена");
                loadSales();
                break;

            case "SERVICE":
                int carIdS = Integer.parseInt(JOptionPane.showInputDialog(this, "ID Авто:"));
                int clientIdS = Integer.parseInt(JOptionPane.showInputDialog(this, "ID Клиента:"));
                LocalDate serviceDate = LocalDate.parse(JOptionPane.showInputDialog(this, "Дата сервиса (YYYY-MM-DD):"));
                String description = JOptionPane.showInputDialog(this, "Описание:");
                boolean underWarranty = JOptionPane.showConfirmDialog(this, "По гарантии?", "Выберите", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

                ServiceRecord record = new ServiceRecord(carIdS, clientIdS, serviceDate, description, underWarranty);
                Response servResp = UserSender.insertServiceRecord(record);
                showMessage(servResp, "Сервисная запись добавлена");
                loadServiceRecords();
                break;
        }
    }

    private void handleEdit(String entity, JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Выберите строку для редактирования");
            return;
        }

        int id = (int) table.getValueAt(row, 0);

        switch (entity) {
            case "CARS":
                String vin = JOptionPane.showInputDialog(this, "VIN:", table.getValueAt(row, 1));
                String brand = JOptionPane.showInputDialog(this, "Бренд:", table.getValueAt(row, 2));
                String modelCar = JOptionPane.showInputDialog(this, "Модель:", table.getValueAt(row, 3));
                int year = Integer.parseInt(JOptionPane.showInputDialog(this, "Год:", table.getValueAt(row, 4)));
                double price = Double.parseDouble(JOptionPane.showInputDialog(this, "Цена:", table.getValueAt(row, 5)));
                int warranty = Integer.parseInt(JOptionPane.showInputDialog(this, "Гарантия:", table.getValueAt(row, 6)));
                boolean available = JOptionPane.showConfirmDialog(this, "В наличии?", "Выберите", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                int qty = Integer.parseInt(JOptionPane.showInputDialog(this, "Количество:", table.getValueAt(row, 8)));

                Car car = new Car(vin, brand, modelCar, year, price, warranty, available, qty);
                car.setId(id);
                Response carResp = UserSender.updateCar(car);
                showMessage(carResp, "Автомобиль обновлён");
                loadCars();
                break;

            case "CLIENTS":
                String name = JOptionPane.showInputDialog(this, "ФИО:", table.getValueAt(row, 1));
                String phone = JOptionPane.showInputDialog(this, "Телефон:", table.getValueAt(row, 2));
                String email = JOptionPane.showInputDialog(this, "Email:", table.getValueAt(row, 3));
                String addr = JOptionPane.showInputDialog(this, "Адрес:", table.getValueAt(row, 4));

                Client client = new Client(name, phone, email, addr);
                client.setId(id);
                Response cliResp = UserSender.updateClient(client);
                showMessage(cliResp, "Клиент обновлён");
                loadClients();
                break;

            case "SALES":
                int carId = Integer.parseInt(JOptionPane.showInputDialog(this, "ID Авто:", table.getValueAt(row, 1)));
                int clientId = Integer.parseInt(JOptionPane.showInputDialog(this, "ID Клиента:", table.getValueAt(row, 2)));
                LocalDate date = LocalDate.parse(JOptionPane.showInputDialog(this, "Дата:", table.getValueAt(row, 3)));
                double priceS = Double.parseDouble(JOptionPane.showInputDialog(this, "Цена:", table.getValueAt(row, 4)));

                Sale sale = new Sale(carId, clientId, date, priceS);
                sale.setId(id);
                Response saleResp = UserSender.updateSale(sale);
                showMessage(saleResp, "Продажа обновлена");
                loadSales();
                break;

            case "SERVICE":
                int carIdS = Integer.parseInt(JOptionPane.showInputDialog(this, "ID Авто:", table.getValueAt(row, 1)));
                int clientIdS = Integer.parseInt(JOptionPane.showInputDialog(this, "ID Клиента:", table.getValueAt(row, 2)));
                LocalDate dateS = LocalDate.parse(JOptionPane.showInputDialog(this, "Дата сервиса:", table.getValueAt(row, 3)));
                String desc = JOptionPane.showInputDialog(this, "Описание:", table.getValueAt(row, 4));
                boolean warrantyS = JOptionPane.showConfirmDialog(this, "По гарантии?", "Выберите", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

                ServiceRecord record = new ServiceRecord(carIdS, clientIdS, dateS, desc, warrantyS);
                record.setId(id);
                Response recResp = UserSender.updateServiceRecord(record);
                showMessage(recResp, "Запись обновлена");
                loadServiceRecords();
                break;
        }
    }

    private void handleDelete(String entity, JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Выберите строку для удаления");
            return;
        }

        int id = (int) table.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Удалить запись?", "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Response resp = null;
        switch (entity) {
            case "CARS": resp = UserSender.deleteCar(id); loadCars(); break;
            case "CLIENTS": resp = UserSender.deleteClient(id); loadClients(); break;
            case "SALES": resp = UserSender.deleteSale(id); loadSales(); break;
            case "SERVICE": resp = UserSender.deleteServiceRecord(id); loadServiceRecords(); break;
        }
        showMessage(resp, "Удаление выполнено");
    }

    private void showMessage(Response resp, String successMessage) {
        if (resp.isSuccess()) {
            JOptionPane.showMessageDialog(this, successMessage);
        } else {
            JOptionPane.showMessageDialog(this, "Ошибка: " + resp.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadCars() {
        List<Car> cars = UserSender.getAllCarsDetailed();
        DefaultTableModel model = (DefaultTableModel) carsTable.getModel();
        model.setRowCount(0);
        for (Car car : cars) {
            model.addRow(new Object[]{
                    car.getId(), car.getVIN(), car.getBrand(), car.getModel(),
                    car.getYear(), car.getPrice(), car.getWarrantyYears(),
                    car.isAvailable() ? "Да" : "Нет", car.getQuantity()
            });
        }
    }

    private void loadClients() {
        List<Client> clients = UserSender.getAllClients();
        DefaultTableModel model = (DefaultTableModel) clientsTable.getModel();
        model.setRowCount(0);
        for (Client c : clients) {
            model.addRow(new Object[]{
                    c.getId(), c.getFull_name(), c.getPhone(), c.getEmail(), c.getAddress()
            });
        }
    }

    private void loadSales() {
        List<Sale> sales = UserSender.getAllSales();
        DefaultTableModel model = (DefaultTableModel) salesTable.getModel();
        model.setRowCount(0);
        for (Sale s : sales) {
            model.addRow(new Object[]{
                    s.getId(), s.getCar_id(), s.getClient_id(), s.getSale_date(), s.getSale_price()
            });
        }
    }

    private void loadServiceRecords() {
        List<ServiceRecord> records = UserSender.getAllServiceRecords();
        DefaultTableModel model = (DefaultTableModel) serviceRecordsTable.getModel();
        model.setRowCount(0);
        for (ServiceRecord r : records) {
            model.addRow(new Object[]{
                    r.getId(), r.getCar_id(), r.getClient_id(), r.getService_date(),
                    r.getDescription(), r.is_under_warranty() ? "Да" : "Нет"
            });
        }
    }
}



class CarsWindow extends JFrame {
    private JTable carsTable;

    public CarsWindow(List <Car> cars) {

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

        loadCarsDataFromServer(cars);

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

    private void loadCarsDataFromServer(List<Car> cars) {
        DefaultTableModel model = (DefaultTableModel) carsTable.getModel();
        model.setRowCount(0);  // Очистить все строки
        for (Car car : cars) {
            model.addRow(new Object[]{
                    car.getBrand(),
                    car.getModel(),
                    car.getYear(),
                    car.getPrice(),
                    car.getWarrantyYears(),
                    car.isAvailable() ? "Да" : "Нет"
            });
        }
    }

}

class CreateOrderWindow extends JFrame {
    private JTable carsTable;
    private int clientId;
    private List<Car> carList;

    public CreateOrderWindow(int clientId, List<Car> carList) {
        this.clientId = clientId;
        this.carList = carList;

        setTitle("Создание заказа на покупку автомобиля");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Поисковая панель
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Поиск по автомобилям:");
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Таблица автомобилей
        String[] columnNames = {"ID", "Марка", "Модель", "Год", "Цена", "Гарантия(лет)", "Наличие"};
        carsTable = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollPane = new JScrollPane(carsTable);
        add(scrollPane, BorderLayout.CENTER);

        carsTable.getColumnModel().getColumn(0).setMinWidth(0);
        carsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        carsTable.getColumnModel().getColumn(0).setWidth(0);

        loadCarsData();

        // Кнопки
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        JButton orderButton = new JButton("Оформить заказ");
        JButton exitButton = new JButton("Назад");
        buttonPanel.add(orderButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        orderButton.addActionListener(this::handleCreateOrder);
        exitButton.addActionListener(e -> dispose());

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
        DefaultTableModel model = (DefaultTableModel) carsTable.getModel();
        for (Car car : carList) {
            System.out.println(car.getId());
            if (car.isAvailable()) {
                model.addRow(new Object[]{
                        car.getId(),
                        car.getBrand(),
                        car.getModel(),
                        car.getYear(),
                        car.getPrice(),
                        car.getWarrantyYears(),
                        "Да"
                });
            }
        }
    }

    private void handleCreateOrder(ActionEvent e) {
        int row = carsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите автомобиль.");
            return;
        }

        int modelRow = carsTable.convertRowIndexToModel(row);

        int carId = (int) carsTable.getValueAt(modelRow, 0);
        String brand = (String) carsTable.getValueAt(modelRow, 1);
        String modelName = (String) carsTable.getValueAt(modelRow, 2);
        double price = (double) carsTable.getValueAt(modelRow, 4);

        String[] methods = {"Наличные", "Карта", "Кредит", "Другое"};
        String paymentMethod = showPaymentDialog(methods);

        if (paymentMethod == null) return;

        try {
            // Отправка заказа на сервер
            Order order = new Order(clientId, carId, LocalDate.now(), "Ожидает подтверждения", paymentMethod, price);
            Response response = UserSender.sendOrder(order);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Заказ успешно оформлен!");
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при оформлении заказа: " + response.getMessage());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при отправке заказа: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String showPaymentDialog(String[] methods) {
        return (String) JOptionPane.showInputDialog(
                this,
                "Выберите способ оплаты:",
                "Способ оплаты",
                JOptionPane.QUESTION_MESSAGE,
                null,
                methods,
                methods[0]);
    }
}


class MyOrdersWindow extends JFrame {
    private JTable ordersTable;
    private DefaultTableModel model;
    private int clientId;

    public MyOrdersWindow(int clientId) {
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
        try {
            List<OrderInfo> orders = UserSender.getOrdersForClient(clientId);

            if (orders == null || orders.isEmpty()) {
                JOptionPane.showMessageDialog(this, "У вас нет заказов.");
                return;
            }

            model = (DefaultTableModel) ordersTable.getModel();
            model.setRowCount(0);

            for (OrderInfo order : orders) {
                model.addRow(new Object[]{
                        order.getId(), order.getBrand(), order.getModel(), order.getYear(),
                        order.getTotalPrice(), order.getPaymentMethod(), order.getOrderDate(), order.getStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке заказов: " + e.getMessage());
            e.printStackTrace();
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

        try {
            Response response = UserSender.cancelOrder(orderId);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Заказ успешно отменён.");
                loadOrdersData(clientId);
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при отмене: " + response.getMessage());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка при отмене заказа: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class ManageOrdersWindow extends JFrame {
    private JTable ordersTable;
    private DBManager dbManager;
    private DefaultTableModel model;
    private List<OrderInfo> currentOrders;

    private SalesMonitoringWindow monitoringWindow;

    public ManageOrdersWindow(List<OrderInfo> orders, SalesMonitoringWindow monitoringWindow) {
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
        model.setRowCount(0);
        currentOrders = UserSender.getAllOrders();  // Загрузка с сервера
        for (OrderInfo order : currentOrders) {
            model.addRow(new Object[]{
                    order.getId(),
                    order.getClientId(),
                    order.getBrand(),
                    order.getModel(),
                    order.getYear(),
                    order.getTotalPrice(),
                    order.getPaymentMethod(),
                    order.getOrderDate(),
                    order.getStatus()
            });
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

        Response response = UserSender.updateOrderStatus(orderId, newStatus);
        System.out.println(response);
        if (response.isSuccess()) {
            JOptionPane.showMessageDialog(this, response.getMessage());
            loadOrdersData();
        } else {
            JOptionPane.showMessageDialog(this, "Ошибка: " + response.getMessage());
        }
    }

}


class ChangePasswordDialog extends JDialog {
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private final int userId; // ID текущего пользователя
    private boolean passwordChanged;

    public ChangePasswordDialog(JFrame parent, int userId) {
        super(parent, "Сменить пароль", true);
        this.userId = userId;


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

        // Отправляем запрос на смену пароля
        Response response = UserSender.changePassword(userId, oldPass, newPass);

        if (response.isSuccess()) {
            passwordChanged = true;
            JOptionPane.showMessageDialog(this, "Пароль успешно изменён. Войдите заново.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, response.getMessage());
        }
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }
}
class MyPurchasesWindow extends JFrame {
    private JTable purchasesTable;
    private DefaultTableModel model;
    private int clientId;

    public MyPurchasesWindow(int clientId) {
        this.clientId = clientId;

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
        List<PurchaseInfo> purchases = UserSender.getClientPurchases(clientId);
        model.setRowCount(0);

        for (PurchaseInfo p : purchases) {
            model.addRow(new Object[]{
                    p.getBrand(),
                    p.getModel(),
                    p.getYear(),
                    p.getTotalPrice(),
                    p.getPaymentMethod(),
                    p.getOrderDate()
            });
        }
    }
}
class CreateServiceRequestWindow extends JFrame {
    private final int clientId;
    private final JComboBox<Car> carComboBox; // Комбобокс для выбора автомобиля
    private final JTextArea descriptionArea;

    public CreateServiceRequestWindow(int clientId) {
        this.clientId = clientId;

        setTitle("Создание запроса на техобслуживание");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Панель выбора автомобиля
        JPanel carPanel = new JPanel(new BorderLayout());
        carPanel.add(new JLabel("Выберите автомобиль (опционально):"), BorderLayout.NORTH);

        carComboBox = new JComboBox<>();
        carPanel.add(carComboBox, BorderLayout.CENTER);

        loadUserCars(); // Загружаем машины в таблицу и комбобокс

        // Панель ввода описания
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.add(new JLabel("Описание проблемы:"), BorderLayout.NORTH);
        descriptionArea = new JTextArea(5, 30);
        descriptionPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        // Кнопка отправки
        JButton submitButton = new JButton("Отправить заявку");
        submitButton.addActionListener(e -> handleSubmit());

        // Кнопка закрытия
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        add(carPanel, BorderLayout.NORTH);
        add(descriptionPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadUserCars() {
        List<PurchaseInfo> purchases = UserSender.getClientSales(clientId);

        DefaultComboBoxModel<Car> carComboBoxModel = new DefaultComboBoxModel<>(); // Модель для комбобокса

        for (PurchaseInfo p : purchases) {
            // Заполняем комбобокс
            //System.out.println("PurchaseInfo ID: " + p.getId());
            Car car = new Car(p.getId(), p.getBrand(), p.getModel(), p.getYear());
            carComboBoxModel.addElement(car);
            System.out.println(p.getId() + " " + p.getBrand() + " " + p.getModel() + " " + p.getYear());
        }
        // Устанавливаем модель для комбобокса
        carComboBox.setModel(carComboBoxModel);
    }

    private void handleSubmit() {
        String description = descriptionArea.getText().trim();
        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите описание проблемы.");
            return;
        }

        Car selectedCar = (Car) carComboBox.getSelectedItem();
        Integer carId = selectedCar != null ? selectedCar.getId() : null;

        ServiceRequest request = new ServiceRequest(
                0,
                clientId,
                carId,
                description,
                LocalDateTime.now(),
                "В ожидании"
        );
        System.out.println("Car ID: " + request.getCarId());
        Response response = UserSender.sendServiceRequest(request);
        if ("success".equals(response.getStatus())) {
            JOptionPane.showMessageDialog(this, "Заявка отправлена успешно.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Ошибка при отправке заявки: " + response.getMessage());
        }
    }
}

