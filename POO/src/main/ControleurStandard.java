package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ControleurStandard implements ActionListener, ListSelectionListener {

	private VueStandard vue;

	public ControleurStandard(VueStandard vue) {
		this.vue = vue;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			JList<String> list = vue.getActiveUsersList();
			System.out.println(list.getSelectedValue());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

}
