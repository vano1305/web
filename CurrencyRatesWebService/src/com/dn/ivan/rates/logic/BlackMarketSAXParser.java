package com.dn.ivan.rates.logic;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.dn.ivan.rates.BlackMarketRateItem;

public class BlackMarketSAXParser {
	
	private String cityCode = "";
	private String currencyCode = "";
	private String opCode = "";
	
	private InputSource source;
	private SAXParser parser;
	private DefaultHandler documentHandler;
	
	private ArrayList<BlackMarketRateItem> rates = new ArrayList<BlackMarketRateItem>();
			
	public BlackMarketSAXParser(String xml, String cityCode, String currencyCode, String opCode) throws Exception {
		
		this.cityCode = cityCode;
		this.currencyCode = currencyCode;
		this.opCode = opCode;
		
		source = new InputSource(new StringReader(xml.trim().replaceAll("&", "&amp;").replaceAll("<span>", "").replaceAll("</span>", "").replaceAll("<span", "")));
		parser = SAXParserFactory.newInstance().newSAXParser();
		documentHandler = new XMLParser();
		parser.parse(source, documentHandler);
	}
	
	public ArrayList<BlackMarketRateItem> getRates() {		
		
		ArrayList<BlackMarketRateItem> out = new ArrayList<BlackMarketRateItem>();
		
		double averageRate = 0;
		int count = 0;
		for (int i = 0; i < rates.size(); i++) {
			if ((("USD".equalsIgnoreCase(currencyCode) || "EUR".equalsIgnoreCase(currencyCode)) && Double.valueOf(rates.get(i).rate) > 10 && Double.valueOf(rates.get(i).rate) < 100) 
					|| ("RUB".equalsIgnoreCase(currencyCode) && Double.valueOf(rates.get(i).rate) > 0.1 && Double.valueOf(rates.get(i).rate) < 1)) {
				
				averageRate = new BigDecimal(averageRate).add(new BigDecimal(rates.get(i).rate)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				count++;
			}
		}
		
		BlackMarketRateItem resultItem = new BlackMarketRateItem();
		
		resultItem.opCode = opCode;
		resultItem.cityCode = cityCode;
		resultItem.currencyCode = currencyCode;
		if (count > 0 && averageRate > 0) {
			resultItem.rate = new BigDecimal(averageRate).divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP).toPlainString();
		}
		else {
			resultItem.rate = "0";
		}
		
		out.add(resultItem);
		
		return out;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	class XMLParser extends DefaultHandler {
		
		String thisElement = "";
		
		private int tr_count = 0;
		private int td_count = 0;
		
		private BlackMarketRateItem rate = null;
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			
			thisElement = qName;
			
			if ("tr".equalsIgnoreCase(qName)) {
				
				rate = new BlackMarketRateItem();
				
				rate.currencyCode = currencyCode;
				rate.opCode = opCode;
				rate.cityCode = cityCode;
				
				tr_count ++;
			}
			if ("td".equalsIgnoreCase(qName)) {				
				td_count ++;
			}
		}
		
		public void characters(char[] ch, int start, int length) {
			
			if (td_count == 2 && thisElement.equals("td")) {
				rate.rate = rate.rate + new String(ch, start, length);
			}			
		}
		
		public void endElement(String namespaceURI, String localName, String qName) {			
			
			thisElement = "";
			
			if ("tr".equalsIgnoreCase(qName)) {
				
				td_count = 0;
				if (tr_count > 1) {
					rates.add(rate);
				}
				rate = null;
			}
		}
	}
}