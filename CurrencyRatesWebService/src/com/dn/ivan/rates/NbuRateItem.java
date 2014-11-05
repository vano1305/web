package com.dn.ivan.rates;

public class NbuRateItem {

	public String date = "";
	public String code = "";
	public String char3 = "";
	public String size = "1";
	public String name = "";
	public String rate = "";	
	public String change = "";
	
	@Override
	public String toString() {
		return "NbuRateItem [date=" + date + ", code=" + code + ", char3="
				+ char3 + ", size=" + size + ", name=" + name + ", rate="
				+ rate + ", change=" + change + "]";
	}	
}
