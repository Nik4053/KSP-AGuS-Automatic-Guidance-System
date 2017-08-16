package agus.gunit.execution.assist;

import agus.exception.AGuSDataException;

/**
 * The assist interface that every assist should use.
 * @author Niklas Heidenreich
 *
 */
public interface ASSIST {
	/**
	 * Executes the current assist
	 */
	public void run() throws InterruptedException, AGuSDataException;
}
