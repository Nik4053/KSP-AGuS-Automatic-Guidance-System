/**
 * 
 */
package agus.gunit.sequence;

/**
 * Provides a vector of the Burn containing of deltaV that should be executed.
 * @author Niklas heidenreich
 *
 */
public class VECTOR_BURN {

	private double prograde, normal, radial;
	
	public VECTOR_BURN(double nprograde, double nnormal, double nradial)
	{
		this.prograde=nprograde;
		this.normal=nnormal;
		this.radial=nradial;		
	}
	
	/**
	 * 
	 * @return the deltaV in prograde
	 */
	public double getPrograde()
	{
		return prograde;
	}
	
	/**
	 * 
	 * @return the deltaV in normal
	 */
	public double getNormal()
	{
		return normal;
	}
	
	/**
	 * 
	 * @return the deltaV in radial
	 */
	public double getRadial()
	{
		return radial;
	}
	
	/**
	 * 
	 * @return the absolute deltaV
	 */
	public double getAbsolute()
	{
		return Math.sqrt(Math.pow(prograde, 2)+Math.pow(normal, 2)+ Math.pow(radial, 2));
	}
	
	/**
	 * 
	 * @return an ORIENTATION vector object containing the rotation of he vessel
	 */
    public VECTOR_ORIENTATION getOrientationVector()
    {
        double heading = calcHeading();
        double pitch = calcPitch();
        return new VECTOR_ORIENTATION(pitch, heading);
    }

    private double calcHeading()
    {
        double heading;
        if(radial!=0||prograde!=0)
        {
            double[] pr = new double[2];
            pr[0]= radial;
            pr[1]= prograde;
            double prAbs=Math.sqrt(Math.pow(pr[0], 2)+Math.pow(pr[1], 2));           
            heading=Math.acos((pr[1]*1)/(prAbs*1)) * (180/Math.PI);
            if(radial<0)
            {
                heading=-heading;
            }

        }else
        {
            heading=0;
        }

        System.out.println("Heading: " + heading);
        return heading;
    }
    
    private double calcPitch()
    {
        double pitch;
        if(radial!=0||normal!=0)
        {
            double[] rn = new double[2];
            rn[0]= radial;
            rn[1]= normal;
            double rnAbs=Math.sqrt(Math.pow(rn[0], 2)+Math.pow(rn[1], 2));           
            pitch=Math.acos((rn[1]*1)/(rnAbs*1)) * (180/Math.PI);
            pitch=90-pitch;
        }else{
            pitch=0;
        }
        return pitch;
    }
	

}
