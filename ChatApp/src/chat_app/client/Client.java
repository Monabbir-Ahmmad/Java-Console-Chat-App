package chat_app.client;

import chat_app.Constants;
import chat_app.interfaces.IConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;


public class Client implements IConnection {
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    private String username;
    private String[] connectedUsers;
    private final Scanner scanner = new Scanner(System.in);

    private final String broadcastKeyword = "everyone";


    public Client() {
        createConnection();
        receiveMessages();
        setUserName();
        sendMessage();
    }

    private void sendMessage() {
        try {
            System.out.println("---- Type '@everyone' before message to broadcast messages ----");

            System.out.println("---- Type '@receiver_name' before message to send private messages ----");

            while (socket.isConnected()) {
                String input = scanner.nextLine();

                String[] inputs = input.split(" ", 2);

                String msgTo = inputs[0].replace("@", "");

                String text = inputs[1];

                if (msgTo.equals(broadcastKeyword)) {
                    sendBroadcastMessage(text);
                } else {
                    sendPrivateMessage(msgTo, text);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }

    }

    private void sendPrivateMessage(String msgTo, String text) throws IOException {
        dataOutputStream.writeUTF(Constants.GET_CLIENT_LIST);

        if (Arrays.stream(connectedUsers).anyMatch(msgTo::equals)) {
            dataOutputStream.writeUTF(Constants.PRIVATE_MESSAGE);
            dataOutputStream.writeUTF(msgTo);
            dataOutputStream.writeUTF(text);
            dataOutputStream.flush();
        } else {
            System.out.println("No such user exists. Try again");
        }

    }

    private void sendBroadcastMessage(String text) throws IOException {
        dataOutputStream.writeUTF(Constants.BROADCAST_MESSAGE);
        dataOutputStream.writeUTF(text);
        dataOutputStream.flush();

    }

    private void receiveMessages() {
        new Thread(() -> {
            try {
                while (socket.isConnected()) {

                    String msgType = dataInputStream.readUTF();


                    if (msgType.equals(Constants.BROADCAST_MESSAGE)) {
                        String senderName = dataInputStream.readUTF();
                        String text = dataInputStream.readUTF();

                        System.out.println("\n" + senderName + " (Broadcast): " + text);

                    } else if (msgType.equals(Constants.PRIVATE_MESSAGE)) {
                        String senderName = dataInputStream.readUTF();
                        String text = dataInputStream.readUTF();

                        System.out.println("\n" + senderName + " (Private): " + text);

                    } else if (msgType.equals(Constants.GET_CLIENT_LIST)) {
                        connectedUsers = dataInputStream.readUTF().split(", ");
                    } else if (msgType.equals(Constants.NEW_USER_JOINED_MESSAGE)) {
                        System.out.println("\n" + dataInputStream.readUTF());
                        dataOutputStream.writeUTF(Constants.GET_CLIENT_LIST);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                closeConnection();
            }

        }).start();
    }

    @Override
    public void createConnection() {
        try {
            socket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());


        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }
    }

    @Override
    public void closeConnection() {
        try {
            if (dataInputStream != null) dataInputStream.close();

            if (dataOutputStream != null) dataOutputStream.close();

            if (socket != null) socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void setUserName() {
        boolean flag = true;
        try {
            while (flag) {
                dataOutputStream.writeUTF(Constants.GET_CLIENT_LIST);

                System.out.print("Enter your username: ");
                username = scanner.nextLine();
                if (username.trim().isBlank()) {
                    System.out.println("Username can not be empty.");
                } else if (Arrays.stream(connectedUsers).anyMatch(username::equals)) {
                    System.out.println("This username is already taken. Try something new.");
                } else if (username.equals(broadcastKeyword)) {
                    System.out.println("Username can not be a chat keyword.");
                } else {
                    flag = false;
                }
            }

            dataOutputStream.writeUTF(Constants.SET_CLIENT_NAME);
            dataOutputStream.writeUTF(username);

            dataOutputStream.flush();

            System.out.println("Username saved");

        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }

    }
}
