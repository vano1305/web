package com.dn.ivan.rates.logic;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.dn.ivan.rates.FuelRateItem;

public class FuelSAXParserMinfin {
	
	private InputSource source;
	private SAXParser parser;
	private DefaultHandler documentHandler;
	
	private ArrayList<FuelRateItem> rates = new ArrayList<FuelRateItem>();
			
	public FuelSAXParserMinfin(String xml) throws Exception {
		
		xml = xml.replaceAll("colspan=8", "colspan='8'").replaceAll("border=0 cellspacing=0 cellpadding=2", "border='0' cellspacing='0' cellpadding='2'").replaceAll("&nbsp;", "").replaceAll("<br>", "");
		
		source = new InputSource(new StringReader(xml.trim()));
		parser = SAXParserFactory.newInstance().newSAXParser();
		documentHandler = new XMLParser();
		parser.parse(source, documentHandler);
	}
	
	public ArrayList<FuelRateItem> getRates() {		
		
		HashMap<String, String> regions = new HashMap<>();
		
		regions.put("АРКрым", "4");
		regions.put("Винницкаяобл.", "5");
		regions.put("Волынскаяобл.", "28");
		regions.put("Днепропетровскаяобл.", "6");
		regions.put("Донецкаяобл.", "7");
		regions.put("Житомирскаяобл.", "8");
		regions.put("Закарпатскаяобл.", "9");
		regions.put("Запорожскаяобл.", "10");
		regions.put("Ивано-Франковскаяобл.", "11");
		regions.put("Киев", "12");
		regions.put("Киевскаяобл.", "35");
		regions.put("Кировоградскаяобл.", "13");
		regions.put("Луганскаяобл.", "14");
		regions.put("Львовскаяобл.", "15");
		regions.put("Николаевскаяобл.", "16");
		regions.put("Одесскаяобл.", "17");
		regions.put("Полтавскаяобл.", "18");
		regions.put("Ровенскаяобл.", "19");
		regions.put("Сумскаяобл.", "20");
		regions.put("Тернопольскаяобл.", "21");
		regions.put("Харьковскаяобл.", "22");
		regions.put("Херсонскаяобл.", "23");
		regions.put("Хмельницкаяобл.", "24");
		regions.put("Черкасскаяобл.", "25");
		regions.put("Черниговскаяобл.", "26");
		regions.put("Черновицкаяобл.", "27");
		
		// //////////////////////////////////////////////////
		
		HashMap<String, String> fuel_stations = new HashMap<>();
		
		fuel_stations.put("Лукойл", "azs_10");
		fuel_stations.put("WOG", "azs_11");
		fuel_stations.put("ОККО", "azs_12");
		fuel_stations.put("Джерело/Wells", "azs_13");
		fuel_stations.put("НефтекОйл", "azs_16");
		fuel_stations.put("Параллель", "azs_18");
		fuel_stations.put("Маркет", "azs_21");
		fuel_stations.put("KLO", "azs_23");
		
		fuel_stations.put("БРСМ-Нафта", "azs_25");
		fuel_stations.put("BelOil", "azs_25");
		fuel_stations.put("Orange", "azs_25");
		
		fuel_stations.put("РУР", "azs_26");
		fuel_stations.put("ТНК,Восточные ресурсы", "azs_27");
		
		fuel_stations.put("Укр-Петроль", "azs_32");
		fuel_stations.put("Укр-Петроль/Караван", "azs_32");
		fuel_stations.put("Укр-Петроль/Еней", "azs_32");
		
		fuel_stations.put("Татнефть", "azs_33");
		
		fuel_stations.put("Нафтогаз/ТНК", "azs_34");
		fuel_stations.put("Надежда", "azs_34");
		
		fuel_stations.put("ОЛАС", "azs_36");
		fuel_stations.put("ГНП", "azs_37");
		fuel_stations.put("Автотехсервис", "azs_38");
		fuel_stations.put("ТНК", "azs_4");
		
		fuel_stations.put("АТАН", "azs_5");
		fuel_stations.put("ТНК/АТАН", "azs_5");		
		
		fuel_stations.put("Современник", "azs_6");
		fuel_stations.put("Авиас/Сентоза", "azs_7");
		fuel_stations.put("Shell", "azs_8");
		fuel_stations.put("ANP", "azs_9");
		
		// //////////////////////////////////////////////////
		
		ArrayList<FuelRateItem> out = new ArrayList<FuelRateItem>();
		
		for (int i = 0; i < rates.size(); i++) {
			
			FuelRateItem item = rates.get(i);
			if (!"".equalsIgnoreCase(item.regionCode) && !"".equalsIgnoreCase(item.name) && !"".equalsIgnoreCase(item.spg)) {
				
				item.regionCode = regions.get(item.regionCode);
				item.code = fuel_stations.get(item.name);
				item.spg = item.spg.replaceAll(",", ".");
				
				if (item.code != null && !"".equalsIgnoreCase(item.code)) {
					out.add(item);
				}
			}
		}
		
		return out;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	class XMLParser extends DefaultHandler {
		
		private FuelRateItem rate = null;
		
		private String thisElement = "";
		
		private String region = "";
		private boolean isNewRegion = false;
		
		private int td_position = 0;
				
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			
			thisElement = qName;
			
			if ("tr".equalsIgnoreCase(qName)) {
				rate = new FuelRateItem();
			}
			
			if ("th".equalsIgnoreCase(qName) && "8".equalsIgnoreCase(attributes.getValue("colspan"))) {
				
				region = "";
				isNewRegion = true;
			}
			
			if ("td".equalsIgnoreCase(qName)) {
				td_position ++;
			}
		}
		
		public void characters(char[] ch, int start, int length) {
			
			if (isNewRegion && thisElement.equals("th")) {
				region = region + new String(ch, start, length);
			}
			
			if (td_position == 1 && thisElement.equals("td")) {
				rate.name = rate.name + new String(ch, start, length);
			}
			/*if (td_position == 3 && thisElement.equals("td")) {
				rate.a_80 = rate.a_80 + new String(ch, start, length);
			}
			if (td_position == 4 && thisElement.equals("td")) {
				rate.a_92 = rate.a_92 + new String(ch, start, length);
			}
			if (td_position == 5 && thisElement.equals("td")) {
				rate.a_95 = rate.a_95 + new String(ch, start, length);
			}
			if (td_position == 6 && thisElement.equals("td")) {
				rate.a_95_plus = rate.a_95_plus + new String(ch, start, length);
			}
			if (td_position == 7 && thisElement.equals("td")) {
				rate.dt = rate.dt + new String(ch, start, length);
			}*/
			if (td_position == 8 && thisElement.equals("td")) {
				rate.spg = rate.spg + new String(ch, start, length);
			}
		}
		
		public void endElement(String namespaceURI, String localName, String qName) {
			
			thisElement = "";
			
			if ("tr".equalsIgnoreCase(qName)) {
				
				rate.regionCode = region;
				td_position = 0;
				rates.add(rate);
				rate = null;
			}
			
			if ("th".equalsIgnoreCase(qName) && isNewRegion) {				
				isNewRegion = false;
			}
		}
	}
}