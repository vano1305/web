package com.dn.ivan.rates.logic;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CleanTablesJob implements Job {
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			ManageLogic.cleanTables();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}