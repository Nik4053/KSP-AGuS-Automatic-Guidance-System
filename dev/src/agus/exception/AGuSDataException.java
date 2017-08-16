package agus.exception;

/**
 * An exception thrown if there are inconsistency's/ errors while reading the
 * krpc data
 * 
 * @author Niklas Heidenreich
 *
 */
public class AGuSDataException extends Exception {
	public AGuSDataException() {
		super();
	}

	public AGuSDataException(String message) {
		super(message);
	}

	public AGuSDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public AGuSDataException(Throwable cause) {
		super(cause);
	}
}
