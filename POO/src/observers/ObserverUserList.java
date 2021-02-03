package observers;

import java.util.ArrayList;

import main.Utilisateur;

public interface ObserverUserList {

	/**
	 * Method called when the userlist is updated
	 * 
	 * @param o        : The observer to notify
	 * @param userList : The userlist
	 */
	public void updateList(Object o, ArrayList<Utilisateur> userList);

}
