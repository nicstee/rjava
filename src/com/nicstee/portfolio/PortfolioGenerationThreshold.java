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
		
//		int perf[]             = {40,30,20};//i
//		double seuilPurchase[] = {.975,.95,.925};//j
//		int maxMonth[] = {9,12,15};//l
		DymParam dymParamHigh = new DymParam(15,20,0.925);
		DymParam dymParamMedium = new DymParam(15,20,0.925);
		DymParam dymParamLow = new DymParam(9,40,0.975);
		
		DymParamLMH dymParamLMH =new DymParamLMH(dymParamLow,dymParamMedium,dymParamHigh);

		String name=null;
//		int i = 0;
//		int j = 0;
//		int l = 0;
//		for(int i = 1; i<3;i++){  // perf[i]
//			for(int j = 0; j<3;j++){// seuilPurchase[j]
//					for(int l = 0;l<3;l++){	
						PoliticMiniMax politic = new PoliticMiniMax();
						politic.setMaxStocks(40);
						politic.setArbitrationDay(3);
						politic.setFirstArbitrationMonth(1);	
						politic.setArbitrationCycle(1);
						politic.setMinimumInPortfolio(1);
						politic.setPenteMth(13);
//	Paramètres dynamiques						
//						politic.setPerfPeriodForPurchase(perf[i]);
//						politic.setPurchaseThreshold(seuilPurchase[j]);
//						politic.setMaxMonth(maxMonth[l]);
						politic.setDymParamLMH(dymParamLMH);

						Portfolio portfolio = new Portfolio();

						politic.setMinimumInPortfolio(3);
						portfolio.setPolitic(politic);
						portfolio.setdCreation("2007-01-01");
						portfolio.setdFin("2017-09-15");
						portfolio.setStartCash(BigDecimal.valueOf(1000000.));
						portfolio.setBank(Portfolio.BINCKBANCK);
						name = String.format("%s;BQ%s;PMTH%sj;MIN%sm;C%sm",d,portfolio.bank,politic.penteMth,
								politic.maxStocks,
								politic.minimumInPortfolio,politic.arbitrationCycle);
						portfolio.setName(name);
						//---------------------------------------------------------------
						System.out.println("*** Paramètres ***");
						System.out.println(name);
						portfolio.generationPortfolio();
						System.out.println("END PHASE");

//					}
//				}
//			}
//		}
	}
}
