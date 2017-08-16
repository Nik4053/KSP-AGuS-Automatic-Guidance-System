package agus.gunit;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.CelestialBody;
import nhlog.LOGGER;

/**
 * Contains a tree with all existing bodies
 * 
 * @author Niklas Heidenreich
 *
 */
public class BODIES {
	private SpaceCenter spaceCenter;
	private BODY body;

	/**
	 * Will create a tree structure containing all bodies of the game
	 * <p>
	 * Note: it is not known how this will work with multiple start systems
	 * 
	 * @param SpaceCenter
	 *            the spaceCenter object of krpc
	 * @throws AGuSDataException
	 */
	public BODIES(SpaceCenter SpaceCenter) throws AGuSDataException {
		LOGGER.logger.info("searching through bodies");
		this.spaceCenter = SpaceCenter;
		try {
			Object[] a = spaceCenter.getBodies().values().toArray();
			CelestialBody b = (CelestialBody) a[0];
			try {
				while (true) {
					b = b.getOrbit().getBody();
				}
			} catch (NullPointerException e) {
				// No need to catch
			}

			this.body = new BODY(spaceCenter, b, null);
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	/**
	 * returns the root body (the sun) containing all the other bodies in a tree
	 * structure
	 * 
	 * @return
	 */
	public BODY getRootBody() {
		return body;
	}

	/**
	 * Will return the minimum soi changes needed to get from one body to
	 * another.
	 * <p>
	 * The list will contain all bodies in between
	 * <p>
	 * Example: startBody = Kerbin ; endBody = Tylo
	 * <p>
	 * return: Kerbin,Sun,Jool,Tylo
	 * 
	 * @param startBody
	 *            the body at which to start
	 * @param endBody
	 *            the target body
	 * @return a list containing all bodies in between
	 */
	public LinkedList<BODY> getMinBodiesBetween(BODY startBody, BODY endBody) {
		LinkedList<BODY> bodies = new LinkedList<BODY>();
		String target = endBody.getName();
		BODY b = startBody;
		try {
			while (true) {

				if (b.getName().equals(target)) {
					bodies.add(b);
					return bodies;
				}

				if (b.searchBody(target) == null) {
					bodies.add(b);
					b = b.getParentBody();
				} else {
					bodies.add(b);
					Iterator<BODY> bIt = b.getSatellites().iterator();
					while (bIt.hasNext()) {
						BODY b2 = bIt.next();
						if (b2.searchBody(target) != null) {
							b = b2;
							break;
						}
					}
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			return null;
		}

	}

	/**
	 * Will search recursively trough the body tree to find the given body
	 * 
	 * @param name
	 *            the name of the body
	 * @return the body, null if it cant be found
	 */
	public BODY getBody(String name) {
		BODY b = getRootBody();
		return b.searchBody(name);
	}
}
