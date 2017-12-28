package com.nicstee.portfolio.dbLoading;

public class YahooDataLoading {

	public static void main(String[] args) {
		System.out.println("Quotes Download");
		QuotesDownload.main();
		System.out.println("Dividendes Download");
		DividendsDownload.main();
		System.out.println("Quotes & Dividendes Update");
		QuotesUpdate.main();;
	}

}
