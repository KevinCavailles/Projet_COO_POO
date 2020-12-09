package communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import main.Utilisateur;
import messages.*;


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
			try {
				this.client.sendMessageUDP_local(new MessageSysteme(Message.TypeMessage.JE_SUIS_CONNECTE), port, InetAddress.getLocalHost());
			} catch (MauvaisTypeMessageException e) {/*Si ça marche pas essayer là*/}
		}
	}
	
	
	// Send the message "add,id,pseudo" to localhost on all the ports in
	// "portOthers"
	// This allows the receivers' agent (portOthers) to create or modify an entry with the
	// data of this agent
	//Typically used to notify of a name change
	public void sendMessageInfoPseudo() throws UnknownHostException, IOException {

		Utilisateur self = Utilisateur.getSelf();
		
		String pseudoSelf =self.getPseudo();
		String idSelf = self.getId();
		int portSelf = self.getPort();
		
		Message msout = null;
		try {
			msout = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, pseudoSelf, idSelf, portSelf);
			for(int port : this.portOthers) {
				this.client.sendMessageUDP_local(msout, port, InetAddress.getLocalHost());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	//Same, but on only one port
	//Typically used to give your current name and id to a newly arrived host
	public void sendMessageInfoPseudo(int portOther) throws UnknownHostException, IOException {
	
		Utilisateur self = Utilisateur.getSelf();
		try {
			Message msout = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, self.getPseudo(), self.getId(), self.getPort());
			this.client.sendMessageUDP_local(msout, portOther, InetAddress.getLocalHost());
		} catch (MauvaisTypeMessageException e) {e.printStackTrace();}
	}


	// Send the message "del,id,pseudo" to localhost on all the ports in
	// "portOthers"
	// This allows the receivers' agent (portOthers) to delete the entry
	// corresponding to this agent
	public void sendMessageDelete() throws UnknownHostException, IOException {
		for(int port : this.portOthers) {
			try {
				this.client.sendMessageUDP_local(new MessageSysteme(Message.TypeMessage.JE_SUIS_DECONNECTE), port, InetAddress.getLocalHost());
			} catch (MauvaisTypeMessageException e) {/*Si ça marche pas essayer là*/}
		}
	}

	//Pas encore adapte message
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
