package agus.gunit.sequence;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

import agus.exception.AGuSUnknownOrbitException;
import agus.gunit.BODIES;
import agus.gunit.BODY;
import agus.gunit.ORBIT;
import agus.gunit.vessel.VESSEL;
import nhlog.LOGGER;
/**
 * The standard sequence used for creating different transfers
 * @author Niklas Heidenreich
 *
 */
public class SEQUENCE_NORMAL extends SEQUENCE {

	/**
	 * the start Orbit of the sequence
	 */
	protected ORBIT sOrbit;
	/**
	 * the wanted end orbit of the sequence
	 */
	protected ORBIT eOrbit;

	/**
	 * the current orbit of the sequence
	 */
	private ORBIT cOrbit;
	/**
	 * 
	 */
	protected LinkedList<TRANSFER> transferList = new LinkedList<TRANSFER>();
	/**
	 * 
	 */
	protected VESSEL vessel;
	private BODIES bodies;

	public SEQUENCE_NORMAL(VESSEL vessel, BODIES Bodies, ORBIT startOrbit, ORBIT endOrbit) {
		this.bodies = Bodies;
		this.sOrbit = new ORBIT(startOrbit);
		this.eOrbit = new ORBIT(endOrbit);
		this.cOrbit = new ORBIT(startOrbit);
		this.transferList.add(new TRANSFER_START(startOrbit));
		// ListIterator<TRANSFER> li =transferList.listIterator();
		if (checkIfFinalOrbitHasBeenAlreadyReached() == false) {
			try {
				createSequence();
			} catch (AGuSUnknownOrbitException e) {
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "Sequence had failed orbit creations... aborting...", e);
			}
		}
		// removes first unnecessary element
		this.transferList.removeFirst();
		// deleteEmptyTransfer();
	}

	public ORBIT getFinalOrbit() throws AGuSUnknownOrbitException {
		return transferList.getLast().getFinalOrbit();
	}

	public LinkedList<TRANSFER> getTransferList() {
		return transferList;
	}

	public double getDeltaV() {
		double dv = 0;
		int s = transferList.size();
		for (int i = 0; i < s; i++) {
			dv += transferList.get(i).getDeltaV();
		}
		return dv;
	}

	public int getNumberOfBurns() {
		return transferList.size();
	}

	public double getTimeNeeded() {
		return transferList.getLast().getUTend() - transferList.getFirst().getUTstart();
	}

	@Override
	public void update(ORBIT currentOrbit) {
		// TODO Auto-generated method stub
	}

	/*
	 * ___________________________________________________________________
	 * private methods
	 */

	private void createSequence() throws AGuSUnknownOrbitException {
		LOGGER.logger.info("creating sequence");
		if (cOrbit.getBody().getName().equals(eOrbit.getBody().getName()) == false) {
			wrongBody();
		}
		correctBody();
	}

	/**
	 * Checks if the start Orbit is equal to the end Orbit
	 * 
	 * @return true if they are identical
	 */
	private boolean checkIfFinalOrbitHasBeenAlreadyReached() {
		if (Math.round(sOrbit.getApoapsis()) == Math.round(eOrbit.getApoapsis())) {
			if (Math.round(sOrbit.getPeriapsis()) == Math.round(eOrbit.getPeriapsis())) {
				// if(Math.round(sOrbit.getInclination())==Math.round(eOrbit.getInclination())){
				return true;
				// }
			}
		}
		return false;
	}

	/**
	 * If the current body is wrong
	 * @throws AGuSUnknownOrbitException 
	 */
	private void wrongBody() throws AGuSUnknownOrbitException {

		Iterator<BODY> bIter = bodies.getMinBodiesBetween(cOrbit.getBody(), eOrbit.getBody()).iterator();
		ORBIT o = new ORBIT(cOrbit);
		BODY B1 = bIter.next();// will be the current body
		while (bIter.hasNext()) {
			BODY B2 = bIter.next(); // will be the next body
			o.setBody(B2);
			if (B1.getParentBody()!=null&&B1.getParentBody().getName().equals(B2.getName())) {
				//The null check is necessary to prevent an nullpointer with the center body (Sun)
				// Next body is the parent body of this body
				transferList.add(new TRANSFER_LEAVE_SOI(cOrbit, o));
				cOrbit = new ORBIT(transferList.getLast().getFinalOrbit());
			} else {
				// Next body is a satellite of this body
				LOGGER.logger.warning("Entering satellite body soi is not implemented");
			}
		}
		correctBody();
	}

	/**
	 * if the vessel is already in orbit around the correct body
	 * @throws AGuSUnknownOrbitException 
	 */
	private void correctBody() throws AGuSUnknownOrbitException {
		ORBIT o = new ORBIT(transferList.getLast().getFinalOrbit());
		if (o.getApoapsis() != eOrbit.getApoapsis() || o.getPeriapsis() != eOrbit.getPeriapsis()) {
			// needs to burn to higher apoapsis
			transferList.add(findBestTransferForAltitude(o, eOrbit));
		}
		o = new ORBIT(transferList.getLast().getFinalOrbit());
		if (o.getInclination() != eOrbit.getInclination()) {
			findBestTransferTimeForInclination(transferList.getLast(), eOrbit);
		}
	}

	/**
	 * Will find the best way to get from the start orbit to the end orbit
	 * 
	 * @param startOrbit
	 * @param endOrbit
	 * @return
	 */
	private TRANSFER findBestTransferForAltitude(ORBIT startOrbit, ORBIT endOrbit) {
		// TODO check hohmann, biiliptical, gravity assists
		TRANSFER transfer = null;
		// object zum vergleichen von transfer und t
		TRANSFER t = null;
		transfer = new TRANSFER_HOHMANN(startOrbit, endOrbit);
		return transfer;
	}

	/**
	 * will find the best Moment for an inclination change
	 * 
	 * @return
	 * @throws AGuSUnknownOrbitException 
	 */
	private void findBestTransferTimeForInclination(TRANSFER transfer, ORBIT endOrbit) throws AGuSUnknownOrbitException {
		LinkedList<MANEUVER> ma = transfer.getManeuverList();
		int s = ma.size();
		double aphight = 0;
		int highestTransfer = s - 1;
		for (int i = 0; i < s; i++) {
			double ap = ma.get(i).getOrbit().getApoapsis();
			if (ap > aphight) {
				aphight = ap;
				highestTransfer = i;
			}
		}
		for (int i = s - 1; i > highestTransfer; i--) {
			transferList.removeLast();
		}

		// TODO
		// transferList.add(findBestTransferForInclination(transferList.getLast().getFinalOrbit(),
		// endOrbit));
		transferList.add(findBestTransferForAltitude(transferList.getLast().getFinalOrbit(), endOrbit));
	}

	/**
	 * TODO
	 * 
	 * @param startOrbit
	 * @param endOrbit
	 * @return null
	 */
	private TRANSFER findBestTransferForInclination(ORBIT startOrbit, ORBIT endOrbit) {
		// TODO create inlclination transfer
		return null;
	}

	/**
	 * Deletes empty transfers
	 * 
	 * @deprecated not needed if sequence works correctly
	 */
	private void deleteEmptyTransfer() {
		if (transferList.isEmpty()) {
			transferList.removeFirst();
		} else if (transferList.getFirst() == null) {
			transferList.removeFirst();
		} else if (transferList.getFirst().getManeuverList() == null) {
			transferList.removeFirst();
		} else if (transferList.getFirst().getDeltaV() == 0) {
			transferList.removeFirst();
		}

	}

}
