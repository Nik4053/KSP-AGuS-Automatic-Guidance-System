/**
 * 
 */
package agus.gunit.sequence;

/**
 * This class contains all information needed to perform a burn.
 * <p> It stores the deltaV, time and the vector at which to burn
 * @author Niklas Heidenreich
 */
public class BURN {

	private double deltaV;
	/**
	 * the UT
	 */
	private double timeAtBurn;
	private VECTOR_BURN vec;

	/**
	 * 
	 * @param timeAtBurn
	 *            The time at burn in UT
	 * @param prograde
	 *            the deltaV in prograde direction
	 * @param normal
	 *            the deltaV in normal direction
	 * @param radial
	 *            the deltaV in radial direction
	 */
	public BURN(double timeAtBurn, double prograde, double normal, double radial) {
		this.deltaV=Math.sqrt(Math.pow(prograde, 2)+Math.pow(normal, 2)+Math.pow(radial, 2));
		this.timeAtBurn = timeAtBurn;
		this.vec = new VECTOR_BURN(prograde, normal, radial);
	}

	/**
	 * 
	 * @return the DeltaV
	 */
	public double getDeltaV() {
		return deltaV;
	}

	/**
	 * 
	 * @return the time at Burn (UT) in sec
	 */
	public double getTimeAtBurn() {
		return timeAtBurn;
	}

	/**
	 * 
	 * @return the vector burn object
	 */
	public VECTOR_BURN getVectorBurn() {
		return vec;
	}
}