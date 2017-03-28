package messages;

/**
 * Created by Francisca on 28/03/17.
 */
public class Header {
    private static String messageType;
    private static String version;
    private static int senderId;
    private static String fileId;
    private static int chunkNo;
    private static int replicationDeg;

    public Header (String messageType, String version, int senderId, String fileId, int chunkNo, int replicationDeg){
        this.messageType = messageType;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
    }
}
