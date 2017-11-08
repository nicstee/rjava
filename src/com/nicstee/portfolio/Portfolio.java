package com.nicstee.portfolio;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

public class Portfolio {

	static int OP_INVESTMENT = 0;
	static int OP_STOCK_IN = 1;
	static int OP_STOCK_OUT = 2;
	static int OP_DIVIDENDS = 3;
	static int OP_COST = 4;
	static int OP_TAX_DIVIDENDS = 5;

	public static Connection conn;

	boolean savePerf = false;
	boolean saveRend = false;
	boolean printStatus = true;
	Date dCreation;
	Date dFin;
	double commission;
	Politic politic;
	String name ;
	BigDecimal startCash; 
	BigDecimal lastAmount = BigDecimal.valueOf(0.);


	int id_portfolio;

	public Portfolio(String name) throws SQLException{
		String dbURL = "jdbc:postgresql:Portfolio?user=postgres&password=GLOZQCKI";
		if(conn == null) conn = DriverManager.getConnection(dbURL);
		if (conn != null) {
			System.out.println("Connected to database Portfolio");
		}else{
			System.out.println("no connexion to database Portlfolio !!!");
		}
		generationHistoricDetails(name);
	}

	@SuppressWarnings("deprecation")
	public Portfolio(String name, Politic politic, String creation, String fin, double cash, double commissionPourcent) throws SQLException, IOException {
		this.politic=politic;
		this.politic.setPortfolio(this);
		this.commission=commissionPourcent/100.;
		this.name=name;
		dCreation = Date.valueOf(creation);
		dCreation.setDate(1); // begin of month
		dFin = Date.valueOf(fin);
		startCash = new BigDecimal(cash);
		generationPortfolio();
	}

	public Portfolio() throws SQLException, IOException {
	}

	void generationPortfolio () throws SQLException, IOException{
		id_portfolio = creationPortfolio();
		insertMovement(dCreation, startCash,OP_INVESTMENT, "Initial investment");
		politic.initPortfolio(startCash,dCreation); // stocks, first loading
		if(this.printStatus)System.out.println("-------------------------------------------------------------------------");
		generationHistoricMovements();
		savePerformance(dFin);
	}

	int creationPortfolio() throws SQLException{ // appelée par generationPortfolio(...), return id_portfolio
		String dbURL = "jdbc:postgresql:Portfolio?user=postgres&password=GLOZQCKI";
		if(conn == null) conn = DriverManager.getConnection(dbURL);
		if (conn != null) {
			if(this.printStatus)System.out.println("Connected to database Portefeuille");
		}
		String req = String.format("SELECT count(*) FROM portfolios where name = '%s'",name.trim());
		//        System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		rs.next();
		if(rs.getInt(1) > 0){;
		//        	System.out.println("portfolio exits !!!");
		req = String.format("delete FROM portfolios where name = '%s'",name.trim());
		//	        System.out.println(req);
		stmt.executeUpdate(req);
		}
		req = String.format("INSERT INTO portfolios (name) VALUES ('%s')",name.trim());
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
		String req = String.format("select quotestockeur('%s',%s) as close",date,id_stock);
		//        System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(!rs.next())return new BigDecimal(0.);
		return rs.getBigDecimal("close");	
	}

