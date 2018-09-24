package demo;

import org.junit.Ignore;
import org.junit.Test;
import demo.StockSimulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.math3.*;

public class StockSimulatorTest {

	// Test update stock price function using discrete time geometric brownian
	// motion
	
	@Test
	public void updateStockPriceTest() throws InterruptedException {
		StockSimulator simulator = new StockSimulator();
		double s = 10.0;

		double mu = 0.1;
		double std = 0.4;
		double deltaT = 1.0;
		List<Double> stockPriceA = new ArrayList<Double>();

		for (int i = 0; i < 1000; i++) {
			s = 10.0;
			for (int j = 0; j < 86400; j++) {
				s = simulator.updateStockPrice(s, deltaT, mu, std);

			}
			stockPriceA.add(s);

		}

		double sumOfStockPrices = stockPriceA.stream().mapToDouble(f -> f.doubleValue()).sum();

		System.out.println(
				"Mean value of generated stock prices at time t = 1 day: \n" + sumOfStockPrices / stockPriceA.size());

		double year = 1.0 / 365.0;

		double expectedValue = 10.0 * Math.exp(mu * year);
		System.out.println("Expected value of Stock if follows a geometric brownian motion : \n" + expectedValue);
		double diff = expectedValue - sumOfStockPrices / stockPriceA.size();

		System.out.println("diff: " + diff);
		assertTrue(Math.abs(diff) < 0.05);
	}

	// Test update option price function
	@Test
	public void updateOptionPriceStockTest() throws InterruptedException {
		StockSimulator simulator = new StockSimulator();
		double s = 10.0;
		double strike = 12;
		double std = 0.4;
		int maturity = 1;
		double[] optionValue = new double[2];

		optionValue = simulator.updateOptionPriceStock(s, strike, maturity, std);

		System.out.printf("option values call:%.5f put:%.5f\n", optionValue[0], optionValue[1]);

		assertEquals(0.982, optionValue[0], 0.04);
		assertEquals(2.74, optionValue[1], 0.04);

	}

	@Ignore
	@Test
	public void test() {
		StockSimulator.duration = 10 * 1000;
		String[] args = null;
		StockSimulator.main(args);

	}

}
