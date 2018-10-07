package imageprocessing;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import main.Picsi;

public class LinearHistogramCompensation implements IImageProcessor {

	@Override
	public boolean isEnabled(int imageType) {
		return imageType == Picsi.IMAGE_TYPE_GRAY;
	}

	@Override
	public Image run(Image input, int imageType) {
		ImageData inData = input.getImageData();
		linearHistogrammCompensation(inData, imageType); 						// doesn't influence input image
		return new Image(input.getDevice(), inData);
	}
	
	/**
	 * Invert image data
	 * @param imageData will be modified
	 * @param imageType
	 */
	public static void linearHistogrammCompensation(ImageData imageData, int imageType) {
		int[] hist = ImageProcessing.histogram(imageData, 256);		// Histogramm erstellen
		int K = hist.length; 										// Anzahl Intensitäts-Stufen
		int k1 = K-1; 												// maximale Intensität
		int n = imageData.width*imageData.height;					// Anzahl Pixel
		byte[] LUT = new byte[K]; 									// lookup table
		int hKum = 0;												// kumuliertes Histogramm

		for (int i = 0; i < K; i++) {
			hKum += hist[i];										// kumuliertes Histogramm berechnen
			LUT[i] = (byte) (hKum*k1/n);							// Histogrammausgleich auf LUT anwenden
		}
			
		ImageProcessing.applyLUT(imageData, LUT);					// LUT aufs Bild anwenden
	}
}

