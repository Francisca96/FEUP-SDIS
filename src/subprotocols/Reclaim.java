package subprotocols;

import java.util.ArrayList;
import java.util.HashMap;

import peers.List_of_chunks;
import peers.Peer;
import utilities.Chunk;

public class Reclaim {
	
	//space to be reclaim
	int reclaim_space;
	
	Reclaim(int reclaim_space){
		this.reclaim_space = reclaim_space;
	}
	
	public void run() {
		//View if the peer is alone or if peer has lower space to use
		if (reclaim_space > Peer.getData().get_space_use()) {
			System.out.println("Will delete all chunks.");
			reclaim_space = Peer.getData().get_space_use();
		}
		
		List_of_chunks allChunks = new List_of_chunks();
		HashMap<String, List_of_chunks> tmp = Peer.getData().get_chunks_save();
		for (List_of_chunks chunks : tmp.values()) {
		    allChunks.addAll(chunks);
		}
		
		//Missing create chunks to delete!
		ArrayList<Chunk> chunksToDelete = null;
		
		while (chunksToDelete.size() > 0) {
			Peer.getData().delete_chunk(chunksToDelete.get(0));
			chunksToDelete.remove(0);
		}
	}
	
}
