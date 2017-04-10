package peers;

import java.io.Serializable;

public class FileManage implements Serializable {

	private static final long serialVersionUID = 1L;
	private String file_name;
	private String file_id;
	private int chunks_number;
	private int file_size;
	
	public FileManage(String fileName, String fileId, int numberOfChunks, long size) {
		this.file_name = fileName;
		this.file_id = fileId;
		this.chunks_number = numberOfChunks;
		this.file_size = (int) size;
	}
	
	public String get_file_id() {
		return file_id;
	}
	public int get_chunks_number() {
		return chunks_number;
	}
}