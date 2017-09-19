package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

public class PoliticBase implements Politic{
	
	int id_portfolio;
	Portfolio portfolio;
	int arbitrationDay;
	
	public void init(BigDecimal cash, Date creation) throws SQLException{
        String req = "SELECT count(*) FROM stocks";
//        System.out.println(req);
        Statement stmt = Portfolio.conn.createStatement();
        ResultSet rs = stmt.executeQuery(req);
        rs.next();
        int nbStocks = rs.getInt(1);
//        System.out.println("stocks number added = " + nbStocks);
        if(nbStocks == 0)return;      
        req = "SELECT * FROM stocks";
//        System.out.println(req);
        rs = stmt.executeQuery(req);
        while (rs.next()) {
        	int id_stock = rs.getInt("id");
        	BigDecimal quote = Portfolio.quote(creation, id_stock);
        	int quantite = cash.divide(quote,2,RoundingMode.HALF_EVEN)
        			.divide(new BigDecimal(nbStocks),2,RoundingMode.HALF_EVEN).intValue();
        	portfolio.stocksPurchase(id_stock, creation, quantite);	
        }
        portfolio.movement(creation, cash, "Initial investment");
	    return;
	}

	public void setPortfolio(Portfolio portefeuille) {
		this.portfolio = portefeuille;
		this.id_portfolio = portefeuille.id_portfolio;
	}


	public void setArbitrationDay(int arbitrationDay) {
		this.arbitrationDay = arbitrationDay;
	}

	@Override
	public int getArbitrationDay() {
		return arbitrationDay;
	}
	
}
