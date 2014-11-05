package com.dn.ivan.rates;

public class CommercialRateItem {

	public String date = "";
	public String bankName = "";
	public String sourceUrl = "";
	public String codeNumeric = "";
	public String codeAlpha = "";
	public String rateBuy = "";	
	public String rateBuyDelta = "0";
	public String rateSale = "";	
	public String rateSaleDelta = "0";
	
	@Override
	public String toString() {
		return "CommercialRateItem [date=" + date + ", bankName=" + bankName
				+ ", sourceUrl=" + sourceUrl + ", codeNumeric=" + codeNumeric
				+ ", codeAlpha=" + codeAlpha + ", rateBuy=" + rateBuy
				+ ", rateBuyDelta=" + rateBuyDelta + ", rateSale=" + rateSale
				+ ", rateSaleDelta=" + rateSaleDelta + "]";
	}	
}
