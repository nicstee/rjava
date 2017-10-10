package com.nicstee.portfolio;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Random;

public class PoliticRandom extends PoliticBase implements Politic{

	public PoliticRandom(long seed, int maxStocks, int nbMonthToStartArbitration, int monthDayForArbitration) {
		super(seed, maxStocks, nbMonthToStartArbitration, monthDayForArbitration);
	}

	public double perfStockForSell(Date currentDay, int id_stock) throws SQLException {	
		double perf = rd.nextDouble();
//		System.out.println("id_stock " + id_stock + " perf " + perf);
		return perf;
	}

	public double perfStockForPurchase(Date currentDay, int id_stock) throws SQLException {
		double perf = rd.nextDouble();
//		System.out.println("id_stock " + id_stock + " perf " + perf);
		return perf;
	}

}
