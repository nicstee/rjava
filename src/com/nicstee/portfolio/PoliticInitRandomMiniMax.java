package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class PoliticInitRandomMiniMax extends PoliticBase{

	Random rd;

	public void setSeed(long seed){
		rd = new Random();
		rd.setSeed(seed);
	}

		public double perfStockForSell(Date currentDay,int id_portfolio, Stock s) throws SQLException {
		double perf=s.amountPurchase.divide(s.amount,3,BigDecimal.ROUND_HALF_DOWN).doubleValue();
		perf=Math.pow(perf, (1./s.since));
		return perf; // attention selection sur le minimum 
	}

	public double perfStockForPurchase(Date currentDay, int id_portfolio, Stock s) {
		Statement stmt;
		double perf = 9999.;
		try {
			stmt = Portfolio.conn.createStatement();
			String req = String.format( // selection rapport prix actuel / prix 30 jrs  chute la + forte sur 30 jrs
					"select coalesce(quoteStockEur(date '%s',%s)" //
					+ "/quoteStockEur(date '%s' - %s,%s),1.) as performance",
					currentDay,s.id_stock,currentDay,perfPeriodForPurchase,s.id_stock);
			ResultSet rs = stmt.executeQuery(req);
			if(!rs.next())return 1.;
			perf = rs.getBigDecimal("performance").doubleValue();
		} catch (SQLException e) {
			return 999.; // pour empecher un achat
		}
		return perf; // attention selection sur le minimum 
	}

	@Override
	public double perfStockForPurchaseInit(Date currentDay, int portfolio, Stock s) throws SQLException {
		double perf = rd.nextDouble();
		return perf;
	}

	public void setPerfPeriodForPurchase(int perfPeriodForPurchase) {
		this.perfPeriodForPurchase = perfPeriodForPurchase;
	}

}
