package com.dn.ivan.rates.logic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.dn.ivan.rates.Const;
import com.dn.ivan.rates.NbuHistoryItem;
import com.dn.ivan.rates.NbuRateItem;
import com.dn.ivan.rates.NbuRatesLst;

public class NbuManageLogic {
	
	public static NbuRatesLst getNbuRates() throws Exception {

		String xml = ManageLogic.sendGet(Const.NBU_SERVICE);

		String r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table nbu_rate\">"));
		String r2 = r1.substring(0, r1.indexOf("</table>") + 8);
		
		NbuRatesLst response = new NbuRatesLst();
		response.nbuRatesList = new NbuSAXParser(r2).getRates();

		return response;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void getNbuRatesForHistory(int days) throws Exception {
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("MM");
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy");
		
		Calendar calendar = new GregorianCalendar();
				
		for (int i = 0; i < days; i++) {
			
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_YEAR, -i);
			
			String xml = ManageLogic.sendGet(Const.NBU_SERVICE + "?d=" + sdf1.format(calendar.getTime()) + "&m=" + sdf2.format(calendar.getTime()) + "&y=" + sdf3.format(calendar.getTime()));

			String r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table nbu_rate\">"));
			String r2 = r1.substring(0, r1.indexOf("</table>") + 8);
			
			NbuRatesLst response = new NbuRatesLst();
			response.nbuRatesList = new NbuSAXParser(r2).getRates();
			
			saveNbuHistory2DB(response, calendar.getTime());
		}
	}
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static NbuHistoryItem getNbuHistory(String currencyCode, String date1, String date2) throws Exception {
		
		NbuHistoryItem response = new NbuHistoryItem();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			
			conn = DBManager.createConnection();
			
			pstmt = conn.prepareStatement(Const.GET_NBU_HISTORY_FROM_DB);
			
			pstmt.setString(1, currencyCode);
			pstmt.setString(2, date1);
			pstmt.setString(3, date2);
			
			rs = pstmt.executeQuery();
			
			response.currencyCode = currencyCode;
			response.date1 = date1;
			response.date2 = date2;
			
			while (rs.next()) {
				
				String date = rs.getString(1);
				String rate = new BigDecimal(rs.getDouble(2)).setScale(7, BigDecimal.ROUND_HALF_UP).toPlainString();
				
				response.history = response.history + date + ":" + rate + ";";
			}
			
			if (!"".equalsIgnoreCase(response.history)) {
				response.history = response.history.substring(0, response.history.lastIndexOf(";"));
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
	
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void saveNbuHistory2DB(NbuRatesLst rates, Date date) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			
			conn = DBManager.createConnection();
			
			for (int i = 0; i < rates.nbuRatesList.size(); i++) {
				
				NbuRateItem item = rates.nbuRatesList.get(i);
				
				pstmt = conn.prepareStatement("insert nbu_rates (date, digitalCode, charCode, size, rate) values (?,?,?,?,?)");
				
				pstmt.setString(1, sdf.format(date));
				pstmt.setString(2, item.code.length() == 2? ("0" + item.code): item.code);
				pstmt.setString(3, item.char3);
								
				if ("BYR;RUB".indexOf(item.char3) != -1) {
					pstmt.setInt(4, 10);
				}
				else if ("HUF;JPY".indexOf(item.char3) != -1) {
					pstmt.setInt(4, 1000);
				}
				else {
					pstmt.setInt(4, 100);
				}
				
				if ("BYR;RUB".indexOf(item.char3) != -1) {
					pstmt.setDouble(5, new BigDecimal(item.rate).multiply(new BigDecimal("10")).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
				}
				else if ("HUF;JPY".indexOf(item.char3) != -1) {
					pstmt.setDouble(5, new BigDecimal(item.rate).multiply(new BigDecimal("1000")).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
				}
				else {
					pstmt.setDouble(5, new BigDecimal(item.rate).multiply(new BigDecimal("100")).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
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