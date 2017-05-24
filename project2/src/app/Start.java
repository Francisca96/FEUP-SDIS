package app;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class Start {

    public static void main(String[] args) {

        Object[] values = {"Server1", "Server2", "User"};
        Object[] rooms = {"Room1", "Room2"};

        UIManager UI = new UIManager();
        UI.put("OptionPane.background", new ColorUIResource(108, 183, 242));
        UI.put("Panel.background", new ColorUIResource(108, 183, 242));
        Icon icon = new ImageIcon("/Users/Francisca/Desktop/MIEIC/3Ano/2semestre/SDIS/FEUP-SDIS/project2/src/res/communication.png");

        Object selecao = JOptionPane.showInputDialog(null, "Login as: ", "Chat", JOptionPane.QUESTION_MESSAGE, icon, values, "Server1");
        if (selecao.equals("Server1")) {
            String[] arguments = new String[]{"2222"};
            new MultiThreadChatServerSync().main(arguments);
        } else if (selecao.equals("Server2")) {
            String[] arguments = new String[]{"2223"};
            new MultiThreadChatServerSync().main(arguments);
        } else if (selecao.equals("User")) {
            Object room = JOptionPane.showInputDialog(null, "Choose your ChatRoom: ", "Chat", JOptionPane.QUESTION_MESSAGE, icon, rooms, "Room1");
            String chosenRoom = String.valueOf(room);
            String[] argumentos = new String[]{"localhost", chosenRoom};
            new Chat().main(argumentos);
        }

    }
}
