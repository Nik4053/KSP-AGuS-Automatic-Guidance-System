package agus.gunit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.CelestialBody;
import nhlog.LOGGER;

/**
 * Will contain all information about a specific body
 * 
 * @author Niklas Heidenreich
 *
 */
public class BODY {
	private CelestialBody body;
	private ORBIT orbit;
	private List<BODY> satellites = new ArrayList<BODY>();
	private BODY parentBody;

	// All the other variables are stored before loadData()

	/**
	 * This class will provide all information needed about the given in game body
	 * 
	 * @param spaceCenter
	 *            the spacecenter object
	 * @param Body
	 *            the krpc body object this body object has to represent
	 * @param parentBody
	 *            the body this body is orbiting around
	 * @throws AGuSDataException
	 */
	public BODY(SpaceCenter spaceCenter, CelestialBody Body, BODY ParentBody) throws AGuSDataException {
		this.body = Body;
		this.parentBody = ParentBody;
		Iterator<CelestialBody> iter;
		try {
			iter = body.getSatellites().iterator();

			while (iter.hasNext()) {
				this.satellites.add(new BODY(spaceCenter, iter.next(), this));
			}
			try {
				this.orbit = new ORBIT(body.getOrbit(), parentBody, spaceCenter.getUT());
			} catch (NullPointerException e) {
				// The Sun/Root body will return a nullpointer when asked for the orbit
				// This process will then now that it has reached the root body
			}
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		try {
			loadData();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	private float mass;
	private String name;
	private float soi;

	private void loadData() throws RPCException, IOException {
		mass = getCelestialBody().getMass();
		name = getCelestialBody().getName();
		soi = getCelestialBody().getSphereOfInfluence();
	}

	/**
	 * 
	 * @return the krpc body object of this body
	 */
	public CelestialBody getCelestialBody() {
		return body;
	}

	/**
	 * 
	 * @return the orbit of this body
	 */
	public ORBIT getOrbit() {
		return orbit;
	}

	/**
	 * The bodies orbiting this body
	 * 
	 * @return a list containing all bodies orbiting this body
	 */
	public List<BODY> getSatellites() {
		return satellites;
	}

	/**
	 * 
	 * @return the body this body is orbiting around
	 */
	public BODY getParentBody() {
		return parentBody;
	}

	public float getMass() {
		return mass;
	}

	public String getName() {
		return name;
	}
	
	public float getSphereOfInfluence() {
		return soi;
	}

	/**
	 * Searches for the body with the given name recursively
	 * 
	 * @param name
	 *            the name of the body
	 * @return the body
	 */
	protected BODY searchBody(String name) {
		if (getName().equals(name)) {
			// if this is the body
			return this;
		}
		Iterator<BODY> iter = getSatellites().iterator();
		BODY b = null;
		while (iter.hasNext() && b == null) {
			b = iter.next().searchBody(name);
		}
		return b;
	}

}
