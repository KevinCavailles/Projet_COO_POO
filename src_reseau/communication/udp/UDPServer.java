package communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import main.Utilisateur;
import messages.*;

public class UDPServer extends Thread {

	private DatagramSocket sockUDP;
	private CommunicationUDP commUDP;
	private byte[] buffer;
	private boolean running;

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

				DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
				this.sockUDP.receive(inPacket);
				String msgString = new String(inPacket.getData(), 0, inPacket.getLength());
				Message msg = Message.stringToMessage(msgString);

				if (inPacket.getAddress().equals(InetAddress.getLocalHost())) {
					continue;
				}

				switch (msg.getTypeMessage()) {
				case JE_SUIS_CONNECTE:

					if (Utilisateur.getSelf() != null) {

						this.commUDP.sendMessageInfoPseudo(inPacket.getAddress());
					}

					break;

				case INFO_PSEUDO:
					MessageSysteme m = (MessageSysteme) msg;

					if (this.commUDP.containsUserFromID(m.getId())) {
						this.commUDP.changePseudoUser(m.getId(), m.getPseudo(), inPacket.getAddress());
					} else {

						this.commUDP.addUser(m.getId(), m.getPseudo(), inPacket.getAddress());
						System.out.println(m.getId() + ", " + m.getPseudo());
					}
					break;

				case JE_SUIS_DECONNECTE:
					MessageSysteme m2 = (MessageSysteme) msg;
					this.commUDP.removeUser(m2.getId(), m2.getPseudo(), inPacket.getAddress());
					break;

				default: // Others types of messages are ignored because they are supposed to be
							// transmitted by TCP and not UDP
				}

			} catch (IOException e) {
				System.out.println("receive exception");
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
