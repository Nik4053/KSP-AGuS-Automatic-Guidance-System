package agus.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import agus.exception.AGuSDataException;
import agus.gunit.data.DATA_SETTINGS;
import nhlog.LOGGER;

/**
 * A simple gui containing only one button.
 * <p>
 * 
 * @author Niklas Heidenreich
 *
 */
public class GUI_BASIC extends JFrame {

	private DATA_SETTINGS settings;
	private SETUP setup;
	private JButton button;
	private String name;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// LOGGER starts in constructor
					GUI_BASIC frame = new GUI_BASIC("AGuS");
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	public GUI_BASIC(String name) {
		super(name);
		this.name = name;
		settings = new DATA_SETTINGS();
		setup = new SETUP(settings);
		setup.initializeLogger(settings);
		connectScreen();

	}

	public void connectScreen() {
		JLabel textLabel = new JLabel("Connection target: " + settings.getHostname());
		button = new JButton("Start");
		Container c = getContentPane();
		c.add(textLabel, BorderLayout.CENTER);
		c.add(button, BorderLayout.SOUTH);
		button.addActionListener(e -> {
			textLabel.setText("connecting to " + settings.getHostname() + "...");
			boolean connected;
			try {
				connected = setup.connect();
			} catch (AGuSDataException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "", e1);
				connected = false;
			}
			if (!(setup.getSettings() == null)) {

			}
			if (!connected) {
				textLabel.setText("Connection to" + settings.getHostname() + " failed!!!");
				button.setText("Retry");				 
			} else {
				textLabel.setText("connected");
				setVisible(false);
				startSecondScreen();
			}
		});
		finishSetup();
	}

	public void startSecondScreen() {
		GUI_BASIC2 frame = new GUI_BASIC2(name, settings, setup);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void finishSetup() {
		setSize(350, 100);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

}
