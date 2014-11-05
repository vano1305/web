package com.dn.ivan.rates.logic;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.dn.ivan.rates.NbuRateItem;

public class NbuSAXParser {
	
	private InputSource source;
	private SAXParser parser;
	private DefaultHandler documentHandler;
	
	private ArrayList<NbuRateItem> nbuRates = new ArrayList<NbuRateItem>();
			
	public NbuSAXParser(String xml) throws Exception {
		
		source = new InputSource(new StringReader(xml.trim()));
		parser = SAXParserFactory.newInstance().newSAXParser();
		documentHandler = new XMLParser();
		parser.parse(source, documentHandler);
	}
	
	public ArrayList<NbuRateItem> getRates() {		
		
		ArrayList<NbuRateItem> out = new ArrayList<NbuRateItem>();
		
		for (int i = 0; i < nbuRates.size(); i++) {
			
			NbuRateItem item = nbuRates.get(i);
			
			if (!"".equalsIgnoreCase(item.code) && !"".equalsIgnoreCase(item.char3)) {
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
		private NbuRateItem rate = null;
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			
			thisElement = qName;
			
			if ("tr".equalsIgnoreCase(qName)) {				
				rate = new NbuRateItem();
			}
			if ("td".equalsIgnoreCase(qName)) {				
				td_count ++;
			}
		}
		
		public void characters(char[] ch, int start, int length) {
			
			if (td_count == 1 && thisElement.equals("td")) {
				rate.code = rate.code + new String(ch, start, length);
			}
			if (td_count == 2 && thisElement.equals("td")) {
				rate.char3 = rate.char3 + new String(ch, start, length);
			}
			if (td_count == 3 && thisElement.equals("td")) {
				rate.name = rate.name + new String(ch, start, length);
			}
			if (td_count == 4 && thisElement.equals("td")) {
				rate.rate = rate.rate + new String(ch, start, length);
			}
			if (td_count == 5 && thisElement.equals("td")) {
				rate.change = rate.change + new String(ch, start, length);
			}
		}
		
		public void endElement(String namespaceURI, String localName, String qName) {			
			
			thisElement = "";
			
			if ("tr".equalsIgnoreCase(qName)) {
				
				td_count = 0;
				nbuRates.add(rate);
				rate = null;
			}
		}
	}
}