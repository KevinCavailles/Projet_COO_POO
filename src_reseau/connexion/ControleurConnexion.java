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

	private enum Etat {DEBUT, ID_OK};
	
	private VueConnexion vue;
	private Etat etat;
	private CommunicationUDP comUDP;
	private String username;
	private SQLiteManager sqlManager;
	private VueStandard vueStd;
	
	public ControleurConnexion(VueConnexion vue) {
		this.vue = vue;
		this.etat = Etat.DEBUT;
		this.username = "";
		this.sqlManager = new SQLiteManager(0);
		this.vueStd = null;
		//Pour les tests, changer pour un truc plus general quand on change CommunicationUDP
		
		try {
			this.comUDP = new CommunicationUDP();	
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
				this.vue.removePasswordPanel();
				
				this.vue.setTextUsernameField("Veuillez entrer votre pseudonyme");
				this.vue.resetUsernameField();
				inputOK=false;
			}
			else {
				this.vue.setConnexionInfo("Identifiant ou mot de passe invalide, veuillez r�essayer");
				this.vue.resetPasswordField();
			}
			
		}
		else {
			pseudo = vue.getUsernameValue();
			
			//Recherche dans la liste locale des utilisateurs connectes, report sur inputOK
			inputOK = !this.comUDP.containsUserFromPseudo(pseudo);
			if(pseudo.equals("")) {
				this.vue.setConnexionInfo("Votre pseudonyme doit contenir au moins 1 carat�re");
			}else if (inputOK) {
				//Reglage de l'utilisateur
				try {
					Utilisateur.setSelf(this.username, pseudo, "localhost");
				} catch (UnknownHostException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}							
					this.comUDP.sendMessageInfoPseudo();
					
				try {
					this.resetView();
					this.vue.setVisible(false);
					this.setVueStandard();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			else this.vue.setConnexionInfo("Ce nom est d�j� utilis�, veuillez en choisir un autre");
		}
	}
	
	private void setVueStandard() throws IOException {
		if(this.vueStd == null) {
			this.vueStd = new VueStandard("Standard", this.comUDP, this.sqlManager, this.vue);
			
		}else {
			this.vueStd.initControleur();
			this.vueStd.setPseudoSelf();
			this.vueStd.setVisible(true);
		}
	}
	
	private void resetView() {
		this.etat = Etat.DEBUT;
		this.vue.addPasswordPanel();
		this.vue.resetPasswordField();
		this.vue.resetUsernameField();
		this.vue.setTextUsernameField("Nom d'utilisateur");
		this.vue.setConnexionInfo("");
		
	}
}
