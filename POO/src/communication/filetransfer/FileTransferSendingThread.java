package communication.filetransfer;

import java.io.File;
import java.io.IOException;

import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;

import messages.MauvaisTypeMessageException;
import messages.Message;
import messages.MessageFichier;
import messages.Message.TypeMessage;
import observers.ObserverInputMessage;

public class FileTransferSendingThread extends Thread {

	private SocketChannel sockTransfert;
	private File file;
	private ObserverInputMessage obsInput;

	/**
	 * Create the thread that will send one file during a file transfer. This allows
	 * users to write in the chat while sending/receiving files.
	 * 
	 * @param port       	The port on localhost to which the SocketChannel will connect.
	 * @param fileToSend 	The file to send.
	 * @param o          	The observer to notify once the file is fully received.
	 * @throws IOException if the socket's creation fails.
	 */
	public FileTransferSendingThread(int port, File fileToSend, ObserverInputMessage o) throws IOException {
		SocketChannel sock = SocketChannel.open();
		SocketAddress addr = new InetSocketAddress(port);
		sock.connect(addr);
		this.sockTransfert = sock;
		this.file = fileToSend;
		this.obsInput = o;
	}

	public void run() {
		try {

			// Buffer to send a chunk of the file
			ByteBuffer fileData = ByteBuffer.allocate(4 * FileTransferUtils.KB_SIZE);

			// OutputStream to write the first object which is a message containing the name
			// and size of the file
			ObjectOutputStream outputFileInformation = new ObjectOutputStream(
					this.sockTransfert.socket().getOutputStream());

			// Channel to read the data of the file
			FileChannel fileReader = FileChannel.open(Paths.get(file.getPath()));
			String str = file.getName() + ";" + file.getTotalSpace();

			// Send file data (name + size);
			outputFileInformation.writeObject(new MessageFichier(TypeMessage.FICHIER, str, ""));

			while (fileReader.read(fileData) > 0) {
				fileData.flip();
				this.sockTransfert.write(fileData);
				fileData.clear();
			}

			fileReader.close();
			outputFileInformation.close();

			// Process the message to display (thumbnails in the case of images) and notify
			// the observer
			Message mUpdate = FileTransferUtils.processMessageToDisplay(this.file);
			mUpdate.setSender("Moi");
			this.obsInput.updateInput(this, mUpdate);

		} catch (IOException | MauvaisTypeMessageException e) {
			e.printStackTrace();
		} finally {
			try {
				this.sockTransfert.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
