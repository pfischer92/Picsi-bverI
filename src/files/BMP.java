package files;
import javax.swing.JTextArea;

import main.Picsi;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import imageprocessing.ImageProcessing;

/**
 * Windows bitmaps (Windows image file type)
 * @author Christoph Stamm
 *
 */
public class BMP implements IImageFile {
	@Override
	public Image read(String fileName, Display display) {
		// Read the new image from the chosen file.
		return new Image(display, fileName);
	}

	@Override
	public void save(String fileName, int fileType, Image image, int imageType) {
		// Save the current image to the specified file.
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { image.getImageData() };
		loader.save(fileName, fileType);
	}

	@Override
	public boolean isBinaryFormat() {
		return true;
	}

	@Override
	public void displayTextOfBinaryImage(Image image, JTextArea text) {
		ImageData imageData = image.getImageData();
		int imageType = ImageProcessing.determineImageType(imageData);
		
		switch(imageType) {
		case Picsi.IMAGE_TYPE_BINARY:
			text.append("P1");
			text.append("\n" + imageData.width + " " + imageData.height);
			text.append("\n");
			PNM.writePBM(image, text);
			break;
		case Picsi.IMAGE_TYPE_GRAY:
			text.append("P2");
			text.append("\n" + imageData.width + " " + imageData.height);
			text.append("\n255\n");
			PNM.writePGM(image, text, 255);
			break;
		case Picsi.IMAGE_TYPE_RGB:
			text.append("P3");
			text.append("\n" + imageData.width + " " + imageData.height);
			text.append("\n255\n");
			PNM.writePPM(image, text, 255);
			break;
		}
	}
}
