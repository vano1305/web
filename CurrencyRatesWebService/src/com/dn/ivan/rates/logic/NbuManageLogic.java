package com.dn.ivan.rates.logic;

import com.dn.ivan.rates.Const;
import com.dn.ivan.rates.NbuRatesLst;

public class NbuManageLogic {

	public static NbuRatesLst getNbuRates() throws Exception {

		String xml = ManageLogic.sendGet(Const.NBU_SERVICE);

		String r1 = xml.toString().substring(xml.toString().indexOf("<table class=\"local_table nbu_rate\">"));
		String r2 = r1.substring(0, r1.indexOf("</table>") + 8);
		
		NbuRatesLst response = new NbuRatesLst();
		response.nbuRatesList = new NbuSAXParser(r2).getRates();

		return response;
	}
}