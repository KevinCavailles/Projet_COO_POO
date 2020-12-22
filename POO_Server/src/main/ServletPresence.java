package main;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import communication.CommunicationUDP;

/**
 * Servlet implementation class ServletPresence
 */
@WebServlet("/ServletPresence")
public class ServletPresence extends HttpServlet implements Observer {
	private static final long serialVersionUID = 1L;

	//suscribe(), publish(), snotify(), puis voir doPost
	//Voir avant tout le fonctionnement des requetes HTTP, et comment elles donnent des infos
	//rajouter une classe pour la communication HTTP <= tenir Kevin au courant
	
	private CommunicationUDP comUDP;
 
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

    //Permet a un utilisateur externe de s'ajouter/s'enlever à la liste des utilisateurs externes : au tout début de l'application
    private void suscribe() {
    }
    
    private void unsubscribe() { 	
    }
    
    //Permet de dire si on a changé de pseudo
    private void publish() {
    }
    
    //Informe de la modification de la liste tous les utilisateurs internes et externes => par reponse HTTP
    private void snotify() {
    	
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	@Override
	//Rien a faire : pas d'affichage sur un serveur
	public void update(Object o, Object arg) {
		
	}

}
