package com.dn.ivan.rates;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement(name="chapter")
public class FuelRatesLst  implements Serializable {

	@XmlElement(name="item")
	public ArrayList <FuelRateItem> fuelRatesList = new ArrayList<FuelRateItem>();
}
