package communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import main.Utilisateur;

public class CommunicationUDP extends Communication {

	// public enum Mode {PREMIERE_CONNEXION, CHANGEMENT_PSEUDO, DECONNEXION};

	private UDPClient client;
	private int portServer;
	private ArrayList<Integer> portOthers;

	public CommunicationUDP(int portClient, int portServer, int[] portsOther) throws IOException {
		this.portServer = portServer;
		this.portOthers = this.getArrayListFromArray(portsOther);
		new UDPServer(portServer, this).start();
		this.client = new UDPClient(portClient);
	}

	private ArrayList<Integer> getArrayListFromArray(int ports[]) {
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for (int port : ports) {
			tmp.add(port);
		}
		tmp.remove(Integer.valueOf(portServer));

		return tmp;
	}

	
	public void sendMessageConnecte() throws UnknownHostException, IOException {
		for(int port : this.portOthers) {
			this.client.sendMessageUDP_local("first_connection", port, InetAddress.getLocalHost());
		}
	}
	
	
	// Send the message "add,id,pseudo" to localhost on all the ports in
	// "portOthers"
	// This allows the receivers' agent (portOthers) to create an entry with the
	// data of this agent
	public void sendMessageAdd() throws UnknownHostException, IOException {
		this.sendIDPseudo_local("add");
	}

	public void sendMessageAdd(ArrayList<Integer> portServers) throws UnknownHostException, IOException {
		this.sendIDPseudo_local("add", portServers);
	}

	// Send the message "modify,id,pseudo" to localhost on all the ports in
	// "portOthers"
	// This allows the receivers' agent (portOthers) to update the entry
	// corresponding to this agent
	public void sendMessageModify() throws UnknownHostException, IOException {
		this.sendIDPseudo_local("modify");
	}

	// Send the message "del,id,pseudo" to localhost on all the ports in
	// "portOthers"
	// This allows the receivers' agent (portOthers) to delete the entry
	// corresponding to this agent
	public void sendMessageDelete() throws UnknownHostException, IOException {
		this.sendIDPseudo_local("del");
	}

	// Private function to create the message "[prefix],id,pseudo"
	// and send it to localhost on all the ports in "portOthers"
	private void sendIDPseudo_local(String prefixe, ArrayList<Integer> portServers) throws UnknownHostException, IOException {
		Utilisateur self = Utilisateur.getSelf();
		String idSelf = self.getId();
		String pseudoSelf = self.getPseudo();

		if (!pseudoSelf.equals("")) {

			String message = prefixe + "," + idSelf + "," + pseudoSelf;
			// A modifier pour créer un objet de type Message
			//
			//

			for (int port : portServers) {
				this.client.sendMessageUDP_local(message, port, InetAddress.getLocalHost());
			}
		}

	}

	private void sendIDPseudo_local(String prefixe) throws UnknownHostException, IOException {
		this.sendIDPseudo_local(prefixe, this.portOthers);
	}

//	private void sendIDPseudo_broadcast(String prefixe) throws UnknownHostException, IOException {
//		Utilisateur self = Utilisateur.getSelf();
//		String idSelf = self.getId();
//		String pseudoSelf = self.getPseudo();
//
//		String message = prefixe+","+idSelf + "," + pseudoSelf;
//		
//		
//		this.client.sendMessageUDP_broadcast(message, this.portServer);
//		
//	}

//	public synchronized void createSenderUDP(int port, Mode mode) throws SocketException {
//		new SenderUDP(mode, port).start();
//	}

}