	public BigDecimal getQuotePurch(Date date, int id_stock) throws SQLException {
		String req = String.format("select quotepurchstockeur('%s',%s,%s) as quote",
				date,id_portfolio,id_stock);
		//        System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(!rs.next())return new BigDecimal(0.);
		return rs.getBigDecimal("quote");	
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

	public void stocksPurchase(int id_stock,Date date,Stock sx) throws SQLException, IOException{
		if(sx.quantity == 0)return;
		BigDecimal currentCash = getCash(date);
		sx.amount = sx.quoteEur.multiply(new BigDecimal(-sx.quantity));
		if(currentCash.add(sx.amount).compareTo(new BigDecimal(0.)) <=  0){		
			sx.quantity=currentCash.divide(sx.quoteEur,0,BigDecimal.ROUND_DOWN).intValue();
			sx.amount = sx.quoteEur.multiply(new BigDecimal(-sx.quantity));
			if(sx.quantity <= 0 )return;
		}
		sx.cost = sx.quoteEur.multiply(new BigDecimal(sx.quantity)).divide(new BigDecimal(1/commission),2,BigDecimal.ROUND_UP).multiply(new BigDecimal(-1.));
		String req = String.format("INSERT INTO movements (id_stock,id_portfolio,quantity,quote,date,amount,type,comment) "
				+ "VALUES (%s,%s,%s,%s,'%s',%s,%s,'%s')",
				id_stock,id_portfolio,sx.quantity,sx.quoteEur,date,sx.amount,Portfolio.OP_STOCK_IN,"stocks purchase cost");
		//	    System.out.println(req);
		Statement stm = Portfolio.conn.createStatement();
		stm.executeUpdate(req);	
		req = String.format("INSERT INTO movements (date,amount,id_portfolio,type,comment,id_stock) VALUES ('%s',%s,%s,%s,'%s',%s)",
				date,sx.cost,id_portfolio,Portfolio.OP_COST,"stocks purchase extracost",id_stock);
		//	    System.out.println(req);
		stm.executeUpdate(req);
	}

	public void stocksPurchase(Date date,Vector<Stock> vectorPurchaseStocks) throws SQLException, IOException{	
		for(Stock s : vectorPurchaseStocks){
			stocksPurchase(s.id_stock,date,s);
		}
		if(this.printStatus)printVectorStocks("Actions achetées",date,vectorPurchaseStocks);
	}

	public void stocksSell(int id_stock,Date date,Stock sx) throws SQLException, IOException{
		if(sx.quantity <= 0)return;
		int currentQuantity= getQuantity(date,id_stock);
		if(sx.quantity > getQuantity(date,id_stock))sx.quantity=currentQuantity;
		sx.amount = sx.quoteEur.multiply(new BigDecimal(sx.quantity));
		sx.cost = sx.quoteEur.multiply(new BigDecimal(sx.quantity)).divide(new BigDecimal(1/commission),2,BigDecimal.ROUND_UP).multiply(new BigDecimal(-1.));
		String req = String.format("INSERT INTO movements (id_stock,id_portfolio,quantity,quote,date,amount,type,comment) "
				+ "VALUES ('%s','%s','%s','%s','%s',%s,%s,'%s')",
				id_stock,id_portfolio,-sx.quantity,sx.quoteEur,date,sx.amount,Portfolio.OP_STOCK_OUT,"stocks sell cost");
		//	    System.out.println(req);
		Statement stm = Portfolio.conn.createStatement();
		stm.executeUpdate(req);
		req = String.format("INSERT INTO movements (date,amount,id_portfolio,type,comment,id_stock) VALUES ('%s',%s,%s,%s,'%s',%s)",
				date,sx.cost,id_portfolio,Portfolio.OP_COST,"stocks sell extracost",id_stock);
		//	    System.out.println(req);
		stm.executeUpdate(req);
	}

	public void stocksSell(Date date,Vector<Stock> vectorSellStocks) throws SQLException, IOException{
		for(Stock s : vectorSellStocks){
			stocksSell(s.id_stock,date,s);
		}
		if(this.printStatus)printVectorStocks("Actions vendues",date,vectorSellStocks);
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
		String req = String.format(
				"select txtotale from pays where code = (select pays from stocks where id = %s) ",id_stock);
		//  System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(! rs.next())return new BigDecimal(0.);
		BigDecimal tx = rs.getBigDecimal("txtotale").divide(new BigDecimal(-100.));
		return amont.multiply(tx);
	}

	void generationHistoricMovements() throws SQLException, IOException {
		Date currentDay = getDateAfter(dCreation);
		while(! currentDay.after(dFin)){
			ResultSet rs = getActiveStocks(currentDay);
			// chargement des dividends
			if(this.savePerf)this.savePerformance(currentDay);
			while (rs.next()) dividendsInMovement(currentDay,rs.getInt("id_stock"));
			// FIN
			// arbitrage/titres
			Vector<Stock> vectorActiveStocks =  politic.arbitrationStocks(currentDay);
			if(vectorActiveStocks != null){
				//		impression
				if(printStatus)printPortfolio(currentDay,vectorActiveStocks);
				System.out.println("");
			}
			Calendar c = Calendar.getInstance();
			c.setTime(currentDay);
			if(c.get(Calendar.DAY_OF_WEEK) == 6)saveStatistic(currentDay);
			currentDay=getDateAfter(currentDay);
		}
		String req = String.format("update portfolios set status=true,creation='%s',fin='%s'"
				+ " where id = %s",dCreation,dFin,id_portfolio);
		//		  System.out.println(req);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(req);

	}

	private void saveStatistic(Date currentDay) throws SQLException {
		Vector<Stock> vectorActiveStocks =	this.getVectorActiveStocks(currentDay);
		BigDecimal sAmount = BigDecimal.valueOf(0.);
		java.util.Iterator<Stock> itr = vectorActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s =itr.next();
			BigDecimal amount = s.quoteEur.multiply(BigDecimal.valueOf(s.quantity));
			sAmount = sAmount.add(amount);
		}
		BigDecimal cash = getCash(currentDay);
		sAmount = sAmount.add(cash);
		System.out.print("Vendredi du " + currentDay + " val. portefeuille " + sAmount);
		BigDecimal perfSem = ((sAmount.multiply(BigDecimal.valueOf(100.)))
				.divide(lastAmount,2,BigDecimal.ROUND_UP)).subtract(BigDecimal.valueOf(100.));
		System.out.println(" valeur précédente " + lastAmount + " " +perfSem );
		String req = String.format(
				"INSERT INTO temporaire (id_portfolio,date,samount,lastamount,perfSem) "
				+ "VALUES (%s,'%s',%s,%s,%s)",//,%s,%s)",
				id_portfolio,currentDay,sAmount,lastAmount,perfSem);//,lissageAmount,perfLiss);
		//	    System.out.println(req);
		Statement stm = Portfolio.conn.createStatement();
		stm.executeUpdate(req);	
		
		lastAmount=sAmount;
	}

