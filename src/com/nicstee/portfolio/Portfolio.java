package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Vector;


public class Portfolio {

	static int OP_INVESTMENT = 0;
	static int OP_STOCK_IN = 1;
	static int OP_STOCK_OUT = 2;
	static int OP_DIVIDENDS = 3;
	static int OP_COST = 4;
	static int OP_TAX_DIVIDENDS = 5;

	static Connection conn;

	int id_portfolio;
	Date dCreation;
	double commission;
	Politic politic;
	String name ;


	public Portfolio(String name, Politic politic, String creation, double cash, double commissionPourcent) throws SQLException {
		this.politic=politic;
		this.politic.setPortfolio(this);
		this.commission=commissionPourcent/100.;
		this.name=name;
		generationPortfolio(name,new BigDecimal(cash),Date.valueOf(creation));
	}

	@SuppressWarnings("deprecation")
	void generationPortfolio (String name,BigDecimal cash, Date creation) throws SQLException{
		//
		dCreation = creation;
		dCreation.setDate(1); // begin of month
		id_portfolio = creationPortfolio(name);
		insertMovement(creation, cash,OP_INVESTMENT, "Initial investment");
		politic.initPortfolio(cash,dCreation); // stocks, first loading
		generationHistoricMovements(dCreation); 
		generationHistoricDetails(dCreation);
	}

	int creationPortfolio(String name) throws SQLException{
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

	static BigDecimal getQuote(Date date, int id_stock) throws SQLException {
		String req = String.format("select close from quotes where id_stock = %s and close > 0 and date < '%s' order by date desc limit 1",id_stock,date);
		//        System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(!rs.next())return new BigDecimal(0.);
		return rs.getBigDecimal("close");	
	}

	static Date getDateAfter(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);  // number of days to add
		return new java.sql.Date(c.getTimeInMillis());
	}

