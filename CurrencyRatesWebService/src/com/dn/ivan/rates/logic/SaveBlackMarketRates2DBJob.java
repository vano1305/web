package com.dn.ivan.rates.logic;

import java.util.ArrayList;
import java.util.Arrays;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SaveBlackMarketRates2DBJob implements Job {
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		ArrayList<String> cityCode = new ArrayList<String>(Arrays.asList("dnepropetrovsk", "kiev", "lvov", "odessa", "harkov"));
		ArrayList<String> currencyCode = new ArrayList<String>(Arrays.asList("usd", "eur", "rub"));
		ArrayList<String> opCode = new ArrayList<String>(Arrays.asList("1", "2"));
		
		for(int i = 0; i < cityCode.size(); i++) {
			
			for(int a = 0; a < currencyCode.size(); a++) {
				
				for(int b = 0; b < opCode.size(); b++) {
					
					try {
						ManageLogic.getBlackMarketRatesFromWEB(cityCode.get(i), currencyCode.get(a), opCode.get(b), true);
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}