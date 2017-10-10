package com.nicstee.portfolio;

import java.io.IOException;
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

public abstract class PoliticBase implements Politic{

	Portfolio portfolio;
	int arbitrationDay;
	int maxStocks;
	int firstArbitrationMonth;
	Date endArbitration;
	Random rd;

	public PoliticBase(long seed,int maxStocks, int nbMonthToStartArbitration, int monthDayForArbitration) {
		this.maxStocks = maxStocks;
		this.firstArbitrationMonth = nbMonthToStartArbitration;
		this.arbitrationDay = monthDayForArbitration;
		rd = new Random();
		rd.setSeed(seed);
	}

	public void initPortfolio(BigDecimal cash, Date creation) throws SQLException, IOException{
		Vector<Stock> vectorPurchaseStocks = new Vector<Stock>();
		Calendar c = Calendar.getInstance();
		c.setTime(creation);
		c.add(Calendar.MONTH, this.firstArbitrationMonth);  // number of days to add
		endArbitration = new java.sql.Date(c.getTimeInMillis());
		Statement stmt = Portfolio.conn.createStatement();
		String req = String.format("SELECT id FROM stocks where actived and firstquote <= '%s' order by id",creation);
		//        System.out.println(req);
		ResultSet rs = stmt.executeQuery(req);
		while (rs.next()) {
			int id_stock = rs.getInt("id");
			Stock s = new Stock(creation,id_stock,0);
			s.perf = perfStockForPurchase(creation,id_stock);
			vectorPurchaseStocks.add(s);
		}
		BigDecimal inv_by_stock = cash.divide(new BigDecimal(maxStocks),2,RoundingMode.HALF_DOWN);
		Collections.sort(vectorPurchaseStocks);
		//		System.out.print("D " + creation +" Ac. achetés ");
		int countNbStocks = 0;
		for(Stock s : vectorPurchaseStocks){
			countNbStocks=countNbStocks+1;
			if(countNbStocks > maxStocks){
				break;
			}
			int id_stock = s.id_stock;
			if(s.quoteEur.compareTo(new BigDecimal(0.))<=0){
				System.out.println("pas de cotation pour "+ id_stock);
				continue;
			}
			s.quantity = inv_by_stock.divide(s.quoteEur,2,RoundingMode.HALF_DOWN).intValue();
		}
		System.out.println("");
		int dim = vectorPurchaseStocks.size();	
		for(int i = countNbStocks;i<=dim;i++)vectorPurchaseStocks.remove(countNbStocks-1);
		portfolio.stocksPurchase(creation,vectorPurchaseStocks);
		portfolio.printPortfolio(creation);
		return;
	}

	public void arbitrationStocks(Portfolio portfolio, Date currentDay) throws SQLException, IOException{
		if(currentDay.before(endArbitration))return;
		if(currentDay.getDate() != arbitrationDay)return;
		Vector<Stock> vectorSellStocks = portfolio.getVectorActiveStocks(currentDay);
		java.util.Iterator<Stock> itr = vectorSellStocks.iterator();
		 while(itr.hasNext()){
			 Stock s =itr.next();
			 s.perf=perfStockForSell(currentDay,s.id_stock);		 
		 }
		Collections.sort(vectorSellStocks);
		Stock stockToSell=vectorSellStocks.firstElement();
		stockToSell.quantity=9999;
		vectorSellStocks.clear();
		vectorSellStocks.add(stockToSell);
		portfolio.stocksSell(currentDay,vectorSellStocks);
//		choix pour achats
		 Vector<Stock> vectorPurchaseStocks = portfolio.getVectorNotActiveStocks(currentDay);
		 itr=vectorPurchaseStocks.iterator();
			 while(itr.hasNext()){
				 Stock s =itr.next();
				 s.perf=perfStockForPurchase(currentDay,s.id_stock);		 
			 }
		Collections.sort(vectorPurchaseStocks);
		Stock stockToPurchase=vectorPurchaseStocks.firstElement();
		stockToPurchase.quantity=9999;
		vectorPurchaseStocks.clear();
		vectorPurchaseStocks.add(stockToPurchase);
		portfolio.stocksPurchase(currentDay,vectorPurchaseStocks);
//		impression
		portfolio.printPortfolio(currentDay);
		System.out.println("");
	}

	public abstract double perfStockForSell(Date currentDay, int id_stock) throws SQLException;// throws SQLException {	

	public abstract double perfStockForPurchase(Date currentDay, int id_stock) throws SQLException;// throws SQLException {

	public void setPortfolio(Portfolio portefeuille) {
		this.portfolio = portefeuille;
	}

}
