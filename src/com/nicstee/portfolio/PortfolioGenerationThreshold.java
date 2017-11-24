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
		int perf[] = {20,30,40};
		double seuilPurchase[] = {.975,.95,.925};
		double seuilSell[] = {.55,.65,.75};
		int i = 2;
		int j = 1;
		int k = 1;
//		for(int i = 0; i<3;i++){  // perf[i]
//			for(int j = 0; j<3;j++){// seuilPurchase[j]
//				for(int k = 0;k<3;k++){ // seuilSell[k]
// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
//					if(i == 0 && j== 0 && k < 2)continue;
// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
					PoliticMiniMax politic = new PoliticMiniMax();

					politic.setMaxStocks(20);
					politic.setArbitrationDay(3);
					politic.setFirstArbitrationMonth(3);	
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
					String name = String.format("%s;BQ%s;PERF%sj;SA%sp;SV%sp;SACT%sa;MIN%sm;C%sm",
							d,portfolio.bank,politic.perfPeriodForPurchase,
							politic.purchaseThreshold,politic.sellThreshold,politic.maxStocks,
							politic.minimumInPortfolio,politic.arbitrationCycle);
					portfolio.setName(name);
					//---------------------------------------------------------------
					System.out.println("*** Paramètres ***");
					System.out.println(name);
					portfolio.generationPortfolio();
					System.out.println("END");
				}
//			}
//		}
//	}
}
