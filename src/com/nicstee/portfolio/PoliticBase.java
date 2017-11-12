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
import java.util.Iterator;
import java.util.Vector;




public abstract class PoliticBase implements Politic{

	public int minimumInPortfolio = 0; // en mois
	public int arbitrationDay = 3;
	public int maxStocks = 12;
	public int firstArbitrationMonth = 3;
	public Date endArbitration = Date.valueOf("2017-09-15");
	public int arbitrationCycle = 1;
	public int perfPeriodForPurchase = 30; // en jours

	public Vector <Stock> vectorActiveStocks;
	public Vector <Stock> vectorSellStocks;
	public Vector <Stock> vectorNotActiveStocks;
	public Vector <Stock> vectorPurchaseStocks;

	Portfolio portfolio;

	BigDecimal maxValueStock;
	BigDecimal minValueStock;

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
			Stock s = new Stock(creation,id_stock);
			s.perf = perfStockForPurchase(creation, portfolio.id_portfolio, s);
			//			savePerformance(creation,"P",id_stock,s.perf);
			vectorPurchaseStocks.add(s);
		}
		BigDecimal inv_by_stock = cash.divide(new BigDecimal(maxStocks),2,RoundingMode.HALF_DOWN);
		maxValueStock = inv_by_stock.multiply(BigDecimal.valueOf(2.));
		minValueStock = inv_by_stock.multiply(BigDecimal.valueOf(.66));
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
		return;
	}

	@SuppressWarnings("deprecation")
	public Vector<Stock> arbitrationStocks(Date currentDay) throws SQLException, IOException{
		if(currentDay.before(endArbitration))return null;
		if(currentDay.getDate() != arbitrationDay)return null;
		if(currentDay.getMonth()%arbitrationCycle != 0)return null;
		vectorActiveStocks = portfolio.getVectorActiveStocks(currentDay); // sort by amount
		vectorNotActiveStocks = portfolio.getVectorNotActiveStocks(currentDay);
		arbitration(currentDay);
		return vectorActiveStocks;
	}

	private void arbitration(Date currentDay) throws SQLException, IOException{
		BigDecimal valueStocks = BigDecimal.valueOf(0.);
		Iterator<Stock> itr = vectorActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s =itr.next();
			s.sortBy=1;
			valueStocks=valueStocks.add(s.amount);
		}
		maxValueStock = valueStocks.multiply(BigDecimal.valueOf(0.1));
		Collections.sort(vectorActiveStocks); // sort sur amount 
		Stock stockTooBig = vectorActiveStocks.lastElement();
		if(stockTooBig.amount.compareTo(maxValueStock) > 0)
			vectorActiveStocks.remove(stockTooBig);
		else stockTooBig = null;
		itr = vectorActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s =itr.next();
			s.sortBy=0;// pour sort sur perf
			s.perf=(s.amount.divide(s.amountPurchase,2,RoundingMode.HALF_DOWN)).floatValue();
		}
		Collections.sort(vectorActiveStocks); // sort sur amount 
		Stock stockTooSmall = vectorActiveStocks.firstElement();
		if(stockTooSmall.perf < 0.66)
			vectorActiveStocks.remove(stockTooSmall);
		else stockTooSmall=null;
		itr = vectorActiveStocks.iterator();
		vectorSellStocks = new Vector<Stock>();
		while(itr.hasNext()){
			Stock s =itr.next();
			if(s.since >= this.minimumInPortfolio){
				s.perf=perfStockForSell(currentDay, portfolio.id_portfolio,s);
				vectorSellStocks.add(s);
			}
		}
		if(! vectorSellStocks.isEmpty()){
			try{
				Collections.sort(vectorSellStocks);
			}catch(Exception e ){
				portfolio.printVectorStocks("???",currentDay, vectorSellStocks);
				System.exit(9);
			}
			Stock stockToSellHigh=vectorSellStocks.firstElement();
			vectorSellStocks.clear();
			vectorSellStocks.add(stockToSellHigh);
			vectorActiveStocks.remove(stockToSellHigh);
		}
		if(stockTooBig != null){
			stockTooBig.quantity=stockTooBig.quantity/2;
			vectorSellStocks.add(stockTooBig);
		}
		if(stockTooSmall != null){
			stockTooSmall.quantity=99999;
			vectorSellStocks.add(stockTooSmall);
			vectorActiveStocks.remove(stockTooSmall);
		}
		if(vectorSellStocks.isEmpty())return;		
		portfolio.stocksSell(currentDay,vectorSellStocks);
		//		choix pour achats
		itr=vectorNotActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s =itr.next();
			s.sortBy=0; // pour sort sur perf
			s.perf=perfStockForPurchase(currentDay, portfolio.id_portfolio,s);
		}
		Collections.sort(vectorNotActiveStocks); // moins perfo.
		//		A FAIRE  APRES LES VENTES SINON PAS DE CASH
		// Ne pas acheter pour moins de 5000 euro en cash
		BigDecimal cash = portfolio.getCash(currentDay);
		int nbToPurchase =vectorSellStocks.size();
		int nbToPurchaseMax = cash.divide(BigDecimal.valueOf(5000.),0,RoundingMode.HALF_DOWN).intValue();
