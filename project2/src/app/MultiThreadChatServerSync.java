package app;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadChatServerSync {

    private static ServerSocket serverSocket = null;

    private static Socket clientSocket = null;

    // Maximo clientes variavel
    private static final int max = 10;
    private static final clientThread[] clientes = new clientThread[max];

    public static void main(String[] args) {

        // Numero da porta pode ser variavel
        int portNumber = Integer.parseInt(args[0]);
        if (args.length < 1) {
            //System.out.println("Usage: java MultiThreadChatServerSync <portNumber>\n");
            System.out.println("Using port = " + portNumber);
        } else {
            portNumber = Integer.valueOf(args[0]).intValue();
        }

        // Abrir servidor na porta
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

        // Criar uma socket para cada cliente
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                //só entra no loop depois de ter sido feita uma conexao
                int i;
                for (i = 0; i < max; i++) {
                    if (clientes[i] == null) {
                        (clientes[i] = new clientThread(clientSocket, clientes)).start();
                        break;
                    }
                }
                if (i == max) {
                    //PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    //os.close();
                    UIManager UI = new UIManager();
                    UI.put("OptionPane.background", new ColorUIResource(108, 183, 242));
                    UI.put("Panel.background", new ColorUIResource(108, 183, 242));
                    Icon icon = new ImageIcon("/Users/Francisca/Desktop/MIEIC/3Ano/2semestre/SDIS/FEUP-SDIS/project2/src/res/communication.png");
                    //PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    JOptionPane.showMessageDialog(null, "Server is full. Please try later. ", "Chat", JOptionPane.ERROR_MESSAGE, icon);
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
