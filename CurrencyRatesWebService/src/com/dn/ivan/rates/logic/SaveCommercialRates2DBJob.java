package com.dn.ivan.rates.logic;

import java.util.ArrayList;
import java.util.Arrays;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SaveCommercialRates2DBJob implements Job {
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		ArrayList<String> currencyCode = new ArrayList<String>(Arrays.asList("USD", "EUR", "RUB"));
				
		for(int i = 0; i < currencyCode.size(); i++) {
			
			try {
				CommercialManageLogic.getCommercialRatesFromWEB(currencyCode.get(i), true);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}