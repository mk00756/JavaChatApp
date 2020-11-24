package org.com1028.cw2;
import java.net.*;
import java.io.*;

public class Client  {

	// For I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;					// the socket
	
	// if I use a GUI or not
	private ClientGUI cGUI;
	
	// the server, the port and the username
	private String server, username;
	private int port;

	/*
	 * Constructor call when used from a GUI
	 * in console mode the ClienGUI parameter is null
	 */
	Client(String server, int port, String username, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.cGUI = cg;
	}
	
	public boolean start() {
		// Try to connect to the server
		try {
			socket = new Socket(server, port);
		} 
		// Catch and return if connection failed
		catch(Exception ec) {
			display("Error connecting to server: " + ec);
			return false;
		}
		
		String msg = "Connection established: " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		// Create I/O streams
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		// Catch and return if creating streams failed
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// Creates a thread to listen from the server
		new ServerListener().start();
		// Send our username to the server
		try
		{
			sOutput.writeObject(username);
		}
		// Catch and return if an exception occurs
		catch (IOException eIO) {
			display("Exception logging in: " + eIO);
			disconnect();
			return false;
		}
		return true;
	}

	// To append a message to the chat room
	private void display(String msg) {
			cGUI.append(msg + "\n");		// append to the ClientGUI JTextArea
	}
	
	// To send a message to the server
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	// If an error has occurred, close the I/O streams and disconnect
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} // No actions to be taken
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {}
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {}
			cGUI.connectionFailed();
			
	}

	// A class that waits for the message from the server and append them to the JTextArea
	class ServerListener extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
						cGUI.append(msg);
				}
				catch(IOException e) {
					display("Server has closed the connection: " + e);
						cGUI.connectionFailed();
					break;
				}
				// mandatory exception catch
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}

