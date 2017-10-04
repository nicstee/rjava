package com.nicstee.portfolio.dbLoading;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class USD_loading {

	public static void main(String[] args) throws SQLException {
        Connection conn = null;
        String dbURL1 = "jdbc:postgresql:Portfolio?user=postgres&password=GLOZQCKI";
        conn = DriverManager.getConnection(dbURL1);
        if (conn != null) {
            System.out.println("Connected to database Portefeuille");
        }else System.exit(999);
        
        String csvFile = "C:/Users/claude/Desktop/R folder/downloads/exchange.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                String[] ligne = line.split(cvsSplitBy);
                System.out.println("date= " + ligne[0] + " , tx=" + ligne[1]);
                String req = String.format("INSERT INTO exchange " +
  		              "(code,date,taux) " +
  		              "VALUES ('%s','%s','%s')","USD",ligne[0],new BigDecimal(ligne[1]));
//  		              System.out.println(req);
  		              Statement stmt1 = conn.createStatement();
  		              stmt1.executeUpdate(req);

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
