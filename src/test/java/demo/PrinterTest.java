package demo;



import java.io.IOException;
import java.net.ConnectException;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class PrinterTest {

	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	
	@Test
	public void testConnectionException() throws ConnectException {
		String [] args = null;
		try {
			Printer.main(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}



}
