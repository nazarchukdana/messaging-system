import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionHandler implements Runnable {
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private String clientName;
    private Thread thread;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
        try {
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendMessage(String message) {
       output.println(message);
    }
    public String getClientName(){return clientName;}

    @Override
    public void run() {
        try{
            thread = Thread.currentThread();
            showConnectedUsers();
            setClientName();
            if(exitBeforeName(clientName)) return;
            Server.broadcastMessage(clientName + " has connected", this);
            String message;
            while (!thread.isInterrupted() && (message = input.readLine().trim()) != null) {
                if (isExit(message)) {
                    return;
                }
                if (message.matches(".*-msg\\s*\"[^\"]+\"(\\s*\"[^\"]+\")*$")) {
                    handlePrivateMessage(message);
                }
                else if (message.matches(".*-e\\s*\"[^\"]+\"(\\s*\"[^\"]+\")*$")) {
                    handleExclusionMessage(message);
                }
                else Server.broadcastMessage(clientName+": "+message, this);
            }
        } catch (IOException e) {

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
            Server.disconnectClient(this);
        }
    }
    private void handlePrivateMessage(String message) {
        String actualMessage = message.replaceAll("-msg\\s*\"[^\"]+\"(\\s*\"[^\"]+\")*$", "").trim();
        List<String> targetClientsNames = extractClientsNames(message);
        for (String targetClientName : targetClientsNames) {
            if(!targetClientName.equals(clientName)) {
                sendPrivateMessage(targetClientName, actualMessage);
            }
        }
    }
    private void handleExclusionMessage(String message) {
        String actualMessage = message.replaceAll("-e\\s*\"[^\"]+\"(\\s*\"[^\"]+\")*$", "").trim();
        List<String> excludedClientsNames = extractClientsNames(message);
        List<String> clientsNames = Server.getClientsNames();
        for (String targetClientName : clientsNames){
            if (!excludedClientsNames.contains(targetClientName) && !targetClientName.equals(clientName)) {
                sendPrivateMessage(targetClientName, actualMessage);
            }
        }
    }
    private List<String> extractClientsNames(String message){
        List<String> targetClients = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]+)\"").matcher(message);
        while (matcher.find()) {
            targetClients.add(matcher.group(1));
        }
        return targetClients;
    }
    private void sendPrivateMessage(String targetClientName, String message){
        ConnectionHandler targetClient = Server.getClientByName(targetClientName);
        if (targetClient != null) {
            targetClient.sendMessage(clientName + " (private): " + message);
        } else{
            sendMessage("User \"" + targetClientName + "\" not found.");
        }
    }
    private boolean exitBeforeName(String message){
        if(isExit(message)) {
            setClientNameDefault();
            return true;
        }
        return false;
    }
    private boolean isExit(String message){
        return message.equalsIgnoreCase("exit");
    }
    public Thread getThread() {
        return thread;
    }
    private synchronized void showConnectedUsers(){
        List<String> clientsNames = Server.getClientsNames();
        String message = clientsNames.isEmpty() ? "No users connected" : "Connected users:\n"+ String.join(",\n", clientsNames);
        sendMessage(message);
    }
    private void setClientName() throws IOException {
        sendMessage("Enter your name:");
        clientName = input.readLine();
        if (clientName == null || clientName.trim().isEmpty() ) {
            setClientNameDefault();
        }
    }
    private void setClientNameDefault(){
        clientName = "Client";
    }
}
