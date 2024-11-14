import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class Client {
    private static final String HOSTNAME = "localhost";
    private static final int SERVER_PORT = 29888;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean connected = false;
    private JFrame frame;
    private JTextField messageField;
    private JTextArea chatArea;
    private Thread receiverThread;
    public Client() {
        frame = new JFrame("Client");
        Font font = new Font("Centaur", Font.BOLD, 18);
        Color backgroundColor = Color.BLACK;
        Color textColor = new Color(252,19,192);

        messageField = new JTextField(30);
        messageField.setFont(font);
        messageField.setForeground(textColor);
        messageField.setBackground(backgroundColor);
        messageField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(textColor, 2), // Magenta border of 2px width
                new EmptyBorder(10, 10, 10, 10) // Padding inside
        ));
        messageField.setCaretColor(textColor);

        JButton sendButton = new JButton("Send");
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

        try {
            socket = new Socket(HOSTNAME, SERVER_PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            chatArea.append("Connected to server\n");
            connected = true;
            receiverThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        String message;
                        while ((message = input.readLine()) != null) {
                            chatArea.append(message + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        chatArea.append("Disconnected from server\n");
                    }
                }
            });
            receiverThread.start();

        } catch (IOException e) {
            chatArea.append("Unable to connect to server\n");
        }
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

        // Handle "Enter" key press
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
                    output.println("exit");
                    if (receiverThread != null) {
                        receiverThread.interrupt();
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
    private synchronized void sendMessage(){
        if(connected) {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                output.println(message);
                chatArea.append("You: " + message + '\n');
                messageField.setText("");
                if(message.equalsIgnoreCase("exit")){
                    if (receiverThread != null && receiverThread.isAlive()) {
                        receiverThread.interrupt();
                    }
                    frame.dispose();
                    System.exit(0);
                }
            }
        }
    }
}
