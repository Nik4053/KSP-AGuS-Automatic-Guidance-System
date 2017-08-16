/**
 * 
 */
package agus.gunit.vessel.stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Part;
import krpc.client.services.SpaceCenter.Propellant;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;

/**
 * The last stage with the id of -1
 * @author Niklas Heidenreich
 *
 */
public class STAGE_END extends STAGE {

	private Vessel vessel;
	private List<Part> decoupledParts;

	public STAGE_END(Vessel vessel) throws AGuSDataException {
		this.vessel = vessel;
		this.decoupledParts = checkDecoupledParts();
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public STAGE getStage(int id) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public STAGE getNextStage() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public List<Part> getAllParts() {
		// TODO Auto-generated method stub
		List<Part> p = new ArrayList<Part>();
		return p;
	}

	@Override
	public float getMass() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getMassAfterBurn() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getMassAfterSRBs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDeltaV() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDeltaVofSRBs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getSpecificImpulse() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getSpecificImpulseofSRBs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getAvailabeThrust() {
		return 0;
	}

	@Override
	public float getAcceleration(float percent) {
		return 0;
	}

	@Override
	public float getAccelerationPercent(float acceleration) {
		return 0;
	}

	@Override
	public float getAccelerationAtEnd(float percent) {
		return 0;
	}

	@Override
	public float getAccelerationPercentAtEnd(float acceleration) {
		return 0;
	}

	@Override
	public float getBurnTime() {
		return 0;
	}

	@Override
	public List<Engine> getActiveEngines() {
		// TODO Auto-generated method stub
		List<Engine> e = new ArrayList<Engine>();
		return e;
	}

	@Override
	public List<Engine> getSRBs() {
		// TODO Auto-generated method stub
		List<Engine> e = new ArrayList<Engine>();
		return e;
	}

	@Override
	public List<Propellant> getNeededPropellant() {
		// TODO Auto-generated method stub
		List<Propellant> p = new ArrayList<Propellant>();
		return p;
	}

	@Override
	public List<Part> getDecoupledParts() {
		// TODO Auto-generated method stub}
		return decoupledParts;
	}

	@Override
	public List<Part> getActivatedParts() {
		// TODO Auto-generated method stub
		List<Part> p = new ArrayList<Part>();
		return p;
	}

	@Override
	public int getNumberOfIgnitions() {
		return 0;
	}

	@Override
	public void update() throws AGuSDataException {
		// TODO Auto-generated method stub
		this.decoupledParts = checkDecoupledParts();
	}

	/**
	 * --------------------------------------------------------------------------------------------------------
	 * The calculation: private methods
	 * 
	 * @throws AGuSDataException
	 */

	private List<Part> checkDecoupledParts() throws AGuSDataException {
		// TODO Auto-generated method stub
		List<Part> p = new ArrayList<Part>();
		try {
			p.addAll(vessel.getParts().inDecoupleStage(-1));
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		return p;
	}

}
