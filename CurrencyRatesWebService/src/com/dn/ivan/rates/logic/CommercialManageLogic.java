package com.dn.ivan.rates.logic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import com.dn.ivan.rates.CommercialRateItem;
import com.dn.ivan.rates.CommercialRatesLst;
import com.dn.ivan.rates.Const;

public class CommercialManageLogic {
	
	public static CommercialRatesLst getCommercialRates() throws Exception {
		
		CommercialRatesLst response = new CommercialRatesLst();
		
		ArrayList<String> currencyCode = new ArrayList<String>(Arrays.asList("USD", "EUR", "RUB"));
		
		try {
			
			for(int a = 0; a < currencyCode.size(); a++) {
				
				try {
					CommercialRatesLst result = CommercialManageLogic.getCommercialRatesFromDB(currencyCode.get(a));
					for (int i = 0; i < result.commercialRatesList.size(); i++) {
						response.commercialRatesList.add(result.commercialRatesList.get(i));
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return response;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void getCommercialRatesFromWEB(String currencyCode, boolean isSave2DB) throws Exception {

		String xml = "";

		ArrayList<CommercialRateItem> commercialRates = new ArrayList<CommercialRateItem>();

		// ///////////////////////////////////////////////////////

		xml = ManageLogic.sendGet(new String(Const.COMMERCIAL_SERVICE).replaceAll("#ccy#", currencyCode.toLowerCase()));

		xml = xml.substring(xml.toString().indexOf("<table class=\"local_table\""));
		xml = xml.substring(0, xml.indexOf("</div>"));

		commercialRates = new CommercialSAXParser(xml, currencyCode).getRates();

		CommercialRatesLst response = new CommercialRatesLst();
		response.commercialRatesList = commercialRates;
		
		if (currencyCode != null && !"".equalsIgnoreCase(currencyCode) && isSave2DB) {
			saveCommercialRates2DB(response, currencyCode);
		}
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static CommercialRatesLst getCommercialRatesFromDB(String currencyCode) throws Exception {
		
		CommercialRatesLst response = new CommercialRatesLst();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			
			conn = DBManager.createConnection();
			
			pstmt = conn.prepareStatement(Const.GET_COMMERCIAL_RATES_FROM_DB);
			pstmt.setString(1, currencyCode);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				
				CommercialRateItem item = new CommercialRateItem();
				
				item.date = rs.getString(1);				
				item.sourceUrl = rs.getString(2);
				item.codeAlpha = rs.getString(3);
				
				item.rateBuy = rs.getDouble(4) == 0? "0": String.valueOf(rs.getDouble(4));
				item.rateBuyDelta = rs.getDouble(5) == 0? "0": String.valueOf(rs.getDouble(5));
				item.rateSale = rs.getDouble(6) == 0? "0": String.valueOf(rs.getDouble(6));
				item.rateSaleDelta = rs.getDouble(7) == 0? "0": String.valueOf(rs.getDouble(7));
				
				response.commercialRatesList.add(item);
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
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void saveCommercialRates2DB(CommercialRatesLst commercialRates, String currencyCode) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			
			CommercialRatesLst previousCommercialRates = getCommercialRatesFromDB(currencyCode);
			
			conn = DBManager.createConnection();
			
			for (int i = 0; i < commercialRates.commercialRatesList.size(); i++) {
				
				CommercialRateItem item = commercialRates.commercialRatesList.get(i);
				
				String current_date = ManageLogic.getCurrentSQLDate(conn);
				
				double current_buy = "".equalsIgnoreCase(item.rateBuy)? 0: Double.valueOf(item.rateBuy);
				double current_sale = "".equalsIgnoreCase(item.rateSale)? 0: Double.valueOf(item.rateSale);
								
				String previous_date = "";
				
				double previous_buy = 0;
				double previous_sale = 0;
				
				double previous_buy_delta = 0;
				double previous_sale_delta = 0;
				
				for (int a = 0; previousCommercialRates != null && a < previousCommercialRates.commercialRatesList.size(); a++) {
					
					if (previousCommercialRates.commercialRatesList.get(a).sourceUrl.equalsIgnoreCase(item.sourceUrl)) {
						
						previous_date = previousCommercialRates.commercialRatesList.get(a).date;
						
						previous_buy = "".equalsIgnoreCase(previousCommercialRates.commercialRatesList.get(a).rateBuy)? 0: Double.valueOf(previousCommercialRates.commercialRatesList.get(a).rateBuy);
						previous_sale = "".equalsIgnoreCase(previousCommercialRates.commercialRatesList.get(a).rateSale)? 0: Double.valueOf(previousCommercialRates.commercialRatesList.get(a).rateSale);
						
						previous_buy_delta = "".equalsIgnoreCase(previousCommercialRates.commercialRatesList.get(a).rateBuyDelta)? 0: Double.valueOf(previousCommercialRates.commercialRatesList.get(a).rateBuyDelta);
						previous_sale_delta = "".equalsIgnoreCase(previousCommercialRates.commercialRatesList.get(a).rateSaleDelta)? 0: Double.valueOf(previousCommercialRates.commercialRatesList.get(a).rateSaleDelta);
					}
				}
				
				pstmt = conn.prepareStatement(Const.SAVE_COMMERCIAL_RATES_2_DB);
				
				pstmt.setString(1, item.sourceUrl);
				pstmt.setString(2, currencyCode);
				pstmt.setDouble(3, current_buy);
				pstmt.setDouble(4, current_sale);
				
				if (previous_date.trim().equalsIgnoreCase(current_date.trim())) {
					
					pstmt.setDouble(5, (current_buy == 0 || previous_buy == 0)? 0: (new BigDecimal(String.valueOf(current_buy)).subtract(new BigDecimal(String.valueOf(previous_buy))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_buy_delta: new BigDecimal(String.valueOf(current_buy)).subtract(new BigDecimal(String.valueOf(previous_buy))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
					pstmt.setDouble(6, (current_sale == 0 || previous_sale == 0)? 0: (new BigDecimal(String.valueOf(current_sale)).subtract(new BigDecimal(String.valueOf(previous_sale))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_sale_delta: new BigDecimal(String.valueOf(current_sale)).subtract(new BigDecimal(String.valueOf(previous_sale))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
				}
				else {
					
					pstmt.setDouble(5, (current_buy == 0 || previous_buy == 0)? 0: new BigDecimal(String.valueOf(current_buy)).subtract(new BigDecimal(String.valueOf(previous_buy))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
					pstmt.setDouble(6, (current_sale == 0 || previous_sale == 0)? 0: new BigDecimal(String.valueOf(current_sale)).subtract(new BigDecimal(String.valueOf(previous_sale))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
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