	public void generationHistoricDetails(String name)throws SQLException {
		Statement stmt = Portfolio.conn.createStatement();
		String req = String.format("select * from portfolios where name = '%s'",name.trim());
		ResultSet rs = stmt.executeQuery(req);
		if(!rs.next()){
			System.out.print("Portefeuille inconnu !!!");
			return;
		}
		if(! rs.getBoolean("status")){
			System.out.println("Portefeuille incomplet !!!");
			return;
		}
		name=rs.getString("name").trim();
		id_portfolio=rs.getInt("id");
		dCreation=rs.getDate("creation");
		dFin=rs.getDate("fin");
		req = String.format("delete from details where id_portfolio = %s",id_portfolio);
		stmt.executeUpdate(req);
		Calendar cal = Calendar.getInstance();
		if(this.printStatus)System.out.println( "Démarrage génération des détails de " + name + " debut " + dCreation + " fin " + dFin +
				" à " + new SimpleDateFormat("HH:mm:ss").format(cal.getTime()) );
		startHistoricDetails();
		cal = Calendar.getInstance();
		System.out.println("Fin de la génération à  " + new SimpleDateFormat("HH:mm:ss").format(cal.getTime()));

	}

	@SuppressWarnings("deprecation")
	private void startHistoricDetails() throws SQLException {
		Date currentDay = dCreation;
		ResultSet rs;
		Statement stm = Portfolio.conn.createStatement();
		String req;
		while(! currentDay.after(dFin)){
			rs = getActiveStocks(currentDay);
			while(rs.next()){
				int id_stock = rs.getInt("id_stock");
				int quantity = getQuantity(currentDay,id_stock);
				BigDecimal quote = getQuote(currentDay,id_stock);
				BigDecimal earningsCosts = getEarningsCosts(currentDay,id_stock);
				req = String.format("INSERT INTO details (date,id_portfolio,id_stock,quantity,quote,earningscosts) "
						+ "VALUES ('%s',%s,%s,%s,%s,%s)",
						currentDay,id_portfolio,id_stock,quantity,quote,earningsCosts);
				//				System.out.println(req);
				stm.executeUpdate(req);		    	
			}

			//	Le rendement du mois
			if(currentDay.getDate() == 1 && currentDay.compareTo(dCreation) != 0){
				req = String.format("INSERT INTO rendementsDetails (id_portfolio,date,rend_annuel,total) "+
						"VALUES ('%s','%s',100*(power((select (sum(total)/(select sum(amount) from movements "+
						"where type = 0 and date < '%s')) from details where date = '%s'),"
						+ "365./(select '%s'::date - '%s'::date))-1.),(select sum(total) from details where date = '%s'))",
						id_portfolio, currentDay,currentDay,currentDay,currentDay,dCreation,currentDay);
				//		    System.out.println(req);
				Statement stmt = Portfolio.conn.createStatement();
				stmt.executeUpdate(req);
			}
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

		if(this.printStatus)System.out.println("\nDate : "+currentDay+" valeur du portefeuille " +name+" "+ valeur + " rendement " + rendement);
	}

	private BigDecimal getEarningsCosts(Date date, int id_stock) throws SQLException {
		String req = String.format(
				"select amount as earningscost from movements "+
						"where id_portfolio = %s and id_stock = %s and date = '%s' and type in ('%s','%s','%s')",
						id_portfolio,id_stock,date,OP_DIVIDENDS,OP_TAX_DIVIDENDS,OP_COST);
		//  System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(! rs.next())return new BigDecimal(0.);
		return rs.getBigDecimal("earningscost");	
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
				" where id_portfolio = %s and type in (%s,%s) and date <= '%s'"+
				" group by id_stock having sum(quantity) > 0",
				id_portfolio,OP_STOCK_IN,OP_STOCK_OUT, day);//,day,id_portfolio);
		//		System.out.println(req);
		return stmt.executeQuery(req);

	}

