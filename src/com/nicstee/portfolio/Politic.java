package com.nicstee.portfolio;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
public interface Politic {
	public void setPortfolio(Portfolio portfolio);
	public void initPortfolio(java.math.BigDecimal amount, Date creation) throws SQLException, IOException;
	public void arbitrationStocks(Portfolio portfolio, Date currentDay) throws SQLException, IOException;
}
