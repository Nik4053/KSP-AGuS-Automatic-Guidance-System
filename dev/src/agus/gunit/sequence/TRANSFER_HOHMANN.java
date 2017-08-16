package agus.gunit.sequence;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import agus.gunit.ORBIT;
/**
 * Will create a simple hohmann transfer 
 * @author Niklas Heidenreich
 *
 */
public class TRANSFER_HOHMANN extends TRANSFER {

	/**
	 * The start and end orbits
	 */
	private ORBIT startOrbit, endOrbit;
	/**
	 * The list containing all maneuver for this transfer
	 */
	private LinkedList<MANEUVER> maneuver;

	/**
	 * This class will create maneuvers in order to change the altitude of the
	 * apoapsis and the periapsis of the orbit to the given ones.
	 * <p>
	 * Make sure that the orbit
	 * 
	 * @param StartOrbit
	 *            the orbit at which to start
	 * @param EndOrbit
	 *            the orbit that should be achieved
	 */
	public TRANSFER_HOHMANN(ORBIT StartOrbit, ORBIT EndOrbit) {
		this.startOrbit = StartOrbit;
		this.endOrbit = EndOrbit;
		this.maneuver = new LinkedList<MANEUVER>();
		setup();
		deleteEmptyManeuver();
	}

	@Override
	public LinkedList<MANEUVER> getManeuverList() {
		return maneuver;
	}

	/*
	 * _____________________________________________________________ private
	 */
	/**
	 * Will setup the object
	 */
	private void setup() {
		double sAp = startOrbit.getApoapsis();
		double eAp = endOrbit.getApoapsis();
		ORBIT orbit = new ORBIT(startOrbit);
		//burn to apoapsis
		// if the new apoapsis is higher than the old one 
		if (eAp > sAp) {
			EndApBiggerThanStartAp(orbit);
		} else if (eAp < sAp) {
			//if its lower 
			StartApBiggerThanEndAp(orbit);
		} else if (eAp == sAp) {
			maneuver.add(new MANEUVER_ALTITUDE(orbit, endOrbit));
		}

	}

	/**
	 * Will delete Maneuvers that had errors while creating
	 */
	private void deleteEmptyManeuver() {
		List<Integer> li = new ArrayList<Integer>();
		ListIterator<MANEUVER> mi = getManeuverList().listIterator();
		while (mi.hasNext()) {
			if (mi.next().errors()) {
				mi.remove();
			}
		}

	}

	private void EndApBiggerThanStartAp(ORBIT StartOrbit) {
		double eAp = endOrbit.getApoapsis();
		ORBIT orbit = StartOrbit;
		ORBIT o = new ORBIT(StartOrbit);
		o.setApoapsis(eAp);
		maneuver.add(new MANEUVER_ALTITUDE(orbit, o));
		//burn to periapsis
		maneuver.add(new MANEUVER_ALTITUDE(o, endOrbit));
	}

	private void StartApBiggerThanEndAp(ORBIT StartOrbit) {
		ORBIT orbit = StartOrbit;
		ORBIT o = new ORBIT(StartOrbit);
		o.setPeriapsis(endOrbit.getPeriapsis());
		/*
		 * if(eAp>sPe){ o.setApoapsis(eAp); }else{ o.setApoapsis(sPe);
		 * o.setPeriapsis(eAp); }
		 */
		maneuver.add(new MANEUVER_ALTITUDE(orbit, o));
		maneuver.add(new MANEUVER_ALTITUDE(o, endOrbit));
	}
}
