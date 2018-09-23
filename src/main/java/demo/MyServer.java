package demo;

import java.net.*;
import java.io.*;

class MyServer {
	public static void main(String args[]) throws Exception {
		ServerSocket ss = new ServerSocket(3333);
		
		int i = 0;
		try {
            while (i < 2) {
            	MultithreadServerTest server = new MultithreadServerTest(ss.accept(), "test");
                Thread thread = new Thread(server);
                thread.start();
                i++;
            }
        } finally {
        	ss.close();
        }
//		Socket s = ss.accept();
//		DataInputStream din = new DataInputStream(s.getInputStream());
//		DataOutputStream dout = new DataOutputStream(s.getOutputStream());
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//
//		String str = "", str2 = "";
//		while (!str.equals("stop")) {
//			str = din.readUTF();
//			System.out.println("client says: " + str);
////			str2 = br.readLine();
//			dout.writeUTF("Sever!");
//			dout.flush();
//		}
//		din.close();
//		s.close();
//		ss.close();
	}


}