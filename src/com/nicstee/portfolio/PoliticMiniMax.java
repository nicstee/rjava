package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PoliticMiniMax extends PoliticBase{

	public double perfStockForSell(Date currentDay,int id_portfolio, Stock s) throws SQLException {
		double perf=s.amount.divide(s.amountPurchase,3,BigDecimal.ROUND_HALF_DOWN).doubleValue();
		if( s.since > 0)perf=Math.pow(perf, (1./s.since));
		return perf; // attention selection sur le minimum 
	}

	public double perfStockForPurchase(Date currentDay, int id_portfolio, Stock s) {
		Statement stmt;
		double perf = 9999.;
		try {
			stmt = Portfolio.conn.createStatement();
			String req = String.format( // selection rapport prix actuel / prix 30 jrs  chute la + forte sur 30 jrs
			"select %s/quoteStockEur(date '%s' - %s,%s) as performance",
			s.quoteEur,currentDay,perfPeriodForPurchase,s.id_stock);
//					System.out.println(req);
			ResultSet rs = stmt.executeQuery(req);
			if(!rs.next())return 1.;
			perf = rs.getBigDecimal("performance").doubleValue();
		} catch (SQLException e) {
			return 999.; // pour empecher un achat
		}
//		System.out.println("id_stock " + s.id_stock + " perf achat " + perf);
		return perf; // attention selection sur le minimum 
	}

	@Override
	public double perfStockForPurchaseInit(Date currentDay, int portfolio, Stock s) throws SQLException {
		return perfStockForPurchase(currentDay,portfolio,s);
	}

	public void setPerfPeriodForPurchase(int perfPeriodForPurchase) {
		this.perfPeriodForPurchase = perfPeriodForPurchase;
	}

}
