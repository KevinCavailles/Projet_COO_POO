package connexion;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;

import communication.*;
import communication.udp.CommunicationUDP;
import database.SQLiteManager;
import main.Utilisateur;
import observers.ObserverUserList;
import standard.VueStandard;

public class ControleurConnexion implements ActionListener, ObserverUserList{

	private enum Etat {DEBUT, ID_OK};
	
	private VueConnexion vue;
	private Etat etat;
	private CommunicationUDP comUDP;
	private int portTCP;
	private int num;
	private String username;
	private SQLiteManager sqlManager;
	
	public ControleurConnexion(VueConnexion vue, int numtest) {
		this.vue = vue;
		this.etat = Etat.DEBUT;
		this.num = numtest;
		this.username = "";
		this.sqlManager = new SQLiteManager(0);
		//Pour les tests, changer pour un truc plus général quand on change CommunicationUDP
		try {
			switch(numtest) {
			case 0 : 
				this.comUDP = new CommunicationUDP(2208, 2209, new int[] {2309, 2409});
				this.portTCP = 7010;
				break;
			case 1 :
				this.comUDP = new CommunicationUDP(2308, 2309, new int[] {2209, 2409});
				this.portTCP = 7020;
				break;
			case 2 :
				this.comUDP = new CommunicationUDP(2408, 2409, new int[] {2209, 2309});
				this.portTCP = 7030;
				break;
			default :
				this.comUDP = new CommunicationUDP(2408, 2409, new int[] {2209, 2309});
				this.portTCP = 7040;
			}
			
			this.comUDP.setObserver(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String pseudo;
		boolean inputOK = false;
		if (this.etat == Etat.DEBUT) {
			
			this.username = this.vue.getUsernameValue();
			char[] password = this.vue.getPasswordValue();
			
			try {
				int res = this.sqlManager.checkPwd(this.username, password);
				inputOK = (res == 1);
	
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			
			
			if (inputOK) {
				this.etat=Etat.ID_OK;

				//Envoi broadcast du message "JeSuisActif" et, attente du retour de la liste des utilisateurs actifs
				try {
					comUDP.sendMessageConnecte();
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try {
					Thread.sleep(2);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				//Mise en place de la demande du pseudo
				this.vue.setConnexionInfo("");
				this.vue.setTextUsernameField("Veuillez entrer votre pseudonyme");
				this.vue.resetUsernameField();
				inputOK=false;
			}
			else this.vue.setConnexionInfo("Identifiant ou mot de passe invalide, veuillez réessayer");
		}
		else {
			pseudo = vue.getUsernameValue();
			
			//Recherche dans la liste locale des utilisateurs connectes, report sur inputOK
			inputOK = !this.comUDP.containsUserFromPseudo(pseudo);
			if(pseudo.equals("")) {
				this.vue.setConnexionInfo("Votre pseudonyme doit contenir au moins 1 caratère");
			}else if (inputOK) {
				//Reglage de l'utilisateur
				try {
					Utilisateur.setSelf(this.username, pseudo, "localhost", this.portTCP);
				} catch (UnknownHostException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				//Broadcast du pseudo
				try {
					this.comUDP.sendMessageInfoPseudo();
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					this.vue.close();
					new VueStandard("Standard", this.comUDP, this.portTCP, this.sqlManager, this.num);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else this.vue.setConnexionInfo("Ce nom est déjà utilisé, veuillez en choisir un autre");
		}
	}

	@Override
	public void updateList(Object o, ArrayList<Utilisateur> userList) {
		// TODO Auto-generated method stub
		
	}

}
