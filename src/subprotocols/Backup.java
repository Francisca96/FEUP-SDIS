package subprotocols;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

import peers.List_of_chunks;
import peers.FileManage;
import peers.Peer;
import utilities.Chunk;
import utilities.Header;
import utilities.Message;

public class Backup extends Thread {
    private File file;
    private int replicationDeg;
    static ArrayList<Header> valid_replies;

    public Backup(String fileName, int replicationDeg) {
        this.file = new File("../res/"+fileName);
        this.replicationDeg = replicationDeg;
    }

    public void run() {
    	try{
    		byte[] file_data = Files.readAllBytes(file.toPath());
    		
    		//Create Root for file
        	FileInputStream inputStream = new FileInputStream("../res/" + file.getName());
        	
        	//Buf with the max chunk size
        	int size = 64 * 1000;
        	byte[] buf = new byte[size];
        	String file_id = getFileId(file);
        	String peer_id = Peer.getPeer_id();
        	
        	String version = Peer.getProtocole_version();
    		Header header = new Header("PUTCHUNK", version, peer_id, file_id, 0, replicationDeg);
    		
    		int bytesRead = 0;
    		int chunk_number = 0;
    		
    		while ((bytesRead = inputStream.read(buf)) != -1) {
    			byte[] body = Arrays.copyOfRange(buf, 0, bytesRead);
    			Header.setChunkNo(chunk_number);
    			send_chunk(header, body);
    			chunk_number++;
    		}
    		
    		if (Peer.getData().get_file_backup().get(file.getName()) == null) {
    			Peer.getData().get_file_backup().file_mark(file.getName(), new FileManage(file.getName(),file_id, chunk_number, file.length()));
    		}
    		inputStream.close();
    		System.out.println("Backup finish");
    		
    	}catch(IOException e){
    		System.out.println("The file doesn't exist.");
    	}
    }


    public static void send_chunk(Header header, byte[] chunk) {
    	
		int chunksSent = 0;
		int waitingTime = 500;
		
		//5 is the max chunk retry
		while (chunksSent < 5) {
			
			//Create message
			Message message = new Message(Peer.getMdbChannel().getSocket(), Peer.getMdbChannel().getAddr(), header, chunk);
			valid_replies = new ArrayList<>();
			
			//Send
			new Thread(message).start();
			
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			check(message);
			
			List_of_chunks chunksList = null;
			if(Peer.getData().get_chunks_backup().get(header.get_file_id()) != null)
				chunksList = Peer.getData().get_chunks_backup().get(header.get_file_id());
			int confirmedBackUps = 0;
			Chunk thisChunkInfo = new Chunk(header, chunk.length);
			//Getting BackUps
			if (chunksList != null)
				for (Chunk chunkInfo : chunksList)
					if (chunkInfo.equals(thisChunkInfo))
						confirmedBackUps = chunkInfo.get_headers().size();
			//Checking if this Peer has the chunk stored
			Chunk chunkInfo = new Chunk(header, chunk.length);
			if (Peer.getData().get_chunks_save().get(header.get_file_id()) != null) 
				if (Peer.getData().get_chunks_save().get(header.get_file_id()).contains(chunkInfo))
					confirmedBackUps++;
			
			int repDeg = header.getReplicationDeg();
			if (confirmedBackUps < repDeg) {
				chunksSent++;
				waitingTime  *= 2;
				System.out.println("ReplicationDeg was not achieved (" + confirmedBackUps + ") ... Waiting more " + waitingTime + "ms.");
			} else {
				break;
			}
		}
		waitingTime = 5;
	}
	
	
	public static void check(Message message) {
		int replicationDeg = message.getHeader().getReplicationDeg();
		Message reply;
		ArrayList<Message> replies_stored = Peer.getMcChannel().getReplies_stored();
		int counter = 0;
		for (int i = 0; i < replies_stored.size(); i++) {
			
			reply = replies_stored.get(i);
			Header header = reply.getHeader();
			String message_type = header.getMessageType();
			String sender_id = header.getSenderId();
			String peer_id = Peer.getPeer_id();
			String header_file_id = header.get_file_id();
			String message_file_id = message.getHeader().get_file_id();
			int header_chunk_number = header.get_chunk_number();
			int message_chunk_number = message.getHeader().get_chunk_number();
			
			//Check if the reply is valid
			if ((message_type == "STORED") && (sender_id != peer_id) && (header_file_id == message_file_id) && (header_chunk_number == message_chunk_number)) {
				valid_replies.add(header);
				counter++;
			} 
			Peer.getMcChannel().getReplies_stored().remove(replies_stored.get(i));
		}
		
		//Checking if this peer has the chunk saved
		Chunk chunk = new Chunk(message.getHeader(), message.getBody().length);
		if (Peer.getData().get_chunks_save().get(message.getHeader().get_file_id()) != null) 
			if (Peer.getData().get_chunks_save().get(message.getHeader().get_file_id()).contains(chunk))
				counter++;
		
		if (counter >= replicationDeg) {
			complete_database(message);
		}
			
	}
	
	private static void complete_database(Message message) {
		List_of_chunks chunks = null;
		if(Peer.getData().get_chunks_backup().get(message.getHeader().get_file_id()) != null)
			chunks = Peer.getData().get_chunks_backup().get(message.getHeader().get_file_id());
		else
			new List_of_chunks();
		
		Chunk chunk = new Chunk(message.getHeader(), message.getBody().length);
		
		for (Chunk savedChunkInfo : chunks) {
			if (chunk == savedChunkInfo) {
				chunk = savedChunkInfo;
				break;
			}
		}
		chunk.add_header(valid_replies);
		boolean state = chunks.contains(chunk);
		if (!state)
			chunks.add(chunk);
		
		Peer.getData().get_chunks_backup().put(message.getHeader().get_file_id(), chunks);
	}
	
	//Get File Id
	public static String getFileId(File file) {
		return sha256(file.getName() + file.lastModified() + Peer.getPeer_id());
	}
	
	
	// From internet http://stackoverflow.com/questions/3103652/hash-string-via-sha-256-in-java
	public static String sha256(String base) {
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(base.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}
}
