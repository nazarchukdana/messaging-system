import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class Client {
    private int SERVER_PORT;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean connected;
    private JFrame frame;
    private JTextField messageField;
    private JTextArea chatArea;
    private JButton sendButton;
    private Color textColor;
    private Color backgroundColor;
    private Thread receiverThread;
    public Client() {
        setGUI();
        setConnection();
    }
    private void setGUI(){
        frame = new JFrame("Client");
        Font font = new Font("Centaur", Font.BOLD, 18);
        backgroundColor = Color.BLACK;
        textColor = new Color(252,19,192);

        messageField = new JTextField(30);
        messageField.setFont(font);
        messageField.setForeground(textColor);
        messageField.setBackground(backgroundColor);
        messageField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(textColor, 2),
                new EmptyBorder(10, 10, 10, 10)
        ));
        messageField.setCaretColor(textColor);

        sendButton = new JButton("Send");
        sendButton.setFont(font);
        sendButton.setForeground(backgroundColor);
        sendButton.setBackground(textColor);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(new LineBorder(textColor, 2));
        sendButton.setPreferredSize(new Dimension(100, 50));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(messageField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        frame.add(panel, BorderLayout.SOUTH);

        chatArea = new JTextArea();
        chatArea.setFont(font);
        chatArea.setForeground(textColor);
        chatArea.setBackground(backgroundColor);
        chatArea.setEditable(false);
        chatArea.setCaretColor(backgroundColor);
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(null);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        messageField.requestFocusInWindow();
        setListeners();
    }
    private void setConnection(){
        connected = false;
        if(!readServerInfo()) {
            chatArea.append("Unable to connect to server(cannot read file)\n");
            return;
        }
        try {
            socket = new Socket("localhost", SERVER_PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            chatArea.append(input.readLine() + "\n");
            connected = true;
            receiverThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        String message;
                        while ((message = input.readLine()) != null) {
                            chatArea.append(message + "\n");
                        }
                    } catch (IOException e) {
                    } finally {
                        closeStreamsAndStopThread();
                        connected = false;
                        chatArea.append("Disconnected from server\n");
                    }
                }
            });
            receiverThread.start();

        } catch (IOException e) {
            chatArea.append("Unable to connect to server\n");
        }
    }
    private boolean readServerInfo(){
        try (BufferedReader reader = new BufferedReader(new FileReader("./src/server_info.txt"))) {
            reader.readLine();
            String portLine = reader.readLine();
            if (portLine == null) {
                return false;
            }
            SERVER_PORT = Integer.parseInt(portLine.trim());
            reader.close();
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("File with server info not found");
        } catch (IOException e) {
            System.err.println("Exception while reading the file");
        }
        return false;
    }

    private void setListeners(){
        sendButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sendMessage();
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {
                sendButton.setForeground(textColor);
                sendButton.setBackground(backgroundColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                sendButton.setForeground(backgroundColor);
                sendButton.setBackground(textColor);
            }
        });
        messageField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) sendMessage();
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if(connected) {
                    connected = false;
                    output.println("exit");
                    closeStreamsAndStopThread();
                }
            }
        });
    }
    private void sendMessage(){
        if(connected) {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                output.println(message);
                displayMessage(message);
                closeIfExit(message);
            }
        }
    }
    private void displayMessage(String message){
        chatArea.append("You: " + message + '\n');
        messageField.setText("");
    }
    private void closeIfExit(String message){
        if(isExit(message)){
            closeStreamsAndStopThread();
            frame.dispose();
            System.exit(0);
        }
    }
    private boolean isExit(String message){
        return message.equalsIgnoreCase("exit");
    }
    private void closeStreamsAndStopThread(){
        receiverThread.interrupt();
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException ex) {
            System.err.println("Exception with closing");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
