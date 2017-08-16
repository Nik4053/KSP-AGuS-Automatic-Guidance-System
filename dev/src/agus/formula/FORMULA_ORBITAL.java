package agus.formula;

import java.util.logging.Level;

import nhlog.LOGGER;

/**
 * Provides all formulas needed for orbital calculations
 * 
 * @author Niklas Heidenreich
 *
 */
public class FORMULA_ORBITAL {
	/**
	 * The Gravitational constant = 6.67408*10^-11
	 */
	private static final double G = 6.67408 * Math.pow(10, -11);// Gravitational
																// constant

	/**
	 * The VisVisa-Equation used to calculate the velocity of an object at the
	 * given height
	 * 
	 * @param M
	 *            Mass of the orbited Body
	 * @param rT
	 *            Radius at Target
	 * @param rA
	 *            Radius at Apoapsis
	 * @param rP
	 *            Radius at Periapsis
	 * @return Velocity in m/s
	 */
	public static double VisVisaEquation(double M, double rT, double rA, double rP) {
		if (M <= 0) {
			LOGGER.logger.log(Level.SEVERE, "", new IllegalArgumentException("Invalid Mass: " + M));
		}
		if (rT <= 0) {
			LOGGER.logger.log(Level.SEVERE, "", new IllegalArgumentException("Invalid Target Radius: " + rT));
		}
		if (rA <= 0) {
			LOGGER.logger.log(Level.SEVERE, "", new IllegalArgumentException("Invalid Apoapsis: " + rA));
		}
		if (rP <= 0) {
			LOGGER.logger.log(Level.SEVERE, "", new IllegalArgumentException("Invalid Periapsis: " + rP));
		}
		return Math.sqrt(G * M * (2 / rT - 2 / (rA + rP)));
	}

	/**
	 * Calculate the radius of the orbit at any given angle
	 * 
	 * @param a
	 *            the semi major axis
	 * @param Eccentricity
	 *            the eccentricity
	 * @param TrueAnomaly
	 *            the true anomaly in radian
	 * @return the radius at the given true anomaly
	 */
	public static double RadiusAtTrueAnomaly(double a, double Eccentricity, double TrueAnomaly) {
		// If orbit is hyperbolic or parabolic
		double e = Eccentricity;
		if (e >= 1) {
			System.out.println("RadiusAtTrueAnomaly does not support parabolic or hyperbolic orbits!");
			LOGGER.logger.warning("RadiusAtTrueAnomaly does not support parabolic or hyperbolic orbits!" + " e = " + e);
			return -1;
		}
		// convert from radian to degree
		double f = (TrueAnomaly / (2 * Math.PI)) * 360;
		double r;
		r = (a * (1 - Math.pow(e, 2))) / (1 + e * Math.cos(f));
		return r;
	}

	/**
	 * Calculates the time needed to complete the given Orbit one time
	 * 
	 * @param M
	 *            The Mass of the Body being orbited
	 * @param a
	 *            The semi-major axis of the Orbit
	 * @return The time it takes to complete one Orbit
	 */
	public static double OrbitPeriodTime(double M, double a) {
		//LOGGER.logger.info("StartMethod: " + Thread.currentThread().getStackTrace()[1].getClassName() + " " + Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
		return 2 * Math.PI * Math.sqrt(Math.pow((a), 3) / (G * M));

	}

	/**
	 * calculates the semi major axis of an orbit
	 * 
	 * @param apoapsis
	 *            the apoapsis of the orbit
	 * @param periapsis
	 *            the periapsis of the orbit
	 * @return the semi major axis
	 */
	public static double SemiMajorAxis(double apoapsis, double periapsis) {
		double sma = apoapsis + periapsis;
		sma = sma / 2;
		return sma;
	}

	/**
	 * Calculates the semi Minor axis of the orbit
	 * 
	 * @param apoapsis
	 *            the apoapsis of the orbit
	 * @param periapsis
	 *            the periapsis of the orbit
	 * @return the semi minor axis
	 */
	public static double SemiMinorAxis(double apoapsis, double periapsis) {
		return Math.sqrt(apoapsis * periapsis);
	}

	/**
	 * @param M
	 *            The Mass of the Body being orbited
	 * @param timeSinceLastPeriapsis
	 *            the time since the last periapsis
	 * @param a
	 *            the semi-major axis
	 * @return the time to the next periapsis
	 */
	public static double TimeToPeriapsis(double M, double timeSinceLastPeriapsis, double a) {
		double T = 0;
		double t = timeSinceLastPeriapsis;
		T = 2 * Math.PI * Math.sqrt(Math.pow(a, 3) / (M * G));
		return T - t;
	}

