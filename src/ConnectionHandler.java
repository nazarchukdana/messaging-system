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
            showConnectedUsers();
            setClientName();
            if(isExit(clientName)){
                setClientNameDefault();
                return;
            }
            Server.broadcastMessage(clientName + " has connected", this);
            String message;
            while (!thread.isInterrupted() && (message = input.readLine().trim()) != null) {
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
            Server.disconnectClient(clientName, this);
        }
    }
    private boolean isExit(String message){
        return message.equalsIgnoreCase("exit");
    }
    public Thread getThread() {
        return thread;
    }
    private void showConnectedUsers(){
        List<String> clientsNames = Server.getClientsNames();
        sendMessage(clientsNames.isEmpty() ? "No users connected" : "Connected users:\n"+ String.join(",\n", Server.getClientsNames()));
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
