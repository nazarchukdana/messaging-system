import java.io.*;
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
    private List<String> banned;

    public ConnectionHandler(Socket socket, List<String> banned) {
        this.banned = banned;
        this.socket = socket;
        try {
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Exception while init output and input");
        }

    }
    @Override
    public void run() {
        try{
            thread = Thread.currentThread();
            sendMessage("Connected to server");
            showConnectedUsers();
            setClientName();
            if(exitBeforeName(clientName)) return;
            Server.broadcastMessage(clientName + " has connected", this);
            String message;
            while ((message = input.readLine()) != null) {
                if (isExit(message)) {
                    return;
                }
                if(containsBannedPhrase(message)){
                    sendMessage("You can't send this message. It contains prohibited content.");
                }
                else if (message.matches(".*-msg\\s*\"[^\"]+\"(\\s*\"[^\"]+\")*$")) {
                    handlePrivateMessage(message);
                }
                else if (message.matches(".*-e\\s*\"[^\"]+\"(\\s*\"[^\"]+\")*$")) {
                    handleExclusionMessage(message);
                }
                else if (message.matches("^\\s*-banned\\s*$")){
                    showBannedPhrases();
                }
                else Server.broadcastMessage(clientName+": "+message, this);
            }
        } catch (IOException e) {
            System.err.println("Exception while sending messages");
        }finally {
            thread.interrupt();
            try {
                socket.close();
                input.close();
                output.close();
            } catch (IOException e) {
                System.err.println("Exception with closing");
            }
            Server.disconnectClient(this);
        }
    }

    public void sendMessage(String message) {
       output.println(message);
    }
    private synchronized void showConnectedUsers(){
        List<String> clientsNames = Server.getClientsNames();
        String message = clientsNames.isEmpty() ? "No users connected" : "Connected users:\n"+ String.join(",\n", clientsNames);
        sendMessage(message);
    }
    private synchronized void setClientName(){
        sendMessage("Enter your name:");
        try {
            clientName = input.readLine();
        } catch (IOException e) {
            System.err.println("Exception while reading name");
        }
    }
    private void setClientNameDefault(){
        clientName = "Client";
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
    private boolean containsBannedPhrase(String message) {
        for (String bannedPhrase : banned) {
            if (message.toLowerCase().contains(bannedPhrase.toLowerCase())) {
                return true;
            }
        }
        return false;
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
    private void showBannedPhrases(){
        sendMessage("Banned phrases:\n"+String.join(",\n", banned));
    }


    public String getClientName(){return clientName;}
}
