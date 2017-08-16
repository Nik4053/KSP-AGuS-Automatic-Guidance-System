/**
 * 
 */
package agus.gunit.vessel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import agus.gunit.vessel.stage.STAGE;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Part;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;

/**
 * This class is checking if the vessel should stage
 * 
 * @version 16.06.2017
 * @author Niklas Heidenreich
 */
public class STAGING {
	private SpaceCenter sCenter;
	private Vessel vessel;
	private STAGE currentStage;
	private Flight flight;
	private Thread autoStaging;
	/**
	 * If set to false there will be no automatic staging threads created
	 * Already created ones will still exist.
	 * <p>
	 * Remove them with stopAutomaticSTaging();
	 */
	private boolean automaticStagingAllowed;
	/**
	 * If set to false all already created automatic staging thread ones will
	 * get terminated
	 * <p>
	 * will automatically be set to true if startAutomaticSTaging() is called
	 */
	private boolean automaticStagingActive;
	/**
	 * if this is set to true the staging thread will start to sleep until it is set to false again
	 */
	private boolean stagingThreadRun;
	/**
	 * The current MET
	 */
	private double met;
	/**
	 * List containing all parts that will be decoupled in the next stage
	 */
	private List<Part> partDeList;
	/**
	 * size of the partDeList
	 */
	private int pds;
	/**
	 * List containing all parts that will be Activated in the next stage
	 */
	private List<Part> partAcList;
	/**
	 * size of the partAcList
	 */
	private int pas;

	/**
	 * True if there is no propellant in the stage
	 */
	private boolean propellant;
	/**
	 * The MET (Mission elapsed time) at when to check next
	 */
	private double nextCheckPropellant;
	/**
	 * True if there are fairings are ready to decouple in the next stage or if
	 * there are none
	 */
	private boolean fairings;
	/**
	 * True if there is a launch escape system ready to decouple in the next
	 * stage or if there is none
	 */
	private boolean launchEscape;
	/**
	 * True if there is a launch clamp ready to activate in the next stage or if
	 * there is none
	 */
	private boolean launchClamp;

	/**
	 * True if there is a parachute ready to deploy in the next stage or if
	 * there is none
	 */
	private boolean parachute;
	/**
	 * The altitude the parachute can deploy
	 */
	private float parachuteAlt;
	/**
	 * the min pressure needed for the parachute to deploy
	 */
	private float parachutePres;

	/**
	 * true if this stage had srbs
	 */
	private boolean hadSRBs;

	public STAGING(SpaceCenter spaceCenter, Vessel nvessel, STAGE ncurrentStage) throws AGuSDataException {
		this.sCenter = spaceCenter;
		this.vessel = nvessel;
		this.currentStage = ncurrentStage;
		update();
	}

	/**
	 * Starts a thread that will stage the vessel automatically.
	 * <p>
	 * Only one stagingThread can be active at any given time
	 * <p>
	 * Calling this method while there is an existing thread will result in
	 * nothing
	 * 
	 * @param millisec
	 *            the time interval between the checks for staging
	 */
	public synchronized void createAutomaticStaging(long millisec) {
		LOGGER.logger.info("creating staging thread. Delay = " + millisec);
		if (automaticStagingActive == false && automaticStagingAllowed) {
			this.automaticStagingActive = true;
			Runnable staging = new Runnable() {
				@Override
				public void run() {
					LOGGER.addLogger(Thread.currentThread().getId());
					LOGGER.logger.info("Starting staging thread Delay = " + millisec);
					try {
						while ((!Thread.currentThread().isInterrupted()) && automaticStagingActive && vessel.getControl().getCurrentStage()!=0) {
							while(stagingThreadRun){
							automaticStagingThread();
							Thread.sleep(millisec);
							}
							Thread.sleep(millisec);
						}
					} catch (RPCException | IOException e) {
						e.printStackTrace();
						LOGGER.logger.log(Level.SEVERE, "Error while executing automatic staging: interrupted", e);
					} catch (AGuSDataException e) {
						e.printStackTrace();
						LOGGER.logger.log(Level.SEVERE, "Error while executing automatic staging: interrupted", e);
					} catch (InterruptedException e1){
						e1.printStackTrace();
						LOGGER.logger.log(Level.WARNING, "Automatic staging: interrupted", e1);
					} finally {
						Thread.currentThread().interrupt();
					}
				}
			};
			autoStaging = new Thread(staging);
			autoStaging.start();
		}
	}

