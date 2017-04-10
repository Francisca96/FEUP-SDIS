package peers;

import java.io.Serializable;
import java.util.ArrayList;

import utilities.Chunk;

public class List_of_chunks extends ArrayList<Chunk> implements Serializable {

	private static final long serialVersionUID = 1L;

	public void addChunk(Chunk chunk) {
		for (int i = 0; i < this.size(); i++) {
			if (chunk.getChunkNo() == this.get(i).getChunkNo())
				return;
		}
		this.add(chunk);
	}
}