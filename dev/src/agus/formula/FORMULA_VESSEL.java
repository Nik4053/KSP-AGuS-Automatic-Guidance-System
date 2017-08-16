package agus.formula;

import agus.gunit.ORBIT;

/**
 * Contains all formulas for calculating vessel specific things
 * 
 * @author Niklas Heidenreich
 *
 */
public class FORMULA_VESSEL {

	/**
	 * Calculates the burntime for a given deltaV using the rocket equation
	 * 
	 * @param AvailableThrust
	 *            The total available thrust that can be produced by the
	 *            vessel’s active engines, in Newtons.
	 * @param SpecificImpulse
	 *            The combined specific impulse of all active engines, in
	 *            seconds.
	 * @param Mass
	 *            The total mass of the vessel, including resources, in kg.
	 * @param dV
	 *            The deltaV that should be used
	 * @return burn time needed
	 */
	public static float BurnTime(float AvailableThrust, float SpecificImpulse, float Mass, double dV) {
		//LOGGER.logger.info("StartMethod: " + Thread.currentThread().getStackTrace()[1].getClassName() + " "+ Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
		if (AvailableThrust == 0 || SpecificImpulse == 0 || Mass == 0 || dV == 0) {
			return 0;
		}
		// Calculate burn time (using rocket equation)
		float force = AvailableThrust;
		float isp = SpecificImpulse * 9.82f;
		float m0 = Mass;
		float m1 = (float) (m0 / Math.exp(dV / isp));
		float flowRate = force / isp;
		return (m0 - m1) / flowRate;
	}

	/**
	 * Calculates the deltaV of an rocket stage
	 * 
	 * @param m0
	 *            The mass of the rocket
	 * @param mf
	 *            the dry mass of the rocket
	 * @param isp
	 *            the isp of the engines
	 * @return The deltaV of the Rocket/stage
	 */
	public static double DeltaVofStage(double m0, double mf, double isp) {
		double dv;
		dv = isp * 9.8 * Math.log(m0 / mf);
		return dv;
	}

	/**
	 * Calculates the combined specific impulse of 2 engines
	 * http://wiki.kerbalspaceprogram.com/wiki/Specific_impulse#Multiple_engines
	 * 
	 * @param thrusta
	 * @param thrustb
	 * @param ispa
	 * @param ispb
	 * @return
	 */
	public static float CombinedSpecificImpulse(float thrusta, float thrustb, float ispa, float ispb) {
		float isp;
		isp = (thrusta + thrustb) / ((thrusta / ispa) + (thrustb / ispb));
		return isp;
	}

	/**
	 * Calculates the range between 2 objects at the given UT
	 * 
	 * @param Orbit1
	 *            the orbit of the first object
	 * @param Orbit2
	 *            the orbit of the second object
	 * @param UT
	 *            the time at which to calculate
	 * @return the distance in meters between the two objects
	 */
	public static double getRangeAtUT(ORBIT Orbit1, ORBIT Orbit2, double UT) {
		double range = getRangeAtUT(Orbit1.getAltitudeAtUT(UT), Orbit2.getAltitudeAtUT(UT),
				Orbit1.getTrueAnomalyAtUT(UT) - Orbit2.getTrueAnomalyAtUT(UT));
		return range;
	}

	/**
	 * Calculates the range between 2 objects at the given UT
	 * 
	 * @param altitude1
	 *            the altitude of the first object
	 * @param altitude2
	 *            the altitude of the second object
	 * @param angle
	 *            the angle between both objects
	 * @return the distance in meters
	 */
	public static double getRangeAtUT(double altitude1, double altitude2, double angle) {
		double range;
		range = Math.pow(altitude1, 2) + Math.pow(altitude2, 2) - 2 * altitude1 * altitude2 * Math.cos(angle);
		range = Math.sqrt(range);
		return range;
	}

}
