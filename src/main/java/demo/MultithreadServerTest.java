package demo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MultithreadServerTest implements Runnable {
	protected Socket s = null;
	protected String str = null;

	public MultithreadServerTest(Socket clientSocket, String serverText) {
		this.s = clientSocket;
		this.str = serverText;
	}

	public void run() {
		try {
			DataInputStream din = new DataInputStream(s.getInputStream());
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			long time = System.currentTimeMillis();
			String str = "", str2 = "";
			while (!str.equals("stop")) {
				System.out.println("str in server is : " + str);
				if (str2.equalsIgnoreCase("stop")) {
					s.close();
					break;
				} else {
					str = din.readUTF();
					System.out.println("client says: " + str);

//				str2 = br.readLine();
					dout.writeUTF("Sever!");
					dout.flush();
				}
			}
		} catch (IOException e) {
			// report exception somewhere.
			e.printStackTrace();
		}
	}
}
