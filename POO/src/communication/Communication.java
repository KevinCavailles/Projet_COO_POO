package communication;

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
	
	//TODO
	//Combiner add et change
	protected static synchronized void addUser(List<String> datas) throws UnknownHostException {

		String idClient = datas.get(0);
		String pseudoClient = datas.get(1);
		String clientAddress = datas.get(2);
		
		if (!Communication.containsUserFromID(idClient)) {
			Communication.users.add(new Utilisateur(idClient, pseudoClient, clientAddress));
			/*VueStandard.userList.addElement(pseudoClient);*/
		}
	}
	
	protected static synchronized void changePseudoUser(List<String> datas) {
		String idClient = datas.get(0);
		String pseudoClient = datas.get(1);
		int index = Communication.getIndexFromID(idClient);
		System.out.println(index);
		if(index != -1) {
			Communication.users.get(index).setPseudo(pseudoClient);
			/*VueStandard.userList.set(index, pseudoClient);*/
		}
	}
	
	protected static synchronized void removeUser(List<String> datas) {
		String idClient = datas.get(0);
		int index = Communication.getIndexFromID(idClient);
		System.out.println(index);
		if( index != -1) {
			Communication.users.remove(index);
			/*VueStandard.userList.remove(index);*/
		}
	}
	
	public static void removeAll(){
		int oSize = Communication.users.size();
		for(int i=0; i<oSize;i++) {
			Communication.users.remove(0);
		}
	}
}
