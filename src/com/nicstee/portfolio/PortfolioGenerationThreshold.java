package com.nicstee.portfolio;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PortfolioGenerationThreshold {

	public static void main(String[] args) throws SQLException, IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		String d = dateFormat.format(c.getTime());
		int perf[]             = {40,30,20};//i
		double seuilPurchase[] = {.975,.95,.925};//j
		double seuilSell[]     = {.55,.65,.75};//k
		int maxMonth[] = {9,12,15};//l
		String name=null;
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
//		for(int i = 1; i<3;i++){  // perf[i]
//			for(int j = 0; j<3;j++){// seuilPurchase[j]
//				for(int k = 1;k<3;k++){ // seuilSell[k]
//					for(int l = 0;l<3;l++){	
						PoliticMiniMax politic = new PoliticMiniMax();
						politic.setMaxStocks(40);
						politic.setArbitrationDay(3);
						politic.setFirstArbitrationMonth(1);	
						politic.setArbitrationCycle(1);
						politic.setMaxMonth(maxMonth[l]);
						politic.setPerfPeriodForPurchase(perf[i]);
						politic.setPurchaseThreshold(seuilPurchase[j]);
						politic.setSellThreshold(seuilSell[k]);

						politic.setMinimumInPortfolio(1);
//						Portfolio portfolio = new Portfolio();
//
//						portfolio.setPolitic(politic);
//						portfolio.setdCreation("2006-01-01");
//						portfolio.setdFin("2006-12-31");
//						portfolio.setStartCash(BigDecimal.valueOf(1000000.));
//						portfolio.setBank(Portfolio.BINCKBANCK);
//						ame = String.format("%s;P1 BQ%s;MTH%s;PERF%sj;SA%sp;SV%sp;SACT%sa;MIN%sm;C%sm",
//								d,portfolio.bank,politic.maxMonth,politic.perfPeriodForPurchase,
//								politic.purchaseThreshold,politic.sellThreshold,politic.maxStocks,
//								politic.minimumInPortfolio,politic.arbitrationCycle);
//						portfolio.setName(name);
//						//---------------------------------------------------------------
//						System.out.println("*** Paramètres ***");
//						System.out.println(name);
//						portfolio.generationPortfolio();
//
//						System.out.println("END PHASE 1");

						Portfolio portfolio2 = new Portfolio();

						politic.setMinimumInPortfolio(3);
						portfolio2.setPolitic(politic);
						portfolio2.setdCreation("2008-01-01");
						portfolio2.setdFin("2009-12-31");
						portfolio2.setStartCash(BigDecimal.valueOf(1000000.));
						portfolio2.setBank(Portfolio.BINCKBANCK);
						name = String.format("%s;P2 BQ%s;MTH%s;PERF%sj;SA%sp;SV%sp;SACT%sa;MIN%sm;C%sm",
								d,portfolio2.bank,politic.maxMonth,politic.perfPeriodForPurchase,
								politic.purchaseThreshold,politic.sellThreshold,politic.maxStocks,
								politic.minimumInPortfolio,politic.arbitrationCycle);
						portfolio2.setName(name);
						//---------------------------------------------------------------
						System.out.println("*** Paramètres ***");
						System.out.println(name);
//						portfolio2.generationPortfolio(portfolio.getSaveVectorActiveStocks());
//						portfolio2.generationPortfolio("C:/Users/claude/Desktop/R folder/downloads/outputPortfolioSave.csv");
						portfolio2.generationPortfolio();
						System.out.println("END PHASE");

//					}
//				}
//			}
//		}
	}
}