	/**
	 * Everything in this method will be called repeatedly for as long as the
	 * automatic staging thread remains active
	 * @throws AGuSDataException 
	 */
	private synchronized void automaticStagingThread() throws AGuSDataException {
		if (check()) {
			LOGGER.logger.info("staging");
			stage();
		}
	}

	/**
	 * Will terminate the active staging thread
	 */
	public void stopAutomaticStaging() {
		this.automaticStagingActive = false;
		autoStaging.interrupt();
	}
	/**
	 * 
	 * @param allow if false will block the automatic staging till it is set back to true
	 */
	public void runAutomaticStaging(boolean allow){
		LOGGER.logger.info("staging allowed = "+ allow);
		this.stagingThreadRun=allow;
	}

	/**
	 * 
	 * @return true if it is currently active
	 */
	public boolean getAutomaticStagingActive() {
		return automaticStagingActive;
	}

	/**
	 * Returns true if automatic staging is allowed
	 * 
	 * @return true if it is allowed
	 */
	public boolean getAutomaticStagingAllowed() {
		return automaticStagingAllowed;
	}

	/**
	 * set to true if you want to allow automatic staging
	 * <p>
	 * Setting it to false will disable the creation of automatic staging
	 * threads, but will not interfere already created ones.
	 * <p>
	 * To disable already created threads use stopAutomaticStaging()
	 * 
	 * @param allow
	 *            true if you want to allow staging thread
	 */
	public void setAutomaticStagingAllowed(boolean allow) {
		this.automaticStagingAllowed = allow;
	}

