package agus.gunit.execution;

import agus.exception.AGuSDataException;
import agus.gunit.sequence.SEQUENCE;

/**
 * An interface that should be used by every Execution
 * 
 * @author Niklas Heidenreich
 *
 */
public interface EXECUTION {
	/**
	 * Will run the execution
	 */
	public abstract void run() throws InterruptedException, AGuSDataException;

	/**
	 * The sequence the program wants to do next
	 * 
	 * @return the sequence object
	 */
	public abstract SEQUENCE getSequence();
}
