package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PoliticMaxFall extends PoliticBase{

	public PoliticMaxFall(int maxStocks, int nbMonthToStartArbitration, int monthDayForArbitration) {
		super(maxStocks, nbMonthToStartArbitration, monthDayForArbitration);
		// TODO Auto-generated constructor stub
	}
	double perfStockForSell(Date currentDay, int id_stock) throws SQLException {
		Statement stmt = Portfolio.conn.createStatement();
		String req = String.format(
				"select coalesce(-quoteStockEur(date '%s' - 180,%s)"
				+ "/quoteStockEur(date '%s',%s),0.) as performance",
				currentDay,id_stock,currentDay,id_stock);
//		System.out.println(req);
		ResultSet rs = stmt.executeQuery(req);
		if(!rs.next())return 0.;
		return rs.getBigDecimal("performance").doubleValue();	
	}

	double perfStockForPurchase(Date currentDay, int id_stock) throws SQLException {
		Statement stmt = Portfolio.conn.createStatement();
		String req = String.format(
				"select coalesce(quoteStockEur(date '%s' - 180,%s)"
				+ "/quoteStockEur(date '%s',%s),0.) as performance",
				currentDay,id_stock,currentDay,id_stock);
//		System.out.println(req);
		ResultSet rs = stmt.executeQuery(req);
		if(!rs.next())return 0.;
		return rs.getBigDecimal("performance").doubleValue();	
	}

}
