package com.dn.ivan.rates;

public class BlackMarketRateItem {

	public String opCode = "";
	public String date = "";
	public String cityCode = "";
	public String cityName = "";
	public String currencyCode = "";
	public String currencyName = "";
	public String rate = "";
	public String rate_delta = "";
	
	@Override
	public String toString() {
		return "BlackMarketRateItem [type=" + opCode + ", date=" + date
				+ ", cityCode=" + cityCode + ", cityName=" + cityName
				+ ", currencyCode=" + currencyCode + ", currencyName="
				+ currencyName + ", rate=" + rate + ", rate_delta="
				+ rate_delta + "]";
	}								
}
