package agus.ui;


import java.util.logging.Level;
import agus.gunit.GUIDANCEUNIT;
import agus.gunit.data.DATA_SETTINGS;
import krpc.client.Connection;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;
/**
 * The defaultmain that starts the program without any visual ui.
 * <p> For use of debugging, or testing
 * @author Niklas Heidenreich
 */
public class UI_NONE {
	
	/**
	 * The default way to start the program
	 * @param args args
	 */
	public static void main(String[] args) {
		DATA_SETTINGS settings = new DATA_SETTINGS();
		initializeLogger(settings);
		Connection connection;
		try {
			connection = Connection.newInstance(settings.getName(), settings.getHostname(), settings.getRPC_Port(),
					settings.getStream_Port());
			SpaceCenter spaceCenter = SpaceCenter.newInstance(connection);
			Vessel vessel = spaceCenter.getActiveVessel();
			GUIDANCEUNIT guidanceunit = new GUIDANCEUNIT(settings, connection, spaceCenter, vessel);
			guidanceunit.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "Fatal Error in main", e);
		}
		LOGGER.logger.severe("Autopilot will now shutdown");
		System.exit(0);
	}

	/**
	 * Initializes the logger
	 * 
	 * @param settings
	 *            the settings that the logger should use
	 */
	private static void initializeLogger(DATA_SETTINGS settings) {
		LOGGER.setHtmlDirectory(settings.getHtmlDirectory());
		LOGGER.setTxtDirectory(settings.getTxtDirectory());
		LOGGER.allowHtmlLogs(settings.getEnableHtmlLogs());
		LOGGER.allowTxtLogs(settings.getEnableTxtLogs());
		LOGGER.enableDifferentLogs(settings.getEnableDifferrentLogs());
		LOGGER.setPrintFullLogs(settings.getEnableHtmlFullLogs());
		LOGGER.addLogger(Thread.currentThread().getId());
	}
	

}
