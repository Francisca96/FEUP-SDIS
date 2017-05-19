package app;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author vascoribeiro
 */


public class MultiThreadChatServerSync {

  private static ServerSocket serverSocket = null;

  private static Socket clientSocket = null;

  // Maximo clientes variavel
  private static final int max = 10;
  private static final clientThread[] clientes = new clientThread[max];

  public static void main(String args[]) {

    // Numero da porta pode ser variavel
    int portNumber = 2222;
    if (args.length < 1) {
      System.out.println("Usage: java MultiThreadChatServerSync <portNumber>\n"
          + "Usando porta=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    /*
     * Abrir servidor na porta 2222
     */
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Criar socket para cada cliente
     */
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < max; i++) {
          if (clientes[i] == null) {
            (clientes[i] = new clientThread(clientSocket, clientes)).start();
            break;
          }
        }
        if (i == max) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Servidor cheio, tenta mais tarde.");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }  
}
