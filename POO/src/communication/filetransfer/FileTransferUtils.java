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

	protected static final String DOWNLOADS_RELATIVE_PATH = "../downloads/";
	protected static final ArrayList<String> IMAGE_EXTENSIONS = new ArrayList<String>(List.of("tif","tiff","bmp","jpg","jpeg","gif", "png", "eps", "svg"));
	protected static final int KB_SIZE = 1024;
	
	
	protected static MessageFichier processMessageToDisplay(File file) throws IOException {
		String nameFile = file.getName();
		String extension = processFileExtension(nameFile); 
		TypeMessage type;
		String contenu;
		
		if(IMAGE_EXTENSIONS.contains(extension)) {
			type = TypeMessage.IMAGE;
			BufferedImage img = ImageIO.read(file);
			contenu = encodeImage(createThumbnail(img), extension) ;
			
		}else {
			type = TypeMessage.FICHIER;
			contenu = nameFile;
		}
		
		try {
			//return new MessageFichier(type, contenu, extension);
			return new MessageFichier(type, contenu, extension);
		} catch (MauvaisTypeMessageException e) {
			System.out.println("Should never go in");
			e.printStackTrace();
		}
		return null;
	}
	
	protected static String processFileExtension(String fileName) {
		String extension = "";
		
		int i = fileName.indexOf('.');
		if (i >= 0 || i != -1) {
		    extension = fileName.substring(i+1).toLowerCase();
		}
		return extension;
	}
	
	
	private static BufferedImage createThumbnail(BufferedImage image){
		float w = image.getWidth();
		float ratio = (w > 150) ? (150F/w) : 1;
		BufferedImage scaled = scale(image, ratio);
		return scaled;
	}
	
	private static String encodeImage(BufferedImage img, String extension) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(img, extension, bos);
		String imgString = Base64.getEncoder().encodeToString(bos.toByteArray());
		bos.close();
		return imgString;
	}
	
	
	public static BufferedImage decodeImage(String imageString) throws IOException {
		byte[] imgData = Base64.getDecoder().decode(imageString);
		InputStream is = new ByteArrayInputStream(imgData);
        BufferedImage img = ImageIO.read(is);
        is.close();
        return img;
	}
	
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
}
