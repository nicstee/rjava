package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;


public class Portfolio {
	
	static int OP_INVESTMENT = 0;
	static int OP_STOCK_IN = 1;
	static int OP_STOCK_OUT = 4;
	static int OP_COST = 2;
	static int OP_DIVIDENDS = 3;
	static int OP_TAX_DIVIDENDS = 5;
	
	static Connection conn;
	
	int id_portfolio = 164;
	Date dCreation;
	Politic politic;
	
	@SuppressWarnings("deprecation")
	void calculate (String name,BigDecimal cash, Date creation) throws SQLException{
        //
		dCreation = creation;
		dCreation.setDate(1); // begin of month
		id_portfolio = portfolioCreation(name);
//	    System.out.println("id = " + id_portfolio);
//
		politic.setPortfolio(this);
//
	    politic.init(cash,dCreation); // stocks, first loading
		stocksGeneration(dCreation);    
	}
	
	public static int portfolioCreation(String name) throws SQLException{
        String dbURL = "jdbc:postgresql:Portfolio?user=postgres&password=GLOZQCKI";
        if(conn == null) conn = DriverManager.getConnection(dbURL);
        if (conn != null) {
            System.out.println("Connected to database Portefeuille");
        }
        String req = String.format("SELECT count(*) FROM portfolios where name = '%s'",name);
//        System.out.println(req);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(req);
        rs.next();
        if(rs.getInt(1) > 0){;
//        	System.out.println("portfolio exits !!!");
            req = String.format("delete FROM portfolios where name = '%s'",name);
//	        System.out.println(req);
            stmt.executeUpdate(req);
        }
        req = String.format("INSERT INTO portfolios (name) VALUES ('%s')",name);
//	    System.out.println(req);
	    stmt.executeUpdate(req);
        req = String.format("SELECT id FROM portfolios where name = '%s'",name);
 //       System.out.println(req);
        rs = stmt.executeQuery(req);
        rs.next();
        return rs.getInt(1);
//	    System.out.println("id = " + id_portfolio);
		
	}
	
	static BigDecimal quote(Date date, int id_stock) throws SQLException {
        String req = String.format("select close from quotes where id_stock = %s and close > 0 and date < '%s' order by date desc limit 1",id_stock,date);
//        System.out.println(req);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(req);
        rs.next();
		return rs.getBigDecimal("close");	
	}
	
	static BigDecimal quantity(Date date, int id_stock) throws SQLException {
        String req = String.format(
        		"select close from quotes where id_stock = %s and date < '%s' order by date desc limit 1",id_stock,date);
 //       System.out.println(req);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(req);
        rs.next();
		return rs.getBigDecimal("close");	
	}
	static Date dateAfter(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);  // number of days to add
        return new java.sql.Date(c.getTimeInMillis());
	}
	
	static Date dateNextMonth(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, 1);  // number of days to add
        return new java.sql.Date(c.getTimeInMillis());
	}

	static Date last_quote() throws SQLException{
		Date last;
        String req = "SELECT date FROM quotes order by date desc limit 1";
//        System.out.println(req);
        Statement stmt;
        stmt = Portfolio.conn.createStatement();
        ResultSet rs = stmt.executeQuery(req);
        rs.next();
        last = rs.getDate(1);
		return last;
	}
	
	public void stocksPurchase(int id_stock,Date date,int quantity) throws SQLException{
    	BigDecimal cotation = Portfolio.quote(date, id_stock);
    	BigDecimal mouvement = cotation.multiply(new BigDecimal(-quantity));
    	BigDecimal cost = Portfolio.extraCostPurchase(id_stock,quantity,cotation).multiply(new BigDecimal(-1.));
//   	System.out.println("id_stock = " + id_stock + " quote = " + quote + " quantity = " + quantity);
        String req = String.format("INSERT INTO movements (id_stock,id_portfolio,quantity,quote,date,amount,type,comment) "
    	+ "VALUES ('%s','%s','%s','%s','%s',%s,%s,'%s')",
    	id_stock,id_portfolio,quantity,cotation,date,mouvement,Portfolio.OP_STOCK_IN,"stocks purchase cost");
//	    System.out.println(req);
	    Statement stm = Portfolio.conn.createStatement();
	    stm.executeUpdate(req);
        req = String.format("INSERT INTO movements (date,amount,id_portfolio,type,comment,id_stock) VALUES ('%s',%s,%s,%s,'%s',%s)",
        		date,cost,id_portfolio,Portfolio.OP_COST,"stocks purchase extracost",id_stock);
//	    System.out.println(req);
	    stm.executeUpdate(req);	
	}

	public void movement(Date date,BigDecimal amount,String comment) throws SQLException{
	    String req = String.format("INSERT INTO movements (date,amount,id_portfolio,type,comment) VALUES ('%s',%s,%s,%s,'%s')",
        		date,amount,id_portfolio,Portfolio.OP_INVESTMENT,comment);
//	    System.out.println(req);
	    Statement stmt = Portfolio.conn.createStatement();
	    stmt.executeUpdate(req);	    
	}
	
  public static BigDecimal extraCostPurchase(int id_stock,int quantite,BigDecimal cotation) {
			BigDecimal cost = cotation.multiply(new BigDecimal(quantite)).divide(new BigDecimal(1/.002),2,BigDecimal.ROUND_UP);
	//		System.out.println("Cost = " + cost);
			return cost;
		}
  
  public static BigDecimal taxesDividends(int id_stock,BigDecimal amont) {
		return amont.multiply(new BigDecimal(-0.3));
	}
	public void stocksGeneration(Date startingDay) throws SQLException {
		Date from = dateAfter(startingDay);
		Date nextArbitrationDate = Portfolio.dateNextMonth(startingDay);
		for(int i =0;i<politic.getArbitrationDay();i++)
				nextArbitrationDate=Portfolio.dateAfter(nextArbitrationDate);
//		System.out.println(from + " " + nextArbitrationDate + " " + nextArbitrationDate.after(Calendar.getInstance().getTime()));
		while(! nextArbitrationDate.after(Calendar.getInstance().getTime())){
			Date currentDate = from;
			while(! currentDate.after(nextArbitrationDate)){
//				System.out.println("current date = "+nextDay);
				ResultSet rs = getActiveStocks(currentDate);
				while (rs.next()) {
					DividendsLoading(currentDate,rs.getInt("id_stock"));
				}
				currentDate=dateAfter(currentDate);
			}
			from = dateAfter(nextArbitrationDate) ;
			nextArbitrationDate = Portfolio.dateNextMonth(nextArbitrationDate);
		}
	}

