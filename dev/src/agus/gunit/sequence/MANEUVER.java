package agus.gunit.sequence;

import java.util.logging.Level;

import agus.exception.AGuSUnknownOrbitException;
import agus.gunit.ORBIT;
import nhlog.LOGGER;

/**
 * Creates an Orbit and Burn in order to get The vessel from one orbit to
 * another.
 * <p>
 * Make sure to give the Maneuver orbits that can be achieved only with the used
 * maneuver
 * 
 * @author Niklas Heidenreich
 *
 */
public abstract class MANEUVER {
	/**
	 * Gets the deltaV by looking at the burn object
	 * @return the deltaV needed
	 */
	public double getDeltaV()
	{
		return getBurn().getDeltaV();
	}

	/**
	 * The orbit that will be achieved after the burn.
	 * <p>
	 * Can differ from the orbit that was wanted to be achieved
	 * 
	 * @return orbit
	 */
	public abstract ORBIT getOrbit() throws AGuSUnknownOrbitException;

	/**
	 * 
	 * @return The burn object
	 */
	public abstract BURN getBurn();

	/**
	 * Time measured from UT at which to burn
	 * @return time
	 */
	public double getUT() {
		return getBurn().getTimeAtBurn();
	}
	/**
	 * Will return true if there where arrors while creating this Maneuver#
	 * <p> See log for further help what happend
	 * @return true if there where errors
	 */
	public abstract boolean errors();
}
