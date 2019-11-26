
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
	/* initialize socket and input stream */
	private Socket socket = null;
	//private ServerSocket server = null;
	private DataInputStream inStream = null;
	private DataOutputStream outStream = null;
	private String publicKey = null;
	private String privateKey = null;
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
			String clientAddress = socket.getInetAddress().getHostAddress();
			String clientHostName = socket.getInetAddress().getHostName();
			System.out.println(clientHostName + "(" + clientAddress + ")" + " Client accepted!");
			outStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			inStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			byte[] key = new byte[16];
			/* reads message from client until End of file exception
			 * is encountered. This is actually how the DataInputStream.readUTF
			 * is told that there is no more data. */
			//while (true) {
			try
			{
				/* obtain AES (symmetric) key information from client */
				inStream.readFully(key, 0, 16);
				System.out.println("obtained key from client:" + key);
			}
			catch(EOFException eofe)
			{
				System.out.println("End Of File Exception, no more data sent in this stream\n" + eofe);
				//eofe.printStackTrace();
				//break;
			}
			catch(IOException ioe)
			{
				System.out.println("IO encountered while server running:\n" + ioe);
				ioe.printStackTrace();
			}
			if(key.length>0)
			{
				//try {
				// string to contain the encrypted text
				byte[] encryptedByteArray = null;
				//while (true) {
				try
				{
					int length = inStream.readInt();
					System.out.println("length sent from client:" + length);
					encryptedByteArray = new byte[length];
					inStream.read(encryptedByteArray, 0, length);
					System.out.println("length of encrypted array:" + encryptedByteArray.length);
				}
				catch(EOFException eofe) {
					System.out.println("End Of File Exception, no more data sent in this stream\n" + eofe);
					//eofe.printStackTrace();
					//break;
				}catch(IOException ioe) {
					System.out.println("IO encountered while server running:\n" + ioe);
					ioe.printStackTrace();
				}
				//}
				// decrypt the text
				if(encryptedByteArray.length>0)
				{
					ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedByteArray);
					int ivLength = byteBuffer.getInt();
					if(ivLength < 12 || ivLength >=16) {
						throw new IllegalArgumentException("invalid iv length");
					}
					byte[] iv = new byte[ivLength];
					byteBuffer.get(iv);
					byte[] cipherText = new byte[byteBuffer.remaining()];
					byteBuffer.get(cipherText);
					final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
					cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
					byte[] plainText = cipher.doFinal(cipherText);
					String decryptedString = new String(plainText);
					System.out.println("decrypted String:" + decryptedString);
				}
				else
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
		/*catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NoSuchPaddingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvalidKeyException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvalidAlgorithmParameterException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalBlockSizeException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (BadPaddingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
		/*try
		{
			server = new ServerSocket(port);
			System.out.println("Server started");
			System.out.println("waiting for a client to connect on port " + port + "...");
			
			socket = server.accept();*/
			
			/* make our streams which send and receive the data */
	public static void main(String[] args) throws IOException
	{
		ServerSocket Server = new ServerSocket(8090);
		System.out.println("waiting for connect");
		while(true)
		{
			Socket socket = Server.accept();
			new Server(socket).start();
		}
	}
}

