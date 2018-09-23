package demo;

import java.net.*;
import java.io.*;

class Client2 {
	public static void main(String args[]) throws Exception {
		Socket s = new Socket("localhost", 3333);
		DataInputStream din = new DataInputStream(s.getInputStream());
		DataOutputStream dout = new DataOutputStream(s.getOutputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String str = "clien2", str2 = "";
		for (int i = 0; i <5; i++) {
			Thread.sleep(3000);
		//while (!str.equals("stop")) {
			if(i == 4) 
				str = "stop";
//			str = br.readLine();
			dout.writeUTF(str);
			dout.flush();
			str2 = din.readUTF();
			System.out.println("Clinet2: Server says: " + str2);
			
			
		}

		dout.close();
		s.close();
	}
}