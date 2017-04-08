package peers;

import java.io.Serializable;
import java.util.HashMap;

public class BackedUpFiles extends HashMap<String, FileInfo> implements Serializable {

	private static final long serialVersionUID = -7257861820917361024L;

	public void markAsBackedUp(String fileName, FileInfo fileInfo) {
		this.put(fileName, fileInfo);
	}
}