package communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPClient {

	private DatagramSocket sockUDP;
	private byte[] buffer;
	
	public UDPClient(int port) throws SocketException {
		this.sockUDP = new DatagramSocket(port);
		this.buffer = new byte[256];
	}
	
	
	protected void sendMessageUDP(String message, int port, InetAddress clientAddress) throws IOException {
		DatagramPacket outpacket = new DatagramPacket(message.getBytes(), message.length(), clientAddress, port);
		this.sockUDP.send(outpacket);
	}
}
