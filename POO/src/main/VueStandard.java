package main;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

public class VueStandard extends Vue {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JList<String> activeUsersList;
	private JTextField pseudoSelf;
	private JButton modifierPseudo;
	private ControleurStandard c;
	public static DefaultListModel<String> userList = new DefaultListModel<String>();
	
	
	public VueStandard(String title, int port, int clientPort, int[] portsOther) throws IOException {
		super(title);
		
		JPanel main = new JPanel(new GridLayout(3, 1));
		
		this.c = new ControleurStandard(this, port, clientPort, portsOther);
		
		
		JPanel self = new JPanel(new GridLayout(1, 3));
		
		this.pseudoSelf = new JTextField(Utilisateur.getSelf().getPseudo());
		this.pseudoSelf.setEditable(false);
		
		this.modifierPseudo = new JButton("Modifier");
		this.modifierPseudo.addActionListener(c);
		
		self.add(new JLabel("Moi : "));
		self.add(this.pseudoSelf);
		self.add(this.modifierPseudo);
		
		
		this.activeUsersList = new JList<String>(VueStandard.userList);
		this.activeUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.activeUsersList.setLayoutOrientation(JList.VERTICAL);
		this.activeUsersList.addListSelectionListener(this.c);
		
		JScrollPane listScroller = new JScrollPane(this.activeUsersList);
		listScroller.setPreferredSize(new Dimension(50,50));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		listScroller.setBorder(BorderFactory.createCompoundBorder(
	            BorderFactory.createTitledBorder("Utilisateurs Actifs"), 
	            BorderFactory.createEmptyBorder(5,2,2,2)));
		
		main.add(self);
		main.add(listScroller);
		
		this.add(main);
		
		this.setSize(350,600);
		this.setVisible(true);
		
		this.addWindowListener(c);
	}
	
	public JList<String> getActiveUsersList(){
		return this.activeUsersList;
	}
	
	
	protected JButton getButtonModifierPseudo() {
		return this.modifierPseudo;
	}
	
	protected String getDisplayedPseudo() {
		return this.pseudoSelf.getText();
	}
	
	protected void setDisplayedPseudo(String pseudo) {
		this.pseudoSelf.setText(pseudo);
	}
	
	protected void toggleEditPseudo() {
		this.pseudoSelf.setEditable(!this.pseudoSelf.isEditable());
	}
	
}
