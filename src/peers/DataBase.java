package peers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import channels.McChannel;
import subprotocols.Restore;
import utilities.Chunk;
import utilities.Header;

public class DataBase implements Serializable {
	HashMap<Chunk, ArrayList<Header>> receivedStoreMessages;

	HashMap<String, ChunksList> chunksBackedUp; //FileId as key, Array of ChuksList as value
	HashMap<String, ChunksList> chunksSaved; //FileId as key, Array of ChunkNo as value

	BackedUpFiles backedUpFiles; //HashMap containing which files are backed up, fileName as Keys
	int usedSpace;
	File chunks;
	
	public DataBase() {
		receivedStoreMessages = new HashMap<Chunk, ArrayList<Header>>();
		chunksBackedUp = new HashMap<String, ChunksList>();
		chunksSaved = new HashMap<String, ChunksList>();
		backedUpFiles = new BackedUpFiles();
		chunks = new File("../res/" + "chunks_" + Peer.getPeer_id());
		usedSpace = 0;
		createFolders();
	}

	private void createFolders() {
		if (!chunks.exists())
			chunks.mkdirs();
	}

	public HashMap<Chunk, ArrayList<Header>> getReceivedStoreMessages() {
		return receivedStoreMessages;
	}

	public void setReceivedStoreMessages(HashMap<Chunk, ArrayList<Header>> receivedStoreMessages) {
		this.receivedStoreMessages = receivedStoreMessages;
	}

	public HashMap<String, ChunksList> getChunksBackedUp() {
		return chunksBackedUp;
	}

	public void setChunksBackedUp(HashMap<String, ChunksList> chunksBackedUp) {
		this.chunksBackedUp = chunksBackedUp;
	}

	public HashMap<String, ChunksList> getChunksSaved() {
		return chunksSaved;
	}

	public void setChunksSaved(HashMap<String, ChunksList> chunksSaved) {
		this.chunksSaved = chunksSaved;
	}

	public BackedUpFiles getBackedUpFiles() {
		return backedUpFiles;
	}

	public void setBackedUpFiles(BackedUpFiles backedUpFiles) {
		this.backedUpFiles = backedUpFiles;
	}

	public int getUsedSpace() {
		return usedSpace;
	}

	public void setUsedSpace(int usedSpace) {
		this.usedSpace = usedSpace;
	}

	public File getChunks() {
		return chunks;
	}

	public void setChunks(File chunks) {
		this.chunks = chunks;
	}
	
	public static boolean repDegAchieved(Header header) {
		Chunk chunkInfo = new Chunk(header);
		HashMap<Chunk, ArrayList<Header>> stores = Peer.getData().getReceivedStoreMessages();
		int repDeg = header.getReplicationDeg();
		if (stores.get(chunkInfo) != null)
			if (stores.get(chunkInfo).size() >= repDeg)
				return true;
		return false;
	}

	public void saveChunk(Header header, byte[] bodyByteArray) throws IOException {
		File chunkFolder = new File(chunks.getPath() + "/" + header.getFileId() + "/");
		if (!chunkFolder.exists())
			chunkFolder.mkdirs();
		FileOutputStream stream = new FileOutputStream(chunkFolder.getPath() + "/" + header.getChunkNo() + ".data");
	
		try {
			if (bodyByteArray != null)
				stream.write(bodyByteArray);
		} finally {
			stream.close();
			ChunksList chunks = chunksSaved.get(header.getFileId()) != null ? chunksSaved.get(header.getFileId()) : new ChunksList();
			
			Chunk chunk = new Chunk(header);
			if (bodyByteArray != null) {
				chunk.setChunkSize((int)bodyByteArray.length);
				usedSpace += bodyByteArray.length;
			}
			chunks.addChunk(chunk);
			chunksSaved.put(header.getFileId(), chunks);
		}
		
	}

	public boolean chunkIsStored(String fileId, int chunkNo) {
		ChunksList chunksList = chunksSaved.get(fileId) != null ? chunksSaved.get(fileId) : null;
		if (chunksList == null) {
			System.out.println("chunksList not found");
			return false;
		}
		for (int i = 0; i < chunksList.size(); i++)  {
			if (chunksList.get(i).getChunkNo() == chunkNo)
				return true;
		}
		System.out.println("ChunkNo not found");
		return false;
	}

	public void addToReceivedStoreMessages(Header header) {
		Chunk chunk = new Chunk(header);
		ArrayList<Header> headers = receivedStoreMessages.get(chunk) != null ? receivedStoreMessages.get(chunk) : new ArrayList<Header>();
		if(!headers.contains(header)) {
			headers.add(header);
			receivedStoreMessages.put(chunk, headers);
		}
		
	}

	public byte[] getChunkBody(String fileId, int chunkNo) throws IOException {
		Path restorableChunk = Paths.get(chunks.getPath() + "/" + fileId + "/" + chunkNo + ".data");
		return Files.readAllBytes(restorableChunk);
	}	

	public void clearStoredChunks(Header header) {
		if (chunksSaved.get(header.getFileId()) != null) {
			chunksSaved.remove(header.getFileId());
		}
		
	}

	public void saveRestoredChunk(String file_name, byte[] bodyByteArray) throws IOException {
		FileInfo fileInfo = backedUpFiles.get(file_name);
		Restore.getNew_output().write(bodyByteArray);
		int size = 64*1000;
		if (bodyByteArray.length < size) {
			Peer.getMdrChannel().setWaitingChunks(false);
			Restore.getNew_output().close();
			System.out.println("File was restored!");
			if (Restore.get_number_of_chunks() != fileInfo.getNumberOfChunks()){
					System.out.println("The number of received chunks doesn't match the number of chunks in this file");
				}
			Restore.loadDefaults();
		} else {
			Restore.inc_number_of_chunks();
			Restore.sendChunk();
		}
		
	}

	public int deleteChunk(Chunk chunk) {
		File file = new File(chunks.getPath() + "/" + chunk.getFileId() + "/" + chunk.getChunkNo() + ".data");
		int size = (int) file.length();
		if(!file.delete()){
			System.out.println("Could not delete chunk.");
			return 0;
		}
		deleteFromChunksSaved(chunk);
		McChannel.sendRemoved(chunk);
		usedSpace -= size;
		return size;
		
	}

	private void deleteFromChunksSaved(Chunk chunk) {
		ChunksList chunks = chunksSaved.get(chunk.getFileId());
		chunks.remove(chunk);
	}

	public Chunk removeFromReceivedStoreMessages(Header header) {
		header.setMessageType("STORED");
		Chunk chunkInfo = new Chunk(header);
		ArrayList<Header> headers = receivedStoreMessages.get(chunkInfo) != null ? receivedStoreMessages.get(chunkInfo) : new ArrayList<Header>();
		if(headers.contains(header)) {
			headers.remove(header);
		} 
		boolean iHaveIt = chunksSaved.get(header.getFileId()) != null  && chunksSaved.get(header.getFileId()).contains(chunkInfo) ? true : false;
		if (!iHaveIt) {
			System.out.println("I dont have this chunk");
			return null;
		}
		int replication = headers.size() + 1;
		int replicationDeg = -1;
		for (Chunk info : chunksSaved.get(header.getFileId())) {
			if (info.equals(chunkInfo)) {
				replicationDeg = info.getReplicationDeg();
				if (replication < replicationDeg)
					return info;
				else
					break;
			}
		}
		return null;
	}
	
	

}
