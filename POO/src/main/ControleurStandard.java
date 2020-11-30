package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import communication.Communication;
import communication.CommunicationUDP;

public class ControleurStandard implements ActionListener, ListSelectionListener, WindowListener {

	private enum EtatModif {
		TERMINE, EN_COURS
	}

	private EtatModif etatModif;
	private VueStandard vue;
	private CommunicationUDP commUDP;
	private String lastPseudo;
	private int clientPort;

	public ControleurStandard(VueStandard vue, int portClient, int portServer, int[] portsOther) throws IOException {
		this.vue = vue;
		this.commUDP = new CommunicationUDP(portClient,portServer, portsOther);
		this.commUDP.sendMessageConnecte();
		this.commUDP.sendMessageAdd();
		this.etatModif = EtatModif.TERMINE;
	}

	//---------- LISTSELECTION LISTENER OPERATIONS ----------//
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			JList<String> list = vue.getActiveUsersList();
			System.out.println(list.getSelectedValue());
		}
	}

	
	//---------- ACTION LISTENER OPERATIONS ----------//
	@Override
	public void actionPerformed(ActionEvent e) {
		if ((JButton) e.getSource() == this.vue.getButtonModifierPseudo()) {
			JButton modifierPseudo = (JButton) e.getSource();

			if (this.etatModif == EtatModif.TERMINE) {
				this.lastPseudo = Utilisateur.getSelf().getPseudo();
				modifierPseudo.setText("OK");
				this.etatModif = EtatModif.EN_COURS;
			} else {

				if (!Communication.containsUserFromPseudo(this.vue.getDisplayedPseudo())) {

					Utilisateur.getSelf().setPseudo(this.vue.getDisplayedPseudo());

					try {
						this.commUDP.sendMessageModify();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				} else {
					this.vue.setDisplayedPseudo(this.lastPseudo);
				}

				modifierPseudo.setText("Modifier");
				this.etatModif = EtatModif.TERMINE;
			}

			this.vue.toggleEditPseudo();

		}
	}
	

	//---------- WINDOW LISTENER OPERATIONS ----------//

	@Override
	public void windowClosing(WindowEvent e) {
		
		try {
			this.commUDP.sendMessageDelete();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

}
