package agus.gunit.vessel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter.Part;
import nhlog.LOGGER;

/**
 * TODO search for parts(Engines, antennas) may not need to update completely
 * every time
 * <p>
 * This class allows fast access to all parts of the vessel.
 * <p>
 * It does so by traversing the vessel as a tree and giving each part a number,
 * id.
 * 
 * @author Niklas Heidenreich
 *
 */
public class PARTS {

	private VESSEL vessel;
	private Part[] partArr;

	public PARTS(VESSEL Vessel) {
		this.vessel = Vessel;
		this.partArr = updateOrderedPartList();
	}

	/**
	 * TODO not implemented yet!!!!
	 * <p>
	 * It is not recommended to update the partlist midflight!!! will create a
	 * thread that automatically updates the partlist
	 * 
	 * @param intervall
	 *            the interval at which to update in seconds
	 */
	public void autoUpdatePartList(float intervall) {
		//TODO
	}

	/**
	 * <p>
	 * It is not recommended to update the partlist midflight!!! This method
	 * will update the partlist by traversing recursively through all the parts.
	 * <p>
	 * This will create ~ 2kb of traffic
	 * 
	 * @return an array filled with parts, where every part is matched to the id
	 *         they have in the array
	 */
	public Part[] updateOrderedPartList() {
		Part[] parts = null;
		try {
			parts = new Part[vessel.getVessel().getParts().getAll().size()];
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "Error getting partlist", e);
		}
		Part root = null;
		try {
			root = vessel.getVessel().getParts().getRoot();
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "Error getting root part", e);
		}
		//Recursive search through vessel
		updateOrderedPartListRecursive(parts, root, 0);
		this.partArr = parts;
		return parts;

	}

	/**
	 * This will return the Partlist of the last time it was updated. This list
	 * may be out dated, if the vessel has docked with an other vessel, or
	 * performed staging.
	 * <p>
	 * To update it use updateOrderedPartList
	 * <p>
	 * Returns
	 * 
	 * @return an array filled with parts, where every part is matched to the id
	 *         they have in the array.
	 */
	public Part[] getOrderedPartList() {
		return partArr;
	}

	/**
	 * Will create a .txt document at the given location and with the given name
	 * <p>
	 * Leaving the value of both strings empty will result in an system outprint
	 * 
	 * @param dir
	 *            the directory where the file should be stored
	 * @param name
	 *            the name of the file
	 */
	public void outprintVesselTreeStructure(String dir, String name) {
		int size = partArr.length;
		if (dir.equals("") && name.equals("")) {
			//both are empty 
			for (int i = 0; i < size; i++) {
				try {
					System.out.println(i + ": " + partArr[i].getName());
				} catch (RPCException | IOException e) {
					e.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "Error printing information", e);
				}
			}
		}

		//TODO
	}

	/**
	 * Returns the part with the given id
	 * 
	 * @param id
	 *            the id of the part
	 * @return the part
	 */
	public Part getPartWithId(int id) {
		return partArr[id];
	}

	/**
	 * Returns a List containing the ids of all engines of the vessel
	 * 
	 * @return the ids of all engines
	 */
	public List<Integer> getAllEngineIds() {
		int size = partArr.length;
		List<Integer> data = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			try {
				if (partArr[i].getEngine() != null) {
					data.add(i);
				}
			} catch (RPCException | IOException e) {
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "Error fetching object", e);
			}
		}
		return data;
	}

	/**
	 * Returns a List containing the ids of all antennas of the vessel
	 * 
	 * @return the ids of all antennas
	 */
	public List<Integer> getAllAntennaIds() {
		int size = partArr.length;
		List<Integer> data = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			try {
				if (partArr[i].getAntenna() != null) {
					data.add(i);
				}
			} catch (RPCException | IOException e) {
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "Error fetching object", e);
			}
		}
		return data;
	}

	/**
	 * Searches for the antenna with the maximum range
	 * 
	 * @return the maximum range of the antenna
	 */
	public double getMaximumAntennaRange() {
		double power = 0;
		Iterator<Integer> dataIt = getAllAntennaIds().iterator();
		while (dataIt.hasNext()) {
			double pow = 0;
			try {
				pow = getPartWithId(dataIt.next()).getAntenna().getPower();
			} catch (RPCException | IOException e) {
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "Failed fetching antenna power", e);
			}
			if (pow > power) {
				power = pow;
			}
		}
		return power;
	}
	/*
	 * _________________________ private
	 */

	/**
	 * Recursivly searches through the vessel to create the partlist
	 * 
	 * @param parts
	 *            the partlist
	 * @param root
	 *            the root part
	 * @param id
	 *            the id of the root part Default 0
	 * @return the id of the current root part
	 */
	private int updateOrderedPartListRecursive(Part[] parts, Part root, int id) {
		parts[id] = root;
		List<Part> childs = null;
		try {
			childs = root.getChildren();
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "Error getting part children", e);
		}
		while (childs.size() > 0) {
			id = updateOrderedPartListRecursive(parts, childs.get(0), id + 1);
			childs.remove(0);
		}
		return id;
	}
}
