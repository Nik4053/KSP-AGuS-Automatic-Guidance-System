/**
 * 
 */
package agus.gunit.sequence;

/**
 * Conatins the a vector for orientating the spacecraft
 * @author Niklas heidenreich
 *
 */
public class VECTOR_ORIENTATION {

	private double pitch, heading;
	
	public VECTOR_ORIENTATION(double npitch, double nheading)
	{
		this.pitch=npitch;
		this.heading=nheading;
	}
	
	public double getPitch()
	{
		return pitch;
	}
	public double getHeading()
	{
		return heading;
	}
	
	public double getAbsolute()
	{
		return Math.sqrt(Math.pow(pitch, 2)+Math.pow(heading, 2));
	}
	
	
}
