package com.nicstee.portfolio;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PoliticMiniMax extends PoliticBase{

	public void loadParam(Date currentDay) throws SQLException {
		String req = String.format("select COALESCE((select samount from temporaire where id_portfolio="
				+ "%s and date <= date(date("
				+ "'%s') - interval "
				+ "'%s weeks') order by date desc limit 1)/ (select samount from temporaire where id_portfolio="
				+ "%s and date <= "
				+ "'%s' order by date desc limit 1),.0) as pente",
				portfolio.id_portfolio,currentDay,penteMth,portfolio.id_portfolio,currentDay);
		//		System.out.println(req);
		double pente= 1.;
		Statement stmt = Portfolio.conn.createStatement();
		ResultSet rs = stmt.executeQuery(req);
		if(rs.next())pente=Math.pow(rs.getBigDecimal("pente").doubleValue(),52./penteMth);
		if(pente >= 1.){ //low
			this.maxMonth=dymParamLMH.dymParamLow.maxMonth;
			this.perfPeriodForPurchase=dymParamLMH.dymParamLow.perfPeriodForPurchase;
			this.purchaseThreshold=dymParamLMH.dymParamLow.purchaseThreshold;
			System.out.println(" Status Low " + pente);
		}else{
			this.maxMonth=dymParamLMH.dymParamHigh.maxMonth;
			this.perfPeriodForPurchase=dymParamLMH.dymParamHigh.perfPeriodForPurchase;
			this.purchaseThreshold=dymParamLMH.dymParamHigh.purchaseThreshold;			
			System.out.println(" Status high "+pente);
		}
	}

	public double perfStockForSell(Date currentDay,int id_portfolio, Stock s) throws SQLException {
		double perf=s.amount.divide(s.amountPurchase,3,BigDecimal.ROUND_HALF_DOWN).doubleValue();
		if( s.since > 0)perf=Math.pow(perf, (1./s.since));
		return perf; // attention selection sur le minimum 
	}

	public double perfStockForPurchase(Date currentDay, int id_portfolio, Stock s) {
		Statement stmt;
		double perf = 9999.;
		try {
			stmt = Portfolio.conn.createStatement();
			String req = String.format( // selection rapport prix actuel / prix 30 jrs  chute la + forte sur 30 jrs
					"select %s/quoteStockEur(date '%s' - %s,%s) as performance",
					s.quoteEur,currentDay,perfPeriodForPurchase,s.id_stock);
			ResultSet rs = stmt.executeQuery(req);
			if(!rs.next())return 999.;
			perf = rs.getBigDecimal("performance").doubleValue();
		} catch (SQLException e) {
			return 999.; // pour empecher un achat
		}
		return perf; // attention selection sur le minimum 
	}

	@Override
	public double perfStockForPurchaseInit(Date currentDay, int portfolio, Stock s) throws SQLException {
		return perfStockForPurchase(currentDay,portfolio,s);
	}

}
