package com.dn.ivan.rates;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement(name="NbuHistoryItem")
public class NbuHistoryItem implements Serializable {
	
	public String currencyCode = "";
	public String date1 = "";
	public String date2 = "";
	public String history = "";
}
