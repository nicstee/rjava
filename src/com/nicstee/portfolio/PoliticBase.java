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

public class PoliticBase implements Politic{

	Portfolio portfolio;
	int arbitrationDay;
	int maxStocks;
	int firstArbitrationMonth;
	Date endArbitration;
	Random rd;

	public PoliticBase(int maxStocks, int nbMonthToStartArbitration, int monthDayForArbitration) {
		this.maxStocks = maxStocks;
		this.firstArbitrationMonth = nbMonthToStartArbitration;
		this.arbitrationDay = monthDayForArbitration;
		rd = new Random();
		rd.setSeed(10);	
	}

	public void initPortfolio(BigDecimal cash, Date creation) throws SQLException, IOException{
		Vector<Stock> vectorPurchaseStocks = new Vector<Stock>();
		Calendar c = Calendar.getInstance();
		c.setTime(creation);
		c.add(Calendar.MONTH, this.firstArbitrationMonth);  // number of days to add
		endArbitration = new java.sql.Date(c.getTimeInMillis());
		Statement stmt = Portfolio.conn.createStatement();
		String req = "SELECT id FROM stocks where actived order by id";
		//        System.out.println(req);
		ResultSet rs = stmt.executeQuery(req);
		while (rs.next()) {
			int id_stock = rs.getInt("id");
			vectorPurchaseStocks.add(
			new Stock(creation,id_stock,0,this.perfStockForPurchase(creation,id_stock)));
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
			portfolio.stocksPurchase(id_stock, creation, s);
		}
		System.out.println("");
		int dim = vectorPurchaseStocks.size();	
		for(int i = countNbStocks;i<=dim;i++)vectorPurchaseStocks.remove(countNbStocks-1);
		portfolio.stocksPurchase(creation,vectorPurchaseStocks);
		System.out.print("valeur du portefeuille " + portfolio.portfolioValue(creation));
		System.out.println(" cash " + portfolio.getCash(creation)+"\n");
		portfolio.printPortfolio(creation);
		return;
	}

	public void arbitrationStocks(Portfolio portfolio, Date currentDay) throws SQLException, IOException{
		if(currentDay.before(endArbitration))return;
		if(currentDay.getDate() != arbitrationDay)return;
		Vector<Stock> vectorPurchaseStocks = new Vector<Stock>();
		Vector<Stock> vectorSellStocks = new Vector<Stock>();
		//		System.out.print("D "+ currentDay);
		System.out.println("");
		// Ventes
		ResultSet rsActives = portfolio.getActiveStocks(currentDay);
		ResultSet rsNotActives = portfolio.getNotActiveStocks(currentDay);
		while(rsActives.next()){
			int id_stock=rsActives.getInt("id_stock");
			vectorSellStocks.add(new Stock(currentDay,id_stock,0,perfStockForSell(currentDay,id_stock)));
		}		
		Collections.sort(vectorSellStocks);
		Stock stockToSell=vectorSellStocks.firstElement();
		stockToSell.quantity=9999;
		vectorSellStocks.clear();
		vectorSellStocks.add(stockToSell);
		portfolio.stocksSell(currentDay,vectorSellStocks);
//			System.out.print(" valeur du portefeuille " + portfolio.portfolioValue(currentDay));
//			System.out.println(" cash EUR " + portfolio.getCash(currentDay));
		// Achats
		while(rsNotActives.next()){
			int id_stock=rsNotActives.getInt("id_stock");
			vectorPurchaseStocks.add(new Stock(currentDay,id_stock,0,perfStockForPurchase(currentDay,id_stock)));
		}
		Collections.sort(vectorPurchaseStocks);
		Stock stockToPurchase=vectorPurchaseStocks.firstElement();
		stockToPurchase.quantity=9999;
		vectorPurchaseStocks.clear();
		vectorPurchaseStocks.add(stockToPurchase);
		portfolio.stocksPurchase(currentDay,vectorPurchaseStocks);
//		System.out.println("Actions Achetées le "+ currentDay);		
//		if (vectorPurchaseStocks.size() > 0 ){
//			Stock s = vectorPurchaseStocks.firstElement();
//			s.quantity=99999;
//			portfolio.stocksPurchase(s.id_stock,currentDay,s);
//			System.out.print(" " + s.id_stock);
//			System.out.print(" " + s.code);
//			System.out.print(" quantité "+ s.quantity);
//			System.out.print(" cotation EUR "+ s.quoteEur);
//			System.out.print(" montant EUR "+ s.amount);
//			System.out.println(" coût EUR "+ s.cost);
//			System.out.print(" valeur EUR du portefeuille " + portfolio.portfolioValue(currentDay));
//			System.out.println(" cash EUR " + portfolio.getCash(currentDay)+"\"");
//		}
		portfolio.printPortfolio(currentDay);
		System.out.println("");


	}

	double perfStockForSell(Date currentDay, int id_stock) {	
		return rd.nextDouble();
	}

	double perfStockForPurchase(Date currentDay, int id_stock) {
		return rd.nextDouble();
	}

	public void setPortfolio(Portfolio portefeuille) {
		this.portfolio = portefeuille;
	}

}
