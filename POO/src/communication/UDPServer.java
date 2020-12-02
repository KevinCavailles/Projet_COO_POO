package communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;



public class UDPServer extends Thread {

	private DatagramSocket sockUDP;
	private CommunicationUDP commUDP;
	private byte[] buffer;

	public UDPServer(int port, CommunicationUDP commUDP) throws SocketException {
		this.commUDP = commUDP;
		this.sockUDP = new DatagramSocket(port);
		this.buffer = new byte[256];
	}

	@Override
	public void run() {
		while (true) {

			try {
				DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
				this.sockUDP.receive(inPacket);
				String msg = new String(inPacket.getData(), 0, inPacket.getLength());
				
				if (msg.equals("first_connection")) {	
					//System.out.println("first co");
					ArrayList<Integer> portClient = new ArrayList<Integer>();
					portClient.add(inPacket.getPort()+1);
					this.commUDP.sendMessageAdd(portClient);
					
				} else if (msg.contains("add,")) {		
					//System.out.println("add");
					ArrayList<String> datas = this.getDatas(inPacket);
					Communication.addUser(datas);
					
				} else if (msg.contains("modify,")) {	
					ArrayList<String> datas = this.getDatas(inPacket);
					Communication.changePseudoUser(datas);
					
				} else if (msg.contains("del,")) {
					ArrayList<String> datas = this.getDatas(inPacket);
					Communication.removeUser(datas);
				}

			} catch (IOException e) {
				System.out.println("receive exception");

			}

		}
	}
	
	protected ArrayList<String> getDatas(DatagramPacket inPacket) {
		//Message
		//
		
		String msg = new String(inPacket.getData(), 0, inPacket.getLength());
		String tmp[] = msg.split(",");
		
		
		
		ArrayList<String> datas = new ArrayList<String>(Arrays.asList(tmp));
		datas.remove(0);
		datas.add(inPacket.getAddress().toString());

		return datas;
	}

}
