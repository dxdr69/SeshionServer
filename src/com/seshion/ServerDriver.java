package com.seshion;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

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
		
		/* just making some seshions */
//		UserAccount twizzy = new UserAccount("Twizzy", "doesn't matter");
//		DBManager db = new DBManager();
//		
//		ArrayList<UserSession> seshions = new ArrayList<UserSession>();
//		
//        UserSession sesh = new UserSession("Scazi", "Twizzy",
//                41.063378, -73.546277,
//                -73.546277, 41.062885,
//                41.063378, -73.545720,
//                41.062885, -73.545720,
//                LocalDate.now(), LocalTime.NOON,
//                false);
//        seshions.add(sesh);
//
//        UserSession sesh2 = new UserSession("Calf Pasture Beach Skatepark", "Twizzy",
//                41.084396, -73.396121,
//                41.084020, -73.395482,
//                LocalDate.now(), LocalTime.NOON,
//                false);
//        seshions.add(sesh);
//
//        UserSession sesh3 = new UserSession("Danbury Skatepark", "Twizzy",
//                41.396016, -73.450117,
//                41.395187, -73.449259,
//                LocalDate.now(), LocalTime.NOON,
//                false);
//        seshions.add(sesh);
//        
//        UserSession sesh4 = new UserSession("Newtown Skatepark", "Twizzy",
//                41.397028,  -73.301002,
//                41.395187, -73.449259,
//                LocalDate.now(), LocalTime.NOON,
//                false);
//        seshions.add(sesh);
//        
//        for(int i =0; i< seshions.size(); i++) {
//        	int result = db.createNewSession(seshions.get(i));
//        	System.out.println("Result of create session" + i + ":" + result);
//        }
        	
        
        
		ServerSocket Server = new ServerSocket(8090);
		System.out.println("waiting for connect");
		while(true)
		{
			Socket socket = Server.accept();
			new Server(socket).start();
		}
	}

}
