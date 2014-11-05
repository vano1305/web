package com.dn.ivan.rates.logic;

import java.util.ArrayList;
import java.util.Arrays;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SaveFuelRates2DBJob implements Job {
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		ArrayList<String> regionList = new ArrayList<String>(
				Arrays.asList("4","5","28","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27"));
		
		for(int i = 0; i < regionList.size(); i++) {
			
			try {
				ManageLogic.getFuelRatesFromWEB(regionList.get(i), true);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}