	public  Vector<Stock> getVectorActiveStocks(Date day) throws SQLException {
		Vector<Stock> vectorActiveStocks = new Vector<Stock>();
		ResultSet rsActives = getActiveStocks(day);
		while(rsActives.next()){
			int id_stock=rsActives.getInt("id_stock");
			int since = getSince(day,id_stock); // en mois
			Stock s = new Stock(day,id_stock);
			s.since = since;
			s.quantity=this.getQuantity(day, id_stock);
			s.amount=BigDecimal.valueOf(s.quantity).multiply(s.quoteEur);
			s.quotePurchEur=getQuotePurch(day,id_stock);
			s.amountPurchase=BigDecimal.valueOf(s.quantity).multiply(s.quotePurchEur);
			vectorActiveStocks.add(s);
		}
		return vectorActiveStocks;
	}


	private int getSince(Date date,int id_stock) throws SQLException {
		String req = String.format(
				"select purchaseDuration('%s',%s,%s) as duration",
				date,id_portfolio,id_stock);
		//		  System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(! rs.next())return 0;
		return rs.getInt("duration");
	}

	public ResultSet getNotActiveStocks(Date day) throws SQLException {
		Statement stmt = conn.createStatement();
		String req = String.format("select id as id_stock from stocks where actived and firstquote <= '%s' and id not in "+
				"(select distinct id_stock from movements" +
				" where id_portfolio = %s and type in (%s,%s) and date < '%s'"+
				"group by id_stock having sum(quantity) > 0)",
				day,id_portfolio,OP_STOCK_IN,OP_STOCK_OUT, day);
		//	System.out.println(req);
		return stmt.executeQuery(req);
	}

