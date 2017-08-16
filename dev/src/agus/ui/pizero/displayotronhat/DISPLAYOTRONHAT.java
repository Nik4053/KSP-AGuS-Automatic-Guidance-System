package agus.ui.pizero.displayotronhat;

import java.util.logging.Level;

import nhlog.LOGGER;

import java.io.IOException;

/**
 * 
 * @author Jakob Emmerling, Niklas Heidenreich
 *
 */
public class DISPLAYOTRONHAT {
	/**
	 * Will create a string[] of size 3 filled with "null"
	 */
	//private static String[] lines = Collections.nCopies(3, "null").toArray(new String[3]);
	private static String[] lines = new String[] { "null", "null", "null" };

	/**
	 * Changes the content of one of the 3 lines of the display
	 * 
	 * @param line
	 *            the line that should be changed
	 * @param text
	 *            the new text
	 */
	public static void write(int line, String text) {
		if (line < 0 || line > 2) {
			LOGGER.logger.log(Level.SEVERE, "display line out of bounds", new IllegalArgumentException("" + line));
			return;
		}
		if (text.length() > 16) {
			LOGGER.logger.log(Level.SEVERE, "display text out of bounds", new IllegalArgumentException(text));
			text = text.substring(0, 16);
		}
		lines[line] = text;
		try {
			Runtime.getRuntime().exec("python write.py " + lines[0] + " " + lines[1] + " " + lines[2]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * clears the display
	 */
	public static void clear() {
		try {
			Runtime.getRuntime().exec("python write.py " + "null" + " " + "null" + " " + "null");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return the current display lines
	 */
	public static String[] getDisplayText() {
		return lines;
	}
}
