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
	
	public FileTransferReceivingThread(SocketChannel sock, ObserverInputMessage obs) {
		this.sockTransfert = sock;
		this.obsInput = obs;
	}

	public void run() {
		try {
			int nbByteRead = 0;
			ByteBuffer fileData = ByteBuffer.allocate(4 * FileTransferUtils.KB_SIZE);

			ObjectInputStream inputFileInformation = new ObjectInputStream(
					this.sockTransfert.socket().getInputStream());

			int nbTotalBytesRead;

			nbTotalBytesRead = 0;

			Object o = inputFileInformation.readObject();
			MessageFichier m = (MessageFichier) o;
			String[] fileInfo = this.processFileInformation(m);
			String filePath = FileTransferUtils.DOWNLOADS_RELATIVE_PATH + fileInfo[0];
			long fileSize = Long.parseLong(fileInfo[1]);
			

			FileOutputStream fOutStream = new FileOutputStream(filePath);

			FileChannel fileWriter = fOutStream.getChannel();

			while (nbTotalBytesRead < fileSize && (nbByteRead = this.sockTransfert.read(fileData)) > 0) {
				fileData.flip();
				fileWriter.write(fileData);
				fileData.clear();

				nbTotalBytesRead += nbByteRead;

			}

			fileWriter.close();
			fOutStream.close();
			
			Message mUpdate = FileTransferUtils.processMessageToDisplay(new File(filePath));
			mUpdate.setSender("other");
			this.obsInput.update(this, mUpdate);
			

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

	private String[] processFileInformation(MessageFichier m) {
		return m.getContenu().split(";");
	}

}
