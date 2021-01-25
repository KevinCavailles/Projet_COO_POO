package communication.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import main.Utilisateur;

import messages.*;
import observers.ObserverUserList;

public class CommunicationUDP extends Thread {

	private UDPClient client;
	private UDPServer server;
	private int portServer;
	private ArrayList<Integer> portOthers;
	private ArrayList<Utilisateur> users = new ArrayList<Utilisateur>();
	private ObserverUserList observer;

	public CommunicationUDP(int portClient, int portServer, int[] portsOther) throws IOException {
		this.portServer = portServer;
		this.portOthers = this.getArrayListFromArray(portsOther);
		this.server = new UDPServer(portServer, this);
		this.server.start();
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

	public void setObserver(ObserverUserList obs) {
		this.observer = obs;
	}

	//-------------- USER LIST UPDATE FUNCTION --------------//

	protected synchronized void addUser(String idClient, String pseudoClient, InetAddress ipClient, int port)
			throws IOException {
		users.add(new Utilisateur(idClient, pseudoClient, ipClient, port));
		observer.updateList(this, users);

	}

	protected synchronized void changePseudoUser(String idClient, String pseudoClient, InetAddress ipClient, int port) {
		int index = getIndexFromID(idClient);
		users.get(index).setPseudo(pseudoClient);
		observer.updateList(this, users);
	}

	protected synchronized void removeUser(String idClient, String pseudoClient, InetAddress ipClient, int port) {
		int index = getIndexFromIP(ipClient);
		if (index != -1) {
			users.remove(index);
		}
		observer.updateList(this, users);
	}

	public void removeAll() {
		int oSize = users.size();
		for (int i = 0; i < oSize; i++) {
			users.remove(0);
		}
	}

	//-------------- CHECKERS --------------//

	protected boolean containsUserFromID(String id) {
		for (Utilisateur u : users) {
			if (u.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsUserFromPseudo(String pseudo) {
		for (Utilisateur u : users) {
			if (u.getPseudo().toLowerCase().equals(pseudo)) {
				return true;
			}
		}

		return false;
	}

	//-------------- GETTERS --------------//

	public Utilisateur getUserFromPseudo(String pseudo) {
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getPseudo().equals(pseudo)) {
				return users.get(i);
			}
		}
		return null;
	}

	private int getIndexFromID(String id) {
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getId().equals(id)) {
				return i;
			}
		}
		return -1;
	}

	private int getIndexFromIP(InetAddress ip) {
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getIp().equals(ip)) {
				return i;
			}
		}
		return -1;
	}
	
	
	
	//-------------- SEND MESSAGES --------------//
	
	public void sendMessageConnecte() throws UnknownHostException, IOException {
		for (int port : this.portOthers) {
			try {
				this.client.sendMessageUDP_local(new MessageSysteme(Message.TypeMessage.JE_SUIS_CONNECTE), port,
						InetAddress.getLocalHost());
			} catch (MauvaisTypeMessageException e) {
				/* Si ça marche pas essayer là */}
		}
	}

	// Send the message "add,id,pseudo" to localhost on all the ports in
	// "portOthers"
	// This allows the receivers' agent (portOthers) to create or modify an entry
	// with the
	// data of this agent
	// Typically used to notify of a name change
	public void sendMessageInfoPseudo() throws UnknownHostException, IOException {

		Utilisateur self = Utilisateur.getSelf();

		String pseudoSelf = self.getPseudo();
		String idSelf = self.getId();
		int portSelf = self.getPort();

		Message msout = null;
		try {
			msout = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, pseudoSelf, idSelf, portSelf);
			for (int port : this.portOthers) {
				this.client.sendMessageUDP_local(msout, port, InetAddress.getLocalHost());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Same, but on only one port
	// Typically used to give your current name and id to a newly arrived host
	public void sendMessageInfoPseudo(int portOther) throws UnknownHostException, IOException {

		Utilisateur self = Utilisateur.getSelf();
		try {
			Message msout = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, self.getPseudo(), self.getId(),
					self.getPort());
			this.client.sendMessageUDP_local(msout, portOther, InetAddress.getLocalHost());
		} catch (MauvaisTypeMessageException e) {
			e.printStackTrace();
		}
	}

	// Send the message "del,id,pseudo" to localhost on all the ports in
	// "portOthers"
	// This allows the receivers' agent (portOthers) to delete the entry
	// corresponding to this agent
	public void sendMessageDelete() throws UnknownHostException, IOException {
		for (int port : this.portOthers) {
			try {
				this.client.sendMessageUDP_local(new MessageSysteme(Message.TypeMessage.JE_SUIS_DECONNECTE), port,
						InetAddress.getLocalHost());
			} catch (MauvaisTypeMessageException e) {
			}
		}
	}

	// Pas encore adapte message
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
	
	public void destroyAll() {
		this.client.destroyAll();
		this.server.interrupt();
	}

}
