package demo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.h2.tools.Server;
import java.util.stream.*;


public class StockSimulator {

	private static final Logger LOGGER = LoggerFactory.getLogger(StockSimulator.class);
	private static ServerSocket socketServer;
	private static int port = 9876;

	static final float STOCK_A_MU = 0.08f;
	static final float STOCK_B_MU = 0.05f;
	static final float STOCK_A_STD = 0.4f;
	static final float STOCK_B_STD = 0.2f;
	static final float RISK_FREE_RATE = 0.02f;

	static double optionStrikeA = 102f;
	static double optionStrikeB = 45f;
	static double stockPriceA = 100;
	static double stockPriceB = 50;
	static double[] optionPricesA = null;
	static double[] optionPricesB = null;

	static int stockAShare = 0;
	static int stockBShare = 0;
	static int callA = 0;
	static int putA = 0;
	static int callB = 0;
	static int putB = 0;

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "org.h2.Driver";
	static final String DB_URL = "jdbc:h2:tcp://localhost/~/test";

	// Database credentials
	static final String USER = "sa";
	static final String PASS = "";

	// Constructor
	public StockSimulator(int stockID, double initialPrice) {

	}

	private double updateStockPrice(double s, double deltaT, double mu, double std) {
		Random random = new Random();
		return Math.max(0.000001,
				s + s * (mu * (deltaT / 7257600.00) + std * random.nextGaussian() * Math.sqrt(deltaT / 7257600.00)));
	}

	private double[] updateOptionPriceStock(double s, double k, int maturity, double std) {
		double d1 = (Math.log(s / k) + (RISK_FREE_RATE + std * std / 2) * maturity) / (std * Math.sqrt(maturity));
		double d2 = d1 - std * Math.sqrt(maturity);
		NormalDistribution normal = new NormalDistribution();

		return new double[] {
				s * normal.cumulativeProbability(d1)
						- k * Math.exp(-RISK_FREE_RATE * maturity) * normal.cumulativeProbability(d2),
				k * Math.exp(-RISK_FREE_RATE * maturity) * normal.cumulativeProbability(-d2)
						- s * normal.cumulativeProbability(-d1) };
	}

	public static double getNav(double[] info) {
		double sum = 0.0;
		for (double val : info) {
			sum += val;
		}
		return sum;
	}
	public static void loadProperties(Properties prop) {
		if (prop.getProperty("stock.a.share").length() == 0)
			stockAShare = 0;
		else
			stockAShare = Integer.parseInt(prop.getProperty("stock.a.share"));

		if (prop.getProperty("option.call.a.contract").length() == 0)
			callA = 0;
		else
			callA = Integer.parseInt(prop.getProperty("option.call.a.contract"));

		if (prop.getProperty("option.put.a.contract").length() == 0)
			putA = 0;
		else
			putA = Integer.parseInt(prop.getProperty("option.put.a.contract"));
		if (prop.getProperty("stock.b.share").length() == 0)
			stockBShare = 0;
		else
			stockBShare = Integer.parseInt(prop.getProperty("stock.b.share"));
		if (prop.getProperty("option.call.b.contract").length() == 0)
			callB = 0;
		else
			callB = Integer.parseInt(prop.getProperty("option.call.b.contract"));
		if (prop.getProperty("option.put.b.contract").length() == 0)
			putB = 0;
		else
			putB = Integer.parseInt(prop.getProperty("option.put.b.contract"));
	}

