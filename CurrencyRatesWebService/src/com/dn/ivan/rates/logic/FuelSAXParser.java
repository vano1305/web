package com.dn.ivan.rates.logic;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.dn.ivan.rates.FuelRateItem;

public class FuelSAXParser {
	
	private InputSource source;
	private SAXParser parser;
	private DefaultHandler documentHandler;
	
	private ArrayList<FuelRateItem> rates = new ArrayList<FuelRateItem>();
			
	public FuelSAXParser(String xml) throws Exception {
		
		source = new InputSource(new StringReader(xml.trim()));
		parser = SAXParserFactory.newInstance().newSAXParser();
		documentHandler = new XMLParser();
		parser.parse(source, documentHandler);
	}
	
	public ArrayList<FuelRateItem> getRates() {		
		
		ArrayList<FuelRateItem> out = new ArrayList<FuelRateItem>();
		
		for (int i = 0; i < rates.size(); i++) {
			
			FuelRateItem item = rates.get(i);
			item.code = item.code.replaceAll("/azs/", "azs_").replaceAll("/", "");
			
			if (!"".equalsIgnoreCase(item.code) && !"".equalsIgnoreCase(item.name)) {
				out.add(item);
			}
		}
		
		return out;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	class XMLParser extends DefaultHandler {
		
		private String thisElement = "";
		
		private int tr_position = 0;
		private int th_position = 0;
		private int td_position = 0;
		private FuelRateItem rate = null;
		
		int a_80_position = -1;
		int a_92_position = -1;
		int a_95_position = -1;
		int dt_position = -1;
		
		String fuelCode = "";
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			
			thisElement = qName;
			
			if ("tr".equalsIgnoreCase(qName)) {				
				rate = new FuelRateItem();
				tr_position ++;
			}
			if ("td".equalsIgnoreCase(qName)) {				
				td_position ++;
			}
			if ("th".equalsIgnoreCase(qName)) {
				th_position ++;
			}
			if ("a".equalsIgnoreCase(qName)) {
				
				if (td_position == 1 && attributes.getValue("href") != null && !"".equalsIgnoreCase(attributes.getValue("href"))) {
					rate.code = attributes.getValue("href");
				}
			}
		}
		
		public void characters(char[] ch, int start, int length) {
			
			if (td_position == 1 && thisElement.equals("a")) {
				rate.name = rate.name + new String(ch, start, length);
			}			
			if (td_position == a_80_position && thisElement.equals("td")) {
				rate.a_80 = rate.a_80 + new String(ch, start, length);
			}
			if (td_position == a_92_position && thisElement.equals("td")) {
				rate.a_92 = rate.a_92 + new String(ch, start, length);
			}
			if (td_position == a_95_position && thisElement.equals("td")) {
				rate.a_95 = rate.a_95 + new String(ch, start, length);
			}
			if (td_position == dt_position && thisElement.equals("td")) {
				rate.dt = rate.dt + new String(ch, start, length);
			}
			if (thisElement.equals("a") && tr_position == 1) {
				fuelCode = fuelCode + new String(ch, start, length);
			}
		}
		
		public void endElement(String namespaceURI, String localName, String qName) {			
			
			thisElement = "";
			
			if ("tr".equalsIgnoreCase(qName)) {
				
				th_position = 0;
				td_position = 0;
				rates.add(rate);
				rate = null;
			}
			if ("a".equalsIgnoreCase(qName) && tr_position == 1) {
				
				if ("A-80".equalsIgnoreCase(fuelCode) || "А-80".equalsIgnoreCase(fuelCode) || "a_80".equalsIgnoreCase(fuelCode) || "а_80".equalsIgnoreCase(fuelCode)) {
					a_80_position = th_position;
				}
				if ("A-92".equalsIgnoreCase(fuelCode) || "А-92".equalsIgnoreCase(fuelCode) || "a_92".equalsIgnoreCase(fuelCode) || "а_92".equalsIgnoreCase(fuelCode)) {
					a_92_position = th_position;
				}
				if ("A-95".equalsIgnoreCase(fuelCode) || "А-95".equalsIgnoreCase(fuelCode) || "a_95".equalsIgnoreCase(fuelCode) || "а_95".equalsIgnoreCase(fuelCode)) {
					a_95_position = th_position;
				}
				if ("DT".equalsIgnoreCase(fuelCode) || "ДТ".equalsIgnoreCase(fuelCode)) {
					dt_position = th_position;
				}				
				
				fuelCode = "";
			}
		}
	}
}