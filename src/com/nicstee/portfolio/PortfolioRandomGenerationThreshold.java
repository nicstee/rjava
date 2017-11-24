package com.nicstee.portfolio;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PortfolioRandomGenerationThreshold {

	public static void main(String[] args) throws SQLException, IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		String d = dateFormat.format(c.getTime());
		long seed = 10;
		int perf[] = {20,30,40};
		double seuilPurchase[] = {.975,.95,.925};
		double seuilSell[] = {.55,.65,.75};
		int i = 0;
		int j = 1;
		int k = 2;
		
		
// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
//					if(i == 0 && j== 0 && k < 2)continue;
// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
		for(int n = 5;n<12;n++){
					PoliticInitRandomMiniMax politic = new PoliticInitRandomMiniMax();
					politic.setSeed(seed);
					politic.setMaxStocks(20);
					politic.setArbitrationDay(3);
					politic.setFirstArbitrationMonth(1);	
					politic.setMinimumInPortfolio(1);
					politic.setArbitrationCycle(1);

					politic.setPerfPeriodForPurchase(perf[i]);
					politic.setPurchaseThreshold(seuilPurchase[j]);
					politic.setSellThreshold(seuilSell[k]);

					Portfolio portfolio = new Portfolio();//"aexminimaxvport30-20");

					portfolio.setPolitic(politic);
					portfolio.setdCreation("2007-01-01");
					portfolio.setdFin("2017-09-15");
					portfolio.setStartCash(BigDecimal.valueOf(1000000.));
					portfolio.setBank(Portfolio.BINCKBANCK);
					String name = String.format("%s;%s;BQ%s;PERF%sj;SA%sp;SV%sp;SACT%sa;MIN%sm;C%sm",
							d,n,portfolio.bank,politic.perfPeriodForPurchase,
							politic.purchaseThreshold,politic.sellThreshold,politic.maxStocks,
							politic.minimumInPortfolio,politic.arbitrationCycle);
					portfolio.setName(name);
					//---------------------------------------------------------------
					System.out.println("*** Paramètres Random on start***");
					System.out.println(name);
					portfolio.generationPortfolio();
					System.out.println("END");
					seed = seed*10 + 1;
				}
	}
}
