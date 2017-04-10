package utilities;

import java.io.Serializable;
import java.util.ArrayList;

public class Chunk implements Serializable {

	private static final long serialVersionUID = 1L;
	String file_id;
	int chunk_number;
	ArrayList<Header> headers;
	int chunk_size;
	int replication_deg;
	

	public Chunk(Header header, int chunkSize) {
		this.chunk_number = header.get_chunk_number();
		this.file_id = header.get_file_id();
		this.headers = new ArrayList<Header>();
		this.replication_deg = header.getReplicationDeg() != 0 ? header.getReplicationDeg() : -1;
		this.chunk_size = chunkSize;
		
	}
	public Chunk(Header header) {
		this.chunk_number = header.get_chunk_number();
		this.file_id = header.get_file_id();
		this.headers = new ArrayList<Header>();
		this.replication_deg = header.getReplicationDeg() != 0 ? header.getReplicationDeg() : -1;
		this.chunk_size = -1;
	}
	public void add_header(ArrayList<Header> valid_reply) {
		for (Header header : valid_reply){
			boolean state = headers.contains(header);
			if (state)
				headers.add(header);
		}	
	}
	public String get_file_id() {
		return file_id;
	}

	public int get_replication_deg() {
		return replication_deg;
	}

	public ArrayList<Header> get_headers() {
		return headers;
	}
	public void set_chunk_size(int length) {
		chunk_size = length;
	}
	public int get_chunk_number() {
		return chunk_number;
	}
}