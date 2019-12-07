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
				while (true) {
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

						//Json Parser to convert objects and string to Json form in order to send it to client
						Gson gson = new Gson();
						JsonParser parser = new JsonParser();
						DBManager db = new DBManager(); //DBManger perform all function that talk to the data bass
						JsonArray array = parser.parse(decryptedString).getAsJsonArray();
						System.out.println("array created");
						String action = gson.fromJson(array.get(0), String.class); //get operation from the client
						System.out.println("deserialized action:" + action);


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
							Collection collection = new ArrayList(); // a collection with vary types of info
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
							String jString = gson.toJson(collection); //convert the collection to Json format String

							/* send response to client */
							byte[] encryptedResponseArray = aes.encrypt(jString.getBytes()); //encrypting the string
							dataNetOutputStream.writeInt(encryptedResponseArray.length);
							dataNetOutputStream.write(encryptedResponseArray);
							System.out.println("send string");
						} else if (action.equals("logout")) {
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
						//set coordinates and check whether or not user is in the open seshion range for check in
						else if (action.equals("setcoordinates")) {
							System.out.println("reach setcoordinates if statement");
							UserAccount user = gson.fromJson(array.get(1), UserAccount.class);
							System.out.println("User latitude: " + user.getLatitude() + "\n" + "User longitude: " + user.getLongitude());
							db.setUserCoordinates(user.getUserName(), user.getLatitude(), user.getLongitude());
							List<UserSession> userSessions = db.getAllOpenSessions();
							UserSession checkedIn = null;
							boolean withinRange = false;
							String result = "0";
							//loop through the all open session list
							for (int i = 0; i < userSessions.size(); i++) {
								System.out.println("inside of for loop");
								UserSession uSh = userSessions.get(i);
								//check whether or not the user latitude is in the range
								if (user.getLatitude() <= uSh.getLatitudeTopRight() && user.getLatitude() >= uSh.getLatitudeBottomRight()) {
									System.out.println("latitude is in the range");
									//check whether or not the user longitude is in the range
									if (user.getLongitude() <= uSh.getLongitudeTopLeft() && user.getLongitude() >= uSh.getLongitudeTopRight()) {
										System.out.println("Longitude is in the range");
										withinRange = true;
										checkedIn = uSh; //get the session where the user is.
										result = String.valueOf(db.checkInSessionUser(checkedIn.getID(), user.getUserName()));
										System.out.println("Check in function was called");
										break;
									}
								}
							}
							Collection collection = new ArrayList();
							collection.add(result);
							System.out.println("get result: " + result);
							if (withinRange) { //if the user is in the range add the checked in session into array
								collection.add(checkedIn);
								System.out.println("get the checked in seshion " + checkedIn.getName());
							}
							String jString = gson.toJson(collection);
							byte[] encryptedResponse = aes.encrypt(jString.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("addfriend")) {
							System.out.println("reach addfriend if statement");
							UserAccount user = gson.fromJson(array.get(1), UserAccount.class);
							String friendName = gson.fromJson(array.get(2), String.class);
							String result = gson.toJson(String.valueOf(db.sendFriendRequest(user.getUserName(), friendName)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("changeuservisibility")) {
							System.out.println("reach changeuservisibility if statement");
							UserAccount user = gson.fromJson(array.get(1), UserAccount.class);
							String result = gson.toJson(String.valueOf(db.changeUserVisibility(user)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						}
					/*else if(action=="friendRequest")
					{
					//need a function which returns all friends that isfriendrequestaccepted = false
					//then in the client user can decide whether to accept or not. Once user accept call db.manageFriendRequest
						String result = String.valueOf(db.manageFriendRequest(aUser));
						encryptedByteArray = Cryptor.encryption(key, result);
						outStream.write(encryptedByteArray);
					}
					*/
						else if (action.equals("getfriend")) {
							System.out.println("reach getfriend if statement");
							UserAccount user = gson.fromJson(array.get(1), UserAccount.class);
							String result = gson.toJson(db.getFriends(user.getUserName()));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("removefriend")) {
							System.out.println("reach removefriend if statement");
							UserAccount user = gson.fromJson(array.get(1), UserAccount.class);
							String friendName = gson.fromJson(array.get(2), String.class);
							String result = gson.toJson(String.valueOf(db.removeFriend(user.getUserName(), friendName)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("reloadseshion")) {
							System.out.println("reach reloadseshion if statement");
							UserAccount user = gson.fromJson(array.get(1), UserAccount.class);
							Collection collection = new ArrayList();
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
						} else if (action.equals("reloadgroup")) {
							System.out.println("reach reloadgroup if statement");
							UserAccount user = gson.fromJson(array.get(1), UserAccount.class);
							Collection collection = new ArrayList();
							collection.add(db.getOwnedGroups(user.getUserName()));
							System.out.println("get owned groups");
							collection.add(db.getJoinedGroups(user.getUserName()));
							System.out.println("get Joined groups");
							String jString = gson.toJson(collection);
							/* send response to client */
							byte[] encryptedResponseArray = aes.encrypt(jString.getBytes());
							dataNetOutputStream.writeInt(encryptedResponseArray.length);
							dataNetOutputStream.write(encryptedResponseArray);
							System.out.println("send string");
						} else if (action == "createnewgroup") {
							System.out.println("reach createnewgroup if statement");
							String groupName = gson.fromJson(array.get(1), String.class);
							UserAccount user = gson.fromJson(array.get(2), UserAccount.class);
							List<UserAccount> friend = gson.fromJson(array.get(3), List.class);
							UserGroup group = new UserGroup(groupName, user.getUserName());
							for (int i = 0; i < friend.size(); i++) {
								group.addGroupMember(friend.get(i).getUserName());
							}
							String result = gson.toJson(String.valueOf(db.createNewGroup(group)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("deletegroup")) {
							System.out.println("reach deletegroup if statement");
							UserGroup group = gson.fromJson(array.get(1), UserGroup.class);
							String result = gson.toJson(String.valueOf(db.deleteGroup(group)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("addgroupmember")) {
							System.out.println("reach addgroupmember if statement");
							UserGroup group = gson.fromJson(array.get(1), UserGroup.class);
							String memberName = gson.fromJson(array.get(2), String.class);
							String result = gson.toJson(String.valueOf(db.addGroupMember(group.getID(), memberName)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("removegroupmember")) {
							System.out.println("reach removegroupmember if statement");
							UserGroup group = gson.fromJson(array.get(1), UserGroup.class);
							String memberName = gson.fromJson(array.get(2), String.class);
							String result = gson.toJson(String.valueOf(db.removeGroupMember(group.getID(), memberName)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("getgroupmember")) {
							System.out.println("reach getgroupmember if statement");
							UserGroup group = gson.fromJson(array.get(1), UserGroup.class);
							String result = gson.toJson(db.getGroupMembers(group.getID()));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("changegroupname")) {
							System.out.println("reach changegroupname if statement");
							UserGroup group = gson.fromJson(array.get(1), UserGroup.class);
							String newName = gson.fromJson(array.get(2), String.class);
							String result = gson.toJson(String.valueOf(db.changeGroupName(group.getID(), newName)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("getgroupmessages")) {
							System.out.println("reach getgroupmessages if statement");
							UserGroup group = gson.fromJson(array.get(1), UserGroup.class);
							String result = gson.toJson(db.getGroupMessages(group.getID()));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("sendmessageindividual")) {
							System.out.println("reach sendmessageindividual if statement");
							Message ms = gson.fromJson(array.get(1), Message.class);
							String recipient = gson.fromJson(array.get(2), String.class);
							String result = gson.toJson(String.valueOf(db.sendMessageIndividual(ms, recipient)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("sendmessagegroup")) {
							System.out.println("reach sendmessagegroup if statement");
							Message ms = gson.fromJson(array.get(1), Message.class);
							UserGroup recipient = gson.fromJson(array.get(2), UserGroup.class);
							String result = gson.toJson(String.valueOf(db.sendMessageGroup(ms, recipient.getID())));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("createnewsession")) {
							System.out.println("reach createnewsession if statement");
							UserSession seshion = gson.fromJson(array.get(1), UserSession.class);
							String result = gson.toJson(String.valueOf(db.createNewSession(seshion)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("cancelsession")) {
							System.out.println("reach cancelsession if statement");
							UserSession seshion = gson.fromJson(array.get(1), UserSession.class);
							String result = gson.toJson(String.valueOf(db.cancelSession(seshion)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("endsession")) {
							System.out.println("reach endsession if statement");
							UserSession seshion = gson.fromJson(array.get(1), UserSession.class);
							String result = gson.toJson(String.valueOf(db.endSession(seshion.getID())));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						} else if (action.equals("invitesessionuser")) {
							System.out.println("reach invitesessionuser if statement");
							UserSession seshion = gson.fromJson(array.get(1), UserSession.class);
							String userName = gson.fromJson(array.get(2), String.class);
							String result = gson.toJson(String.valueOf(db.inviteSessionUser(seshion.getID(), userName)));
							System.out.println("get result: " + result);
							byte[] encryptedResponse = aes.encrypt(result.getBytes());
							System.out.println("encryption successful:" + new String(encryptedResponse));
							int responseSize = encryptedResponse.length;
							dataNetOutputStream.writeInt(responseSize);
							dataNetOutputStream.write(encryptedResponse);
							System.out.println("send string");
						}


					} else
						System.out.println("byte array has no length outside of while");

				}
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
