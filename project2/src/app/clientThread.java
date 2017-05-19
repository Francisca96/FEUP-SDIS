/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package app;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 * @author vascoribeiro
 */



public class clientThread extends Thread{

  private String clientName = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] clientes;
  private int max;

  public clientThread(Socket clientSocket, clientThread[] clientes) {
    this.clientSocket = clientSocket;
    this.clientes = clientes;
    max = clientes.length;
  }

  public void run() {
    int max = this.max;
    clientThread[] threads = this.clientes;

    try {

      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      String name;
      while (true) {
        os.println("Introduza o seu nome!");
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
          break;
        } else {
          os.println("O nome nao pode conter o caracter '@'.");
        }
      }

      /* Novo Cliente */
      os.println("Bem Vindo " + name
          + " ao nosso chat.\nPara sair insira /sair numa linha nova.");
      synchronized (this) {
        for (int i = 0; i < max; i++) {
          if (threads[i] != null && threads[i] == this) {
            clientName = "@" + name;
            break;
          }
        }
        for (int i = 0; i < max; i++) {
          if (threads[i] != null && threads[i] != this) {
            threads[i].os.println("*** Novo utilizador " + name
                + " entrou na sala !!! ***");
          }
        }
      }
      /* Inicio da conversa */
      while (true) {
        String line = is.readLine();
        if (line.startsWith("/sair")) {
          break;
        }

        /* Mensagem privada */
        if (line.startsWith("@")) {
          String[] words = line.split("\\s", 2);
          if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
              synchronized (this) {
                for (int i = 0; i < max; i++) {
                  if (threads[i] != null && threads[i] != this
                      && threads[i].clientName != null
                      && threads[i].clientName.equals(words[0])) {
                    threads[i].os.println("<" + name + "> " + words[1]);
                    /*
                     * Confirmação da mensagem
                     */
                    this.os.println(">" + name + "> " + words[1]);
                    break;
                  }
                }
              }
            }
          }
        } else {
          /* Mensagem publica */
          synchronized (this) {
            for (int i = 0; i < max; i++) {
              if (threads[i] != null && threads[i].clientName != null) {
                threads[i].os.println("<" + name + "> " + line);
              }
            }
          }
        }
      }
      synchronized (this) {
        for (int i = 0; i < max; i++) {
          if (threads[i] != null && threads[i] != this
              && threads[i].clientName != null) {
            threads[i].os.println("*** O utilizador " + name
                + " deixou o chat !!! ***");
          }
        }
      }
      os.println("*** Adeus " + name + " ***");

      /*
       * Limpar cliente para ganhar novo lugar
       */
      synchronized (this) {
        for (int i = 0; i < max; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }

      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
}
