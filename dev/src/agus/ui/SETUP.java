package agus.ui;

import java.io.IOException;
import java.net.ConnectException;
import java.util.logging.Level;
import agus.exception.AGuSDataException;
import agus.gunit.GUIDANCEUNIT;
import agus.gunit.data.DATA_SETTINGS;
import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;
import nhlog.LOGGER;

/**
 * This class provides all method necessary to start the guidance unit from an
 * ui
 * 
 * @author Niklas Heidenreich
 *
 */
public class SETUP {
	private DATA_SETTINGS settings;
	private Connection connection;
	private SpaceCenter spaceCenter;
	private Vessel vessel;
	private GUIDANCEUNIT guidanceunit;
	private Thread guidanceThread;
	private boolean complete;

	/**
	 * Will setup the guidanceunit for use in a new thread
	 */
	public SETUP(DATA_SETTINGS data_SETTINGS) {
		this.settings = data_SETTINGS;
	}

	/**
	 * Connects to the server
	 * 
	 * @return true if connection could be established
	 * @throws AGuSDataException
	 */
	public boolean connect() throws AGuSDataException {
		try {
			try {
				this.connection = Connection.newInstance(settings.getName(), settings.getHostname(),
						settings.getRPC_Port(), settings.getStream_Port());
			} catch (ConnectException e) {
				e.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "Could not connect to server", e);
				return false;
			}
			this.spaceCenter = SpaceCenter.newInstance(connection);
			this.vessel = spaceCenter.getActiveVessel();
			this.guidanceunit = new GUIDANCEUNIT(settings, connection, spaceCenter, vessel);
			createGuidanceThread();
		} catch (IOException | RPCException e) {
			e.printStackTrace();
			LOGGER.logger.log(Level.SEVERE, "Unknown Error while settings up server connection", e);
			return false;
		}
		return true;
	}

	private void createGuidanceThread() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					complete = false;
					LOGGER.addLogger(Thread.currentThread().getId());
					LOGGER.logger.info("Starting guidanceunit thread");
					guidanceunit.run();
					LOGGER.logger.info("Ending guidanceunit thread");
					complete = true;
				} catch (InterruptedException ei) {
					ei.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "Guidanceunit interrupted", ei);
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					e.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e);
				} finally{
					try {
						vessel.getAutoPilot().engage();
						vessel.getAutoPilot().disengage();
					} catch (RPCException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
					Thread.currentThread().interrupt();
				}
			}
		};
		this.guidanceThread = new Thread(runnable);
	}

	/**
	 * Initializes the logger
	 * 
	 * @param settings
	 *            the settings that the logger should use
	 */
	public void initializeLogger(DATA_SETTINGS settings) {
		LOGGER.setHtmlDirectory(settings.getHtmlDirectory());
		LOGGER.setTxtDirectory(settings.getTxtDirectory());
		LOGGER.allowHtmlLogs(settings.getEnableHtmlLogs());
		LOGGER.allowTxtLogs(settings.getEnableTxtLogs());
		LOGGER.enableDifferentLogs(settings.getEnableDifferrentLogs());
		LOGGER.setPrintFullLogs(settings.getEnableHtmlFullLogs());
		LOGGER.addLogger(Thread.currentThread().getId());
		LOGGER.logger.info("Logger started");
	}

	/**
	 * Will start the guidance unit execution
	 * 
	 * @throws InterruptedException
	 */
	public void start() throws InterruptedException {
		if (guidanceThread == null) {
			createGuidanceThread();
		} else if (guidanceThread.isInterrupted()) {
			createGuidanceThread();
		} else if (guidanceThread.isAlive()) {
			guidanceThread.interrupt();
			guidanceThread.join();
			createGuidanceThread();
		}
		guidanceThread.start();

	}

	/**
	 * Will terminate the guidance unit execution
	 * 
	 * @throws InterruptedException
	 */
	public void terminate() throws InterruptedException {
		if (guidanceThread != null) {
			guidanceunit.getVessel().getStaging().stopAutomaticStaging();
			guidanceThread.interrupt();
			guidanceThread.join();
		}
		guidanceThread = null;
	}

	public boolean complete() {
		return complete;
	}

	/**
	 * 
	 * @return the thread that is running the guidanceunit
	 */
	public Thread getGuidanceUnitThread() {
		return guidanceThread;
	}

	/**
	 * the guidanceunit object
	 * 
	 * @return null if no connection to krpc exists
	 */
	public GUIDANCEUNIT getGuidanceunit() {
		return guidanceunit;
	}

	/**
	 * the settings for the program
	 * 
	 * @return null if no connection to krpc exists
	 */
	public DATA_SETTINGS getSettings() {
		return settings;
	}

	/**
	 * 
	 * @return the krpc spaceCenter object
	 */
	public SpaceCenter getSpaceCenter() {
		return spaceCenter;
	}

}