	/**
	 * Calculates the average rate of sweep in radians from 0 - 2pi
	 * <p>
	 * See: https://en.wikipedia.org/wiki/Mean_anomaly
	 * 
	 * @param TimeForPeriod
	 * @return the mean angular motion
	 */
	public static double MeanAngularMotionRad(double TimeForPeriod) {
		double T = TimeForPeriod;
		return (2 * Math.PI) / T;
	}

	/**
	 * Calculates the average rate of sweep in degree from 0 - 360
	 * <p>
	 * See: https://en.wikipedia.org/wiki/Mean_anomaly
	 * 
	 * @param TimeForPeriod
	 * @return the mean angular motion
	 */
	public static double MeanAngularMotionDeg(double TimeForPeriod) {
		double T = TimeForPeriod;
		return (360) / T;
	}

	/**
	 * Calculates the average rate of sweep in radians from 0 - 2pi
	 * <p>
	 * calculates using keplers 3rd law
	 * https://en.wikipedia.org/wiki/Mean_motion
	 * 
	 * @param M
	 *            the mass of the body
	 * @param a
	 *            the semi major axis
	 * @return the mean angular motion in rad/sec
	 */
	public static double MeanAngularMotion(double M, double a) {
		return Math.sqrt((G * M) / Math.pow(a, 3));
	}

	/**
	 * Calculates the mean anomaly given the time from last periapsis
	 * 
	 * @param MeanAngularMotion
	 *            in degrees or radians
	 * @param deltaTfromPeriapsis
	 *            the time since the last periapsis
	 * @return the meananomaly in degree or radians
	 */
	public static double MeanAnomaly(double MeanAngularMotion, double deltaTfromPeriapsis) {
		double n = MeanAngularMotion;
		double t = deltaTfromPeriapsis;
		return n * t;
	}

	/*
	 * -------old------- Calculates the mean anomaly given the exact time
	 * 
	 * @param MeanAngularMotion in degrees or radians
	 * 
	 * @param newTime the time at which to measure toe MeanAnomaly
	 * 
	 * @param lastTimeAtPeriapsis the time when the periapsis was last crossed
	 * 
	 * @return public static double MeanAnomalyAtTime(double MeanAngularMotion,
	 * double newTime, double lastTimeAtPeriapsis) { double n =
	 * MeanAngularMotion; double t = newTime; double r = lastTimeAtPeriapsis;
	 * return n * (t - r); }
	 */

	/**
	 * Calculates the mean anomaly at given time
	 * 
	 * @param CurrentMeanAnomaly
	 *            the current mean anomaly
	 * @param deltaT
	 *            the time difference to the next check
	 * @param MeanAngularMotion
	 *            the mean angular motion
	 * @return the mean anomaly at the given time difference
	 */
	public static double MeanAnomalyAtTime(double CurrentMeanAnomaly, double deltaT, double MeanAngularMotion) {
		double Mo = CurrentMeanAnomaly;
		double n = MeanAngularMotion;
		return n * deltaT + Mo;
	}

	/**
	 * 
	 * @param a
	 *            the semi-major axis
	 * @param b
	 *            the semi-minor axis
	 * @return the linear eccentricity e
	 */
	public static double LinearEccentricity(double a, double b) {
		return Math.pow(a, 2) + Math.pow(b, 2);
	}

	/**
	 * 
	 * @param e
	 *            the linear Eccentricity
	 * @param a
	 *            the semi-major axis
	 * @return the numeric eccentricity
	 */
	public static double NumericEccentricity(double e, double a) {
		return e / a;
	}

	/**
	 * 
	 * @param g
	 *            the gravitation on the surface
	 * @param r
	 *            the radius of the body
	 * @return the speed needed to get into an orbit at the surface of the body
	 */
	public static double FirstCosmicVelocity(double g, double R) {
		return Math.sqrt(g * R);
	}

	/**
	 * 
	 * @param g
	 *            the gravitation on the surface
	 * @param r
	 *            the radius of the body
	 * @return the speed needed to leave the sphere of influence of the planet
	 *         at the surface of the body
	 */
	public static double SecondCosmicVelocity(double g, double R) {
		return Math.sqrt(2 * g * R);
	}

