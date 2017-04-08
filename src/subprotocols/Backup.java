package subprotocols;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;

import peers.ChunksList;
import peers.FileInfo;
import peers.Peer;
import utilities.Chunk;
import utilities.Header;


/**
 * Created by Francisca on 28/03/17.
 */
public class Backup extends Thread {
    private File file_name;
    private int replication_deg;

    public Backup(String fileName, int replicationDeg) {
        this.file_name = new File("../res/"+fileName);
        this.replication_deg = replicationDeg;
    }

    public void run() {
    	try{
    		byte[] fileData = Files.readAllBytes(file_name.toPath());
    		sendAllChunks(fileData);
    	}catch(IOException e){
    		System.out.println("The file doesn't exist.");
    	}
    }

    private void sendAllChunks(byte[] fileData) throws IOException {
		//Create Root for file
    	FileInputStream inputStream = new FileInputStream("../res/" + file_name.getName());
    	
    	//Buf with the max chunk size
    	byte[] buf = new byte[64000];
    	String fileId = getFileId(file_name);
    	
    	String version = "1.0";
		Header header = new Header("PUTCHUNK", version, Peer.getPeerId(), fileId, 0, replication_deg);
		
		int bytesRead = 0;
		int numberOfChunks = 0;
		
		while ((bytesRead = inputStream.read(buf)) != -1) {
			byte[] chunk = Arrays.copyOfRange(buf, 0, bytesRead);
			Header.setChunkNo(numberOfChunks);
			sendChunk(header, chunk);
			numberOfChunks++;
		}
		
		if (Peer.getData().getBackedUpFiles().get(file_name.getName()) == null) {
			Peer.getData();
			Peer.getData().getBackedUpFiles().markAsBackedUp(file_name.getName(), new FileInfo(file_name.getName(), fileId, numberOfChunks, file_name.length()));
		}
		inputStream.close();
		System.out.println("File backup done!");
		
	}

    public static void sendChunk(Header header, byte[] chunk) {
    	
		int chunksSent = 0;
		
		int waitingTime = 500;
		//5 is the max chunk retry
		while (chunksSent < 5) {
			ChunkManage backupChunk = new ChunkManage(header, chunk);
			backupChunk.sendChunk();
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			backupChunk.checkReplies();
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
	
	//Get File Id
	public static String getFileId(File file) {
		return sha256(file.getName() + file.lastModified() + Peer.getPeerId());
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
