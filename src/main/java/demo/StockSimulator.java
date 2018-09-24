package demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.ServerSocket;

import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

/**
* The StockSimulator program implements an simulator 
* for stocks, publish relevant info to client and update
* H2 database during run time.
*
* @author  Kun Deng 
*/
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
	
	//Timeout of process
	static long duration = 60 * 1000; // 60 sec * 1000 ms/sec;

	List<String> temp = new ArrayList<String>();
	/**
	 * Returns a updated stock price
	 *
	 * @param  s
	 * @param  deltaT
	 * @param  mu
	 * @param  std 
	 * @return the updated stock price s
	 */
	protected double updateStockPrice(double s, double deltaT, double mu, double std) {
		Random random = new Random();
		return Math.max(0.000001,
				s + s * (mu * (deltaT / 7257600.00) + std * random.nextGaussian() * Math.sqrt(deltaT / 7257600.00)));
	}

	/**
	 * Returns a updated option value
	 *
	 * @param  s
	 * @param  k
	 * @param  maturity
	 * @param  std 
	 * @return the updated option value
	 */
	protected double[] updateOptionPriceStock(double s, double k, int maturity, double std) {
		double d1 = (Math.log(s / k) + (RISK_FREE_RATE + std * std / 2) * maturity) / (std * Math.sqrt(maturity));
		double d2 = d1 - std * Math.sqrt(maturity);
		NormalDistribution normal = new NormalDistribution();

		return new double[] {
				s * normal.cumulativeProbability(d1)
						- k * Math.exp(-RISK_FREE_RATE * maturity) * normal.cumulativeProbability(d2),
				k * Math.exp(-RISK_FREE_RATE * maturity) * normal.cumulativeProbability(-d2)
						- s * normal.cumulativeProbability(-d1) };
	}

	/**
	 * Returns a NAV value 
	 *
	 * @param  info Current stock and option values
	 * @return NAV value
	 */
	protected static double getNav(double[] info) {
		double sum = 0.0;
		for (double val : info) {
			sum += val;
		}
		return sum;
	}

	/**
	 * Load properties 
	 *
	 * @param  prop 
	 */
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

		StockSimulator simulator = new StockSimulator();
		Random random = new Random();

		Connection conn = null;
		Statement stmt = null;

		long start = System.currentTimeMillis();
		long end = start + duration; 

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
					+ " expected_return VARCHAR(255), " + " volatility VARCHAR(255), " + " Strike varchar2(20) ,"
					+ " Maturity varchar2(10), " + " PRIMARY KEY ( id ))";
			stmt.executeUpdate(sql);
			LOGGER.info("Created PROFILE table in given database...");

			sql = "truncate table PROFILE";
			stmt.executeUpdate(sql);

			sql = "insert into PROFILE values('STOCKA', '" + STOCK_A_MU + "','" + STOCK_A_STD + "','','')";
			stmt.executeUpdate(sql);

			sql = "insert into PROFILE values('STOCKB', '" + STOCK_B_MU + "','" + STOCK_B_STD + "','','')";
			stmt.executeUpdate(sql);

			sql = "insert into PROFILE values('CALLA', '','','" + optionStrikeA + "','1')";
			stmt.executeUpdate(sql);

			sql = "insert into PROFILE values('PUTA', '','','" + optionStrikeA + "','1')";
			stmt.executeUpdate(sql);

			sql = "insert into PROFILE values('CALLB', '','','" + optionStrikeB + "','1')";
			stmt.executeUpdate(sql);
			sql = "insert into PROFILE values('PUTB', '','','" + optionStrikeB + "','1')";
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
//					LOGGER.debug("deltaT {}", deltaT);
					try {
						Thread.sleep(deltaT);
						stockPriceA = simulator.updateStockPrice(stockPriceA, deltaT, STOCK_A_MU, STOCK_A_STD);
						stockPriceB = simulator.updateStockPrice(stockPriceB, deltaT, STOCK_B_MU, STOCK_B_STD);

						optionPricesA = simulator.updateOptionPriceStock(stockPriceA, optionStrikeA, 1, STOCK_A_STD);
						optionPricesB = simulator.updateOptionPriceStock(stockPriceB, optionStrikeB, 1, STOCK_B_STD);

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

			Future<String> future = executorService.submit(callable);
			Thread.sleep(2 * 1000);
			socketServer = new ServerSocket(port);
			socketServer.setSoTimeout((int) (end - System.currentTimeMillis())); 
			LOGGER.info("Starting server....");
			while (System.currentTimeMillis() < end) {
				try {
					MultithreadServer multithreadServer = new MultithreadServer(socketServer.accept(), "Server", end);
					executorService.submit(multithreadServer);
				} catch (SocketTimeoutException e) {
					LOGGER.warn(e.getMessage());
					break;
				}

			}
			future.get();
//			LOGGER.info(future.get());
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
				LOGGER.error(se2.getMessage(), se2);
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				LOGGER.error(se.getMessage(), se);
			}
		}

		System.exit(0);

	}
}
