package imageprocessing;

import main.Picsi;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;

import utils.Parallel;

/**
 * Image inverter
 * @author Christoph Stamm
 *
 */
public class Inverter implements IImageProcessor {

	@Override
	public boolean isEnabled(int imageType) {
		return true;
	}

	@Override
	public Image run(Image input, int imageType) {
		ImageData inData = input.getImageData();
		invert(inData, imageType); // doesn't influence input image
		return new Image(input.getDevice(), inData);
	}

	/**
	 * Invert image data
	 * @param imageData will be modified
	 * @param imageType
	 */
	public static void invert(ImageData imageData, int imageType) {
		if (imageType == Picsi.IMAGE_TYPE_RGB) {
			// change pixel colors
			/* sequential image loop 
			for(int v=0; v < imageData.height; v++) {
				for (int u=0; u < imageData.width; u++) {
					int pixel = imageData.getPixel(u,v);
					RGB rgb = imageData.palette.getRGB(pixel);
					rgb.red   = 255 - rgb.red;
					rgb.green = 255 - rgb.green;
					rgb.blue  = 255 - rgb.blue;
					imageData.setPixel(u, v, imageData.palette.getPixel(rgb));
				}
			}
			*/
			// parallel image loop
			Parallel.For(0, imageData.height, v -> {
				for (int u=0; u < imageData.width; u++) {
					RGB rgb = imageData.palette.getRGB(imageData.getPixel(u,v));
					rgb.red   = 255 - rgb.red;
					rgb.green = 255 - rgb.green;
					rgb.blue  = 255 - rgb.blue;
					imageData.setPixel(u, v, imageData.palette.getPixel(rgb));
				}
			});
		} else {
			/*
			// change palette
			RGB[] palette = imageData.getRGBs();
			for (int i=0; i < palette.length; i++) {
				RGB rgb = palette[i];
				rgb.red   = 255 - rgb.red;
				rgb.green = 255 - rgb.green;
				rgb.blue  = 255 - rgb.blue;
			}
			
			// direct image byte access
			byte[] data = imageData.data;
			
			for(int i=0; i < data.length; i++) {
				data[i] = (byte)~data[i];
			}
			*/
			
			// parallel image loop
			Parallel.For(0, imageData.height, v -> {
				for (int u=0; u < imageData.width; u++) {
					int pixel = imageData.getPixel(u,v);
					imageData.setPixel(u, v, 255 - pixel);
				}
			});
		}
	}
}
