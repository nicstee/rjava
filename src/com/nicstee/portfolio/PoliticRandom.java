package com.nicstee.portfolio;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Random;

public class PoliticRandom extends PoliticBase implements Politic{
	
	Random rd;

	//	public PoliticRandom(long seed, int maxStocks, int nbMonthToStartArbitration, int monthDayForArbitration) {
	//		super(seed, maxStocks, nbMonthToStartArbitration, monthDayForArbitration);
	//	}
	
	public void setSeed(long seed){
		rd = new Random();
		rd.setSeed(seed);
	}

	public double perfStockForSell(Date currentDay, int portfolio,Stock s) throws SQLException {	
		double perf = rd.nextDouble();
		//		System.out.println("id_stock " + id_stock + " perf " + perf);
		return perf;
	}

	public double perfStockForPurchase(Date currentDay, int portfolio, Stock s) throws SQLException {
		double perf = rd.nextDouble();
		//		System.out.println("id_stock " + id_stock + " perf " + perf);
		return perf;
	}

}
