package com.seshion;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerDriver {
//
//	public static void main(String[] args) {
//		/* get user from input */
//		String portString = args[0];
//		int port = Integer.parseInt(portString);
//		
//		
//		
//		/* create a server object with the user specified port */
//		Server server = new Server(port);
//	}
//	
	/* make our streams which send and receive the data */
	public static void main(String[] args) throws IOException
	{
//		OpenSeshion  opS = new OpenSeshion();
//        opS.start();
		ServerSocket Server = new ServerSocket(8090);
		System.out.println("waiting for connect");
		while(true)
		{
			Socket socket = Server.accept();
			new Server(socket).start();
		}
	}

}
