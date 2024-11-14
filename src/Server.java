import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 29888;
    private static List<ConnectionHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Server is listening on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket);
                clients.add(connectionHandler);
                new Thread(connectionHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static synchronized void broadcastMessage(String message, ConnectionHandler sender) {
        for (ConnectionHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    public static synchronized void removeClient(ConnectionHandler connectionHandler){
        clients.remove(connectionHandler);
        Thread clientThread = connectionHandler.getThread();
        if (clientThread != null) {
            clientThread.interrupt();
        }
    }
    public static synchronized List<String> getClientsNames() {
        List<String> clientNames = new ArrayList<>();
        for (ConnectionHandler client : clients) {
            String clientName = client.getClientName();
            if(clientName != null) clientNames.add(client.getClientName());
        }
        return clientNames;
    }
}
