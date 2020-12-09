package standard;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
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
	private HashMap<JButton,VueSession> sessions;
	public static DefaultListModel<String> userList = new DefaultListModel<String>();
	
	
	public VueStandard(String title, int portClientUDP, int portServerUDP, int[] portsOther, int portServerTCP) throws IOException {
		super(title);
		
		this.sessions = new HashMap<JButton,VueSession>();
		this.c = new ControleurStandard(this, portClientUDP, portServerUDP, portsOther, portServerTCP);
		
		
		getContentPane().setLayout(new GridBagLayout());
		
		
		JPanel left = new JPanel(new BorderLayout());
		left.setBackground(Color.red);
		//left.setPreferredSize(new Dimension(200, 200));
		
		this.zoneSessions = new JTabbedPane();
		this.zoneSessions.setTabPlacement(JTabbedPane.BOTTOM);
		
		//JPanel defaultTab = new JPanel(new GridLayout(1,1));
	
		//JLabel noSession = new JLabel("Aucune session en cours");
		//noSession.setHorizontalAlignment(JLabel.CENTER);
		//defaultTab.add(noSession);
		
		//this.zoneSessions.addTab("1", defaultTab);
		
		this.zoneSessions.setBackground(Color.green);
		this.zoneSessions.setPreferredSize(new Dimension(600, 600));
		
		JPanel bottom = new JPanel(new GridLayout(1, 2));
		bottom.setBackground(Color.yellow);
		bottom.setPreferredSize(new Dimension(600, 100));
		
		
		
		//--------Panel haut pseudo--------//
		JPanel self = new JPanel(new FlowLayout());
		
		this.pseudoSelf = new JTextField(Utilisateur.getSelf().getPseudo());
		this.pseudoSelf.setPreferredSize(new Dimension(100, 20));
		this.pseudoSelf.setEditable(false);
		this.pseudoSelf.setFocusable(false);
		
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
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					modifierPseudo.doClick();
				}
			}
		});
		
		//--------Panel milieu liste utilisateurs--------//
		this.activeUsersList = new JList<String>(VueStandard.userList);
		this.activeUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.activeUsersList.setLayoutOrientation(JList.VERTICAL);
		this.activeUsersList.addListSelectionListener(this.c);
		
		System.out.println("listener ajouté");
		
		JScrollPane listScroller = new JScrollPane(this.activeUsersList);
		listScroller.setPreferredSize(new Dimension(50,50));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		listScroller.setBorder(BorderFactory.createCompoundBorder(
	            BorderFactory.createTitledBorder("Utilisateurs Actifs"), 
	            BorderFactory.createEmptyBorder(5,2,2,2)));
		
		
		//--------Panel bas deconnexion--------//
		JPanel deconnexion = new JPanel(new GridLayout(1, 2));
		
		this.seConnecter = new JButton("Se Connecter");
		this.seConnecter.setEnabled(false);
		this.seConnecter.addActionListener(this.c);
		
		this.seDeconnecter = new JButton("Se Déconnecter");
		this.seDeconnecter.addActionListener(this.c);
		
		deconnexion.add(this.seConnecter);
		deconnexion.add(this.seDeconnecter);
		
		//--------Ajout à la vue--------//
		left.add(self, BorderLayout.PAGE_START);
		left.add(listScroller, BorderLayout.CENTER);
		left.add(deconnexion, BorderLayout.PAGE_END);
		
		
		
		GridBagConstraints gridBagConstraint1 = new GridBagConstraints();
		GridBagConstraints gridBagConstraint2 = new GridBagConstraints();
		GridBagConstraints gridBagConstraint3 = new GridBagConstraints();
		
		gridBagConstraint1.fill = GridBagConstraints.BOTH;
		gridBagConstraint1.gridx = 0;
		gridBagConstraint1.gridy = 0;
		gridBagConstraint1.gridwidth = 1;
		gridBagConstraint1.gridheight = 4;
		gridBagConstraint1.weightx = 0.33;
		gridBagConstraint1.weighty = 1;
		
		getContentPane().add(left,gridBagConstraint1);
		
		gridBagConstraint2.fill = GridBagConstraints.BOTH;
		gridBagConstraint2.gridx = 1;
		gridBagConstraint2.gridy = 0;
		gridBagConstraint2.gridwidth = 2;
		gridBagConstraint2.gridheight = 3;
		gridBagConstraint2.weightx = 0.66;
		gridBagConstraint2.weighty = 0.66;
		
		getContentPane().add(this.zoneSessions,gridBagConstraint2);
		
		gridBagConstraint3.fill = GridBagConstraints.BOTH;
		gridBagConstraint3.gridx = 1;
		gridBagConstraint3.gridy = 3;
		gridBagConstraint3.gridwidth = 2;
		gridBagConstraint3.gridheight = 1;
		gridBagConstraint3.weightx = 0.66;
		gridBagConstraint3.weighty = 0.33;
		
		
		getContentPane().add(bottom,gridBagConstraint3);
		
		
		this.pack();
		this.setVisible(true);
		
		this.addWindowListener(c);
	}
	
	public JList<String> getActiveUsersList(){
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
	
	protected void setDisplayedPseudo(String pseudo) {
		this.pseudoSelf.setText(pseudo);
	}
	
	protected void toggleEditPseudo() {
		this.pseudoSelf.setEditable(!this.pseudoSelf.isEditable());
		this.pseudoSelf.setFocusable(!this.pseudoSelf.isFocusable());
	}
	
	protected void toggleEnableButtonDeconnexion() {
		this.seDeconnecter.setEnabled(!this.seDeconnecter.isEnabled());
	}
	
	protected void toggleEnableButtonConnexion() {
		this.seConnecter.setEnabled(!this.seConnecter.isEnabled());
	}
	
	protected int displayJOptionCreation(String pseudo) {
		return JOptionPane.showConfirmDialog(this, 
				"Voulez vous créer une session avec "+pseudo+" ?",
				"Confirmation session",
				JOptionPane.YES_NO_OPTION);
	}
	
	protected int displayJOptionDemande(String pseudo) {
		return JOptionPane.showConfirmDialog(this, 
				pseudo+" souhaite creer une session avec vous.",
				"Accepter demande",
				JOptionPane.YES_NO_OPTION);
	}
	
	protected void displayJOptionResponse(String reponse) {
		JOptionPane.showMessageDialog(this, "Demande de session "+reponse);
	}
	
	protected void addSession(String pseudo, VueSession session) {
		int nbTab = this.zoneSessions.getTabCount();
//		if(nbTab == 1) {
//			this.zoneSessions.removeTabAt(0);
//		}
		
		JPanel tabTitle = new JPanel();
		
		JButton closeTab = new JButton("X");
        closeTab.setToolTipText("close this tab");
        //Make the button looks the same for all Laf's
        closeTab.setUI(new BasicButtonUI());
        //Make it transparent
        closeTab.setContentAreaFilled(false);
		closeTab.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			 JButton button = (JButton) e.getSource();
			 boolean containsKey = sessions.containsKey(button);
			    if (containsKey) {
			    	zoneSessions.remove(zoneSessions.indexOfTabComponent(button.getParent() ) );
			    	sessions.remove(button);
			    }	
			}
		});
		
		
		tabTitle.add(new JLabel(pseudo));
		tabTitle.add(closeTab);
	
		this.zoneSessions.addTab(pseudo, session);
		this.zoneSessions.setTabComponentAt(this.zoneSessions.getTabCount()-1, tabTitle);
		
		this.sessions.put(closeTab, session);
		session.requestFocus();
		
	}
	
	
}
