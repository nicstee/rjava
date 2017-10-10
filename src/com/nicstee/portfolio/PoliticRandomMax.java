package com.nicstee.portfolio;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class PoliticRandomMax extends PoliticBase implements Politic{

	public PoliticRandomMax(long seed, int maxStocks, int nbMonthToStartArbitration, int monthDayForArbitration) {
		super(seed,maxStocks, nbMonthToStartArbitration, monthDayForArbitration);
	}

	public double perfStockForSell(Date currentDay, int id_stock) throws SQLException {	
		Statement stmt = Portfolio.conn.createStatement();
		String req = String.format(
				"select coalesce(quoteStockEur(date '%s',%s)"
				+ "/quoteStockEur(date '%s' - 180,%s),0.) as performance",
				currentDay,id_stock,currentDay,id_stock);
//		System.out.println(req);
		ResultSet rs = stmt.executeQuery(req);
		if(!rs.next())return 0.;
		double perf = rs.getBigDecimal("performance").doubleValue();
//		System.out.println("id_stock " + id_stock + " perf " + perf);
		return 1./perf; // pour vente de performance max
	}

	public double perfStockForPurchase(Date currentDay, int id_stock) throws SQLException {
		double perf = rd.nextDouble();
//		System.out.println("id_stock " + id_stock + " perf " + perf);
		return perf;
	}

}
