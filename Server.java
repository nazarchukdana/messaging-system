import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int PORT;
    private static List<ConnectionHandler> clients;
    private final List<String> banned;
    public Server(){
        clients = new ArrayList<>();
        banned = new ArrayList<>();
        readServerInfo();
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                synchronized (clients) {
                    System.out.println("New client connected");
                    ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket, banned);
                    clients.add(connectionHandler);
                    new Thread(connectionHandler).start();
                }
            }
        } catch (IOException e) {
            System.err.println("Exception while setting socket");
            System.exit(1);
        }
    }
    private void readServerInfo(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./src/server_info.txt"));
            reader.readLine();
            String portLine = reader.readLine().trim();
            if (portLine == null) {
                System.err.println("Invalid format.");
                System.exit(1);
            }
            PORT = Integer.parseInt(portLine);
            String line;
            while ((line = reader.readLine().trim()) != null && !line.isEmpty()){
                banned.add(line);
            }
            if(banned.isEmpty()) {
                System.err.println("Invalid format.");
                System.exit(1);
            }
            reader.close();

        } catch (FileNotFoundException e) {
            System.err.println("File with server info not found");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Exception while reading the file with server info");
            System.exit(1);
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
    public static synchronized ConnectionHandler getClientByName(String name) {
        for (ConnectionHandler client : clients) {
            if (name.equals(client.getClientName())) {
                return client;
            }
        }
        return null;
    }
    public static synchronized void broadcastMessage(String message, ConnectionHandler sender) {
        for (ConnectionHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    public static void disconnectClient(ConnectionHandler client){
        removeClient(client);
        broadcastMessage(client.getClientName() + " has disconnected", client);
    }

    public static synchronized void removeClient(ConnectionHandler client){
        clients.remove(client);
    }

    public static void main(String[] args) {
        Server server = new Server();
    }

}
