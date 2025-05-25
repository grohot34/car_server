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
    public static User currentUser;
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
    public static List<Car> getAllCars(User currentUser) {
        Response response = sendRequest(new Request("GET_ALL_CARS", null, currentUser));
        if (response.isSuccess()) {
            System.out.println("true");
            return (List<Car>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());

            return new ArrayList<>();
        }
    }
    public static List<Car> getAllCarsDetailed(User currentUser) {
        Response response = sendRequest(new Request("GET_ALL_CARS_DETAILED", null, currentUser));
        if (response.isSuccess()) {
            return (List<Car>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка получения списка автомобилей: " + response.getMessage());
            return new ArrayList<>();
        }
    }

    public static Response sendOrder(Order order, User currentUser) throws IOException, ClassNotFoundException {
        return sendRequest(new Request("CREATE_ORDER", order, currentUser));
    }

    public static List<OrderInfo> getOrdersForClient(int clientId) {
        Response response = sendRequest(new Request("GET_CLIENT_ORDERS", clientId, currentUser));

        if (response.isSuccess()) {
            return (List<OrderInfo>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());
            return new ArrayList<>();
        }
    }

    public static Response cancelOrder(int orderId) {
        return sendRequest(new Request("CANCEL_ORDER", orderId, currentUser));
    }

    public static List<OrderInfo> getAllOrders() {
        Response response = sendRequest(new Request("GET_ALL_ORDERS", null, currentUser));
        if (response.isSuccess()) {
            return (List<OrderInfo>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());
            return new ArrayList<>();
        }
    }
    public static Response updateOrderStatus(int orderId, String newStatus) {
        Object[] data = {orderId, newStatus};
        return sendRequest(new Request("UPDATE_ORDER_STATUS", data, currentUser));
    }

    public static List<User> getAllUsers() {
        Response response = sendRequest(new Request("GET_ALL_USERS", null, currentUser));
        if (response.isSuccess()) {
            return (List<User>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());
            return new ArrayList<>();
        }
    }

    public static Response insertUser(String login, String password, String role) {
        Object[] data = {login, password, role};
        return sendRequest(new Request("INSERT_USER", data, currentUser));
    }

    public static Response updateUserRole(int userId, String newRole) {
        Object[] data = {userId, newRole};
        return sendRequest(new Request("UPDATE_USER_ROLE", data, currentUser));
    }

    public static Response deleteUser(int userId) {
        return sendRequest(new Request("DELETE_USER", userId, currentUser));
    }

    public static Response setUserBlocked(String login, int blockStatus) {
        Object[] data = {login, blockStatus};
        return sendRequest(new Request("SET_USER_BLOCKED", data, currentUser));
    }

    public static List<Client> getAllClients() {
        Response response = sendRequest(new Request("GET_ALL_CLIENTS", null, currentUser));
        return response.isSuccess() ? (List<Client>) response.getData() : new ArrayList<>();
    }

    public static List<Sale> getAllSales() {
        Response response = sendRequest(new Request("GET_ALL_SALES", null, currentUser));
        return response.isSuccess() ? (List<Sale>) response.getData() : new ArrayList<>();
    }

    public static List<ServiceRecord> getAllServiceRecords() {
        Response response = sendRequest(new Request("GET_ALL_SERVICE_RECORDS", null, currentUser));
        return response.isSuccess() ? (List<ServiceRecord>) response.getData() : new ArrayList<>();
    }


    public static Response insertCar(Car car) {
        return sendRequest(new Request("INSERT_CAR", car, currentUser));
    }

    public static Response updateCar(Car car) {
        return sendRequest(new Request("UPDATE_CAR", car, currentUser));
    }

    public static Response deleteCar(int carId) {
        return sendRequest(new Request("DELETE_CAR", carId, currentUser));
    }

    public static Response insertClient(Client client) {
        return sendRequest(new Request("INSERT_CLIENT", client, currentUser));
    }

    public static Response updateClient(Client client) {
        return sendRequest(new Request("UPDATE_CLIENT", client, currentUser));
    }

    public static Response deleteClient(int clientId) {
        return sendRequest(new Request("DELETE_CLIENT", clientId, currentUser));
    }
    public static Response insertSale(Sale sale) {
        return sendRequest(new Request("INSERT_SALE", sale, currentUser));
    }

    public static Response updateSale(Sale sale) {
        return sendRequest(new Request("UPDATE_SALE", sale, currentUser));
    }

    public static Response deleteSale(int saleId) {
        return sendRequest(new Request("DELETE_SALE", saleId, currentUser));
    }
    public static Response insertServiceRecord(ServiceRecord record) {
        return sendRequest(new Request("INSERT_SERVICE_RECORD", record, currentUser));
    }

    public static Response updateServiceRecord(ServiceRecord record) {
        return sendRequest(new Request("UPDATE_SERVICE_RECORD", record, currentUser));
    }

    public static Response deleteServiceRecord(int recordId) {
        return sendRequest(new Request("DELETE_SERVICE_RECORD", recordId, currentUser));
    }

    public static List<PurchaseInfo> getClientPurchases(int clientId) {
        Response response = sendRequest(new Request("GET_CLIENT_PURCHASES", clientId, currentUser));
        if (response.isSuccess()) {
            return (List<PurchaseInfo>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());
            return new ArrayList<>();
        }
    }

    public static List<PurchaseInfo> getClientSales(int clientId) {
        Response response = sendRequest(new Request("GET_CLIENT_SALES", clientId, currentUser));
        if (response.isSuccess()) {
            System.out.println(response.getData());
            return (List<PurchaseInfo>) response.getData();
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка: " + response.getMessage());
            return new ArrayList<>();
        }
    }


    public static Response changePassword(int userId, String oldPassword, String newPassword) {
        Object[] data = {userId, oldPassword, newPassword};
        return sendRequest(new Request("CHANGE_PASSWORD", data));
    }
    public static Response createBackup() {
        return sendRequest(new Request("CREATE_BACKUP", null, currentUser));
    }
    public static void downloadBackup(Component parent) {
        Response response = sendRequest(new Request("DOWNLOAD_BACKUP", null, currentUser));
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
        return sendRequest(new Request("CREATE_SERVICE_REQUEST", request, currentUser));

    }

    public static Response getAllServiceRequests() {
        Request request = new Request("GET_ALL_SERVICE_REQUESTS", null, currentUser);
        return sendRequest(request);
    }

    public static Response updateServiceRequestStatus(int orderId, String newStatus) {
        Object[] data = {orderId, newStatus};
        return sendRequest(new Request("UPDATE_SERVICE_REQUEST", data, currentUser));
    }

    public static Response sendReview(int clientId, String description, int rating) {
        Review review = new Review(clientId, description, rating);
        Request request = new Request("SEND_REVIEW", review, currentUser);
        return sendRequest(request);
    }

    public static Response getAllReviews() {
        Request request = new Request("GET_ALL_REVIEWS", null, currentUser);
        return sendRequest(request);
    }

    public static Response getClientProfile(int clientId) {
        Request request = new Request("GET_CLIENT_PROFILE", clientId, currentUser);
        return sendRequest(request);
    }

    public static Response updateClientProfile(Client client) {
        Request request = new Request("UPDATE_CLIENT_PROFILE", client, currentUser);
        return sendRequest(request);
    }

    public static Response getLogs() {
        return sendRequest(new Request("GET_LOGS", null));
    }

    public static List<ServiceRequest> getServiceRequestsForClient(int clientId) throws IOException, ClassNotFoundException {
        Request request = new Request("GET_SERVICE_REQUESTS_FOR_CLIENT", clientId, currentUser);
        Response response = sendRequest(request);

        if (response.isSuccess()) {
            return (List<ServiceRequest>) response.getData();
        } else {
            throw new RuntimeException("Ошибка при получении заявок: " + response.getMessage());
        }
    }

    public static Response cancelServiceRequest(int requestId) throws IOException, ClassNotFoundException {
        Request request = new Request("CANCEL_SERVICE_REQUEST", requestId, currentUser);
        return sendRequest(request);
    }

    public static List<ServiceRecord> getServiceHistoryForClient(int clientId) throws IOException, ClassNotFoundException {
        Request request = new Request("GET_SERVICE_HISTORY", clientId, currentUser);
        Response response = sendRequest(request);

        if (response.isSuccess()) {
            return (List<ServiceRecord>) response.getData();
        } else {
            throw new RuntimeException(response.getMessage());
        }
    }






}



