package communication.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import main.Utilisateur;

import messages.*;
import observers.ObserverUserList;

public class CommunicationUDP extends Thread {

	protected static int PORT_SERVEUR = 3000;
	protected static int PORT_CLIENT = 2000;

	private UDPClient client;
	private UDPServer server;
	private int portServer;

	private ArrayList<Utilisateur> users = new ArrayList<Utilisateur>();
	private ObserverUserList observer;

	public CommunicationUDP() throws SocketException, UnknownHostException {
		this.portServer = PORT_SERVEUR;
		this.server = new UDPServer(portServer, this);
		this.server.start();
		this.client = new UDPClient(PORT_CLIENT);
	}

	public void setObserver(ObserverUserList obs) {
		this.observer = obs;
	}

	// -------------- USER LIST UPDATE FUNCTION --------------//

	protected synchronized void addUser(String idClient, String pseudoClient, InetAddress ipClient)
			throws IOException {
		users.add(new Utilisateur(idClient, pseudoClient, ipClient));
		
		this.sendUpdate();

	}

	protected synchronized void changePseudoUser(String idClient, String pseudoClient, InetAddress ipClient) {
		int index = getIndexFromID(idClient);
		users.get(index).setPseudo(pseudoClient);
		this.sendUpdate();
	}

	protected synchronized void removeUser(String idClient, String pseudoClient, InetAddress ipClient) {
		int index = getIndexFromIP(ipClient);
		if (index != -1) {
			users.remove(index);
		}
		this.sendUpdate();
	}

	public void removeAll() {
		int oSize = users.size();
		for (int i = 0; i < oSize; i++) {
			users.remove(0);
		}
	}

	// -------------- CHECKERS --------------//

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

	// -------------- GETTERS --------------//

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

	// -------------- SEND MESSAGES --------------//


	public void sendMessageConnecte() throws UnknownHostException, IOException {
		try {
			MessageSysteme m = new MessageSysteme(Message.TypeMessage.JE_SUIS_CONNECTE);
			this.client.sendMessageUDP_broadcast(m);
		} catch (MauvaisTypeMessageException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void sendMessageInfoPseudo(InetAddress addrOther) {
		Utilisateur self = Utilisateur.getSelf();
		try {
			MessageSysteme m = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, self.getPseudo(), self.getId());
			this.client.sendMessageUDP_local(m, addrOther);
		} catch (MauvaisTypeMessageException | IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessageInfoPseudo() {
		Utilisateur self = Utilisateur.getSelf();
		try {
			MessageSysteme m = new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, self.getPseudo(), self.getId());
			this.client.sendMessageUDP_broadcast(m);
		} catch (MauvaisTypeMessageException | IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessageDelete() {
		
		try {
			MessageSysteme m = new MessageSysteme(Message.TypeMessage.JE_SUIS_DECONNECTE);
			this.client.sendMessageUDP_broadcast(m);
		} catch (MauvaisTypeMessageException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	private void sendUpdate() {
		if(this.observer != null) {
			this.observer.updateList(this, users);
		}
	}

	public void destroyAll() {
		this.client.destroyAll();
		this.server.interrupt();
	}

}
