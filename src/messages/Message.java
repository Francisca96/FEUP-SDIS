package messages;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Francisca on 28/03/17.
 */
public class Message {
    private byte[] body;
    private Header header;
    private InetAddress addr;
    private MulticastSocket socket;

    public Message(Header header, byte[] body){
        this.header = header;
        this.body = body;
        this.socket = null;
        this.addr = null;
    }

    //To know the socket and adress of a channel
    public Message(MulticastSocket socket, InetAddress addr, Header header, byte[] body){
        this.header = header;
        this.body = body;
        this.socket = socket;
        this.addr = addr;
    }



}
