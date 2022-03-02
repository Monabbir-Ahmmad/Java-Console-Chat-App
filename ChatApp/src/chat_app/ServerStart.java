package chat_app;

import chat_app.server.Server;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerStart {

    public static void main(String[] args) {
        try {
            Server server = new Server(new ServerSocket(Constants.SERVER_PORT));
            server.createConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
