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
	

	Files_backup files_backup;
	int space_use;
	File original_final;
	
	HashMap<Chunk, ArrayList<Header>> stored_messages;

	HashMap<String, List_of_chunks> chunks_backup;
	HashMap<String, List_of_chunks> chunks_save; 
	
	public DataBase() {
		files_backup = new Files_backup();
		original_final = new File("../res/" + "chunks_" + Peer.getPeer_id());
		space_use = 0;
		
		initialize_hashMap();
		
		
		boolean exist = original_final.exists();
		if (!exist)
			original_final.mkdirs();
		
	}
	
	private void initialize_hashMap() {
		stored_messages = new HashMap<Chunk, ArrayList<Header>>();
		chunks_backup = new HashMap<String, List_of_chunks>();
		chunks_save = new HashMap<String, List_of_chunks>();
		
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
		HashMap<Chunk, ArrayList<Header>> stores = Peer.getData().get_stored_messages();
		int replication = header.getReplicationDeg();
		Chunk chunk = new Chunk(header);
		if ((stores.get(chunk) != null) && (stores.get(chunk).size() >= replication))
				return true;
		return false;
	}

	public void save_chunk(Header header, byte[] body) throws IOException {
		String path = original_final.getPath();
		String file_id = header.get_file_id();
		
		File folder = new File(path + "/" + file_id + "/");
		
		boolean exist = folder.exists();
		if (!exist)
			folder.mkdirs();
		
		int chunk_number = header.get_chunk_number();
		FileOutputStream stream = new FileOutputStream(folder.getPath() + "/" + chunk_number + ".data");
	
		if (body != null)
			stream.write(body);
		
		stream.close();
		List_of_chunks chunks = null;
		if(chunks_save.get(header.get_file_id()) != null)
			chunks = chunks_save.get(header.get_file_id());
		else
			new List_of_chunks();
			
		Chunk chunk = new Chunk(header);
		
		if (body != null) {
			chunk.set_chunk_size((int)body.length);
			int space_tmp = space_use;
			space_use = space_tmp + body.length;
		}
		chunks.add_chunk(chunk);
		chunks_save.put(header.get_file_id(), chunks);
	}


	public boolean check_stored(String fileId, int chunk_number) {
		
		List_of_chunks list_chunks = null;
		if(chunks_save.get(fileId) != null)
			list_chunks = chunks_save.get(fileId);
		
		if (list_chunks == null) {
			return false;
		}
		for (int i = 0; i < list_chunks.size(); i++)  {
			if (list_chunks.get(i).get_chunk_number() == chunk_number)
				return true;
		}
		System.out.println("Chunk is not stored");
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
		Path chunk = Paths.get(original_final.getPath() + "/" + fileId + "/" + chunkNo + ".data");
		return Files.readAllBytes(chunk);
	}	

	public void clear_store(Header header) {
		if (chunks_save.get(header.get_file_id()) != null) {
			chunks_save.remove(header.get_file_id());
		}
		
	}

	public int delete_chunk(Chunk chunk) {
		String path = original_final.getPath();
		String file_id = chunk.get_file_id();
		int chunk_number = chunk.get_chunk_number();
		
		File file = new File(path + "/" + file_id + "/" + chunk_number + ".data");
		int size = (int) file.length();
		boolean delete = file.delete();
		if(delete){
			List_of_chunks all_chunks = chunks_save.get(chunk.get_file_id());
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
		
		if((chunks_save.get(header.get_file_id()) == null)  || (!chunks_save.get(header.get_file_id()).contains(chunk))){
			System.out.println("chunk doesnt exist");
			return null;}

		int replication = headers.size() + 1;
		int replication_deg = -1;
		
		for (Chunk info : chunks_save.get(header.get_file_id())) {
			if (info == chunk) {
				replication_deg = info.get_replication_deg();
				if (replication < replication_deg)
					return info;
				else
					break;
			}
		}
		return null;
	}
}
