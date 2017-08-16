package agus.gunit.sequence;

import java.util.LinkedList;
import agus.gunit.BODY;
import agus.gunit.ORBIT;
/**
 * Will use altitude changes to leave the current SOI into the soi of the parent body
 * @author Niklas Heidenreich
 *
 */
public class TRANSFER_LEAVE_SOI extends TRANSFER {
	/**
	 * The start and end orbits
	 */
	private ORBIT startOrbit, endOrbit;
	/**
	 * The list containing all maneuver for this transfer
	 */
	private LinkedList<MANEUVER> maneuverList;

	/**
	 * Will leave the current SOI and into the SOI of the next bigger Body
	 * <p>
	 * Note: This transfer is far from perfect and will just leave the soi of
	 * the current body in an inefficient way also because of this the final
	 * orbit method provided by this transfer will not return the correct final
	 * orbit
	 * <p>
	 * The use of this method is discouraged
	 * 
	 * @param StartOrbit
	 * @param EndOrbit
	 */
	public TRANSFER_LEAVE_SOI(ORBIT StartOrbit, ORBIT EndOrbit) {
		this.startOrbit = StartOrbit;
		this.endOrbit = EndOrbit;
		this.maneuverList = new LinkedList<MANEUVER>();
		setup();
		deleteEmptyManeuver();
	}

	private void setup() {
		ORBIT o1 = new ORBIT(startOrbit);
		BODY oldBody = startOrbit.getBody();
		BODY newBody = endOrbit.getBody();
		if (oldBody.getName().equals(newBody.getName())) {
			// Both orbits are around the same body
			return;
		}

		ORBIT bodyOrbit = oldBody.getOrbit();
		if (endOrbit.getPeriapsis() < bodyOrbit.getPeriapsis()) {
			// TODO
			o1.setApoapsis(oldBody.getSphereOfInfluence() * 1.01);
		} else {
			// TODO
			o1.setApoapsis(oldBody.getSphereOfInfluence() * 1.01);
		}
		MANEUVER m = new MANEUVER_ALTITUDE(startOrbit, o1);
		maneuverList.add(m);
	}

	private void deleteEmptyManeuver() {
		// TODO Auto-generated method stub

	}

	@Override
	public LinkedList<MANEUVER> getManeuverList() {
		// TODO Auto-generated method stub
		return maneuverList;
	}

}
