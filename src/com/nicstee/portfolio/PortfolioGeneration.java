package com.nicstee.portfolio;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PortfolioGeneration {

	public static void main(String[] args) throws SQLException, IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		String d = dateFormat.format(c.getTime());
		int cycle[] = {1,2,3};
		int mini[] = {1,2,3};
		int perf[] = {20,30,40};
//		int i = 0;
//		int j = 2;
//		int k = 1;
		for(int i = 0; i<3;i++){  // 
			for(int j = i; j<3;j++){// minimum en portefeuille >= cycle d'arbitrage
				for(int k = 0;k<3;k++){ // recule pour analyse du max de décroissance
// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
//					if(i == 0 && j== 0 && k == 0)continue;
// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
					//-------------------------------------------------------------
					PoliticMiniMax politic = new PoliticMiniMax();
					//		1er param. = nbre pour le random
					//		politic.setSeed(10);
					//		2me param. = le nombre max. d'actions en portefeuille
					politic.setMaxStocks(20);
					//		3me param. = le jour du mois d'arbitrage
					politic.setArbitrationDay(3);
					//		4me param. = nbre de mois pour commencer les arbitrages
					politic.setFirstArbitrationMonth(3);	
					//		5me param. = nbre de mois minimum à garder dans le portefeuille
					politic.setMinimumInPortfolio(mini[j]);
					//		6me param. = cycle d'arbitrage en mois
					politic.setArbitrationCycle(cycle[i]);
					//		7me param. = periode pour déterminer la perf. en vue de l'achat
					politic.setPerfPeriodForPurchase(perf[k]);
					// ------------------------------------------------------------
					Portfolio portfolio = new Portfolio();//"aexminimaxvport30-20");
					//		1er param. = la strategie
					portfolio.setPolitic(politic);
					//		2me param. = commission achat/vente en pourcent
					// nouveau ,006
					portfolio.setCommission(0.006);
					//		3me param. = la date d'ouverture du portefeuille
					portfolio.setdCreation("2007-01-01");
					//		4me param. = la date de fermeture du portefeuille
					portfolio.setdFin("2017-09-15");
					//		5me param. = l'investissement, nombre floating > 0
					portfolio.setStartCash(BigDecimal.valueOf(1000000.));
					portfolio.setBank(Portfolio.BINCKBANCK);
					//		6ème param. = le nom du portefeuille
					//					String name = String.format("%s;%s;%s;%s;%s;%s;%s;\"%s\";\"%s\";%s",politic.maxStocks,politic.arbitrationDay,
					//							politic.firstArbitrationMonth,politic.minimumInPortfolio,politic.arbitrationCycle,
					//							politic.perfPeriodForPurchase,portfolio.commission,portfolio.dCreation,portfolio.dFin,
					//							portfolio.startCash);
					String name = String.format("BINCKBANK %s minimumInPortfolio = %s mois; arbitrationCycle = %s mois; perfPeriodForPurchase = %s jours",
							d,politic.minimumInPortfolio,politic.arbitrationCycle,politic.perfPeriodForPurchase);
					//		7me param. de lissage 0> ,100<
					portfolio.setName(name);
					//---------------------------------------------------------------
					System.out.println("*** Paramètres ***");
					System.out.println("minimumInPortfolio = "+politic.minimumInPortfolio);
					System.out.println("arbitrationCycle = "+politic.arbitrationCycle);
					System.out.println("perfPeriodForPurchase = "+politic.perfPeriodForPurchase+"\n");
					portfolio.generationPortfolio();
					System.out.println("END");
				}
			}
		}
	}
}
