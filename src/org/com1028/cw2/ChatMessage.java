package org.com1028.cw2;
import java.io.*;
/*
 * This is a simple class containing the message to be sent
 * as well as control signals for operation.
 * It also seems to be easier to send objects rather than just strings.
 */
public class ChatMessage implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	// The different types of control signals
	// LISTUSERS to receive the list of the users connected, not broadcasted
	// MESSAGE an ordinary message, message is broadcasted
	// LOGOUT to disconnect from the Server, server broadcasts its own message upon completion
	static final int MESSAGE = 0, DISCONNECT = 1, LISTUSERS = 2;
	private int type;
	private String message;
	
	// Constructor
	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	// Getters for the two parameters
	int getType() {
		return type;
	}
	String getMessage() {
		return message;
	}
}

