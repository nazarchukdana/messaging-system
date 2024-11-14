import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

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
            List<String> clientsNames = Server.getClientsNames();
            sendMessage(clientsNames.isEmpty() ? "No users connected" : "Connected users:\n"+ String.join(",\n", Server.getClientsNames()));
            sendMessage("Enter your name:");
            clientName = input.readLine();
            if (clientName == null || clientName.trim().isEmpty() ) {
                clientName = "Client";
            }
            if(isExit(clientName)){
                clientName = "Client";
                return;
            }
            Server.broadcastMessage(clientName + " has connected", this);
            String message;
            while (!thread.isInterrupted() && (message = input.readLine()) != null) {
                if (isExit(message)) {
                    return;
                }
                Server.broadcastMessage(clientName+": "+message, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Server.removeClient(this);
            Server.broadcastMessage(clientName + " has disconnected", this);
        }
    }
    private boolean isExit(String message){
        return message.equalsIgnoreCase("exit");
    }
    public Thread getThread() {
        return thread;
    }
}
