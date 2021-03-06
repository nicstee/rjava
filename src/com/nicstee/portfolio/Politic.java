package com.nicstee.portfolio;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Vector;
public interface Politic {
	public void initPortfolio(java.math.BigDecimal amount, Date creation) throws SQLException, IOException;
	public Vector<Stock> arbitrationStocks(Date currentDay) throws SQLException, IOException;
	public double perfStockForSell(Date currentDay, int id_portfolio, Stock s) throws SQLException;
	public double perfStockForPurchase(Date currentDay, int id_portfolio,Stock s) throws SQLException;
	public void setPortfolio(Portfolio portfolio);
	public void setMinimumInPortfolio(int minimumInPortfolio);
	public void setArbitrationDay(int arbitrationDay);
	public void setMaxStocks(int maxStocks);
	public void setFirstArbitrationMonth(int firstArbitrationMonth);
	public void setEndArbitration(Date endArbitration);
	public void setArbitrationCycle(int arbitrationCycle);
	public void setPerfPeriodForPurchase(int perfPeriodForPurchase);
	public void setMaxMonth(int maxMonth);
	public void setPenteMth(int penteMth);
	public void loadParam(Date currentDay) throws SQLException;
	public BigDecimal initData(BigDecimal cash, Date creation);
}
