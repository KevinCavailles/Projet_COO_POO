package communication.filetransfer;


import java.io.File;

import java.io.IOException;
import java.net.UnknownHostException;

import java.util.ArrayList;

import observers.ObserverInputMessage;




public class FileTransferClient {
	
	private int port;
	private ArrayList<File> files = null;
	private ObserverInputMessage obsInput;


	public FileTransferClient(int port, ArrayList<File> filesToSend, ObserverInputMessage obs) throws UnknownHostException, IOException {
		this.port = port;
		this.files = filesToSend;
		this.obsInput = obs;
	}

	public void sendFiles() throws IOException, InterruptedException {
		for(File f: this.files) {
			FileTransferSendingThread ftc = new FileTransferSendingThread(this.port, f,this.obsInput);
			ftc.start();
			ftc.join();
		}
		
	}

}
