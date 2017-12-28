package com.nicstee.portfolio;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import com.nicstee.portfolio.dbLoading.CSVUtils;

public class Portfolio {

	static int OP_INVESTMENT = 0;
	static int OP_STOCK_IN = 1;
	static int OP_STOCK_OUT = 2;
	static int OP_DIVIDENDS = 3;
	static int OP_COST = 4;
	static int OP_TAX_DIVIDENDS = 5;
	public static int ING = 1;
	public static int BINCKBANCK = 2;

	public static Connection conn;
	public int id_portfolio;
	boolean printStatus = true;
	Date dCreation;
	Date dFin;
	Politic politic;

	String name ;
	int bank;
	public Vector <Stock> saveVectorActiveStocks;

	BigDecimal startCash; 
	BigDecimal lastAmount = BigDecimal.valueOf(0.);	

	BigDecimal trancheBinckBank[] = {new BigDecimal(2500.),new BigDecimal(5000.),
			new BigDecimal(25000.),new BigDecimal(50000.)};
	BigDecimal tarifBinckBank[][] = {
			{ new BigDecimal(7.25), new BigDecimal(9.75), new BigDecimal(9.75)},
			{ new BigDecimal(9.75), new BigDecimal(9.75),new BigDecimal(14.75)},
			{new BigDecimal(14.75),new BigDecimal(14.75),new BigDecimal(24.75)},
			{new BigDecimal(24.75),new BigDecimal(24.75),new BigDecimal(29.75)}
	};

	public Portfolio() throws SQLException, IOException {
	}

	void generationPortfolio () throws SQLException, IOException{
		id_portfolio = creationPortfolio();
		insertMovement(dCreation, startCash,OP_INVESTMENT, "Initial investment");
		politic.initPortfolio(startCash,dCreation); // stocks, first loading
		if(this.printStatus)System.out.println("-------------------------------------------------------------------------");
		generationHistoricMovements(getDateAfter(dCreation));
		//		savePerformance(dFin);
	}

	void generationPortfolio (String file) throws SQLException, IOException{
		id_portfolio = creationPortfolio();
		insertMovement(dCreation, startCash,OP_INVESTMENT, "Initial investment");
		loadingPortfolio(dCreation,file); // stocks, first loading
		politic.initData(startCash, dCreation);
		if(this.printStatus)System.out.println("-------------------------------------------------------------------------");
		generationHistoricMovements(getDateAfter(dCreation));
		//		savePerformance(dFin);
	}

	void generationPortfolio (Vector<Stock> saveVectorActiveStocks) throws SQLException, IOException{
		id_portfolio = creationPortfolio();
		insertMovement(dCreation, startCash,OP_INVESTMENT, "Initial investment");
		loadingPortfolio(dCreation,saveVectorActiveStocks); // stocks, first loading
		politic.initData(startCash, dCreation);
		if(this.printStatus)System.out.println("-------------------------------------------------------------------------");
		generationHistoricMovements(getDateAfter(dCreation));
		//		savePerformance(dFin);
	}

