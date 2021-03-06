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

		DymParam dymParamHigh = new DymParam(15,20,0.925);
		DymParam dymParamMedium = new DymParam(15,20,0.925);
		DymParam dymParamLow = new DymParam(9,40,0.975);		
		DymParamLMH dymParamLMH =new DymParamLMH(dymParamLow,dymParamMedium,dymParamHigh);

		String name=null;
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
		portfolio.setdCreation("2017-12-01");
		portfolio.setdFin("2017-12-31");
		portfolio.setStartCash(BigDecimal.valueOf(1000000.));
		portfolio.setBank(Portfolio.BINCKBANCK);
		name = String.format("%s;BQ%s;PMTH%sj;MIN%sm;C%sm",
				d,portfolio.bank,politic.penteMth,politic.maxStocks,
				politic.minimumInPortfolio,politic.arbitrationCycle);
		portfolio.setName(name);
		System.out.println("*** Param�tres ***");
		System.out.println(name);
		
		portfolio.generationPortfolio();
		
		System.out.println("END PHASE");
	}
}
