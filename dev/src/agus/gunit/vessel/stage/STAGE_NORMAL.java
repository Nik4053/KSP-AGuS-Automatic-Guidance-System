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
import krpc.client.services.SpaceCenter.Resources;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;

/**
 * A object that contains all needed information of a stage.
 * <p>Will update only when update() is called
 * @author Niklas Heidenreich
 *
 */
public class STAGE_NORMAL extends STAGE {

	private Connection connection;
	private Vessel vessel;
	private int id;
	private STAGE nextStage;
	private List<Part> allParts;
	private float mass;
	private float mf;
	private float mfsrb;
	private double deltaV;
	private double deltaVSRB;
	private float isp;
	private float ispSRB;
	private float availabeThrust;
	private float burnTime;
	private List<Engine> activeEngines;
	private List<Engine> SRBs;
	private List<Propellant> neededPropellant;
	private List<Part> decoupledParts;
	private List<Part> activatedParts;

	public STAGE_NORMAL(Connection nconnection, int ID, Vessel nvessel) throws AGuSDataException {
		this.connection = nconnection;
		this.vessel = nvessel;
		this.id = ID;
		// if this is the last stage or not
		if (getId() > 0) {
			this.nextStage = new STAGE_NORMAL(connection, getId() - 1, vessel);
		} else {
			this.nextStage = new STAGE_END(vessel);
		}
		this.allParts = checkAllParts();
		this.mass = calcMass();
		this.activeEngines = checkActiveEngines();
		this.neededPropellant = checkNeededPropellant();
		this.mf = calcMassAfterBurn();
		this.SRBs = checkSRBs();
		this.mfsrb = calcMassAfterSRBs();
		this.isp = calcSpecificImpulse();
		this.deltaV = calcDeltaV();
		this.ispSRB = calcSpecificImpulseofSRBs();
		this.deltaVSRB = calcDeltaVofSRBs();
		this.decoupledParts = checkDecoupledParts();
		this.activatedParts = checkActivatedParts();
		this.availabeThrust = calcAvailableThrust();
		this.burnTime = calcBurnTime();
	}
	
