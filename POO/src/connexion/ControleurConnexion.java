package connexion;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;


import communication.*;
import main.Observer;
import main.Utilisateur;
import standard.VueStandard;

public class ControleurConnexion implements ActionListener, Observer{

	private enum Etat {DEBUT, ID_OK};
	
	private VueConnexion vue;
	private Etat etat;
	private CommunicationUDP comUDP;
	private String id;
	private String pseudo;
	private int portTCP;
	
	public ControleurConnexion(VueConnexion vue, int numtest) {
		this.vue = vue;
		this.etat = Etat.DEBUT;
		this.id="";
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		comUDP.setObserver(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		boolean inputOK = false;
		if (this.etat == Etat.DEBUT) {
			id=vue.getValeurTextField();
			
			//Recherche dans la liste des utilisateurs enregistres, report sur inputOK
			inputOK = (id.contentEquals("idvalide")||id.contentEquals("idv2"));
			
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
				vue.setTexteLabelInput("Veuillez entrer votre nom");
				vue.resetValeurTextField();
				inputOK=false;
			}
			else vue.setTexteLabelInput("Identifiant invalide, veuillez réessayer");
		}
		else {
			this.pseudo=vue.getValeurTextField();
			
			//Recherche dans la liste locale des utilisateurs connectes, report sur inputOK
			inputOK = !this.comUDP.containsUserFromPseudo(this.pseudo);
			if(this.pseudo.equals("")) {
				this.vue.setTexteLabelInput("Votre pseudonyme doit contenir au moins 1 caratère");
			}else if (inputOK) {
				//Reglage de l'utilisateur
				try {
					Utilisateur.setSelf(this.id, this.pseudo, "localhost", this.portTCP);
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
					new VueStandard("Standard", comUDP, this.portTCP);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else this.vue.setTexteLabelInput("Ce nom est déjà utilisé, veuillez en choisir un autre");
		}
	}

	@Override
	public void update(Object o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
