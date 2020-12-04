package communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPClient {

	private DatagramSocket sockUDP;
	private InetAddress broadcast;
	
	public UDPClient(int port) throws SocketException, UnknownHostException {
		this.sockUDP = new DatagramSocket(port);
		
		InetAddress localHost = InetAddress.getLocalHost();
		NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
		this.broadcast = networkInterface.getInterfaceAddresses().get(0).getBroadcast();
	}
	
	
	//Send a string message to the specified port on localhost
	protected void sendMessageUDP_local(String message, int port, InetAddress clientAddress) throws IOException {
		
		//A modifier, faire passer un type Message en paramètre
		//puis écrire les instructions pour envoyer un Message à traver la socket
		
		DatagramPacket outpacket = new DatagramPacket(message.getBytes(), message.length(), clientAddress, port);
		this.sockUDP.send(outpacket);
		
	}
	
//	protected void sendMessageUDP_broadcast(String message, int port) throws IOException{
//		DatagramPacket outpacket = new DatagramPacket(message.getBytes(), message.length(), this.broadcast, port);
//		this.sockUDP.send(outpacket);
//	}
	
}
