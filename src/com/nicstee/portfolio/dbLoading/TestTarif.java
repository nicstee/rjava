package com.nicstee.portfolio.dbLoading;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.nicstee.portfolio.Portfolio;

public class TestTarif {

	public static void main(String[] args) throws SQLException, IOException {
		Connection conn = null;
		String dbURL1 = "jdbc:postgresql:Portfolio?user=postgres&password=GLOZQCKI";
		conn = DriverManager.getConnection(dbURL1);
		if (conn != null) {
			System.out.println("Connected to database Portefeuille");
		}else System.exit(999);
		Portfolio p = new Portfolio();
//		p.savePortfolio(1731, Date.valueOf("2017-09-15"));
	}
}
