package agus.formula;
/**
 * Provides all formulas for calculating transfer types
 * @author Niklas Heidenreich
 *
 */
public class FORMULA_TRANSFER {
	
	/**
	 * The Hohmann Transfer used to transfer between 2 points in the most
	 * efficient way possible
	 * @param M Mass of the orbited Body
	 * @param rT Radius at target burn (Periapsis or Apoapsis)
	 * @param rS Radius at the start of the burn
	 * @param rE Radius at the end of the burn
	 * @return deltaV in m/s
	 */
	public static double HohmannTransfer(double M, double rT, double rS, double rE) {
		//LOGGER.logger.info("StartMethod: " + Thread.currentThread().getStackTrace()[1].getClassName() + " " + Thread.currentThread().getStackTrace()[1].getMethodName() + "()");		
		return FORMULA_ORBITAL.VisVisaEquation(M, rT, rT, rS) - FORMULA_ORBITAL.VisVisaEquation(M, rT, rT, rE);
	}
	
	/**
	 * Will return the height of the parking orbit in m measured from the center
	 * of the body
	 * 
	 * @param EquatorialRadius
	 *            The equatorial radius of the body, in meters.
	 * @param AtmosphereDepth
	 *            The depth of the atmosphere, in meters.
	 * @param SurfaceGravity
	 *            The acceleration due to gravity at sea level (mean altitude)
	 *            on the body, in m/s^2
	 * @return hight of the orbit measured from the center of the body in m
	 */
	public static double ParkingOrbit(double EquatorialRadius, float AtmosphereDepth, float SurfaceGravity) {
		//LOGGER.logger.info("StartMethod: " + Thread.currentThread().getStackTrace()[1].getClassName() + " "	+ Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
		return (int) (EquatorialRadius + EquatorialRadius / 10 + AtmosphereDepth + SurfaceGravity + 10000);
	}

}
