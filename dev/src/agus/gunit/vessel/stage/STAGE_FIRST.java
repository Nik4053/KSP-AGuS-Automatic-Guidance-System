/**
 * 
 */
package agus.gunit.vessel.stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import agus.formula.FORMULA_VESSEL;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Part;
import krpc.client.services.SpaceCenter.Propellant;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;
import krpc.client.services.SpaceCenter.Resources;

/**
 * A object that contains all needed information of a stage.
 * <p> Will update the values every time it is called
 * <p> to update the stage id and the one of every following stage use update()
 * @author Niklas Heidnereich
 *
 */
public class STAGE_FIRST extends STAGE {

	private Vessel vessel;
	private int id;
	private STAGE nextStage;
	private Connection connection;

	public STAGE_FIRST(Connection nconnection, Vessel Vessel) throws AGuSDataException {
		LOGGER.logger.info("created first stage");
		this.connection = nconnection;
		this.vessel = Vessel;
		try {
			this.id = vessel.getControl().getCurrentStage();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		this.nextStage = new STAGE_NORMAL(connection, getId() - 1, vessel);

	}

	/**
	 * Returns the ID of the Stage, counted from the last/highest stage as stage
	 * 0 to the current one
	 * 
	 * @return the ID of the Stage
	 * @throws AGuSDataException 
	 */
	@Override
	public int getId() throws AGuSDataException {
		// TODO Auto-generated method stub		
		try {
			if (vessel.getControl().getCurrentStage() != id) {
				update();
			}
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		return id;
	}

	@Override
	public STAGE getStage(int ID) throws AGuSDataException {
		// TODO Auto-generated method stub
		if (getId() != ID) {
			return getNextStage().getStage(ID);
		}
		return this;
	}

	@Override
	public STAGE getNextStage() throws AGuSDataException {
		// TODO Auto-generated method stub
		int nID = -1;
		try {
			nID = vessel.getControl().getCurrentStage();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		if (getId() != nID) {
			this.id = nID;
			this.nextStage = nextStage.getNextStage();

		}
		return nextStage;
	}

	@Override
	public List<Part> getAllParts() throws AGuSDataException {
		// TODO Auto-generated method stub
		return checkAllParts();
	}

	@Override
	public float getMass() throws AGuSDataException {
		// TODO Auto-generated method stub
		return calcMass();
	}

	@Override
	public float getMassAfterBurn() throws AGuSDataException {
		// TODO Auto-generated method stub
		return calcMassAfterBurn();
	}

	@Override
	public float getMassAfterSRBs() throws AGuSDataException {
		return calcMassAfterSRBs();
	}

	@Override
	public double getDeltaV() throws AGuSDataException {
		// TODO Auto-generated method stub
		return calcDeltaV();
	}

	@Override
	public double getDeltaVofSRBs() throws AGuSDataException {
		return calcDeltaVofSRBs();
	}

	@Override
	public float getSpecificImpulse() throws AGuSDataException {
		// TODO Auto-generated method stub
		try {
			return vessel.getSpecificImpulse();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	@Override
	public float getSpecificImpulseofSRBs() throws AGuSDataException {
		return calcSpecificImpulseOfSRBs();
	}

	@Override
	public float getAvailabeThrust() throws AGuSDataException {
		return calcAvailabeThrust();
	}

	@Override
	public float getAcceleration(float percent) throws AGuSDataException {
		if (percent > 0 && percent < 1) {
			float t = 0;
			List<Engine> e = getActiveEngines();
			int s = e.size();
			for (int i = 0; i < s; i++) {
				try {
					if (e.get(i).getThrottleLocked()) {
						t += e.get(i).getMaxVacuumThrust();
					} else {
						t += e.get(i).getMaxVacuumThrust() * percent;
					}
				} catch (RPCException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e1);
					throw new AGuSDataException(e1);
				}
			}
			return t / getMass();
		} else if (percent == 1) {
			return getAvailabeThrust() / getMass();
		} else if (percent == 0) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public float getAccelerationPercent(float acceleration) throws AGuSDataException {
		if (acceleration > 0) {
			float maxth = acceleration * getMass();
			float th = 0;
			float th2 = 0;
			float p = 0;
			List<Engine> e = getActiveEngines();
			int s = e.size();
			for (int i = 0; i < s; i++) {
				try {
					if (e.get(i).getThrottleLocked()) {
						th += e.get(i).getMaxVacuumThrust();
						e.remove(i);
						i--;
						s--;
					} else {
						th2 += e.get(i).getMaxVacuumThrust();
					}
				} catch (RPCException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e1);
					throw new AGuSDataException(e1);
				}
			}
			float th3 = maxth - th;
			if (th3 <= 0) {
				return 0;
			}
			p = th3 / th2;
			if (p > 1) {
				return 1;
			}

			if (p < 0) {
				return 0;
			}
			return p;
		} else if (acceleration == 0) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public float getAccelerationAtEnd(float percent) throws AGuSDataException {
		if (percent > 0 && percent < 1) {
			float t = 0;
			List<Engine> e = getActiveEngines();
			int s = e.size();
			for (int i = 0; i < s; i++) {
				try {
					if (e.get(i).getThrottleLocked()) {
						t += e.get(i).getMaxVacuumThrust();
					} else {
						t += e.get(i).getMaxVacuumThrust() * percent;
					}
				} catch (RPCException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e1);
					throw new AGuSDataException(e1);
				}
			}
			return t / getMassAfterBurn();
		} else if (percent == 1) {
			return getAvailabeThrust() / getMassAfterBurn();
		} else if (percent == 0) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public float getAccelerationPercentAtEnd(float acceleration) throws AGuSDataException {
		if (acceleration > 0) {
			float maxth = acceleration * getMassAfterBurn();
			float th = 0;
			float th2 = 0;
			float p = 0;
			List<Engine> e = getActiveEngines();
			int s = e.size();
			for (int i = 0; i < s; i++) {
				try {
					if (e.get(i).getThrottleLocked()) {
						th += e.get(i).getMaxVacuumThrust();
						e.remove(i);
						i--;
						s--;
					} else {
						th2 += e.get(i).getMaxVacuumThrust();
					}
				} catch (RPCException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e1);
					throw new AGuSDataException(e1);
				}
			}
			float th3 = maxth - th;
			if (th3 <= 0) {
				return 0;
			}
			p = th3 / th2;
			if (p > 1) {
				return 1;
			}

			if (p < 0) {
				return 0;
			}
			return p;
		} else if (acceleration == 0) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public float getBurnTime() throws AGuSDataException {
		return calcBurnTime();
	}

	@Override
	public List<Engine> getActiveEngines() throws AGuSDataException {
		return checkActiveEngines();
	}

	@Override
	public List<Engine> getSRBs() throws AGuSDataException {
		return checkSRBs();
	}

	@Override
	public List<Propellant> getNeededPropellant() throws AGuSDataException {
		// TODO Auto-generated method stub
		return checkNeededPropellant();
	}

	@Override
	public void update() throws AGuSDataException {
		LOGGER.logger.info("updating stages");
		try {
			this.id = vessel.getControl().getCurrentStage();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		this.nextStage = new STAGE_NORMAL(connection, id - 1, vessel);
		// this.nextStage
	}

	@Override
	public List<Part> getDecoupledParts() {
		List<Part> p = new ArrayList<Part>();
		return p;
	}

	@Override
	public List<Part> getActivatedParts() throws AGuSDataException {
		List<Part> p = new ArrayList<Part>();
		try {
			p = vessel.getParts().inStage(id);
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		return p;
	}
	

	@Override
	public int getNumberOfIgnitions() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * --------------------------------------------------------------------------------------------------------
	 * The calculation: private methods
	 * @throws AGuSDataException 
	 */

	private List<Part> checkAllParts() throws AGuSDataException {
		try {
			return vessel.getParts().getAll();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	private float calcMass() throws AGuSDataException {
		try {
			return vessel.getMass();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	private float calcMassAfterBurn() throws AGuSDataException {
		float m = getMass();
		List<Propellant> pro = getNeededPropellant();
		Resources res = null;
		try {
			res = vessel.resourcesInDecoupleStage(getId() , false);
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		int spro = pro.size();
		for (int i = 0; i < spro; i++) {
			try {
				m = m - (res.amount(pro.get(i).getName()) * Resources.density(connection, pro.get(i).getName()));

			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}
		return m;
	}

	private double calcDeltaV() throws AGuSDataException {
		double dV;
		dV = FORMULA_VESSEL.DeltaVofStage(getMass(), getMassAfterBurn(), getSpecificImpulse());
		return dV;
	}

	private double calcDeltaVofSRBs() throws AGuSDataException {
		double dV;
		dV = FORMULA_VESSEL.DeltaVofStage(getMass(), getMassAfterSRBs(), getSpecificImpulseofSRBs());
		return dV;
	}

	private float calcMassAfterSRBs() throws AGuSDataException {
		float m = getMass();
		List<Engine> srb = getSRBs();
		int s = srb.size();
		for (int i = 0; i < s; i++) {
			try {
				m = (float) (m - (srb.get(i).getPart().getMass() - srb.get(i).getPart().getDryMass()));
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}
		return m;
	}

	private float calcSpecificImpulseOfSRBs() throws AGuSDataException {
		float cisp = 0;
		List<Engine> srb = getSRBs();
		int s = srb.size();
		if (s == 0) {
			return 0;
		}
		for (int i = 0; i < s; i++) {
			try {
				cisp += srb.get(i).getSpecificImpulse();
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}
		cisp = cisp / s;
		return cisp;
	}

	private float calcAvailabeThrust() throws AGuSDataException {
		try {
			return vessel.getAvailableThrust();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	private float calcBurnTime() throws AGuSDataException {
		return FORMULA_VESSEL.BurnTime(getAvailabeThrust(), getSpecificImpulse(), getMass(), getDeltaV());
	}

	private List<Engine> checkActiveEngines() throws AGuSDataException {
		// TODO Auto-generated method stub
		List<Engine> Engines = null;
		try {
			Engines = vessel.getParts().getEngines();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		List<Engine> aEngines = new ArrayList<Engine>();
		int s = Engines.size();
		for (int i = 0; i < s; i++) {
			Engine en = Engines.get(i);
			try {
				if (en.getActive() == true) {
					aEngines.add(en);
				}
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}

		}
		return aEngines;
	}

	private List<Engine> checkSRBs() throws AGuSDataException {
		List<Engine> srb = new ArrayList<Engine>();
		List<Engine> en = getActiveEngines();
		int se = en.size();
		for (int i = 0; i < se; i++) {
			try {
				if (en.get(i).getThrottleLocked()) {
					srb.add(en.get(i));
				}
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}
		return srb;
	}

	private List<Propellant> checkNeededPropellant() throws AGuSDataException {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		List<Propellant> pr = new ArrayList<Propellant>();

		List<Engine> en = getActiveEngines();
		int s = en.size();
		for (int i = 0; i < s; i++) {
			List<Propellant> p = null;
			try {
				p = en.get(i).getPropellants();
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
			int sp = p.size();
			for (int j = 0; j < sp; j++) {
				Propellant pro = p.get(j);
				boolean exists = false;
				int spr = pr.size();
				for (int ipr = 0; ipr < spr && exists == false; ipr++) {
					try {
						if (pr.get(ipr).getName().equals(pro.getName())) {
							exists = true;
						}
					} catch (RPCException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						LOGGER.logger.log(Level.SEVERE, "", e);
						throw new AGuSDataException(e);
					}

				}
				if (exists == false) {
					pr.add(p.get(j));
				}
			}
		}
		return pr;
	}

}
