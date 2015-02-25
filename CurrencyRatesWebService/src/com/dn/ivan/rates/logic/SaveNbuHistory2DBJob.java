package com.dn.ivan.rates.logic;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SaveNbuHistory2DBJob implements Job {
	
	public void execute(JobExecutionContext context) throws JobExecutionException {		
		try {
			NbuHistoryManageLogic.getNbuRatesForHistory(1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}