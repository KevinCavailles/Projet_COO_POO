package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

//Faire un publish (get) séparé en utilisant les cookies pour stocker les modifications : pose problème au niveau de la synchro des pseudos (à dire rapport)
// BUG 1 : problème des conflits de pseudo
//Transmission de la liste des locaux direct du serveur aux externes ? à voir (direct dans le doPost du coup)
public class ServletPresence extends HttpServlet implements Observer {
	private static final long serialVersionUID = 1L;
	
	private CommunicationUDP comUDP;
	private ArrayList<Utilisateur> remoteUsers;
 
    public ServletPresence() {
    	//A changer en passant aux IP
        try {
			comUDP = new CommunicationUDP(3333, 3334, new int[] {2209, 2309, 2409, 3334});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        comUDP.setObserver(this);
        remoteUsers = new ArrayList<Utilisateur>();
        try {
			Utilisateur.setSelf("serv_p", "Serveur de presence", "localhost", 3334);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }
    
    private int getIndexByID(String id) {
    	for(int i=0; i < remoteUsers.size() ; i++) {
			if(remoteUsers.get(i).getId().equals(id) ) {
				return i;
			}
		}
		return -1;
    }
    
    private boolean remoteContainsUserFromPseudo(String pseudo) {
    	for(Utilisateur u : remoteUsers) {
			if(u.getPseudo().equals(pseudo) ) {
				return true;
			}
    	}
    	return false;
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
	//Note : le serveur agit comme un proxy pour le TCP et remplace le port de l'utilisateur par le sien
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String id = request.getParameter("id");
		String pseudo = request.getParameter("pseudo");
		int port = Integer.parseInt(request.getParameter("port"));
		InetAddress ip = InetAddress.getByName(request.getRemoteAddr());
		
		//Si le pseudo est déjà pris : génère du html pour en informer l'utilisateur
		if (comUDP.containsUserFromPseudo(pseudo)||remoteContainsUserFromPseudo(pseudo)) {
			response.setContentType( "text/html" );
		    PrintWriter out = response.getWriter();
		    out.println( "<HTML>" );
		    out.println( "<HEAD>");
		    out.println( "<TITLE>Erreur pseudo déjà utilisé</TITLE>" );
		    out.println( "</HEAD>" );
		    out.println( "<BODY>" );
		    out.println( "<H1>Erreur : Ce pseudo est déjà utilisé</H1>" );
		    out.println( "<H2>Veuillez choisir un autre pseudo</H2>" );
		    out.println( "</BODY>" );
		    out.println( "</HTML>" );
		    out.close();
		}
		
		//Sinon
		else {
			Utilisateur user = new Utilisateur(id, pseudo, ip, port);
	    	remoteUsers.add(user);
	    	try {
				snotify(new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, pseudo, id, 3334), user);
			} catch (MauvaisTypeMessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String id = request.getParameter("id");
		int index = getIndexByID(id);
    	Utilisateur user = remoteUsers.get(index);
    	remoteUsers.remove(index);
    	try {
			snotify(new MessageSysteme(Message.TypeMessage.JE_SUIS_DECONNECTE, id), user);
		} catch (MauvaisTypeMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    //Permet de dire si on a changé de pseudo (pour les utilisateurs externes)
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String id = request.getParameter("id");
		String pseudo = request.getParameter("pseudo");
		int index = getIndexByID(id);
		
		//Si le pseudo est déjà pris : génère du html pour en informer l'utilisateur
		if (comUDP.containsUserFromPseudo(pseudo)||remoteContainsUserFromPseudo(pseudo)) {
			response.setContentType( "text/html" );
			PrintWriter out = response.getWriter();
			out.println( "<HTML>" );
			out.println( "<HEAD>");
			out.println( "<TITLE>Erreur pseudo déjà utilisé</TITLE>" );
			out.println( "</HEAD>" );
			out.println( "<BODY>" );
			out.println( "<H1>Erreur : Ce pseudo est déjà utilisé</H1>" );
			out.println( "<H2>Veuillez choisir un autre pseudo</H2>" );
			out.println( "</BODY>" );
			out.println( "</HTML>" );
			out.close();
		}
		
		else {
			Utilisateur user = remoteUsers.get(index);
			user.setPseudo(pseudo);
			try {
				snotify(new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, pseudo, id, 3334), user);
			} catch (MauvaisTypeMessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

		try {
			String type= request.getParameter("type");
		
			switch(type) {
				case "POST" :
					doPost(request, response);
					break;
					
				case "DELETE" :
					doDelete(request, response);
					break;
					
				case "PUT" :
					doPut(request, response);
					break;
					
				//génère une jolie page
				default :
					response.setContentType( "text/html" );
				    PrintWriter out = response.getWriter();
				    out.println( "<HTML>" );
				    out.println( "<HEAD>");
				    out.println( "<TITLE>Erreur requête invalide</TITLE>" );
				    out.println( "</HEAD>" );
				    out.println( "<BODY>" );
				    out.println( "<H1>Erreur : nous n'avons pas compris votre requête</H1>" );
				    out.println( "<H2>Veuillez vérifier votre syntaxe et réessayer</H2>" );
				    out.println( "</BODY>" );
				    out.println( "</HTML>" );
				    out.close();
			}
		} 
		//Si pas d'argument type : page d'accueil
		catch (java.lang.NullPointerException e) {
			response.setContentType( "text/html" );
		    PrintWriter out = response.getWriter();
		    out.println( "<HTML>" );
		    out.println( "<HEAD>");
		    out.println( "<TITLE>Serveur de présence - Accueil</TITLE>" );
		    out.println( "</HEAD>" );
		    out.println( "<BODY>" );
		    out.println( "<H1>Bienvenue sur le service de connexion à distance</H1>" );
		    out.println( "<H2>Vous pouvez taper votre requête dans la barre de navigation</H2>" );
		    out.println( "<H2>Requêtes (à ajouter derrière l'URL) : </H2>" );
		    out.println( "<H3>Se connecter : ?type=POST&id=[votre id]&pseudo=[pseudo voulu]&port=[port utilisé] </H3>" );
		    out.println( "<H3>Se déconnecter : ?type=DELETE&id=[votre id] </H3>" );
		    out.println( "<H3>Changer de pseudo : ?type=PUT&id=[votre id]&pseudo=[pseudo voulu] </H3>" );
		    out.println( "</BODY>" );
		    out.println( "</HTML>" );
		    out.close();
			
		}
	}
	
	
	@Override
	//Note : on part du principe que pour les communications TCP et autres, le serveur agira comme un proxy et donc que les 
	//utilisateurs externes n'ont pas besoin de connaitre les ip internes des machines
	
	public void update(Object o, Object arg) {
		// pour transmettre aux utilisateurs externes les modifications internes
		if (arg instanceof MessageSysteme) {
			MessageSysteme message = (MessageSysteme) arg;
			snotify(message, comUDP.getUserFromID(message.getId()));
		}
		//pour transmettre la liste des utilisateurs externes aux nouveaux arrivants internes
		if (arg instanceof Integer) {
			int port = (int)arg;
			for (Utilisateur u : remoteUsers) {
	    		try {
					comUDP.sendMessage(new MessageSysteme(Message.TypeMessage.INFO_PSEUDO, u.getPseudo(), u.getId(), u.getPort()), port);
				} catch (MauvaisTypeMessageException e) {
					e.printStackTrace();
				}
	    	}
		}
	}

}
