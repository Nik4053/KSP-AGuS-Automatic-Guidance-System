/**
 * 
 */
package agus.gunit.vessel.stage;

import java.util.List;
import agus.exception.AGuSDataException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Part;
import krpc.client.services.SpaceCenter.Propellant;

/**
 * A object that will contain all needed information of a stage
 * 
 * @author Niklas Heidenreich
 *
 */
public abstract class STAGE {
	/**
	 * Returns the ID of the Stage, counted from the last/highest stage as stage
	 * 0 to the current one and -1 for parts that are never decoupled
	 * 
	 * @return the ID of the Stage
	 */
	public abstract int getId() throws AGuSDataException;

	/**
	 * Checks through all stages recursive
	 * 
	 * @param id
	 *            The Id of the Stage you want to have
	 * @return the stage with the given ID. returns stage with id -1 when
	 *         nothing is found
	 */
	public abstract STAGE getStage(int id) throws AGuSDataException;

	/**
	 * This will return the current stage - 1
	 * 
	 * @return The next Stage
	 */
	public abstract STAGE getNextStage() throws AGuSDataException;

	/**
	 * All parts that are contained in this stage and every stage coming after
	 * that.
	 * <p>
	 * Achieved by checking through all stages recursive
	 * </p>
	 * 
	 * @return a List containing all Parts of the Vessel
	 */
	public abstract List<Part> getAllParts() throws AGuSDataException;

	/**
	 * 
	 * @return The Mass of the rocket at the time this stage is activated
	 */
	public abstract float getMass() throws AGuSDataException;

	/**
	 * 
	 * @return The Mass after the Burn was executed
	 */
	public abstract float getMassAfterBurn() throws AGuSDataException;

	/**
	 * 
	 * @return The Mass of the Vessel after all engines have fired
	 */
	public abstract float getMassAfterSRBs() throws AGuSDataException;

	/**
	 * 
	 * @return The maximum DeltaV that can be achieved with this stage
	 */
	public abstract double getDeltaV() throws AGuSDataException;

	/**
	 * 
	 * @return The DeltaV that the SRBs of this stage can achieve. returns 0 if
	 *         there are no SRBs
	 */
	public abstract double getDeltaVofSRBs() throws AGuSDataException;

	/**
	 * Calculated by using the formula described here:
	 * http://wiki.kerbalspaceprogram.com/wiki/Specific_impulse#Multiple_engines
	 * 
	 * @return The combined specific impulse of all engines, in seconds.
	 */
	public abstract float getSpecificImpulse() throws AGuSDataException;

	/**
	 * 
	 * @return The combined specific impulse of all SRBs, in seconds.
	 */
	public abstract float getSpecificImpulseofSRBs() throws AGuSDataException;

	/**
	 * 
	 * @return The maximum available Thrust
	 */
	public abstract float getAvailabeThrust() throws AGuSDataException;

	/**
	 * Calculates the acceleration of the Vessel if all engines are set to the
	 * given percent
	 * 
	 * @param percent
	 *            The percent form 0-1 at which the acceleration should be
	 *            calculated
	 * @return The acceleration of the vessel in m/s
	 */
	public abstract float getAcceleration(float percent) throws AGuSDataException;

	/**
	 * Calculates the percent to which the engines should be set given the
	 * acceleration they should produce
	 * 
	 * @param acceleration
	 *            The acceleration in m/s which the Vessel should have
	 * @return the percent from 0 to 1 to which the engines should be set
	 */
	public abstract float getAccelerationPercent(float acceleration) throws AGuSDataException;

	/**
	 * Calculates the acceleration of the Vessel if all engines are set to the
	 * given percent at th End of the Burn
	 * 
	 * @param percent
	 *            The percent form 0-1 at which the acceleration should be
	 *            calculated
	 * @return The acceleration of the vessel in m/s
	 */
	public abstract float getAccelerationAtEnd(float percent) throws AGuSDataException;

	/**
	 * Calculates the percent to which the engines should be set given the
	 * acceleration they should produce at the End of the Burn
	 * 
	 * @param acceleration
	 *            The acceleration in m/s which the Vessel should have
	 * @return the percent from 0 to 1 to which the engines should be set
	 */
	public abstract float getAccelerationPercentAtEnd(float acceleration) throws AGuSDataException;

	/**
	 * 
	 * @return The maximum Burntime of the Stage if the Engines are burning at
	 *         100% throttle
	 */
	public abstract float getBurnTime() throws AGuSDataException;

	/**
	 * 
	 * @return returns a List containing all Engines of the stage
	 */
	public abstract List<Engine> getActiveEngines() throws AGuSDataException;

	/**
	 * 
	 * @return returns a List containing all SRBs of the stage
	 */
	public abstract List<Engine> getSRBs() throws AGuSDataException;

	/**
	 * gets all propellants that are used on this stage engines
	 * 
	 * @return the propellant
	 */
	public abstract List<Propellant> getNeededPropellant() throws AGuSDataException;

	/**
	 * 
	 * @return a list containing all Parts that are decoupled in this stage
	 */
	public abstract List<Part> getDecoupledParts() throws AGuSDataException;

	/**
	 * 
	 * @return a list containing all Parts that are active/activated in this
	 *         stage
	 */
	public abstract List<Part> getActivatedParts() throws AGuSDataException;

	/**
	 * CURRENTLY ALWAY RETURNS 0 The number of ignitions that the engines with
	 * the most remaining ignitions on this stage has
	 * 
	 * @return remaining ignitions
	 */
	public abstract int getNumberOfIgnitions() throws AGuSDataException;

	/**
	 * Updates this stage and every following one
	 */
	public abstract void update() throws AGuSDataException;

}
