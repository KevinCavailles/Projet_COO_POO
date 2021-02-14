package communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import main.Utilisateur;
import messages.*;

class UDPServer extends Thread {

	private DatagramSocket sockUDP;
	private CommunicationUDP commUDP;
	private byte[] buffer;
	private boolean running;

	
	/**
	 * Create an UDP Server on the specified port. It will be used to read the
	 * other users states (Connected/Disconnected/Pseudo).
	 * 
	 * @param port
	 * @param commUDP
	 * @throws SocketException
	 */
	public UDPServer(int port, CommunicationUDP commUDP) throws SocketException {
		this.running = true;
		this.commUDP = commUDP;
		this.sockUDP = new DatagramSocket(port);
		this.buffer = new byte[256];
	}

	@Override
	public void run() {
		while (this.running) {

			try {
				
				//When a datagram is received, converts its data in a Message
				DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
				this.sockUDP.receive(inPacket);
				String msgString = new String(inPacket.getData(), 0, inPacket.getLength());
				Message msg = Message.stringToMessage(msgString);

				//Depending on the type of the message
				switch (msg.getTypeMessage()) {
				case JE_SUIS_CONNECTE:

					if (Utilisateur.getSelf() != null) {
						
						int portClient = inPacket.getPort();
						int portServer = portClient+1;
						
						//Answer back with this application's user data
						this.commUDP.sendMessageInfoPseudo(portServer);
					}

					break;

				case INFO_PSEUDO:
					
					MessageSysteme m = (MessageSysteme) msg;
					
					//Update the userlist with the data received (Add the user or update it)
					if (this.commUDP.containsUserFromID(m.getId())) {
						this.commUDP.changePseudoUser(m.getId(), m.getPseudo(), inPacket.getAddress(), m.getPort());
					} else {
						this.commUDP.addUser(m.getId(), m.getPseudo(), inPacket.getAddress(), m.getPort());
					}
					break;

				case JE_SUIS_DECONNECTE:
					
					MessageSysteme m2 = (MessageSysteme) msg;
					//Remove the user from the userlist
					this.commUDP.removeUser(m2.getId(), m2.getPseudo(), inPacket.getAddress(), m2.getPort());
					break;

				//Do nothing
				default: 
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public void interrupt() {
		// Stop the thread
		this.running = false;
		// Close the stream and the socket
		this.sockUDP.close();
		this.buffer = null;
		this.commUDP = null;
	}
}
