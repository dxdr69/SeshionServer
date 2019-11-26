package com.seshion;


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

public class Server extends Thread
{
	/* declare socket and input stream */
	private Socket socket = null;
	private ServerSocket server = null;
	private DataInputStream dataBufferedNetInputStream = null;
	private DataOutputStream dataBufferedNetOutputStream = null;
	private DataOutputStream dataNetOutputStream = null;
	/* RSA */
	private String publicKey = null;
	private String privateKey = null;
	/* AES */
	private String aesSymmetricKey = null;
	
	/* constructor with parameter port number */
	public Server(Socket socket)
	{
		/* start server and wait for a connection */
		this.socket = socket; 
	}
	
	public void run()
	{
		try
		{
			/* good to have these for print outs */
			String clientAddress = socket.getInetAddress().getHostAddress();
			String clientHostName = socket.getInetAddress().getHostName();
			System.out.println(clientHostName + "(" + clientAddress + ")" + " Client accepted!");
			
			
			/* netOutputStream decorated by bufferedInputStream, decorated by dataOutputStream */
			dataBufferedNetOutputStream = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );
			
			/* without the buffered to send simple data types */
			dataNetOutputStream = new DataOutputStream ( socket.getOutputStream() );
			
			/* netInputStream decorated by bufferedInputStream, decorated by dataInputStream */
			dataBufferedNetInputStream = new DataInputStream( new BufferedInputStream( socket.getInputStream() ) );
			
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
			System.out.println("public key length:" + publicKeyLength + "\nAttempting to send public key length to client...");
			dataNetOutputStream.writeInt(publicKeyLength);
			System.out.println("Attempting to send public key to client...");
			dataNetOutputStream.write(publicKey,  0, publicKeyLength);
			System.out.println("Sent!");

			/* making symmetric key 16 bytes, (client knows to send one that length) */
			byte[] encryptedAESKey = new byte[128];
			byte[] realAESKey = new byte[16];

			/* obtain encrypted AES (symmetric) key information from client */	
			try {
				
				/* after RSA encryption, total size of sent data is 128 */
				dataBufferedNetInputStream.readFully(encryptedAESKey, 0, 128);
				System.out.println("obtained key from client:" + encryptedAESKey);
				
			}catch(EOFException eofe) {
						   
	
				System.out.println("End Of File Exception, no more data sent in this stream\n" + eofe);
				//eofe.printStackTrace();
				//break;
				
			}catch(IOException ioe) {
	
				System.out.println("IO encountered while server running:\n" + ioe);
				ioe.printStackTrace();
			}
			
			/* decrypt symmetric key */
			System.out.println("Encrypted symmetric key:" + encryptedAESKey);
			realAESKey = rsa.decrypt(encryptedAESKey);
			
			/* use symmetric key to decrypt */
			if(realAESKey.length>0) {
				
		   
					/* string to contain the encrypted text */
					byte[] encryptedByteArray = null;
					
						try {
							
							int length = dataBufferedNetInputStream.readInt();
							System.out.println("length sent from client:" + length);
							encryptedByteArray = new byte[length];
							dataBufferedNetInputStream.read(encryptedByteArray, 0, length);
							System.out.println("length of encrypted array:" + encryptedByteArray.length);
							
							
						}catch(EOFException eofe) {
							System.out.println("End Of File Exception, no more data sent in this stream\n" + eofe);
							//eofe.printStackTrace();
							//break;
							
						}catch(IOException ioe) {
							System.out.println("IO encountered while server running:\n" + ioe);
							ioe.printStackTrace();
						}
					
					/* decrypt the text */
					if(encryptedByteArray.length>0) {
						
						/* decrypt */
						AES aes = new AES(realAESKey);
						byte[] decrytpedByteArray = aes.decrypt(encryptedByteArray);						
															  																	
												   
						String decryptedString = new String(decrytpedByteArray);
						
						System.out.println("decrypted String:" + decryptedString);
						
					}else
						System.out.println("byte array has no length outside of while");
				
			}
		}
		catch(EOFException eofe)
		{
			System.out.println("End Of File Exception encountered while starting server:\n" + eofe);
		}
		catch(IOException ioe)
		{
			System.out.println("IO encountered while starting server:\n" + ioe);
		}
	}
		/*try
		{
			server = new ServerSocket(port);
			System.out.println("Server started");
			System.out.println("waiting for a client to connect on port " + port + "...");
			
			socket = server.accept();*/
			

}

