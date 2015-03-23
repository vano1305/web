package com.dn.ivan.rates.logic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import com.dn.ivan.rates.Const;
import com.dn.ivan.rates.FuelRateItem;
import com.dn.ivan.rates.FuelRatesLst;

public class FuelManageLogic {
	
	public static void loadFuelRates() {
		
		FuelRatesLst result_minfin = null;		
		try {
			result_minfin = getFuelRatesFromWEB_Minfin();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		ArrayList<String> regionList = new ArrayList<String>(
				Arrays.asList("4","5","28","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27"));
		
		for(int i = 0; i < regionList.size(); i++) {
			
			try {
				getFuelRatesFromWEB(regionList.get(i), true, result_minfin);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void getFuelRatesFromWEB(String regionCode, boolean isSave2DB, FuelRatesLst minfin) throws Exception {
		
		String xml = "";		
		if (regionCode != null && !"".equalsIgnoreCase(regionCode)) {
			xml = ManageLogic.sendGet(Const.FUEL_SERVICE + regionCode + "/");
		}
		else {
			xml = ManageLogic.sendGet(Const.FUEL_SERVICE);
		}		

		String r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table consulting\">"));
		String r2 = r1.substring(0, r1.indexOf("</table>") + 8);
		
		FuelRatesLst result = new FuelRatesLst();
		result.fuelRatesList = new FuelSAXParser(r2).getRates();
		
		// //////////////////////////////////////////////////////////////////////////////////////
		
		for (int i = 0; i < result.fuelRatesList.size(); i++) {
			
			for (int a = 0; minfin != null && a < minfin.fuelRatesList.size(); a++) {
				
				if (result.fuelRatesList.get(i).code.equalsIgnoreCase(minfin.fuelRatesList.get(a).code) && regionCode.equalsIgnoreCase(minfin.fuelRatesList.get(a).regionCode)) {
					result.fuelRatesList.get(i).spg = minfin.fuelRatesList.get(a).spg;
				}
			}
		}
		
		// //////////////////////////////////////////////////////////////////////////////////////
		
		if (regionCode != null && !"".equalsIgnoreCase(regionCode) && isSave2DB) {
			saveFuelRates2DB(result, regionCode);
		}		
	}
	
	public static FuelRatesLst getFuelRatesFromWEB_Minfin() throws Exception {
		
		String xml = ManageLogic.sendGet(Const.FUEL_SERVICE_MINFIN);		

		String r1 = xml.toString().substring(xml.toString().indexOf("<table border=0 cellspacing=0 cellpadding=2 class='zebra'>"));
		String r2 = r1.substring(0, r1.indexOf("</table>") + 8);
		
		FuelRatesLst result = new FuelRatesLst();
		result.fuelRatesList = new FuelSAXParserMinfin(r2).getRates();
		
		return result;
	}
	
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void saveFuelRates2DB(FuelRatesLst fuelRates, String regionCode) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			
			FuelRatesLst previousFuelRates = getFuelRatesFromDB(regionCode);
			
			conn = DBManager.createConnection();
			
			for (int i = 0; i < fuelRates.fuelRatesList.size(); i++) {
				
				FuelRateItem item = fuelRates.fuelRatesList.get(i);
				
				String current_date = ManageLogic.getCurrentSQLDate(conn);
				
				double current_a_80 = "".equalsIgnoreCase(item.a_80)? 0: Double.valueOf(item.a_80);
				double current_a_92 = "".equalsIgnoreCase(item.a_92)? 0: Double.valueOf(item.a_92);
				double current_a_95 = "".equalsIgnoreCase(item.a_95)? 0: Double.valueOf(item.a_95);
				double current_dt = "".equalsIgnoreCase(item.dt)? 0: Double.valueOf(item.dt);
				double current_spg = "".equalsIgnoreCase(item.spg)? 0: Double.valueOf(item.spg);
				
				String previous_date = "";
				
				double previous_a_80 = 0;
				double previous_a_92 = 0;
				double previous_a_95 = 0;
				double previous_dt = 0;
				double previous_spg = 0;
				
				double previous_a_80_delta = 0;
				double previous_a_92_delta = 0;
				double previous_a_95_delta = 0;
				double previous_dt_delta = 0;
				double previous_spg_delta = 0;
				
				for (int a = 0; previousFuelRates != null && a < previousFuelRates.fuelRatesList.size(); a++) {
					
					if (previousFuelRates.fuelRatesList.get(a).code.equalsIgnoreCase(item.code)) {
						
						previous_date = previousFuelRates.fuelRatesList.get(a).date.substring(0, previousFuelRates.fuelRatesList.get(a).date.indexOf(" "));
						
						previous_a_80 = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_80)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_80);
						previous_a_92 = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_92)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_92);
						previous_a_95 = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_95)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_95);
						previous_dt = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).dt)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).dt);
						previous_spg = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).spg)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).spg);
						
						previous_a_80_delta = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_80_delta)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_80_delta);
						previous_a_92_delta = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_92_delta)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_92_delta);
						previous_a_95_delta = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_95_delta)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_95_delta);
						previous_dt_delta = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).dt_delta)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).dt_delta);
						previous_spg_delta = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).spg_delta)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).spg_delta);
					}
				}
				
				pstmt = conn.prepareStatement(Const.SAVE_FUEL_RATES_2_DB);
				
				pstmt.setString(1, regionCode);
				pstmt.setString(2, item.code);
				pstmt.setDouble(3, current_a_80);
				pstmt.setDouble(4, current_a_92);
				pstmt.setDouble(5, current_a_95);
				pstmt.setDouble(6, current_dt);
				pstmt.setDouble(7, current_spg);
				
				if (previous_date.trim().equalsIgnoreCase(current_date.trim())) {
					
					pstmt.setDouble(8, (current_a_80 == 0 || previous_a_80 == 0)? 0: (new BigDecimal(String.valueOf(current_a_80)).subtract(new BigDecimal(String.valueOf(previous_a_80))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_a_80_delta: new BigDecimal(String.valueOf(current_a_80)).subtract(new BigDecimal(String.valueOf(previous_a_80))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
					pstmt.setDouble(9, (current_a_92 == 0 || previous_a_92 == 0)? 0: (new BigDecimal(String.valueOf(current_a_92)).subtract(new BigDecimal(String.valueOf(previous_a_92))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_a_92_delta: new BigDecimal(String.valueOf(current_a_92)).subtract(new BigDecimal(String.valueOf(previous_a_92))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
					pstmt.setDouble(10, (current_a_95 == 0 || previous_a_95 == 0)? 0: (new BigDecimal(String.valueOf(current_a_95)).subtract(new BigDecimal(String.valueOf(previous_a_95))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_a_95_delta: new BigDecimal(String.valueOf(current_a_95)).subtract(new BigDecimal(String.valueOf(previous_a_95))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
					pstmt.setDouble(11, (current_dt == 0 || previous_dt == 0)? 0: (new BigDecimal(String.valueOf(current_dt)).subtract(new BigDecimal(String.valueOf(previous_dt))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_dt_delta: new BigDecimal(String.valueOf(current_dt)).subtract(new BigDecimal(String.valueOf(previous_dt))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
					pstmt.setDouble(12, (current_spg == 0 || previous_spg == 0)? 0: (new BigDecimal(String.valueOf(current_spg)).subtract(new BigDecimal(String.valueOf(previous_spg))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_spg_delta: new BigDecimal(String.valueOf(current_spg)).subtract(new BigDecimal(String.valueOf(previous_spg))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
				}
				else {
					
					pstmt.setDouble(8, (current_a_80 == 0 || previous_a_80 == 0)? 0: new BigDecimal(String.valueOf(current_a_80)).subtract(new BigDecimal(String.valueOf(previous_a_80))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
					pstmt.setDouble(9, (current_a_92 == 0 || previous_a_92 == 0)? 0: new BigDecimal(String.valueOf(current_a_92)).subtract(new BigDecimal(String.valueOf(previous_a_92))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
					pstmt.setDouble(10, (current_a_95 == 0 || previous_a_95 == 0)? 0: new BigDecimal(String.valueOf(current_a_95)).subtract(new BigDecimal(String.valueOf(previous_a_95))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
					pstmt.setDouble(11, (current_dt == 0 || previous_dt == 0)? 0: new BigDecimal(String.valueOf(current_dt)).subtract(new BigDecimal(String.valueOf(previous_dt))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
					pstmt.setDouble(12, (current_spg == 0 || previous_spg == 0)? 0: new BigDecimal(String.valueOf(current_spg)).subtract(new BigDecimal(String.valueOf(previous_spg))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
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
	
	public static FuelRatesLst getFuelRatesFromDB(String regionCode) throws Exception {
		
		FuelRatesLst response = new FuelRatesLst();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			
			conn = DBManager.createConnection();
			
			pstmt = conn.prepareStatement(Const.GET_FUEL_RATES_FROM_DB);
			pstmt.setString(1, regionCode);						
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				
				FuelRateItem item = new FuelRateItem();
				
				item.date = rs.getString(1);
				item.code = rs.getString(3);
				item.name = rs.getString(4);
				
				item.a_80 = rs.getDouble(5) == 0? "": String.valueOf(rs.getDouble(5));
				item.a_92 = rs.getDouble(6) == 0? "": String.valueOf(rs.getDouble(6));
				item.a_95 = rs.getDouble(7) == 0? "": String.valueOf(rs.getDouble(7));
				item.dt = rs.getDouble(8) == 0? "": String.valueOf(rs.getDouble(8));
				item.spg = rs.getDouble(9) == 0? "": String.valueOf(rs.getDouble(9));
				
				item.a_80_delta = rs.getDouble(10) == 0? "": String.valueOf(rs.getDouble(10));
				item.a_92_delta = rs.getDouble(11) == 0? "": String.valueOf(rs.getDouble(11));
				item.a_95_delta = rs.getDouble(12) == 0? "": String.valueOf(rs.getDouble(12));
				item.dt_delta = rs.getDouble(13) == 0? "": String.valueOf(rs.getDouble(13));
				item.spg_delta = rs.getDouble(14) == 0? "": String.valueOf(rs.getDouble(14));
				
				response.fuelRatesList.add(item);
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
}