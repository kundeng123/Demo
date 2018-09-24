package demo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultithreadServer implements Runnable {
	protected Socket s = null;
	protected String clientType = "";
	long end;
	private static final Logger LOGGER = LoggerFactory.getLogger(MultithreadServer.class);

	public MultithreadServer(Socket clientSocket, String clientType, long endTime) {
		this.s = clientSocket;
		this.clientType = clientType;
		this.end = endTime;
	}

	public void run() {
		try {
			DataInputStream din = null;
			DataOutputStream dout = null;

			din = new DataInputStream(s.getInputStream());
			dout = new DataOutputStream(s.getOutputStream());
			String str = "";

			double[] currentData = new double[7];
			double[] temp = new double[7];
			str = din.readUTF();
			if (str.equalsIgnoreCase("printer")) {


				currentData[0] = StockSimulator.stockPriceA * StockSimulator.stockAShare;

				currentData[1] = StockSimulator.optionPricesA[0] * StockSimulator.callA;

				currentData[2] = StockSimulator.optionPricesA[1] * StockSimulator.putA;

				currentData[3] = StockSimulator.stockPriceB * StockSimulator.stockBShare;

				currentData[4] = StockSimulator.optionPricesB[0] * StockSimulator.callB;

				currentData[5] = StockSimulator.optionPricesB[1] * StockSimulator.putB;

				currentData[6] = StockSimulator.getNav( Arrays.copyOfRange(currentData,0,5));

				dout.writeUTF(Arrays.toString(currentData));

			} else if (str.equalsIgnoreCase("subscriber")) {
				while (System.currentTimeMillis() < end) {
					
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						LOGGER.error(e.getMessage(),e);
					}

//					LOGGER.info("client says: " + str);

					currentData[0] = StockSimulator.stockPriceA * StockSimulator.stockAShare;

					currentData[1] = StockSimulator.optionPricesA[0] * StockSimulator.callA;

					currentData[2] = StockSimulator.optionPricesA[1] * StockSimulator.putA;

					currentData[3] = StockSimulator.stockPriceB * StockSimulator.stockBShare;

					currentData[4] = StockSimulator.optionPricesB[0] * StockSimulator.callB;

					currentData[5] = StockSimulator.optionPricesB[1] * StockSimulator.putB;

					currentData[6] = StockSimulator.getNav( Arrays.copyOfRange(currentData,0,5));

					

					if (currentData != temp) {
						dout.writeUTF(Arrays.toString(currentData));
						dout.flush();
					}
					for (int i = 0; i < currentData.length; i++) {

						temp[i] = currentData[i];

					}

				}
				LOGGER.info("Time out for the server...");
				dout.writeUTF("STOP");
				dout.flush();
			}

			LOGGER.info("Closing sockets.....");
			
				
			
			if (s != null) {

				s.close();
			}
			din.close();
			dout.close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
		}
	}
}
