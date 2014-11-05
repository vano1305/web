package com.dn.ivan.rates.logic.context_listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.dn.ivan.rates.logic.ManageLogic;
import com.dn.ivan.rates.logic.SaveFuelRates2DBJob;

public class LoadRatesContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {		
				
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		ManageLogic.createJob(SaveFuelRates2DBJob.class, "fuelJob", 4);
	}
}
