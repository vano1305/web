package com.dn.ivan.rates.logic.context_listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.dn.ivan.rates.logic.CleanTablesJob;
import com.dn.ivan.rates.logic.ManageLogic;

public class CleanTablesContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {		
				
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		ManageLogic.createJob(CleanTablesJob.class, "cleanJob", 8);
	}
}
