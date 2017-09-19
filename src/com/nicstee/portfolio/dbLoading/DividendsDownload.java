package com.nicstee.portfolio.dbLoading;
	import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
	 
	/**
	 * This program demonstrates how to make database connection to PostgreSQL
	 * server using JDBC.
	 * @author www.codejava.net
	 *
	 * modify by c.glowacki - 9-9-2017
	 */
	
	public class DividendsDownload {
	 
	    public static void main() {
	        // create connection Portefeuille DB
	        Connection conn = null;
	        try {
	            // Connect method #1
	            String dbURL1 = "jdbc:postgresql:Portfolio?user=postgres&password=GLOZQCKI";
	            conn = DriverManager.getConnection(dbURL1);
	            if (conn != null) {
	                System.out.println("Connected to database Portefeuille");
	            }
	            ResultSet resultats = null;
	            String requete = "SELECT * FROM stocks";
	            Statement stmt = conn.createStatement();
	            resultats = stmt.executeQuery(requete);
	            GetYahooQuotes c = new GetYahooQuotes();
	            while (resultats.next()) {
	            	int id_stock = resultats.getInt(1);
	            	String symbol = resultats.getString(2).trim();
	            	int quotes_status = resultats.getInt(4);
	            	int dividends_status = resultats.getInt(5);
		            if(dividends_status != 0)continue;
		            Statement stmt0 = conn.createStatement();
		            stmt0.executeUpdate(String.format("delete from dividends where id_stock = %s",id_stock));		          		          
		            System.out.println(id_stock + " " + symbol + " " + resultats.getString(3)+
	            	" Quotes status = " + quotes_status + " Dividends status = " + dividends_status);	
		            String crumb = c.getCrumb(symbol);
	                if (crumb != null && !crumb.isEmpty()) {
	                    System.out.println(String.format("Downloading data to %s", symbol));
	                    System.out.println("Crumb: " + crumb);
	                    c.downloadDividends(symbol, 0, System.currentTimeMillis(), crumb);
	                } else {
	                    System.out.println(String.format("Error retreiving data for %s", symbol));
	                }	            
		            BufferedReader br = new BufferedReader(new FileReader(symbol.concat(".csv")));
		            String ligne = br.readLine();
		            while ((ligne = br.readLine()) != null)
		             {
		              // Retourner la ligne dans un tableau
		              String[] data = ligne.split(",");        
		              // Afficher le contenu du tableau
		              String req = String.format("INSERT INTO dividends (id_stock,date,dividends) VALUES (%s,'%s',%s)",
		            		  id_stock,data[0], data[1]);
//		              System.out.println(req);
		              Statement stmt1 = conn.createStatement();
		              stmt1.executeUpdate(req);
		             }
		            br.close();
		            new File(symbol.concat(".csv")).delete();
		            String reqUpdate = String.format("update quotes set dividends= (select dividends from dividends "+
		            "where quotes.date = dividends.date and quotes.id_stock = dividends.id_stock) where id_stock = %s",
		            id_stock);
		            Statement stmt2 = conn.createStatement();
		              stmt2.executeUpdate(reqUpdate);		            		            
		            reqUpdate = String.format("UPDATE stocks SET dividends_status = 1 where id = %s",id_stock);
		            stmt2 = conn.createStatement();
		              stmt2.executeUpdate(reqUpdate);		            
	            }
	            resultats.close();
	    	    System.out.println("FIN");
	        } catch (SQLException | IOException ex) {
	            ex.printStackTrace();
	        } finally {
	            try {
	                if (conn != null && !conn.isClosed()) {
	                    conn.close();
	                }
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }
	        }
	    }
	}

