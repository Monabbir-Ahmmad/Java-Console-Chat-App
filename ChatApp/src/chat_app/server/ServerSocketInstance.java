package chat_app.server;

import chat_app.Constants;
import chat_app.interfaces.IConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ServerSocketInstance implements Runnable, IConnection {

    private static final ArrayList<ServerSocketInstance> serverSocketInstances = new ArrayList<>();

    private final Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    private String clientName = "";


    public ServerSocketInstance(Socket socket) {
        this.socket = socket;
        createConnection();

    }

    @Override
    public void run() {
        serverSocketInstances.add(this);
        try {
            while (socket.isConnected()) {
                receiveMessage();
            }

        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }
    }

    @Override
    public void createConnection() {
        try {
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

            System.out.println("A client has left the chat. Total clients: " + String.valueOf(serverSocketInstances.size() - 1));
            serverSocketInstances.remove(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setClientName() throws IOException {
        clientName = dataInputStream.readUTF();
        broadcastNewUserJoined();
    }

    private void broadcastNewUserJoined() throws IOException {
        for (ServerSocketInstance socketInstance : serverSocketInstances) {
            if (socketInstance != this) {
                socketInstance.dataOutputStream.writeUTF(Constants.NEW_USER_JOINED_MESSAGE);
                socketInstance.dataOutputStream.writeUTF("SERVER: " + clientName + " has joined the chat");
                socketInstance.dataOutputStream.flush();

            }
        }
    }

    private void sendConnectedClientList() throws IOException {
        StringBuilder clientList = new StringBuilder();
        for (int i = 0; i < serverSocketInstances.size(); i++) {
            if (serverSocketInstances.get(i) != this) {
                clientList.append(serverSocketInstances.get(i).clientName);
                if (i < serverSocketInstances.size() - 1)
                    clientList.append(", ");
            }
        }
        dataOutputStream.writeUTF(Constants.GET_CLIENT_LIST);
        dataOutputStream.writeUTF(clientList.toString());
    }

    private void receiveMessage() throws IOException {
        String msgType = dataInputStream.readUTF();

        if (msgType.equals(Constants.SET_CLIENT_NAME)) {
            setClientName();

        } else if (msgType.equals(Constants.GET_CLIENT_LIST)) {
            sendConnectedClientList();

        } else if (msgType.equals(Constants.BROADCAST_MESSAGE)) {
            receiveMessageForBroadcast();

        } else if (msgType.equals(Constants.PRIVATE_MESSAGE)) {
            receiveMessageForPrivate();

        }
    }

    private void receiveMessageForPrivate() throws IOException {
        String clientToSend = dataInputStream.readUTF();
        String text = dataInputStream.readUTF();
        sendPrivateMessage(clientToSend, text);
    }

    private void sendPrivateMessage(String clientToSend, String text) throws IOException {
        for (ServerSocketInstance socketInstance : serverSocketInstances) {
            if (socketInstance.clientName.equals(clientToSend) && socketInstance != this) {
                socketInstance.dataOutputStream.writeUTF(Constants.PRIVATE_MESSAGE);
                socketInstance.dataOutputStream.writeUTF(clientName);
                socketInstance.dataOutputStream.writeUTF(text);

                socketInstance.dataOutputStream.flush();
            }

        }
    }


    private void receiveMessageForBroadcast() throws IOException {
        String text = dataInputStream.readUTF();
        broadcastMsg(text);
    }

    //Broadcast the msg to other clients except the one who sent it
    private void broadcastMsg(String text) throws IOException {
        for (ServerSocketInstance socketInstance : serverSocketInstances) {
            if (socketInstance != this) {
                socketInstance.dataOutputStream.writeUTF(Constants.BROADCAST_MESSAGE);
                socketInstance.dataOutputStream.writeUTF(clientName);
                socketInstance.dataOutputStream.writeUTF(text);

                socketInstance.dataOutputStream.flush();

            }

        }
    }

}
