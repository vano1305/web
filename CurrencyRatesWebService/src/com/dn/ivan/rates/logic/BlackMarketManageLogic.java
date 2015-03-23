package com.dn.ivan.rates.logic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import com.dn.ivan.rates.BlackMarketRateItem;
import com.dn.ivan.rates.BlackMarketRatesLst;
import com.dn.ivan.rates.Const;

public class BlackMarketManageLogic {
	
	public static void loadBlackMarketRates() {
		
		ArrayList<String> cityCode = new ArrayList<String>(Arrays.asList("dnepropetrovsk", "kiev", "lvov", "odessa", "harkov"));
		ArrayList<String> currencyCode = new ArrayList<String>(Arrays.asList("usd", "eur", "rub"));
		ArrayList<String> opCode = new ArrayList<String>(Arrays.asList("1", "2"));
		
		for(int i = 0; i < cityCode.size(); i++) {
			
			for(int a = 0; a < currencyCode.size(); a++) {
				
				for(int b = 0; b < opCode.size(); b++) {
					
					try {
						getBlackMarketRatesFromWEB(cityCode.get(i), currencyCode.get(a), opCode.get(b), true);
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static BlackMarketRatesLst getBlackMarketRates(String cityCode) throws Exception {
		
		BlackMarketRatesLst response = new BlackMarketRatesLst();
		
		ArrayList<String> currencyCode = new ArrayList<String>(Arrays.asList("usd", "eur", "rub"));
		ArrayList<String> opCode = new ArrayList<String>(Arrays.asList("1", "2"));
		
		try {
			
			for(int a = 0; a < currencyCode.size(); a++) {
				
				for(int b = 0; b < opCode.size(); b++) {
					
					try {
						BlackMarketRatesLst result = BlackMarketManageLogic.getBlackMarketRatesFromDB(cityCode, currencyCode.get(a), opCode.get(b));
						for (int i = 0; i < result.blackMarketRatesList.size(); i++) {
							response.blackMarketRatesList.add(result.blackMarketRatesList.get(i));
						}
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return response;
	}
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void getBlackMarketRatesFromWEB(String cityCode, String currencyCode, String opCode, boolean isSave2DB) throws Exception {
		
		String xml = ManageLogic.sendGet(Const.BLACK_MARKET_SERVICE + cityCode + "/" + currencyCode + "/?type=" + opCode);

		String r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table local_table-black_market\">"));
		String r2 = r1.substring(0, r1.indexOf("</table>") + 8);
		
		BlackMarketRatesLst result = new BlackMarketRatesLst();
		result.blackMarketRatesList = new BlackMarketSAXParser(r2, cityCode, currencyCode, opCode).getRates();
		
		if (isSave2DB) {
			saveBlackMarketRates2DB(result, cityCode, currencyCode, opCode);
		}
	}
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static BlackMarketRatesLst getBlackMarketRatesFromDB(String cityCode, String currencyCode, String opCode) throws Exception {
		
		BlackMarketRatesLst response = new BlackMarketRatesLst();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			
			conn = DBManager.createConnection();
			
			pstmt = conn.prepareStatement(Const.GET_BLACK_METALS_RATES_FROM_DB);
			
			pstmt.setString(1, cityCode);
			pstmt.setString(2, currencyCode);
			pstmt.setString(3, opCode);
			
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				
				BlackMarketRateItem item = new BlackMarketRateItem();
				
				item.date = rs.getString(1);
				item.cityCode = rs.getString(2);
				item.currencyCode = rs.getString(3);
				item.opCode = rs.getString(4);
				
				item.rate = rs.getDouble(5) == 0? "": String.valueOf(rs.getDouble(5));
				item.rate_delta = rs.getDouble(6) == 0? "": String.valueOf(rs.getDouble(6));
				
				response.blackMarketRatesList.add(item);
			}			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return response;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void saveBlackMarketRates2DB(BlackMarketRatesLst blackMarketRates, String cityCode, String currencyCode, String opCode) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			
			BlackMarketRatesLst previousBlackMarketRates = getBlackMarketRatesFromDB(cityCode, currencyCode, opCode);
			
			conn = DBManager.createConnection();
			
			for (int i = 0; i < blackMarketRates.blackMarketRatesList.size(); i++) {
				
				BlackMarketRateItem item = blackMarketRates.blackMarketRatesList.get(i);
				
				String current_date = ManageLogic.getCurrentSQLDate(conn);
				
				double current_rate = "".equalsIgnoreCase(item.rate)? 0: Double.valueOf(item.rate);
				
				String previous_date = "";
				
				double previous_rate = 0;								
				double previous_rate_delta = 0;
				
				for (int a = 0; previousBlackMarketRates != null && a < previousBlackMarketRates.blackMarketRatesList.size(); a++) {
					
					if (previousBlackMarketRates.blackMarketRatesList.get(a).cityCode.equalsIgnoreCase(item.cityCode) 
							&& previousBlackMarketRates.blackMarketRatesList.get(a).currencyCode.equalsIgnoreCase(item.currencyCode) 
							&& previousBlackMarketRates.blackMarketRatesList.get(a).opCode.equalsIgnoreCase(item.opCode)) {
						
						previous_date = previousBlackMarketRates.blackMarketRatesList.get(a).date.substring(0, previousBlackMarketRates.blackMarketRatesList.get(a).date.indexOf(" "));
						
						previous_rate = "".equalsIgnoreCase(previousBlackMarketRates.blackMarketRatesList.get(a).rate)? 0: Double.valueOf(previousBlackMarketRates.blackMarketRatesList.get(a).rate);						
						previous_rate_delta = "".equalsIgnoreCase(previousBlackMarketRates.blackMarketRatesList.get(a).rate_delta)? 0: Double.valueOf(previousBlackMarketRates.blackMarketRatesList.get(a).rate_delta);
					}
				}
				
				pstmt = conn.prepareStatement(Const.SAVE_BLACK_METALS_RATES_2_DB);
				
				pstmt.setString(1, cityCode);
				pstmt.setString(2, currencyCode);
				pstmt.setString(3, opCode);
				pstmt.setDouble(4, current_rate);
				
				if (previous_date.trim().equalsIgnoreCase(current_date.trim())) {
					pstmt.setDouble(5, (current_rate == 0 || previous_rate == 0)? 0: (new BigDecimal(String.valueOf(current_rate)).subtract(new BigDecimal(String.valueOf(previous_rate))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_rate_delta: new BigDecimal(String.valueOf(current_rate)).subtract(new BigDecimal(String.valueOf(previous_rate))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
				}
				else {					
					pstmt.setDouble(5, (current_rate == 0 || previous_rate == 0)? 0: new BigDecimal(String.valueOf(current_rate)).subtract(new BigDecimal(String.valueOf(previous_rate))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
				}
				
				pstmt.execute();
				pstmt.close();
				pstmt = null;				
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}