package com.dn.ivan.rates.logic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.dn.ivan.rates.BlackMarketRateItem;
import com.dn.ivan.rates.BlackMarketRatesLst;
import com.dn.ivan.rates.CommercialRateItem;
import com.dn.ivan.rates.CommercialRatesLst;
import com.dn.ivan.rates.Const;
import com.dn.ivan.rates.FuelRateItem;
import com.dn.ivan.rates.FuelRatesLst;
import com.dn.ivan.rates.NbuRatesLst;

public class ManageLogic {
	
	public static NbuRatesLst getNbuRates() throws Exception {

		String xml = sendGet(Const.NBU_SERVICE);

		String r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table nbu_rate\">"));
		String r2 = r1.substring(0, r1.indexOf("</table>") + 8);
		
		NbuRatesLst response = new NbuRatesLst();
		response.nbuRatesList = new NbuSAXParser(r2).getRates();

		return response;
	}

	public static CommercialRatesLst getCommercialRates() throws Exception {

		String xml = "";

		String r1 = "";
		String r2 = "";

		ArrayList<CommercialRateItem> commercialRates = new ArrayList<CommercialRateItem>();

		// ///////////////////////////////////////////////////////

		xml = sendGet(Const.COMMERCIAL_USD_SERVICE);

		r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table\""));
		r2 = r1.substring(0, r1.indexOf("</div>"));

		ArrayList<CommercialRateItem> usdRates = new CommercialSAXParser(r2, "USD").getRates();

		xml = sendGet(Const.COMMERCIAL_EUR_SERVICE);

		r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table\""));
		r2 = r1.substring(0, r1.indexOf("</div>"));

		ArrayList<CommercialRateItem> eurRates = new CommercialSAXParser(r2, "EUR").getRates();

		xml = sendGet(Const.COMMERCIAL_RUB_SERVICE);

		r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table\""));
		r2 = r1.substring(0, r1.indexOf("</div>"));

		ArrayList<CommercialRateItem> rubRates = new CommercialSAXParser(r2, "RUB").getRates();

		// ////////////////////////////////////////////////////////

		commercialRates.addAll(usdRates);
		commercialRates.addAll(eurRates);
		commercialRates.addAll(rubRates);
		
		CommercialRatesLst response = new CommercialRatesLst();
		response.commercialRatesList = commercialRates;

		return response;
	}

	public static void getFuelRatesFromWEB(String regionCode, boolean isSave2DB) throws Exception {
		
		String xml = "";		
		if (regionCode != null && !"".equalsIgnoreCase(regionCode)) {
			xml = sendGet(Const.FUEL_SERVICE + regionCode + "/");
		}
		else {
			xml = sendGet(Const.FUEL_SERVICE);
		}		

		String r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table consulting\">"));
		String r2 = r1.substring(0, r1.indexOf("</table>") + 8);
		
		FuelRatesLst result = new FuelRatesLst();
		result.fuelRatesList = new FuelSAXParser(r2).getRates();
		
		if (regionCode != null && !"".equalsIgnoreCase(regionCode) && isSave2DB) {
			saveFuelRates2DB(result, regionCode);
		}		
	}
	
	public static void getBlackMarketRatesFromWEB(String cityCode, String currencyCode, String opCode, boolean isSave2DB) throws Exception {
		
		String xml = sendGet(Const.BLACK_MARKET_SERVICE + cityCode + "/" + currencyCode + "/?type=" + opCode);

		String r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table local_table-black_market\">"));
		String r2 = r1.substring(0, r1.indexOf("</table>") + 8);
		
		BlackMarketRatesLst result = new BlackMarketRatesLst();
		result.blackMarketRatesList = new BlackMarketSAXParser(r2, cityCode, currencyCode, opCode).getRates();
		
		if (isSave2DB) {
			saveBlackMarketRates2DB(result, cityCode, currencyCode, opCode);
		}
	}
	
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static String sendGet(String url) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", Const.USER_AGENT);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "cp1251"));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}
	
	public static void saveFuelRates2DB(FuelRatesLst fuelRates, String regionCode) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			
			FuelRatesLst previousFuelRates = getFuelRatesFromDB(regionCode);
			
			conn = DBManager.createConnection();
			
			for (int i = 0; i < fuelRates.fuelRatesList.size(); i++) {
				
				FuelRateItem item = fuelRates.fuelRatesList.get(i);
				
				String current_date = getCurrentSQLDate(conn);
				
				double current_a_80 = "".equalsIgnoreCase(item.a_80)? 0: Double.valueOf(item.a_80);
				double current_a_92 = "".equalsIgnoreCase(item.a_92)? 0: Double.valueOf(item.a_92);
				double current_a_95 = "".equalsIgnoreCase(item.a_95)? 0: Double.valueOf(item.a_95);
				double current_dt = "".equalsIgnoreCase(item.dt)? 0: Double.valueOf(item.dt);
				
				String previous_date = "";
				
				double previous_a_80 = 0;
				double previous_a_92 = 0;
				double previous_a_95 = 0;
				double previous_dt = 0;
				
				double previous_a_80_delta = 0;
				double previous_a_92_delta = 0;
				double previous_a_95_delta = 0;
				double previous_dt_delta = 0;
				
				for (int a = 0; previousFuelRates != null && a < previousFuelRates.fuelRatesList.size(); a++) {
					
					if (previousFuelRates.fuelRatesList.get(a).code.equalsIgnoreCase(item.code)) {
						
						previous_date = previousFuelRates.fuelRatesList.get(a).date;
						
						previous_a_80 = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_80)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_80);
						previous_a_92 = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_92)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_92);
						previous_a_95 = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_95)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_95);
						previous_dt = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).dt)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).dt);
						
						previous_a_80_delta = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_80_delta)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_80_delta);
						previous_a_92_delta = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_92_delta)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_92_delta);
						previous_a_95_delta = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).a_95_delta)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).a_95_delta);
						previous_dt_delta = "".equalsIgnoreCase(previousFuelRates.fuelRatesList.get(a).dt_delta)? 0: Double.valueOf(previousFuelRates.fuelRatesList.get(a).dt_delta);
					}
				}
				
				pstmt = conn.prepareStatement(Const.SAVE_FUEL_RATES_2_DB);
				
				pstmt.setString(1, regionCode);
				pstmt.setString(2, item.code);
				pstmt.setDouble(3, current_a_80);
				pstmt.setDouble(4, current_a_92);
				pstmt.setDouble(5, current_a_95);
				pstmt.setDouble(6, current_dt);
				
				if (previous_date.trim().equalsIgnoreCase(current_date.trim())) {
					
					pstmt.setDouble(7, (current_a_80 == 0 || previous_a_80 == 0)? 0: (new BigDecimal(String.valueOf(current_a_80)).subtract(new BigDecimal(String.valueOf(previous_a_80))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_a_80_delta: new BigDecimal(String.valueOf(current_a_80)).subtract(new BigDecimal(String.valueOf(previous_a_80))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
					pstmt.setDouble(8, (current_a_92 == 0 || previous_a_92 == 0)? 0: (new BigDecimal(String.valueOf(current_a_92)).subtract(new BigDecimal(String.valueOf(previous_a_92))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_a_92_delta: new BigDecimal(String.valueOf(current_a_92)).subtract(new BigDecimal(String.valueOf(previous_a_92))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
					pstmt.setDouble(9, (current_a_95 == 0 || previous_a_95 == 0)? 0: (new BigDecimal(String.valueOf(current_a_95)).subtract(new BigDecimal(String.valueOf(previous_a_95))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_a_95_delta: new BigDecimal(String.valueOf(current_a_95)).subtract(new BigDecimal(String.valueOf(previous_a_95))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
					pstmt.setDouble(10, (current_dt == 0 || previous_dt == 0)? 0: (new BigDecimal(String.valueOf(current_dt)).subtract(new BigDecimal(String.valueOf(previous_dt))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 0.0? previous_dt_delta: new BigDecimal(String.valueOf(current_dt)).subtract(new BigDecimal(String.valueOf(previous_dt))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
				}
				else {
					
					pstmt.setDouble(7, (current_a_80 == 0 || previous_a_80 == 0)? 0: new BigDecimal(String.valueOf(current_a_80)).subtract(new BigDecimal(String.valueOf(previous_a_80))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
					pstmt.setDouble(8, (current_a_92 == 0 || previous_a_92 == 0)? 0: new BigDecimal(String.valueOf(current_a_92)).subtract(new BigDecimal(String.valueOf(previous_a_92))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
					pstmt.setDouble(9, (current_a_95 == 0 || previous_a_95 == 0)? 0: new BigDecimal(String.valueOf(current_a_95)).subtract(new BigDecimal(String.valueOf(previous_a_95))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
					pstmt.setDouble(10, (current_dt == 0 || previous_dt == 0)? 0: new BigDecimal(String.valueOf(current_dt)).subtract(new BigDecimal(String.valueOf(previous_dt))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
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
				
				item.a_80_delta = rs.getDouble(9) == 0? "": String.valueOf(rs.getDouble(9));
				item.a_92_delta = rs.getDouble(10) == 0? "": String.valueOf(rs.getDouble(10));
				item.a_95_delta = rs.getDouble(11) == 0? "": String.valueOf(rs.getDouble(11));
				item.dt_delta = rs.getDouble(12) == 0? "": String.valueOf(rs.getDouble(12));
				
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
	
	public static void createJob(Class<? extends Job> jobClass, String identity, int intervalInHours) {
		
		try {
			
			JobDetail job = JobBuilder.newJob(jobClass).withIdentity(identity).build();

			Trigger trigger = TriggerBuilder
					.newTrigger()
					.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(intervalInHours).repeatForever())
					.build();
			
			/*Trigger trigger = TriggerBuilder.newTrigger()
					.startNow()
					.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(12, 0))
					.build();*/

			SchedulerFactory schFactory = new StdSchedulerFactory();
			Scheduler sch = schFactory.getScheduler();
			sch.start();
			sch.scheduleJob(job, trigger);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getCurrentSQLDate(Connection conn) {
		
		String date = "";
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			
			stmt = conn.createStatement();						
			rs = stmt.executeQuery(Const.GET_CURRENT_SQL_DATE);
			
			while (rs.next()) {				
				date = rs.getString(1);
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
				if (stmt != null) {
					stmt.close();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return date;
	}
	
	public static void cleanTables() {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
				
		try {
			
			conn = DBManager.createConnection();
			
			pstmt = conn.prepareStatement(Const.CLEAN_FUEL_RATES_TABLE);
			pstmt.executeUpdate();		
			pstmt.close();
			pstmt = null;
			
			pstmt = conn.prepareStatement(Const.CLEAN_BLACK_MARKET_RATES_TABLE);
			pstmt.executeUpdate();
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
	
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static BlackMarketRatesLst getBlackMarketRates(String cityCode) throws Exception {
		
		BlackMarketRatesLst response = new BlackMarketRatesLst();
		
		ArrayList<String> currencyCode = new ArrayList<String>(Arrays.asList("usd", "eur", "rub"));
		ArrayList<String> opCode = new ArrayList<String>(Arrays.asList("1", "2"));
		
		try {
			
			for(int a = 0; a < currencyCode.size(); a++) {
				
				for(int b = 0; b < opCode.size(); b++) {
					
					try {
						BlackMarketRatesLst result = ManageLogic.getBlackMarketRatesFromDB(cityCode, currencyCode.get(a), opCode.get(b));
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
	
	public static void saveBlackMarketRates2DB(BlackMarketRatesLst blackMarketRates, String cityCode, String currencyCode, String opCode) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			
			BlackMarketRatesLst previousBlackMarketRates = getBlackMarketRatesFromDB(cityCode, currencyCode, opCode);
			
			conn = DBManager.createConnection();
			
			for (int i = 0; i < blackMarketRates.blackMarketRatesList.size(); i++) {
				
				BlackMarketRateItem item = blackMarketRates.blackMarketRatesList.get(i);
				
				String current_date = getCurrentSQLDate(conn);
				
				double current_rate = "".equalsIgnoreCase(item.rate)? 0: Double.valueOf(item.rate);
				
				String previous_date = "";
				
				double previous_rate = 0;								
				double previous_rate_delta = 0;
				
				for (int a = 0; previousBlackMarketRates != null && a < previousBlackMarketRates.blackMarketRatesList.size(); a++) {
					
					if (previousBlackMarketRates.blackMarketRatesList.get(a).cityCode.equalsIgnoreCase(item.cityCode) 
							&& previousBlackMarketRates.blackMarketRatesList.get(a).currencyCode.equalsIgnoreCase(item.currencyCode) 
							&& previousBlackMarketRates.blackMarketRatesList.get(a).opCode.equalsIgnoreCase(item.opCode)) {
						
						previous_date = previousBlackMarketRates.blackMarketRatesList.get(a).date;
						
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