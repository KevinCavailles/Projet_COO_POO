package communication;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import main.Utilisateur;

public class CommunicationUDP extends Communication {

	//public enum Mode {PREMIERE_CONNEXION, CHANGEMENT_PSEUDO, DECONNEXION};

	private UDPClient client;
	private int portServer;
	private ArrayList<Integer> portsOther;

	public CommunicationUDP(int portClient, int portServer, int[] portsOther) throws IOException {
		this.portServer = portServer;
		this.portsOther = this.getArrayListFromArray(portsOther);
		new UDPServer(portServer, this).start();
		this.client = new UDPClient(portClient);
	}
	

	private ArrayList<Integer> getArrayListFromArray(int ports[]){
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for(int port : ports) {
			tmp.add(port);
		}
		tmp.remove( Integer.valueOf(portServer) );
		
		return tmp;
	}
	
	public void sendMessageConnecte() throws UnknownHostException, IOException {
		
		for(int port : this.portsOther) {
			this.client.sendMessageUDP("first_connection", port, InetAddress.getLocalHost());
		}
	}
	
	public void sendMessageAdd() throws UnknownHostException, IOException { this.sendIDPseudo("add"); }
	public void sendMessageModify() throws UnknownHostException, IOException{ this.sendIDPseudo("modify"); }
	public void sendMessageDelete() throws UnknownHostException, IOException{ this.sendIDPseudo("del"); }
	
	
	private void sendIDPseudo(String prefixe) throws UnknownHostException, IOException {
		Utilisateur self = Utilisateur.getSelf();
		String idSelf = self.getId();
		String pseudoSelf = self.getPseudo();

		String message = prefixe+","+idSelf + "," + pseudoSelf;
		
		for(int port : this.portsOther) {
			this.client.sendMessageUDP(message, port, InetAddress.getLocalHost());
		}
	}
	
	
//	public synchronized void createSenderUDP(int port, Mode mode) throws SocketException {
//		new SenderUDP(mode, port).start();
//	}

}
