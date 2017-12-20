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
	public double sellThreshold = .66;
	public double purchaseThreshold = .95;
	public int maxMonth = 12;

	public Vector <Stock> vectorActiveStocks;
	public Vector <Stock> vectorSellStocks;
	public Vector <Stock> vectorNotActiveStocks;
	public Vector <Stock> vectorPurchaseStocks;

	Portfolio portfolio;

	BigDecimal maxValueStock;
//	BigDecimal minValueStock;

	public void initPortfolio(BigDecimal cash, Date creation) throws SQLException, IOException{

		BigDecimal inv_by_stock=initData(cash,creation);
//
		Vector<Stock> vectorPurchaseStocks = new Vector<Stock>();
		Statement stmt = Portfolio.conn.createStatement();
		String req = String.format("SELECT id FROM stocks where actived and firstquote <= '%s' order by id",creation);
		//        System.out.println(req);
		ResultSet rs = stmt.executeQuery(req);
		while (rs.next()) {
			int id_stock = rs.getInt("id");
			Stock s = new Stock(creation,id_stock);
			s.perf = perfStockForPurchaseInit(creation, portfolio.id_portfolio, s);
			//			savePerformance(creation,"P",id_stock,s.perf);
			vectorPurchaseStocks.add(s);
		}
		Stock.sortBy=0;;
		Collections.sort(vectorPurchaseStocks);
		//		System.out.print("D " + creation +" Ac. achet�s ");
		int countNbStocks = 0;
		for(Stock s : vectorPurchaseStocks){
			countNbStocks=countNbStocks+1;
			if(countNbStocks > maxStocks){
// ???				countNbStocks=countNbStocks-1;
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
//??		for(int i = countNbStocks;i<=dim;i++)vectorPurchaseStocks.remove(i-1);
		portfolio.stocksPurchase(creation,vectorPurchaseStocks);
		return;
	}

	public BigDecimal initData(BigDecimal cash, Date creation) {
		BigDecimal inv_by_stock = cash.divide(new BigDecimal(maxStocks),2,RoundingMode.HALF_DOWN);
		maxValueStock = inv_by_stock.multiply(BigDecimal.valueOf(2.));
		Calendar c = Calendar.getInstance();
		c.setTime(creation);
		c.add(Calendar.MONTH, this.firstArbitrationMonth);  // number of months to add
		endArbitration = new java.sql.Date(c.getTimeInMillis());
		return inv_by_stock;
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
//		int nbToPurchase = Math.max(0, maxStocks - vectorActiveStocks.size());
		BigDecimal cash = portfolio.getCash(currentDay);
		BigDecimal inv_by_stock = cash.multiply(BigDecimal.valueOf(.1));
		if(vectorActiveStocks.size()>0){
			BigDecimal valueStocks = BigDecimal.valueOf(0.);
			Iterator<Stock> itr = vectorActiveStocks.iterator();
			while(itr.hasNext()){
				Stock s =itr.next();
				valueStocks=valueStocks.add(s.amount);
			}
			valueStocks = valueStocks.add(cash); // valeur du pf avec cash
			maxValueStock = valueStocks.multiply(BigDecimal.valueOf(0.1)); //10% du pf
			Stock.sortBy = 1; // montant d�croissant
			Collections.sort(vectorActiveStocks); // sort sur amount d�croissant
			Stock stockTooBig = vectorActiveStocks.lastElement(); // action le + important
			if(stockTooBig.amount.compareTo(maxValueStock) > 0)
				vectorActiveStocks.remove(stockTooBig); // pr�paration � la r�duct. de l'invest.
			else stockTooBig = null; //totalit� est conserv�
			itr = vectorActiveStocks.iterator();
			int nbPossibleToSell=0;
			while(itr.hasNext()){
				Stock s =itr.next();
				if(s.perf < this.sellThreshold)nbPossibleToSell=nbPossibleToSell+1;
			}
			System.out.println("Nbre possible de ventes sous le seuil de "+sellThreshold+" = "+nbPossibleToSell);
			if(vectorActiveStocks.size() > 0){
				Stock.sortBy=0;// d'abord le - performant
				Collections.sort(vectorActiveStocks); 
				Stock stockTooSmall = vectorActiveStocks.firstElement();
				if(stockTooSmall.perf < this.sellThreshold){ 
					vectorActiveStocks.remove(stockTooSmall); //�jection si en dessous d'un seuil
				}
				else stockTooSmall=null;
				if(vectorActiveStocks.size() > 0){
					itr = vectorActiveStocks.iterator();
					vectorSellStocks = new Vector<Stock>();
					while(itr.hasNext()){
						Stock s =itr.next();
						if(s.since >= this.minimumInPortfolio){
							s.perf=perfStockForSell(currentDay, portfolio.id_portfolio,s);
							vectorSellStocks.add(s);
						}
					}
					
					if(vectorSellStocks.size() > 0){
						Stock.sortBy=2; //d'abord les + performant
						Collections.sort(vectorSellStocks);
						Stock stockToSellHigh=vectorSellStocks.firstElement(); // prendre le plus performant
						stockToSellHigh.sellType = 2; // croissance + forte
						vectorSellStocks.clear();
						vectorSellStocks.add(stockToSellHigh);
						vectorActiveStocks.remove(stockToSellHigh);
					}
					if(stockTooBig != null){
						int qty=stockTooBig.quantity;
						stockTooBig.quantity=stockTooBig.quantity/2;
						vectorSellStocks.add(stockTooBig);
						stockTooBig.quantity=qty-stockTooBig.quantity;	
						stockTooBig.sellType=3; // 1/2 vendu
						vectorActiveStocks.add(stockTooBig);
					}
					if(stockTooSmall != null){
						stockTooSmall.quantity=99999;
						vectorSellStocks.add(stockTooSmall);
						stockTooSmall.sellType=1; // + dessous du seuil
						vectorActiveStocks.remove(stockTooSmall);
					}
					
					if(vectorActiveStocks.size() > 0){
						Stock.sortBy=2; // perf achat/act. + -> -
						Collections.sort(vectorActiveStocks);
						itr = vectorActiveStocks.iterator();
						while(itr.hasNext()){
							Stock s =itr.next();
							if(s.since < maxMonth)continue;
							s.sellType=4;
							vectorSellStocks.add(s);
							vectorActiveStocks.remove(s);
							break;			
						}
					}
							
					if(vectorSellStocks.size() == 0){
						System.out.println("rien � vendre");
						return;		
					}
					
					portfolio.stocksSell(currentDay,vectorSellStocks);
					cash = portfolio.getCash(currentDay);
				}
			}
		}
		
		// ACHATS
		Iterator<Stock> itr=vectorNotActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s =itr.next();
			s.perf=perfStockForPurchase(currentDay, portfolio.id_portfolio,s);
//			System.out.println("--->"+s.code+" "+s.perf);
		}
		// Ne pas acheter pour moins de 5000 euro en cash
		int nbToPurchase =vectorSellStocks.size();
		
		if(this.maxStocks>=vectorActiveStocks.size()+nbToPurchase){
			nbToPurchase=maxStocks-vectorActiveStocks.size();		
		}
		
		int nbToPurchaseMax = cash.divide(BigDecimal.valueOf(5000.),0,RoundingMode.HALF_DOWN).intValue();
		nbToPurchase = Math.min(nbToPurchaseMax,nbToPurchase);
		
		Stock.sortBy=0;
		Collections.sort(vectorNotActiveStocks); // moins perfo.
		itr = vectorNotActiveStocks.iterator();
		int nbPossibleToPurchase = 0;
		while(itr.hasNext()){
			Stock s = itr.next();
			if(s.perf > purchaseThreshold)break;
			nbPossibleToPurchase= nbPossibleToPurchase+1;
		}
		System.out.println("Nbre possible d'achats sous le seuil de "+purchaseThreshold
				+" = "+nbPossibleToPurchase);
		nbToPurchase = Math.min(nbPossibleToPurchase,nbToPurchase);
		
		if(nbToPurchase == 0){
			System.out.println("PAS D'ACHAT !!!");
			return;
		}
		inv_by_stock = cash.divide(new BigDecimal(nbToPurchase),2,RoundingMode.HALF_DOWN);
		inv_by_stock=inv_by_stock.min(this.maxValueStock); // max 10% port + cash

		vectorPurchaseStocks = new Vector<Stock>();
		itr = vectorNotActiveStocks.iterator();
		while(itr.hasNext()){
			Stock s = itr.next();
			if(s.perf > purchaseThreshold)break;
			//			System.out.println("---> "+s.code+" "+s.perf+" "+Math.round((1./s.perf - 1.)*10000.)/100.);
			s.quantity = inv_by_stock.divide(s.quoteEur,2,RoundingMode.HALF_DOWN).intValue();
			vectorPurchaseStocks.add(s);
			vectorActiveStocks.add(s);	
			nbToPurchase=nbToPurchase-1;
			if(nbToPurchase==0)break;
		}
		if(vectorPurchaseStocks.size()>0)portfolio.stocksPurchase(currentDay,vectorPurchaseStocks);
	}

//	private void saveSellPerf(Date date, Stock s) throws SQLException {
//		String req = String.format("INSERT INTO sellperf (date,id_portfolio,id_stock,perf,since) "
//				+ "VALUES ('%s',%s,%s,%s,%s)",
//				date,portfolio.id_portfolio,s.id_stock,s.perf,s.since);
////			    System.out.println(req);
//		Statement stm = Portfolio.conn.createStatement();
//		stm.executeUpdate(req);	
//	}

	public abstract double perfStockForSell(Date currentDay, int portfolio, Stock s) throws SQLException;// throws SQLException {	

	public abstract double perfStockForPurchase(Date currentDay, int portfolio, Stock s) throws SQLException;// throws SQLException {

	public abstract double perfStockForPurchaseInit(Date currentDay, int portfolio, Stock s) throws SQLException;// throws SQLException {

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

	public void setSellThreshold(double sellThreshold) {
		this.sellThreshold = sellThreshold;
	}

	public void setPurchaseThreshold(double purchaseThreshold) {
		this.purchaseThreshold = purchaseThreshold;
	}

	public void setMaxMonth(int maxMonth) {
		this.maxMonth = maxMonth;
	}

}