	/**
	 * Updates this class and the stage classes and returns the current stage
	 * object of the vessel
	 * 
	 * @return the current stage Object of the vessel
	 * @throws AGuSDataException 
	 */
	public STAGE getCurrentStage() throws AGuSDataException {
		try {
			if (vessel.getControl().getCurrentStage() - 1 != currentStage.getNextStage().getId()) {
				update();
			}
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		return currentStage;
	}

	/**
	 * Checks if the vessel is ready to be staged
	 * 
	 * @return true if the vessel is ready to be staged
	 * @throws AGuSDataException 
	 */
	public boolean check() throws AGuSDataException {
		try {
			if (vessel.getControl().getCurrentStage() - 1 != currentStage.getNextStage().getId()) {
				update();
			}
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		if (currentStage.getNextStage().getId() == -1) {
			return false;
		}
		try {
			if (sCenter.getRailsWarpFactor() != 0) {
				return false;
			}

		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		} catch (NullPointerException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "Trying to get Rails warp Factor: ", e);
		}
		this.met = getMET();
		this.flight = getFlight();
		if (checkPropellant() == false) {// if fuel would be decoupled
			return false;
		} else {// if no fuel would be decoupled
			if (checkFairings() == false) {
				return false;
			}
			if (checkLaunchEscape() == false) {
				return false;
			}
			if (checkLaunchClamp() == false) {
				return false;
			}
			if (checkParachute() == false) {
				return false;
			}
			if (checkSRBs() == false) {
				return false;
			}

		}

		return true;
	}

	/**
	 * Stages the vessel
	 * @throws AGuSDataException 
	 */
	public void stage() throws AGuSDataException {
		LOGGER.logger.info("staging current stage");
		try {
			vessel.getControl().activateNextStage();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		update();
	}

	/**
	 * updates the STAGE and STAGING objects
	 * @throws AGuSDataException 
	 */
	private void update() throws AGuSDataException {
		this.met = getMET();
		this.flight = getFlight();
		this.currentStage.update();
		this.partDeList = currentStage.getNextStage().getDecoupledParts();
		this.pds = partDeList.size();
		this.partAcList = currentStage.getNextStage().getActivatedParts();
		this.pas = partAcList.size();
		this.propellant = false;
		this.nextCheckPropellant = 0;
		this.fairings = false;
		this.launchEscape = false;
		this.launchClamp = false;
		this.parachute = false;
		this.hadSRBs = false;
		this.propellant = checkPropellant();
		this.fairings = checkFairings();
		this.launchEscape = checkLaunchEscape();
		this.launchClamp = checkLaunchClamp();
		this.parachute = checkParachute();
		this.hadSRBs = checkSRBs();
	}

	/**
	 * checks if fuel for the engines will be decoupled
	 * <p>
	 * only checks if certain conditions are met
	 * 
	 * @return true if there is no fuel that would be decoupled
	 * @throws AGuSDataException 
	 */
	private boolean checkPropellant() throws AGuSDataException {
		if (propellant == false) {
			if (nextCheckPropellant <= met) {
				this.propellant = Propellant();
				if (propellant == false) {
					this.nextCheckPropellant = currentStage.getBurnTime() + met;
					return false;
				}
			} else {
				return false;
			}
		}
		this.propellant = true;
		return true;
	}

	/**
	 * checks if fuel for the engines will be decoupled
	 * 
	 * @return true if there is no fuel that would be decoupled
	 * @throws AGuSDataException 
	 */
	private boolean Propellant() throws AGuSDataException {
		if (currentStage.getDeltaV() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if a fairing would be decoupled in the next stage and if the
	 * height of the rocket is above atmosphere
	 * 
	 * @return true if a fairing can be decoupled or if there is none
	 * @throws AGuSDataException 
	 */
	private boolean checkFairings() throws AGuSDataException {
		if (fairings == false) {
			try {
				if (flight.getMeanAltitude() < vessel.getOrbit().getBody().getAtmosphereDepth()) {
					this.fairings = Fairings();
					if (fairings == false) {
						this.fairings = false;
						return false;
					}
				}
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}

		}
		this.fairings = true;
		return true;
	}

	/**
	 * checks if there are fairings getting activated in the next stage
	 * 
	 * @return true if there are no fairings getting activated in the stage
	 * @throws AGuSDataException 
	 */
	private boolean Fairings() throws AGuSDataException {
		for (int i = 0; i < pas; i++) {
			Part f = partAcList.get(i);
			try {
				if (f.getFairing() != null) {
					if (f.getStage() == currentStage.getNextStage().getId()) {
						return false;
					}
				}
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}
		return true;

	}

	/**
	 * checks if there is an launch escape system in the next stage
	 * 
	 * @return true if a launch escape system in the next stage can be decoupled
	 *         or if there is none
	 * @throws AGuSDataException 
	 */
	private boolean checkLaunchEscape() throws AGuSDataException {
		if (launchEscape == false) {
			try {
				if (flight.getMeanAltitude() <= vessel.getOrbit().getBody().getAtmosphereDepth()) {
					this.launchEscape = LaunchEscape();
					if (launchEscape) {
						this.launchEscape = false;
						return false;
					}
				}
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}
		this.launchEscape = true;
		return true;
	}

	/**
	 * checks if there is an launch escape system in the next stage
	 * 
	 * @return true if there is a launch escape system in the next stage
	 * @throws AGuSDataException 
	 */
	private boolean LaunchEscape() throws AGuSDataException {
		List<Part> p;
		int s;
		if (pas > pds) {
			p = partDeList;
			s = pds;
		} else {
			p = partAcList;
			s = pas;
		}
		for (int i = 0; i < s; i++) {
			Part pa = p.get(i);
			try {
				if (pa.getStage() == pa.getDecoupleStage()) {
					if (pa.getEngine() != null) {
						return true;
					}
				}
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}
		return false;
	}

	/**
	 * checks if a launch clamp will be activated in the next stage
	 * 
	 * @return true if a launch clamp can be activated in the next stage or if
	 *         there is none
	 * @throws AGuSDataException 
	 */
	private boolean checkLaunchClamp() throws AGuSDataException {
		if (launchClamp == false) {
			this.launchClamp = LaunchClamp();
			if (launchClamp) {
				try {

					if (currentStage.getNextStage().getAcceleration(1) < vessel.getOrbit().getBody()
							.getSurfaceGravity()) {
						this.launchClamp = false;
						return false;
					}
				} catch (RPCException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e);
					throw new AGuSDataException(e);
				}
			}
		}
		this.launchClamp = true;
		return true;
	}

	/**
	 * 
	 * @return true if there is a launch clamp getting activated in the next
	 *         stage
	 * @throws AGuSDataException 
	 */
	private boolean LaunchClamp() throws AGuSDataException {
		List<krpc.client.services.SpaceCenter.LaunchClamp> lc = new ArrayList<krpc.client.services.SpaceCenter.LaunchClamp>();
		try {
			lc = vessel.getParts().getLaunchClamps();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		int s = lc.size();
		for (int i = 0; i < s; i++) {
			try {
				if (lc.get(i).getPart().getStage() == currentStage.getNextStage().getId()) {
					return true;
				}
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}
		return false;
	}

	/**
	 * 
	 * @return true if a parachute can be activated in the next stage, or if
	 *         there is none
	 * @throws AGuSDataException 
	 */
	private boolean checkParachute() throws AGuSDataException {
		if (parachute == false) {
			this.parachute = Parachute();
			if (parachute) {
				try {
					if (flight.getMeanAltitude() > parachuteAlt * 1.25) { //|| flight.getDynamicPressure() < parachutePres) {
						this.parachute = false;
						return false;
					}
				} catch (RPCException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e);
					throw new AGuSDataException(e);
				}
			}
		}
		this.parachute = true;
		return true;
	}

	/**
	 * 
	 * @return true if there is a parachute that will be activated in the next
	 *         stage
	 * @throws AGuSDataException 
	 */
	private boolean Parachute() throws AGuSDataException {
		List<krpc.client.services.SpaceCenter.Parachute> p = new ArrayList<krpc.client.services.SpaceCenter.Parachute>();
		try {
			p = vessel.getParts().getParachutes();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		int s = p.size();
		for (int i = 0; i < s; i++) {
			try {
				if (p.get(i).getPart().getStage() == currentStage.getNextStage().getId()) {
					krpc.client.services.SpaceCenter.Parachute pa = p.get(i);
					this.parachuteAlt = pa.getDeployAltitude();
					this.parachutePres = pa.getDeployMinPressure();
					return true;
				}
			} catch (RPCException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}
		return false;
	}

	/**
	 * true if the srbs can be decoupled, ot if there are none
	 * 
	 * @return
	 * @throws AGuSDataException 
	 */
	private boolean checkSRBs() throws AGuSDataException {
		if (hadSRBs) {
			return SRBs();
		}
		return true;
	}

	/**
	 * true if the srbs can be decoupled
	 * 
	 * @return
	 * @throws AGuSDataException 
	 */
	private boolean SRBs() throws AGuSDataException {
		if (currentStage.getDeltaVofSRBs() == 0) {
			this.hadSRBs = false;
			return true;
		}
		return false;

	}

	private Flight getFlight() throws AGuSDataException {
		try {
			return vessel.flight(vessel.getOrbit().getBody().getReferenceFrame());
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	/**
	 * 
	 * @return the current MET in seconds
	 * @throws AGuSDataException 
	 */
	private double getMET() throws AGuSDataException {
		try {
			return vessel.getMET();
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

}