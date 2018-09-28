package files;

import java.io.RandomAccessFile;

import javax.swing.JTextArea;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * AOS Raw image file implementation (read only)
 * 
 * @author Christoph Stamm
 *
 */
public class Raw implements IImageFile {
	@Override
	public Image read(String fileName, Display display) throws Exception {
		RandomAccessFile raf = new RandomAccessFile(fileName, "r");
		
		try {
			// read header (1024 bytes)
			// TODO: jump to file position 468, read width and height as 2 ints in little-endian format,
			// convert these ints to big-endian format and compute stride (bytes per line, multiple of 4)
			raf.seek(468);
			int width = raf.readInt();
			int height = raf.readInt();
			
			width = Integer.reverseBytes(width);
			height = Integer.reverseBytes(height);
			
			int stride = (width-1)/4*4+4;
			
			// read raw data
			byte[] raw = new byte[stride*height];			
			raf.seek(1024);
			raf.read(raw);
			
			// create palette
			RGB[] palette = new RGB[256];
			for (int i=0; i < palette.length; i++) {
				palette[i] = new RGB(i, i ,i);
			}
			
			// create image
			return new Image(display, new ImageData(width, height, 8, new PaletteData(palette), 4, raw));	// stride is a multiple of 4 bytes
		
		} finally {
			raf.close();			
		}
	}

	@Override
	public void save(String fileName, int fileType, Image image, int imageType) throws Exception {
		// not implemented
	}

	@Override
	public void displayTextOfBinaryImage(Image image, JTextArea text) {
		ImageData imageData = image.getImageData();
		
		text.append("P2");
		text.append("\n" + imageData.width + " " + imageData.height);
		text.append("\n255\n");
		PNM.writePGM(image, text, 255);
	}

	@Override
	public boolean isBinaryFormat() {
		return true;
	}
}
