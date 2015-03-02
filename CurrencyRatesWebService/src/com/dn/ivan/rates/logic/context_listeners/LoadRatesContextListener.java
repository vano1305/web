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
		
		ManageLogic.createDailyJob(SaveFuelRates2DBJob.class, "FuelJob1", 8, 0, 24);
		ManageLogic.createDailyJob(SaveFuelRates2DBJob.class, "FuelJob2", 12, 0, 24);
		ManageLogic.createDailyJob(SaveFuelRates2DBJob.class, "FuelJob3", 14, 0, 24);
		ManageLogic.createDailyJob(SaveFuelRates2DBJob.class, "FuelJob4", 16, 0, 24);
		
		ManageLogic.createDailyJob(SaveBlackMarketRates2DBJob.class, "BlackMarketJob", 6, 0, 3);
		
		ManageLogic.createDailyJob(SaveCommercialRates2DBJob.class, "CommercialJob1", 8, 45, 24);
		ManageLogic.createDailyJob(SaveCommercialRates2DBJob.class, "CommercialJob2", 11, 0, 24);
		
		ManageLogic.createDailyJob(SaveNbuHistory2DBJob.class, "NbuHistory", 12, 45, 24);
	}
}
