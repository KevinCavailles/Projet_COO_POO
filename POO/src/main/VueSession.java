package main;

import java.awt.*;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

public class VueSession extends Vue {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private JButton envoyerMessage;
	private ControleurSession c;

	public VueSession(String title) throws IOException {
		
		super(title);
		
		JPanel main = new JPanel(new BorderLayout());
		main.setBackground(Color.green);
		
		
		JTextArea chatWindow  = new JTextArea();
		
		
		JScrollPane chatScroll = new JScrollPane();
		chatScroll.setPreferredSize(new Dimension(575, 600));
		chatScroll.setBackground(Color.blue);
		
		JTextField chatInput = new JTextField("Entrez votre message");
		chatInput.setPreferredSize(new Dimension(575, 150));
		

		
		
		
		
		this.c = new ControleurSession(this);
		
		
		
		
		GridBagConstraints gridBagConstraint = new GridBagConstraints();
		
		gridBagConstraint.fill = GridBagConstraints.BOTH;
		gridBagConstraint.gridx = 0;
		gridBagConstraint.gridy = 0;
		gridBagConstraint.gridwidth = 1;
		gridBagConstraint.gridheight = 5;
		gridBagConstraint.weightx = 0.33;
		gridBagConstraint.weighty = 1;
		
		//main.add(left,gridBagConstraint);
		
		gridBagConstraint.fill = GridBagConstraints.BOTH;
		gridBagConstraint.gridx = 1;
		gridBagConstraint.gridy = 0;
		gridBagConstraint.gridwidth = 2;
		gridBagConstraint.gridheight = 3;
		gridBagConstraint.weightx = 0.66;
		gridBagConstraint.weighty = 0.66;
		
		//main.add(chat,gridBagConstraint);
		
		gridBagConstraint.fill = GridBagConstraints.BOTH;
		gridBagConstraint.gridx = 1;
		gridBagConstraint.gridy = 3;
		gridBagConstraint.gridwidth = 2;
		gridBagConstraint.gridheight = 1;
		gridBagConstraint.weightx = 0.66;
		gridBagConstraint.weighty = 0.33;
		
		
		//main.add(bottom,gridBagConstraint);
		
		this.add(main);
		
		this.setSize(900,900);
		this.setVisible(true);
		
	}
	
	
	protected JButton getButtonEnvoyer() {
		return this.envoyerMessage;
	}
	
}
