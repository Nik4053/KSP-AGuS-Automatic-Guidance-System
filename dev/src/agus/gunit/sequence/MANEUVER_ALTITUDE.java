package agus.gunit.sequence;

import agus.exception.AGuSUnknownOrbitException;
import agus.formula.FORMULA_ORBITAL;
import agus.gunit.ORBIT;
import nhlog.LOGGER;

/**
 * Will create an Maneuver that changes the altitude of an orbit, by simply
 * firering at perigee or apogee.
 * <p>
 * Make sure than the end orbit is possible the achieve with only one burn
 * starting at the start orbit
 * 
 * @author Niklas Heidenreich
 *
 */
public class MANEUVER_ALTITUDE extends MANEUVER {

	private ORBIT startOrbit, endOrbit;
	private ORBIT finalOrbit;
	private BURN burn;
	/**
	 * If this boolean is true there where errors while creating his maneuver
	 */
	private boolean errors;

	/**
	 * Will create an Maneuver that changes the altitude of an orbit, by simply
	 * firering at perigee or apogee.
	 * <p>
	 * Make sure than the end orbit is possible the achieve with only one burn
	 * starting at the start orbit
	 * 
	 * @param startORBIT
	 *            the orbit at which to start
	 * @param endORBIT
	 *            the orbit that should be reached
	 */
	public MANEUVER_ALTITUDE(ORBIT startORBIT, ORBIT endORBIT) {
		this.startOrbit = startORBIT;
		this.endOrbit = endORBIT;
		this.errors = checkForErrors();
		setup();
	}
	

	/**
	 * {@inheritDoc}
	 * 
	 * @return returns an cloned orbit. changing it will not affect this object
	 * @throws AGuSUnknownOrbitException the orbits calculation required not implemented formulas
	 */
	@Override
	public ORBIT getOrbit() throws AGuSUnknownOrbitException {
		if(!startOrbit.getBody().equals(endOrbit.getBody())){
			//if the bodys are different
			throw new AGuSUnknownOrbitException("Could not calculate the final orbit: "+ "Different Bodys: "+ startOrbit.getBody().getName() + " and "+ endOrbit.getBody().getName());
		}
		return new ORBIT(finalOrbit);
	}

	@Override
	public BURN getBurn() {
		return burn;
	}

	@Override
	public boolean errors() {
		return errors;
	}

	/*
	 * ________________________________________ private
	 */

	/**
	 * Will setup the object
	 */
	private void setup() {
		double rT = checkIfApOrPe();
		double bodyMass = 0;
		bodyMass = startOrbit.getBody().getMass();
		double dV = FORMULA_ORBITAL.VisVisaEquation(bodyMass, rT, endOrbit.getApoapsis(), endOrbit.getPeriapsis());
		dV -= FORMULA_ORBITAL.VisVisaEquation(bodyMass, rT, startOrbit.getApoapsis(), startOrbit.getPeriapsis());
		if (rT == endOrbit.getApoapsis()) {
			this.finalOrbit = new ORBIT(startOrbit);
			this.finalOrbit.setPeriapsis(endOrbit.getPeriapsis());
		} else {
			this.finalOrbit = new ORBIT(startOrbit);
			this.finalOrbit.setApoapsis(endOrbit.getApoapsis());
		}
		double ut = calcUT();
		this.finalOrbit.setUTatStart(ut);
		this.burn = new BURN(ut, dV, 0, 0);
	}

	/**
	 * checks if everything is ok with the given orbits
	 * <p>
	 * checks:
	 * <p>
	 * -needs more than one burn to change orbits
	 * <p>
	 * -both orbits are the same
	 * 
	 * @return true if there were errors
	 */
	private boolean checkForErrors() {
		boolean error = false;
		// Needs more than one burn to change orbits
		if (startOrbit.getApoapsis() != endOrbit.getApoapsis() && startOrbit.getApoapsis() != endOrbit.getPeriapsis()
				&& startOrbit.getPeriapsis() != endOrbit.getPeriapsis()
				&& startOrbit.getPeriapsis() != endOrbit.getApoapsis()) {
			LOGGER.logger.warning(
					"MANEUVER_ALTITUDE got start and endOrbits with different apoapsi and periapsi that requires to burns to change."
							+ " StartOrbit: ap=" + startOrbit.getApoapsis() + " pe=" + startOrbit.getPeriapsis()
							+ " EndOrbit: ap=" + endOrbit.getApoapsis() + " pe=" + endOrbit.getPeriapsis());
			error = true;
		}
		// Both orbits are the same
		if (startOrbit.getApoapsis() == endOrbit.getApoapsis()
				&& startOrbit.getPeriapsis() == endOrbit.getPeriapsis()) {
			LOGGER.logger.warning("Start and end orbit have the same qp and pe" + " ap: " + startOrbit.getApoapsis()
					+ " pe: " + startOrbit.getPeriapsis());
			error = true;
		}
		return error;
	}

	/**
	 * checks if the burn should happen at ap or pe
	 * <p>
	 * return NaN if an error occurs
	 * 
	 * @return the height at which to burn
	 */
	private double checkIfApOrPe() {
		if (startOrbit.getApoapsis() == endOrbit.getApoapsis()) {
			return startOrbit.getApoapsis();
		} else if (startOrbit.getApoapsis() == endOrbit.getPeriapsis()) {
			return startOrbit.getApoapsis();
		} else if (startOrbit.getPeriapsis() == endOrbit.getApoapsis()) {
			return startOrbit.getPeriapsis();
		} else if (startOrbit.getPeriapsis() == endOrbit.getPeriapsis()) {
			return startOrbit.getPeriapsis();
		} else {
			LOGGER.logger.warning("checkIfApOrPe() in MANEUVER ALTITUDE could not find a result");
			return Double.NaN;
		}
	}

	/**
	 * Calculates the time at which to burn
	 * 
	 * @return the met in seconds
	 */
	private double calcUT() {
		double UT = startOrbit.getUTatNextPeriapsis(startOrbit.getUTatStart());
		// UT+=startOrbit.getUTatStart();
		// if fire at apoapsis
		if (startOrbit.getApoapsis() == endOrbit.getApoapsis() || startOrbit.getApoapsis() == endOrbit.getPeriapsis()) {
			if (startOrbit.getUTatStart() > UT - startOrbit.getTimeForPeriod() / 2) {
				return UT + startOrbit.getTimeForPeriod() / 2;
			}
			UT -= startOrbit.getTimeForPeriod() / 2;
			if (UT < startOrbit.getUTatStart()) {
				UT += startOrbit.getTimeForPeriod();
			}
			return UT;
		} else {
			// if fire at periapsis
			return UT;
		}
	}

}
