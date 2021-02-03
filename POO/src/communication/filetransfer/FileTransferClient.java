package communication.filetransfer;

import java.io.File;

import java.io.IOException;

import java.util.ArrayList;

import observers.ObserverInputMessage;

public class FileTransferClient {

	private int port;
	private ArrayList<File> files;
	private ObserverInputMessage obsInput;

	/**
	 * Create a client to transfer one or several files on the specified port of
	 * localhost. A new Thread is created for each file. The files are sent one by
	 * one to save bandwidth and avoid issues.
	 * 
	 * @param port        	The port of localhost on which to send the files.
	 * @param filesToSend 	The file(s) to send.
	 * @param o           	The observer to notify each time a file is fully sent.
	 */
	public FileTransferClient(int port, ArrayList<File> filesToSend, ObserverInputMessage o) {
		this.port = port;
		this.files = filesToSend;
		this.obsInput = o;
	}

	/**
	 * Try to send every file on localhost on the specified port with a new thread.
	 * An observer is passed to the thread and it is notified each time a file is
	 * fully sent.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void sendFiles() throws IOException, InterruptedException {
		for (File f : this.files) {
			FileTransferSendingThread ftc = new FileTransferSendingThread(this.port, f, this.obsInput);
			ftc.start();
			ftc.join();
		}

	}

}
