package com.dn.ivan.rates.logic;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.dn.ivan.rates.CommercialRateItem;

public class CommercialSAXParser {
	
	String currencyCode = "";

	private InputSource source;
	private SAXParser parser;
	private DefaultHandler documentHandler;
	
	private ArrayList<CommercialRateItem> rates = new ArrayList<CommercialRateItem>();
			
	public CommercialSAXParser(String xml, String currencyCode_) throws Exception {
		
		currencyCode = currencyCode_;
		
		source = new InputSource(new StringReader(xml.trim()));
		parser = SAXParserFactory.newInstance().newSAXParser();
		documentHandler = new XMLParser();
		parser.parse(source, documentHandler);
	}
	
	public ArrayList<CommercialRateItem> getRates() {
		
		ArrayList<CommercialRateItem> out = new ArrayList<CommercialRateItem>();
		
		for (int i = 0; i < rates.size(); i++) {
			
			CommercialRateItem item = rates.get(i);
			
			if (!"".equalsIgnoreCase(item.bankName) && !"".equalsIgnoreCase(item.sourceUrl)) {
				out.add(item);
			}
		}
		
		return out;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	class XMLParser extends DefaultHandler {
		
		String thisElement = "";
		
		private int td_count = 0;
		private CommercialRateItem rate = null;
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			
			thisElement = qName;
			
			if ("tr".equalsIgnoreCase(qName)) {				
				rate = new CommercialRateItem();
				rate.codeAlpha = currencyCode;
			}
			if ("td".equalsIgnoreCase(qName)) {				
				td_count ++;
			}
			if ("a".equalsIgnoreCase(qName)) {
				
				if (td_count == 1 && attributes.getValue("href") != null && !"".equalsIgnoreCase(attributes.getValue("href"))) {
					rate.sourceUrl = attributes.getValue("href");
				}
			}
			if ("i".equalsIgnoreCase(qName)) {
				
				if (td_count == 2 && attributes.getValue("class") != null && !"".equalsIgnoreCase(attributes.getValue("class"))) {
					rate.rateBuyDelta = "decrease".equalsIgnoreCase(attributes.getValue("class"))? "-0.1": "0.1";
				}
				if (td_count == 3 && attributes.getValue("class") != null && !"".equalsIgnoreCase(attributes.getValue("class"))) {
					rate.rateSaleDelta = "decrease".equalsIgnoreCase(attributes.getValue("class"))? "-0.1": "0.1";
				}
			}
		}
		
		public void characters(char[] ch, int start, int length) {
			
			if (td_count == 1 && thisElement.equals("a")) {
				rate.bankName = rate.bankName + new String(ch, start, length);
			}
			if (td_count == 2 && thisElement.equals("td")) {
				rate.rateBuy = rate.rateBuy + new String(ch, start, length);
			}
			if (td_count == 3 && thisElement.equals("td")) {
				rate.rateSale = rate.rateSale + new String(ch, start, length);
			}			
		}
		
		public void endElement(String namespaceURI, String localName, String qName) {			
			
			thisElement = "";
			
			if ("tr".equalsIgnoreCase(qName)) {
				
				td_count = 0;
				rates.add(rate);
				rate = null;
			}
		}
	}
}