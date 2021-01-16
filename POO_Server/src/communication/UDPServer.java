package communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import main.Utilisateur;
import messages.*;



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
				String msgString = new String(inPacket.getData(), 0, inPacket.getLength());
				Message msg = Message.stringToMessage(msgString);

				switch(msg.getTypeMessage()) {
				case JE_SUIS_CONNECTE :	
					
					if(Utilisateur.getSelf() != null) {
						//System.out.println("first co");
						int portClient = inPacket.getPort();
						int portServer = portClient+1;
						
						this.commUDP.sendMessageInfoPseudo(portServer);
						this.commUDP.getObserver().update(this, portServer);
					}
					
					
					break;
					
				case INFO_PSEUDO :
					

					if (this.commUDP.containsUserFromID(((MessageSysteme) msg).getId())) {
						this.commUDP.changePseudoUser(((MessageSysteme) msg).getId(), ((MessageSysteme) msg).getPseudo(), inPacket.getAddress(),((MessageSysteme) msg).getPort()); 
					}
					else {
						
						this.commUDP.addUser(((MessageSysteme) msg).getId(), ((MessageSysteme) msg).getPseudo(), inPacket.getAddress(), ((MessageSysteme) msg).getPort() );
						System.out.println(((MessageSysteme) msg).getId()+", "+((MessageSysteme) msg).getPseudo());
					}
					break;
					
				case JE_SUIS_DECONNECTE :
					this.commUDP.removeUser( ((MessageSysteme) msg).getId() , ((MessageSysteme) msg).getPseudo(), inPacket.getAddress(), inPacket.getPort());
					break;
					
				default : //Others types of messages are ignored because they are supposed to be transmitted by TCP and not UDP
				}

			} catch (IOException e) {
				System.out.println("receive exception");
			}

		}
	}
}
