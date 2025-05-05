import Request_Response.Request;
import Request_Response.Response;
import model.Car;
import model.Order;
import model.OrderInfo;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
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
}



