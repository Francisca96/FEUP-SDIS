package subprotocols;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

import peers.ChunksList;
import peers.FileInfo;
import peers.Peer;
import utilities.Chunk;
import utilities.Header;
import utilities.Message;

public class Backup extends Thread {
    private File file;
    private int replicationDeg;
    static ArrayList<Header> validReplies;

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
        	
        	String version = "1.0";
    		Header header = new Header("PUTCHUNK", version, peer_id, file_id, 0, replicationDeg);
    		
    		int bytesRead = 0;
    		int chunk_number = 0;
    		
    		while ((bytesRead = inputStream.read(buf)) != -1) {
    			byte[] body = Arrays.copyOfRange(buf, 0, bytesRead);
    			Header.setChunkNo(chunk_number);
    			sendChunk(header, body);
    			chunk_number++;
    		}
    		
    		if (Peer.getData().getBackedUpFiles().get(file.getName()) == null) {
    			Peer.getData().getBackedUpFiles().markAsBackedUp(file.getName(), new FileInfo(file.getName(),file_id, chunk_number, file.length()));
    		}
    		inputStream.close();
    		System.out.println("Backup finish");
    		
    	}catch(IOException e){
    		System.out.println("The file doesn't exist.");
    	}
    }


    public static void sendChunk(Header header, byte[] chunk) {
    	
		int chunksSent = 0;
		int waitingTime = 500;
		
		//5 is the max chunk retry
		while (chunksSent < 5) {
			
			//Create message
			Message message = new Message(Peer.getMdbChannel().getSocket(), Peer.getMdbChannel().getAddr(), header, chunk);
			validReplies = new ArrayList<>();
			
			//Send
			new Thread(message).start();
			
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			checkReplies(message);
			ChunksList chunksList = Peer.getData().getChunksBackedUp().get(header.getFileId()) != null ? Peer.getData().getChunksBackedUp().get(header.getFileId()) : null;
			int confirmedBackUps = 0;
			Chunk thisChunkInfo = new Chunk(header, chunk.length);
			//Getting BackUps
			if (chunksList != null)
				for (Chunk chunkInfo : chunksList)
					if (chunkInfo.equals(thisChunkInfo))
						confirmedBackUps = chunkInfo.getStoredHeaders().size();
			//Checking if this Peer has the chunk stored
			Chunk chunkInfo = new Chunk(header, chunk.length);
			if (Peer.getData().getChunksSaved().get(header.getFileId()) != null) 
				if (Peer.getData().getChunksSaved().get(header.getFileId()).contains(chunkInfo))
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
	
	
	public static void checkReplies(Message message) {
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
			String header_file_id = header.getFileId();
			String message_file_id = message.getHeader().getFileId();
			int header_chunk_number = header.getChunkNo();
			int message_chunk_number = message.getHeader().getChunkNo();
			
			//Check if the reply is valid
			if ((message_type == "STORED") && (sender_id != peer_id) && (header_file_id == message_file_id) && (header_chunk_number == message_chunk_number)) {
				validReplies.add(header);
				counter++;
			} 
			Peer.getMcChannel().getReplies_stored().remove(replies_stored.get(i));
		}
		
		//Checking if this peer has the chunk saved
		Chunk chunk = new Chunk(message.getHeader(), message.getBody().length);
		if (Peer.getData().getChunksSaved().get(message.getHeader().getFileId()) != null) 
			if (Peer.getData().getChunksSaved().get(message.getHeader().getFileId()).contains(chunk))
				counter++;
		
		if (counter >= replicationDeg) {
			System.out.println("RepDeg complete");
			fillBase(message);
		}
			
	}
	
	private static void fillBase(Message message) {
		ChunksList chunks = null;
		if(Peer.getData().getChunksBackedUp().get(message.getHeader().getFileId()) != null)
			chunks = Peer.getData().getChunksBackedUp().get(message.getHeader().getFileId());
		else
			new ChunksList();
		
		Chunk chunk = new Chunk(message.getHeader(), message.getBody().length);
		
		for (Chunk savedChunkInfo : chunks) {
			if (chunk == savedChunkInfo) {
				chunk = savedChunkInfo;
				break;
			}
		}
		chunk.addToStoredHeaders(validReplies);
		if (!chunks.contains(chunk))
			chunks.add(chunk);
		
		Peer.getData().getChunksBackedUp().put(message.getHeader().getFileId(), chunks);
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
