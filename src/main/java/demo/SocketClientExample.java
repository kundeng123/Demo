package demo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketClientExample {
	private static final Logger LOGGER = LoggerFactory.getLogger(SocketClientExample.class);
	private static int port = 9876;

	public static void main(String[] args)
			throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
		// get the localhost IP address, if server is running on some other IP, you need
		// to use that
		InetAddress host = InetAddress.getLocalHost();
		Socket socket = null;
		socket = new Socket(host, port);

//		ObjectOutputStream oos = null;
//		ObjectInputStream ois = null;


		DataInputStream din = new DataInputStream(socket.getInputStream());
		DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
		
		String str = "";
		String str2 = "";
		String temp = "";
		dout.writeUTF("subscriber");
		dout.flush();
//		
		
		while(!str2.equalsIgnoreCase("stop") ){
			Thread.sleep(500);
			
			
		//while (!str.equals("stop")) {
			
//			str = br.readLine();
			
			str2 = din.readUTF();
			
//			LOGGER.info("str2 {}", Arrays.asList(str2.split(",")));
//			LOGGER.info("temp {}",Arrays.asList(temp.split(",")));
			
//			LOGGER.debug("true or false {}", Arrays.asList(temp.split(",")).equals(Arrays.asList(str2.split(","))));
			if(!Arrays.asList(temp.split(",")).equals(Arrays.asList(str2.split(","))))
				LOGGER.info("Price changed,,,,{}",  Arrays.asList(str2.split(",")));
			if(str2.equalsIgnoreCase("stop")) {
				LOGGER.info("Closing socket....");
				socket.close();
				din.close();
				dout.close();
			}
			else {
			dout.writeUTF("subscriber!");
			dout.flush();
			temp = str2;
			}
			
			
			
		}
		
//		while (true) {
//			System.out.println("Sending request to Socket Server");
//			ois = new ObjectInputStream(socket.getInputStream());
//
//			String message = (String) ois.readObject();
//			System.out.println("Message: " + message);
//			oos = new ObjectOutputStream(socket.getOutputStream());
//			oos.writeObject("send from client");
//
//			if (message != null && message.equalsIgnoreCase("stop")) {
////                 System.out.println("Closing this connection : " + s); 
////                 s.close(); 
//				System.out.println("Connection closed");
//				break;
//			}
//		}

		socket.close();

	}
}

	
	