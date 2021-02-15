package connexion;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;

import communication.udp.CommunicationUDP;
import database.SQLiteManager;
import main.Utilisateur;
import standard.VueStandard;

public class ControleurConnexion implements ActionListener{

	//Controller state : either DEBUT at initialization or ID_OK if the user has signed in
	private enum Etat {DEBUT, ID_OK};
	
	private VueConnexion vue;
	private Etat etat;
	private CommunicationUDP comUDP;
	private int portTCP;
	private String username;
	private SQLiteManager sqlManager;
	private VueStandard vueStd;
	

	/**
	 * Create and initialize the object in charge of monitoring all actions depending on what the user do.
	 * 
	 * @param vue : associated instance of VueConnexion
	 * @param numtest : on local mode, allows you to choose which port to use. Integer between 0 and 3
	 * 
	 */
	public ControleurConnexion(VueConnexion vue, int numtest) {
		this.vue = vue;
		this.etat = Etat.DEBUT;
		this.username = "";
		this.sqlManager = new SQLiteManager(0);
		this.vueStd = null;
		
		int[] portServer = {2209, 2309, 2409, 2509};
		try {
			switch(numtest) {
			case 0 : 
				this.comUDP = new CommunicationUDP(2208, 2209, portServer);
				this.portTCP = 7010;
				break;
			case 1 :
				this.comUDP = new CommunicationUDP(2308, 2309, portServer);
				this.portTCP = 7020;
				break;
			case 2 :
				this.comUDP = new CommunicationUDP(2408, 2409, portServer);
				this.portTCP = 7030;
				break;
			case 3 :
				this.comUDP = new CommunicationUDP(2508, 2509, portServer);
				this.portTCP = 7040;
				break;
			default :
				this.comUDP = new CommunicationUDP(2408, 2409, portServer);
				this.portTCP = 7040;
			}			
			
		} catch (IOException e) {
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
			}
			
			
			if (inputOK) {
				this.etat=Etat.ID_OK;

				//Broadcast "JE_SUIS_CONNECTE" message and waits for the other devices' answers
				try {
					comUDP.sendMessageConnecte();
				} catch (IOException e2) {
				}
			
				try {
					Thread.sleep(2);
				} catch (InterruptedException e1) {
				}
				
				//setup pseudo ask
				this.vue.setConnexionInfo("");
				this.vue.removePasswordPanel();
				
				this.vue.setTextUsernameField("Veuillez entrer votre pseudonyme");
				this.vue.resetUsernameField();
				inputOK=false;
			}
			else {
				this.vue.setConnexionInfo("Nom d'utilisateur ou mot de passe invalide, veuillez réessayer");
				this.vue.resetPasswordField();
			}
			
		}
		else {
			pseudo = vue.getUsernameValue();
			
			//Search in the local list of active users id the chosen pseudo is already in use 
			inputOK = !this.comUDP.containsUserFromPseudo(pseudo);
			if(pseudo.equals("")) {
				this.vue.setConnexionInfo("Votre pseudonyme doit contenir au moins 1 caratère");
			}else if (inputOK) {
				//setup Utilisateur "self" static attribute
				try {
					Utilisateur.setSelf(this.username, pseudo, "localhost", this.portTCP);
				} catch (UnknownHostException e2) {
				}
				
				//broadcast new pseudo
				try {
					this.comUDP.sendMessageInfoPseudo();
				} catch (UnknownHostException e1) {
				} catch (IOException e1) {
				}
					
				try {
					this.resetView();
					this.vue.setVisible(false);
					this.setVueStandard();
				} catch (IOException e1) {
				}
			}
			else this.vue.setConnexionInfo("Ce nom est déjà utilisé, veuillez en choisir un autre");
		}
	}

	
	// ----- SETTING & RESETTING VIEW ----- //
	
	/**
	 * Create a new VueStandard instance and give it the hand.
	 * 
	 */
	private void setVueStandard() throws IOException {
		if(this.vueStd == null) {
			this.vueStd = new VueStandard("Standard", this.comUDP, this.portTCP, this.sqlManager, this.vue);
			
		}else {
			this.vueStd.initControleur();
			this.vueStd.setPseudoSelf();
			this.vueStd.setVisible(true);
		}
	}

	/**
	 * Restore the associated instance of VueConnexion to its initial state
	 * 
	 */
	private void resetView() {
		this.etat = Etat.DEBUT;
		this.vue.addPasswordPanel();
		this.vue.resetPasswordField();
		this.vue.resetUsernameField();
		this.vue.setTextUsernameField("Nom d'utilisateur");
		this.vue.setConnexionInfo("");
		
	}
}
