package main;

import javax.swing.JFrame;

public class Vue extends JFrame{

	public Vue(String title) {
		super(title);
	}
	
	public void reduireAgent() {}
	
	public void close() {
		this.dispose();
	}
}
