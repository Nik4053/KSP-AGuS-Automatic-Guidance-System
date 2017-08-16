package agus.exception;

/**
 * This exception is thrown if an MANEUVER, or other class that should calculate an orbit fails to do so.
 * <p>Note: It will not be thrown if an created orbit is invalid
 * @author Niklas Heidenreich
 *
 */
public class AGuSUnknownOrbitException extends Exception {
	public AGuSUnknownOrbitException() {
		super();
	}

	public AGuSUnknownOrbitException(String message) {
		super(message);
	}

	public AGuSUnknownOrbitException(String message, Throwable cause) {
		super(message, cause);
	}

	public AGuSUnknownOrbitException(Throwable cause) {
		super(cause);
	}

}
