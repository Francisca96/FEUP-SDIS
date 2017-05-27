package app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;


public class Chat {

    public static void main(String[] args) {
        String server = args[0]; //server = "localhost"
        ChatAccess access = new ChatAccess();
        int port;
        if (args[1] == "Room1") {
            port = 2222;
        } else
            port = 2223;

        JFrame frame = new ChatFrame(access);
        frame.setTitle("ChatRoom");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);

        try {
            access.InitSocket(server, port);
        } catch (IOException ex) {
            System.out.println("Could not connect to " + server + ":" + port);
            ex.printStackTrace();
            System.exit(0);
        }
    }

    static class ChatAccess extends Observable {
        private Socket socket;
        private OutputStream outputStream;

        @Override
        public void notifyObservers(Object arg) {
            super.setChanged();
            super.notifyObservers(arg);
        }

        public void InitSocket(String server, int port) throws IOException {
            socket = new Socket(server, port);
            outputStream = socket.getOutputStream();

            Thread receivingThread = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null)
                        notifyObservers(line);
                } catch (IOException ex) {
                    notifyObservers(ex);
                }
            });
            receivingThread.start();
        }

        private static final String CRLF = "\r\n"; // novalinha


        public void send(String text) {
            try {
                outputStream.write((text + CRLF).getBytes());
                outputStream.flush();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException ex) {
                notifyObservers(ex);
            }
        }
    }

    // Interface gráfica
    static class ChatFrame extends JFrame implements Observer {

        private JEditorPane usersArea;
        private JTextArea textArea;
        private JTextField inputTextField;
        private JButton sendButton;
        private ChatAccess chatAccess;

        public ChatFrame(ChatAccess chatAccess) {
            this.chatAccess = chatAccess;
            chatAccess.addObserver(this);
            buildGUI();
        }

        // User interface
        private void buildGUI() {
            textArea = new JTextArea(20, 50);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setBackground(new Color(108, 183, 242));
            add(new JScrollPane(textArea), BorderLayout.CENTER);

            usersArea = new JEditorPane();
            usersArea.setEditable(false);
            usersArea.setBackground(new Color(141, 141, 145));
            usersArea.setSize(150, 10);
            usersArea.setText("WHO'S ONLINE?\n\n");
            add(usersArea, BorderLayout.EAST);

            Box box = Box.createHorizontalBox();
            add(box, BorderLayout.SOUTH);
            inputTextField = new JTextField();
            sendButton = new JButton("Send");
            box.add(inputTextField);
            box.add(sendButton);

            ActionListener sendListener = e -> {
                String str = inputTextField.getText();
                if (str != null && str.trim().length() > 0)
                    chatAccess.send(str);
                inputTextField.selectAll();
                inputTextField.requestFocus();
                inputTextField.setText("");
            };
            inputTextField.addActionListener(sendListener);
            sendButton.addActionListener(sendListener);

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    chatAccess.close();
                }
            });
        }

        public void update(Observable o, Object arg) {
            final Object finalArg = arg;
            SwingUtilities.invokeLater(() -> {
                textArea.append(finalArg.toString());
                textArea.append("\n");
            });
        }
    }
}