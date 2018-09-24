package demo;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* The SocketClient program implements subscriber 
* to StockSimulator and output updated NAV
* during run time. 
* 
* @author  Kun Deng 
*/
public class SocketClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(SocketClient.class);
	private static int port = 9876;

	public static void main(String[] args)
			throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException,ConnectException {

		
		InetAddress host = InetAddress.getLocalHost();
		Socket socket = null;
		
		socket = new Socket(host, port);
		

		DataInputStream din = new DataInputStream(socket.getInputStream());
		DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
		

		String inMessage = "";
		String temp = "";
		dout.writeUTF("subscriber");
		dout.flush();
		
		
		while(!inMessage.equalsIgnoreCase("stop") ){

			
			inMessage = din.readUTF();

			if(!Arrays.asList(temp.split(",")).equals(Arrays.asList(inMessage.split(","))) && !inMessage.contains("STOP"))
				LOGGER.info("NAV updated.....{}",  Arrays.asList(inMessage.split(",")));
			
			
			if(inMessage.equalsIgnoreCase("stop")) {
				LOGGER.info("Closing socket....");
				socket.close();
				din.close();
				dout.close();
				break;
			}
			else {
			dout.writeUTF("subscriber");
			dout.flush();
			temp = inMessage;
			}

		}
		

		socket.close();

	}
}

	
	