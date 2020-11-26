package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class CommunicationUDP extends Thread {
	
	private int port;
	private DatagramSocket sockUDP;
	private byte[] buffer;
	
	public CommunicationUDP(int port) throws SocketException, UnknownHostException {
		this.sockUDP = new DatagramSocket(port);
		this.buffer = new byte[256];
	}
	
	@Override
	public void run() {
		while(true) {
			DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
			try {
				this.sockUDP.receive(inPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	private class ReponseUDP extends Thread{
		
		private DatagramSocket sockUDP;
		private DatagramPacket inPacket;
		
		public ReponseUDP(DatagramSocket sockUDP, DatagramPacket inPacket) {
			this.sockUDP = sockUDP;
			this.inPacket = inPacket;
		}
		
		@Override
		public void run() {
			String msg = new String(this.inPacket.getData(),0,this.inPacket.getLength());
			if(msg == "Connecté") {	
				
				Utilisateur self = Utilisateur.getSelf();
				String id = self.getId();
				String pseudo = self.getPseudo();
				InetAddress ip = self.getIp();
				
				InetAddress clientAddress = this.inPacket.getAddress();
				int clientPort = this.inPacket.getPort();
				
				
			}
		}
		
	}

}
