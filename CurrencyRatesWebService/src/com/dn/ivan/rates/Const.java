package com.dn.ivan.rates;

public class Const {
	
	/*
	 * Services
	 * */
	
	public static final String NBU_SERVICE = "http://finance.i.ua/nbu/";
	
	public static final String COMMERCIAL_USD_SERVICE = "http://finance.i.ua/usd/";
	public static final String COMMERCIAL_EUR_SERVICE = "http://finance.i.ua/eur/";
	public static final String COMMERCIAL_RUB_SERVICE = "http://finance.i.ua/rub/";
	
	public static final String FUEL_SERVICE = "http://finance.i.ua/fuel/";
	public static final String BLACK_MARKET_SERVICE = "http://finance.i.ua/market/";
	
	/*
	 * SQL
	 * */
	
	public static final String USER_AGENT = "Mozilla/5.0";
	public static final String GET_CURRENT_SQL_DATE = "SELECT DATE_FORMAT(now(), '%d-%m-%Y')";
	public static final String GET_FUEL_RATES_FROM_DB = "SELECT DATE_FORMAT(fr.date, '%d-%m-%Y'), fr.regionCode, fr.stationCode, fs.stationName, fr.a_80, fr.a_92, fr.a_95, fr.dt, fr.a_80_delta, fr.a_92_delta, fr.a_95_delta, fr.dt_delta"
			+ " FROM fuel_rates fr, fuel_stations fs"
			+ " WHERE fr.stationCode = fs.stationCode and fr.regionCode = ? and fr.date = (select max(date) from fuel_rates fr_ where fr_.regionCode = fr.regionCode and fr_.stationCode = fr.stationCode)";
	public static final String SAVE_FUEL_RATES_2_DB = "insert into fuel_rates (date, regionCode, stationCode, a_80, a_92, a_95, dt, a_80_delta, a_92_delta, a_95_delta, dt_delta) values (now(),?,?,?,?,?,?,?,?,?,?)";
	public static final String CLEAN_FUEL_RATES_TABLE = "delete from fuel_rates where date < DATE_SUB(now(),INTERVAL 3 DAY)";
	
	public static final String SAVE_BLACK_METALS_RATES_2_DB = "insert into black_market_rates (date, cityCode, currencyCode, opCode, rate, rate_delta) values (now(),?,?,?,?,?)";
	public static final String GET_BLACK_METALS_RATES_FROM_DB = "SELECT DATE_FORMAT(bmr.date, '%d-%m-%Y'), bmr.cityCode, bmr.currencyCode, bmr.opCode, bmr.rate, bmr.rate_delta"
			+ " FROM black_market_rates bmr"
			+ " WHERE bmr.cityCode = ? and bmr.currencyCode = ? and bmr.opCode = ? and bmr.date = (select max(bmr_.date) from black_market_rates bmr_ where bmr_.cityCode = bmr.cityCode and bmr_.currencyCode = bmr.currencyCode and bmr_.opCode = bmr.opCode)";
	public static final String CLEAN_BLACK_MARKET_RATES_TABLE = "delete from black_market_rates where date < DATE_SUB(now(),INTERVAL 3 DAY)";
}