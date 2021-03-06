package com.dn.ivan.rates;

public class FuelRateItem {

	public String date = "";
	public String regionCode = "";
	public String name = "";
	public String code = "";
	public String a_80 = "";
	public String a_80_delta = "";
	public String a_92 = "";
	public String a_92_delta = "";
	public String a_95 = "";
	public String a_95_delta = "";
	public String a_95_plus = "";
	public String a_95_delta_plus = "";
	public String dt = "";
	public String dt_delta = "";
	public String spg = "";
	public String spg_delta = "";
	
	@Override
	public String toString() {
		return "FuelRateItem [date=" + date + ", regionCode=" + regionCode
				+ ", name=" + name + ", code=" + code + ", a_80=" + a_80
				+ ", a_80_delta=" + a_80_delta + ", a_92=" + a_92
				+ ", a_92_delta=" + a_92_delta + ", a_95=" + a_95
				+ ", a_95_delta=" + a_95_delta + ", dt=" + dt + ", dt_delta="
				+ dt_delta + ", spg=" + spg + ", spg_delta=" + spg_delta + "]";
	}
}
