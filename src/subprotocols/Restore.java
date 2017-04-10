package subprotocols;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import peers.FileInfo;
import peers.Peer;
import utilities.Header;
import utilities.Message;

public class Restore extends Thread{

	private static String file_name;
	private static FileOutputStream new_output;
	private static byte[] file;
	private static Header header;
	private static int number_of_chunks = 0;
	private static Message message;

	public Restore(String file_name) throws FileNotFoundException{
		this.file_name = file_name;
		
		//create output
		new_output = new FileOutputStream("../res/" + "restore_" + file_name);
		
		//Initiator for file
		file = new byte[0];
	}
	
	public void run(){
		if (!Peer.getData().getBackedUpFiles().containsKey(file_name)) {
			System.out.println("File not backed up");
			return;
		}
		
		FileInfo file = Peer.getData().getBackedUpFiles().get(file_name);
		
		String peer_id = Peer.getPeer_id();
		String file_id = file.getFileId();
		header = new Header("GETCHUNK", "1.0", peer_id, file_id, 0, 0);
		
		Peer.getMdrChannel().setWaitingChunks(true);
		sendChunk();
	}

	public static String getFile_name() {
		return file_name;
	}

	public static void setFile_name(String file_name) {
		Restore.file_name = file_name;
	}

	public static int get_number_of_chunks(){ 
		return number_of_chunks;
	}
	
	public static void loadDefaults() {
		file = new byte[0];
		number_of_chunks = 0;
	}

	public static FileOutputStream getNew_output() {
		return new_output;
	}

	public static void inc_number_of_chunks() {
		number_of_chunks++;
	}

	public static void sendChunk() {
		header.setChunkNo(number_of_chunks);
		message = new Message(Peer.getMcChannel().getSocket(), Peer.getMcChannel().getAddr(), header, null);
		new Thread(message).start();
		
	}
	
	


}
