package com.dn.ivan.rates.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dn.ivan.rates.BlackMarketRatesLst;
import com.dn.ivan.rates.CommercialRatesLst;
import com.dn.ivan.rates.FuelRatesLst;
import com.dn.ivan.rates.NbuHistoryItem;
import com.dn.ivan.rates.NbuRatesLst;
import com.dn.ivan.rates.logic.BlackMarketManageLogic;
import com.dn.ivan.rates.logic.CommercialManageLogic;
import com.dn.ivan.rates.logic.FuelManageLogic;
import com.dn.ivan.rates.logic.NbuHistoryManageLogic;
import com.dn.ivan.rates.logic.NbuManageLogic;

@Path("/manage_rates")
@Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
public class RatesService {
	
	@GET
	@Path("/nbu")
	public Response lstNbuRates() {
		
		try {			
			return Response.ok(NbuManageLogic.getNbuRates()).build();
		}
		catch (Exception e) {
			e.printStackTrace();	
			return Response.ok(new NbuRatesLst()).build();
		}		
	}
	
	@GET
	@Path("/commercial")
	public Response lstCommercialRates() {
		
		try {
			return Response.ok(CommercialManageLogic.getCommercialRates()).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(new CommercialRatesLst()).build();
		}		
	}
	
	@GET
	@Path("/fuel")
	public Response lstFuelRates(@QueryParam("region") String regionCode) {
		
		try {			
			return Response.ok(FuelManageLogic.getFuelRatesFromDB(regionCode)).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(new FuelRatesLst()).build();
		}		
	}
	
	@GET
	@Path("/black_market")
	public Response lstBlackMarketRates(@QueryParam("city") String cityCode) {
		
		try {			
			return Response.ok(BlackMarketManageLogic.getBlackMarketRates(cityCode)).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(new BlackMarketRatesLst()).build();
		}
	}
	
	@GET
	@Path("/nbu_history")
	public Response getNbuHistory(@QueryParam("currency") String currencyCode, @QueryParam("date1") String date1, @QueryParam("date2") String date2) {
		
		try {			
			return Response.ok(NbuHistoryManageLogic.getNbuHistory(currencyCode, date1, date2)).build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(new NbuHistoryItem()).build();
		}
	}
	
	@GET
	@Path("/load_nbu_history")
	public Response loadNbuHistory(@QueryParam("days") String days) {
		
		try {			
			NbuHistoryManageLogic.getNbuRatesForHistory(Integer.valueOf(days));
			return Response.ok().build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(new NbuHistoryItem()).build();
		}
	}
	
	@GET
	@Path("/reload_all")
	public Response reloadAllData() {
		
		try {
			
			FuelManageLogic.loadFuelRates();
			CommercialManageLogic.loadCommercialRates();
			BlackMarketManageLogic.loadBlackMarketRates();
			
			NbuHistoryManageLogic.getNbuRatesForHistory(1);
			
			return Response.ok().build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.ok(new NbuHistoryItem()).build();
		}
	}
}