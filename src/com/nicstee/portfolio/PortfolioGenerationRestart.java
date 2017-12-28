package com.nicstee.portfolio;

import java.io.IOException;
import java.sql.SQLException;

public class PortfolioGenerationRestart {

	public static void main(String[] args) throws SQLException, IOException {
		DymParam dymParamHigh = new DymParam(15,20,0.925);
		DymParam dymParamMedium = new DymParam(15,20,0.925);
		DymParam dymParamLow = new DymParam(9,40,0.975);		
		DymParamLMH dymParamLMH =new DymParamLMH(dymParamLow,dymParamMedium,dymParamHigh);


		PoliticMiniMax politic = new PoliticMiniMax();
		politic.setMaxStocks(40); // nbre d'actions dans le portefeuillz
		politic.setArbitrationDay(3); // jour du mois pour les arbitrages
		politic.setFirstArbitrationMonth(3);	// d�marrage de l'arbitrage apr�s le 3�me mois
		politic.setMinimumInPortfolio(3); // une action doit rester au minimum 3 mois dans le pf
		politic.setArbitrationCycle(1); // un arbitrage tous les mois
		politic.setPenteMth(13); // tendance du pf calcul�e sur 13 semaines
		politic.setDymParamLMH(dymParamLMH);

		Portfolio portfolio = new Portfolio();
		portfolio.setPolitic(politic);
		portfolio.setdFin("2017-12-15");
		portfolio.setBank(Portfolio.BINCKBANCK);

		portfolio.generationPortfolio(2536);

		System.out.println("END PHASE");
	}
}
