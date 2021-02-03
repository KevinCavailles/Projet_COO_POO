package communication.filetransfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import messages.Message;
import messages.MessageFichier;
import observers.ObserverInputMessage;

public class FileTransferReceivingThread extends Thread {

	private SocketChannel sockTransfert;
	private ObserverInputMessage obsInput;

	/**
	 * Create the thread that will receive one file during a file transfer. This
	 * allows users to write in the chat while sending/receiving files.
	 * 
	 * @param sock 		The SocketChannel returned by ServerSocketChannel.accept().
	 * @param o    		The observer to notify once the file is fully received.
	 */
	public FileTransferReceivingThread(SocketChannel sock, ObserverInputMessage o) {
		this.sockTransfert = sock;
		this.obsInput = o;
	}

	public void run() {
		try {
			int nbByteRead = 0;

			// Buffer to receive a chunk of the file
			ByteBuffer fileData = ByteBuffer.allocate(4 * FileTransferUtils.KB_SIZE);

			// InputStream to read the first object which is a message containing the name
			// and size of the file
			ObjectInputStream inputFileInformation = new ObjectInputStream(
					this.sockTransfert.socket().getInputStream());

			int nbTotalBytesRead;

			nbTotalBytesRead = 0;

			Object o = inputFileInformation.readObject();
			MessageFichier m = (MessageFichier) o;
			String[] fileInfo = this.processFileInformation(m);
			String filePath = FileTransferUtils.DOWNLOADS_RELATIVE_PATH + fileInfo[0];
			long fileSize = Long.parseLong(fileInfo[1]);

			// OutputStream to create the file if it does not exist
			FileOutputStream fOutStream = new FileOutputStream(filePath);

			// Channel to write the data received in the file
			FileChannel fileWriter = fOutStream.getChannel();

			while (nbTotalBytesRead < fileSize && (nbByteRead = this.sockTransfert.read(fileData)) > 0) {
				fileData.flip();
				fileWriter.write(fileData);
				fileData.clear();

				nbTotalBytesRead += nbByteRead;

			}

			fileWriter.close();
			fOutStream.close();
			inputFileInformation.close();

			// Process the message to display (thumbnails in the case of images) and notify
			// the observer
			Message mUpdate = FileTransferUtils.processMessageToDisplay(new File(filePath));
			mUpdate.setSender("other");
			this.obsInput.updateInput(this, mUpdate);

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				this.sockTransfert.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Split the content of a message with the separator ";". This function is only
	 * used to read the name and the size of the file to receive.
	 * 
	 * @param m 	message containing the file's information (name and size).
	 * @return An array with the file's name and the file's size respectively.
	 */
	private String[] processFileInformation(MessageFichier m) {
		return m.getContenu().split(";");
	}

}
