package org.com1028.cw2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ServerGUI extends JFrame implements ActionListener, WindowListener {
	
	private static final long serialVersionUID = 1L;
	// The button to Start or Stop server
	private JButton stopStart;
	// JTextAreas for the chat room and the events
	private JTextArea chat, event;
	// JTextField for the port number of the server
	private JTextField tPortNumber;
	// The server class to run
	private Server server;
	
	
	// Server constructor that receives the connection port as a parameter
	ServerGUI(int port) {
		super("Chat Server");
		server = null;
		// Create a north panel
		JPanel north = new JPanel();
		// Add a label for the port field, then add the port field
		north.add(new JLabel("Port number: "));
		tPortNumber = new JTextField("  " + port);
		north.add(tPortNumber);
		// Add the Start/Stop button, beginning on start
		stopStart = new JButton("Start");
		// Add an ActionListener to capture button clicks
		stopStart.addActionListener(this);
		north.add(stopStart);
		// move the north panel to the north
		add(north, BorderLayout.NORTH);
		
		// Create a double panel for the chat room and event log
		JPanel centre = new JPanel(new GridLayout(2,1));
		// Add the chat room, set it to read only, and write a line inside it
		chat = new JTextArea(80,80);
		chat.setEditable(false);
		appendRoom("CHAT ROOM\n");
		// Allow the area to be scroll-able
		centre.add(new JScrollPane(chat));
		// Add the event log, set it to read only, and write a line inside it
		event = new JTextArea(80,80);
		event.setEditable(false);
		appendEvent("EVENTS LOG\n");
		// Allow the area to be scroll-able
		centre.add(new JScrollPane(event));
		// add the centre panel
		add(centre);
		
		// We need to be informed when the user clicks the close button on the frame
		addWindowListener(this);
		setSize(400, 600);
		setVisible(true);
	}		

	// Add a message to the chat room window
	void appendRoom(String str) {
		chat.append(str);
		chat.setCaretPosition(chat.getText().length() - 1);
	}
	// Add a message to the event log window
	void appendEvent(String str) {
		event.append(str);
		event.setCaretPosition(chat.getText().length() - 1);
		
	}
	
	// Capture the click of the Start/Stop button
	public void actionPerformed(ActionEvent e) {
		// If running we have to stop
		if(server != null) {
			server.stop();
			server = null;
			tPortNumber.setEditable(true);
			stopStart.setText("Start");
		} else {
      	// Otherwise start the server	
		int port;
		// Ensure that the port number is valid
		try {
			port = Integer.parseInt(tPortNumber.getText().trim());
		}
		catch(Exception er) {
			appendEvent("Invalid port number");
			return;
		}
		// create a new Server
		server = new Server(port, this);
		// and start it as a thread
		new ServerRunning().start();
		stopStart.setText("Stop");
		tPortNumber.setEditable(false);
		}
	}
	
	public static void main(String[] arg) {
		// Start the server on the default port 8050 (even though it will be set to null)
		new ServerGUI(8050);
	}

	/*
	 * If the user clicks the X button to close the application
	 * We need to close the connection to free up the port
	 */
	public void windowClosing(WindowEvent e) {
		// If server is running, close the connection
		if(server != null) {
			try {
				server.stop();
			}
			catch(Exception eClose) {
			}
			server = null;
		}
		// Dispose and exit
		dispose();
		System.exit(0);
	}
	// The other methods can be ignored as they do not need to be captured
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	/*
	 * A thread to run the Server on
	 */
	class ServerRunning extends Thread {
		public void run() {
			// The thread will not continue while the server is running
			server.start();
			// If we got here, the server stopped.
			stopStart.setText("Start");
			// Allow the user to edit the port number again
			tPortNumber.setEditable(true);
			appendEvent("Server stopped\n");
			server = null;
		}
	}

}