	public void generationPortfolio(int id_portfolio) throws SQLException, IOException {
		this.id_portfolio=id_portfolio;
		String dbURL = "jdbc:postgresql:Portfolio?user=postgres&password=GLOZQCKI";
		if(conn == null) conn = DriverManager.getConnection(dbURL);
		if (conn != null) {
			if(this.printStatus)System.out.println("Connected to database Portefeuille");
		}
		String req = String.format("select "
				+ "(select samount from temporaire where id_portfolio=%s order by date desc limit 1) as lastamount,"
				+ "(select min(date) from movements where id_portfolio=%s) as dcreation,"
				+ " GREATEST((select max(date) from temporaire where id_portfolio=%s),"
				+ "(select max(date) from movements where id_portfolio=%s)) as dfin"
				,id_portfolio,id_portfolio,id_portfolio,id_portfolio);
		System.out.println(req);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		rs.next();
		this.lastAmount=rs.getBigDecimal("lastamount");
		this.dCreation=rs.getDate("dcreation");
		generationHistoricMovements(getDateAfter(rs.getDate("dfin")));

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

	public void stocksPurchase(int id_stock,Date date,Stock sx) throws SQLException, IOException{
		if(sx.quantity == 0)return;
		BigDecimal currentCash = getCash(date);
		sx.amount = sx.quoteEur.multiply(new BigDecimal(-sx.quantity));
		if(currentCash.add(sx.amount).compareTo(new BigDecimal(0.)) <=  0){		
			sx.quantity=currentCash.divide(sx.quoteEur,0,BigDecimal.ROUND_DOWN).intValue();
			sx.amount = sx.quoteEur.multiply(new BigDecimal(-sx.quantity));
			if(sx.quantity <= 0 )return;
		}
		sx.cost = getComAndTOB(sx);
		String req = String.format("INSERT INTO movements (id_stock,id_portfolio,quantity,quote,date,amount,type,comment) "
				+ "VALUES (%s,%s,%s,%s,'%s',%s,%s,'%s')",
				id_stock,id_portfolio,sx.quantity,sx.quoteEur,date,sx.amount,Portfolio.OP_STOCK_IN,"stocks purchase cost");
		//	    System.out.println(req);
		sx.amount=sx.amount.multiply(BigDecimal.valueOf(-1.));
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
		sx.cost = getComAndTOB(sx);
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

	public void setAgios(Date date) throws SQLException {
		if(this.bank == Portfolio.BINCKBANCK)return;
		if(this.bank == Portfolio.ING){
			BigDecimal cost = BigDecimal.valueOf(0.);
			//			double tx = -(.0015 + .0005)*1.21/12.; //actuellement
			double tx = -.0025/12.; // agios
			String req = String.format("select samount from temporaire "
					+ "where id_portfolio = %s and date < '%s' order by date desc limit 1;",
					id_portfolio,date);
			//	       System.out.println(req);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(req);
			if(rs.next()){
				cost=rs.getBigDecimal("samount");
				cost=(cost.multiply(BigDecimal.valueOf(tx)).setScale(2,BigDecimal.ROUND_HALF_EVEN));
				cost=cost.add(BigDecimal.valueOf(41.66));// droite de garde
				//			 System.out.println("samount " + cost);
			}
			else{
				System.out.println("!!! Banque inconnue pour le calcul des agios/comm/frais de garde/...");
				System.exit(999);
			}
			req = String.format("INSERT INTO movements (date,amount,id_portfolio,type,comment) VALUES ('%s',%s,%s,%s,'%s')",
					date,cost,id_portfolio,Portfolio.OP_COST,"droits de gardes, agios,...");
			//				    System.out.println(req);
			stmt.executeUpdate(req);			
		}
		else{
			System.out.println("!!! Banque inconnue pour le calcul des agios/comm/frais de garde/...");
			System.exit(999);
		}
		return;

	}


	public BigDecimal getComAndTOB(Stock s) {
		// TOB
		BigDecimal amount = s.amount.abs();
		BigDecimal commission = new BigDecimal(0.);
		if(s.pays.compareTo("BE") == 0)
			commission=amount.multiply(BigDecimal.valueOf(.0012)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		else
			commission=amount.multiply(BigDecimal.valueOf(.0035)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		// frais bancaire
		if(this.bank == Portfolio.ING)commission=commission.add(getCommissionIng(s));
		else if(this.bank == Portfolio.BINCKBANCK)commission=commission.add(getCommissionBinckBank(s));
		else{
			System.out.println("!!! Banque inconnue pour le calcul des frais d'achat/vente");
			System.exit(999);
		}
		return commission.multiply(BigDecimal.valueOf(-1.));
	}

	private BigDecimal getCommissionIng(Stock s) {
		return s.amount.abs().multiply(BigDecimal.valueOf(.00375)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	private BigDecimal getCommissionBinckBank(Stock s) {
		int i = 0;
		while(i<4){
			if((s.amount.abs()).compareTo(trancheBinckBank[i]) <= 0)break;
			else i++;
		}
		int j = 0;
		String p=s.pays.trim();
		if(p.compareTo("BE") == 0)j=0;
		else if(p.compareTo("FR") == 0 || p.compareTo("NL") == 0 || p.compareTo("DE") == 0 )j=1;
		else if(p.compareTo("US") == 0 )j=2;
		else {
			System.out.println("pays " + p + " sans tarification de commission de vente/achat");
			System.exit(999);
		};
		if(i==4){
			return tarifBinckBank[3][j].multiply((s.amount.abs().divide(BigDecimal.valueOf(50000.)).setScale(0,BigDecimal.ROUND_DOWN)));
		}
		return tarifBinckBank[i][j];
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

	void generationHistoricMovements(Date startDate) throws SQLException, IOException {
		Date currentDay = startDate;
		Vector<Stock> vectorActiveStocks = null;
		while(! currentDay.after(dFin)){
			if(currentDay.getDate() == 1)setAgios(currentDay);
			ResultSet rs = getActiveStocks(currentDay);
			// chargement des dividends
			//			if(this.savePerf)this.savePerformance(currentDay);
			while (rs.next()) dividendsInMovement(currentDay,rs.getInt("id_stock"));
			// FIN
			// arbitrage/titres
			vectorActiveStocks =  politic.arbitrationStocks(currentDay);
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
		savePortfolio();

	}

	private void saveStatistic(Date currentDay) throws SQLException {
		Vector<Stock> vectorActiveStocks =	this.getVectorActiveStocks(currentDay);
		BigDecimal sAmount = BigDecimal.valueOf(0.);
		BigDecimal sAmountPurchase = BigDecimal.valueOf(0.);
		java.util.Iterator<Stock> itr = vectorActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s =itr.next();
			sAmount = sAmount.add(s.amount);
			sAmountPurchase = sAmountPurchase.add(s.amountPurchase);
		}
		BigDecimal beneficeOnPurchase = sAmount.subtract(sAmountPurchase);
		BigDecimal cash = getCash(currentDay);
		sAmount = sAmount.add(cash);
		System.out.print("Vendredi du " + currentDay + " val. portefeuille " + sAmount);
		BigDecimal perfSem = ((sAmount.multiply(BigDecimal.valueOf(100.)))
				.divide(lastAmount,2,BigDecimal.ROUND_UP)).subtract(BigDecimal.valueOf(100.));
		System.out.println(" valeur précédente " + lastAmount + " benef. on  purch. " + beneficeOnPurchase + " " +perfSem );
		String req = String.format(
				"INSERT INTO temporaire (id_portfolio,date,samount,lastamount,beneficeOnPurchase,perfSem) "
						+ "VALUES (%s,'%s',%s,%s,%s,%s)",
						id_portfolio,currentDay,sAmount,lastAmount,beneficeOnPurchase,perfSem);
		//			    System.out.println(req);
		Statement stm = Portfolio.conn.createStatement();
		stm.executeUpdate(req);	

		lastAmount=sAmount;
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
			s.perf=politic.perfStockForSell(day, since, s);
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
			if( s.sellType > 0){
				if(s.sellType==1)System.out.print(" pour surperformance");
				if(s.sellType==2)System.out.print(" + 10 % -> 1/2 vendues");
				if(s.sellType==3)System.out.print(" trop longtemps en port.");
			}
			System.out.print(" " + s.id_stock);			
			System.out.print(" " + s.code);
			System.out.print(" quantité "+ s.quantity);
			System.out.print(" cotation "+ s.quoteEur);
			System.out.print(" montant "+ s.amount);
			System.out.print(" coût "+ s.cost);
			double sperf=(1./s.perf-1.)*100.;
			System.out.print(" perf "+ BigDecimal.valueOf(sperf).setScale(2, BigDecimal.ROUND_HALF_EVEN)+"%");
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
			sAmount = sAmount.add(s.amount);
			System.out.print(", mt " + s.amount);
			if(s.since > 0){
				BigDecimal diff = s.amount.subtract(s.amountPurchase);
				System.out.print(" gain ou perte " + diff +" " +
						diff.multiply(BigDecimal.valueOf(100.)).divide(s.amountPurchase, 4, BigDecimal.ROUND_HALF_EVEN)+"%");
			}
			System.out.println();
		}
		BigDecimal cash = getCash(date);
		double rendement = portfolioRendement(date);
		System.out.print("Total Portefeuille " + sAmount + " cash " + cash + " Total " + sAmount.add(cash));
		if(date.after(dCreation)){
			System.out.println(" rend. " + rendement +"%");
		}else{
			System.out.println();
		}
		System.out.println("-------------------------------------------------------------------------");
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
				"select sum(amount) as cash from movements where id_portfolio = %s and date <= '%s'"
				/*+ " and type in (3,4,5)"*/,id_portfolio,date);
		stmt = conn.createStatement();
		rs = stmt.executeQuery(req);
		rs.next();
		sum=sum.add(rs.getBigDecimal("cash"));
		req = String.format(
				"select sum(amount) as invest from movements where id_portfolio = %s and date <= '%s'"
						+ " and type = 0",id_portfolio,date);
		//				System.out.println(req);
		stmt = conn.createStatement();
		rs = stmt.executeQuery(req);
		rs.next();
		double rendement =(sum.divide(rs.getBigDecimal("invest"),6,BigDecimal.ROUND_HALF_EVEN)).floatValue();
		return Math.round((Math.pow(rendement,365./getNberDaysBetween(dCreation, date))-1.)*10000.)/100.;
	}

	static BigDecimal getInEur(Date date,int id_stock,BigDecimal amount,int dec) throws SQLException{
		String req = String.format("select currencystock('%s',%s) as rate",date,id_stock);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(!rs.next())return new BigDecimal(0.);
		return amount.divide(rs.getBigDecimal("rate"),dec,BigDecimal.ROUND_UP);
	}

	@SuppressWarnings("deprecation")
	public void setdCreation(String creation) {
		this.dCreation = Date.valueOf(creation);
		dCreation.setDate(1); // begin of month
	}

	public void setdFin(String dFin) {
		this.dFin = Date.valueOf(dFin);
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

	public void setBank(int bank) {
		this.bank = bank;
	}

	public Vector<Stock> getSaveVectorActiveStocks() {
		return saveVectorActiveStocks;
	}

	private void loadingPortfolio(Date creation, String csvFile) throws SQLException {
		Vector<Stock> vectorPurchaseStocks = new Vector<Stock>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] ligne = line.split(cvsSplitBy);
				Stock s = new Stock(creation,Integer.parseInt(ligne[1].trim()));
				s.quantity=Integer.parseInt(ligne[2].trim());
				vectorPurchaseStocks.add(s);
			}
			stocksPurchase(creation,vectorPurchaseStocks);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void loadingPortfolio(Date creation, Vector<Stock> saveVectorActiveStocks) throws SQLException, IOException {
		Iterator<Stock> itr = saveVectorActiveStocks.iterator();
		double samount = 0.;
		while(itr.hasNext()){
			Stock s =itr.next();
			samount=samount + s.amount.doubleValue();
		}
		itr = saveVectorActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s =itr.next();
			System.out.print(s.id_stock + " " + s.quantity + " -> ");
			s.quantity= (int) Math.round((1000000.*s.quantity)/samount);
			System.out.println(" "+s.quantity);
		}
		stocksPurchase(creation,saveVectorActiveStocks);
	}

	public void savePortfolio() throws IOException, SQLException {
		String csvFile = "C:/Users/claude/Desktop/R folder/downloads/outputPortfolio.csv";
		FileWriter writer = new FileWriter(csvFile);
		saveVectorActiveStocks=getVectorActiveStocks(dFin);
		this.printVectorStocks("Saved Portfolio", dFin, saveVectorActiveStocks);
		Iterator<Stock> itr = saveVectorActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s =itr.next();
			CSVUtils.writeLine(writer,
					Arrays.asList(String.valueOf(id_portfolio),String.valueOf(s.id_stock),
							String.valueOf(s.quantity),String.valueOf(s.quoteEur)));
		}
		writer.flush();
		writer.close();

	}

}