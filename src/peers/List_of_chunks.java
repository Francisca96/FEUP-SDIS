package peers;

import java.io.Serializable;
import java.util.ArrayList;

import utilities.Chunk;

public class List_of_chunks extends ArrayList<Chunk> implements Serializable {

	private static final long serialVersionUID = 1L;

	public void add_chunk(Chunk chunk) {
		for (int i = 0; i < this.size(); i++) {
			if (chunk.get_chunk_number() == this.get(i).get_chunk_number())
				return;
		}
		this.add(chunk);
	}
}