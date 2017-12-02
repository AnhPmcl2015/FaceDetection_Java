package Utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Mat;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public final class Utils {
	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 * 
	 * @param frame
	 * 			the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	
	public static Image mat2Image(Mat frame) {
		try {
			return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
		}
		catch(Exception e) {
			System.err.println("Cannot convert the Mat object: " + e);
			return null;
		}
	}

	/**
	 * Generic methos for putting element running on a non-JavaFX thread on the JavaFX thread, to property update the UI
	 * 
	 * @param property
	 * 			a {@link ObjectProperty}
	 * @return value	
	 * 			the value to set for the given {@link Object property}
	 */
	
	public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
		Platform.runLater(()->{
			property.set(value);
		});
	}
	
	
	/**
	 * Support for the {@link mat2imge()} method
	 * 
	 * @param original
	 * 			the {@link Mat} object in BGR or Grayscale
	 * @return the corresponding {@link BufferedImage}
	 */
	
	public static BufferedImage matToBufferedImage(Mat original) {
		// TODO Auto-generated method stub
		// Init
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		
		byte[] sourcePixel = new byte[width*height*channels];
		original.get(0, 0, sourcePixel);
		
		if(original.channels() > 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		}
		else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		
		final byte[] targetPixel = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		
		
		System.arraycopy(sourcePixel, 0, targetPixel, 0, sourcePixel.length);
		return image;
	}
	
	
}
