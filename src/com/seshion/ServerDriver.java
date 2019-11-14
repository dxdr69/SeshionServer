package com.seshion;

public class ServerDriver {

	public static void main(String[] args) {
		/* get user from input */
		String portString = args[0];
		int port = Integer.parseInt(portString);
		
		
		
		/* create a server object with the user specified port */
		Server server = new Server(port);
	}

}
