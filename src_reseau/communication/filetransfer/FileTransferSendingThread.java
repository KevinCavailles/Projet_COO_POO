package communication.filetransfer;

import java.io.File;
import java.io.IOException;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
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

public class FileTransferSendingThread extends Thread{

	private SocketChannel sockTransfert;
	private File file;
	private ObserverInputMessage obsInput;

	public FileTransferSendingThread(InetAddress addrOther, int port, File fileToSend, ObserverInputMessage obs) throws IOException {
		SocketChannel sock = SocketChannel.open();
		SocketAddress addr = new InetSocketAddress(addrOther, port);
		sock.connect(addr);
		this.sockTransfert = sock;
		this.file = fileToSend;
		this.obsInput = obs;
	}

	public void run() {
		try {

			ByteBuffer fileData = ByteBuffer.allocate(4 * FileTransferUtils.KB_SIZE);

			ObjectOutputStream outputFileInformation = new ObjectOutputStream(
					this.sockTransfert.socket().getOutputStream());

			FileChannel fileReader = FileChannel.open(Paths.get(file.getPath()));
			String str = file.getName() + ";" + file.getTotalSpace();

			// Send file datas (name + size);
			outputFileInformation.writeObject(new MessageFichier(TypeMessage.FICHIER, str, ""));

			while (fileReader.read(fileData) > 0) {
				fileData.flip();
				this.sockTransfert.write(fileData);
				fileData.clear();
			}

			fileReader.close();
			
			Message mUpdate = FileTransferUtils.processMessageToDisplay(this.file);
			mUpdate.setSender("Moi");
			this.obsInput.update(this, mUpdate);
			
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
