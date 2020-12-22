package standard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import communication.CommunicationUDP;
import communication.TCPServer;
import connexion.VueConnexion;
import main.Observer;
import main.Utilisateur;
import standard.VueStandard;
import session.VueSession;

public class ControleurStandard implements ActionListener, ListSelectionListener, WindowListener, Observer {

	private enum EtatModif {
		TERMINE, EN_COURS
	}

	private EtatModif etatModif;
	private VueStandard vue;
	private CommunicationUDP commUDP;
	private String lastPseudo;
	private TCPServer tcpServ;

	public ControleurStandard(VueStandard vue, CommunicationUDP commUDP, int portServerTCP) throws IOException {
		this.vue = vue;
		
		this.tcpServ = new TCPServer(portServerTCP);
		this.tcpServ.addObserver(this);
		this.tcpServ.start();
		
		this.commUDP = commUDP;
		this.commUDP.setObserver(this);
		this.commUDP.sendMessageConnecte();
		this.commUDP.sendMessageInfoPseudo();
		
		this.etatModif = EtatModif.TERMINE;
	}

	//---------- LISTSELECTION LISTENER OPERATIONS ----------//
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		//Cas o� un �l�ment de la liste est s�lectionn�
		if (this.vue.getActiveUsersList().isFocusOwner() && !e.getValueIsAdjusting()) {
			
			JList<String> list = this.vue.getActiveUsersList();
			String pseudo = list.getSelectedValue();
			
			int choix = this.vue.displayJOptionCreation(pseudo);
			System.out.println("choix : "+choix);
			if(choix == 0) {
				
				int tcpServerPort = this.commUDP.getPortFromPseudo(pseudo);
				System.out.println("port = "+tcpServerPort);
				try {
					
					//Send this user's pseudonyme through a TCP-client socket
					//to the TCP-server socket of the selected user
					Socket socketComm = new Socket(InetAddress.getLocalHost(), tcpServerPort);
					this.sendMessage(socketComm, Utilisateur.getSelf().getPseudo());
					
					//Wait for the answer, either "accepted or refused"
					String reponse = this.readMessage(socketComm);
					
					
					System.out.println("reponse : " + reponse);
					
					
					if(reponse.contains("accepted")) {
						
						this.vue.displayJOptionResponse("accept�e");
						this.vue.addSession(pseudo, new VueSession(socketComm));
						
					}else{
						this.vue.displayJOptionResponse("refus�e");
						socketComm.close();
						System.out.println("refused");
					}
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			
			this.vue.getButtonModifierPseudo().requestFocus();
			System.out.println("pseudo de la personne a atteindre : " + pseudo);
			
		}
	}

	
	//---------- ACTION LISTENER OPERATIONS ----------//
	@Override
	public void actionPerformed(ActionEvent e) {
		
		//Cas Modifier Pseudo
		if ((JButton) e.getSource() == this.vue.getButtonModifierPseudo()) {
			JButton modifierPseudo = (JButton) e.getSource();

			if (this.etatModif == EtatModif.TERMINE) {
				this.lastPseudo = Utilisateur.getSelf().getPseudo();
				modifierPseudo.setText("OK");
				this.etatModif = EtatModif.EN_COURS;
			} else {

				if (!this.commUDP.containsUserFromPseudo(this.vue.getDisplayedPseudo())) {

					Utilisateur.getSelf().setPseudo(this.vue.getDisplayedPseudo());

					try {
						this.commUDP.sendMessageInfoPseudo();
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
		
		
		
		//Cas deconnexion
		else if((JButton) e.getSource() == this.vue.getButtonDeconnexion() ) {
			try {
				this.commUDP.sendMessageDelete();
				this.commUDP.removeAll();
				this.vue.removeAllUsers();
				Utilisateur.resetSelf();
				vue.dispose();
				new VueConnexion(5);
//				this.vue.toggleEnableButtonConnexion();
//				this.vue.toggleEnableButtonDeconnexion();
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
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

	@Override
	public void update(Object o, Object arg) {
		
		if(o == this.tcpServ) {
			
			Socket sockAccept = (Socket) arg;
			
			try {
				
				String pseudo = this.readMessage(sockAccept);
						
				int reponse = this.vue.displayJOptionDemande(pseudo);
				
				System.out.println("reponse : " + reponse);
				
				if(reponse == 0) {
					this.sendMessage(sockAccept, "accepted");
					this.vue.addSession(pseudo, new VueSession(sockAccept));
				}else {
					this.sendMessage(sockAccept, "refused");
					sockAccept.close();
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(o == this.commUDP) {
			ArrayList<Utilisateur> users = (ArrayList<Utilisateur>) arg;
			ArrayList<String> pseudos = new ArrayList<String>();
			for (Utilisateur u : users) {
				pseudos.add(u.getPseudo());
			}
			this.vue.setActiveUsersList(pseudos);
		}
		
	}
	
	
	private void sendMessage(Socket sock, String message) throws IOException {
		PrintWriter output= new PrintWriter(sock.getOutputStream(), true);
			output.println(message);
	}
	
	private String readMessage(Socket sock) throws IOException {
		
		BufferedReader input = new BufferedReader(new InputStreamReader( sock.getInputStream() ));
		char buffer[] = new char[25];
		input.read(buffer);
		
		String reponse = new String(buffer).split("\n")[0];
		return reponse;
	}

}
