package files;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.PriorityQueue;

import javax.swing.JTextArea;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import imageprocessing.ImageProcessing;
import main.Picsi;

/**
 * Huffman file format and codec
 * @author Christoph Stamm
 *
 */
public class Huffman implements IImageFile {
	static class Node implements Comparable<Node>, Serializable {
		static final long serialVersionUID = 1;
		protected transient double m_p; 	// probability (not needed during decoding)
		protected transient long m_code;	// binary code; maximum 64 bits  (not needed during decoding)
		protected transient byte m_codeLen;	// binary code length (not needed during decoding)
		protected Node m_left, m_right;		// children

		public Node(double p) {
			m_p = p;
		}
		
		public Node(Node left, Node right) {
			m_left = left;
			m_right = right;
			m_p = left.m_p + right.m_p;
		}

		public double getProbability() {
			return m_p;
		}

		public long getCode() {
			return m_code;
		}

		public byte getCodeLen() {
			return m_codeLen;
		}

		public boolean isInnerNode() {
			return m_left != null;
		}
		
		public int compareTo(Node v) {
			if (v != null) {
				if (m_p < v.m_p) {
					return -1;
				} else if (m_p == v.m_p) {
					return 0;
				} else {
					return 1;
				}
			} else {
				return -1;
			}
		}

		public void setCode(long code, int codeLen) {
			m_code = code;
			m_codeLen = (byte)codeLen;

			if (m_left != null) {
				// 0-Bit
				m_left.setCode(code << 1, codeLen + 1);				
			}
			if (m_right != null) {
				// 1-Bit
				m_right.setCode((code << 1) + 1, codeLen + 1);
			}
		}
		
		public Node decodeBit(boolean bit) {
			return (bit) ? m_right : m_left;
		}
	}

	static class Leaf extends Node {
		static final long serialVersionUID = 1;
		private byte m_intensity;	// pixel intensity used during decoding
		
		public Leaf(double p, byte intensity) {
			super(p);
			m_intensity = intensity;
		}

		public byte getIntensity() {
			return m_intensity;
		}
	}

	@Override
	public Image read(String fileName, Display display) throws Exception {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));

		// read Header
		int width = in.readInt();
		int stride = ((width + 3)/4)*4;
		int height = in.readInt();
		
		// read code tree
		Node root = (Node)in.readObject();

		// read compressed data
		BitSet data = (BitSet)in.readObject();

		// close file
		in.close();
		
		// create palette
		RGB[] palette = new RGB[256];
		for (int i=0; i < palette.length; i++) {
			palette[i] = new RGB(i, i ,i);
		}
		
		// create image
		byte[] raw = new byte[stride*height];
		ImageData inData = new ImageData(width, height, 8, new PaletteData(palette), 4, raw); // stride is a multiple of 4 bytes

		// fill in data
		Node node;
		int index = 0;
		
		for(int v = 0; v < inData.height; v++) {
			for(int u = 0; u < inData.width; u++) {
				node = root;
				while(node.isInnerNode()) {
					node = node.decodeBit(data.get(index++));
				}
				inData.setPixel(u, v, ((Leaf)node).getIntensity());
			}
		}
		return new Image(display, inData);	
	}

	@Override
	public void save(String fileName, int fileType, Image image, int imageType) throws Exception {
		final ImageData outData = image.getImageData();
		if (ImageProcessing.determineImageType(outData) == Picsi.IMAGE_TYPE_RGB) return;

		final int w = outData.width;
		final int h = outData.height;
		final int size = w*h;
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));

		// huffman encoding
		int[] hist = ImageProcessing.histogram(outData, 256);
		Leaf[] codes = new Leaf[hist.length];
		Node root = createHuffmanTree(hist, codes, size);
		BitSet data = encodeImage(outData, codes);
		

		// TODO write Header
		out.writeInt(w);
		out.writeInt(h);
		
		// TODO write code tree
		out.writeObject(root);

		// TODO write compressed data
		out.writeObject(data);

		// close file
		out.close();
	}
	
	/**
	 * Build code tree
	 * @param hist histogram of input image
	 * @param codes code table
	 * @param size number of pixels
	 * @return root node of code tree
	 */
	private Node createHuffmanTree(int[] hist, Leaf[] codes, int size) {
		PriorityQueue<Node> pq = new PriorityQueue<Node>(hist.length);
        double ld = Math.log(2), H = 0, p;

		// compute probabilities and entropy
		for(int i=0; i < hist.length; i++) {
			p = (double)(hist[i])/size;
            	H -= ((p == 0) ? 0 : p*Math.log(p)/ld);
			codes[i] = new Leaf(p, (byte)i); 
			pq.add(codes[i]);
		}

		// estimate mean code length and needed file storage size
		System.out.println("gesch�tzte mittlere Codel�nge: [" + (float)H + ", " + (float)(H + 1) + ")");
		System.out.println("gesch�tzter Speicherbedarf: [" + (int)(H*size/8) + ", " + (int)Math.ceil((H + 1)*size/8) + ") Byte");

		// build Huffman tree
		while(pq.size() >= 2) {
			Node v1 = pq.poll();
			Node v2 = pq.poll();
			
			pq.add(new Node(v1, v2)); 
		}
		
		// get root node and create Huffman codes
		Node root = pq.poll();
		System.out.println("Probability p = " + (float)root.getProbability());
		root.setCode(0, 0);

		// compute mean code length and needed file storage size
		double sum = 0;
		for(int i=0; i < codes.length; i++) {
			sum += codes[i].getProbability()*codes[i].getCodeLen();
		}

		System.out.println("mittlere Codel�nge: " + (float)sum);
		System.out.println("Speicherbedarf: " + (int)Math.ceil(sum*size/8) + " Byte");

		return root;
	}

	/**
	 * Encode image
	 * @param ip
	 * @param codes code table
	 * @return encoded data
	 */
	private BitSet encodeImage(ImageData outData, Leaf[] codes) {
		final int w = outData.width;
		final int h = outData.height;
		BitSet bs = new BitSet();

		// TODO alle Pixel der Reihe nach codieren und im bs abspeichern
		int index = 0;
		Node node;
		long code;
		
		for(int v = 0; v < h; v++) {
			for(int u = 0; u < w; u++) {
				node = codes[outData.getPixel(u, v)];
				code = node.getCode();
				for(int i = node.getCodeLen() - 1; i >= 0; i--) {
					bs.set(index + i, code%2 == 1);
					code >>= 1;
				}
				index += node.getCodeLen();
			}
		}
		
		return bs;
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
