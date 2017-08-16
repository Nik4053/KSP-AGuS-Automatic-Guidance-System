package agus.gunit.sequence;

import java.util.LinkedList;
import java.util.ListIterator;

import agus.exception.AGuSUnknownOrbitException;
import agus.gunit.ORBIT;

/**
 * Make sure that the TRANSFER objects never contain empty Maneuver
 * 
 * @author Niklas Heidenreich
 *
 */
public abstract class TRANSFER {

	/**
	 * calc by getting the orbit of the last maneuver in the maneuver list
	 * 
	 * @return The orbit that will be achieved after the transfer
	 * @throws AGuSUnknownOrbitException 
	 */
	public ORBIT getFinalOrbit() throws AGuSUnknownOrbitException {
		return getManeuverList().getLast().getOrbit();
	}

	/**
	 * 
	 * @return the list containing all maneuver
	 */
	public abstract LinkedList<MANEUVER> getManeuverList();

	/**
	 * calculated by combining all deltaV of every maneuver
	 * 
	 * @return the deltaV needed for this transfer
	 */
	public double getDeltaV() {
		double dV = 0;
		ListIterator<MANEUVER> m = getManeuverList().listIterator();
		while (m.hasNext()) {
			dV += m.next().getDeltaV();
		}
		return dV;
	}

	/**
	 * 
	 * @return the met at the first maneuver
	 */
	public double getUTstart() {
		return getManeuverList().getFirst().getUT();
	}

	/**
	 * 
	 * @return the met at the last maneuver
	 */
	public double getUTend() {
		return getManeuverList().getLast().getUT();
	}

}
