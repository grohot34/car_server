import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DBManager dbManager;  // Добавляем DBManager

    // Конструктор ClientHandler теперь принимает dbManager
    public ClientHandler(Socket socket, DBManager dbManager) {
        this.socket = socket;
        this.dbManager = dbManager;
    }

    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            String command = in.readLine();
            if (command != null) {
                String[] parts = command.split(":", 3);
                if (parts.length != 3) {
                    out.write("INVALID_COMMAND");
                    out.newLine();
                    out.flush();
                    return;
                }

                String type = parts[0];
                String login = parts[1];
                String password = parts[2];


                boolean success = switch (type) {
                    case "REGISTER" -> AuthManager.register(dbManager, login, password);  // Передаем dbManager
                    case "LOGIN" -> AuthManager.login(dbManager, login, password);  // Передаем dbManager
                    default -> false;
                };

                out.write(success ? "SUCCESS" : "FAILURE");
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}