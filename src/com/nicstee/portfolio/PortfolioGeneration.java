package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;

public class PortfolioGeneration {

	public static void main(String[] args) throws SQLException {

		Politic politic = new PoliticBase();
		politic.setArbitrationDay(3);		
		Portfolio portfolio = new Portfolio();
		portfolio.setPolitic(politic);
//		Portfolio.setConn();
//		portfolio.stocksGeneration(Date.valueOf("2017-01-01"));
//		portfolio.DividendsLoading(Date.valueOf("2017-01-16"), 5);
		
		portfolio.calculate("essai1",BigDecimal.valueOf(1000000.),Date.valueOf("2016-01-15"));
		Portfolio.close();
		System.out.println("END");
	}
}
