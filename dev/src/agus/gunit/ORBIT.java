package agus.gunit;

import java.io.IOException;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import agus.formula.FORMULA_ORBITAL;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter.Orbit;
import nhlog.LOGGER;

/**
 * This class represents an Orbit.
 * <p>
 * The orbit represented does not have to be the orbit of an spacecraft, but can
 * also be the orbit of a planet.
 * <p>
 * If information/parameters are set that will never make an valid orbit this
 * class will not do anything to prevent that on its own.
 * <p>
 * If you need to check the orbit for validity use the checkForErrors() class
 * 
 * @author Niklas Heidenreich
 *
 */
public class ORBIT {

	private double apoapsis = 0;
	private double periapsis = 0;
	private double inclination = 0;
	private double meanAnomalyAtStart = 0;
	private double UTAtStart = 0;
	private BODY body = null;

	/**
	 * Empty orbit
	 */
	public ORBIT() {
	}

	/**
	 * Creates a new orbit object
	 * <p>
	 * Calls every set method of this class and sets it to the calculates of the
	 * given orbit
	 * 
	 * @param Orbit
	 *            the orbit
	 * @param Body
	 *            the body this orbit is around
	 * @param UTatStart
	 *            the met when the vessel enters the orbit the first time
	 * @throws AGuSDataException
	 */
	public ORBIT(Orbit Orbit, BODY Body, double UTatStart) throws AGuSDataException {

		try {
			setApoapsis(Orbit.getApoapsis());
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "failed fetching apoapsis", e);
			throw new AGuSDataException(e);
		}
		try {
			setPeriapsis(Orbit.getPeriapsis());
		} catch (RPCException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "failed fetching periapsis", e1);
			throw new AGuSDataException(e1);
		}
		try {
			setInclination(Orbit.getInclination());
		} catch (RPCException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "failed fetching Inlination", e1);
			throw new AGuSDataException(e1);
		}
		try {
			setMeanAnomalyAtStart(Orbit.getMeanAnomaly());
		} catch (RPCException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "failed fetching MeanAnomaly", e1);
			throw new AGuSDataException(e1);
		}
		setUTatStart(UTatStart);
		setBody(Body);
	}

	/**
	 * Clones an existing ORBIT object
	 * 
	 * @param Orbit
	 */
	public ORBIT(ORBIT Orbit) {
		setApoapsis(Orbit.getApoapsis());
		setPeriapsis(Orbit.getPeriapsis());
		setInclination(Orbit.getInclination());
		setMeanAnomalyAtStart(Orbit.getMeanAnomalyAtStart());
		setUTatStart(Orbit.getUTatStart());
		setBody(Orbit.getBody());
	}

	/**
	 * The apoapsis is the highest point of the orbit
	 * 
	 * @return The apoapsis of the orbit in meters measured from the
	 *         gravitational center
	 */
	public double getApoapsis() {
		return apoapsis;
	}

	/**
	 * Sets the apoapsis to the new value.
	 * <p>
	 * Note: The apoapsis is the highest point of the orbit and can never be
	 * less than the periapsis
	 * <p>
	 * Errorchecks: will result in log
	 * <p>
	 * -newApoapsis lower than periapsis
	 * <p>
	 * -newApoapsis is negative
	 * 
	 * @param newApoapsis
	 *            in meters measured from the gravitational center
	 */
	public void setApoapsis(double newApoapsis) {
		// Checks if the new apoapsis is lower than the periapsis
		if (newApoapsis < getPeriapsis()) {
			LOGGER.logger.warning("New Apoapsis was set to be lower than periapsis" + " newAp= " + newApoapsis + " pe: "
					+ getPeriapsis());
		}
		// Checks if newApoapsis is negative
		if (newApoapsis < 0) {
			LOGGER.logger.warning(
					"New Apopsis was set to be negative" + " newAp= " + newApoapsis + " ap: " + getPeriapsis());
		}
		this.apoapsis = newApoapsis;
	}

	/**
	 * The periapsis is the lowest point of the orbit
	 * 
	 * @return The periapsis of the orbit in meters measured from the
	 *         gravitational center
	 */
	public double getPeriapsis() {
		return periapsis;
	}

	/**
	 * Sets the periapsis to the new value.
	 * <p>
	 * Note: The periapsis is the lowest point of the orbit and can never be
	 * less than the apoapsis
	 * <p>
	 * Errorchecks: will result in log
	 * <p>
	 * -newPeriapsis is higher than apoapsis
	 * <p>
	 * -newPeriapsis is negative
	 * 
	 * @param newPeriapsis
	 *            in meters measured from the gravitational center
	 */
	public void setPeriapsis(double newPeriapsis) {
		// If the new periapsis is higher than the apoapsis
		if (newPeriapsis > getApoapsis()) {
			LOGGER.logger.warning("New Periapsis was set to be higher than apoapsis" + " newPe= " + newPeriapsis
					+ " ap: " + getApoapsis());
		}
		// If the new periapsis is negative
		if (newPeriapsis < 0) {
			LOGGER.logger.warning(
					"New Periapsis was set to be negative" + " newPe= " + newPeriapsis + " ap: " + getApoapsis());
		}

		this.periapsis = newPeriapsis;
	}

	/**
	 * The inclination of the orbit relative to the given standard plane of
	 * reference
	 * 
	 * @return the inclination in radians
	 */
	public double getInclination() {
		return inclination;
	}

	/**
	 * Sets the inclination of the orbit relative to the given standard plane of
	 * reference
	 * 
	 * @param newInclination
	 *            the inclination in radians
	 */
	public void setInclination(double newInclination) {
		this.inclination = newInclination;
	}

	/**
	 * NOT WORKING!!! The Longitude of the ascending node through the standard
	 * plane of reference
	 * 
	 * @return Longitude of the ascending in radians
	 */
	public double getLongitudeOfAscendingNode() {
		// TODO
		return 0;
	}

	/**
	 * NOT WORKING!!! The Longitude of the ascending node through the given
	 * plane of reference
	 * 
	 * @param referenceOrbit
	 *            the orbit that should be used as reference
	 * @return the longitude in radians
	 */
	public double getLongitudeOfAscendingNode(ORBIT referenceOrbit) {
		// TODO
		return 0;
	}

	/**
	 * The Semi-major axis calculated from the apoapsis and periapsis
	 * 
	 * @return the semi major axis in meters
	 */
	public double getSemiMajorAxis() {
		return FORMULA_ORBITAL.SemiMajorAxis(getApoapsis(), getPeriapsis());
	}

	/**
	 * The Semi-minor axis calculated from the apoapsis and periapsis
	 * 
	 * @return the semi minor axis in meters
	 */
	public double getSemiMinorAxis() {
		return FORMULA_ORBITAL.SemiMinorAxis(getApoapsis(), getPeriapsis());
	}

	/**
	 * 
	 * @return The eccentricity of the orbit
	 */
	public double getEccentricity() {
		return FORMULA_ORBITAL.LinearEccentricity(getSemiMajorAxis(), getSemiMinorAxis());
	}

	/**
	 * Returns the velocity of the vessel at the given UT
	 * 
	 * @param UT
	 *            the time at which to calculate
	 * @return the velocity in m/s
	 */
	public double getVelocity(double UT) {
		return FORMULA_ORBITAL.VisVisaEquation(getBody().getMass(), getAltitudeAtUT(UT), getApoapsis(), getPeriapsis());
	}

	/**
	 * Returns the radius to the gravitational center at the given time
	 * 
	 * @param UT
	 *            the time at which to calculate
	 * @return the radius to the gravitational center in meter
	 */
	public double getAltitudeAtUT(double UT) {
		double f = getTrueAnomalyAtUT(UT);
		double r0 = FORMULA_ORBITAL.RadiusAtTrueAnomaly(getSemiMajorAxis(), getEccentricity(), f);
		return r0;
	}

	/**
	 * The celestial body (e.g. planet or moon) around which the object is
	 * orbiting.
	 * 
	 * @return the body
	 */
	public BODY getBody() {
		return body;
	}

	/**
	 * Sets the body/ gravitational center being orbited to a new body
	 * 
	 * <p>
	 * Errorcheck:
	 * <p>
	 * -newBody == null
	 * 
	 * @param newBody
	 *            the new body
	 */
	public void setBody(BODY newBody) {
		if (newBody == null) {
			LOGGER.logger.warning("new Body was set to null");
		}
		this.body = newBody;
	}

	/**
	 * The time needed to make one period of the orbit
	 * 
	 * @return the time in seconds
	 */
	public double getTimeForPeriod() {
		return FORMULA_ORBITAL.OrbitPeriodTime(getBody().getMass(), getSemiMajorAxis());
	}

	/**
	 * NOT WORKING!!! The orbit that will happen next if no burns happen
	 * 
	 * @return next orbit
	 */
	public ORBIT getNextOrbit() {
		// TODO
		return this;
	}

	/**
	 * NOT WORKING!!! Returns true if the next orbit will contain an Soi change
	 * 
	 * @return true if the next orbit will contain an Soi change
	 */
	public boolean getSoiChange() {
		// TODO
		return false;
	}

	/**
	 * The true anomaly at the time the vessel first enters the orbit
	 * 
	 * @return the true anomaly in radians
	 */
	public double getTrueAnomalyAtStart() {
		return FORMULA_ORBITAL.MeanToTrueAnomaly(getEccentricity(), getMeanAnomalyAtStart());

	}

	/**
	 * Sets the true anomaly at the time the vessel first enters the orbit to
	 * the given value.
	 * <p>
	 * Note: changing this value will automatically change the mean anomaly at
	 * start
	 * <p>
	 * Errorchecks:
	 * <p>
	 * TODO
	 * 
	 * @param newTrueAnomalyAtStart
	 *            the new true anomaly at start in radians
	 */
	public void setTrueAnomalyAtStart(double newTrueAnomalyAtStart) {
		this.meanAnomalyAtStart = FORMULA_ORBITAL.TrueToMeanAnomaly(getEccentricity(), newTrueAnomalyAtStart);
	}

	/**
	 * Calculates the true anomaly at the given time by calculating first the
	 * mean anomaly and converting it to the true anomaly
	 * 
	 * @param UT
	 *            the time
	 * @return the true anomaly in radians
	 */
	public double getTrueAnomalyAtUT(double UT) {
		double M = getMeanAnomalyAtUT(UT);
		return FORMULA_ORBITAL.MeanToTrueAnomaly(getEccentricity(), M);
	}

	/**
	 * The mean anomaly at the time the vessel first enters the orbit
	 * 
	 * @return the mean anomaly in radians
	 */
	public double getMeanAnomalyAtStart() {
		return meanAnomalyAtStart;
	}

	/**
	 * Sets the mean anomaly at the time the vessel first enters the orbit to
	 * the given value.
	 * <p>
	 * Note: changing this value will automatically change the true anomaly at
	 * start
	 * <p>
	 * Errorchecks:
	 * <p>
	 * TODO
	 * 
	 * @param newMeanAnomalyAtStart
	 *            the new mean anomaly at start in radians
	 */
	public void setMeanAnomalyAtStart(double newMeanAnomalyAtStart) {
		this.meanAnomalyAtStart = newMeanAnomalyAtStart;
	}

	/**
	 * Calculates the mean anomaly at the given time
	 * 
	 * @param UT
	 *            the time
	 * @return the true anomaly in radians
	 */
	public double getMeanAnomalyAtUT(double UT) {
		double mam = FORMULA_ORBITAL.MeanAngularMotionRad(getTimeForPeriod());
		return FORMULA_ORBITAL.MeanAnomalyAtTime(getMeanAnomalyAtStart(), UT - getUTatStart(), mam);
	}

	/**
	 * The UT when the vessel/object first starts this orbit
	 * 
	 * @return the met in seconds
	 */
	public double getUTatStart() {
		return UTAtStart;
	}

	/**
	 * Set the UT when the vessel/object first starts this orbit
	 * 
	 * @param newUT
	 *            the new Met in seconds
	 */
	public void setUTatStart(double newUT) {
		this.UTAtStart = newUT;
	}

	/**
	 * The UT when the vessel will reach the periapsis the next time after the
	 * current UT
	 * <p>
	 * Note: If the UT given is at the periapsis the next periapsis met will be
	 * returned
	 * 
	 * @param UT
	 *            the met from where to start
	 * @return the UT of the next periapsis
	 */
	public double getUTatNextPeriapsis(double UT) {
		return FORMULA_ORBITAL.TimeToNextPeriapsisRadian(getTimeForPeriod(), getMeanAnomalyAtUT(UT)) + UT;
	}

	/**
	 * Checks the Object if the current Orbit is even possible and prints
	 * inconsistencis to the log
	 * <p>
	 * Checks following cases in given order:
	 * <p>
	 * -Apoapsis or periapsis negative: makes them positive
	 * <p>
	 * -Apoapsis lower than periapsis: exchange the values
	 * <p>
	 * 
	 * @param fix
	 *            if true will try to fix the problems
	 * @return true if there where problems
	 */
	public boolean checkForErrors(boolean fix) {
		LOGGER.logger.info("Starting check for errors with fix = " + fix);
		boolean error = false;
		//ap is negative
		if (getApoapsis() < 0) {
			LOGGER.logger.warning("Apoapsis negative");
			if (fix == true) {
				setApoapsis(getApoapsis() * (-1));
			}
			error = true;
		}
		//pe is negative
		if (getPeriapsis() < 0) {
			LOGGER.logger.warning("Periapsis negative");
			if (fix == true) {
				setPeriapsis(getPeriapsis() * (-1));
			}
			error = true;
		}
		// Apoapsius is lower than periapsis
		if (getApoapsis() < getPeriapsis()) {
			LOGGER.logger.warning("Apoapsis lower than periapsis");
			if (fix == true) {
				double pe = getPeriapsis();
				setPeriapsis(getApoapsis());
				setApoapsis(pe);
			}
			error = true;
		}

		return error;
	}

}
