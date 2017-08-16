package agus.gunit.sequence;

import java.util.LinkedList;
import java.util.ListIterator;

import agus.exception.AGuSUnknownOrbitException;
import agus.gunit.ORBIT;

/**
 * Make sure that the sequence never contains empty Transfer
 * 
 * @author Niklas Heidenreich
 *
 */
public abstract class SEQUENCE {

	public double deltaV() {
		double deltaV = 0;
		ListIterator<TRANSFER> lIt = getTransferList().listIterator();
		while (lIt.hasNext()) {
			deltaV += lIt.next().getDeltaV();
		}
		return deltaV;
	}

	/**
	 * The number of maneuvers should be equal to the number of burns
	 * <p>
	 * calculated by adding the number of maneuvers of every single transfer
	 * 
	 * @return the number of maneuvers
	 */
	public int getNumberOfManeuvers() {
		int number = 0;
		ListIterator<TRANSFER> lIt = getTransferList().listIterator();
		while (lIt.hasNext()) {
			number += lIt.next().getManeuverList().size();
		}
		return number;
	}

	/**
	 * The number of maneuvers should be equal to the number of burns
	 * <p>
	 * If they are not equal than there are Maneuvers containing no change in
	 * deltaV and therefore no burn
	 * <p>
	 * calculated by adding the number of maneuvers of every single transfer
	 * 
	 * @return the number of maneuvers
	 */
	public int getNumberOfBurns() {
		int number = 0;
		ListIterator<TRANSFER> lIt = getTransferList().listIterator();
		while (lIt.hasNext()) {
			ListIterator<MANEUVER> lIm = lIt.next().getManeuverList().listIterator();
			while (lIm.hasNext()) {
				if (lIm.next().getDeltaV() > 0) {
					number++;
				}
			}
		}
		return number;
	}

	public abstract LinkedList<TRANSFER> getTransferList();

	public abstract ORBIT getFinalOrbit()  throws AGuSUnknownOrbitException;

	public abstract double getDeltaV();

	public abstract double getTimeNeeded();

	/**
	 * will update the sequence using the new orbit as new start
	 * 
	 * @param currentOrbit
	 */
	public abstract void update(ORBIT currentOrbit);
}