//		portfolio.startCash.multiply(BigDecimal.valueOf(.005));
		nbToPurchase = Math.min(nbToPurchaseMax,nbToPurchase);
		if(nbToPurchase == 0){
			System.out.println("PAS D'ACHAT !!!");
			return;
		}
		if(nbToPurchaseMax < nbToPurchase){
			System.out.println("NBRE D'ACHATS REDUIT DE " +nbToPurchaseMax+" A "+nbToPurchase);
		}	
		BigDecimal inv_by_stock = portfolio.getCash(currentDay).divide(new BigDecimal(nbToPurchase),2,RoundingMode.HALF_DOWN);
		vectorPurchaseStocks = new Vector<Stock>();
		itr = vectorNotActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s = itr.next();
			s.quantity = inv_by_stock.divide(s.quoteEur,2,RoundingMode.HALF_DOWN).intValue();
			vectorPurchaseStocks.add(s);
			vectorActiveStocks.add(s);	
			nbToPurchase=nbToPurchase-1;
			if(nbToPurchase==0)break;
		}
		portfolio.stocksPurchase(currentDay,vectorPurchaseStocks);
	}

	public abstract double perfStockForSell(Date currentDay, int portfolio, Stock s) throws SQLException;// throws SQLException {	

	public abstract double perfStockForPurchase(Date currentDay, int portfolio, Stock s) throws SQLException;// throws SQLException {


	public void setPortfolio(Portfolio portefeuille) {
		this.portfolio = portefeuille;
	}

	public void setMinimumInPortfolio(int minimumInPortfolio) {
		if(this.minimumInPortfolio <= firstArbitrationMonth)
			this.minimumInPortfolio = minimumInPortfolio;
		else this.minimumInPortfolio =this.firstArbitrationMonth;
	}

	public void setArbitrationDay(int arbitrationDay) {
		this.arbitrationDay = arbitrationDay;
	}

	public void setMaxStocks(int maxStocks) {
		this.maxStocks = maxStocks;
	}

	public void setFirstArbitrationMonth(int firstArbitrationMonth) {
		this.firstArbitrationMonth = firstArbitrationMonth;
	}

	public void setEndArbitration(Date endArbitration) {
		this.endArbitration = endArbitration;
	}

	public void setArbitrationCycle(int arbitrationCycle){
		this.arbitrationCycle = arbitrationCycle;
	}

	public void setPerfPeriodForPurchase( int perfPeriodForPurchase){
		this.perfPeriodForPurchase = perfPeriodForPurchase;
	}

}
