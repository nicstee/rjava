package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

public class PoliticBase implements Politic{
	
	Portfolio portfolio;
	int arbitrationDay;
	int maxStocks;
	int firstArbitrationMonth;
	Date beginArbitration;
	Random rd;
		
	public PoliticBase(int maxStocks, int nbMonthToStartArbitration, int monthDayForArbitration) {
		this.maxStocks = maxStocks;
		this.firstArbitrationMonth = nbMonthToStartArbitration;
		this.arbitrationDay = monthDayForArbitration;
		rd = new Random();
		rd.setSeed(10);	
	}

	public void initPortfolio(BigDecimal cash, Date creation) throws SQLException{
		Vector<Stock> vectorPurchaseStocks = new Vector<Stock>();
		Calendar c = Calendar.getInstance();
		c.setTime(creation);
		c.add(Calendar.MONTH, this.firstArbitrationMonth);  // number of days to add
		beginArbitration = new java.sql.Date(c.getTimeInMillis());
		Statement stmt = Portfolio.conn.createStatement();
        String req = "SELECT * FROM stocks order by id";
//        System.out.println(req);
        ResultSet rs = stmt.executeQuery(req);
        while (rs.next()) {
			int id_stock=rs.getInt("id");
			vectorPurchaseStocks.add(new Stock(id_stock,0,this.perfStockForPurchase(creation,id_stock)));
        }
		BigDecimal inv_by_stock = cash.divide(new BigDecimal(maxStocks),2,RoundingMode.HALF_DOWN);
		Collections.sort(vectorPurchaseStocks);
		System.out.print("D " + creation +" Ac. achetés ");
        int countNbStocks = 0;
		for(Stock s : vectorPurchaseStocks){
        	countNbStocks=countNbStocks+1;
			int id_stock = s.id_stock;
			BigDecimal quote = Portfolio.quote(creation, id_stock);
        	if(quote.compareTo(new BigDecimal(0.))<=0)continue;
        	int quantite = inv_by_stock.divide(quote,2,RoundingMode.HALF_DOWN).intValue();
        	portfolio.stocksPurchase(id_stock, creation, quantite);
			System.out.print(" "+s.id_stock);
        	if(countNbStocks > maxStocks)break;
 		}
		System.out.println(" ");
	    return;
	}
	
	public void arbitrationStocks(Portfolio portfolio, Date currentDay) throws SQLException{
		if(currentDay.before(beginArbitration))return;
		if(currentDay.getDate() != arbitrationDay)return;
		Vector<Stock> vectorPurchaseStocks = new Vector<Stock>();
		Vector<Stock> vectorSellStocks = new Vector<Stock>();
		System.out.print("D "+ currentDay);
	// Ventes
		ResultSet rsActives = portfolio.getActiveStocks(currentDay);
		ResultSet rsNotActives = portfolio.getNotActiveStocks(currentDay);
		while(rsActives.next()){
			int id_stock=rsActives.getInt("id_stock");
			vectorSellStocks.add(new Stock(id_stock,0,perfStockForSell(currentDay,id_stock)));
		}		
		Collections.sort(vectorSellStocks);
		System.out.print(" Actives");
		for(Stock s : vectorSellStocks)System.out.print(" "+s.id_stock);
		System.out.print(" vendues");		
		if (vectorSellStocks.size() > 0 ){
			Stock s = vectorSellStocks.firstElement();
			System.out.print(" "+s.id_stock);
			portfolio.stocksSell(s.id_stock,currentDay,99999);
		}
		System.out.println(" ");
	// Achats
		while(rsNotActives.next()){
			int id_stock=rsNotActives.getInt("id_stock");
			vectorPurchaseStocks.add(new Stock(id_stock,0,perfStockForPurchase(currentDay,id_stock)));
		}
		Collections.sort(vectorPurchaseStocks);
		System.out.print(" Not Actives");
		for(Stock s : vectorPurchaseStocks)System.out.print(" "+s.id_stock);
		System.out.print(" Achetées");		
		if (vectorPurchaseStocks.size() > 0 ){
			Stock s = vectorPurchaseStocks.firstElement();
			System.out.print(" "+s.id_stock + "< ");
			portfolio.stocksPurchase(s.id_stock,currentDay,99999);
		}
		System.out.println("");
	}
	
	private double perfStockForSell(Date currentDay, int id_stock) {	
		return rd.nextDouble();
	}

	private double perfStockForPurchase(Date currentDay, int id_stock) {
		return rd.nextDouble();
	}

	public void setPortfolio(Portfolio portefeuille) {
		this.portfolio = portefeuille;
	}
	
}
