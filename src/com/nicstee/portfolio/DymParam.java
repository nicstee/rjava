package com.nicstee.portfolio;

public class DymParam {
	public int perfPeriodForPurchase = 30;
	public double purchaseThreshold = .95;
	public int maxMonth = 12;
	DymParam(int maxMonth, int perfPeriodForPurchase,double purchaseThreshold){
		this.maxMonth=maxMonth;
		this.perfPeriodForPurchase=perfPeriodForPurchase;
		this.purchaseThreshold=purchaseThreshold;
	}
}
