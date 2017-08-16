package agus.gunit.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Will read and provide the settings.ini file
 * 
 * @author Niklas Heidenreich
 *
 */
public class DATA_SETTINGS {

	private Properties properties;

	public DATA_SETTINGS() {
		this.properties = readData();
		getError();
	}

	/**
	 * Reads the settings.ini file
	 * 
	 * @return returns a Properties object containing the data
	 */
	private Properties readData() {
		try {
			// create and load default properties
			Properties defaultProps = new Properties();
			try {
				FileInputStream in = new FileInputStream("./settings/default/settings.ini");

				defaultProps.load(in);
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				// LOGGER.logger.log(Level.SEVERE, "Could not load default
				// ./settings/default/settings.ini ", e);
			}

			// create application properties with default
			Properties applicationProps = new Properties(defaultProps);

			// now load properties
			// from last invocation
			try {
				FileInputStream in = new FileInputStream("./settings/settings.ini");
				applicationProps.load(in);
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				// LOGGER.logger.log(Level.SEVERE, "Could not load ./settings/settings.ini ",
				// e);
			}

			return applicationProps;
		} catch (IOException e) {
			e.printStackTrace();
			// LOGGER.logger.log(Level.SEVERE, "Error loading data", e);
		}
		return null;
	}

	public Properties getProperties() {
		return properties;
	}

	/**
	 * Returns the problems that occurred in the settings file.
	 * <p> Every string of the array contains one error report
	 * @return null if everything went ok
	 */
	public List<String> getError() {
		List<String> errors = new ArrayList<>();
		try{
			getAppName();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}
		try{
			getAppVersion();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}
		try{
			getName();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}try{
			getHostname();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}try{
			getRPC_Port();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}try{
			getStream_Port();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}try{
			getAllowAutomaticStaging();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}try{
			getStagingDelay();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}try{
			getMaxPhysicsWarp();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}
		try{
			getEnableLogs();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}
		try{
			getEnableTxtLogs();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}
		try{
			getEnableHtmlLogs();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}
		try{
			getTxtDirectory();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}
		try{
			getHtmlDirectory();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}
		try{
			getEnableDifferrentLogs();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}
		try{
			getEnableHtmlFullLogs();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}
		try{
			getLogLevel();
		}catch(Exception e) {
			errors.add(e.getLocalizedMessage());
			System.out.println(e.getLocalizedMessage());
		}
		
		if(errors.isEmpty()) {
			return null;
		}
		return errors;
	}

	/*
	 * Properties
	 */
	// [info]
	public String getAppName() {
		return properties.getProperty("app.name");
	}

	public String getAppVersion() {
		return properties.getProperty("app.version");
	}

	// [connection]
	public String getName() {
		return properties.getProperty("con.name");
	}

	public String getHostname() {
		return properties.getProperty("con.hostname");
	}

	public int getRPC_Port() {
		int port = Integer.parseInt(properties.getProperty("con.rpc_port"));
		return port;
	}

	public int getStream_Port() {
		int port = Integer.parseInt(properties.getProperty("con.stream_port"));
		return port;
	}

	// [settings]
	public boolean getAllowAutomaticStaging() {
		boolean staging = Boolean.parseBoolean(properties.getProperty("set.allowAutomaticStaging"));
		return staging;
	}

	public long getStagingDelay() {
		long delay = Long.parseLong(properties.getProperty("set.stagingDelay"));
		return delay;
	}

	public int getMaxPhysicsWarp() {
		int warp = Integer.parseInt(properties.getProperty("set.maxPhysicsWarp"));
		return warp;
	}

	// [logger]
	public boolean getEnableLogs() {
		boolean logs = Boolean.parseBoolean(properties.getProperty("log.enable"));
		return logs;
	}

	public boolean getEnableTxtLogs() {
		boolean logs = Boolean.parseBoolean(properties.getProperty("log.txt"));
		return logs;
	}

	public boolean getEnableHtmlLogs() {
		boolean logs = Boolean.parseBoolean(properties.getProperty("log.html"));
		return logs;
	}

	public String getTxtDirectory() {
		String dir = properties.getProperty("log.txt.dir");
		return dir;
	}

	public String getHtmlDirectory() {
		String dir = properties.getProperty("log.html.dir");
		return dir;
	}

	public boolean getEnableDifferrentLogs() {
		boolean logs = Boolean.parseBoolean(properties.getProperty("log.differentLogs"));
		return logs;
	}

	public boolean getEnableHtmlFullLogs() {
		boolean logs = Boolean.parseBoolean(properties.getProperty("log.html.fullLog"));
		return logs;
	}

	public Level getLogLevel() {
		Level level = Level.parse(properties.getProperty("log.level"));
		return level;
	}
}
