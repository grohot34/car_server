import model.User;
import util.BackupManager;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
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
    public ClientWindow() {
        super("Окно клиента", "Добро пожаловать, Клиент!");
        // можно добавить клиентские компоненты
    }
}
// Окно для клиента
class ManagerWindow extends MainWindow {
    public ManagerWindow() {
        super("Окно менеджера", "Добро пожаловать, Менеджер!");
        // можно добавить клиентские компоненты
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