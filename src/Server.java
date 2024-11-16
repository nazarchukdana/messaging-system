import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int PORT;
    private static List<ConnectionHandler> clients = new ArrayList<>();
    private static final int MAX_CLIENTS = 2;
    public Server(){
        if(!readServerInfo()){
            System.out.println("Unable to connect");
            return;
        }
        System.out.println("Server is listening on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                synchronized (clients) {
                    if (clients.size() >= MAX_CLIENTS) {
                        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                            out.println("Unable to connect, connection limit exceeded");
                        }
                        clientSocket.close();
                        continue; // Skip further processing for this client
                    }

                    System.out.println("New client connected");
                    ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket);
                    clients.add(connectionHandler);
                    new Thread(connectionHandler).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean readServerInfo(){
        try (BufferedReader reader = new BufferedReader(new FileReader("./src/server_info.txt"))) {
            String serverName = reader.readLine();
            String portLine = reader.readLine();
            if (serverName == null || portLine == null) {
                System.out.println("Invalid format.");
                return false;
            }
            PORT = Integer.parseInt(portLine.trim());
            return true;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
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
    public static synchronized void disconnectClient(String clientName, ConnectionHandler connectionHandler){
        removeClient(connectionHandler);
        broadcastMessage(clientName + " has disconnected", connectionHandler);
    }
}
