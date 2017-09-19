package com.nicstee.portfolio;

import java.sql.Date;
import java.sql.SQLException;
public interface Politic {
public void setPortfolio(Portfolio portfolio);
public void init(java.math.BigDecimal amount, Date creation) throws SQLException;
public void setArbitrationDay(int arbitrationDay);
public int getArbitrationDay();
}
