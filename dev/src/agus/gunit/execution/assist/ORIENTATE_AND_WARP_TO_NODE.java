package agus.gunit.execution.assist;

import java.io.IOException;
import java.util.logging.Level;
import org.javatuples.Triplet;
import agus.exception.AGuSDataException;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.AutoPilot;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.ReferenceFrame;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;
/**
 * Orientates the vessel into the given direction and warps to the given node
 * @author Niklas Heidenreich
 *
 */
public class ORIENTATE_AND_WARP_TO_NODE implements ASSIST {
	private SpaceCenter spaceCenter;
	private Vessel krpcVessel;
	private int maxPhysicsWarp;

	public ORIENTATE_AND_WARP_TO_NODE(SpaceCenter SpaceCenter, Vessel Vessel, int MaxPhysicsWarp) {
		this.spaceCenter = SpaceCenter;
		this.krpcVessel = Vessel;
		this.maxPhysicsWarp = MaxPhysicsWarp;
	}

	@Override
	public void run() throws AGuSDataException {
		try {
			Node node = krpcVessel.getControl().getNodes().get(0);
			AutoPilot autoPilot = krpcVessel.getAutoPilot();
			autoPilot.engage();
			ReferenceFrame refFrame = krpcVessel.getOrbit().getBody().getReferenceFrame();
			Triplet<Double, Double, Double> direction = node.direction(refFrame);
			autoPilot.setReferenceFrame(refFrame);
			autoPilot.setTargetDirection(direction);
			spaceCenter.setPhysicsWarpFactor(maxPhysicsWarp);
			autoPilot.wait_();
			spaceCenter.setPhysicsWarpFactor(1);
			spaceCenter.warpTo(node.getUT() - 120, calcMaxRailsWarpFactor(node.getUT()), maxPhysicsWarp);
			autoPilot.disengage();
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}

	}

	/**
	 * Calculates the max recommended warp rate in rails warp given the ut at
	 * which to warp to
	 * 
	 * @param newUT
	 *            the ut you want to warp to
	 * @return the warp factor from (1 -100000)
	 */
	private int calcMaxRailsWarpFactor(double newUT) {
		return 100000;
	}
}
