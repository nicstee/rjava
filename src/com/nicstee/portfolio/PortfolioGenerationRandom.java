package com.nicstee.portfolio;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PortfolioGenerationRandom {

	public static void main(String[] args) throws SQLException, IOException {
		//
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		String d = dateFormat.format(c.getTime());
		long seed = 10;
		PoliticRandom politic = new PoliticRandom();
		politic.setMaxStocks(20);
		politic.setArbitrationDay(3);
		politic.setFirstArbitrationMonth(3);
//		politic.setMinimumInPortfolio(0);
		politic.setMinimumInPortfolio(570);
		// ------------------------------------------------------------
		Portfolio portfolio = new Portfolio();
		for(int i = 0;i<12;i++){
			politic.setSeed(seed);
			portfolio.setPolitic(politic);
			portfolio.setCommission(0.002);
			portfolio.setdCreation("2007-01-01");
			portfolio.setdFin("2017-09-15");
			portfolio.setStartCash(BigDecimal.valueOf(1000000.));
			portfolio.setName(String.format("%s essaie n° %s",d,i));
			// START
			System.out.println("Start "+portfolio.name);
			portfolio.generationPortfolio();
			//---------------------------------------------------------------
			// NEXT
			seed = seed*10 + 1;
			portfolio = new Portfolio();
		}
		System.out.println("END");
	}
}
