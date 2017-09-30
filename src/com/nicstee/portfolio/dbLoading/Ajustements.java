package com.nicstee.portfolio.dbLoading;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Ajustements {

	public static void main(String[] args) throws SQLException {
        Connection conn = null;
            String dbURL1 = "jdbc:postgresql:Portfolio?user=postgres&password=GLOZQCKI";
            conn = DriverManager.getConnection(dbURL1);
            if (conn != null) {
                System.out.println("Connected to database Portefeuille");
            }else System.exit(999);
//	chargement de la date de première cotation
            String req = String.format("select id_stock,min(date) as minDate from quotes "+
            "where close is not null group by id_stock order by minDate");
            System.out.println(req);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(req);
            while(rs.next()){
            	int id_stock = rs.getInt("id_stock");
            	Date minDate = rs.getDate("minDate");
            	req = String.format("UPDATE stocks SET firstquote = '%s' where id = %s",minDate, id_stock);
	              System.out.println(req);
	              Statement stmt1 = conn.createStatement();
	              stmt1.executeUpdate(req);
            }
// 
// recherche des cotations manquantes et ajustement
            req = String.format("select date, id_stock from quotes t1 where close is null and date > (select firstquote from stocks where id = id_stock)");
            System.out.println(req);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(req);
            while(rs.next()){
            	int id_stock = rs.getInt("id_stock");
            	Date date = rs.getDate("date");
//            	System.out.println(id_stock + " " + date);
         
                req = String.format("select close from quotes where id_stock = %s and close > 0 and date < '%s'"+
                		" order by date desc limit 1",id_stock,date);
//              System.out.println(req);
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery(req);
                BigDecimal quoteInf = new BigDecimal(0.);
                if(rs1.next())quoteInf = rs1.getBigDecimal("close");
                
                req = String.format("select close from quotes where id_stock = %s and close > 0 and date > '%s'"+
                		" order by date asc limit 1",id_stock,date);
//              System.out.println(req);
                stmt1 = conn.createStatement();
                rs1 = stmt1.executeQuery(req);
                BigDecimal quoteSup = new BigDecimal(0.);
                if(rs1.next())quoteSup = rs1.getBigDecimal("close");
                BigDecimal quoteCalculate = quoteInf.add(quoteSup).multiply(new BigDecimal(.5));
                if(rs1.next())quoteInf = rs1.getBigDecimal("close");              
          	  	req = String.format("UPDATE quotes SET close = %s where date = '%s' and id_stock = %s",
          	  			quoteCalculate,date,id_stock);
//          	System.out.println(req);
          	  	stmt1 = conn.createStatement();
          	  	stmt1.executeUpdate(req); 
          	  	System.out.println(date + " " + id_stock + 
          	  			"q. avant "+ quoteInf + "q. après " + quoteSup + " q mod." + quoteCalculate);
            }
	}
}
