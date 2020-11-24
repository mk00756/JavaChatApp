package org.com1028.cw2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	// The label to tell the user what the field is for
	private JLabel label;
	// Text field for user input (username and messages)
	private JTextField tf;
	// Text fields hold the server address an the port number
	private JTextField tfServer, tfPort;
	// Buttons to log in, log out, and to get the list of users
	private JButton login, logout, listUsers;
	// A text area for the chat room
	private JTextArea ta;
	// Determines whether the client is connected to a server or not
	private boolean connected;
	// The client object
	private Client client;
	// Default port and host
	private int defaultPort;
	private String defaultHost;
	// Secret for encryption
	private static String secret = "u11LsaDOZBoeNTCnDd3k";

	// Constructor, with socket as parameter
	ClientGUI(String host, int port) {
		super("Chat Client");
		defaultPort = port;
		defaultHost = host;
		
		// Create a north panel
		JPanel northPanel = new JPanel(new GridLayout(3,1));
		// Create a panel for the two text fields and their labels
		JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		// Create the text fields with default values inserted
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

		// Add the labels and text fields to the panel
		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		// add the socket panel to the north panel
		northPanel.add(serverAndPort);

		// Create the text field for the user's username and messages
		label = new JLabel("Enter your username below", SwingConstants.CENTER);
		northPanel.add(label);
		tf = new JTextField("User");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		// Move the north panel to the north
		add(northPanel, BorderLayout.NORTH);

		// Create the chat room and set it as the center panel
		ta = new JTextArea("Welcome. Please connect to a server.\n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

		// Create the Log In, Log Out, and List Users buttons, and then add them to the south panel
		login = new JButton("Log In");
		login.addActionListener(this);
		logout = new JButton("Log out");
		logout.addActionListener(this);
		logout.setEnabled(false);		// Begin disabled
		listUsers = new JButton("List Users");
		listUsers.addActionListener(this);
		listUsers.setEnabled(false);		// Begin disabled

		JPanel southPanel = new JPanel();
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(listUsers);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();

	}

	// Called by the client to append text in the TextArea 
	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}
	// Called by the GUI is the connection failed
	// The changes to the fields and buttons are reverted
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		listUsers.setEnabled(false);
		label.setText("Enter your username below");
		tf.setText("User");
		// reset port number and host name as a construction time
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		// let the user change them
		tfServer.setEditable(true);
		tfPort.setEditable(true);
		// don't react to a <CR> after the username
		tf.removeActionListener(this);
		connected = false;
	}
		
	// Capture the press of a button
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		// If it is the Log Out button, disconnect
		if(o == logout) {
			client.sendMessage(new ChatMessage(ChatMessage.DISCONNECT, ""));
			return;
		}
		// If it is the List Users button, list users
		if(o == listUsers) {
			client.sendMessage(new ChatMessage(ChatMessage.LISTUSERS, ""));				
			return;
		}
		// If none of the above if statements captured the event, then the user is trying to send a message
		if(connected) {
			// just have to send the message
			String msg = AES.encrypt(tf.getText(), secret);
			client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));				
			tf.setText("");
			return;
		}
		// All other options have been tested, the user must be trying to log in
		if(o == login) {
			String username = tf.getText().trim();
			// Ignore the request if the username is empty
			if(username.length() == 0)
				return;
			// Ignore the request if the server address is empty
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			// Ignore the request if the port number is invalid or empty
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;
			}

			// Replace the client with the GUI
			client = new Client(server, port, username, this);
			// Check if we can start the client
			if(!client.start()) 
				return;
			tf.setText("");
			label.setText("Enter your message below");
			connected = true;
			
			// disable login button
			login.setEnabled(false);
			// enable the buttons that started out disabled
			logout.setEnabled(true);
			listUsers.setEnabled(true);
			// disable the Server and Port fields
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			// Action listener captures user trying to send a message
			tf.addActionListener(this);
		}

	}

	public static void main(String[] args) {
		// Start the client with default socket
		new ClientGUI("localhost", 8050);
	}

}
