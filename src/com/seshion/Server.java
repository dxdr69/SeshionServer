package com.seshion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.UUID;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class Server extends Thread {
	/* declare socket and input stream */
	private Socket socket = null;
	private ServerSocket server = null;
	private DataInputStream dataBufferedNetInputStream = null;
	private DataOutputStream dataBufferedNetOutputStream = null;
	private DataOutputStream dataNetOutputStream = null;
	private DataInputStream dataNetInputStream = null;
	/* RSA */
	private String publicKey = null;
	private String privateKey = null;
	/* AES */
	private String aesSymmetricKey = null;

	/* constructor with parameter port number */
	public Server(Socket socket) {
		/* start server and wait for a connection */
		this.socket = socket;
	}

	public void run() {
		try {
			/* good to have these for print outs */
			String clientAddress = socket.getInetAddress().getHostAddress();
			String clientHostName = socket.getInetAddress().getHostName();
			System.out.println(clientHostName + "(" + clientAddress + ")" + " Client accepted!");

			/*
			 * netOutputStream decorated by bufferedInputStream, decorated by
			 * dataOutputStream
			 */
			dataBufferedNetOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

			/* without the buffered to send simple data types */
			dataNetOutputStream = new DataOutputStream(socket.getOutputStream());

			/*
			 * netInputStream decorated by bufferedInputStream, decorated by dataInputStream
			 */
			dataBufferedNetInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

			/* without the buffered to receive */
			dataNetInputStream = new DataInputStream(socket.getInputStream());

			System.out.println("Making RSA Keys...");
			/* generate RSA public and private keys */
			RSA rsa = null;

			/* generate keys of given length */
			rsa = new RSA();
			rsa.generateKeys(1024);
			System.out.println("keys created!");

			/* fetch public key from RSA object */
			byte[] publicKey = rsa.getPublicKey().getEncoded();

			/* send key length and public key to client */
			int publicKeyLength = publicKey.length;
			System.out.println(
					"public key length:" + publicKeyLength + "\nAttempting to send public key length to client...");
			dataNetOutputStream.writeInt(publicKeyLength);
			System.out.println("Attempting to send public key to client...");
			dataNetOutputStream.write(publicKey, 0, publicKeyLength);
			System.out.println("Sent!");

			/* making symmetric key 16 bytes, (client knows to send one that length) */
			byte[] encryptedAESKey = new byte[128];
			byte[] realAESKey = new byte[16];

			/* obtain encrypted AES (symmetric) key information from client */
			try {

				/* after RSA encryption, total size of sent data is 128 */
				dataNetInputStream.readFully(encryptedAESKey, 0, 128);
				System.out.println("obtained key from client:" + encryptedAESKey);

			} catch (EOFException eofe) {

				System.out.println("End Of File Exception, no more data sent in this stream\n" + eofe);
				// eofe.printStackTrace();
				// break;

			} catch (IOException ioe) {

				System.out.println("IO encountered while server running:\n" + ioe);
				ioe.printStackTrace();
			}

			/* decrypt symmetric key using RSA */
			System.out.println("Encrypted symmetric key:" + encryptedAESKey);
			realAESKey = rsa.decrypt(encryptedAESKey);

			/* use newly obtained symmetric key to decrypt actions sent from client */
			if (realAESKey.length > 0) {

				/* array to contain the encrypted json */
				byte[] encryptedByteArray = null;

				try {

					/* read the length of the array from the client */
					System.out.println("attempting to read length of data from client using non buffered input stream");
					int length = dataNetInputStream.readInt();
					System.out.println("length of json sent from client:" + length);
					encryptedByteArray = new byte[length];

					/* obtain the json data from the client */
					dataNetInputStream.read(encryptedByteArray, 0, length);
					System.out.println("length of json encrypted array:" + encryptedByteArray.length);

				} catch (EOFException eofe) {
					System.out.println("End Of File Exception, no more data sent in this stream\n" + eofe);
					// eofe.printStackTrace();
					// break;

				} catch (IOException ioe) {
					System.out.println("IO encountered while server running:\n" + ioe);
					ioe.printStackTrace();
				}

				/* decrypt the action */
				if (encryptedByteArray.length > 0) {

					// try {
					// string to contain the encrypted text
					System.out.println("attempting to decrypt array");
					AES aes = new AES(realAESKey);
					byte[] decrytpedByteArray = aes.decrypt(encryptedByteArray);
					String decryptedString = new String(decrytpedByteArray);
					System.out.println("decrypted String:" + decryptedString);

					Gson gson = new Gson();
					JsonParser parser = new JsonParser();
					JsonArray array = parser.parse(decryptedString).getAsJsonArray();
					System.out.println("array created");
					String action = gson.fromJson(array.get(0), String.class);
					DBManager db = new DBManager();
					System.out.println("deserialized action:" + action);

					/// ByteArrayOutputStream bos = new ByteArrayOutputStream();
					// List<UserGroup> groupList = new ArrayList<UserGroup>();
					// List<UserSession> sessionList = new ArrayList<UserSession>();

					if (action.equals("createuser")) {
						System.out.println("reached create user");
						UserAccount user = gson.fromJson(array.get(1), UserAccount.class);
						System.out.println("made user");
						String result = gson.toJson(String.valueOf(db.createNewUser(user)));
						System.out.println("made result string" + result);
						byte[] encryptedResponse = aes.encrypt(result.getBytes());
						System.out.println("encryption successful:" + new String(encryptedResponse));
						int responseSize = encryptedResponse.length;
						dataNetOutputStream.writeInt(responseSize);
						dataNetOutputStream.write(encryptedResponse); // createNewUser Function will return 0 if
																		// username is already taken
						// user registration calls the new user function in db class
						System.out.println("send successful!");
						
						
					} else if (action.equals("login")) {
						System.out.println("reach if statement");
						UserAccount user = gson.fromJson(array.get(1), UserAccount.class);
						String result = String.valueOf(db.userLogIn(user));
						Collection collection = new ArrayList();
						collection.add(result);
						System.out.println("get result: " + result);
						// collection.add(db.getfriendrequest())
						collection.add(db.getFriends(user.getUserName()));
						System.out.println("get friends list");
						collection.add(db.getOwnedGroups(user.getUserName()));
						System.out.println("get owned groups");
						collection.add(db.getJoinedGroups(user.getUserName()));
						System.out.println("get Joined groups");
						collection.add(db.getUserMessages(user.getUserName()));
						System.out.println("get UserMessages");
						collection.add(db.getOwnedSessions(user.getUserName()));
						System.out.println("get OwnedSessions");
						collection.add(db.getInvitedSessions(user.getUserName()));
						System.out.println("get InvitedSessions");
						collection.add(db.getJoinedSessions(user.getUserName()));
						System.out.println("get JoinedSessions");
						collection.add(db.getAllOpenSessions());
						System.out.println("get JAllOpenSessions");
						String jString = gson.toJson(collection);
						
						/* send response to client */
						byte[] encryptedResponseArray = aes.encrypt(jString.getBytes());
						dataNetOutputStream.writeInt(encryptedResponseArray.length);
						dataNetOutputStream.write(encryptedResponseArray);
						System.out.println("send string");
					}

					else if (action.equals("logout")) {
						System.out.println("reached logout");
						UserAccount user = gson.fromJson(array.get(1), UserAccount.class);
						System.out.println("made user");
						String result = gson.toJson(String.valueOf(db.userLogOut(user.getUserName())));
						System.out.println("made result string" + result);
						byte[] encryptedResponse = aes.encrypt(result.getBytes());
						System.out.println("encryption successful:" + new String(encryptedResponse));
						dataNetOutputStream.write(encryptedResponse); // createNewUser Function will return 0 if
																		// username is already taken
						// user registration calls the new user function in db class
						System.out.println("send successful!");
					}
					// else if(action=="setcoordinates")
					// {
					// double latitude = (Double)json.get("latitude");
					// aUser.setLatitude(latitude);
					// double longitude =(Double)json.get("longitude");
					// aUser.setLongitude(longitude);
					// String result = String.valueOf(db.setUserCoordinates(aUser.getUserName(),
					// latitude, longitude));
					// encryptedByteArray = aes.encrypt(result.getBytes());
					// dataNetOutputStream.write(encryptedByteArray);
					// }
					/*
					 * else if(Action=="addfriend") { String friendname = (String)
					 * json.get("friendname"); String result =
					 * String.valueOf(db.sendFriendRequest(aUser.getUserName(), friendname));
					 * encryptedByteArray = aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="changeUservisibility") { aUser.changeProfileVisibility(); String
					 * result = String.valueOf(db.changeUserVisibility(aUser)); encryptedByteArray =
					 * aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); } /*else
					 * if(Action=="friendRequest") { //need a function which returns all friends
					 * that isfriendrequestaccepted = false //then in the client user can decide
					 * whether to accept or not. Once user accept call db.manageFriendRequest String
					 * result = String.valueOf(db.manageFriendRequest(aUser)); encryptedByteArray =
					 * Cryptor.encryption(key, result); outStream.write(encryptedByteArray); }
					 * 
					 * else if(Action=="getfriend") { List<String> result =
					 * db.getFriends(aUser.getUserName()); ObjectOutputStream oos = new
					 * ObjectOutputStream(bos); oos.writeObject(result); byte[] bytes =
					 * bos.toByteArray(); encryptedByteArray = aes.encrypt(bytes);
					 * dataBufferedNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="removefriend") { String friendname =(String)
					 * json.get("friendname"); String result =
					 * String.valueOf(db.removeFriend(aUser.getUserName(),friendname));
					 * encryptedByteArray = aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="getownedsessions") { List<UserSession> result =
					 * db.getOwnedSessions(aUser.getUserName()); ObjectOutputStream oos = new
					 * ObjectOutputStream(bos); oos.writeObject(result); byte[] bytes =
					 * bos.toByteArray(); encryptedByteArray = aes.encrypt(bytes);
					 * dataBufferedNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="getinvitedsessions") { List<UserSession> result =
					 * db.getInvitedSessions(aUser.getUserName()); ObjectOutputStream oos = new
					 * ObjectOutputStream(bos); oos.writeObject(result); byte[] bytes =
					 * bos.toByteArray(); encryptedByteArray = aes.encrypt(bytes);
					 * dataBufferedNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="getjoinedsessions") { List<UserSession> result =
					 * db.getJoinedSessions(aUser.getUserName()); ObjectOutputStream oos = new
					 * ObjectOutputStream(bos); oos.writeObject(result); byte[] bytes =
					 * bos.toByteArray(); encryptedByteArray = aes.encrypt(bytes);
					 * dataBufferedNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="getownedgroups") { List<UserGroup> result =
					 * db.getOwnedGroups(aUser.getUserName()); groupList = result;
					 * ObjectOutputStream oos = new ObjectOutputStream(bos);
					 * oos.writeObject(result); byte[] bytes = bos.toByteArray(); encryptedByteArray
					 * = aes.encrypt(bytes); dataBufferedNetOutputStream.write(encryptedByteArray);
					 * } else if(Action=="getjoinedgroups") { List<UserGroup> result =
					 * db.getJoinedGroups(aUser.getUserName()); ObjectOutputStream oos = new
					 * ObjectOutputStream(bos); oos.writeObject(result); byte[] bytes =
					 * bos.toByteArray(); encryptedByteArray = aes.encrypt(bytes);
					 * dataBufferedNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="getusermessages") { List<Message> result =
					 * db.getUserMessages(aUser.getUserName()); ObjectOutputStream oos = new
					 * ObjectOutputStream(bos); oos.writeObject(result); byte[] bytes =
					 * bos.toByteArray(); encryptedByteArray = aes.encrypt(bytes);
					 * dataBufferedNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="createnewgroup") { String groupname = (String)
					 * json.get("groupname"); UserGroup group = new UserGroup(groupname,
					 * aUser.getUserName()); groupList.add(group); String result =
					 * String.valueOf(db.createNewGroup(group)); encryptedByteArray =
					 * aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="deletegroup") { //String groupname = (String)
					 * json.get("groupname"); UUID groupid = (UUID) json.get("groupid"); for(int i
					 * =0 ; i<groupList.size(); i++) { if(groupList.get(i).getID() == groupid) {
					 * String result = String.valueOf(db.deleteGroup(groupList.get(i)));
					 * groupList.remove(i); encryptedByteArray = aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); break; } } } else
					 * if(Action=="addgroupmember") { String memberUsername = (String)
					 * json.get("memberusername"); UUID groupid = (UUID) json.get("groupid");
					 * for(int i =0 ; i<groupList.size(); i++) { if(groupList.get(i).getID() ==
					 * groupid) { String result = String.valueOf(db.addGroupMember(groupid,
					 * memberUsername)); groupList.get(i).addGroupMember(memberUsername);
					 * encryptedByteArray = aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); break; } } } else
					 * if(Action=="removegroupmember") { String memberUsername = (String)
					 * json.get("memberusername"); UUID groupid = (UUID) json.get("groupid");
					 * for(int i =0 ; i<groupList.size(); i++) { if(groupList.get(i).getID() ==
					 * groupid) { String result = String.valueOf(db.removeGroupMember(groupid,
					 * memberUsername)); groupList.get(i).removeGroupMember(memberUsername);
					 * encryptedByteArray = aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); break; } } } else
					 * if(Action=="getgroupmember") { UUID groupid = (UUID) json.get("groupid");
					 * List<String> result = db.getGroupMembers(groupid); ObjectOutputStream oos =
					 * new ObjectOutputStream(bos); oos.writeObject(result); byte[] bytes =
					 * bos.toByteArray(); encryptedByteArray = aes.encrypt(bytes);
					 * dataBufferedNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="changegroupname") { String newname = (String)
					 * json.get("newname"); UUID groupid = (UUID) json.get("groupid"); for(int i =0
					 * ; i<groupList.size(); i++) { if(groupList.get(i).getID() == groupid) { String
					 * result = String.valueOf(db.changeGroupName(groupid, newname));
					 * groupList.get(i).setName(newname); encryptedByteArray =
					 * aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); break; } } } else
					 * if(Action=="getgroupmessages") { UUID groupid = (UUID) json.get("groupid");
					 * for(int i =0 ; i<groupList.size(); i++) { if(groupList.get(i).getID() ==
					 * groupid) { List<Message> result= db.getGroupMessages(groupid);
					 * ObjectOutputStream oos = new ObjectOutputStream(bos);
					 * oos.writeObject(result); byte[] bytes = bos.toByteArray(); encryptedByteArray
					 * = aes.encrypt(bytes); dataBufferedNetOutputStream.write(encryptedByteArray);
					 * } } } else if(Action=="sendmessageindividual") { LocalDate dateCreated =
					 * (LocalDate) json.get("datecreated"); LocalTime timeCreated = (LocalTime)
					 * json.get("timecreated"); String messageContent = (String)
					 * json.get("messagecontent"); String recipient = (String)
					 * json.get("recipient"); Message msg = new Message(aUser.getUserName(),
					 * recipient, dateCreated,timeCreated,messageContent,true,false); String result
					 * = String.valueOf(db.sendMessageIndividual(msg, recipient));
					 * encryptedByteArray = aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="sendmessagegroup") { LocalDate dateCreated = (LocalDate)
					 * json.get("datecreated"); LocalTime timeCreated = (LocalTime)
					 * json.get("timecreated"); String messageContent = (String)
					 * json.get("messagecontent"); UUID groupid = (UUID) json.get("groupid");
					 * Message msg = new Message(aUser.getUserName(), null,
					 * dateCreated,timeCreated,messageContent,false,true); String result =
					 * String.valueOf(db.sendMessageGroup(msg, groupid)); encryptedByteArray =
					 * aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); } else
					 * if(Action==" createnewsession") { String sessionname = (String)
					 * json.get("sessionname"); double latitudeTopLeft = (double)
					 * json.get("latitudetopleft"); double latitudeTopRight = (double)
					 * json.get("latitudetopright"); double longitudeTopRight = (double)
					 * json.get("longitudetopright"); double longitudeTopLeft = (double)
					 * json.get("longitudetopleft"); double latitudeBottomLeft = (double)
					 * json.get("latitudebottomleft"); double longitudeBottomLeft = (double)
					 * json.get("longitudebottomleft"); double latitudeBottomRight = (double)
					 * json.get("latitudebottomright"); double longitudeBottomRight = (double)
					 * json.get("longitudebottomright"); LocalDate startDate = (LocalDate)
					 * json.get("startdate"); LocalTime startTime = (LocalTime)
					 * json.get("starttime"); boolean issessionprivate = (boolean)
					 * json.get("issessionprivate"); UserSession session = new
					 * UserSession(sessionname,aUser.getUserName(),latitudeTopLeft,latitudeTopRight,
					 * longitudeTopRight,
					 * longitudeTopLeft,latitudeBottomLeft,longitudeBottomLeft,latitudeBottomRight,
					 * longitudeBottomRight, startDate, startTime,issessionprivate);
					 * sessionList.add(session); String result =
					 * String.valueOf(db.createNewSession(session)); encryptedByteArray =
					 * aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="cancelsession") { UUID sessionID = (UUID) json.get("sessionid");
					 * for(int i =0 ; i<sessionList.size(); i++) { if(sessionList.get(i).getID() ==
					 * sessionID) { String result =
					 * String.valueOf(db.cancelSession(sessionList.get(i))); sessionList.remove(i);
					 * encryptedByteArray = aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); break; } } }
					 * 
					 * else if(Action=="endsession") { UUID sessionID = (UUID)
					 * json.get("sessionid"); for(int i =0 ; i<sessionList.size(); i++) {
					 * if(sessionList.get(i).getID() == sessionID) { String result =
					 * String.valueOf(db.endSession(sessionID)); sessionList.remove(i);
					 * encryptedByteArray = aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); break; } } } else
					 * if(Action=="invitesessionuser") { UUID sessionID = (UUID)
					 * json.get("sessionid"); String username = (String) json.get("username");
					 * for(int i =0 ; i<sessionList.size(); i++) { if(sessionList.get(i).getID() ==
					 * sessionID) { String result =
					 * String.valueOf(db.inviteSessionUser(sessionID,username));
					 * sessionList.get(i).inviteUser(username); encryptedByteArray =
					 * aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); break; } } } else
					 * if(Action=="checkinsessionuser") { UUID sessionID = (UUID)
					 * json.get("sessionid"); String result =
					 * String.valueOf(db.checkInSessionUser(sessionID,aUser.getUserName()));
					 * encryptedByteArray = aes.encrypt(result.getBytes());
					 * dataNetOutputStream.write(encryptedByteArray); } else
					 * if(Action=="checkinsessionuser") { UUID sessionID = (UUID)
					 * json.get("sessionid"); String result =
					 * String.valueOf(db.getAllSessionUsers(sessionID)); ObjectOutputStream oos =
					 * new ObjectOutputStream(bos); oos.writeObject(result); byte[] bytes =
					 * bos.toByteArray(); encryptedByteArray = aes.encrypt(bytes);
					 * dataBufferedNetOutputStream.write(encryptedByteArray); }
					 */

				} else
					System.out.println("byte array has no length outside of while");

			}

		} catch (EOFException eofe) {
			System.out.println("End Of File Exception encountered while starting server:\n" + eofe);
		} catch (IOException ioe) {
			System.out.println("IO encountered while starting server:\n" + ioe);
		}
	}
	/*
	 * try { server = new ServerSocket(port); System.out.println("Server started");
	 * System.out.println("waiting for a client to connect on port " + port +
	 * "...");
	 * 
	 * socket = server.accept();
	 */

} 
