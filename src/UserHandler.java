import model.Car;
import model.Order;
import model.OrderInfo;
import model.User;

import Request_Response.Request;
import Request_Response.Response;

import java.io.*;
import java.net.*;
import java.util.List;

public class UserHandler implements Runnable {
    private final Socket socket;
    private final DBManager dbManager;  // Добавляем DBManager

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
                Request request = (Request) in.readObject();
                Response response = handleRequest(request);
                out.writeObject(response);
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Клиент отключился.");
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
                return new Response(true, new User(id, login, role), "Успешно");
            case "GET_ALL_CARS":
                List<Car> cars = dbManager.getAllCars();
                return new Response(true, cars, "Список автомобилей получен.");
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
            // Добавляйте другие команды по мере необходимости
            default:
                return new Response(false, null, "Неизвестная команда");
        }
    }

}