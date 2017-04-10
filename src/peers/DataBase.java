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
import utilities.Chunk;
import utilities.Header;

public class DataBase implements Serializable {
	HashMap<Chunk, ArrayList<Header>> stored_messages;

	HashMap<String, List_of_chunks> chunks_backup; //FileId as key, Array of ChuksList as value
	HashMap<String, List_of_chunks> chunks_save; //FileId as key, Array of ChunkNo as value

	Files_backup files_backup; //HashMap containing which files are backed up, fileName as Keys
	int space_use;
	File chunks;
	
	public DataBase() {
		stored_messages = new HashMap<Chunk, ArrayList<Header>>();
		chunks_backup = new HashMap<String, List_of_chunks>();
		chunks_save = new HashMap<String, List_of_chunks>();
		files_backup = new Files_backup();
		chunks = new File("../res/" + "chunks_" + Peer.getPeer_id());
		space_use = 0;
		
		if (!chunks.exists())
			chunks.mkdirs();
		
	}
	
	//Getters
	public HashMap<Chunk, ArrayList<Header>> get_stored_messages() {
		return stored_messages;
	}

	public HashMap<String, List_of_chunks> get_chunks_backup() {
		return chunks_backup;
	}

	public HashMap<String, List_of_chunks> get_chunks_save() {
		return chunks_save;
	}

	public Files_backup get_file_backup() {
		return files_backup;
	}

	public int get_space_use() {
		return space_use;
	}
	
	public static boolean replication_complete(Header header) {
		Chunk chunk = new Chunk(header);
		HashMap<Chunk, ArrayList<Header>> stores = Peer.getData().get_stored_messages();
		int replication = header.getReplicationDeg();
		if ((stores.get(chunk) != null) && (stores.get(chunk).size() >= replication))
				return true;
		return false;
	}

	public void save_chunk(Header header, byte[] body) throws IOException {
		File folder = new File(chunks.getPath() + "/" + header.getFileId() + "/");
		
		if (!folder.exists())
			folder.mkdirs();
		
		int chunk_number = header.getChunkNo();
		FileOutputStream stream = new FileOutputStream(folder.getPath() + "/" + chunk_number + ".data");
	
		if (body != null)
			stream.write(body);
		
		stream.close();
		List_of_chunks chunks = null;
		if(chunks_save.get(header.getFileId()) != null)
			chunks = chunks_save.get(header.getFileId());
		else
			new List_of_chunks();
			
		Chunk chunk = new Chunk(header);
		
		if (body != null) {
			chunk.setChunkSize((int)body.length);
			int space_tmp = space_use;
			space_use = space_tmp + body.length;
		}
		chunks.addChunk(chunk);
		chunks_save.put(header.getFileId(), chunks);
	}


	public boolean check_stored(String fileId, int chunk_number) {
		
		List_of_chunks list_chunks = null;
		if(chunks_save.get(fileId) != null)
			list_chunks = chunks_save.get(fileId);
		
		if (list_chunks == null) {
			return false;
		}
		for (int i = 0; i < list_chunks.size(); i++)  {
			if (list_chunks.get(i).getChunkNo() == chunk_number)
				return true;
		}
		System.out.println("Chunk not stored");
		return false;
	}

	public void add_receive_message(Header header) {
		Chunk chunk = new Chunk(header);
		ArrayList<Header> headers = null;
		if(stored_messages.get(chunk) != null)
			headers = stored_messages.get(chunk);
		else
			headers = new ArrayList<Header>();
		
		if(!headers.contains(header)) {
			headers.add(header);
			stored_messages.put(chunk, headers);
		}
	}

	public byte[] get_chunk_body(String fileId, int chunkNo) throws IOException {
		Path chunk = Paths.get(chunks.getPath() + "/" + fileId + "/" + chunkNo + ".data");
		return Files.readAllBytes(chunk);
	}	

	public void clear_store(Header header) {
		if (chunks_save.get(header.getFileId()) != null) {
			chunks_save.remove(header.getFileId());
		}
		
	}

	public int delete_chunk(Chunk chunk) {
		File file = new File(chunks.getPath() + "/" + chunk.getFileId() + "/" + chunk.getChunkNo() + ".data");
		int size = (int) file.length();
		if(file.delete()){
			List_of_chunks all_chunks = chunks_save.get(chunk.getFileId());
			all_chunks.remove(chunk);
			McChannel.sendRemoved(chunk);
			int space_tmp = space_use;
			space_use = space_tmp - size;
			return size;
		}
		System.out.println("Cant delete chunk.");
		return 0;
		
	}

	public Chunk delete_store_message(Header header) {
		
		header.setMessageType("STORED");
		Chunk chunk = new Chunk(header);

		ArrayList<Header> headers = null;
		if(stored_messages.get(chunk) != null)
			headers = stored_messages.get(chunk);
		else
			new ArrayList<Header>();
		
		if(headers.contains(header)) {
			headers.remove(header);
		}
		
		if((chunks_save.get(header.getFileId()) == null)  || (!chunks_save.get(header.getFileId()).contains(chunk))){
			System.out.println("I don't have this chunk");
			return null;}

		int replication = headers.size() + 1;
		int replication_deg = -1;
		
		for (Chunk info : chunks_save.get(header.getFileId())) {
			if (info == chunk) {
				replication_deg = info.getReplicationDeg();
				if (replication < replication_deg)
					return info;
				else
					break;
			}
		}
		return null;
	}
}