	public  Vector<Stock> getVectorNotActiveStocks(Date day) throws SQLException {
		Vector<Stock> vectorNotActiveStocks = new Vector<Stock>();
		ResultSet rsActives = getNotActiveStocks(day);
		while(rsActives.next()){
			int id_stock=rsActives.getInt("id_stock");
			vectorNotActiveStocks.add(new Stock(day,id_stock));
		}
		return vectorNotActiveStocks;
	}

	void dividendsInMovement(Date currentDate,int id_stock) throws SQLException {
		Statement stmt = conn.createStatement();
		String req = String.format("select dividends from dividends where id_stock=%s and date = '%s'",id_stock,currentDate);
		//    System.out.println(req);
		ResultSet rs = stmt.executeQuery(req);
		while (rs.next()) {
			BigDecimal dividends = getInEur(currentDate,id_stock,rs.getBigDecimal("dividends"),6);
			BigDecimal amount = dividends.multiply(new BigDecimal(getQuantity(currentDate,id_stock)));
			req = String.format("INSERT INTO movements (date,amount,id_portfolio,id_stock,type,comment) VALUES ('%s',%s,%s,%s,%s,'%s')",
					currentDate,amount,id_portfolio,id_stock,Portfolio.OP_DIVIDENDS,"Stocks dividends");
			//				    System.out.println(req);
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
				"select quantitystock('%s',%s,%s) as quantity",
				date,id_portfolio,id_stock);
		//		  System.out.println(req);
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

	void printVectorStocks(String string, Date date, Vector<Stock> vectorStocks) throws SQLException {
		System.out.println(string + " le " + date);
		for(Stock s : vectorStocks){
			System.out.print(" " + s.id_stock);			
			System.out.print(" " + s.code);
			System.out.print(" quantité "+ s.quantity);
			System.out.print(" cotation "+ s.quoteEur);
			System.out.print(" montant "+ s.amount);
			System.out.print(" coût "+ s.cost);
			System.out.print(" perf "+ BigDecimal.valueOf(s.perf).setScale(3, BigDecimal.ROUND_HALF_EVEN));
			System.out.print(" date "+s.date);
			System.out.println(" since "+ s.since);
		}
	}

	void printPortfolio(Date date,Vector<Stock> vectorActiveStocks) throws SQLException{
		System.out.println("Situation au " + date + " nbre d'actions " + vectorActiveStocks.size());
		BigDecimal sAmount = BigDecimal.valueOf(0.);
		java.util.Iterator<Stock> itr = vectorActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s =itr.next();
			System.out.print(" " + s.id_stock);
			System.out.print(" " + s.code);
			System.out.print(", depuis " + s.since);
			System.out.print(" mois, qty " + s.quantity);
			System.out.print(", cot " + s.quoteEur);
			BigDecimal amount = s.quoteEur.multiply(BigDecimal.valueOf(s.quantity));
			sAmount = sAmount.add(amount);
			System.out.print(", mt " + amount);
			System.out.println();	
		}
		BigDecimal cash = getCash(date);
		double rendement = portfolioRendement(date);
		System.out.print("Total Portefeuille " + sAmount + " cash " + cash);
		if(date.after(dCreation)){
			System.out.println(" rend. " + rendement +"%");
			if(saveRend){
				String req = String.format("INSERT INTO rendementsmovements (id_portfolio,date,vstocks,cash,vportfolio,rendement,name) "
						+ "values (%s,'%s',%s,%s,%s,%s)",
						id_portfolio, date,sAmount,cash,sAmount.add(cash),rendement);
				//				System.out.println("\n"+req);
				Statement stmt = Portfolio.conn.createStatement();
				stmt.executeUpdate(req);
			}
		}else{
			System.out.println();
		}
		System.out.println("-------------------------------------------------------------------------");
	}

