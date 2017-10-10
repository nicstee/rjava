package com.nicstee.portfolio;

import java.io.IOException;
import java.sql.SQLException;

public class PortfolioGeneration {

	public static void main(String[] args) throws SQLException, IOException {
//
		long seed = 10;
//		for(int i = 0;i<12;i++){
//		1er param. = nbre pour le random
//		2me param. = le nombre max. d'actions en portefeuille
//		3me param. = le jour du mois d'arbitrage
//		4me param. = nbre de mois pour commencer les arbitrages
		Politic politic = new PoliticMiniMax(seed,20,3,3);
//		1er param. = le nom du portefeuille
//		2me param. = la strategie
//		3me param. = la date d'ouverture du portefeuille
//		4me param. = la date de fermeture du portefeuille
//		5me param. = l'investissement, nombre floating > 0
//		6me param. = commission achat/vente en pourcent
		new Portfolio("minimax",politic,"2007-01-15","2017-09-15",1000000.,0.2);
//		seed = seed*10 + 1;
//		new Portfolio("essai1");
//
//		Portfolio.close();
//		}
		System.out.println("END");
	}
}
