package agus.gunit.execution.assist;

import java.io.IOException;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import agus.formula.FORMULA_TRANSFER;
import agus.gunit.BODIES;
import agus.gunit.ORBIT;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.CelestialBody;
import krpc.client.services.SpaceCenter.Control;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;

/**
 * Will guide a rocket through the atmosphere into an suborbital trajectory with
 * the apoapsis at the height of the parking orbit. Can also be used for non
 * atmosp
 * <p>
 * WARNING: This is only a simple test method. Do not expect an perfect ascend
 * 
 * @author Niklas Heidenreich
 *
 */
public class SURFACE_TO_SPACE_ATMO implements ASSIST {

	private Connection connection;
	private SpaceCenter spaceCenter;
	private Vessel krpcVessel;
	private BODIES bodies;
	/**
	 * The inclination the
	 */
	private float inclination;
	/**
	 * The height of the parking Orbit
	 */
	private float parkingOrbit;

	/**
	 * 
	 * @param Connection
	 *            connection
	 * @param SpaceCenter
	 *            spaceCenter
	 * @param Vessel
	 *            the vessel
	 * @param Inclination
	 *            the desired inclination of the orbit after launch in rad
	 * @throws AGuSDataException
	 */
	public SURFACE_TO_SPACE_ATMO(Connection Connection, SpaceCenter SpaceCenter, Vessel Vessel, BODIES Bodies,
			float Inclination) throws AGuSDataException {
		this.connection = Connection;
		this.spaceCenter = SpaceCenter;
		this.krpcVessel = Vessel;
		this.inclination = Inclination;
		this.bodies = Bodies;
		CelestialBody body;
		try {
			body = Vessel.getOrbit().getBody();
			this.parkingOrbit = (float) FORMULA_TRANSFER.ParkingOrbit(body.getEquatorialRadius(),
					body.getAtmosphereDepth(), body.getSurfaceGravity());
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	/**
	 * 
	 * @param Connection
	 *            connection
	 * @param SpaceCenter
	 *            spaceCenter
	 * @param Vessel
	 *            the vessel
	 * @param Inclination
	 *            the desired inclination of the orbit after launch in rad
	 * @param height
	 *            the desired height to launch into
	 */
	public SURFACE_TO_SPACE_ATMO(Connection Connection, SpaceCenter SpaceCenter, Vessel Vessel, BODIES Bodies,
			float Inclination, float height) {
		this.connection = Connection;
		this.spaceCenter = SpaceCenter;
		this.krpcVessel = Vessel;
		this.inclination = Inclination;
		this.parkingOrbit = height;
		this.bodies = Bodies;
	}

	@Override
	public void run() throws InterruptedException, AGuSDataException {
		SurfaceToSpace(inclination);
	}

	/**
	 * Guides to Vessel from the surface of a body to space, where space is the
	 * parking orbit for that body.
	 * <p>
	 * It will end when the apoapsis exceeds this height.
	 * <p>
	 * This orbit will be suborbital.
	 * 
	 * @param minThrottlePercent
	 *            the minimum throttle the engines should run at
	 * @param inclination
	 *            the inclination in which to start in rad
	 * @return the sum of x and y
	 * @throws InterruptedException
	 * @throws AGuSDataException
	 */
	private void SurfaceToSpace(float inclination) throws InterruptedException // check the orbit variables
																				// vessel.getOrbit()
			, AGuSDataException {
		try {
			// Vessel krpcVessel= vessel.getVessel();
			Flight flight = krpcVessel.flight(krpcVessel.getOrbit().getBody().getReferenceFrame());
			// needed Variables for Pitching the rocket
			System.out.println("Target Inclination: " + inclination+"rad");
			float incDeg = (float)(inclination * 360/(2*Math.PI));
			System.out.println("Target Inclination: " + incDeg+"deg");
			int startTurnAltitude = 30;
			float endPitch = 0;
			float startPitch = 85;
			krpcVessel.getAutoPilot().engage();
			krpcVessel.getAutoPilot().setTargetHeading(incDeg);
			krpcVessel.getAutoPilot().setTargetPitch(90);
			
			krpcVessel.getControl().setThrottle(1);
			ORBIT orbit = new ORBIT(krpcVessel.getOrbit(), bodies.getBody(krpcVessel.getOrbit().getBody().getName()),
					spaceCenter.getUT());
			CelestialBody body = orbit.getBody().getCelestialBody();
			float endTurnAltitude = (parkingOrbit - body.getEquatorialRadius()) / 3;
			System.out.println("Target apoapsis for parking Orbit: " + parkingOrbit);
			System.out.println("Target Gravity Turn end: " + endTurnAltitude);
			Control control = krpcVessel.getControl();
			control.setThrottle(1);
			int i = 0;
			while (krpcVessel.getOrbit().getApoapsis() < parkingOrbit)// &&celestialBody.getAtmosphereDepth()<flight.getMeanAltitude())//If
														// in Space a
			{
				double pitch = (((flight.getMeanAltitude()) - startTurnAltitude) / (endTurnAltitude - startTurnAltitude)
						* (endPitch - startPitch) + startPitch);
				if (pitch > endPitch) {
					krpcVessel.getAutoPilot().setTargetPitch((float) pitch);
				} else {
					krpcVessel.getAutoPilot().setTargetPitch(endPitch);
				}
				// krpcVessel.getAutoPilot().setTargetPitch(((((float)flight.getMeanAltitude())
				// -
				// startTurnAltitude)/(endTurnAltitude-startTurnAltitude)*(endPitch-startPitch)
				// + startPitch));//könnte ohne atmo auch sofort pitchen
				// HoldSpeedFlight(flight.getTerminalVelocity()+200,minThrottlePercent);
				i++;
				if (i > 50) {
					i = 0;
					if (flight.getSpeed() > flight.getTerminalVelocity() + 200.0) {
						control.setThrottle((krpcVessel.getOrbit().getBody().getSurfaceGravity() * 1.5f));
					} else if (flight.getSpeed() < flight.getTerminalVelocity()) {
						control.setThrottle(1);
					}
				}
				Thread.sleep(300);
			}
			System.out.println("Target apoapsis reached");
			krpcVessel.getControl().setThrottle(0);
			krpcVessel.getAutoPilot().disengage();

		} catch (IOException | RPCException  e) {
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

}
