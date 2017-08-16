package agus.gunit.execution;

import java.io.IOException;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import agus.gunit.BODIES;
import agus.gunit.ORBIT;
import agus.gunit.data.DATA_MISSION;
import agus.gunit.data.MISSION_ENDING_STATES;
import agus.gunit.execution.assist.ASSIST;
import agus.gunit.execution.assist.EXECUTE_NODE_BURN;
import agus.gunit.execution.assist.ORIENTATE_AND_WARP_TO_MANEUVER;
import agus.gunit.execution.assist.SURFACE_TO_SPACE_ATMO;
import agus.gunit.sequence.MANEUVER;
import agus.gunit.sequence.SEQUENCE;
import agus.gunit.sequence.SEQUENCE_NORMAL;
import agus.gunit.sequence.VECTOR_BURN;
import agus.gunit.vessel.VESSEL;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Orbit;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.client.services.SpaceCenter.VesselSituation;
import nhlog.LOGGER;

/**
 * This is the autopilot for an space ship
 * 
 * @author Niklas Heidenreich
 *
 */
public class EXECUTION_SPACESHIP implements EXECUTION {
	private SpaceCenter spaceCenter;
	private VESSEL vessel;
	private float maxErrorInOrbit;
	private ORBIT finalOrbit;
	private int maxPhysicsWarp;
	private Connection connection;
	private BODIES bodies;
	private DATA_MISSION mission;
	private SEQUENCE sequence;

	/**
	 * 
	 * @param SpaceCenter
	 *            the space center object
	 * @param Vessel
	 *            the vessel
	 * @param FinalOrbit
	 *            the final orbit that should be reached
	 * @param MaxErrorInOrbit
	 *            the max error in orbit. 0.1 for an maximum 10% change
	 * @param MaxPhysicsWarp
	 *            The max physics warp from 1 to 4
	 */
	public EXECUTION_SPACESHIP(Connection Connection, SpaceCenter SpaceCenter, VESSEL Vessel, BODIES Bodies,
			DATA_MISSION data_MISSION, int MaxPhysicsWarp) {
		this.mission = data_MISSION;
		this.connection = Connection;
		this.spaceCenter = SpaceCenter;
		this.vessel = Vessel;
		this.bodies = Bodies;
		this.maxErrorInOrbit = mission.getMaxError();
		this.finalOrbit = mission.getFinalOrbit();
		this.maxPhysicsWarp = MaxPhysicsWarp;
	}