	public STAGE_NORMAL(Connection nConnection, Vessel nVessel,STAGE stage) throws AGuSDataException {
		this.connection = nConnection;
		this.vessel = nVessel;
		this.id = stage.getId();
		// if this is the last stage or not
		if (getId() > 0) {
			this.nextStage = new STAGE_NORMAL(connection, getId() - 1, vessel);
		} else {
			this.nextStage = new STAGE_END(vessel);
		}

		this.allParts = checkAllParts();
		this.mass = calcMass();
		this.activeEngines = checkActiveEngines();
		this.neededPropellant = checkNeededPropellant();
		this.mf = calcMassAfterBurn();
		this.SRBs = checkSRBs();
		this.mfsrb = calcMassAfterSRBs();
		this.isp = calcSpecificImpulse();
		this.deltaV = calcDeltaV();
		this.ispSRB = calcSpecificImpulseofSRBs();
		this.deltaVSRB = calcDeltaVofSRBs();
		this.decoupledParts = checkDecoupledParts();
		this.activatedParts = checkActivatedParts();
		this.availabeThrust = calcAvailableThrust();
		this.burnTime = calcBurnTime();
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
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
	public STAGE getNextStage() {
		// TODO Auto-generated method stub
		return nextStage;
	}

	@Override
	public List<Part> getAllParts() {
		// TODO Auto-generated method stub
		return allParts;
	}

	@Override
	public float getMass() {
		// TODO Auto-generated method stub
		return mass;
	}

	@Override
	public float getMassAfterBurn() {
		// TODO Auto-generated method stub
		return mf;
	}

	@Override
	public float getMassAfterSRBs() {
		return mfsrb;
	}

	@Override
	public double getDeltaV() {
		// TODO Auto-generated method stub
		return deltaV;
	}

	@Override
	public double getDeltaVofSRBs() {
		// TODO Auto-generated method stub
		return deltaVSRB;
	}

	@Override
	public float getSpecificImpulse() {
		// TODO Auto-generated method stub
		return isp;
	}

	@Override
	public float getSpecificImpulseofSRBs() {
		return ispSRB;
	}

	@Override
	public float getAvailabeThrust() {
		return availabeThrust;
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
	public float getBurnTime() {
		return burnTime;
	}

	@Override
	public List<Engine> getActiveEngines() {
		// TODO Auto-generated method stub
		return activeEngines;
	}

	@Override
	public List<Engine> getSRBs() {
		// TODO Auto-generated method stub
		return SRBs;
	}

	@Override
	public List<Propellant> getNeededPropellant() {
		// TODO Auto-generated method stub
		return neededPropellant;
	}

	@Override
	public List<Part> getDecoupledParts() {
		// TODO Auto-generated method stub
		return decoupledParts;
	}

	@Override
	public List<Part> getActivatedParts() {
		// TODO Auto-generated method stub
		return activatedParts;
	}

	@Override
	public void update() throws AGuSDataException {
		// TODO Auto-generated method stub
		try {
			if(getId()>vessel.getControl().getCurrentStage())
			{
				nextStage=getNextStage().getNextStage();
				this.id--;
			}
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		nextStage.update();
		this.allParts = checkAllParts();
		this.mass = calcMass();
		this.activeEngines = checkActiveEngines();
		this.neededPropellant = checkNeededPropellant();
		this.mf = calcMassAfterBurn();
		this.SRBs = checkSRBs();
		this.mfsrb = calcMassAfterSRBs();
		this.isp = calcSpecificImpulse();
		this.deltaV = calcDeltaV();
		this.ispSRB = calcSpecificImpulseofSRBs();
		this.deltaVSRB = calcDeltaVofSRBs();
		this.decoupledParts = checkDecoupledParts();
		this.activatedParts = checkActivatedParts();
		this.availabeThrust = calcAvailableThrust();
		this.burnTime = calcBurnTime();
	}
	

	@Override
	public int getNumberOfIgnitions() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * --------------------------------------------------------------------------------------------------------
	 * The calculation: private methods
	 */

	/**
	 * Uses the parts that the next stage has and adds the ones that will get
	 * decoupled next stage
	 * 
	 * @return all Parts that the rocket contains
	 * @throws AGuSDataException 
	 */
	private List<Part> checkAllParts() throws AGuSDataException {
		List<Part> p = nextStage.getAllParts();
		p.addAll(nextStage.getDecoupledParts());
		return p;
	}

	/**
	 * Uses the mass of the next stage and adds the mass of every part that will
	 * be decoupled in the next stage
	 * 
	 * @return the mass of this stage
	 * @throws AGuSDataException 
	 */
	private float calcMass() throws AGuSDataException {
		float m = nextStage.getMass();
		List<Part> p = nextStage.getDecoupledParts();
		int s = p.size();
		for (int i = 0; i < s; i++) {
			try {
				m += p.get(i).getMass();
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}
		return m;
	}

	private float calcMassAfterBurn() throws AGuSDataException {
		float m = getMass();
		List<Propellant> pro = getNeededPropellant();
		Resources res = null;
		try {
			res = vessel.resourcesInDecoupleStage(getId(), false);
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

	private double calcDeltaV() {
		double dV;
		dV = FORMULA_VESSEL.DeltaVofStage(getMass(), getMassAfterBurn(), getSpecificImpulse());
		return dV;
	}

	private double calcDeltaVofSRBs() {
		double dV;
		dV = FORMULA_VESSEL.DeltaVofStage(getMass(), getMassAfterSRBs(), getSpecificImpulseofSRBs());
		return dV;
	}

	private float calcSpecificImpulse() throws AGuSDataException {
		float cisp = 0;
		float thrust = 0;
		List<Engine> en = getActiveEngines();
		int s = en.size();

		for (int i = 0; i < s; i++) {
			if (i == 0) {
				try {
					cisp = en.get(0).getVacuumSpecificImpulse();
					thrust = en.get(0).getMaxVacuumThrust();
				} catch (RPCException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e);
					throw new AGuSDataException(e);
				}
			} else {
				try {
					cisp = FORMULA_VESSEL.CombinedSpecificImpulse(thrust, en.get(i).getMaxVacuumThrust(), cisp,
							en.get(i).getVacuumSpecificImpulse());
					thrust += en.get(i).getMaxVacuumThrust();
				} catch (RPCException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e);
					throw new AGuSDataException(e);
				}
			}
		}

		return cisp;
	}

	private float calcSpecificImpulseofSRBs() throws AGuSDataException {
		float cisp = 0;
		float thrust = 0;
		List<Engine> srb = getSRBs();
		int s = srb.size();
		for (int i = 0; i < s; i++) {
			if (i == 0) {
				try {
					cisp = srb.get(0).getVacuumSpecificImpulse();
					thrust = srb.get(0).getMaxVacuumThrust();
				} catch (RPCException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e);
					throw new AGuSDataException(e);
				}
			} else {
				try {
					cisp = FORMULA_VESSEL.CombinedSpecificImpulse(thrust, srb.get(i).getMaxVacuumThrust(), cisp,
							srb.get(i).getVacuumSpecificImpulse());
					thrust += srb.get(i).getMaxVacuumThrust();
				} catch (RPCException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e);
					throw new AGuSDataException(e);
				}
			}
		}
		return cisp;
	}

	private float calcAvailableThrust() throws AGuSDataException {
		List<Engine> en = getActiveEngines();
		float thrust = 0;
		int s = en.size();
		for (int i = 0; i < s; i++) {
			try {
				thrust += en.get(i).getMaxVacuumThrust();
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}
		return thrust;
	}

	private float calcBurnTime() {
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
			Part p = null;
			try {
				p = Engines.get(i).getPart();
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
			try {
				if (p.getDecoupleStage() < getId() && p.getStage() >= getId()) {
					aEngines.add(Engines.get(i));
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
		List<Engine> e = getActiveEngines();
		int se = e.size();
		for (int i = 0; i < se; i++) {
			try {
				if (e.get(i).getThrottleLocked()) {
					srb.add(e.get(i));
				}
			} catch (RPCException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e1);
				throw new AGuSDataException(e1);
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

	private List<Part> checkDecoupledParts() throws AGuSDataException {
		// TODO Auto-generated method stub
		List<Part> p = new ArrayList<Part>();
		try {
			p = vessel.getParts().inDecoupleStage(getId());
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		return p;
	}

	private List<Part> checkActivatedParts() throws AGuSDataException {
		// TODO Auto-generated method stub
		List<Part> p = new ArrayList<Part>();
		try {
			p = vessel.getParts().inStage(getId());
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		return p;
	}

}
