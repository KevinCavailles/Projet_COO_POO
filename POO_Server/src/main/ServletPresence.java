package main;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import communication.CommunicationUDP;
import messages.*;
import messages.Message.TypeMessage;

/**
 * Servlet implementation class ServletPresence
 */
@WebServlet("/ServletPresence")
public class ServletPresence extends HttpServlet implements Observer {
	private static final long serialVersionUID = 1L;
	
	private CommunicationUDP comUDP;
	private ArrayList<Utilisateur> remoteUsers;
 
    public ServletPresence() {
    	//A changer en passant aux IP
        try {
			comUDP = new CommunicationUDP(3333, 3334, new int[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        comUDP.setObserver(this);
    }
    
    private int getIndexByID(String id) {
    	for(int i=0; i < remoteUsers.size() ; i++) {
			if(remoteUsers.get(i).getId().equals(id) ) {
				return i;
			}
		}
		return -1;
    }
    
    //Informe de la modification de la liste tous les utilisateurs internes et externes
    private void snotify(MessageSysteme message, Utilisateur user) {
    	if (remoteUsers.contains(user)) {
    		//diffuse le message localement, envoie la nouvelle liste des utilisateurs aux utilisateurs externes SAUF L'EXPEDITEUR
    		comUDP.sendMessage(message);
    		for (Utilisateur u : remoteUsers) {
    			if (!u.equals(user)) {
    				comUDP.sendMessage(message, u.getPort());
    			}
    		}
    	}
    	else {
    		//envoie la nouvelle liste des utilisateurs aux utilisateurs externes
    		for (Utilisateur u : remoteUsers) {
    			comUDP.sendMessage(message, u.getPort());
    		}
    	}
    }

  // susbribe/unsubscribe : Permet a un utilisateur externe de s'ajouter/s'enlever à la liste des utilisateurs externes : au tout début de l'application
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Utilisateur user = new Utilisateur(id, pseudo, ip, port);
    	remoteUsers.add(user);
	}
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		int index = getIndexByID(id);
    	Utilisateur user = remoteUsers.get(index);
    	remoteUsers.remove(index);
	}

    //Permet de dire si on a changé de pseudo (pour les utilisateurs externes)
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		int index = getIndexByID(id);
		Utilisateur user = remoteUsers.get(index);
		user.setPseudo(pseudo);
	}
	
    //Informe de la modification de la liste tous les utilisateurs internes et externes
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		snotify(message, comUDP.getUserFromID(message.getId()));
	}
	
	@Override
	//Note : on part du principe que pour les communications TCP et autres, le serveur agira comme un proxy et donc que les 
	//utilisateurs externes n'ont pas besoin de connaitre les ip internes des machines
	//Pourquoi j'ai fait ça ?????? Je change si je me souviens
	public void update(Object o, Object arg) {
		/*MessageSysteme message = (MessageSysteme) arg;
		snotify(message, comUDP.getUserFromID(message.getId()));*/
	}

}
