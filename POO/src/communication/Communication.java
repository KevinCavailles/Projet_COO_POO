package communication;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import main.Utilisateur;
import standard.VueStandard;

public class Communication extends Thread{
	protected static ArrayList<Utilisateur> users = new ArrayList<Utilisateur>();
	
	protected static boolean containsUserFromID(String id) {
		for(Utilisateur u : Communication.users) {
			if(u.getId().equals(id) ) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean containsUserFromPseudo(String pseudo) {
		for(Utilisateur u : Communication.users) {
			if(u.getPseudo().equals(pseudo) ) {
				return true;
			}
		}
		
		return false;
	}
	
	public static InetAddress getIPFromPseudo(String pseudo) {
		int index = Communication.getIndexFromPseudo(pseudo);
		if (index != -1) {
			return Communication.users.get(index).getIp();
		}
		return null;
	}
	
	public static int getPortFromPseudo(String pseudo) {
		int index = Communication.getIndexFromPseudo(pseudo);
		if (index != -1) {
			return Communication.users.get(index).getPort();
		}
		return -1;
	}
	
	protected static int getIndexFromID(String id) {
		for(int i=0; i < Communication.users.size() ; i++) {
			if(Communication.users.get(i).getId().equals(id) ) {
				return i;
			}
		}
		return -1;
	}
	
	protected static int getIndexFromIP(InetAddress ip) {
		for(int i=0; i < Communication.users.size() ; i++) {
			if(Communication.users.get(i).getIp().equals(ip)) {
				return i;
			}
		}
		return -1;
	}
	
	protected static int getIndexFromPseudo(String pseudo) {
		for(int i=0; i < Communication.users.size() ; i++) {
			if(Communication.users.get(i).getPseudo().equals(pseudo)) {
				return i;
			}
		}
		return -1;
	}
	
	
	protected static synchronized void addUser(String idClient, String pseudoClient, InetAddress ipClient, int portTCP) throws UnknownHostException {
		System.out.println("port de " +pseudoClient+" : "+ portTCP);
		Communication.users.add(new Utilisateur(idClient, pseudoClient, ipClient, portTCP));
		VueStandard.userList.addElement(pseudoClient);
	}
	
	protected static synchronized void changePseudoUser(String idClient, String pseudoClient, InetAddress ipClient, int port) {
		int index = Communication.getIndexFromID(idClient);
		Communication.users.get(index).setPseudo(pseudoClient);
		VueStandard.userList.set(index, pseudoClient);
	}

	
	protected static synchronized void removeUser(String idClient, String pseudoClient,InetAddress ipClient, int port) {
		int index = Communication.getIndexFromIP(ipClient);
		if( index != -1) {
			Communication.users.remove(index);
			VueStandard.userList.remove(index);
		}
	}
	
	public static void removeAll(){
		int oSize = Communication.users.size();
		for(int i=0; i<oSize;i++) {
			Communication.users.remove(0);
		}
	}
	
}
