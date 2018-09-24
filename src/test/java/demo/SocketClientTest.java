package demo;


import java.io.IOException;
import java.net.ConnectException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SocketClientTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	
	@Test
	public void testConnectionException() throws ConnectException {
		String [] args = null;
		try {
			SocketClient.main(args);
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			e.printStackTrace();
		}
		

	}

}
