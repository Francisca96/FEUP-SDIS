package utilities;

/**
 * Created by Francisca on 28/03/17.
 */
public class Header {
    private static String messageType;
    private static String version;
    private static String senderId;
    private static String fileId;
    private static int chunkNo;
    private static int replicationDeg;

    public Header (String messageType, String version, String senderId, String fileId, int chunkNo, int replicationDeg){
        this.messageType = messageType;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
    }

    //GETTERS AND SETTERS
    public static String getMessageType() {
        return messageType;
    }

    public static void setMessageType(String messageType) {
        Header.messageType = messageType;
    }

    public static String getVersion() {
        return version;
    }

    public static void setVersion(String version) {
        Header.version = version;
    }

    public static String getSenderId() {
        return senderId;
    }

    public static void setSenderId(String senderId) {
        Header.senderId = senderId;
    }

    public static String getFileId() {
        return fileId;
    }

    public static void setFileId(String fileId) {
        Header.fileId = fileId;
    }

    public static int getChunkNo() {
        return chunkNo;
    }

    public static void setChunkNo(int chunkNo) {
        Header.chunkNo = chunkNo;
    }

    public static int getReplicationDeg() {
        return replicationDeg;
    }

    public static void setReplicationDeg(int replicationDeg) {
        Header.replicationDeg = replicationDeg;
    }

}
