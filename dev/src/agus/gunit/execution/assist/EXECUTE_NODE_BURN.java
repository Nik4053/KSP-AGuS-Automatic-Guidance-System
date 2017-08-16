package agus.gunit.execution.assist;

import java.io.IOException;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import agus.formula.FORMULA_VESSEL;
import agus.gunit.vessel.VESSEL;
import agus.gunit.vessel.stage.STAGE;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.AutoPilot;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;

/**
 * Will execute the burn of the next Node
 * 
 * @author Niklas Heidenreich
 *
 */
public class EXECUTE_NODE_BURN implements ASSIST {

	private SpaceCenter spaceCenter;
	private VESSEL vessel;

	/**
	 * Will execute the burn of the current Node
	 * 
	 * @param SpaceCenter
	 *            the krpc spaceCenter
	 * @param Vessel
	 *            the vessel
	 */
	public EXECUTE_NODE_BURN(SpaceCenter SpaceCenter, VESSEL Vessel) {
		this.spaceCenter = SpaceCenter;
		this.vessel = Vessel;
	}

	@Override
	public void run() throws InterruptedException, AGuSDataException {
		try {
			if (vessel.getVessel().getControl().getNodes().isEmpty() == false) {
				executeNodeBurn();
			}
		} catch (RPCException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
			throw new AGuSDataException(e);
		}
	}

	/**
	 * Executes the burn of the current node
	 * 
	 * @throws InterruptedException
	 * @throws AGuSDataException
	 */
	private void executeNodeBurn() throws InterruptedException, AGuSDataException {
		try {
			spaceCenter.setRailsWarpFactor(0);
			spaceCenter.setPhysicsWarpFactor(1);
			Vessel krpcVessel = vessel.getVessel();
			Node node = krpcVessel.getControl().getNodes().get(0);
			STAGE stage = vessel.getCurrentStage();
			double burnTime = vessel.getBurnTime(node.getDeltaV());
			AutoPilot autoPilot = krpcVessel.getAutoPilot();
			autoPilot.engage();
			autoPilot.setReferenceFrame(node.getReferenceFrame());
			autoPilot.setTargetDirection(node.remainingBurnVector(node.getReferenceFrame()));
			autoPilot.wait_();
			//Waiting...
			spaceCenter.setRailsWarpFactor(1);
			while (node.getTimeTo() - (burnTime / 2.0) > 0) {
				autoPilot.setTargetDirection(node.remainingBurnVector(node.getReferenceFrame()));
				Thread.sleep(500);
			}
			spaceCenter.setRailsWarpFactor(0);
			spaceCenter.setPhysicsWarpFactor(0);
			//executing
			System.out.println("Executing burn");
			while (node.getRemainingDeltaV() > 0.1f && node.getTimeTo() > -180) {
				autoPilot.setTargetDirection(node.remainingBurnVector(node.getReferenceFrame()));
				double acc = node.getRemainingDeltaV() / 10;
				krpcVessel.getControl().setThrottle(stage.getAccelerationPercent((float) acc * 2));
				Thread.sleep((long) node.getRemainingDeltaV() * 5 + 100);
			}
			krpcVessel.getControl().setThrottle(0);
			Thread.sleep(50);
		} catch (RPCException | IOException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "", e);
		}
	}

}
