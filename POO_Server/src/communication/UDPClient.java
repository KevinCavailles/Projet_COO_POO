package communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
//import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import messages.*;

public class UDPClient {

	private DatagramSocket sockUDP;
	//private InetAddress broadcast;
	
	
	/**
	 * Create an UDP client on the specified port. It will be used to notify the
	 * other users of this application's user state (Connected/Disconnected/Pseudo
	 * changed).
	 * 
	 * @param port
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public UDPClient(int port) throws SocketException, UnknownHostException {
		this.sockUDP = new DatagramSocket(port);
	}
	
	
	/**
	 * Send a message to the specified port on localhost.
	 * 
	 * @param message
	 * @param port
	 * @throws IOException
	 */
	protected void sendMessageUDP_local(Message message, int port, InetAddress clientAddress) throws IOException {
		String messageString= message.toString();
		DatagramPacket outpacket = new DatagramPacket(messageString.getBytes(), messageString.length(), clientAddress, port);
		this.sockUDP.send(outpacket);
		
	}
	
	/**
	 * Send a message to the given address on the specified port.
	 * 
	 * @param message
	 * @param port
	 * @param clientAddress
	 * @throws IOException
	 */
	private void sendMessageUDP(Message message, int port, InetAddress clientAddress) throws IOException {
		String messageString = message.toString();
		DatagramPacket outpacket = new DatagramPacket(messageString.getBytes(), messageString.length(), clientAddress,
				port);
		this.sockUDP.send(outpacket);
	}
	
}
