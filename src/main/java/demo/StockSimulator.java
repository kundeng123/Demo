package demo;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.math3.distribution.NormalDistribution;

public class StockSimulator extends TimerTask{

	private static final Logger LOGGER = LoggerFactory.getLogger(StockSimulator.class);

	private double stockPriceA;
	private double stockPriceB;

	static final float STOCK_A_MU = 0.09f;
	static final float STOCK_B_MU = 0.05f;
	static final float STOCK_A_STD = 0.4f;
	static final float STOCK_B_STD = 0.2f;
	static final float RISK_FREE_RATE = 0.02f;

	// Constructor
	public StockSimulator(int stockID, double initialPrice) {

	}

	public double updateStockPrice(double s, double deltaT, double mu, double std) {
		Random random = new Random();		
		return s + s *( mu * (deltaT / 7257600.00) + std * random.nextGaussian() * Math.sqrt(deltaT / 7257600.00));
	}
	
	public double updateCallOptionPrice(double s, double k, int maturity, double std) {
		double d1 = Math.log(s/k) + (RISK_FREE_RATE + std*std / 2) * maturity;
		double d2 = d1 - std * Math.sqrt(maturity);
		NormalDistribution normal = new NormalDistribution();
		return s * normal.cumulativeProbability(d1) 
				- k * Math.exp(-RISK_FREE_RATE * maturity) * normal.cumulativeProbability(d2);
	}

	
	public static void main(String[] args) {
		LOGGER.info("Starting stock simulator................");
		
		StockSimulator simulator = new StockSimulator(123, 100);
		Random random = new Random();
		
		double s = 100;
		
		Runnable helloRunnable = new Runnable() {
		    public void run() {
		        System.out.println("Updating price");
				LOGGER.info("random seconds {}", random.nextDouble() * (1.5) + 0.5);

//		        s = simulator.updateStockPrice(s, Math.ran, STOCK_A_MU, STOCK_A_STD)
		    }
		};

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		try {
			Thread.sleep((long)(random.nextDouble() * (1.5) + 0.5) * 1000);
			executor.execute(helloRunnable);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		class Helper extends TimerTask 
		{ 
		    public void run() 
		    { 
		        System.out.println("Timer ran"); 
		        
		    } 
		      
		} 
		
		TimerTask timerTask = new Helper();
        //running timer task as daemon thread
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 10*1000);
        System.out.println("TimerTask started");
        //cancel after sometime
        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.cancel();
        System.out.println("TimerTask cancelled");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        
//		simulator.updateStockPrice(s, deltaT, STOCK_A_MU, STOCK_A_STD);
		
	
	}
}
