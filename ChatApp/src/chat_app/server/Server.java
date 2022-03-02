package chat_app.server;

import chat_app.interfaces.IConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements IConnection {
    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void createConnection() {
        System.out.println("Server is running");

        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                ServerSocketInstance serverSocketInstance = new ServerSocketInstance(socket);

                Thread thread = new Thread(serverSocketInstance);
                thread.start();
                System.out.println("A new client has joined");
            }

        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }
    }

    @Override
    public void closeConnection() {
        System.out.println("Server has stopped working");

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
