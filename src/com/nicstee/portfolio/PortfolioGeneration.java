package com.nicstee.portfolio;

import java.sql.SQLException;

public class PortfolioGeneration {

	public static void main(String[] args) throws SQLException {
//
//		1er param. = le nombre max. d'actions en portefeuille
//		2me param. = le jour du mois d'arbitrage
//		3me param. = nbre de mois pour commencer les arbitrages
		Politic politic = new PoliticBase(9,3,3);
//
//
//		1er param. = le nom du portefeuille
//		2me param. = la strategie
//		3me param. = la date d'ouverture du portefeuille
//		4me param. = l'investissement, nombre floating > 0
//		5me param. = commission achat/vente en pourcent
//
		new Portfolio("essai1",politic,"2014-01-15",1000000.,0.2);
//
		Portfolio.close();
		System.out.println("END");
	}
}