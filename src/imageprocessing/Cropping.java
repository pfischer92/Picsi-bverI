package imageprocessing;

import gui.RectTracker;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Demo of image cropping
 * @author Christoph Stamm
 *
 */
public class Cropping implements IImageProcessor {

	@Override
	public boolean isEnabled(int imageType) {
		return true;
	}

	@Override
	public Image run(Image input, int imageType) {
		ImageData inData = input.getImageData();
		final int w = inData.width, h = inData.height;
		
		// let the user choose the ROI using a tracker
		RectTracker rt = new RectTracker();
		Rectangle r = rt.track(w/4, h/4, w/2, h/2);
		
		return new Image(input.getDevice(), ImageProcessing.crop(inData, r.x, r.y, r.width, r.height));
	}

}