	void savePerformance(Date date) throws SQLException{
		Vector<Stock> vectorActiveStocks = this.getVectorActiveStocks(date);
		BigDecimal sAmount = BigDecimal.valueOf(0.);
		Iterator<Stock> itr = vectorActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s =itr.next();
			BigDecimal amount = s.quoteEur.multiply(BigDecimal.valueOf(s.quantity));
			sAmount = sAmount.add(amount);
		}
		double rend = 0;
		if(date.equals(dFin))rend=portfolioRendement(date);
		BigDecimal cash = getCash(date);
		String req = String.format("INSERT INTO performances (id_portfolio,date,vportfolio,cash,total,rendement) "
				+ "values (%s,'%s',%s,%s,%s,%s)",
				id_portfolio, date,sAmount,cash,sAmount.add(cash),rend);
		Statement stmt = Portfolio.conn.createStatement();
		stmt.executeUpdate(req);
	}

	public double portfolioRendement(Date date) throws SQLException{
		String req = String.format(
				"select round(sum(quantity)*quotestockeur('%s',id_stock),2) as amount from movements "
						+"where id_portfolio = %s and date <= '%s' and type in (1,2) group by id_stock"
						,date,id_portfolio,date);
		//		System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		BigDecimal sum = new BigDecimal(0.);
		while(rs.next())sum = sum.add(rs.getBigDecimal("amount"));
		req = String.format(
				"select sum(amount) as amount from movements where id_portfolio = %s and date <= '%s'"
						+ " and type in (3,4,5)",id_portfolio,date);
		stmt = conn.createStatement();
		rs = stmt.executeQuery(req);
		if(rs.next())sum.add(rs.getBigDecimal("amount"));
		req = String.format(
				"select sum(amount) as amount from movements where id_portfolio = %s and date <= '%s'"
						+ " and type = 0",id_portfolio,date);
		//				System.out.println(req);
		stmt = conn.createStatement();
		rs = stmt.executeQuery(req);
		if(!rs.next())return 0.;
		double rendement =(sum.divide(rs.getBigDecimal("amount"),6,BigDecimal.ROUND_HALF_EVEN)).floatValue();
		return Math.round((Math.pow(rendement,365./getNberDaysBetween(dCreation, date))-1.)*10000.)/100.;
	}

	static BigDecimal getInEur(Date date,int id_stock,BigDecimal amount,int dec) throws SQLException{
		String req = String.format("select currencystock('%s',%s) as rate",date,id_stock);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(!rs.next())return new BigDecimal(0.);
		return amount.divide(rs.getBigDecimal("rate"),dec,BigDecimal.ROUND_UP);
	}
	static BigDecimal getExchangeRate(Date date,String currency) throws SQLException{
		String req = String.format("select rate from exchangerates where date <= '%s' and currency = '%s' "+
				"order by date desc limit 1",date,currency);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(!rs.next())return new BigDecimal(1.);
		return rs.getBigDecimal("rate");
	}
	public void setSavePerf(boolean savePerf) {
		this.savePerf = savePerf;
	}

	public void setSaveRend(boolean saveRend) {
		this.saveRend = saveRend;
	}

	@SuppressWarnings("deprecation")
	public void setdCreation(String creation) {
		this.dCreation = Date.valueOf(creation);
		dCreation.setDate(1); // begin of month
	}

	public void setdFin(String dFin) {
		this.dFin = Date.valueOf(dFin);
	}

	public void setCommission(double commission) {
		this.commission = commission;
	}

	public void setPolitic(Politic politic) {
		this.politic = politic;
		politic.setPortfolio(this);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStartCash(BigDecimal startCash) {
		this.startCash = startCash;
		this.lastAmount = startCash;
	}
}