	public static void main(String[] args) {
		LOGGER.info("Starting stock simulator................");

		StockSimulator simulator = new StockSimulator(123, 100);
		Random random = new Random();

		Connection conn = null;
		Statement stmt = null;

		long start = System.currentTimeMillis();
		long end = start + 30 * 1000; // 60 sec * 1000 ms/sec

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = StockSimulator.class.getClassLoader().getResourceAsStream("trade.properties");

			// load a trade.properties file
			prop.load(input);
			loadProperties(prop);

			Class.forName(JDBC_DRIVER);
			Server server = Server.createTcpServer("-tcpAllowOthers");
			server.start();
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();

			// Setting up database tables
			String sql = "CREATE TABLE if not exists  PROFILE " + "(id varchar(20) not NULL, "
					+ " expected_return VARCHAR(255), " + " volatility VARCHAR(255), "
					+ " Strike varchar2(20) ," +" Maturity varchar2(10), " + " PRIMARY KEY ( id ))";
			stmt.executeUpdate(sql);
			LOGGER.info("Created PROFILE table in given database...");

			sql = "truncate table PROFILE";
			stmt.executeUpdate(sql);

			sql = "insert into PROFILE values('STOCKA', '" + STOCK_A_MU + "','" + STOCK_A_STD + "','','')";
			stmt.executeUpdate(sql);

			sql = "insert into PROFILE values('STOCKB', '" + STOCK_B_MU + "','" + STOCK_B_STD +"','','')";
			stmt.executeUpdate(sql);

			sql = "insert into PROFILE values('CALLA', '','','"+ optionStrikeA +"','1')";
			stmt.executeUpdate(sql);
			
			sql = "insert into PROFILE values('PUTA', '','','"+ optionStrikeA +"','1')";
			stmt.executeUpdate(sql);
			
			sql = "insert into PROFILE values('CALLB', '','','"+ optionStrikeB +"','1')";
			stmt.executeUpdate(sql);
			sql = "insert into PROFILE values('PUTB', '','','"+ optionStrikeB +"','1')";
			stmt.executeUpdate(sql);
			
			
			sql = "CREATE TABLE if not exists  STCK_A_VAL " + "(PRICE varchar(255) not NULL, "
					+ " CALL_VAL VARCHAR(255), " + " PUT_VAL VARCHAR(255))";
			stmt.executeUpdate(sql);
			sql = "truncate table STCK_A_VAL";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE if not exists  STCK_B_VAL " + "(PRICE varchar(255) not NULL, "
					+ " CALL_VAL VARCHAR(255), " + " PUT_VAL VARCHAR(255))";
			stmt.executeUpdate(sql);
			sql = "truncate table STCK_B_VAL";
			stmt.executeUpdate(sql);
			stmt.close();

			ExecutorService executorService = Executors.newFixedThreadPool(3);

			Callable<String> callable = () -> {
				Connection connRun = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmtRun = connRun.createStatement();
				while (System.currentTimeMillis() < end) {
					// start socket server

					// run
					long deltaT = random.nextInt(1501) + 500;
					LOGGER.debug("deltaT {}", deltaT);
					try {
						Thread.sleep(deltaT);
						stockPriceA = simulator.updateStockPrice(stockPriceA, deltaT, STOCK_A_MU, STOCK_A_STD);
						stockPriceB = simulator.updateStockPrice(stockPriceB, deltaT, STOCK_B_MU, STOCK_B_STD);

//						LOGGER.debug("random millisecond {}", deltaT);
//						LOGGER.debug("updatied prcie {}", stockPriceA);

						optionPricesA = simulator.updateOptionPriceStock(stockPriceA, optionStrikeA, 1, STOCK_A_STD);
//						LOGGER.debug("updatied option prices {}", optionPricesA);
						optionPricesB = simulator.updateOptionPriceStock(stockPriceB, optionStrikeB, 1, STOCK_B_STD);
//					LOGGER.debug("updatied option prices {}", optionPricesA);

//						LOGGER.debug("updatied prcie {}", stockPriceB);
//						LOGGER.debug("updatied option prices {}", optionPricesB);

						String insertSql = "insert into STCK_A_VAL values('" + stockPriceA + "', '" + optionPricesA[0]
								+ "','" + optionPricesA[1] + "')";
						stmtRun.executeUpdate(insertSql);

						insertSql = "insert into STCK_B_VAL values('" + stockPriceB + "', '" + optionPricesB[0] + "','"
								+ optionPricesB[1] + "')";
						stmtRun.executeUpdate(insertSql);

					} catch (InterruptedException e) {
						LOGGER.error(e.getMessage(), e);
					}

				}
				stmtRun.close();
				connRun.close();
				return "STOP";
			};

			Callable<String> startServer = () -> {
				socketServer = new ServerSocket(port);
				socketServer.setSoTimeout(30 * 1000); // 500 milliseconds
				LOGGER.info("Starting server....");

				Socket s = null;
				DataInputStream din = null;
				DataOutputStream dout = null;
				try {
					s = socketServer.accept();

					din = new DataInputStream(s.getInputStream());
					dout = new DataOutputStream(s.getOutputStream());

					String str = "";

					double[] currentData = { stockPriceA, optionPricesA[0], optionPricesA[1], stockPriceB,
							optionPricesB[0],

							optionPricesB[1] };

					double[] temp = new double[6];
					while (System.currentTimeMillis() < end) {

						str = din.readUTF();
						LOGGER.info("client says: " + str);

						currentData[0] = stockPriceA * stockAShare;

						currentData[1] = optionPricesA[0] * callA;

						currentData[2] = optionPricesA[1] * putA;

						currentData[3] = stockPriceB * stockBShare;

						currentData[4] = optionPricesB[0] * callB;

						currentData[5] = optionPricesB[1] * putB;

//						LOGGER.info(Arrays.toString(currentData)+","+getNav(currentData));
//						LOGGER.info("Current nav {}", getNav(currentData));
//						LOGGER.info("TEMP {}", temp);

//						LOGGER.info("true or false {}", currentData == temp);

						if (currentData != temp) {
							dout.writeUTF(Arrays.toString(currentData)+","+getNav(currentData));
							dout.flush();
						}
						for (int i = 0; i < currentData.length; i++) {

							temp[i] = currentData[i];

						}

					}
					dout.writeUTF("STOP");
					dout.flush();

					LOGGER.info("Closing sockets.....");
					if (s != null) {
//					 ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
//			         oos.writeObject("STOP");
						s.close();
					}
					din.close();
					dout.close();
					socketServer.close();
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					if (s != null) {

						s.close();
					}
					if (din != null)
						din.close();
					if (dout != null)
						dout.close();
					socketServer.close();
				}
				return "STOP";
			};
			Future<String> future = executorService.submit(callable);
			Thread.sleep(5 * 1000);
			socketServer = new ServerSocket(port);
			socketServer.setSoTimeout(30 * 1000); // 500 milliseconds
			LOGGER.info("Starting server....");
			while (System.currentTimeMillis() < end) {
				try {
				MultithreadServer multithreadServer = new MultithreadServer(socketServer.accept(),"Server",end );
				executorService.submit(multithreadServer);
				}
				catch (SocketTimeoutException e ) {
					LOGGER.warn(e.getMessage());
					break;
				}
				
			}
			
//			Future<String> futureServer = executorService.submit(startServer);

			LOGGER.info(future.get());
//			LOGGER.info(futureServer.get());
			socketServer.close();
			executorService.shutdown();

		} catch (ClassNotFoundException e1) {
			LOGGER.error(e1.getMessage(), e1);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);

		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (ExecutionException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (FileNotFoundException e1) {
			LOGGER.error(e1.getMessage(), e1);
		} catch (IOException e1) {
			LOGGER.error(e1.getMessage(), e1);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		}

		System.exit(0);

//		simulator.updateStockPrice(s, deltaT, STOCK_A_MU, STOCK_A_STD);

	}
}

