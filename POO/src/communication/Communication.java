package communication;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import main.Utilisateur;
/*import main.VueStandard;*/

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
	
	protected static int getIndexFromID(String id) {
		for(int i=0; i < Communication.users.size() ; i++) {
			if(Communication.users.get(i).getId().equals(id) ) {
				return i;
			}
		}
		return -1;
	}
	
	protected static synchronized void addUser(String idClient, String pseudoClient, InetAddress ipClient) throws UnknownHostException {
		Communication.users.add(new Utilisateur(idClient, pseudoClient, ipClient));
		/*VueStandard.userList.addElement(pseudoClient);*/
	}
	
	protected static synchronized void changePseudoUser(String idClient, String pseudoClient, InetAddress ipClient) {
		int index = Communication.getIndexFromID(idClient);
		Communication.users.get(index).setPseudo(pseudoClient);
		/*VueStandard.userList.set(index, pseudoClient);*/
	}
	
	protected static int getIndexFromIP(InetAddress ip) {
		for(int i=0; i < Communication.users.size() ; i++) {
			if(Communication.users.get(i).getIp().equals(ip)) {
				return i;
			}
		}
		return -1;
	}

	
	protected static synchronized void removeUser(String idClient, String pseudoClient,InetAddress ipClient) {
		int index = Communication.getIndexFromIP(ipClient);
		if( index != -1) {
			Communication.users.remove(index);
			//VueStandard.userList.remove(index);
		}
	}
	
	public static void removeAll(){
		int oSize = Communication.users.size();
		for(int i=0; i<oSize;i++) {
			Communication.users.remove(0);
		}
	}
}
