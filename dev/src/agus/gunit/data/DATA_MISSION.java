package agus.gunit.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import agus.formula.FORMULA_TRANSFER;
import agus.gunit.BODIES;
import agus.gunit.ORBIT;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.CelestialBody;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;

/**
 * Will read and provide the mission.ini file
 * 
 * @author Niklas Heidenreich
 *
 */
public class DATA_MISSION {

	private SpaceCenter spaceCenter;
	private Properties properties;
	private BODIES bodies;

	public DATA_MISSION(SpaceCenter SpaceCenter, BODIES Bodies) {
		this.spaceCenter = SpaceCenter;
		this.bodies = Bodies;
		this.properties = readData("mission");
	}

	public DATA_MISSION(SpaceCenter SpaceCenter, BODIES Bodies, String missionName) {
		this.bodies = Bodies;
		this.properties = readData(missionName);
	}

	/**
	 * Will load the next mission
	 */
	public void loadNextMission() {
		this.properties = readData(getNextMissionName());
	}

	/**
	 * Checks if there is a mission following after the current one
	 * 
	 * @return true if there is a next mission
	 * @throws AGuSDataException
	 */
	public boolean nextMission() {

		if (getNextMissionName().equals("none")) {
			return false;
		}
		try {
			new FileInputStream("./settings/missions/" + getNextMissionName() + ".ini").close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "Could not find ./settings/missions/" + getNextMissionName() + ".ini ", e);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "Error finding ./settings/missions/" + getNextMissionName() + ".ini ", e);
		}
		return true;
	}

	/**
	 * 
	 * @return the name of the next mission
	 */
	public String getNextMissionName() {
		String name = properties.getProperty("nextMission");
		if (name == null) {
			name = "none";
		}
		return name;
	}

	/**
	 * 
	 * @param missionName
	 *            the name of the file containing the mission
	 * @return the properties object
	 */
	private Properties readData(String missionName) {
		try {
			// create and load default properties
			Properties defaultProps = new Properties();
			try {
				FileInputStream in = new FileInputStream("./settings/default/mission.ini");

				defaultProps.load(in);
				in.close();
			} catch (FileNotFoundException e) {
				LOGGER.logger.log(Level.SEVERE, "Could not load default ./settings/default/mission.ini ", e);
			}

			// create application properties with default
			Properties applicationProps = new Properties(defaultProps);

			// now load properties 
			// from last invocation
			try {
				FileInputStream in = new FileInputStream("./settings/missions/" + missionName + ".ini");
				applicationProps.load(in);
				in.close();
			} catch (FileNotFoundException e) {
				LOGGER.logger.log(Level.SEVERE, "Could not load ./settings/missions/" + missionName + ".ini ", e);
			}
			return applicationProps;
		} catch (IOException e) {
			LOGGER.logger.log(Level.SEVERE, "Error loading data", e);
		}
		return null;
	}

	/**
	 * 
	 * 
	 */

	/**
	 * The state the vessel should have after the flight
	 * 
	 * @return
	 */
	public MISSION_ENDING_STATES getMissionEndingState() {
		if (properties.getProperty("endMission").equals("orbiting")) {
			return MISSION_ENDING_STATES.ORBITING;
		} else if (properties.getProperty("endMission").equals("landed")) {
			return MISSION_ENDING_STATES.LANDED;
		} else if (properties.getProperty("endMission").equals("docked")) {
			return MISSION_ENDING_STATES.DOCKED;
		} else {
			return MISSION_ENDING_STATES.ORBITING;
		}
	}

	/**
	 * The final orbit of the mission
	 * <p>
	 * 
	 * @return orbit
	 */
	public ORBIT getFinalOrbit() {
		try {
			ORBIT orbit = new ORBIT();
			orbit.setApoapsis(Double.parseDouble(properties.getProperty("apoapsis")));
			orbit.setPeriapsis(Double.parseDouble(properties.getProperty("periapsis")));
			// from degree to radian
			double inc = Double.parseDouble(properties.getProperty("inclination"));
			inc = inc / 360 * 2 * Math.PI;
			System.out.println("Desired Inclination: "+inc);
			orbit.setInclination(inc);
			orbit.setMeanAnomalyAtStart(Double.parseDouble(properties.getProperty("meanAnomaly")));
			orbit.setUTatStart(Double.parseDouble(properties.getProperty("ut")));
			Map<String, CelestialBody> bs = spaceCenter.getBodies();
			Iterator<Entry<String, CelestialBody>> bsIt = bs.entrySet().iterator();
			String name = properties.getProperty("body");
			while (bsIt.hasNext()) {
				CelestialBody body = bsIt.next().getValue();
				if (body.getName().equals(name)) {
					orbit.setBody(bodies.getBody(body.getName()));
					break;
				}
			}
			if (orbit.getBody() == null) {
				LOGGER.logger.warning("given orbits body could not be found: " + properties.getProperty("body"));
				try {
					orbit.setBody(bodies.getBody(spaceCenter.getBodies().values().iterator().next().getName()));
				} catch (RPCException | IOException e) {
					e.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e);
				}
			}

			if (orbit.getApoapsis() <= 0) {
				orbit.setApoapsis(
						FORMULA_TRANSFER.ParkingOrbit(orbit.getBody().getCelestialBody().getEquatorialRadius(),
								orbit.getBody().getCelestialBody().getAtmosphereDepth(),
								orbit.getBody().getCelestialBody().getSurfaceGravity()));
			}
			if (orbit.getPeriapsis() <= 0) {
				orbit.setApoapsis(
						FORMULA_TRANSFER.ParkingOrbit(orbit.getBody().getCelestialBody().getEquatorialRadius(),
								orbit.getBody().getCelestialBody().getAtmosphereDepth(),
								orbit.getBody().getCelestialBody().getSurfaceGravity()));
			}
			if (orbit.getInclination() > 2 * Math.PI) {
				orbit.setInclination(orbit.getInclination() - Math.PI);
			} else if (orbit.getInclination() < 0) {
				orbit.setInclination(orbit.getInclination() + Math.PI);
			}
			System.out.println("AP:" + orbit.getApoapsis() + " PE " + orbit.getPeriapsis());
			return orbit;
		} catch (IOException | RPCException e) {
			LOGGER.logger.log(Level.SEVERE, "", e);
		}
		return null;
	}

	/**
	 * returns null if docking is not set as the end of the orbit
	 * 
	 * @return The target vessel for docking
	 */
	public Vessel getTargetVessel() {
		try {
			if (getMissionEndingState().equals(MISSION_ENDING_STATES.DOCKED)) {
				Iterator<Vessel> vesIt;
				vesIt = spaceCenter.getVessels().iterator();
				while (vesIt.hasNext()) {
					Vessel vessel = vesIt.next();
					if (vessel.getName().equals(properties.getProperty("targetSpacecraft"))) {
						return vessel;
					}
				}
			}
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
		}
		return null;
	}

	public float getMaxError() {
		float error = Float.parseFloat(properties.getProperty("maxError"));
		return error;
	}

	/**
	 * The ut at which to start the mission
	 * 
	 * @return the start ut
	 */
	public double getStartUt() {
		//double ut = Double.parseDouble(properties.getProperty("startUt"));
		double ut = Double.parseDouble("-1");
		try {
			if (ut > spaceCenter.getUT()) {
				return spaceCenter.getUT();
			}
		} catch (RPCException | IOException | NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "cant get ut", e);
		}
		return ut;
	}

}
