// http://www.eclipse.org/swt/javadoc.php

package main;
import gui.MainWindow;
import gui.TwinView;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import files.ImageFiles;

/**
 * Main method of the Picsi image viewer
 * 
 * @author Christoph Stamm
 *
 */
public class Picsi {
	public static final int IMAGE_TYPE_BINARY = 1;
	public static final int IMAGE_TYPE_GRAY = 2;
	public static final int IMAGE_TYPE_RGB = 4;
	public static final int IMAGE_TYPE_INDEXED = 8;

	public static final String APP_NAME = "FHNW Picsi";
	public static final String APP_VERSION = "2.0.2018.33";
	public static final String APP_COPYRIGHT = "Copyright \u00a9 " + new GregorianCalendar().get(Calendar.YEAR) + "\nUniversity of Applied Sciences Northwestern Switzerland\nFHNW School of Engineering, IMVS\nEfficient and Parallel Software\nWindisch, Switzerland\n\nhttp://www.fhnw.ch/imvs\n\nVersion ";
	
	public static Shell s_shell;
	
	public static void main(String[] args) {
		ImageFiles.registerUserImageFiles();
		Display display = new Display();
		MainWindow picsi = new MainWindow();
		s_shell = picsi.open(display);
		
		while (!s_shell.isDisposed())
			if (!display.readAndDispatch()) display.sleep();
		display.dispose();
	}

	/*
	 * Open an error dialog displaying the specified information.
	 */
	public static String createMsg(String msg, Object[] args) {
		MessageFormat formatter = new MessageFormat(msg);
		return formatter.format(args);
	}
	
	public static String createMsg(String msg, Object arg) {
		MessageFormat formatter = new MessageFormat(msg);
		return formatter.format(new Object[]{arg});
	}

	public static TwinView getTwinView() {
		Control c = s_shell.getChildren()[0];
		if (c instanceof TwinView) {
			return (TwinView)c;
		} else {
			return null;
		}
	}
}
