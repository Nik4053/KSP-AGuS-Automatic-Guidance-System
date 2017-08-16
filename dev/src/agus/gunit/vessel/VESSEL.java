package agus.gunit.vessel;

import agus.exception.AGuSDataException;
import agus.formula.FORMULA_VESSEL;
import agus.gunit.vessel.stage.STAGE;
import agus.gunit.vessel.stage.STAGE_FIRST;
import agus.gunit.vessel.stage.STAGE_NORMAL;
import krpc.client.Connection;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;

/**
 * Contains all the data of the vessel
 * 
 * @author Niklas Heidenreich
 *
 */
public class VESSEL {

	private Connection connection;
	private SpaceCenter spaceCenter;
	private Vessel vessel;
	private PARTS parts;
	private STAGE stage;
	private STAGING staging;

	public VESSEL(Connection Connection, SpaceCenter SpaceCenter, Vessel Vessel) throws AGuSDataException {
		this.connection = Connection;
		this.vessel = Vessel;
		this.spaceCenter = SpaceCenter;
		this.stage = new STAGE_FIRST(connection, vessel);
		this.staging = new STAGING(spaceCenter, vessel, stage);
		this.parts = new PARTS(this);
	}

	/**
	 * Will return the vessel object from krpc
	 * 
	 * @return the vessel
	 */
	public Vessel getVessel() {
		return vessel;
	}

	/**
	 * All the parts of the vessel
	 * 
	 * @return the part object
	 */
	public PARTS getParts() {
		return parts;
	}

	/**
	 * Returns the current stage object
	 * <p>
	 * The returned object will automatically update all its variables
	 * 
	 * @return the Stage object
	 * @throws AGuSDataException
	 */
	public STAGE getCurrentStage() throws AGuSDataException {
		return staging.getCurrentStage();
	}

	/**
	 * Recursively searches through all stage objects until it finds the one with
	 * the given id.
	 * 
	 * @param ID
	 *            the id of the stage you want to have returned
	 * @return the STAGE object with this id. Will return an empty stage (-1) if
	 *         stage id is not found
	 * @throws AGuSDataException
	 */
	public STAGE getStage(int ID) throws AGuSDataException {
		return getCurrentStage().getStage(ID);
	}

	/**
	 * TODO testing Returns the current stage object
	 * <p>
	 * The returned object will be "static" and not update an variable whatsoever
	 * 
	 * @return
	 * @throws AGuSDataException
	 */
	public STAGE getStaticCurrentStage() throws AGuSDataException {
		return new STAGE_NORMAL(connection, vessel, getCurrentStage());
	}

	/**
	 * Can be used to automatically stage the vessel
	 * 
	 * @return The STAGING object
	 */
	public STAGING getStaging() {
		return staging;
	}

	/**
	 * Returns the combined deltaV of the vessel
	 * 
	 * @return the deltaV in m/s
	 * @throws AGuSDataException
	 */
	public double getDeltaV() throws AGuSDataException {
		double dV = 0;
		STAGE stage = getCurrentStage();
		dV = stage.getDeltaV();
		while (stage.getId() > -1) {
			stage = stage.getNextStage();
			dV += stage.getDeltaV();
		}
		return dV;
	}

	/**
	 * Calculates the needed burntime
	 * 
	 * @param deltaV
	 *            the deltaV in m/s
	 * @return the burntime in seconds
	 * @throws AGuSDataException 
	 */
	public double getBurnTime(double deltaV) throws AGuSDataException {
		return getBurnTime(deltaV, 0);
	}

	/**
	 * Calculates the needed burntime
	 * 
	 * @param deltaV
	 *            the deltaV in m/s
	 * @param inDV
	 *            In how many deltaV this calculation should start
	 * @return the burntime in seconds
	 * @throws AGuSDataException 
	 */
	public double getBurnTime(double deltaV, double inDV) throws AGuSDataException {
		STAGE stage = getCurrentStage();
		double burnTime = 0;
		while (inDV >= 0) {
			if (stage.getDeltaV() >= inDV) {
				double bTime = stage.getBurnTime() * (inDV / stage.getDeltaV());
				burnTime += stage.getBurnTime() - bTime;
				deltaV -= inDV;
				inDV = -1;
			} else {
				deltaV -= stage.getDeltaV();
				//burnTime += stage.getBurnTime();
				inDV -= stage.getDeltaV();
				stage= stage.getNextStage();
			}
		}
		if (deltaV < stage.getDeltaV()) {			
			return burnTime;
		} else {
			deltaV -= stage.getDeltaV();
			burnTime += getBurnTimeRekursive(deltaV, stage.getNextStage());
			return burnTime;
		}
	}

	/**
	 * Used by getBurnTime()
	 * 
	 * @param deltaV
	 * @param stage
	 *            the current stage to be checked
	 * @return
	 * @throws AGuSDataException 
	 */
	private double getBurnTimeRekursive(double deltaV, STAGE stage) throws AGuSDataException {
		if (stage.getId()==-1) {
			// if there is not enough deltaV
			LOGGER.logger.warning("Not enough deltaV: needed " + deltaV + "m/s more");
			return 0;
		}
		if (deltaV < stage.getDeltaV()) {
			double burnTime = FORMULA_VESSEL.BurnTime(stage.getAvailabeThrust(), stage.getSpecificImpulse(),
					stage.getMass(), deltaV);
			return burnTime;
		} else {
			double burnTime = stage.getBurnTime();
			deltaV -= stage.getDeltaV();
			burnTime += getBurnTimeRekursive(deltaV, stage.getNextStage());
			return burnTime;
		}
	}

}
