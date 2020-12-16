package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;


import communication.*;

public class ControleurConnexion implements ActionListener, Observer{

	private enum Etat {DEBUT, ID_OK};
	
	private VueConnexion vue;
	private Etat etat;
	private CommunicationUDP comUDP;
	private String id;
	private String pseudo;
	
	public ControleurConnexion(VueConnexion vue, int numtest) {
		this.vue = vue;
		this.etat = Etat.DEBUT;
		this.id="";
		//Pour les tests, changer pour un truc plus général quand on change CommunicationUDP
		try {
			switch(numtest) {
			case 0 : 
				this.comUDP = new CommunicationUDP(2208, 2209, new int[] {2309, 2409});
				break;
			case 1 :
				this.comUDP = new CommunicationUDP(2308, 2309, new int[] {2209, 2409});
				break;
			case 2 :
				this.comUDP = new CommunicationUDP(2408, 2409, new int[] {2209, 2309});
				break;
			default :
				this.comUDP = new CommunicationUDP(2408, 2409, new int[] {2209, 2309});
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
			pseudo=vue.getValeurTextField();
			
			//Recherche dans la liste locale des utilisateurs connectes, report sur inputOK
			inputOK = !comUDP.containsUserFromPseudo(pseudo);
			
			if (inputOK) {
				//Reglage de l'utilisateur
				try {
					Utilisateur.setSelf(id, pseudo, "localhost");
				} catch (UnknownHostException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				//Broadcast du pseudo
				try {
					comUDP.sendMessageInfoPseudo();
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					vue.close();
					new VueStandard("Standard", comUDP);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else vue.setTexteLabelInput("Ce nom est déjà utilisé, veuillez en choisir un autre");
		}
	}

	@Override
	public void update(Object o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
