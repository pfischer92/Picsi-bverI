package imageprocessing;

import javax.swing.JOptionPane;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import main.Picsi;

public class ModifiedGammaCorr implements IImageProcessor{

	@Override
	public boolean isEnabled(int imageType) {
		return imageType == Picsi.IMAGE_TYPE_GRAY;
	}

	@Override
	public Image run(Image input, int imageType) {
		double gamma = Double.parseDouble(JOptionPane.showInputDialog("Choose gamma: "));
		double x0 = Double.parseDouble(JOptionPane.showInputDialog("Choose x0: "));
		ImageData inData = input.getImageData();
		modGammaCorr(inData, imageType, gamma, x0); 						// doesn't influence input image
		return new Image(input.getDevice(), inData);
	}
	
	/**
	 * Invert image data
	 * @param imageData will be modified
	 * @param imageType
	 */
	public static void modGammaCorr(ImageData imageData, int imageType, double gamma, double x0) {
		final int K = 256; 													//Anzahl Intensit‰ten
		final int iMax = K - 1;
		final double gammaI = 1/gamma;											// sRGB
		//final double gamma = 1/2.222;	// ITU
		//final double x0 = 0.018;	// ITU

		byte[] LUT = new byte[K]; 											//Lookup Table

		// Vorberechnungen
		final double s = gammaI/(x0*(gammaI - 1) + Math.pow(x0, 1 - gammaI));
		final double d = 1/(Math.pow(x0, gammaI)*(gammaI - 1) + 1) - 1;

		// LUT erstellen
		double x;
		for(int i = 0; i < K; i++){
			x = (double)i/iMax;
			if (x <= x0) {
				LUT[i] = (byte) Math.round(s*x*iMax);
			} else {	
				LUT[i] = (byte) Math.round(((1 + d)*Math.pow(x, gammaI) - d)*iMax);
			}			
		}

		ImageProcessing.applyLUT(imageData, LUT);
	}
}
