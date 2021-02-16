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
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import communication.tcp.TCPServer;
import communication.udp.CommunicationUDP;
import connexion.VueConnexion;
import database.SQLiteManager;
import main.Utilisateur;
import observers.ObserverInputMessage;
import observers.ObserverUserList;
import session.VueSession;

public class ControleurStandard
		implements ActionListener, ListSelectionListener, WindowListener, ObserverInputMessage, ObserverUserList {

	private enum ModifPseudo {
		TERMINE, EN_COURS
	}

	private ModifPseudo modifPseudo;
	private VueStandard vue;
	private CommunicationUDP commUDP;
	private String lastPseudo;
	private TCPServer tcpServ;
	private ArrayList<String> idsSessionEnCours;
	private SQLiteManager sqlManager;
	private VueConnexion vueConnexion;

	public ControleurStandard(VueStandard vue, CommunicationUDP commUDP, int portServerTCP, SQLiteManager sqlManager,
			VueConnexion vueConnexion) throws IOException {
		this.vue = vue;
		// Instruction to avoid closing the application when clicking the upper right
		// cross
		this.vue.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		this.vueConnexion = vueConnexion;

		// The TCP server waiting for session requests
		this.tcpServ = new TCPServer(portServerTCP);

		this.tcpServ.addObserver(this);
		this.tcpServ.start();

		// The UDP communication (server + userlist manager)
		this.commUDP = commUDP;

		// An array to store the usernames of the users a session exists at any point in
		// time.
		this.idsSessionEnCours = new ArrayList<String>();

		this.sqlManager = sqlManager;
	}

	// ---------- LISTSELECTION LISTENER OPERATIONS ----------//
	@Override
	public void valueChanged(ListSelectionEvent e) {

		// Case when a list element is selected
		if (this.vue.getActiveUsersList().isFocusOwner() && !e.getValueIsAdjusting()
				&& this.vue.getActiveUsersList().getSelectedValue() != null) {

			JList<String> list = this.vue.getActiveUsersList();
			String pseudoOther = list.getSelectedValue();
			Utilisateur other = this.commUDP.getUserFromPseudo(pseudoOther);
			String idOther = other.getId();

			// Check if we are already asking for a session/chatting with the person
			// selected
			// null condition because the list.clearSelection() generates an event
			if (!this.idsSessionEnCours.contains(idOther)) {

				int choix = this.vue.displayJOptionSessionCreation(pseudoOther);
				System.out.println("choix : " + choix);

				if (choix == 0) {

					int port = other.getPort();

					System.out.println("port = " + port);
					try {

						Socket socketComm = new Socket(InetAddress.getLocalHost(), port);

						this.sendMessage(socketComm, Utilisateur.getSelf().getPseudo());
						String reponse = this.readMessage(socketComm);

						System.out.println("reponse : " + reponse);

						if (reponse.equals("accepted")) {
							this.idsSessionEnCours.add(idOther);

							VueSession session = new VueSession(socketComm, idOther, pseudoOther, this.sqlManager);
							this.vue.addSession(pseudoOther, session);

							this.vue.displayJOptionResponse("acceptee");

						} else {
							this.vue.displayJOptionResponse("refusee");
							socketComm.close();
							System.out.println("refused");
						}

					} catch (IOException e1) {
					}
				}

			}

			list.clearSelection();
			System.out.println("pseudo de la personne a atteindre : " + pseudoOther);

		}
	}

	// ---------- ACTION LISTENER OPERATIONS ----------//
	@Override
	public void actionPerformed(ActionEvent e) {

		// Case change pseudo
		if ((JButton) e.getSource() == this.vue.getButtonModifierPseudo()) {
			JButton modifierPseudo = (JButton) e.getSource();

			if (this.modifPseudo == ModifPseudo.TERMINE) {
				this.lastPseudo = Utilisateur.getSelf().getPseudo();
				modifierPseudo.setText("OK");
				this.modifPseudo = ModifPseudo.EN_COURS;
			} else {

				if (this.vue.getDisplayedPseudo().length() >= 1
						&& !this.commUDP.containsUserFromPseudo(this.vue.getDisplayedPseudo().toLowerCase())) {

					Utilisateur.getSelf().setPseudo(this.vue.getDisplayedPseudo());

					try {
						this.commUDP.sendMessageInfoPseudo();
					} catch (IOException e1) {
					}

				} else {
					this.vue.setDisplayedPseudo(this.lastPseudo);
				}

				modifierPseudo.setText("Modifier");
				this.modifPseudo = ModifPseudo.TERMINE;
			}

			this.vue.toggleEditPseudo();
		}

		// Case logging off
		else if ((JButton) e.getSource() == this.vue.getButtonDeconnexion()) {
			try {
				this.setVueConnexion();
			} catch (IOException e1) {
			}
		}

		// Case close session
		else if (this.vue.isButtonTab(e.getSource())) {
			JButton button = (JButton) e.getSource();
			int index = this.vue.removeSession(button);
			this.idsSessionEnCours.remove(index);
		}
	}

	// ---------- WINDOW LISTENER OPERATIONS ----------//

	@Override
	public void windowClosing(WindowEvent e) {

		try {
			this.setVueConnexion();
		} catch (IOException e1) {
		}

	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	// ------------SOCKET-------------//

	private void sendMessage(Socket sock, String message) throws IOException {
		PrintWriter output = new PrintWriter(sock.getOutputStream(), true);
		output.println(message);
	}

	private String readMessage(Socket sock) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		return input.readLine();
	}

	// ------------OBSERVERS------------- //

	// Method called when there is a connection on the TCP server
	// This always means, in theory, that an user is asking to create a session
	@Override
	public void updateInput(Object o, Object arg) {

		if (o == this.tcpServ) {
			// TCP socket given from the TCP Server
			Socket sockAccept = (Socket) arg;

			try {

				// Read the other user's pseudo
				String pseudoOther = this.readMessage(sockAccept);
				String idOther = this.commUDP.getUserFromPseudo(pseudoOther).getId();

				int reponse;

				// Display the dialog box and wait for replay
				if (!this.idsSessionEnCours.contains(idOther)) {
					reponse = this.vue.displayJOptionAskForSession(pseudoOther);
					System.out.println("reponse : " + reponse);
				} else {
					reponse = 1;
				}

				// If the session is accepted
				// Create a new VueSession with the socket
				if (reponse == 0) {

					this.idsSessionEnCours.add(idOther);
					this.sendMessage(sockAccept, "accepted");

					VueSession session = new VueSession(sockAccept, idOther, pseudoOther, this.sqlManager);
					this.vue.addSession(pseudoOther, session);
				} else {
					this.sendMessage(sockAccept, "refused");
					sockAccept.close();
				}

			} catch (IOException e) {
			}
		}
	}

	// Method called when the userlist of the CommunicationUDP is updated
	@Override
	public void updateList(Object o, ArrayList<Utilisateur> userList) {

		if (o == this.commUDP) {
			// Get every pseudo from the userlist and give the pseudo's list to the view
			ArrayList<String> pseudos = new ArrayList<String>();
			for (Utilisateur u : userList) {
				pseudos.add(u.getPseudo());
			}
			this.vue.setActiveUsersList(pseudos);
		}
	}

	/**
	 * Send the system message DECONNECTE. Reset the userlist and the user data.
	 * Close all sessions. Set this view invisible and the connexion's view visible.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void setVueConnexion() throws UnknownHostException, IOException {
		this.commUDP.sendMessageDelete();
		this.commUDP.removeAllUsers();
		this.vue.removeAllUsers();
		this.vue.closeAllSession();
		this.idsSessionEnCours.clear();
		Utilisateur.resetSelf();
		this.commUDP.setObserver(null);

		this.vue.setVisible(false);
		this.vueConnexion.setVisible(true);
	}

	/**
	 * Set the controler as the observer of the commUDP to receive the updates on
	 * the userlist. Then send the system message JE_SUIS_CONNECTE and this
	 * application's user data
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	protected void init() throws UnknownHostException, IOException {
		this.commUDP.setObserver(this);
		this.commUDP.sendMessageConnecte();
		this.commUDP.sendMessageInfoPseudo();
		this.modifPseudo = ModifPseudo.TERMINE;
	}

}