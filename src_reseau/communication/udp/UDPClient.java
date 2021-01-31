package communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import messages.*;

public class UDPClient {

	private DatagramSocket sockUDP;
	private InetAddress broadcast;
	
	public UDPClient(int port) throws SocketException, UnknownHostException {
		this.sockUDP = new DatagramSocket(port);
		
		InetAddress localHost = InetAddress.getLocalHost();
		NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
		this.broadcast = networkInterface.getInterfaceAddresses().get(0).getBroadcast();
		System.out.println(this.broadcast);
		System.out.println(InetAddress.getLocalHost());
	}
	
	protected void sendMessageUDP_local(Message message, InetAddress addrOther) throws IOException {
		String messageString= message.toString();
		DatagramPacket outpacket = new DatagramPacket(messageString.getBytes(), messageString.length(), addrOther, CommunicationUDP.PORT_SERVEUR);
		this.sockUDP.send(outpacket);
	}
	
	protected void sendMessageUDP_broadcast(Message message) throws IOException{
		String messageString = message.toString();
		DatagramPacket outpacket = new DatagramPacket(messageString.getBytes(), messageString.length(), this.broadcast, CommunicationUDP.PORT_SERVEUR);
		this.sockUDP.send(outpacket);
	}
	
	protected void destroyAll() {
		this.sockUDP.close();
		this.sockUDP = null;
		this.broadcast = null;
	}
	
}