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
        String command = request.getCommand();
        switch (command) {
            case "REGISTER":
            case "LOGIN": {
                String[] data = (String[]) request.getData();
                String login = data[0];
                String password = data[1];

                if (dbManager.isUserBlocked(login)) {
                    return new Response(false, null, "Пользователь заблокирован.");
                }

                boolean success = command.equals("LOGIN")
                        ? AuthManager.login(dbManager, login, password)
                        : AuthManager.register(dbManager, login, password);

                if (!success) {
                    dbManager.insertLogEntry(login, "Неуспешная попытка " + (command.equals("LOGIN") ? "входа" : "регистрации"), "WARNING");
                    return new Response(false, null, "Ошибка авторизации.");
                }

                int id = dbManager.getUserIdByLogin(login);
                String role = dbManager.getUserRoleByLogin(login);

                dbManager.insertLogEntry(login, "Успешная " + (command.equals("LOGIN") ? "авторизация" : "регистрация"), "INFO");
                return new Response(true, new User(id, login, role), "Успешно");
            }

            case "GET_ALL_CARS": {
                User user = (User) request.getAdditionalData();
                System.out.println(user.getLogin());
                dbManager.insertLogEntry(user.getLogin(), "Просмотр списка автомобилей", "INFO");

                List<Car> cars = dbManager.getAllCars();
                return new Response(true, cars, "Список автомобилей получен.");
            }
            case "GET_ALL_CARS_DETAILED": {
                User user = (User) request.getAdditionalData();
                dbManager.insertLogEntry(user.getLogin(), "Просмотр детальной информации об автомобилях", "INFO");

                List<Car> detailedCars = dbManager.getAllCarsDetailed();
                return new Response(true, detailedCars, "Cписок автомобилей с полной информацией получен");
            }

            case "CREATE_ORDER": {
                User user = (User) request.getAdditionalData();
                Order order = (Order) request.getData();

                boolean success = dbManager.createOrder(order);
                dbManager.insertLogEntry(user.getLogin(), success ? "Создан заказ" : "Не удалось создать заказ", success ? "INFO" : "WARNING");

                if (success) {
                    return new Response(true, null, "Заказ успешно создан");
                } else {
                    return new Response(false, null, "Не удалось создать заказ");
                }
            }
            case "GET_CLIENT_ORDERS": {
                int clientId = (int) request.getData();
                User user = (User) request.getAdditionalData();

                dbManager.insertLogEntry(user.getLogin(), "Просмотр своих заказов", "INFO");
                List<OrderInfo> orders = dbManager.getOrdersForClient(clientId);
                return new Response(true, orders, "Заказы пользователя получены.");
            }
            case "CANCEL_ORDER": {
                int orderToUpdateId = (int) request.getData();
                User user = (User) request.getAdditionalData();

                boolean statusUpdateSuccess = dbManager.updateOrderStatus(orderToUpdateId);
                dbManager.insertLogEntry(user.getLogin(), statusUpdateSuccess ? "Отмена заказа #" + orderToUpdateId : "Не удалось отменить заказ #" + orderToUpdateId,
                        statusUpdateSuccess ? "INFO" : "WARNING");

                if (statusUpdateSuccess) {
                    return new Response(true, null, "Статус заказа обновлён.");
                } else {
                    return new Response(false, null, "Не удалось обновить статус заказа.");
                }
            }
            case "GET_ALL_ORDERS": {
                User user = (User) request.getAdditionalData();
                dbManager.insertLogEntry(user.getLogin(), "Просмотр всех заказов", "INFO");

                List<OrderInfo> allOrders = dbManager.getAllOrders();
                return new Response(true, allOrders, "Список всех заказов получен.");
            }
            case "UPDATE_ORDER_STATUS": {
                Object[] updateData = (Object[]) request.getData();
                int orderIdToUpdate = (int) updateData[0];
                String newStatus = (String) updateData[1];
                User user = (User) request.getAdditionalData();

                boolean updateResult = dbManager.updateOrderStatusWithSales(orderIdToUpdate, newStatus);
                dbManager.insertLogEntry(user.getLogin(),
                        updateResult ? "Изменён статус заказа #" + orderIdToUpdate + " на " + newStatus
                                : "Ошибка при попытке изменить статус заказа #" + orderIdToUpdate,
                        updateResult ? "INFO" : "WARNING");

                if (updateResult) {
                    return new Response(true, null, "Статус заказа обновлён.");
                } else {
                    return new Response(false, null, "Ошибка при обновлении статуса заказа.");
                }
            }
            case "GET_ALL_USERS": {
                User user = (User) request.getAdditionalData();
                dbManager.insertLogEntry(user.getLogin(), "Просмотр списка пользователей", "INFO");
                return new Response(true, dbManager.getUsers(), "Список пользователей получен.");
            }
            case "INSERT_USER": {
                Object[] insertData = (Object[]) request.getData();
                String newLogin = (String) insertData[0];
                String newPassword = (String) insertData[1];
                String newRole = (String) insertData[2];
                User user = (User) request.getAdditionalData();

                if (dbManager.doesLoginExist(newLogin)) {
                    dbManager.insertLogEntry(user.getLogin(), "Попытка добавления пользователя с существующим логином: " + newLogin, "WARNING");
                    return new Response(false, null, "Логин уже существует.");
                }

                dbManager.insertUserWithRole(newLogin, newPassword, newRole);
                dbManager.insertLogEntry(user.getLogin(), "Добавлен пользователь: " + newLogin + " с ролью " + newRole, "INFO");
                return new Response(true, null, "Пользователь добавлен.");
            }

            case "UPDATE_USER_ROLE": {
                Object[] roleData = (Object[]) request.getData();
                int userId = (int) roleData[0];
                String newRole = (String) roleData[1];
                User user = (User) request.getAdditionalData();

                dbManager.updateUserRole(userId, newRole);
                dbManager.insertLogEntry(user.getLogin(), "Обновлена роль пользователя с ID " + userId + " на " + newRole, "INFO");

                return new Response(true, null, "Роль обновлена.");
            }

            case "DELETE_USER": {
                int idToDelete = (int) request.getData();
                User user = (User) request.getAdditionalData();

                dbManager.deleteUserById(idToDelete);
                dbManager.insertLogEntry(user.getLogin(), "Удалён пользователь с ID " + idToDelete, "INFO");

                return new Response(true, null, "Пользователь удалён.");
            }

            case "SET_USER_BLOCKED": {
                Object[] blockData = (Object[]) request.getData();
                String loginToBlock = (String) blockData[0];
                int blockStatus = (int) blockData[1];
                User user = (User) request.getAdditionalData();

                dbManager.setUserBlocked(loginToBlock, blockStatus);
                dbManager.insertLogEntry(user.getLogin(),
                        (blockStatus == 1 ? "Заблокирован" : "Разблокирован") + " пользователь: " + loginToBlock, "INFO");

                return new Response(true, null, "Статус блокировки изменён.");
            }

            case "GET_ALL_CLIENTS": {
                User user = (User) request.getAdditionalData();
                dbManager.insertLogEntry(user.getLogin(), "Просмотр списка клиентов", "INFO");

                return new Response(true, dbManager.getAllClients(), "Клиенты получены.");
            }

            case "GET_ALL_SALES": {
                User user = (User) request.getAdditionalData();
                dbManager.insertLogEntry(user.getLogin(), "Просмотр списка продаж", "INFO");

                return new Response(true, dbManager.getAllSales(), "Продажи получены.");
            }

            case "GET_ALL_SERVICE_RECORDS": {
                User user = (User) request.getAdditionalData();
                dbManager.insertLogEntry(user.getLogin(), "Просмотр сервисных записей", "INFO");

                return new Response(true, dbManager.getAllServiceRecords(), "Сервисные записи получены.");
            }

            case "INSERT_CAR": {
                Car car = (Car) request.getData();
                User user = (User) request.getAdditionalData();

                boolean carInsertSuccess = dbManager.insertCar(car);
                dbManager.insertLogEntry(user.getLogin(),
                        carInsertSuccess ? "Добавлен автомобиль: " + car.getBrand() + " " + car.getModel()
                                : "Ошибка при добавлении автомобиля: " + car.getBrand() + " " + car.getModel(),
                        carInsertSuccess ? "INFO" : "WARNING");

                if (carInsertSuccess) {
                    return new Response(true, null, "Автомобиль добавлен.");
                } else {
                    return new Response(false, null, "Не удалось добавить автомобиль.");
                }
            }
            case "UPDATE_CAR": {
                Car updatedCar = (Car) request.getData();
                User user = (User) request.getAdditionalData();

                boolean carUpdateSuccess = dbManager.updateCar(updatedCar);
                dbManager.insertLogEntry(user.getLogin(),
                        carUpdateSuccess
                                ? "Обновлен автомобиль с ID " + updatedCar.getId()
                                : "Ошибка при обновлении автомобиля с ID " + updatedCar.getId(),
                        carUpdateSuccess ? "INFO" : "WARNING");

                if (carUpdateSuccess) {
                    return new Response(true, null, "Автомобиль обновлен.");
                } else {
                    return new Response(false, null, "Не удалось обновить автомобиль.");
                }
            }

            case "DELETE_CAR": {
                int carIdToDelete = (int) request.getData();
                User user = (User) request.getAdditionalData();

                boolean carDeleteSuccess = dbManager.deleteCar(carIdToDelete);
                dbManager.insertLogEntry(user.getLogin(),
                        carDeleteSuccess
                                ? "Удалён автомобиль с ID " + carIdToDelete
                                : "Ошибка при удалении автомобиля с ID " + carIdToDelete,
                        carDeleteSuccess ? "INFO" : "WARNING");

                if (carDeleteSuccess) {
                    return new Response(true, null, "Автомобиль удалён.");
                } else {
                    return new Response(false, null, "Не удалось удалить автомобиль.");
                }
            }

            case "INSERT_ORDER": {
                OrderInfo orderInfo = (OrderInfo) request.getData();
                User user = (User) request.getAdditionalData();

                boolean orderInsertSuccess = dbManager.insertOrder(orderInfo);
                dbManager.insertLogEntry(user.getLogin(),
                        orderInsertSuccess
                                ? "Добавлен заказ клиента с ID " + orderInfo.getClientId()
                                : "Ошибка при добавлении заказа клиента с ID " + orderInfo.getClientId(),
                        orderInsertSuccess ? "INFO" : "WARNING");

                if (orderInsertSuccess) {
                    return new Response(true, null, "Заказ добавлен");
                } else {
                    return new Response(false, null, "Не удалось добавить заказ");
                }
            }

            case "UPDATE_ORDER": {
                OrderInfo updatedOrder = (OrderInfo) request.getData();
                User user = (User) request.getAdditionalData();

                boolean orderUpdateSuccess = dbManager.updateOrder(updatedOrder);
                dbManager.insertLogEntry(user.getLogin(),
                        orderUpdateSuccess
                                ? "Обновлен заказ с ID " + updatedOrder.getId()
                                : "Ошибка при обновлении заказа с ID " + updatedOrder.getId(),
                        orderUpdateSuccess ? "INFO" : "WARNING");

                if (orderUpdateSuccess) {
                    return new Response(true, null, "Заказ обновлен.");
                } else {
                    return new Response(false, null, "Не удалось обновить заказ.");
                }
            }

            case "DELETE_ORDER": {
                int orderIdToDelete = (int) request.getData();
                User user = (User) request.getAdditionalData();

                boolean orderDeleteSuccess = dbManager.deleteOrder(orderIdToDelete);
                dbManager.insertLogEntry(user.getLogin(),
                        orderDeleteSuccess
                                ? "Удалён заказ с ID " + orderIdToDelete
                                : "Ошибка при удалении заказа с ID " + orderIdToDelete,
                        orderDeleteSuccess ? "INFO" : "WARNING");

                if (orderDeleteSuccess) {
                    return new Response(true, null, "Заказ удалён.");
                } else {
                    return new Response(false, null, "Не удалось удалить заказ.");
                }
            }

            case "INSERT_CLIENT": {
                Client client = (Client) request.getData();
                User user = (User) request.getAdditionalData();

                boolean clientInsertSuccess = dbManager.insertClient(client);
                dbManager.insertLogEntry(user.getLogin(),
                        clientInsertSuccess
                                ? "Добавлен клиент: " + client.getFull_name()
                                : "Ошибка при добавлении клиента: " + client.getFull_name(),
                        clientInsertSuccess ? "INFO" : "WARNING");

                if (clientInsertSuccess) {
                    return new Response(true, null, "Клиент добавлен.");
                } else {
                    return new Response(false, null, "Не удалось добавить клиента.");
                }
            }

            case "UPDATE_CLIENT": {
                Client updatedClient = (Client) request.getData();
                User user = (User) request.getAdditionalData();

                boolean clientUpdateSuccess = dbManager.updateClient(updatedClient);
                dbManager.insertLogEntry(user.getLogin(),
                        clientUpdateSuccess
                                ? "Обновлен клиент с ID " + updatedClient.getId()
                                : "Ошибка при обновлении клиента с ID " + updatedClient.getId(),
                        clientUpdateSuccess ? "INFO" : "WARNING");

                if (clientUpdateSuccess) {
                    return new Response(true, null, "Клиент обновлен.");
                } else {
                    return new Response(false, null, "Не удалось обновить клиента.");
                }
            }

            case "DELETE_CLIENT": {
                int clientIdToDelete = (int) request.getData();
                User user = (User) request.getAdditionalData();

                boolean clientDeleteSuccess = dbManager.deleteClient(clientIdToDelete);
                dbManager.insertLogEntry(user.getLogin(),
                        clientDeleteSuccess
                                ? "Удалён клиент с ID " + clientIdToDelete
                                : "Ошибка при удалении клиента с ID " + clientIdToDelete,
                        clientDeleteSuccess ? "INFO" : "WARNING");

                if (clientDeleteSuccess) {
                    return new Response(true, null, "Клиент удалён.");
                } else {
                    return new Response(false, null, "Не удалось удалить клиента.");
                }
            }

            case "INSERT_SALE": {
                Sale sale = (Sale) request.getData();
                User user = (User) request.getAdditionalData();

                boolean saleInsertSuccess = dbManager.insertSale(sale);
                dbManager.insertLogEntry(user.getLogin(),
                        saleInsertSuccess
                                ? "Добавлена продажа с ID " + sale.getId()
                                : "Ошибка при добавлении продажи с ID " + sale.getId(),
                        saleInsertSuccess ? "INFO" : "WARNING");

                if (saleInsertSuccess) {
                    return new Response(true, null, "Продажа добавлена.");
                } else {
                    return new Response(false, null, "Не удалось добавить продажу.");
                }
            }

            case "UPDATE_SALE": {
                Sale updatedSale = (Sale) request.getData();
                User user = (User) request.getAdditionalData();

                boolean saleUpdateSuccess = dbManager.updateSale(updatedSale);
                dbManager.insertLogEntry(user.getLogin(),
                        saleUpdateSuccess
                                ? "Обновлена продажа с ID " + updatedSale.getId()
                                : "Ошибка при обновлении продажи с ID " + updatedSale.getId(),
                        saleUpdateSuccess ? "INFO" : "WARNING");

                if (saleUpdateSuccess) {
                    return new Response(true, null, "Продажа обновлена.");
                } else {
                    return new Response(false, null, "Не удалось обновить продажу.");
                }
            }

            case "DELETE_SALE": {
                int saleIdToDelete = (int) request.getData();
                User user = (User) request.getAdditionalData();

                boolean saleDeleteSuccess = dbManager.deleteSale(saleIdToDelete);
                dbManager.insertLogEntry(user.getLogin(),
                        saleDeleteSuccess
                                ? "Удалена продажа с ID " + saleIdToDelete
                                : "Ошибка при удалении продажи с ID " + saleIdToDelete,
                        saleDeleteSuccess ? "INFO" : "WARNING");

                if (saleDeleteSuccess) {
                    return new Response(true, null, "Продажа удалена.");
                } else {
                    return new Response(false, null, "Не удалось удалить продажу.");
                }
            }

            case "INSERT_SERVICE_RECORD": {
                ServiceRecord serviceRecord = (ServiceRecord) request.getData();
                User user = (User) request.getAdditionalData();

                boolean serviceRecordInsertSuccess = dbManager.insertServiceRecord(serviceRecord);
                dbManager.insertLogEntry(user.getLogin(),
                        serviceRecordInsertSuccess
                                ? "Добавлена сервисная запись с ID " + serviceRecord.getId()
                                : "Ошибка при добавлении сервисной записи с ID " + serviceRecord.getId(),
                        serviceRecordInsertSuccess ? "INFO" : "WARNING");

                if (serviceRecordInsertSuccess) {
                    return new Response(true, null, "Сервисная запись добавлена.");
                } else {
                    return new Response(false, null, "Не удалось добавить сервисную запись.");
                }
            }

            case "UPDATE_SERVICE_RECORD": {
                ServiceRecord updatedServiceRecord = (ServiceRecord) request.getData();
                User user = (User) request.getAdditionalData();

                boolean serviceRecordUpdateSuccess = dbManager.updateServiceRecord(updatedServiceRecord);
                dbManager.insertLogEntry(user.getLogin(),
                        serviceRecordUpdateSuccess
                                ? "Обновлена сервисная запись с ID " + updatedServiceRecord.getId()
                                : "Ошибка при обновлении сервисной записи с ID " + updatedServiceRecord.getId(),
                        serviceRecordUpdateSuccess ? "INFO" : "WARNING");

                if (serviceRecordUpdateSuccess) {
                    return new Response(true, null, "Сервисная запись обновлена.");
                } else {
                    return new Response(false, null, "Не удалось обновить сервисную запись.");
                }
            }

            case "DELETE_SERVICE_RECORD": {
                int serviceRecordIdToDelete = (int) request.getData();
                User user = (User) request.getAdditionalData();

                boolean serviceRecordDeleteSuccess = dbManager.deleteServiceRecord(serviceRecordIdToDelete);
                dbManager.insertLogEntry(user.getLogin(),
                        serviceRecordDeleteSuccess
                                ? "Удалена сервисная запись с ID " + serviceRecordIdToDelete
                                : "Ошибка при удалении сервисной записи с ID " + serviceRecordIdToDelete,
                        serviceRecordDeleteSuccess ? "INFO" : "WARNING");

                if (serviceRecordDeleteSuccess) {
                    return new Response(true, null, "Сервисная запись удалена.");
                } else {
                    return new Response(false, null, "Не удалось удалить сервисную запись.");
                }
            }

            case "GET_CLIENT_PURCHASES": {
                int clientIdForPurchases = (int) request.getData();
                User user = (User) request.getAdditionalData();

                List<PurchaseInfo> purchases = dbManager.getClientPurchases(clientIdForPurchases);
                dbManager.insertLogEntry(user.getLogin(),
                        "Получены покупки клиента с ID " + clientIdForPurchases,
                        "INFO");

                return new Response(true, purchases, "Покупки клиента получены.");
            }

            case "GET_CLIENT_SALES": {
                int clientIdForSales = (int) request.getData();
                User user = (User) request.getAdditionalData();

                List<PurchaseInfo> clients_cars = dbManager.getClientPurchasesWithCarId(clientIdForSales);
                dbManager.insertLogEntry(user.getLogin(),
                        "Получены автомобили клиента с ID " + clientIdForSales,
                        "INFO");

                return new Response(true, clients_cars, "Автомобили клиента получены.");
            }

            case "CHANGE_PASSWORD":
                Object[] dat = (Object[]) request.getData();
                int userId = (int) dat[0];
                String oldPassword = (String) dat[1];
                String newPass = (String) dat[2];
                User user = (User) request.getAdditionalData();

                boolean success = dbManager.updatePassword(userId, oldPassword, newPass);

                dbManager.insertLogEntry(user.getLogin(),
                        success ? "Пароль успешно изменён пользователем с ID " + userId
                                : "Ошибка при изменении пароля пользователем с ID " + userId,
                        success ? "INFO" : "WARNING");

                if (success) {
                    return new Response(true, null, "Пароль успешно изменён.");
                } else {
                    return new Response(false, null, "Старый пароль неверен.");
                }
            case "CREATE_BACKUP":
                user = (User) request.getAdditionalData();

                boolean suc = dbManager.createBackup();

                dbManager.insertLogEntry(user.getLogin(),
                        suc ? "Резервная копия базы данных успешно создана."
                                : "Ошибка при создании резервной копии базы данных.",
                        suc ? "INFO" : "WARNING");

                if (suc) {
                    return new Response(true, null, "Резервная копия успешно создана на сервере.");
                } else {
                    return new Response(false, null, "Не удалось создать резервную копию.");
                }
            case "DOWNLOAD_BACKUP":
                user = (User) request.getAdditionalData();
                try {
                    File backupFile = dbManager.getLatestBackupFile();
                    byte[] fileBytes = Files.readAllBytes(backupFile.toPath());
                    dbManager.insertLogEntry(user.getLogin(),
                            "Резервная копия базы данных успешно передана.",
                            "INFO");
                    return new Response(true, fileBytes, "Резервная копия передана.");
                } catch (IOException e) {
                    dbManager.insertLogEntry(user.getLogin(),
                            "Ошибка при передаче резервной копии базы данных: " + e.getMessage(),
                            "ERROR");
                    e.printStackTrace();
                    return new Response(false, null, "Ошибка при передаче резервной копии.");
                }
            case "CREATE_SERVICE_REQUEST":
                ServiceRequest serviceRequest = (ServiceRequest) request.getData();
                user = (User) request.getAdditionalData();

                success = dbManager.createServiceRequest(serviceRequest);

                dbManager.insertLogEntry(user.getLogin(),
                        success ? "Создана заявка на техобслуживание с ID " + serviceRequest.getId()
                                : "Ошибка при создании заявки на техобслуживание.",
                        success ? "INFO" : "WARNING");

                if (success) {
                    return new Response(true, null, "Заявка на техобслуживание создана.");
                } else {
                    return new Response(false, null, "Ошибка при создании заявки.");
                }
            case "GET_ALL_SERVICE_REQUESTS":
                user = (User) request.getAdditionalData();

                List<ServiceRequest> requests = dbManager.getAllServiceRequests();

                dbManager.insertLogEntry(user.getLogin(),
                        "Получены все заявки на сервисное обслуживание.",
                        "INFO");

                return new Response(true, requests, "Заявки получены");


            case "UPDATE_SERVICE_REQUEST":
                Object[] update_Data = (Object[]) request.getData();
                int order_IdToUpdate = (int) update_Data[0];
                String new_Status = (String) update_Data[1];
                user = (User) request.getAdditionalData();

                boolean update_Result = dbManager.updateServiceRequest(order_IdToUpdate, new_Status);

                dbManager.insertLogEntry(user.getLogin(),
                        update_Result
                                ? "Обновлён статус заявки на техобслуживание с ID " + order_IdToUpdate + " на '" + new_Status + "'"
                                : "Ошибка при обновлении статуса заявки с ID " + order_IdToUpdate,
                        update_Result ? "INFO" : "WARNING");

                if (update_Result) {
                    return new Response(true, null, "Статус заявки обновлён.");
                } else {
                    return new Response(false, null, "Ошибка при обновлении статуса заявки.");
                }
            case "SEND_REVIEW":
                Review review = (Review) request.getData();
                user = (User) request.getAdditionalData();

                success = dbManager.insertReview(review);

                dbManager.insertLogEntry(user.getLogin(),
                        success ? "Добавлен новый отзыв." : "Ошибка при добавлении отзыва.",
                        success ? "INFO" : "WARNING");

                return new Response(success, null, success ? "Отзыв добавлен." : "Ошибка добавления отзыва");
            case "GET_ALL_REVIEWS":
                user = (User) request.getAdditionalData();

                List<Review> reviews = dbManager.getAllReviews();

                dbManager.insertLogEntry(user.getLogin(),
                        "Получены все отзывы.",
                        "INFO");

                return new Response(true, reviews, "Отзывы получены");
            case "GET_CLIENT_PROFILE":
                int clientId = (int) request.getData();
                user = (User) request.getAdditionalData();

                Client client = dbManager.getClientById(clientId);

                dbManager.insertLogEntry(user.getLogin(),
                        client != null
                                ? "Получен профиль клиента с ID " + clientId
                                : "Клиент с ID " + clientId + " не найден",
                        client != null ? "INFO" : "WARNING");

                if (client != null) {
                    return new Response(true, client, "Данные клиента получены.");
                } else {
                    return new Response(false, null, "Клиент не найден.");
                }
            case "UPDATE_CLIENT_PROFILE":
                Client clientToUpdate = (Client) request.getData();
                user = (User) request.getAdditionalData();

                success = dbManager.updateClient(clientToUpdate);

                dbManager.insertLogEntry(user.getLogin(),
                        success
                                ? "Обновлен профиль клиента с ID " + clientToUpdate.getId()
                                : "Ошибка при обновлении профиля клиента с ID " + clientToUpdate.getId(),
                        success ? "INFO" : "WARNING");

                if (success) {
                    return new Response(true, null, "Данные успешно обновлены.");
                } else {
                    return new Response(false, null, "Не удалось обновить данные клиента.");
                }
            case "GET_LOGS":
                System.out.println("Обработка запроса get_logs");
                List<LogEntry> logs = dbManager.getAllLogs();
                return new Response(true, logs, "Логи успешно получены.");
            case "GET_SERVICE_REQUESTS_FOR_CLIENT": {
                clientId = (int) request.getData();
                user = (User) request.getAdditionalData();

                requests = dbManager.getServiceRequestsForClient(clientId);

                dbManager.insertLogEntry(user.getLogin(),
                        "Просмотр заявок на сервисное обслуживание для клиента ID " + clientId,
                        "INFO");

                return new Response(true, requests, "Заявки загружены");
            }

            case "CANCEL_SERVICE_REQUEST": {
                int requestId = (int) request.getData();
                user = (User) request.getAdditionalData();

                success = dbManager.cancelServiceRequest(requestId);

                dbManager.insertLogEntry(user.getLogin(),
                        success
                                ? "Отмена сервисной заявки ID " + requestId
                                : "Не удалось отменить сервисную заявку ID " + requestId,
                        success ? "INFO" : "WARNING");

                if (success) {
                    return new Response(true, null, "Заявка отменена");
                } else {
                    return new Response(false, null, "Не удалось отменить заявку");
                }
            }

            case "GET_SERVICE_HISTORY": {
                clientId = (int) request.getData();
                user = (User) request.getAdditionalData();

                List<ServiceRecord> history = dbManager.getServiceHistoryForClient(clientId);

                dbManager.insertLogEntry(user.getLogin(),
                        "Просмотр истории сервисного обслуживания для клиента ID " + clientId,
                        "INFO");

                return new Response(true, history, "История получена успешно");
            }
            case "get_sales_report":
                List<Sale> sales = dbManager.getAllSales();
                return new Response(true, sales, "Отчет по продажам");

            case "get_service_report":
                List<ServiceRecord> services = dbManager.getAllServiceRecords();
                return new Response(true, services, "Отчет по техобслуживанию");
            default:
                return new Response(false, null, "Неизвестная команда");
        }


    }

}