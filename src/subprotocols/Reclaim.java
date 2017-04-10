package subprotocols;

import java.util.ArrayList;
import java.util.HashMap;

import peers.ChunksList;
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
		if (reclaim_space > Peer.getData().getUsedSpace()) {
			System.out.println("Will delete all chunks.");
			reclaim_space = Peer.getData().getUsedSpace();
		}
		
		ChunksList allChunks = new ChunksList();
		HashMap<String, ChunksList> tmp = Peer.getData().getChunksSaved();
		for (ChunksList chunks : tmp.values()) {
		    allChunks.addAll(chunks);
		}
		
		//Missing create chunks to delete!
		ArrayList<Chunk> chunksToDelete = null;
		
		while (chunksToDelete.size() > 0) {
			Peer.getData().deleteChunk(chunksToDelete.get(0));
			chunksToDelete.remove(0);
		}
	}
	
}
