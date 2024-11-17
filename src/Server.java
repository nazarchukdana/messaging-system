import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int PORT;
    private static List<ConnectionHandler> clients = new ArrayList<>();
    private final int MAX_CLIENTS = 4;
    public Server(){
        if(!readServerInfo()){
            System.out.println("Cannot read file");
            return;
        }
        System.out.println("Server is listening on port " + PORT);
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                synchronized (clients) {
                    if (clients.size() >= MAX_CLIENTS) {
                        try{
                            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
                            output.println("Unable to connect, connection limit exceeded");
                            output.close();
                        } catch (IOException e) {
                            System.err.println("Exception while sending message");
                        }
                        clientSocket.close();
                        continue;
                    }
                    System.out.println("New client connected");
                    ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket);
                    clients.add(connectionHandler);
                    new Thread(connectionHandler).start();
                }
            }
        } catch (IOException e) {
            System.err.println("Exception while setting socket");
        }
    }
    private boolean readServerInfo(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./src/server_info.txt"));
            String serverName = reader.readLine();
            String portLine = reader.readLine();
            if (serverName == null || portLine == null) {
                System.out.println("Invalid format.");
                return false;
            }
            PORT = Integer.parseInt(portLine.trim());
            reader.close();
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("File with server info not found");
        } catch (IOException e) {
            System.err.println("Exception while reading the file with server info");
        }
        return false;
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
    public static void disconnectClient(ConnectionHandler connectionHandler){
        removeClient(connectionHandler);
        broadcastMessage(connectionHandler.getClientName() + " has disconnected", connectionHandler);
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
    }

    public static void main(String[] args) {
        Server server = new Server();
    }

}
