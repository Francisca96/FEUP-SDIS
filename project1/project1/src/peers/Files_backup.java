package peers;

import java.io.Serializable;
import java.util.HashMap;

public class Files_backup extends HashMap<String, FileManage> implements Serializable {

	private static final long serialVersionUID = -7257861820917361024L;

	public void file_mark(String fileName, FileManage fileInfo) {
		this.put(fileName, fileInfo);
	}
}