	/**
	 * Will complete the Parking Orbit after the SURFACE_TO_SPACE class
	 * 
	 * @throws RPCException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws AGuSDataException
	 */
	private void completeParkingOrbit() throws InterruptedException, AGuSDataException {
		try {
			Vessel krpcVessel = vessel.getVessel();
			ORBIT orbit = new ORBIT(krpcVessel.getOrbit(), bodies.getBody(krpcVessel.getOrbit().getBody().getName()),
					spaceCenter.getUT());
			ORBIT orbit2 = new ORBIT(orbit);
			orbit2.setPeriapsis(orbit.getBody().getCelestialBody().getSpaceHighAltitudeThreshold()
					+ orbit.getBody().getCelestialBody().getEquatorialRadius());
			SEQUENCE seq = new SEQUENCE_NORMAL(vessel, bodies, orbit, orbit2);
			MANEUVER man = seq.getTransferList().getFirst().getManeuverList().getFirst();
			executeManeuver(man);
		} catch (IOException | RPCException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	@Override
	public void run() throws InterruptedException, AGuSDataException {
		LOGGER.logger.info("Starting execution");
		checks();
		if (mission.getMissionEndingState().equals(MISSION_ENDING_STATES.LANDED)) {
			LOGGER.logger.info("Landing is currently not implemented");
		} else if (mission.getMissionEndingState().equals(MISSION_ENDING_STATES.DOCKED)) {
			LOGGER.logger.info("Docking is currently not implemented");
		} else if (mission.getMissionEndingState().equals(MISSION_ENDING_STATES.ORBITING)) {
			try {
				VesselSituation situation = vessel.getVessel().getSituation();
				vessel.getStaging().runAutomaticStaging(true);
				if (situation.equals(VesselSituation.PRE_LAUNCH) || situation.equals(VesselSituation.LANDED)
						|| (situation.equals(VesselSituation.FLYING) && vessel.getVessel()
								.velocity(vessel.getVessel().getOrbit().getBody().getReferenceFrame())
								.getValue0() >= -1)) {
					Orbit o = vessel.getVessel().getOrbit();
					if (o.getPeriapsisAltitude() < o.getBody().getSpaceHighAltitudeThreshold()
							+ o.getBody().getEquatorialRadius()) {
						if (o.getBody().getHasAtmosphere()) {
							if (o.getBody().getName().equals(finalOrbit.getBody().getCelestialBody().getName())) {
								ASSIST assist = new SURFACE_TO_SPACE_ATMO(connection, spaceCenter, vessel.getVessel(),
										bodies, (float) finalOrbit.getInclination());
								assist.run();
								completeParkingOrbit();
							} else {
								ASSIST assist = new SURFACE_TO_SPACE_ATMO(connection, spaceCenter, vessel.getVessel(),
										bodies, 0);
								assist.run();
								completeParkingOrbit();
							}
						} else {
							//TODO change to SURFACE_TO_SPACE
							ASSIST assist = new SURFACE_TO_SPACE_ATMO(connection, spaceCenter, vessel.getVessel(),
									bodies, 0);
							assist.run();
							completeParkingOrbit();
						}
					}
				}
				//ORBIT currentOrbit = new ORBIT(o, spaceCenter.getUT());
				while (finalOrbitIsReached() == false) {
					checks();
					SEQUENCE sequence = updateSequence();
					MANEUVER maneuver = sequence.getTransferList().getFirst().getManeuverList().getFirst();
					executeManeuver(maneuver);
					//o = vessel.getVessel().getOrbit();
					//currentOrbit = new ORBIT(o, spaceCenter.getUT());
				}
			} catch (RPCException | IOException e) {
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e);
				throw new AGuSDataException(e);
			}
		}

		checks();

	}

	/**
	 * Clears all nodes and disengages the autopilot
	 * 
	 * @throws AGuSDataException
	 * 
	 */
	private void checks() throws AGuSDataException {
		Vessel krpcVessel = vessel.getVessel();
		try {
			krpcVessel.getControl().getNodes().clear();
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		try {
			krpcVessel.getAutoPilot().disengage();
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		try {
			spaceCenter.setRailsWarpFactor(0);
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
		try {
			spaceCenter.setPhysicsWarpFactor(0);
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	/**
	 * Updates the sequence and returns it
	 * 
	 * @return
	 * @throws AGuSDataException
	 */
	private SEQUENCE updateSequence() throws AGuSDataException {
		try {
			Orbit o = vessel.getVessel().getOrbit();
			ORBIT orbit = new ORBIT(o, bodies.getBody(o.getBody().getName()), spaceCenter.getUT());
			ORBIT o1 = finalOrbit;
			ORBIT o2 = orbit;
			float error = maxErrorInOrbit;
			if (o1.getBody().getName().equals(o2.getBody().getName())) {
				//if both orbits are around the same object
				if (Math.abs(o1.getApoapsis() - o2.getApoapsis()) <= o2.getApoapsis() * error) {
					o2.setApoapsis(o1.getApoapsis());
				}
				if (Math.abs(o1.getPeriapsis() - o2.getPeriapsis()) <= o2.getPeriapsis() * error) {
					o2.setPeriapsis(o1.getPeriapsis());
				}
				if (Math.abs(o1.getInclination() - o2.getInclination()) <= o2.getInclination() * error) {
					o2.setInclination(o1.getInclination());
				}
			}
			return new SEQUENCE_NORMAL(vessel, bodies, orbit, finalOrbit);
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	/**
	 * TODO implement inclination change Returns true if the final orbit has
	 * been reached
	 * 
	 * @return true if the final orbit has been reached
	 * @throws AGuSDataException
	 */
	private boolean finalOrbitIsReached() throws AGuSDataException {
		ORBIT o1 = finalOrbit;
		ORBIT o2 = null;
		try {
			o2 = new ORBIT(vessel.getVessel().getOrbit(),
					bodies.getBody(vessel.getVessel().getOrbit().getBody().getName()), o1.getUTatStart());

			float error = maxErrorInOrbit;
			if (Math.abs(o1.getApoapsis() - o2.getApoapsis()) <= o2.getApoapsis() * error
					&& Math.abs(o1.getPeriapsis() - o2.getPeriapsis()) <= o2.getPeriapsis() * error
					//&& Math.abs(o1.getInclination() - o2.getInclination()) <= o2.getInclination() * error
					//&& Math.abs(o1.getLongitudeOfAscendingNode() - o2.getLongitudeOfAscendingNode()) <= error
					&& o1.getBody().getCelestialBody().getName().equals(o2.getBody().getCelestialBody().getName())) {
				return true;
			}
			return false;
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	/**
	 * Executes the given Maneuver
	 * 
	 * @param maneuver
	 *            the maneuver to execute
	 * @throws InterruptedException
	 * @throws AGuSDataException
	 */
	private void executeManeuver(MANEUVER maneuver) throws InterruptedException, AGuSDataException {
		try {
			Vessel krpcVessel = vessel.getVessel();
			krpcVessel.getControl().removeNodes();
			VECTOR_BURN vBurn = maneuver.getBurn().getVectorBurn();
			krpcVessel.getControl().addNode(maneuver.getUT(), (float) vBurn.getPrograde(), (float) vBurn.getNormal(),
					(float) vBurn.getRadial());
			new ORIENTATE_AND_WARP_TO_MANEUVER(spaceCenter, krpcVessel, maneuver, maxPhysicsWarp).run();
			new EXECUTE_NODE_BURN(spaceCenter, vessel).run();
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	@Override
	public SEQUENCE getSequence() {
		return sequence;
	}

}