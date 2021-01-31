package communication.filetransfer;


import java.io.File;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.ArrayList;

import observers.ObserverInputMessage;




public class FileTransferClient {
	
	private int portOther;
	private InetAddress addrOther;
	private ArrayList<File> files = null;
	private ObserverInputMessage obsInput;


	public FileTransferClient(InetAddress addrOther, int portOther, ArrayList<File> filesToSend, ObserverInputMessage obs) throws UnknownHostException, IOException {
		this.addrOther = addrOther;
		this.portOther = portOther;
		this.files = filesToSend;
		this.obsInput = obs;
	}

	public void sendFiles() throws IOException, InterruptedException {
		for(File f: this.files) {
			FileTransferSendingThread ftc = new FileTransferSendingThread(this.addrOther, this.portOther, f,this.obsInput);
			ftc.start();
			ftc.join();
		}
		
	}

}
