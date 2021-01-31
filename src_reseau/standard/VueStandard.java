package standard;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicButtonUI;

import communication.udp.CommunicationUDP;
import connexion.VueConnexion;
import database.SQLiteManager;
import main.Utilisateur;
import main.Vue;
import session.VueSession;

public class VueStandard extends Vue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JList<String> activeUsersList;
	private JTextField pseudoSelf;
	private JTabbedPane zoneSessions;
	private JButton modifierPseudo;
	private JButton seConnecter;
	private JButton seDeconnecter;
	private ControleurStandard c;
	private ArrayList<JButton> tabButtons;
	private ArrayList<VueSession> sessions;
	private DefaultListModel<String> userList = new DefaultListModel<String>();

	public VueStandard(String title, CommunicationUDP commUDP, SQLiteManager sqlManager, VueConnexion vueConnexion) throws IOException {
		super(title);

		this.tabButtons = new ArrayList<JButton>();
		this.sessions = new ArrayList<VueSession>();
		this.c = new ControleurStandard(this, commUDP, sqlManager, vueConnexion);
		this.c.init();

		getContentPane().setLayout(new GridBagLayout());

		JPanel left = new JPanel(new BorderLayout());

		this.zoneSessions = new JTabbedPane();
		this.zoneSessions.setTabPlacement(JTabbedPane.BOTTOM);

		this.zoneSessions.setPreferredSize(new Dimension(600, 600));

		// --------Panel haut pseudo--------//
		JPanel self = new JPanel(new FlowLayout());

		this.pseudoSelf = new JTextField(Utilisateur.getSelf().getPseudo());
		this.pseudoSelf.setPreferredSize(new Dimension(100, 30));
		this.pseudoSelf.setEditable(false);
		this.pseudoSelf.setFocusable(false);
		this.pseudoSelf.setEnabled(false);

		this.modifierPseudo = new JButton("Modifier");
		this.modifierPseudo.addActionListener(this.c);

		self.add(new JLabel("Moi : "));
		self.add(this.pseudoSelf);
		self.add(this.modifierPseudo);
		this.pseudoSelf.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					modifierPseudo.doClick();
				}
			}
		});

		// --------Panel milieu liste utilisateurs--------//
		this.activeUsersList = new JList<String>(this.userList);
		this.activeUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.activeUsersList.setLayoutOrientation(JList.VERTICAL);
		this.activeUsersList.addListSelectionListener(this.c);

		System.out.println("listener ajouté");

		JScrollPane listScroller = new JScrollPane(this.activeUsersList);
		listScroller.setPreferredSize(new Dimension(50, 50));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		listScroller.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Utilisateurs Actifs"), BorderFactory.createEmptyBorder(5, 2, 2, 2)));

		// --------Panel bas deconnexion--------//
		JPanel deconnexion = new JPanel(new GridLayout(1, 2));

		this.seDeconnecter = new JButton("Se Déconnecter");
		this.seDeconnecter.addActionListener(this.c);
		deconnexion.add(this.seDeconnecter);
		
		if(Utilisateur.getSelf().getId() == "admin") {
			JButton addNewUser = new JButton("Ajouter un nouvel utilisateur");
			deconnexion.add(addNewUser);
		}

		// --------Ajout ï¿½ la vue--------//
		left.add(self, BorderLayout.PAGE_START);
		left.add(listScroller, BorderLayout.CENTER);
		left.add(deconnexion, BorderLayout.PAGE_END);

		GridBagConstraints gridBagConstraintLeft = new GridBagConstraints();
		GridBagConstraints gridBagConstraintSessions = new GridBagConstraints();

		gridBagConstraintLeft.fill = GridBagConstraints.BOTH;
		gridBagConstraintLeft.gridx = 0;
		gridBagConstraintLeft.gridy = 0;
		gridBagConstraintLeft.gridwidth = 1;
		gridBagConstraintLeft.gridheight = 4;
		gridBagConstraintLeft.weightx = 0.33;
		gridBagConstraintLeft.weighty = 1;

		getContentPane().add(left, gridBagConstraintLeft);

		gridBagConstraintSessions.fill = GridBagConstraints.BOTH;
		gridBagConstraintSessions.gridx = 1;
		gridBagConstraintSessions.gridy = 0;
		gridBagConstraintSessions.gridwidth = 2;
		gridBagConstraintSessions.gridheight = 4;
		gridBagConstraintSessions.weightx = 0.66;
		gridBagConstraintSessions.weighty = 1;

		getContentPane().add(this.zoneSessions, gridBagConstraintSessions);

		this.pack();
		this.setVisible(true);

		this.addWindowListener(c);
	}

	// ------------ GETTERS -------------//

	protected JList<String> getActiveUsersList() {
		return this.activeUsersList;
	}

	protected JButton getButtonModifierPseudo() {
		return this.modifierPseudo;
	}

	protected JButton getButtonDeconnexion() {
		return this.seDeconnecter;
	}

	protected JButton getButtonConnexion() {
		return this.seConnecter;
	}

	protected String getDisplayedPseudo() {
		return this.pseudoSelf.getText();
	}

	// ------------ SETTERS -------------//

	protected void setActiveUsersList(ArrayList<String> users) {
		this.removeAllUsers();
		this.userList.addAll(users);
	}

	protected void setDisplayedPseudo(String pseudo) {
		this.pseudoSelf.setText(pseudo);
	}
	
	public void setPseudoSelf() {
		this.setDisplayedPseudo(Utilisateur.getSelf().getPseudo());
	}

	// ------------ JOPTIONS -------------//

	protected int displayJOptionSessionCreation(String pseudo) {
		return JOptionPane.showConfirmDialog(this, "Voulez vous créer une session avec " + pseudo + " ?",
				"Confirmation session", JOptionPane.YES_NO_OPTION);
	}

	protected int displayJOptionAskForSession(String pseudo) {
		return JOptionPane.showConfirmDialog(this, pseudo + " souhaite créer une session avec vous.",
				"Accepter demande", JOptionPane.YES_NO_OPTION);
	}

	protected void displayJOptionResponse(String reponse) {
		JOptionPane.showMessageDialog(this, "Demande de session " + reponse);
	}

	// ------------ TOGGLEBUTTONS -------------//

	protected void toggleEditPseudo() {
		this.pseudoSelf.setEditable(!this.pseudoSelf.isEditable());
		this.pseudoSelf.setFocusable(!this.pseudoSelf.isFocusable());
		this.pseudoSelf.setEnabled(!this.pseudoSelf.isEnabled());
	}

	protected void toggleEnableButtonDeconnexion() {
		this.seDeconnecter.setEnabled(!this.seDeconnecter.isEnabled());
	}

	protected void toggleEnableButtonConnexion() {
		this.seConnecter.setEnabled(!this.seConnecter.isEnabled());
	}

	// ------------SESSION-------------//

	protected boolean isButtonTab(Object o) {
		return this.tabButtons.contains(o);
	}

	protected int removeSession(JButton button) {
		int index = this.tabButtons.indexOf(button);

		VueSession vue = this.sessions.get(index);
		vue.destroyAll();

		this.zoneSessions.remove(vue);
		this.sessions.remove(index);
		this.tabButtons.remove(index);

		return index;
	}

	protected void addSession(String pseudo, VueSession session) {
		JPanel tabTitle = new JPanel();

		TabButton closeTab = new TabButton();

		tabTitle.add(new JLabel(pseudo));
		tabTitle.add(closeTab);

		this.zoneSessions.addTab(pseudo, session);
		this.zoneSessions.setTabComponentAt(this.zoneSessions.getTabCount() - 1, tabTitle);

		this.tabButtons.add(closeTab);
		this.sessions.add(session);

		session.requestFocus();

	}
	
	protected synchronized int removeSession(VueSession vue) {
		int index = this.sessions.indexOf(vue);

		vue.destroyAll();

		this.zoneSessions.remove(vue);
		this.sessions.remove(index);
		this.tabButtons.remove(index);

		return index;
	}
	
	protected void closeAllSession() {
		Iterator<VueSession> it = this.sessions.iterator();
		while(it.hasNext()) {
			VueSession session = it.next();
			this.zoneSessions.remove(session);
			session.destroyAll();
			it.remove();
		}
		
		this.tabButtons.clear();
	}


	// ------------ OTHERS -------------//

	protected void removeAllUsers() {
		this.userList.removeAllElements();
	}
	
	public void initControleur() throws UnknownHostException, IOException {
		this.c.init();
	}

	// ------------- PRIVATE CLASSES FOR THE TABS BUTTON -------------//
	private class TabButton extends JButton {
		
		private static final long serialVersionUID = 1L;

		public TabButton() {
			int size = 17;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("close this tab");
			// Make the button looks the same for all Laf's
			setUI(new BasicButtonUI());
			// Make it transparent
			setContentAreaFilled(false);
			// No need to be focusable
			setFocusable(false);
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);

			addMouseListener(VueStandard.buttonMouseListener);
			// Making nice rollover effect
			// we use the same listener for all buttons
			addActionListener(c);
			setRolloverEnabled(true);
		}

		// we don't want to update UI for this button
		public void updateUI() {
		}

		// paint the cross
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			// shift the image for pressed buttons
			if (getModel().isPressed()) {
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.BLACK);
			if (getModel().isRollover()) {
				g2.setColor(Color.MAGENTA);
			}
			int delta = 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
			g2.dispose();
		}
	}

	private final static MouseListener buttonMouseListener = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
			}
		}

		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
			}
		}
	};

}