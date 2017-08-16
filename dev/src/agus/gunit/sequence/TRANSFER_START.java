package agus.gunit.sequence;

import java.util.LinkedList;
import agus.gunit.ORBIT;

/**
 * Needs to be at the start of every transfer list.
 * <p>
 * Contains the start Orbit and time needed for sequence generation
 * 
 * @author Niklas Heidenreich
 *
 */
public class TRANSFER_START extends TRANSFER {
	private ORBIT startOrbit;

	public TRANSFER_START(ORBIT orbit) {
		this.startOrbit = orbit;
	}

	@Override
	public double getDeltaV() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getUTstart() {
		// TODO Auto-generated method stub
		return startOrbit.getUTatStart();
	}

	@Override
	public double getUTend() {
		// TODO Auto-generated method stub
		return startOrbit.getUTatStart();
	}

	@Override
	public ORBIT getFinalOrbit() {
		// TODO Auto-generated method stub
		return startOrbit;
	}

	@Override
	public LinkedList<MANEUVER> getManeuverList() {
		// TODO Auto-generated method stub
		return null;
	}

}
