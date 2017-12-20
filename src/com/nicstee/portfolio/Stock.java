package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Stock implements Comparable<Stock>
{
	public static int sortBy = 0; // 0 -> perf ; 1 -> amount;

	public int id_stock;
	public int quantity;
	public String name;
	public String code;
	public double perf =999.;
	public double perf_since;
	public BigDecimal amount = BigDecimal.valueOf(0.);
	public BigDecimal amountPurchase = BigDecimal.valueOf(0.);
	public BigDecimal cost = BigDecimal.valueOf(0.) ;
	public Date date = Date.valueOf("2001-01-01");
	public BigDecimal quoteEur = BigDecimal.valueOf(0.);
	public BigDecimal quotePurchEur = BigDecimal.valueOf(0.);
	public String currency;
	public int since = 0; // en mois
	public String pays;
	public int sellType = 0; //type indéfini
	
	public Stock(Date date, int id_stock) throws SQLException
	{
		this.id_stock = id_stock;
	    String req = String.format(
	    	    "select code,description,pays,currency,(select quoteStockeur('%s',%s)) as quoteEur from stocks where id = %s",
	    	    date,id_stock,id_stock);
	    	  Statement stmt = Portfolio.conn.createStatement();
	    	  ResultSet rs = stmt.executeQuery(req);
	    	  if(! rs.next())System.exit(900);
	    	  this.name = rs.getString("description").trim();
	    	  this.code = rs.getString("code").trim();
	    	  this.quoteEur=rs.getBigDecimal("quoteEur");
	    	  this.currency=rs.getString("currency").trim();
	    	  this.date = date;
	    	  this.pays = rs.getString("pays").trim();
	    	  this.perf = 999.;
 	}
	
	public Stock getInvertedPoint() throws SQLException
	{
		return new Stock(date,id_stock);
	}

	@Override
	public int compareTo(Stock other) {
		    switch (Stock.sortBy) {
		    case 0: // Sort by perf décroissant
		    	if (this.perf < other.perf) return -1;
		    	if (this.perf > other.perf) return 1;
		    	return 0;
		    case 1: // Sort by amount
		    	return amount.compareTo(other.amount);
		    default: // Sort by perf croissant
		    	if (this.perf < other.perf) return -1;
		    	if (this.perf > other.perf) return 1;
		    	return 0;
	    }

	}


}