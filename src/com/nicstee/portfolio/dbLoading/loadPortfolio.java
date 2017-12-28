package com.nicstee.portfolio.dbLoading;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

import com.nicstee.portfolio.Stock;

public class loadPortfolio {
	static int id_portfolio=9999;
    static Connection conn = null;
	public static void main(String[] args) throws SQLException {
        // Connect method #1
    String dbURL1 = "jdbc:postgresql:Portfolio?user=postgres&password=GLOZQCKI";
    conn = DriverManager.getConnection(dbURL1);
    if (conn != null) {
            System.out.println("Connected to database Portefeuille");
    }
    loadingPortfolio(Date.valueOf("2016-09-01"),"C:/Users/claude/Desktop/R folder/downloads/portfolio.csv");

	}

	private static void loadingPortfolio(Date creation, String csvFile) throws SQLException {
		Vector<Stock> vectorPurchaseStocks = new Vector<Stock>();
		BufferedReader br = null;
	    String line = "";
	    String cvsSplitBy = ";";
	    try {
	        br = new BufferedReader(new FileReader(csvFile));
	        while ((line = br.readLine()) != null) {
	            String[] ligne = line.split(cvsSplitBy);
	            System.out.println("code= " + ligne[0].trim() + " , quantite=" + ligne[1].trim());
	            Stock s = new Stock(creation,Integer.parseInt(ligne[0].trim()));
	            s.quantity=Integer.parseInt(ligne[1].trim());
	            vectorPurchaseStocks.add(s);
	        }      

	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if (br != null) {
	            try {
	                br.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
		
	}

}
