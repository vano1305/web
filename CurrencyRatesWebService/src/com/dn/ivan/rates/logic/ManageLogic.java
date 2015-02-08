package com.dn.ivan.rates.logic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.dn.ivan.rates.Const;

public class ManageLogic {
	
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
	
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
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
	
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
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
	
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
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
			pstmt.close();
			pstmt = null;
			
			pstmt = conn.prepareStatement(Const.CLEAN_COMMERCIAL_RATES_TABLE);
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
}