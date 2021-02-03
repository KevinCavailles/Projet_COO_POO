package communication.filetransfer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import observers.ObserverInputMessage;

public class FileTransferServer extends Thread {

	private ServerSocketChannel sockFTListen;
	private int nbFile;
	private ObserverInputMessage obsInput;

	/**
	 * Create a server to transfer one or several files. A new socket and thread is
	 * created for each file to receive. The files are received one by one to save
	 * bandwidth and avoid issues.
	 * 
	 * @param nbFile 	The number of file to receive.
	 * @param o      	The observer to notify once a file is fully received.
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public FileTransferServer(int nbFile, ObserverInputMessage o) throws UnknownHostException, IOException {
		this.sockFTListen = ServerSocketChannel.open();
		this.sockFTListen.socket().bind(new InetSocketAddress(0));
		this.nbFile = nbFile;
		this.obsInput = o;
	}

	/**
	 * @return The port binded to the ServerSocketChannel.
	 */
	public int getPort() {
		return this.sockFTListen.socket().getLocalPort();
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < this.nbFile; i++) {
				SocketChannel sock = this.sockFTListen.accept();

				Thread ft = new FileTransferReceivingThread(sock, this.obsInput);
				ft.start();
				ft.join();
			}

			this.sockFTListen.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}