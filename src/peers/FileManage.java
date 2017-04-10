package peers;

import java.io.Serializable;

public class FileManage implements Serializable {

	private static final long serialVersionUID = 1L;
	private String fileName;
	private String fileId;
	private int numberOfChunks;
	private int fileSize;
	
	public FileManage(String fileName, String fileId, int numberOfChunks, long size) {
		this.fileName = fileName;
		this.fileId = fileId;
		this.numberOfChunks = numberOfChunks;
		this.fileSize = (int) size;
	}
	
	public String getFileId() {
		return fileId;
	}
	public int getNumberOfChunks() {
		return numberOfChunks;
	}
}