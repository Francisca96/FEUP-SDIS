package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class Channel{

    MulticastSocket socket;
    InetAddress address;
    private int port_number;

    Channel(String address, String port_number) throws IOException {
        this.address = InetAddress.getByName(address);
        this.port_numer = Integer.parseInt(port_number);
        this.socket = new MulticastSocket(this.port_number);
        //this.socket.setTimeToLive(1);
    }

}