package com.dn.ivan.rates.logic;

import java.util.ArrayList;

import com.dn.ivan.rates.CommercialRateItem;
import com.dn.ivan.rates.CommercialRatesLst;
import com.dn.ivan.rates.Const;

public class CommercialManageLogic {

	public static CommercialRatesLst getCommercialRates() throws Exception {

		String xml = "";

		String r1 = "";
		String r2 = "";

		ArrayList<CommercialRateItem> commercialRates = new ArrayList<CommercialRateItem>();

		// ///////////////////////////////////////////////////////

		xml = ManageLogic.sendGet(Const.COMMERCIAL_USD_SERVICE);

		r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table\""));
		r2 = r1.substring(0, r1.indexOf("</div>"));

		ArrayList<CommercialRateItem> usdRates = new CommercialSAXParser(r2, "USD").getRates();

		xml = ManageLogic.sendGet(Const.COMMERCIAL_EUR_SERVICE);

		r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table\""));
		r2 = r1.substring(0, r1.indexOf("</div>"));

		ArrayList<CommercialRateItem> eurRates = new CommercialSAXParser(r2, "EUR").getRates();

		xml = ManageLogic.sendGet(Const.COMMERCIAL_RUB_SERVICE);

		r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table\""));
		r2 = r1.substring(0, r1.indexOf("</div>"));

		ArrayList<CommercialRateItem> rubRates = new CommercialSAXParser(r2, "RUB").getRates();

		// ////////////////////////////////////////////////////////

		commercialRates.addAll(usdRates);
		commercialRates.addAll(eurRates);
		commercialRates.addAll(rubRates);
		
		CommercialRatesLst response = new CommercialRatesLst();
		response.commercialRatesList = commercialRates;

		return response;
	}
}