package com.dn.ivan.rates.logic;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SaveCommercialRates2DBJob implements Job {
	
	public void execute(JobExecutionContext context) throws JobExecutionException {		
		CommercialManageLogic.loadCommercialRates();
	}
}