package peers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import utilities.Chunk;
import utilities.Header;

public class DataBase {
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
		chunks = new File("../res/" + "chunks_" + Peer.getPeerId());
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
	
	

}
