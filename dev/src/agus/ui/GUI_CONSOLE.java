package agus.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class GUI_CONSOLE extends JFrame {

	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public GUI_CONSOLE(String name) {
		super(name);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		setup();
	}
	

	public void setup(){
		JTextArea conComp = new JTextArea();  
		JScrollPane scrollPane = new JScrollPane(conComp);
		scrollPane.setBounds(21, 204, 228, 33);
		contentPane.add(scrollPane);
		MessageConsole mc = new MessageConsole(conComp);
			mc.redirectOut();
			mc.redirectErr(Color.RED, null);
			mc.setMessageLines(100);
	}

}