	static Date getDateBefore(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, -1);  // number of days to add
		return new java.sql.Date(c.getTimeInMillis());
	}

	public void stocksPurchase(int id_stock,Date date,Stock sx) throws SQLException{
		if(sx.quantity == 0)return;
		BigDecimal currentCash = getCash(date);
		sx.quote = Portfolio.getQuote(date, id_stock);
		sx.amount = sx.quote.multiply(new BigDecimal(-sx.quantity));
		//       	System.out.println("id_stock = " + id_stock + " quote = " + quote + " quantity = " + quantity + " " +amount + " "+currentCash);
		if(currentCash.add(sx.amount).compareTo(new BigDecimal(0.)) <=  0){		
			sx.quantity=currentCash.divide(sx.quote,0,BigDecimal.ROUND_DOWN).intValue();
			sx.amount = sx.quote.multiply(new BigDecimal(-sx.quantity));
			//    		System.out.println("Achat changé : date " + date + " id_stock "+ id_stock + " new quantity "  + quantity);
			if(sx.quantity <= 0 )return;
		}
		sx.cost = sx.quote.multiply(new BigDecimal(sx.quantity)).divide(new BigDecimal(1/commission),2,BigDecimal.ROUND_UP).multiply(new BigDecimal(-1.));
		String req = String.format("INSERT INTO movements (id_stock,id_portfolio,quantity,quote,date,amount,type,comment) "
				+ "VALUES (%s,%s,%s,%s,'%s',%s,%s,'%s')",
				id_stock,id_portfolio,sx.quantity,sx.quote,date,sx.amount,Portfolio.OP_STOCK_IN,"stocks purchase cost");
		//	    System.out.println(req);
		Statement stm = Portfolio.conn.createStatement();
		stm.executeUpdate(req);	
		req = String.format("INSERT INTO movements (date,amount,id_portfolio,type,comment,id_stock) VALUES ('%s',%s,%s,%s,'%s',%s)",
				date,sx.cost,id_portfolio,Portfolio.OP_COST,"stocks purchase extracost",id_stock);
		//	    System.out.println(req);
		stm.executeUpdate(req);
	}


	public void stocksSell(int id_stock,Date date,Stock sx) throws SQLException{
		if(sx.quantity <= 0)return;
		//		int currentQuantity= currentQuantity(date,id_stock);
		int currentQuantity= getQuantity(date,id_stock);
		//		System.out.println("Current quantity " + currentQuantity);
		if(sx.quantity > getQuantity(date,id_stock))sx.quantity=currentQuantity;
		sx.quote = Portfolio.getQuote(date, id_stock);
		sx.amount = sx.quote.multiply(new BigDecimal(sx.quantity));
		sx.cost = sx.quote.multiply(new BigDecimal(sx.quantity)).divide(new BigDecimal(1/commission),2,BigDecimal.ROUND_UP).multiply(new BigDecimal(-1.));

		String req = String.format("INSERT INTO movements (id_stock,id_portfolio,quantity,quote,date,amount,type,comment) "
				+ "VALUES ('%s','%s','%s','%s','%s',%s,%s,'%s')",
				id_stock,id_portfolio,-sx.quantity,sx.quote,date,sx.amount,Portfolio.OP_STOCK_OUT,"stocks sell cost");
		//	    System.out.println(req);
		Statement stm = Portfolio.conn.createStatement();
		stm.executeUpdate(req);
		req = String.format("INSERT INTO movements (date,amount,id_portfolio,type,comment,id_stock) VALUES ('%s',%s,%s,%s,'%s',%s)",
				date,sx.cost,id_portfolio,Portfolio.OP_COST,"stocks sell extracost",id_stock);
		//	    System.out.println(req);
		stm.executeUpdate(req);
	}

	void insertMovement(Date date,BigDecimal amount,int op_code,String comment) throws SQLException{
		if(amount.compareTo(new BigDecimal(0.)) == 0)return;
		String req = String.format("INSERT INTO movements (date,amount,id_portfolio,type,comment) VALUES ('%s',%s,%s,%s,'%s')",
				date,amount,id_portfolio,op_code,comment);
		//	    System.out.println(req);
		Statement stmt = Portfolio.conn.createStatement();
		stmt.executeUpdate(req);	    
	}

	public static BigDecimal getTaxesDividends(int id_stock,BigDecimal amont) throws SQLException {
//		return amont.multiply(new BigDecimal(-0.3));
		String req = String.format(
				"select txtotale from pays where code = (select pays from stocks where id = %s) ",id_stock);
		//  System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(! rs.next())return new BigDecimal(0.);
		BigDecimal tx = rs.getBigDecimal("txtotale").divide(new BigDecimal(-100.));
//		System.out.println("tx " + tx);
		return amont.multiply(tx);
	}

	void generationHistoricMovements(Date startingDay) throws SQLException {
		Date currentDay = getDateAfter(startingDay);
		while(! currentDay.after(Calendar.getInstance().getTime())){
			ResultSet rs = getActiveStocks(currentDay);
			// chargement des dividends
			while (rs.next()) dividendsInMovement(currentDay,rs.getInt("id_stock"));
			// FIN
			// arbitrage/titres
			politic.arbitrationStocks(this, currentDay);//,vectorSellStocks,vectorPurchaseStocks);
			currentDay=getDateAfter(currentDay);
		}
	}

	void generationHistoricDetails(Date startingDay) throws SQLException {
		Date currentDay = getDateAfter(startingDay);
		ResultSet rs;
		Statement stm = Portfolio.conn.createStatement();
		String req;
		while(! currentDay.after(Calendar.getInstance().getTime())){
			//
			rs = getActiveStocks(currentDay);
			while(rs.next()){
				int id_stock = rs.getInt("id_stock");
				int quantity = getQuantity(currentDay,id_stock);
				BigDecimal quote = getQuote(currentDay,id_stock);
				BigDecimal dividends = getDividends(currentDay,id_stock);
				dividends = dividends.add(Portfolio.getTaxesDividends(id_stock, dividends)); // taxes negatives
				req = String.format("INSERT INTO details (date,id_portfolio,id_stock,quantity,quote,dividends) "
						+ "VALUES ('%s',%s,%s,%s,%s,%s)",
						currentDay,id_portfolio,id_stock,quantity,quote,dividends);
				//		    	System.out.println(req);
				stm.executeUpdate(req);		    	
			}
			req = String.format("update details set total=quantity*quote+dividends where id_portfolio = %s",id_portfolio);
			stm.executeUpdate(req);

			//	Le rendement du jour
			req = String.format("INSERT INTO rendements (id_portfolio,date,rend_annuel,total) "+
					"VALUES ('%s','%s',100*(power((select (sum(total)/(select sum(amount) from movements "+
					"where type = 0 and date < '%s')) from details where date = '%s'),"
					+ "365./(select '%s'::date - '%s'::date))-1.),(select sum(total) from details where date = '%s'))",
					id_portfolio, currentDay,currentDay,currentDay,currentDay,dCreation,currentDay);
			//		    System.out.println(req);
			Statement stmt = Portfolio.conn.createStatement();
			stmt.executeUpdate(req);	    
			//    	
			currentDay=getDateAfter(currentDay);
		}
		currentDay=getDateBefore(currentDay);
		Statement stmt = conn.createStatement();
		req = String.format("select *  from rendements where id_portfolio = %s order by date desc limit 1",
				id_portfolio);
		//		System.out.println(req);
		rs = stmt.executeQuery(req);
		double rendement = 0;
		if(rs.next()){
			rendement  = rs.getDouble("rend_annuel");
		}
		req = String.format("select sum(total) as total from details where id_portfolio = %s and date = '%s'",
				id_portfolio,currentDay);
		rs = stmt.executeQuery(req);
		rs.next();
		BigDecimal valeur = rs.getBigDecimal("total");

		System.out.println("\nDate : "+currentDay+" valeur du portefeuille " +name+" "+ valeur + " rendement " + rendement);
	}

	BigDecimal getDividends(Date currentDay, int id_stock) throws SQLException {
		String req = String.format(
				"select amount as dividends from movements "+
						"where id_portfolio = %s and id_stock = %s and date = '%s' and type = '%s'",
						id_portfolio,id_stock,currentDay,OP_DIVIDENDS);
		//  System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(! rs.next())return new BigDecimal(0.);
		return rs.getBigDecimal("dividends");	
	}

	public ResultSet getActiveStocks(Date day) throws SQLException {
		Statement stmt = conn.createStatement();
		String req = String.format("select distinct id_stock from movements" +
				" where id_portfolio = %s and type in (%s,%s) and date < '%s'"+
				"group by id_stock having sum(quantity) > 0",
				id_portfolio,OP_STOCK_IN,OP_STOCK_OUT, day);
		//		System.out.println(req);
		return stmt.executeQuery(req);

	}

	public ResultSet getNotActiveStocks(Date day) throws SQLException {
		Statement stmt = conn.createStatement();
		String req = String.format("select id as id_stock from stocks where id not in "+
				"(select distinct id_stock from movements" +
				" where id_portfolio = %s and type in (%s,%s) and date < '%s'"+
				"group by id_stock having sum(quantity) > 0)",
				id_portfolio,OP_STOCK_IN,OP_STOCK_OUT, day);
		//	System.out.println(req);
		return stmt.executeQuery(req);
	}

	void dividendsInMovement(Date currentDate,int id_stock) throws SQLException {
		Statement stmt = conn.createStatement();
		String req = String.format("select dividends from dividends where id_stock=%s and date = '%s'",id_stock,currentDate);
		//    System.out.println(req);
		ResultSet rs = stmt.executeQuery(req);
		while (rs.next()) {
			BigDecimal dividends = rs.getBigDecimal("dividends");
			//	    	System.out.println(currentDate + " dividendes " + dividends);
			BigDecimal amount = dividends.multiply(new BigDecimal(getQuantity(currentDate,id_stock)));
			req = String.format("INSERT INTO movements (date,amount,id_portfolio,id_stock,type,comment) VALUES ('%s',%s,%s,%s,%s,'%s')",
					currentDate,amount,id_portfolio,id_stock,Portfolio.OP_DIVIDENDS,"Stocks dividends");
			//	    System.out.println(req);
			Statement stmtn = Portfolio.conn.createStatement();
			stmtn.executeUpdate(req);
			BigDecimal taxes = getTaxesDividends(id_stock, amount);
			if( taxes.compareTo(new BigDecimal(0.)) == 0)return;
			req = String.format("INSERT INTO movements (date,amount,id_portfolio,id_stock,type,comment) VALUES ('%s',%s,%s,%s,%s,'%s')",
					currentDate,taxes,id_portfolio,id_stock,Portfolio.OP_TAX_DIVIDENDS,"Stocks taxes on dividends");
			//	    System.out.println(req);
			stmtn = Portfolio.conn.createStatement();
			stmtn.executeUpdate(req);	 
		}
	}

	public static void close() throws SQLException {
		Portfolio.conn.close();
	}

	int getQuantity(Date date, int id_stock) throws SQLException {
		String req = String.format(
				"select sum(quantity) as quantity from movements "+
						"where id_portfolio = %s and id_stock = %s and date < '%s' and type in ('%s','%s') group by id_stock",// having sum(quantity) > 0",
						id_portfolio,id_stock,date,OP_STOCK_IN, OP_STOCK_OUT);
		//  System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(! rs.next())return 0;
		int squantity = rs.getInt("quantity");
		if(squantity < 0) System.out.println("----> QUANTITE NEGATIVE POUR " + date + " id_stock " + id_stock +"<----");
		return squantity;
	}

	public BigDecimal getCash(Date date) throws SQLException {
		String req = String.format(
				"select sum(amount) as amount from movements "+
						"where id_portfolio = %s and date <= '%s'",
						id_portfolio,date);
		//  System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(  ! rs.next())return new BigDecimal(0.);
		return rs.getBigDecimal("amount");	
	}

	static long getNberDaysBetween(Date one, Date two) {
		long difference =  (one.getTime()-two.getTime())/86400000;
		return Math.abs(difference);
	}

	void print(String string, Date date, Vector<Stock> vectorStocks) throws SQLException {
		System.out.println(string + " le " + date);
		for(Stock s : vectorStocks){
			System.out.print(" " + s.name);
			System.out.print(" quantité "+ s.quantity);
			System.out.print(" cotation "+ s.quote);
			System.out.print(" montant "+ s.amount);
			System.out.print(" coût "+ s.cost);
			System.out.print(" valeur du portefeuille " + portfolioValue(date));
			System.out.println(" cash "+ this.getCash(date));		
		}

	}

	public BigDecimal portfolioValue(Date date) throws SQLException{
		String req = String.format(
				"select id_stock,COALESCE(sum(quantity)*(select close from quotes where date <= '%s' and id_stock = t1.id_stock" +
						" order by date desc limit 1),0.) as amount from movements t1 where id_stock in (select id from stocks where id in" +
						" (select id_stock from movements where id_portfolio = %s and date <= '%s'  group by id_stock having sum(quantity) >0 ))" +
						" and date <= '%s' group by id_stock", date,id_portfolio,date,date);
		//  System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		BigDecimal sum = new BigDecimal(0.);
		while(rs.next())sum = sum.add(rs.getBigDecimal("amount"));
		return sum;
	}

} 