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
		politic.setMinimumInPortfolio(3);
		// ------------------------------------------------------------
		Portfolio portfolio = new Portfolio();
		for(int i = 8;i<12;i++){
			politic.setSeed(seed);
			portfolio.setPolitic(politic);
			// nouveau .006
			portfolio.setCommission(0.006);
			portfolio.setdCreation("2007-01-01");
			portfolio.setdFin("2017-09-15");
			portfolio.setStartCash(BigDecimal.valueOf(1000000.));
			portfolio.setBank(Portfolio.BINCKBANCK);
			portfolio.setName(String.format("%s BINCKBANK RANDOM n° %s min. en port. %s mois",d,i,politic.minimumInPortfolio));
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
