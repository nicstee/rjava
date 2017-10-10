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
	 */
	public class DividendsDownload {
	 
	    public static void main() {
	        // create connection Portefeuille DB
	        Connection conn = null;
	        try {
	            // Connect method #1
	            String dbURL1 = "jdbc:postgresql:Portefeuille?user=postgres&password=GLOZQCKI";
	            conn = DriverManager.getConnection(dbURL1);
	            if (conn != null) {
	                System.out.println("Connected to database Portefeuille");
	            }
	            ResultSet resultats = null;
	            String requete = "SELECT * FROM actions";
	            Statement stmt = conn.createStatement();
	            resultats = stmt.executeQuery(requete);
	            GetYahooQuotes c = new GetYahooQuotes();
	            while (resultats.next()) {
	            	int id_action = resultats.getInt(1);
	            	String symbol = resultats.getString(2).trim();
	            	int quotes_status = resultats.getInt(4);
	            	int dividends_status = resultats.getInt(5);
		            if(dividends_status != 0)continue;
		            Statement stmt0 = conn.createStatement();
		            stmt0.executeUpdate(String.format("delete from dividendes where id = %s",id_action));		          		          
		            System.out.println(id_action + " " + symbol + " " + resultats.getString(3)+
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
		              String req = String.format("INSERT INTO dividendes (id_action,date,dividends) VALUES (%s,'%s',%s)", id_action,data[0], data[1]);
//		              System.out.println(req);
		              Statement stmt1 = conn.createStatement();
		              stmt1.executeUpdate(req);
		             }
		            br.close();
		            new File(symbol.concat(".csv")).delete();
		            String reqUpdate = String.format("UPDATE actions SET dividends_status = 1 where id = %s",id_action);
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

