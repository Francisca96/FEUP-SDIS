package app;

/**
 * @author vascoribeiro
 */

import javax.swing.*;

public class Start {

	
	public static void main(String [] args){
		
		Object[] values = { "Servidor","Cliente"};

		Object selecao = JOptionPane.showInputDialog(null, "Login as : ", "Chat", JOptionPane.QUESTION_MESSAGE, null, values, "Servidor");
		if(selecao.equals("Servidor")){
                   String[] arguments = new String[] {};
			new MultiThreadChatServerSync().main(arguments);
		}else if(selecao.equals("Cliente")){
			String IPServidor = JOptionPane.showInputDialog("Indique o ip do servidor");
                        String[] argumentos = new String[] {IPServidor};
			new Chat().main(argumentos);
		}
		
	}

}
