package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Stock implements Comparable<Stock>
{
	public int id_stock;
	public int quantity;
	public String name;
	public String code;
	public BigDecimal quote;
	public double perf;
	public BigDecimal amount;
	public BigDecimal cost;
	
	public Stock(int id_stock, int quantity, double perf) throws SQLException
	{
		this.id_stock = id_stock;
		this.quantity = quantity;
		this.perf = perf;
	    String req = String.format(
	    	    "select code,description from stocks where id = %s",id_stock);
	    	//  System.out.println(req);
	    	  Statement stmt = Portfolio.conn.createStatement();
	    	  ResultSet rs = stmt.executeQuery(req);
	    	  if(! rs.next()){
	    		  name = "???";
	    		  this.code="???";
	    	  }
	    	  this.name = rs.getString("description");
	    	  this.code = rs.getString("code");
	}
	
	public Stock getInvertedPoint() throws SQLException
	{
		return new Stock(id_stock, quantity, perf);
	}

	@Override
	public int compareTo(Stock other) {
		if (this.perf < other.perf) return -1;
		if (this.perf > other.perf) return 1;
		return 0;
	}


}