	/**
	 * Calculates the time needed to reach the next periapsis
	 * 
	 * @param TimeForPeriod
	 *            the time needed for one period
	 * @param CurrentMeanAnomaly
	 *            the current mean anomaly in degree (0 - 360)
	 * @return the time needed to reach the next periapsis in seconds
	 */
	public static double TimeToNextPeriapsisDegree(double TimeForPeriod, double CurrentMeanAnomaly) {
		double Ma = CurrentMeanAnomaly;
		double U = TimeForPeriod;
		return (360 - Ma) * (U / 360);
	}

	/**
	 * Calculates the time needed to reach the next periapsis
	 * 
	 * @param MeanAngularMotion
	 *            the mean angular motion in degree/second
	 * @param CurrentMeanAnomaly
	 *            the current mean anomaly in degree (0 - 360)
	 * @return the time needed to reach the next periapsis in seconds
	 */
	public static double TimeToNextPeriapsisDegree2(double MeanAngularMotion, double CurrentMeanAnomaly) {
		double Ma = CurrentMeanAnomaly;
		return (360 - Ma) * Math.pow(MeanAngularMotion, -1);
	}

	/**
	 * Calculates the time needed to reach the next periapsis
	 * 
	 * @param TimeForPeriod
	 *            the time needed for one period
	 * @param CurrentMeanAnomaly
	 *            the current mean anomaly in radians (0 - 2pi)
	 * @return the time needed to reach the next periapsis in seconds
	 */
	public static double TimeToNextPeriapsisRadian(double TimeForPeriod, double CurrentMeanAnomaly) {
		double Ma = CurrentMeanAnomaly;
		double U = TimeForPeriod;
		return (2 * Math.PI - Ma) * (U / (2 * Math.PI));
	}

	/**
	 * Calculates the time needed to reach the next periapsis
	 * 
	 * @param MeanAngularMotion
	 *            the mean angular motion in radian/second
	 * @param CurrentMeanAnomaly
	 *            the current mean anomaly in radians (0 - 2pi)
	 * @return the time needed to reach the next periapsis in seconds
	 */
	public static double TimeToNextPeriapsisRadian2(double MeanAngularMotion, double CurrentMeanAnomaly) {
		double Ma = CurrentMeanAnomaly;
		return (2 * Math.PI - Ma) * Math.pow(MeanAngularMotion, -1);
	}

	/**
	 * Converts eccentric anomaly to mean anomaly
	 * 
	 * @param Eccentricity
	 *            the eccentricity of the orbit
	 * @param EccentricAnomaly
	 *            the eccentric anomaly in rad
	 * @return mean anomaly in rad
	 */
	public static double EccenToMeanAnomaly(double Eccentricity, double EccentricAnomaly) {
		double e = Eccentricity;
		if (e >= 1) {
			System.out.println("EccenToMeanAnomaly does not support parabolic or hyperbolic orbits!");
			LOGGER.logger.warning("EccenToMeanAnomaly does not support parabolic or hyperbolic orbits!" + " e = " + e);
			return -1;
		} else {
			double E = EccentricAnomaly;
			double M = E - e * Math.sin(E);
			return M;
		}

	}

	/**
	 * Converts eccentric anomaly to true anomaly
	 * 
	 * @param Eccentricity
	 *            the eccentricity of the orbit
	 * @param EccentricAnomaly
	 *            the eccentric anomaly in rad
	 * @return true anomaly in rad
	 */
	public static double EccenToTrueAnomaly(double Eccentricity, double EccentricAnomaly) {
		double e = Eccentricity;
		if (e >= 1) {
			System.out.println("EccenToTrueAnomaly does not support parabolic or hyperbolic orbits!");
			LOGGER.logger.warning("EccenToTrueAnomaly does not support parabolic or hyperbolic orbits!" + " e = " + e);
			return -1;
		} else {
			double E = EccentricAnomaly;
			double sinf = (Math.sin(E) * Math.sqrt(1 - Math.pow(e, 2))) / (1 - e * Math.cos(E));
			double cosf = (Math.cos(E) - e) / (1 - e * Math.cos(E));
			double f = Math.atan2(sinf, cosf);
			return f;

		}
	}

