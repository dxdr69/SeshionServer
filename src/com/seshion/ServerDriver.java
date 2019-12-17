package com.seshion;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public class ServerDriver {
		/* for when we want to specify the port number from shell */
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

	
	/* make our streams which send and receive the data */
	public static void main(String[] args) throws IOException
	{
//		OpenSeshion  opS = new OpenSeshion();
//		opS.start();
//
//		DBManager db = new DBManager();
//
//		// /* just making some seshions */
//		UserAccount twizzy = new UserAccount("Twizzy", "doesn't matter");	
//		ArrayList<UserSession> seshions = new ArrayList<UserSession>();
//
//		UserSession sesh = new UserSession("Scazi", "Twizzy", null,
//				41.063378, -73.546277,
//				-73.546277, 41.062885,
//				41.063378, -73.545720,
//				41.062885, -73.545720,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, null);
//		seshions.add(sesh);
//		System.out.println("createdusersession object");
//
//		UserSession sesh2 = new UserSession("Calf Pasture Beach Skatepark", "Twizzy", null,
//				41.084396, -73.396121,
//				41.084020, -73.395482,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, null);
//		seshions.add(sesh2);
//
//		UserSession sesh3 = new UserSession("Danbury Skatepark", "Twizzy", null,
//				41.396016, -73.450117,
//				41.395187, -73.449259,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, null);
//		seshions.add(sesh3);
//
//		UserSession sesh4 = new UserSession("Newtown Skatepark", "Twizzy", null,
//				41.397028,  -73.301002,
//				41.395187, -73.449259,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, null);
//		seshions.add(sesh4);
//
//		for (int i=0; i<seshions.size(); i++)
//		{
//			System.out.println("Seshion: " + seshions.get(i).getName());
//			System.out.println(seshions.get(i).getStartDate());
//			System.out.println(seshions.get(i).getStartTime());
//			System.out.println();
//		}
//
//		for (int i=0; i<seshions.size(); i++) {
//			int result = db.createNewSession(seshions.get(i));
//			System.out.println("Result of create session " + (i+1) + ":" + result);
//		}
//
//		/* Testing getFriends function*/
//		String user1 = twizzy.getUserName();
//		List<UserAccount> friends = db.getFriends(user1);
//
//		if (!friends.isEmpty())
//		{
//			System.out.println("1st friend of Twizzy: " + friends.get(0).getUserName());
//			System.out.println("Current location: " + friends.get(0).getLatitude() + ", " 
//					+ friends.get(0).getLongitude());
//			System.out.println("Online: " + friends.get(0).isLoggedIn());
//			System.out.println("Private profile: " + friends.get(0).isProfilePrivate());
//		}
//
//		UserGroup ug1 = new UserGroup("Scazi homies sesh", "Twizzy");
//		UserGroup ug2 = new UserGroup("Tennis with the girls", "Twizzy");
//		UserGroup ug3 = new UserGroup("Volley ball", "Twizzy");
//
//		UserGroup ug4 = new UserGroup("Group project", "NickIsTheMan");
//
//		System.out.println("groups created");
		DBManager dbMan = new DBManager();
//		dbMan.createNewGroup(ug1);
//		dbMan.createNewGroup(ug2);
//		dbMan.createNewGroup(ug3);
//		dbMan.createNewGroup(ug4);
//		System.out.println("Groups added");
//
//		dbMan.addGroupMember(ug4.getID(), "Twizzy");
//
//		System.out.println("Joined Group");
////
		ArrayList<String> invitedUsers = new ArrayList<String>();
		invitedUsers.add("Twizzy");
		invitedUsers.add("David1");
		invitedUsers.add("NickIsTheMan");
		invitedUsers.add("Wushenghui");
////
//		UserSession sesh = new UserSession("PresentationSesh", "Twizzy", "We look so good right now",
//				41.401121, -73.445337,
//				41.400845, -73.445098,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, invitedUsers);		
//
//		System.out.println("createdusersession object");
//		     
//
//		System.out.println("created new sesh");
//		UUID id = sesh.getID();
//		System.out.println("id = " + id.toString());
		//dbMan.checkInSessionUser(id, "Twizzy");
		//System.out.println("checked twizzy in");
		
//		UserSession sesh = new UserSession("Scazi Skatepark", "Twizzy", "Shred the best bowls on the east coast!",
//				41.063325, -73.546173,
//				41.062997, -73.545847,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, invitedUsers);
//		
//		UserSession sesh2 = new UserSession("Danbury Skatepark", "Twizzy", "I love and hate this skatepark",
//				41.395782, -73.449879,
//				41.395605, -73.449410,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, invitedUsers);	
//		
//		UserSession sesh3 = new UserSession("Newtown skatepark", "Twizzy", "The training grounds for learning rails",
//				41.397034, -73.301047,
//				41.396513, -73.300047,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, invitedUsers);	
//		
//		UserSession sesh4 = new UserSession("Calf Pasture", "Twizzy", "The chill zone",
//				41.084394, -73.395972,
//				41.084052, -73.395548,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, invitedUsers);
//		
//		UserSession sesh5 = new UserSession("Scazi Tennis courts", "Twizzy", "We look so good right now",
//				41.06388457115096, -73.55072917112095,
//				41.063378178899136,  -73.54955543684893,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, invitedUsers);
//		
//		UserSession sesh6 = new UserSession("Scazi Baseball field", "Twizzy", "We look so good right now",
//				41.06231522755108, -73.55008114981585,
//				41.06121990063457, -73.54819502056755,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, invitedUsers);
//		
//		UserSession sesh7 = new UserSession("PresentationSesh", "Twizzy", "We look so good right now",
//				41.401121, -73.445337,
//				41.400845, -73.445098,
//				LocalDate.now(), null, LocalTime.now(), null,
//				false, invitedUsers);	
//		
//		dbMan.createNewSession(sesh);
//		dbMan.createNewSession(sesh2);
//		dbMan.createNewSession(sesh3);
//		dbMan.createNewSession(sesh4);
//		dbMan.createNewSession(sesh5);
//		dbMan.createNewSession(sesh6);
//		dbMan.createNewSession(sesh7);

		ServerSocket Server = new ServerSocket(8090);
		System.out.println("waiting for connect");
		while(true)
		{
			Socket socket = Server.accept();
			new Server(socket).start();
		}

	}

}
