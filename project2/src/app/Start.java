package app;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class Start {

    public static void main(String[] args) {

        Object[] values = {"Server", "User"};

        UIManager UI = new UIManager();
        UI.put("OptionPane.background", new ColorUIResource(108, 183, 242));
        UI.put("Panel.background", new ColorUIResource(108, 183, 242));
        Icon icon = new ImageIcon("/Users/Francisca/Desktop/MIEIC/3Ano/2semestre/SDIS/FEUP-SDIS/project2/src/res/communication.png");

        Object selecao = JOptionPane.showInputDialog(null, "Login as: ", "Chat", JOptionPane.QUESTION_MESSAGE, icon, values, "Server");
        if (selecao.equals("Server")) {
            String[] arguments = new String[]{};
            new MultiThreadChatServerSync().main(arguments);
        } else if (selecao.equals("User")) {
            /*String IPServidor = (String)JOptionPane.showInputDialog(null,
					"Indique o IP do servidor:",
					"Chat",
					JOptionPane.QUESTION_MESSAGE,
					icon,
					null,
					null);*/
            String[] argumentos = new String[]{"localhost"};
            new Chat().main(argumentos);
        }

    }

}
