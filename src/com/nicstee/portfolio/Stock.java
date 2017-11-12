package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Stock implements Comparable<Stock>
{
	public int id_stock;
	public int quantity;
	public String name;
	public String code;
	public double perf;
	public BigDecimal amount;
	public BigDecimal amountPurchase;
	public BigDecimal cost;
	public Date date;
	public BigDecimal quoteEur;
	public BigDecimal quotePurchEur;
	public String currency;
	public int since = 0; // en mois
	public int sortBy = 0; // 0 -> perf ; 1 -> amount;
	public String pays;
	
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
 	}
	
	public Stock getInvertedPoint() throws SQLException
	{
		return new Stock(date,id_stock);
	}

	@Override
	public int compareTo(Stock other) {
		    switch (sortBy) {
		    case 0: // Sort by perf
		    	if (this.perf < other.perf) return -1;
		    	if (this.perf > other.perf) return 1;
		    	return 0;
		    default: // Sort by amount
		    	return amount.compareTo(other.amount);
	    }

	}


}