package agus.gunit;

import java.io.IOException;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import agus.gunit.data.DATA_MISSION;
import agus.gunit.data.DATA_SETTINGS;
import agus.gunit.execution.EXECUTION;
import agus.gunit.execution.EXECUTION_SPACESHIP;
import agus.gunit.vessel.VESSEL;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;

/**
 * The main guidance unit class that will start the desired execution
 * 
 * @author Niklas Heidenreich
 *
 */
public class GUIDANCEUNIT {
	private Connection connection;
	private SpaceCenter sCenter;
	private VESSEL vessel;
	private DATA_SETTINGS settings;
	private DATA_MISSION mission;
	private BODIES bodies;

	public GUIDANCEUNIT(DATA_SETTINGS data_SETTINGS, Connection connection, SpaceCenter spaceCenter,
			Vessel krpcVessel) throws AGuSDataException {
		// TODO Auto-generated constructor stub
		if (connection == null || spaceCenter == null || krpcVessel == null) {
			if (connection == null) {
				LOGGER.logger.log(Level.SEVERE, "Exiting Programm", new IllegalArgumentException("connection == null"));
			}
			if (spaceCenter == null) {
				LOGGER.logger.log(Level.SEVERE, "Exiting Programm",
						new IllegalArgumentException("spaceCenter == null"));
			}
			if (krpcVessel == null) {
				LOGGER.logger.log(Level.SEVERE, "Exiting Programm", new IllegalArgumentException("vessel == null"));
			}
			throw new IllegalArgumentException();
		} else {
			this.connection = connection;
			this.sCenter = spaceCenter;
			this.vessel = new VESSEL(connection, sCenter, krpcVessel);
			this.settings = data_SETTINGS;
			this.bodies = new BODIES(spaceCenter);
			this.mission = new DATA_MISSION(spaceCenter, bodies);
			//GUIDANCEUNIT.spaceCenter=sCenter;
		}

	}

	/**
	 * Needed for getting the ut
	 */
	//private static SpaceCenter spaceCenter;
	/**
	 * Gets the current ingame UT
	 * 
	 * @return the current UT
	 * @throws RPCException
	 * @throws IOException
	 */
	//public static double getUT() throws RPCException, IOException{
	//	return spaceCenter.getUT();
	//}

	/**
	 * Starts the execution
	 * @throws InterruptedException 
	 * @throws AGuSDataException 
	 */
	public void run() throws InterruptedException, AGuSDataException {
		LOGGER.logger.info("Guidanceunit started");
		try {
			vessel.getVessel().getControl().setThrottle(0);
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		if (settings.getAllowAutomaticStaging()) {
			vessel.getStaging().setAutomaticStagingAllowed(true);
			vessel.getStaging().createAutomaticStaging(settings.getStagingDelay());
		}
		runMission();
	}

	private void runMission() throws InterruptedException, AGuSDataException {
		try {
			if (mission.getStartUt() > sCenter.getUT()) {
				sCenter.warpTo(mission.getStartUt(), 100000, settings.getMaxPhysicsWarp());
			}
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		EXECUTION execution = new EXECUTION_SPACESHIP(connection, sCenter, vessel, bodies, mission,
				settings.getMaxPhysicsWarp());
		execution.run();
		if (mission.nextMission()) {
			LOGGER.logger.info("Starting next mission with name: " + mission.getNextMissionName());
			mission.loadNextMission();
			runMission();
		}
	}

	/**
	 * The tree containing all Bodies sorted by there distance from the parent
	 * star
	 * 
	 * @return the bodies object
	 */
	public BODIES getBodies() {
		return bodies;
	}

	/**
	 * 
	 * @return The Vessel object
	 */
	public VESSEL getVessel() {
		return vessel;
	}

	/**
	 * Contains the data contained in the mission.ini file
	 * 
	 * @return the mission object
	 */
	public DATA_MISSION getMission() {
		return mission;
	}

}