	/**
	 * Converts mean anomaly to eccentric anomaly
	 * 
	 * @param Eccentricity
	 *            the eccentricity of the orbit
	 * @param MeanAnomaly
	 *            the mean anomaly in rad (between -pi and pi)
	 * @return the eccentric anomaly in rad
	 */
	public static double MeanToEccenAnomaly(double Eccentricity, double MeanAnomaly) {
		double e = Eccentricity;
		if (e >= 1) {
			System.out.println("MeanToEccenAnomaly does not support parabolic or hyperbolic orbits!");
			LOGGER.logger.warning("MeanToEccenAnomaly does not support parabolic or hyperbolic orbits!" + " e = " + e);
			return -1;
		}

		double M = MeanAnomaly;
		/*
		 * if (M < -Math.PI || M > Math.PI) { System.out.
		 * println("MeanToEccenAnomaly does not support an mean anomaly of less than -pi or more than pi!"
		 * ); LOGGER.logger
		 * .warning("MeanToEccenAnomaly does not support an mean anomaly of less than -pi or more than pi!"
		 * + " M = " + M); }
		 */
		double E;// eccentric anomaly
		M = M % (2 * Math.PI);
		if (M < Math.PI) {
			M = M + (2 * Math.PI);
		} else if (M > Math.PI) {
			M = M - (2 * Math.PI);
		}
		if ((M > -Math.PI && M < 0) || (M > Math.PI)) {
			E = M - e;
		} else {
			E = M + e;
		}
		double Enew = E;
		double y = 1 * Math.pow(E, -6);
		double x = 1;
		while (x > y || Math.abs(Enew - E) > y) {
			x = 0;
			E = Enew;
			Enew = E + (M - E + e * Math.sin(E)) / (1 - e * Math.cos(E));
			E = Enew;
			Enew = E + (M - E + e * Math.sin(E)) / (1 - e * Math.cos(E));

		}
		return E;
	}

	/**
	 * converts mean anomaly to true anomaly
	 * 
	 * @param Eccentricity
	 * @param MeanAnomaly
	 * @return the true anomaly in rad
	 */
	public static double MeanToTrueAnomaly(double Eccentricity, double MeanAnomaly) {
		double e = Eccentricity;
		if (e >= 1) {
			System.out.println("MeanToTrueAnomaly does not support parabolic or hyperbolic orbits!");
			LOGGER.logger.warning("MeanToTrueAnomaly does not support parabolic or hyperbolic orbits!" + " e = " + e);
			return -1;
		} else {
			double E;
			double M = MeanAnomaly;
			E = MeanToEccenAnomaly(e, M);
			double f = EccenToTrueAnomaly(e, E);
			return f;
		}
	}

	/**
	 * Converts true anomaly to eccentric anomaly
	 * 
	 * @param Eccentricity
	 *            the eccentricity of the orbit
	 * @param TrueAnomaly
	 *            the true anomaly in rad
	 * @return eccentric anomaly in rad
	 */
	public static double TrueToEccenAnomaly(double Eccentricity, double TrueAnomaly) {
		double e = Eccentricity;
		if (e >= 1) {
			System.out.println("TrueToEccenAnomaly does not support parabolic or hyperbolic orbits!");
			LOGGER.logger.warning("TrueToEccenAnomaly does not support parabolic or hyperbolic orbits!" + " e = " + e);
			return -1;
		} else {
			double f = TrueAnomaly;
			double sinE = Math.sin(f) * Math.sqrt(1 - Math.pow(e, 2)) / (1 + e * Math.cos(f));
			double cosE = (e + Math.cos(f)) / (1 + e * Math.cos(f));
			double E = Math.atan2(sinE, cosE);
			return E;
		}
	}

	/**
	 * Converts true anomaly to mean anomaly
	 * 
	 * @param Eccentricity
	 *            the eccentricity of the orbit
	 * @param TrueAnomaly
	 *            the true anomaly in rad
	 * @return the Mean anomaly in rad
	 */
	public static double TrueToMeanAnomaly(double Eccentricity, double TrueAnomaly) {
		double e = Eccentricity;
		if (e >= 1) {
			System.out.println("TrueToMeanAnomaly does not support parabolic or hyperbolic orbits yet!");
			LOGGER.logger
					.warning("TrueToMeanAnomaly does not support parabolic or hyperbolic orbits yet!" + " e = " + e);
			return -1;
		} else {
			double M = -1; // output
			double f = TrueAnomaly;
			double E = TrueToEccenAnomaly(e, f);
			M = EccenToMeanAnomaly(e, E);
			return M;
		}
	}
}