private ResultSet getActiveStocks(Date nextDay) throws SQLException {
		Statement stmt = conn.createStatement();
		String req = String.format("select distinct id_stock from movements" +
				" where id_portfolio = %s and type in (%s,%s) and date < '%s'"+
				"group by id_stock having sum(quantity) > 0",
				id_portfolio,OP_STOCK_IN,OP_STOCK_OUT, nextDay);
//		System.out.println(req);
		return stmt.executeQuery(req);
	}

public void DividendsLoading(Date currentDate,int id_stock) throws SQLException {
	Statement stmt = conn.createStatement();
    String req = String.format("select dividends from dividends where id_stock=%s and date = '%s'",id_stock,currentDate);
//    System.out.println(req);
    ResultSet rs = stmt.executeQuery(req);
    while (rs.next()) {
	    	BigDecimal dividends = rs.getBigDecimal("dividends");
//	    	System.out.println(currentDate + " dividendes " + dividends);
	    	BigDecimal amount = dividends.multiply(new BigDecimal(quantityStocks(currentDate,id_stock)));
		    req = String.format("INSERT INTO movements (date,amount,id_portfolio,id_stock,type,comment) VALUES ('%s',%s,%s,%s,%s,'%s')",
	        		currentDate,amount,id_portfolio,id_stock,Portfolio.OP_DIVIDENDS,"Stocks dividends");
	//	    System.out.println(req);
		    Statement stmtn = Portfolio.conn.createStatement();
		    stmtn.executeUpdate(req);
		    BigDecimal taxes = taxesDividends(id_stock, amount);
		    if( taxes.compareTo(new BigDecimal(0.)) == 0)return;
		    req = String.format("INSERT INTO movements (date,amount,id_portfolio,id_stock,type,comment) VALUES ('%s',%s,%s,%s,%s,'%s')",
	        		currentDate,taxes,id_portfolio,id_stock,Portfolio.OP_TAX_DIVIDENDS,"Stocks taxes on dividends");
	//	    System.out.println(req);
		    stmtn = Portfolio.conn.createStatement();
		    stmtn.executeUpdate(req);	
		    
    }
}

private int quantityStocks(Date currentDate, int id_stock) throws SQLException {
	Statement stmt = conn.createStatement();
	String req = String.format("select sum(quantity) as quantity from movements "+""
			+ "where id_portfolio = %s and id_stock = %s and type IN (%s,%s) and date < '%s'",
			id_portfolio, id_stock,OP_STOCK_IN,OP_STOCK_OUT,currentDate);
//	System.out.println(req);
    ResultSet rs = stmt.executeQuery(req);
    rs.next();
    return rs.getInt("quantity");

}

public static void close() throws SQLException {
	  Portfolio.conn.close();
  }

public void setPolitic(Politic politic) {
	this.politic = politic;
}

public static void setConn() throws SQLException {
    String dbURL = "jdbc:postgresql:Portfolio?user=postgres&password=GLOZQCKI";
    if(conn == null) conn = DriverManager.getConnection(dbURL);
    if (conn != null) {
        System.out.println("Connected to database Portefeuille");
    }
;
}

}