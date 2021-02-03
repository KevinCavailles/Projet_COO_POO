package communication.filetransfer;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import messages.MessageFichier;
import messages.MauvaisTypeMessageException;
import messages.Message.TypeMessage;

public class FileTransferUtils {

	// Relative path of the folder where are put the received files
	protected static final String DOWNLOADS_RELATIVE_PATH = "../downloads/";
	protected static final ArrayList<String> IMAGE_EXTENSIONS = new ArrayList<String>(
			List.of("tif", "tiff", "bmp", "jpg", "jpeg", "gif", "png", "eps", "svg"));
	protected static final int KB_SIZE = 1024;

	/**
	 * Process what to display on the chat depending on the file sent/received. A
	 * thumbnail will be created in the case of an image, A String with the file's
	 * name will be created otherwise.
	 * 
	 * @param file 		The file to process.
	 * @return A message of which content is either a thumbnail or the file's name.
	 * @throws IOException
	 */
	protected static MessageFichier processMessageToDisplay(File file) throws IOException {
		String nameFile = file.getName();
		String extension = processFileExtension(nameFile);
		TypeMessage type;
		String contenu;

		if (IMAGE_EXTENSIONS.contains(extension)) {
			type = TypeMessage.IMAGE;
			BufferedImage img = ImageIO.read(file);
			contenu = encodeImage(createThumbnail(img), extension);

		} else {
			type = TypeMessage.FICHIER;
			contenu = nameFile;
		}

		try {
			return new MessageFichier(type, contenu, extension);
		} catch (MauvaisTypeMessageException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param fileName 		The name of the file (with its extension).
	 * @return The extension of the file.
	 */
	protected static String processFileExtension(String fileName) {
		String extension = "";

		int i = fileName.indexOf('.');
		if (i >= 0 || i != -1) {
			extension = fileName.substring(i + 1).toLowerCase();
		}
		return extension;
	}

	/**
	 * @param image 	A buffered image.
	 * @return A thumbnail of the image.
	 */
	private static BufferedImage createThumbnail(BufferedImage image) {
		float w = image.getWidth();
		float ratio = (w > 150) ? (150F / w) : 1;
		BufferedImage scaled = scale(image, ratio);
		return scaled;
	}

	/**
	 * @param img      		A buffered image.
	 * @param extension		The extension of the image.
	 * @return The base64 encoded string corresponding to the given image.
	 * @throws IOException
	 */
	private static String encodeImage(BufferedImage img, String extension) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(img, extension, bos);
		String imgString = Base64.getEncoder().encodeToString(bos.toByteArray());
		bos.close();
		return imgString;
	}

	/**
	 * @param imageString 	The base64 encoded string of an image.
	 * @return A buffered image corresponding to the given base64 encoded string.
	 * @throws IOException
	 */
	public static BufferedImage decodeImage(String imageString) throws IOException {
		byte[] imgData = Base64.getDecoder().decode(imageString);
		InputStream is = new ByteArrayInputStream(imgData);
		BufferedImage img = ImageIO.read(is);
		is.close();
		return img;
	}

	// Used to scale an image with a given ratio
	private static BufferedImage scale(BufferedImage source, double ratio) {
		int w = (int) (source.getWidth() * ratio);
		int h = (int) (source.getHeight() * ratio);
		BufferedImage bi = getCompatibleImage(w, h);
		Graphics2D g2d = bi.createGraphics();
		double xScale = (double) w / source.getWidth();
		double yScale = (double) h / source.getHeight();
		AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
		g2d.drawRenderedImage(source, at);
		g2d.dispose();
		return bi;
	}

	private static BufferedImage getCompatibleImage(int w, int h) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h);
		return image;
	}

	/**
	 * Create the folder with the path "DOWNLOADS_RELATIVE_PATH" if it does not
	 * exist.
	 */
	public static void createDownloads() {
		File downloads = new File(FileTransferUtils.DOWNLOADS_RELATIVE_PATH);

		if (!downloads.exists()) {
			downloads.mkdir();
		}
	}
}
