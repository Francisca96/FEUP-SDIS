package utilities;

import java.io.Serializable;
import java.util.ArrayList;

public class Chunk implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String fileId;
	int chunkNo;
	int chunkSize;
	int replicationDeg;
	ArrayList<Header> storedHeaders;

	public Chunk(Header header, int chunkSize) {
		this.fileId = header.getFileId();
		this.chunkNo = header.getChunkNo();
		this.replicationDeg = header.getReplicationDeg() != 0 ? header.getReplicationDeg() : -1;
		this.chunkSize = chunkSize;
		this.storedHeaders = new ArrayList<Header>();
	}
	public Chunk(Header header) {
		this.fileId = header.getFileId();
		this.chunkNo = header.getChunkNo();
		this.replicationDeg = header.getReplicationDeg() != 0 ? header.getReplicationDeg() : -1;
		this.chunkSize = -1;
		this.storedHeaders = new ArrayList<Header>();
	}
	public void addToStoredHeaders(ArrayList<Header> validReplies) {
		for (Header header : validReplies)
			if (!storedHeaders.contains(header))
				storedHeaders.add(header);
	}
	public String getFileId() {
		return fileId;
	}
	public int getChunkSize() {
		return chunkSize;
	}
	public int getReplicationDeg() {
		return replicationDeg;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chunkNo;
		result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chunk other = (Chunk) obj;
		if (chunkNo != other.chunkNo)
			return false;
		if (fileId == null) {
			if (other.fileId != null)
				return false;
		} else if (!fileId.equals(other.fileId))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "ChunkInfo [fileId=" + fileId + ", chunkNo=" + chunkNo + ", chunkSize=" + chunkSize + "]";
	}
	public ArrayList<Header> getStoredHeaders() {
		return storedHeaders;
	}
	public void setChunkSize(int length) {
		chunkSize = length;
	}
	public int getChunkNo() {
		return chunkNo;
	}
}