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

public class NbuHistoryManageLogic {
	
	public static void getNbuRatesForHistory(int days) throws Exception {
		
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
		Calendar calendar = new GregorianCalendar();
				
		for (int i = 0; i < days; i++) {
			
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_YEAR, -i);
			
			String xml = ManageLogic.sendGet("http://pfsoft.com.ua/service/currency/?date=" + sdf.format(calendar.getTime()));

			NbuRatesLst response = new NbuRatesLst();
			response.nbuRatesList = new NbuHistorySAXParser(xml).getRates();
			
			saveNbuHistory2DB(response, calendar.getTime());
		}
	}

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
				pstmt.setString(2, item.code);
				pstmt.setString(3, item.char3);
				pstmt.setInt(4, Integer.valueOf(item.size));
				pstmt.setDouble(5, Double.valueOf(item.rate));
				
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