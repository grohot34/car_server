import Request_Response.Request;
import Request_Response.Response;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserSender {
    public static Response sendRequest(Request request) {
        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new Response(false, null, "Ошибка соединения с сервером.");
        }
    }
    public static List<Car> getAllCars() {
        Response response = sendRequest(new Request("GET_ALL_CARS", null));
        if (response.isSuccess()) {
            System.out.println("true");
            return (List<Car>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());

            return new ArrayList<>();
        }
    }
    public static List<Car> getAllCarsDetailed() {
        Response response = sendRequest(new Request("GET_ALL_CARS_DETAILED", null));
        if (response.isSuccess()) {
            return (List<Car>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка получения списка автомобилей: " + response.getMessage());
            return new ArrayList<>();
        }
    }

    public static Response sendOrder(Order order) throws IOException, ClassNotFoundException {
        return sendRequest(new Request("CREATE_ORDER", order));
    }

    public static List<OrderInfo> getOrdersForClient(int clientId) {
        // Отправляем запрос на получение заказов пользователя
        Response response = sendRequest(new Request("GET_CLIENT_ORDERS", clientId));

        if (response.isSuccess()) {
            return (List<OrderInfo>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());
            return new ArrayList<>();
        }
    }

    public static Response cancelOrder(int orderId) {
        return sendRequest(new Request("CANCEL_ORDER", orderId));
    }

    public static List<OrderInfo> getAllOrders() {
        Response response = sendRequest(new Request("GET_ALL_ORDERS", null));
        if (response.isSuccess()) {
            return (List<OrderInfo>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());
            return new ArrayList<>();
        }
    }
    public static Response updateOrderStatus(int orderId, String newStatus) {
        Object[] data = {orderId, newStatus};
        return sendRequest(new Request("UPDATE_ORDER_STATUS", data));
    }

    public static List<User> getAllUsers() {
        Response response = sendRequest(new Request("GET_ALL_USERS", null));
        if (response.isSuccess()) {
            return (List<User>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());
            return new ArrayList<>();
        }
    }

    public static Response insertUser(String login, String password, String role) {
        Object[] data = {login, password, role};
        return sendRequest(new Request("INSERT_USER", data));
    }

    public static Response updateUserRole(int userId, String newRole) {
        Object[] data = {userId, newRole};
        return sendRequest(new Request("UPDATE_USER_ROLE", data));
    }

    public static Response deleteUser(int userId) {
        return sendRequest(new Request("DELETE_USER", userId));
    }

    public static Response setUserBlocked(String login, int blockStatus) {
        Object[] data = {login, blockStatus};
        return sendRequest(new Request("SET_USER_BLOCKED", data));
    }

    public static List<Client> getAllClients() {
        Response response = sendRequest(new Request("GET_ALL_CLIENTS", null));
        return response.isSuccess() ? (List<Client>) response.getData() : new ArrayList<>();
    }

    public static List<Sale> getAllSales() {
        Response response = sendRequest(new Request("GET_ALL_SALES", null));
        return response.isSuccess() ? (List<Sale>) response.getData() : new ArrayList<>();
    }

    public static List<ServiceRecord> getAllServiceRecords() {
        Response response = sendRequest(new Request("GET_ALL_SERVICE_RECORDS", null));
        return response.isSuccess() ? (List<ServiceRecord>) response.getData() : new ArrayList<>();
    }


    public static Response insertCar(Car car) {
        return sendRequest(new Request("INSERT_CAR", car));
    }

    public static Response updateCar(Car car) {
        return sendRequest(new Request("UPDATE_CAR", car));
    }

    public static Response deleteCar(int carId) {
        return sendRequest(new Request("DELETE_CAR", carId));
    }

    public static Response insertClient(Client client) {
        return sendRequest(new Request("INSERT_CLIENT", client));
    }

    public static Response updateClient(Client client) {
        return sendRequest(new Request("UPDATE_CLIENT", client));
    }

    public static Response deleteClient(int clientId) {
        return sendRequest(new Request("DELETE_CLIENT", clientId));
    }
    public static Response insertSale(Sale sale) {
        return sendRequest(new Request("INSERT_SALE", sale));
    }

    public static Response updateSale(Sale sale) {
        return sendRequest(new Request("UPDATE_SALE", sale));
    }

    public static Response deleteSale(int saleId) {
        return sendRequest(new Request("DELETE_SALE", saleId));
    }
    public static Response insertServiceRecord(ServiceRecord record) {
        return sendRequest(new Request("INSERT_SERVICE_RECORD", record));
    }

    public static Response updateServiceRecord(ServiceRecord record) {
        return sendRequest(new Request("UPDATE_SERVICE_RECORD", record));
    }

    public static Response deleteServiceRecord(int recordId) {
        return sendRequest(new Request("DELETE_SERVICE_RECORD", recordId));
    }

    public static List<PurchaseInfo> getClientPurchases(int clientId) {
        Response response = sendRequest(new Request("GET_CLIENT_PURCHASES", clientId));
        if (response.isSuccess()) {
            return (List<PurchaseInfo>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());
            return new ArrayList<>();
        }
    }

    public static List<PurchaseInfo> getClientSales(int clientId) {
        Response response = sendRequest(new Request("GET_CLIENT_SALES", clientId));
        if (response.isSuccess()) {
            return (List<PurchaseInfo>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());
            return new ArrayList<>();
        }
    }


    public static Response changePassword(int userId, String oldPassword, String newPassword) {
        Object[] data = {userId, oldPassword, newPassword}; // Подготавливаем данные для запроса
        return sendRequest(new Request("CHANGE_PASSWORD", data)); // Отправляем запрос
    }
    public static Response createBackup() {
        return sendRequest(new Request("CREATE_BACKUP", null));
    }
    public static void downloadBackup(Component parent) {
        Response response = sendRequest(new Request("DOWNLOAD_BACKUP", null));
        if (response.isSuccess()) {
            byte[] fileData = (byte[]) response.getData();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохранить резервную копию");
            fileChooser.setSelectedFile(new File("car_dealing_backup.sql"));

            int userSelection = fileChooser.showSaveDialog(parent);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                try {
                    FileOutputStream fos = new FileOutputStream(fileChooser.getSelectedFile());
                    fos.write(fileData);
                    fos.close();
                    JOptionPane.showMessageDialog(parent, "Резервная копия сохранена успешно.");
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(parent, "Ошибка при сохранении файла.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(parent, "Ошибка: " + response.getMessage());
        }
    }
    public static Response sendServiceRequest(ServiceRequest request) {
        return sendRequest(new Request("CREATE_SERVICE_REQUEST", request));

    }

}



