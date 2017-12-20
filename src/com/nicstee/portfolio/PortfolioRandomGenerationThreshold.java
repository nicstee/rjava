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
		int maxMonth[] = {9,12,15};
		int perf[] = {20,30,40};
		double seuilPurchase[] = {.975,.95,.925};
		double seuilSell[] = {.55,.65,.75};
		int i = 0;
		int j = 1;
		int k = 1;
		int l = 0;
		long seed = 10;
		PoliticInitRandomMiniMax politic = null;
		Portfolio portfolio = null;
		for(int m = 0; m<12;m++){
					politic = new PoliticInitRandomMiniMax();
					
					politic.setSeed(seed);
					politic.setMaxStocks(20);
					politic.setArbitrationDay(3);
					politic.setFirstArbitrationMonth(3);	
					politic.setMinimumInPortfolio(1);
					politic.setArbitrationCycle(1);
					politic.setMaxMonth(maxMonth[l]);
					politic.setPerfPeriodForPurchase(perf[i]);
					politic.setPurchaseThreshold(seuilPurchase[j]);
					politic.setSellThreshold(seuilSell[k]);

					portfolio = new Portfolio();//"aexminimaxvport30-20");

					portfolio.setPolitic(politic);
					portfolio.setdCreation("2016-09-01");
					portfolio.setdFin("2017-09-15");
					portfolio.setStartCash(BigDecimal.valueOf(1000000.));
					portfolio.setBank(Portfolio.BINCKBANCK);
					String name = String.format("%s;%s;BQ%s;MTH%s;PERF%sj;SA%sp;SV%sp;SACT%sa;MIN%sm;C%sm",
							d,m,portfolio.bank,politic.maxMonth,politic.perfPeriodForPurchase,
							politic.purchaseThreshold,politic.sellThreshold,politic.maxStocks,
							politic.minimumInPortfolio,politic.arbitrationCycle);
					portfolio.setName(name);
					
					System.out.println("*** Paramètres ***");
					System.out.println(name);
					
					portfolio.generationPortfolio();
					
					// NEXT
					seed = seed*10 + 1;
				}
		System.out.println("END");
//			}
//		}
//	}
	}
}
