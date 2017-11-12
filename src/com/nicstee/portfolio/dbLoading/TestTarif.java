package com.nicstee.portfolio.dbLoading;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.nicstee.portfolio.Portfolio;
import com.nicstee.portfolio.Stock;

public class TestTarif {

	public static void main(String[] args) throws SQLException, IOException {
		Connection conn = null;
		String dbURL1 = "jdbc:postgresql:Portfolio?user=postgres&password=GLOZQCKI";
		conn = DriverManager.getConnection(dbURL1);
		if (conn != null) {
			System.out.println("Connected to database Portefeuille");
		}else System.exit(999);
		Portfolio p = new Portfolio();
		p.conn=conn;
//		double amount = -120000;
//		Stock s = new Stock(Date.valueOf("2015-05-02"),6);
//		s.amount = BigDecimal.valueOf(amount);
//		p.setBank(Portfolio.BINCKBANCK);
//		BigDecimal commission = p.getCost(s);
//		System.out.println("Commission BINCKBANK pour 6  = "+commission);
//		p.setBank(Portfolio.ING);
//		commission = p.getCost(s);
//		System.out.println("Commission ING       pour 6  = "+commission);
//		//
//		s = new Stock(Date.valueOf("2015-05-02"),20);
//		s.amount = BigDecimal.valueOf(amount);
//		p.setBank(Portfolio.BINCKBANCK);
//		commission = p.getCost(s);
//		System.out.println("Commission BINCKBANK pour 20 = "+commission);
//		p.setBank(Portfolio.ING);
//		commission = p.getCost(s);
//		System.out.println("Commission ING       pour 20 = "+commission);
		p.id_portfolio=1731;
		p.setBank(Portfolio.ING);
		p.setAgios(Date.valueOf("2015-05-02"));
	}
}
