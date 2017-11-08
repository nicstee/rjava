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
import java.util.Calendar;

/**
 * This program demonstrates how to make database connection to PostgreSQL
 * server using JDBC.
 * @author www.codejava.net
 *
 * modify by c.glowacki - 9-9-2017
 *
 */
public class QuotesUpdate {

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
			resultats.next();
			GetYahooQuotes c = new GetYahooQuotes();
			long startDate = 0;
			while (resultats.next()) {
				int id_stock = resultats.getInt("id");
				String symbol = resultats.getString("code").trim();
				int quotes_status = resultats.getInt("quotes_status");
				int dividends_status = resultats.getInt("dividends_status");
//				boolean actived = resultats.getBoolean("actived");
				if(quotes_status == 1){	            		
					String req = String.format("select date from quotes where id_stock = %s order by date desc limit 1",id_stock);
					Statement stm1 = conn.createStatement();
					ResultSet rs = stm1.executeQuery(req);
					rs.next();
					Calendar cl = Calendar.getInstance();
					cl.setTime(rs.getDate("date"));
					cl.add(Calendar.DATE, 1);  // number of days to add
					startDate = cl.getTimeInMillis()/1000;
				}else{
		            Statement stmt0 = conn.createStatement();
		            stmt0.executeUpdate(String.format("delete from quotes where id_stock = %s",id_stock));		          
		            startDate = 0;
				}
				System.out.println(id_stock + " " + symbol + " " + resultats.getString(3)+
						" Quotes status = " + quotes_status + " Dividends status = " + dividends_status);	
				String crumb = c.getCrumb(symbol);
				if (crumb != null && !crumb.isEmpty()) {
					System.out.println(String.format("Downloading data to %s", symbol));
					System.out.println("Crumb: " + crumb);
					c.downloadQuote(symbol, startDate, System.currentTimeMillis(), crumb);
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
					String req = String.format("INSERT INTO quotes " +
							"(id_stock,date,open,high,low,close,adjclose,volume) " +
							"VALUES (%s,'%s',%s,%s,%s,%s,%s,%s)",
							id_stock,data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
					//		              System.out.println(req);
					Statement stmt1 = conn.createStatement();
					stmt1.executeUpdate(req);
				}
				br.close();
				new File(symbol.concat(".csv")).delete();
				String reqUpdate = String.format("UPDATE stocks SET quotes_status = 1 where id = %s",id_stock);
				Statement stmt2 = conn.createStatement();
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

