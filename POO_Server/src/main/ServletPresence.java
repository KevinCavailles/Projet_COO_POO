package main;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ServletPresence
 */
@WebServlet("/ServletPresence")
public class ServletPresence extends HttpServlet {
	private static final long serialVersionUID = 1L;

	//suscribe(), publish(), notify()
	
	private ArrayList<Utilisateur> localUsers;
	private ArrayList<Utilisateur> remoteUsers;
 
    public ServletPresence() {
        localUsers = new ArrayList<Utilisateur>();
        remoteUsers = new ArrayList<Utilisateur>();
    }

    //Permet a un utilisateur externe de s'ajouter/s'enlever à la liste des utilisateurs externes : au tout début de l'application
    private void suscribe() {
    }
    
    private void unsubscribe() { 	
    }
    
    //Permet de dire si on est connecté/déconnecté
    private void publish() {
    }
    
    //Informe de la modification de la liste tous les utilisateurs iinternes et externes
    private void snotify() {
    	
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
