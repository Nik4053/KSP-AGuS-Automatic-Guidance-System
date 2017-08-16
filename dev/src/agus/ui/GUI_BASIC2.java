package agus.ui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import agus.exception.AGuSDataException;
import agus.gunit.data.DATA_SETTINGS;
import agus.gunit.execution.assist.ASSIST;
import agus.gunit.execution.assist.EXECUTE_NODE_BURN;
import agus.gunit.execution.assist.ORIENTATE_AND_WARP_TO_NODE;
import krpc.client.RPCException;
import nhlog.LOGGER;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.awt.event.ActionEvent;

/**
 * 
 * @author Niklas Heidenreich
 *
 */
public class GUI_BASIC2 extends JFrame {

	private JPanel contentPane;
	private JLabel textLabel;
	private JButton button;
	private DATA_SETTINGS settings;
	private SETUP setup;
	private JButton autoButton;
	private Thread nodeTh;
	private JButton consolebutton;
	private GUI_CONSOLE console;

	/**
	 * Create the frame.
	 */
	public GUI_BASIC2(String name, DATA_SETTINGS data_SETTINGS, SETUP setup) {
		setTitle(name);
		this.settings = data_SETTINGS;
		this.setup = setup;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 297, 287);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		autoButton = new JButton("Start Autopilot");
		autoButton.setToolTipText("Starts the autopilot");
		autoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		autoButton.setBounds(46, 82, 186, 45);
		contentPane.add(autoButton);
		textLabel = new JLabel("Connected to " + settings.getHostname());
		textLabel.setBounds(28, 31, 221, 20);
		contentPane.add(textLabel);
		//textLabel.setColumns(10);
		button = new JButton("Execute Node");
		button.setToolTipText("Executes the next node for the current vessel");
		button.setBounds(46, 148, 186, 45);
		contentPane.add(button);
		consolebutton = new JButton("console");
		consolebutton.setBounds(98, 214, 89, 23);
		consolebutton.setToolTipText("Opens/Closes the console");
		contentPane.add(consolebutton);
		console = new GUI_CONSOLE("Missionlog");
		console.setLocationRelativeTo(this);
		console.setLocation(console.getLocation().x + 400, console.getLocation().y - 20);
		setup();

	}

	public void setup() {
		autoButton.addActionListener(e -> {
			if (autoButton.getText().equals("Start Autopilot")) {
				autoButton.setText("Terminate Autopilot");
				textLabel.setText("Autopilot running...");
				button.setEnabled(false);
				try {
					setup.start();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e);
				}
				Runnable guidRun = new Runnable() {
					@Override
					public void run() {
						while (!Thread.interrupted()) {
							if (setup.complete()) {
								autoButton.setText("Start Autopilot");
								textLabel.setText("Autopilot completed mission");
								button.setEnabled(true);
								Thread.currentThread().interrupt();
							}
							if (setup.getGuidanceUnitThread() == null) {
								Thread.currentThread().interrupt();
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
								LOGGER.logger.log(Level.SEVERE, "", e);
								Thread.currentThread().interrupt();
							}
						}
					}
				};
				Thread guidThread = new Thread(guidRun);
				guidThread.start();
			} else {
				autoButton.setText("Start Autopilot");
				textLabel.setText("Autopilot terminated by user");
				button.setEnabled(true);
				// setup.getGuidanceUnitThread().interrupt();
				try {
					setup.terminate();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					LOGGER.logger.log(Level.SEVERE, "", e1);
				}
			}
		});
		button.addActionListener(e -> {
			try {
				if (setup.getGuidanceunit().getVessel().getVessel().getControl().getNodes().isEmpty()) {
					textLabel.setText(
							"No nodes found for vessel: " + setup.getGuidanceunit().getVessel().getVessel().getName());
					return;
				}
			} catch (RPCException | IOException e1) {
				e1.printStackTrace();
				LOGGER.logger.log(Level.SEVERE, "Error checking for Nodes", e1);
				textLabel.setText("Error checking for Nodes");
				return;
			}
			if (button.getText().equals("Execute Node")) {
				Runnable nodeRun = new Runnable() {
					public void run() {
						LOGGER.addLogger(Thread.currentThread().getId());
						LOGGER.logger.info("Node execution started");
						ASSIST nodeWarp = new ORIENTATE_AND_WARP_TO_NODE(setup.getSpaceCenter(),
								setup.getGuidanceunit().getVessel().getVessel(),
								setup.getSettings().getMaxPhysicsWarp());

						ASSIST nodeExe = new EXECUTE_NODE_BURN(setup.getSpaceCenter(),
								setup.getGuidanceunit().getVessel());
						try {
							nodeWarp.run();
							nodeExe.run();
							// setup.getGuidanceunit().getVessel().getVessel().getControl();
						} catch (InterruptedException | AGuSDataException e) {
							e.printStackTrace();
							LOGGER.logger.log(Level.SEVERE, "Node execution interrupted", e);
							Thread.currentThread().interrupt();
						} finally {
							textLabel.setText("Node execution finished");
							button.setText("Execute Node");
							autoButton.setEnabled(true);
							try {
								setup.getGuidanceunit().getVessel().getVessel().getAutoPilot().engage();
								setup.getGuidanceunit().getVessel().getVessel().getAutoPilot().disengage();
							} catch (RPCException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}	
							Thread.currentThread().interrupt();
						}
					}
				};
				this.nodeTh = new Thread(nodeRun);
				nodeTh.start();
				button.setText("Terminate node execution");
				autoButton.setEnabled(false);
				textLabel.setText("Node execution running...");
			} else {
				nodeTh.interrupt();
				button.setText("Execute Node");
				autoButton.setEnabled(true);
				textLabel.setText("Node execution terminated by user");
			}
		});
		consolebutton.addActionListener(e -> {
			if (console.isVisible()) {
				console.setVisible(false);
			} else {
				console.setVisible(true);
			}
		});
	}
}
