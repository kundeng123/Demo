package demo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import java.nio.file.Paths;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* The Printer program implements a Text printer 
* for portfolio info.
*
* @author  Kun Deng 
*/
public class Printer {
	private static final Logger LOGGER = LoggerFactory.getLogger(Printer.class);
	private static int port = 9876;


	public static void main(String[] args) throws IOException, ConnectException {

		InetAddress host = InetAddress.getLocalHost();
		Socket socket = null;
		
		String currentDir = Paths.get(".").toAbsolutePath().normalize().toString();
		FileWriter writer = new FileWriter(currentDir + "/" + "portfolio_report.txt", false);

		
		socket = new Socket(host, port);
		
		
		DataInputStream din = new DataInputStream(socket.getInputStream());
		DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
		
		String str2 = "";
		dout.writeUTF("printer");
		dout.flush();
		
		try {
			str2 = din.readUTF();
			LOGGER.info("current price...{}",  Arrays.asList(str2.split(",")));

			generateReport(Arrays.asList(str2.split(",")), writer);
			
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (socket != null) {

			socket.close();
		}
		din.close();
		dout.close();
		writer.close();
	}
	
	/**
	 * generate a text file and format content
	 *
	 * @param  currentData
	 * @param  writer
	 */
	private static void generateReport(List<String> currentData, FileWriter writer) throws IOException {
	    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
	    Date date = new Date();  
	    writer.write("Date: " + formatter.format(date) + "\n");
	    writer.write("*****************************\n");
	    writer.write("Current NAV: " + currentData.get(currentData.size()-1).replace("]", "") + "\n");
	    writer.write("*****************************\n");
	    writer.write("Detail:\n");
	    writer.write("----------------------------------\n");
	    writer.write(String.format("|%-10s\t|%-20s|\r\n", "Security", "Current Value ($)"));
	    writer.write("----------------------------------\n");
	    writer.write(String.format("|%-10s\t|%-20s|\r\n", "STOCKA", currentData.get(0).replace("[", "").trim()));
	    writer.write("----------------------------------\n");
	    writer.write(String.format("|%-10s\t|%-20s|\r\n", "CALLA", currentData.get(1).trim()));
	    writer.write("----------------------------------\n");
	    writer.write(String.format("|%-10s\t|%-20s|\r\n", "PUTA", currentData.get(2).trim()));
	    writer.write("----------------------------------\n");
	    writer.write(String.format("|%-10s\t|%-20s|\r\n", "STOCKB", currentData.get(3).trim()));
	    writer.write("----------------------------------\n");
	    writer.write(String.format("|%-10s\t|%-20s|\r\n", "CALLB", currentData.get(4).trim()));
	    writer.write("----------------------------------\n");
	    writer.write(String.format("|%-10s\t|%-20s|\r\n", "PUTB", currentData.get(5).trim()));
	    writer.write("----------------------------------\n");
	    writer.flush();

	}

}
