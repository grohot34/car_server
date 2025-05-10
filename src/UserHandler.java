import model.*;

import Request_Response.Request;
import Request_Response.Response;
import util.BackupManager;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.List;

public class UserHandler implements Runnable {
    private final Socket socket;
    private final DBManager dbManager;// Добавляем DBManager

    // Конструктор ClientHandler теперь принимает dbManager
    public UserHandler(Socket socket, DBManager dbManager) {
        this.socket = socket;
        this.dbManager = dbManager;
    }

    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            while (true) {
                Request request = (Request) in.readObject(); // Ждем команду от клиента
                Response response = handleRequest(request);
                out.writeObject(response);
                out.flush();

            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Клиент отключился");
        }
    }
    private Response handleRequest(Request request) {
        switch (request.getCommand()) {
            case "REGISTER":
            case "LOGIN":
                String[] data = (String[]) request.getData();
                String login = data[0];
                String password = data[1];
                if (dbManager.isUserBlocked(login)) {
                    return new Response(false, null, "Пользователь заблокирован.");
                }

                boolean success = request.getCommand().equals("LOGIN")
                        ? AuthManager.login(dbManager, login, password)
                        : AuthManager.register(dbManager, login, password);

                if (!success)
                    return new Response(false, null, "Ошибка авторизации.");

                int id = dbManager.getUserIdByLogin(login);
                String role = dbManager.getUserRoleByLogin(login);
                System.out.println("1");
                return new Response(true, new User(id, login, role), "Успешно");
            case "GET_ALL_CARS":
                List<Car> cars = dbManager.getAllCars();
                return new Response(true, cars, "Список автомобилей получен.");
            case "GET_ALL_CARS_DETAILED":
                List<Car> detailedCars = dbManager.getAllCarsDetailed();
                return new Response(true, detailedCars, "Cписок автомобилей с полной информацией получен");
            case "CREATE_ORDER":
                Order order = (Order) request.getData();
                success = dbManager.createOrder(order);
                System.out.println(success);
                if (success) {
                    return new Response(true, null,"Заказ успешно создан");
                } else {
                    return new Response(false, null,"Не удалось создать заказ");
                }
            case "GET_CLIENT_ORDERS":
                int clientId = (int) request.getData();
                List<OrderInfo> orders = dbManager.getOrdersForClient(clientId);
                return new Response(true, orders, "Заказы пользователя получены.");
            case "CANCEL_ORDER":
                int orderToUpdateId = (int) request.getData();
                boolean statusUpdateSuccess = dbManager.updateOrderStatus(orderToUpdateId);
                if (statusUpdateSuccess) {
                    return new Response(true, null, "Статус заказа обновлён.");
                } else {
                    return new Response(false, null, "Не удалось обновить статус заказа.");
                }
            case "GET_ALL_ORDERS":
                List<OrderInfo> allOrders = dbManager.getAllOrders();  // этот метод мы сейчас сделаем
                return new Response(true, allOrders, "Список всех заказов получен.");
            case "UPDATE_ORDER_STATUS":
                Object[] updateData = (Object[]) request.getData();
                int orderIdToUpdate = (int) updateData[0];
                String newStatus = (String) updateData[1];
                boolean updateResult = dbManager.updateOrderStatusWithSales(orderIdToUpdate, newStatus);
                if (updateResult) {
                    return new Response(true, null, "Статус заказа обновлён.");
                } else {
                    return new Response(false, null, "Ошибка при обновлении статуса заказа.");
                }
            case "GET_ALL_USERS":
                return new Response(true, dbManager.getUsers(), "Список пользователей получен.");

            case "INSERT_USER":
                Object[] insertData = (Object[]) request.getData();
                String newLogin = (String) insertData[0];
                String newPassword = (String) insertData[1];
                String newRole = (String) insertData[2];

                if (dbManager.doesLoginExist(newLogin)) {
                    return new Response(false, null, "Логин уже существует.");
                }
                dbManager.insertUserWithRole(newLogin, newPassword, newRole);
                return new Response(true, null, "Пользователь добавлен.");

            case "UPDATE_USER_ROLE":
                Object[] roleData = (Object[]) request.getData();
                dbManager.updateUserRole((int) roleData[0], (String) roleData[1]);
                return new Response(true, null, "Роль обновлена.");

            case "DELETE_USER":
                dbManager.deleteUserById((int) request.getData());
                return new Response(true, null, "Пользователь удалён.");

            case "SET_USER_BLOCKED":
                Object[] blockData = (Object[]) request.getData();
                dbManager.setUserBlocked((String) blockData[0], (int) blockData[1]);
                return new Response(true, null, "Статус блокировки изменён.");

            case "GET_ALL_CLIENTS":
                return new Response(true, dbManager.getAllClients(), "Клиенты получены.");

            case "GET_ALL_SALES":
                return new Response(true, dbManager.getAllSales(), "Продажи получены.");

            case "GET_ALL_SERVICE_RECORDS":
                return new Response(true, dbManager.getAllServiceRecords(), "Сервисные записи получены.");
            case "INSERT_CAR":
                Car car = (Car) request.getData();
                boolean carInsertSuccess = dbManager.insertCar(car);
                if (carInsertSuccess) {
                    return new Response(true, null, "Автомобиль добавлен.");
                } else {
                    return new Response(false, null, "Не удалось добавить автомобиль.");
                }

            case "UPDATE_CAR":
                Car updatedCar = (Car) request.getData();
                boolean carUpdateSuccess = dbManager.updateCar(updatedCar);
                if (carUpdateSuccess) {
                    return new Response(true, null, "Автомобиль обновлен.");
                } else {
                    return new Response(false, null, "Не удалось обновить автомобиль.");
                }

            case "DELETE_CAR":
                int carIdToDelete = (int) request.getData();
                boolean carDeleteSuccess = dbManager.deleteCar(carIdToDelete);
                if (carDeleteSuccess) {
                    return new Response(true, null, "Автомобиль удалён.");
                } else {
                    return new Response(false, null, "Не удалось удалить автомобиль.");
                }
            case "INSERT_ORDER":
                OrderInfo orderInfo = (OrderInfo) request.getData();
                boolean orderInsertSuccess = dbManager.insertOrder(orderInfo);
                if (orderInsertSuccess) {
                    return new Response(true, null, "Заказ добавлен");
                } else {
                    return new Response(false, null, "Не удалось добавить заказ");
                }

            case "UPDATE_ORDER":
                OrderInfo updatedOrder = (OrderInfo) request.getData();
                boolean orderUpdateSuccess = dbManager.updateOrder(updatedOrder);
                if (orderUpdateSuccess) {
                    return new Response(true, null, "Заказ обновлен.");
                } else {
                    return new Response(false, null, "Не удалось обновить заказ.");
                }

            case "DELETE_ORDER":
                int orderIdToDelete = (int) request.getData();
                boolean orderDeleteSuccess = dbManager.deleteOrder(orderIdToDelete);
                if (orderDeleteSuccess) {
                    return new Response(true, null, "Заказ удалён.");
                } else {
                    return new Response(false, null, "Не удалось удалить заказ.");
                }
            case "INSERT_CLIENT":
                Client client = (Client) request.getData();
                boolean clientInsertSuccess = dbManager.insertClient(client);
                if (clientInsertSuccess) {
                    return new Response(true, null, "Клиент добавлен.");
                } else {
                    return new Response(false, null, "Не удалось добавить клиента.");
                }

            case "UPDATE_CLIENT":
                Client updatedClient = (Client) request.getData();
                boolean clientUpdateSuccess = dbManager.updateClient(updatedClient);
                if (clientUpdateSuccess) {
                    return new Response(true, null, "Клиент обновлен.");
                } else {
                    return new Response(false, null, "Не удалось обновить клиента.");
                }

            case "DELETE_CLIENT":
                int clientIdToDelete = (int) request.getData();
                boolean clientDeleteSuccess = dbManager.deleteClient(clientIdToDelete);
                if (clientDeleteSuccess) {
                    return new Response(true, null, "Клиент удалён.");
                } else {
                    return new Response(false, null, "Не удалось удалить клиента.");
                }

            case "INSERT_SALE":
                Sale sale = (Sale) request.getData();
                boolean saleInsertSuccess = dbManager.insertSale(sale);
                if (saleInsertSuccess) {
                    return new Response(true, null, "Продажа добавлена.");
                } else {
                    return new Response(false, null, "Не удалось добавить продажу.");
                }

            case "UPDATE_SALE":
                Sale updatedSale = (Sale) request.getData();
                boolean saleUpdateSuccess = dbManager.updateSale(updatedSale);
                if (saleUpdateSuccess) {
                    return new Response(true, null, "Продажа обновлена.");
                } else {
                    return new Response(false, null, "Не удалось обновить продажу.");
                }

            case "DELETE_SALE":
                int saleIdToDelete = (int) request.getData();
                boolean saleDeleteSuccess = dbManager.deleteSale(saleIdToDelete);
                if (saleDeleteSuccess) {
                    return new Response(true, null, "Продажа удалена.");
                } else {
                    return new Response(false, null, "Не удалось удалить продажу.");
                }

            case "INSERT_SERVICE_RECORD":
                ServiceRecord serviceRecord = (ServiceRecord) request.getData();
                boolean serviceRecordInsertSuccess = dbManager.insertServiceRecord(serviceRecord);
                if (serviceRecordInsertSuccess) {
                    return new Response(true, null, "Сервисная запись добавлена.");
                } else {
                    return new Response(false, null, "Не удалось добавить сервисную запись.");
                }

            case "UPDATE_SERVICE_RECORD":
                ServiceRecord updatedServiceRecord = (ServiceRecord) request.getData();
                boolean serviceRecordUpdateSuccess = dbManager.updateServiceRecord(updatedServiceRecord);
                if (serviceRecordUpdateSuccess) {
                    return new Response(true, null, "Сервисная запись обновлена.");
                } else {
                    return new Response(false, null, "Не удалось обновить сервисную запись.");
                }

            case "DELETE_SERVICE_RECORD":
                int serviceRecordIdToDelete = (int) request.getData();
                boolean serviceRecordDeleteSuccess = dbManager.deleteServiceRecord(serviceRecordIdToDelete);
                if (serviceRecordDeleteSuccess) {
                    return new Response(true, null, "Сервисная запись удалена.");
                } else {
                    return new Response(false, null, "Не удалось удалить сервисную запись.");
                }
            case "GET_CLIENT_PURCHASES":
                int clientIdForPurchases = (int) request.getData();
                List<PurchaseInfo> purchases = dbManager.getClientPurchases(clientIdForPurchases);
                return new Response(true, purchases, "Покупки клиента получены.");

                case "GET_CLIENT_SALES":
                    int clientIdForSales = (int) request.getData();
                    List<PurchaseInfo> clients_cars = dbManager.getClientPurchasesWithCarId(clientIdForSales);

                    return new Response(true, clients_cars, "Автомобили клиента получены.");
            case "CHANGE_PASSWORD":
                // Получаем данные из запроса
                Object[] dat = (Object[]) request.getData();
                int userId = (int) dat[0]; // ID пользователя
                String oldPassword = (String) dat[1]; // Старый пароль
                String newPass = (String) dat[2]; // Новый пароль

                // Вызываем метод updatePassword из DBManager
                boolean succes = dbManager.updatePassword(userId, oldPassword, newPass);
                if (succes) {
                    return new Response(true, null, "Пароль успешно изменён.");
                } else {
                    return new Response(false, null, "Старый пароль неверен.");
                }
            case "CREATE_BACKUP":
                boolean suc = dbManager.createBackup();
                if (suc) {
                    return new Response(true, null, "Резервная копия успешно создана на сервере.");
                } else {
                    return new Response(false, null, "Не удалось создать резервную копию.");
                }
            case "DOWNLOAD_BACKUP":
                try {
                    File backupFile = dbManager.getLatestBackupFile(); // метод, возвращающий File
                    byte[] fileBytes = Files.readAllBytes(backupFile.toPath());
                    return new Response(true, fileBytes, "Резервная копия передана.");
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Response(false, null, "Ошибка при передаче резервной копии.");
                }
            case "CREATE_SERVICE_REQUEST":
                ServiceRequest serviceRequest = (ServiceRequest) request.getData();
                success = dbManager.createServiceRequest(serviceRequest);
                if (success) {
                    return new Response(true, null, "Заявка на техобслуживание создана.");
                } else {
                    return new Response(false, null, "Ошибка при создании заявки.");
                }
            default:
                return new Response(false, null, "Неизвестная команда");
        }
    }

}