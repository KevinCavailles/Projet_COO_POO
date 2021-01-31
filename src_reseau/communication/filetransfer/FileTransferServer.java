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


	public FileTransferServer(int nbFile, ObserverInputMessage obs) throws UnknownHostException, IOException {
		this.sockFTListen = ServerSocketChannel.open();
		this.sockFTListen.socket().bind(new InetSocketAddress(0));
		this.nbFile = nbFile;
		this.obsInput = obs;
	}

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