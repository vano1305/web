package com.dn.ivan.rates.logic.context_listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.dn.ivan.rates.logic.ManageLogic;
import com.dn.ivan.rates.logic.SaveBlackMarketRates2DBJob;
import com.dn.ivan.rates.logic.SaveCommercialRates2DBJob;
import com.dn.ivan.rates.logic.SaveFuelRates2DBJob;
import com.dn.ivan.rates.logic.SaveNbuHistory2DBJob;

public class LoadRatesContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
				
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		ManageLogic.createDailyJob(SaveFuelRates2DBJob.class, "FuelJob1", 10, 00, 24);
		ManageLogic.createDailyJob(SaveFuelRates2DBJob.class, "FuelJob2", 14, 00, 24);
		ManageLogic.createDailyJob(SaveFuelRates2DBJob.class, "FuelJob3", 16, 00, 24);
		ManageLogic.createDailyJob(SaveFuelRates2DBJob.class, "FuelJob4", 18, 00, 24);
		
		ManageLogic.createDailyJob(SaveBlackMarketRates2DBJob.class, "BlackMarketJob", 8, 00, 2);
		
		ManageLogic.createDailyJob(SaveCommercialRates2DBJob.class, "CommercialJob1", 10, 45, 24);
		ManageLogic.createDailyJob(SaveCommercialRates2DBJob.class, "CommercialJob2", 13, 00, 24);
		
		ManageLogic.createDailyJob(SaveNbuHistory2DBJob.class, "NbuHistory", 15, 00, 24);
	}
}