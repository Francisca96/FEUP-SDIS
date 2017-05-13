package subprotocols;

import java.util.ArrayList;
import java.util.HashMap;

import peers.List_of_chunks;
import peers.Peer;
import utilities.Chunk;

public class Reclaim extends Thread{
	
	//space to be reclaim
	int reclaim_space;
	
	public Reclaim(int reclaim_space){
		this.reclaim_space = reclaim_space;
	}
	
	public void run() {
		
		if (reclaim_space > Peer.getData().get_space_use()) {
			reclaim_space = Peer.getData().get_space_use();
		}
		
		List_of_chunks all_chunks = new List_of_chunks();
		HashMap<String, List_of_chunks> tmp = Peer.getData().get_chunks_save();
		for (List_of_chunks chunks : tmp.values()) {
		    all_chunks.addAll(chunks);
		}
		
		//Missing create chunks to delete!
		ArrayList<Chunk> delete_chunks = null;
		
		while (delete_chunks.size() > 0) {
			Peer.getData().delete_chunk(delete_chunks.get(0));
			delete_chunks.remove(0);
		}
	}
	
}
