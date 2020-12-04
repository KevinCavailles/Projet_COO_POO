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

public class ControleurSession implements ActionListener{

	
	private VueSession vue;

	public ControleurSession(VueSession vue) throws IOException {
		this.vue = vue;
	}


	
	//---------- ACTION LISTENER OPERATIONS ----------//
	@Override
	public void actionPerformed(ActionEvent e) {
		if ((JButton) e.getSource() == this.vue.getButtonEnvoyer()) {
			
		}
		
	}
}
	