package org.com1028.cw2;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
	// a unique ID for each connection
	private static int conID;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> listOfClients;
	// if I am in a GUI
	private ServerGUI sGUI;
	// to display time
	private SimpleDateFormat dateTime;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean running;
	// the secret used for encryption
	private static String secret = "u11LsaDOZBoeNTCnDd3k";
	
	public Server(int port, ServerGUI sg) {
		// GUI or not
		this.sGUI = sg;
		// the port
		this.port = port;
		// to display hh:mm:ss
		dateTime = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		listOfClients = new ArrayList<ClientThread>();
		// Generate a secret
	}
	
	// Start the server
	public void start() {
		running = true;
		// Create server socket and wait for connection requests
		try 
		{
			// The socket to be used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// Loop while running and wait for connections
			while(running) 
			{
				// Log that the server has been started
				display("Server listening on port " + port);
				
				Socket socket = serverSocket.accept();  	// Accept a connection
				// If the server is no longer running, break out
				if(!running)
					break;
				ClientThread t = new ClientThread(socket);  // create a thread for the new connection
				listOfClients.add(t);						// put it into the client list
				t.start();									// start the thread
			}
			// Server no longer running
			try {
				serverSocket.close();
				// loop through all of the clients and disconnect them
				for(int i = 0; i < listOfClients.size(); ++i) {
					ClientThread tc = listOfClients.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			// Catch exception and output error message
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		// There is an issue with the server
		catch (IOException e) {
            String msg = dateTime.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}		
    // Stop the server
	@SuppressWarnings("resource")
	protected void stop() {
		running = false;
		// Connect to the server in order to stop it
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}
	}
	
	// Display an event
	private void display(String msg) {
		String event = dateTime.format(new Date()) + " " + msg;
			sGUI.appendEvent(event + "\n");
	}
	
	// Broadcast a message to all connected users
	private synchronized void broadcast(String message) {
		// add current time and '\n' to the message
		String time = dateTime.format(new Date());
		String messageLf = "[" + time + "] " + message + "\n";
		// display the message
			sGUI.appendRoom(messageLf);
		
		// We loop in reverse order in case we would have to remove a client because it has disconnected
		for(int i = listOfClients.size(); --i >= 0;) {
			// Get the client thread
			ClientThread ct = listOfClients.get(i);
			/*
			 *  Attempt to write to the client
			 *  If it fails, remove it from the list
			 */
			if(!ct.writeMsg(messageLf)) {
				listOfClients.remove(i);
				display(ct.username + " has been disconnected.");
			}
		}
	}

	// Disconnect a client who has logged off
	synchronized void remove(int id) {
		// Loop through the list of clients
		for(int i = 0; i < listOfClients.size(); ++i) {
			ClientThread ct = listOfClients.get(i);
			// If the id of the client matches the parameter, remove the client from the list
			if(ct.id == id) {
				listOfClients.remove(i);
				return;
			}
		}
	}

	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		// The socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// The ID (easier to disconnect with)
		int id;
		// Client's username
		String username;
		// The message
		ChatMessage cm;
		// The date on which the client connects
		String date;

		// Constructor
		ClientThread(Socket socket) {
			// a unique id
			id = ++conID;
			this.socket = socket;
			// Creating both data streams
			try
			{
				// Create I/O streams
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// Read the username
				username = (String) sInput.readObject();
				// Log the connection
				display(username + " has been connected.");
			}
			// Catch exception while creating I/O streams
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			// Catch the ClassNotFoundException, even if a string has been entered
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}

		// Run forever until the client disconnects
		public void run() {
			boolean connected = true;
			while(connected) {
				// Read the chat message object
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				// Catch exception while reading the object
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// extract the message from the object and decrypt it
				String msg = AES.decrypt(cm.getMessage(), secret);

				// Perform a switch case on the type of message sent
				switch(cm.getType()) {
				// If it's a normal message, simply broadcast it
				case ChatMessage.MESSAGE:
					broadcast(username + ": " + msg);
					break;
					// If it's a disconnect message, disconnect the client, and broadcast a message
				case ChatMessage.DISCONNECT:
					display(username + " has been disconnected.");
					broadcast(username + " has disconnected.");
					connected = false;
					break;
					// If requesting to see users, list the currently connected clients
				case ChatMessage.LISTUSERS:
					writeMsg("List of current users at " + dateTime.format(new Date()) + "\n");
					// Loop through all the users connected and output them
					for(int i = 0; i < listOfClients.size(); ++i) {
						ClientThread ct = listOfClients.get(i);
						writeMsg((i+1) + ") " + ct.username + ": connected since " + ct.date);
					}
					break;
				}
			}
			// Remove the client from the list of clients, then close the thread
			remove(id);
			close();
		}
		
		// Try to close everything
		private void close() {
			// Try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		// Write a string to the client output stream
		private boolean writeMsg(String msg) {
			// Only if the client is still connected, send them the message, otherwise close.
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// Write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// If an error occurs, simply inform the user
			catch(IOException e) {
				display("Error sending message to: " + username);
				display(e.toString());
			}
			return true;
		}
	}
	
}


