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
		PoliticRandom politic = null;
		for(int i = 0;i<12;i++){
			politic = new PoliticRandom();
			
			politic.setSeed(seed);
			politic.setMaxStocks(20);
			politic.setArbitrationDay(3);
			politic.setFirstArbitrationMonth(999); // pas d'arbitrage
			politic.setMinimumInPortfolio(0);

			Portfolio portfolio = new Portfolio();
			portfolio.setPolitic(politic);
			portfolio.setdCreation("2016-09-01");
			portfolio.setdFin("2017-09-15");
			portfolio.setStartCash(BigDecimal.valueOf(1000000.));
			portfolio.setBank(Portfolio.BINCKBANCK);
			portfolio.setName(String.format("%s ING RAND. (%sa,%sb) n° %s,nb.actions %s, min. en port. %sm, 1er arb %sm, cy.arb. %sm",
					d,politic.maxStocks,portfolio.bank,i,politic.maxStocks,politic.minimumInPortfolio,politic.firstArbitrationMonth,
					politic.arbitrationCycle));
			
			System.out.println("Start "+portfolio.name);
			
			portfolio.generationPortfolio();

			// NEXT
			seed = seed*10 + 1;
		}
		System.out.println("END");
	}
}
