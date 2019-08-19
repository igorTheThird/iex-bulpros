package com.bulpros.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import java.util.Set;

import com.bulpros.exceptions.DateParsingException;
import com.bulpros.exceptions.RangeException;
import com.bulpros.exceptions.ZeroRangeException;
import com.bulpros.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

public class Service1 {
	private static final Logger LOGGER = Logger.getLogger(Service1.class);
	private static final String base = "https://sandbox.iexapis.com/stable/";
	private static final String pub_key = "Tpk_560815cecaa442ffb22571e6819bc863";

	/**
	 * This method is used to get History Prices for given company
	 * which is set as a path parameter. Prices between the range.
	 * 
	 * @param req api Request information.
	 * @param res api Response information.
	 * @return String <i>successful operation</i> if everything goes well, <i>failed operation</i> otherwise.
	 * @throws DateParsingException occurs when parsing dates passed through API call.
	 * @throws RangeException occurs when there is something wrong with range.
	 */
	public String getCompanyHistoryPricesRange(Request req, Response res) throws DateParsingException, RangeException {
		LOGGER.info("getCompanyHistoryPricesRange: " + req.pathInfo());
		Map<String, String> params = req.params();
		String company = params.get(Const.PATH_COMPANY);
		String timebegin = params.get(Const.PATH_TIMEBEGIN);
		String timeend = params.get(Const.PATH_TIMEEND);

		TimeLine tl_begin = new TimeLine();
		boolean done = tl_begin.parse(timebegin);
		
		if(!done) {
			String msg = "Parsing date \""+timebegin+"\" failed.";
			LOGGER.error(msg);
			throw new DateParsingException("{\"errorMessage\":\""+msg+"\"}");
		}

		TimeLine tl_end = new TimeLine();
		done = tl_end.parse(timeend);
		
		if(!done) {
			String msg = "Parsing date \""+timeend+"\" failed.";
			LOGGER.error(msg);
			throw new DateParsingException("{\"errorMessage\":\""+msg+"\"}");
		}

		if (tl_begin.before(tl_end)) {
			LocalDate date = LocalDate.now();
			int b_year = date.getYear() - tl_begin.year;
			int b_month = date.getMonthValue() - tl_begin.month;
			int b_day = date.getDayOfMonth() - tl_begin.day;

			String r = "";
			
			if (b_year == 0 && b_month == 0 && b_day == 0) {
				String msg = "\"Range is zero\", \"tFrom\":\""+tl_begin.toString()+"\", \"today\":\""+date.getYear()+"-"+date.getMonthValue()+"-"+date.getDayOfMonth()+"\"";
				LOGGER.error(msg);
				throw new ZeroRangeException("{\"errorMessage\":"+msg+"}");
			}

			r = calculateRange(b_year, b_month, b_day);

			String url = "chart/" + r;
			String completeUrl = base + "stock/" + company + "/" + url;
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(Const.COMPANY_NAME, getCompanyName(company));
			map.put(Const.COMPANY_LOGO, getCompanyLogo(company));
			map.put(Const.TIME_FROM, timebegin);
			map.put(Const.TIME_TO, timeend);
			try {
				map.put(Const.PRICES, checkRange((doUnirestCall(completeUrl)), tl_begin, tl_end));
			} catch (IOException e) {
				String msg = "Exception reading body tree from " + completeUrl;
				LOGGER.error(msg, e);
				map.put(Const.PRICES, null);
			}
			
			Db.addNode((new ObjectMapper()).valueToTree(map));
		} else {
			String msg = "Range is incorect\", \"tFrom\":\""+tl_begin.toString()+"\", \"tTo\":\""+tl_end.toString()+"";
			LOGGER.error(msg);
			throw new RangeException("{\"errorMessage\":\""+msg+"\"}");
		}

		res.type("application/json");
		return "{\"message\":\"successful operation\"}"; 
	}
	
	/**
	 * This method is used to get History Prices for given company,
	 * which is set as path parameter, for given date.
	 * 
	 * @param req api Request info.
	 * @param res api Respose info
	 * @return String <i>successful operation</i> if everything goes well, <i>failed operation</i> otherwise.
	 * @throws DateParsingException 
	 * @throws DateParsingThrowable 
	 */
	public String getCompanyHistoryPricesDate(Request req, Response res) throws DateParsingException  {
		LOGGER.info("getCompanyHistoryPricesDate: " + req.pathInfo());
		Map<String, String> params = req.params();
		QueryParamsMap qm = req.queryMap();

		String company = params.get(Const.PATH_COMPANY);
		String date = params.get(Const.PATH_DATE);
		
		TimeLine tl_begin = new TimeLine();
		boolean done = tl_begin.parse(date);
		
		if(!done) {
			String msg = "Parsing date "+date+" failed.";
			LOGGER.error(msg);
			throw new DateParsingException("{\"message\":\""+msg+"\"}");
		}
		
		String url = "chart/date/" + date;
		
		Set<Entry<String, String[]>> map = qm.toMap().entrySet();
		Iterator<Entry<String, String[]>> iter = map.iterator();
		boolean first = true;
		while(iter.hasNext()) {
			Entry<String, String[]> entry = iter.next();
			if(first) {
				url += "?" + entry.getKey() + "=";
				first = false;
			} else {
				url += "&" + entry.getKey() + "=";
			}
			String[] array = entry.getValue();
			for(int i=0; i<array.length-1; i++) {
				url += array[i]+ ",";
			}
			url += array[array.length-1];
		}
			
		String completeUrl = base + "stock/" + company + "/"+ url;
		
		HashMap<String, Object> hmap = new HashMap<String, Object>();
		hmap.put(Const.COMPANY_NAME, getCompanyName(company));
		hmap.put(Const.COMPANY_LOGO, getCompanyLogo(company));
		hmap.put(Const.DATE, date);
		try {
			hmap.put(Const.PRICES, doUnirestCall(completeUrl));
		} catch (IOException e) {
			String msg = "Exception reading body tree from " + completeUrl;
			LOGGER.error(msg, e);
			hmap.put(Const.PRICES, null);
		}
		Db.addNode((new ObjectMapper()).valueToTree(hmap));
		
		res.type("application/json");
		return "{\"message\": \"successful operation\"}";
	}

	/**
	 * This method is used to get History Prices for companies,
	 * which are provided as query parameter. All prices between range.
	 * 
	 * @param req api Request info.
	 * @param res api Response info.
	 * @return String <i>successful operation</i> if everything goes well, <i>failed operation</i> otherwise.
	 * @throws DateParsingException occurs when parsing dates passed through API call.
	 * @throws RangeException occurs when there is something wrong with range. 
	 */
	public String getCompaniesHistoryPricesRange(Request req, Response res) throws DateParsingException, RangeException  {
		LOGGER.info("getCompaniesHistoryPricesRange: " + req.pathInfo());
		Map<String, String> params = req.params();
		QueryParamsMap qm = req.queryMap();
		
		String timebegin = params.get(Const.PATH_TIMEBEGIN);
		String timeend = params.get(Const.PATH_TIMEEND);

		TimeLine tl_begin = new TimeLine();
		boolean done = tl_begin.parse(timebegin);
		
		if(!done) {
			String msg = "Parsing date "+timebegin+" failed.";
			LOGGER.error(msg);
			throw new DateParsingException("{\"message\":\""+msg+"\"}");
		}

		TimeLine tl_end = new TimeLine();
		done = tl_end.parse(timeend);
		
		if(!done) {
			String msg = "Parsing date "+timeend+" failed.";
			LOGGER.error(msg);
			throw new DateParsingException("{\"message\":\""+msg+"\"}");
		}
		
		String[] companies = null;
		Set<Entry<String, String[]>> qmap = qm.toMap().entrySet();
		Iterator<Entry<String, String[]>> iter = qmap.iterator();
		while(iter.hasNext()) {
			Entry<String, String[]> entry = iter.next();
			if(entry.getKey().equals(Const.COMPANY_QUERY)) {
				companies = entry.getValue();
			}
		}

		if (tl_begin.before(tl_end)) {
			LocalDate date = LocalDate.now();
			int b_year = date.getYear() - tl_begin.year;
			int b_month = date.getMonthValue() - tl_begin.month;
			int b_day = date.getDayOfMonth() - tl_begin.day;

			String r = "";
			
			if (b_year == 0 && b_month == 0 && b_day == 0) {
				String msg = "Range is zero, tFrom:\""+tl_begin.toString()+"\", today:\""+date.getYear()+"-"+date.getMonthValue()+"-"+date.getDayOfMonth()+"\"";
				LOGGER.error(msg);
				throw new ZeroRangeException("{\"errorMessage\":\""+msg+"\"}");
			}

			r = calculateRange(b_year, b_month, b_day);
			
			String url = "chart/" + r;
			
			for(String c : companies) {
				String completeUrl = base + "stock/" + c + "/" + url;
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(Const.COMPANY_NAME, getCompanyName(c));
				map.put(Const.COMPANY_LOGO, getCompanyLogo(c));
				map.put(Const.TIME_FROM, timebegin);
				map.put(Const.TIME_TO, timeend);
				try {
					map.put(Const.PRICES, checkRange((doUnirestCall(completeUrl)), tl_begin, tl_end));
				} catch (IOException e) {
					String msg = "Exception reading body tree from " + completeUrl;
					LOGGER.error(msg, e);
					map.put(Const.PRICES, null);
				}
				
				Db.addNode((new ObjectMapper()).valueToTree(map));
			}
		} else {
			String msg = "Range is incorect tFrom: "+tl_begin.toString()+", tTo:"+tl_end.toString();
			LOGGER.error(msg);
			throw new RangeException("{\"errorMessage\":\""+msg+"\"}");
		}
		res.type("application/json");
		return "{\"message\": \"successful operation\"}";
	}

	/**
	 * This method is used to get History Prices for companies,
	 * which are provided as query parameters, for a given date.
	 * 
	 * @param req api Request info.
	 * @param res api Response info.
	 * @return String <i>successful operation</i> if everything goes well, <i>failed operation</i> otherwise.
	 * @throws DateParsingException 
	 */
	public String getCompaniesHistoryPricesDate(Request req, Response res) throws DateParsingException {
		LOGGER.info("getCompaniesHistoryPricesDate: " + req.pathInfo());
		Map<String, String> params = req.params();
		QueryParamsMap qm = req.queryMap();
		
		String date = params.get(Const.PATH_DATE);
		TimeLine tl_begin = new TimeLine();
		boolean done = tl_begin.parse(date);
		
		if(!done) {
			String msg = "Parsing date "+date+" failed.";
			LOGGER.error(msg);
			throw new DateParsingException("{\"message\":\""+msg+"\"}");
		}
		
		String url = "chart/date/" + date;
		String[] companies = null;
		Set<Entry<String, String[]>> map = qm.toMap().entrySet();
		Iterator<Entry<String, String[]>> iter = map.iterator();
		boolean first = true;
		while(iter.hasNext()) {
			Entry<String, String[]> entry = iter.next();
			if(entry.getKey().equals(Const.COMPANY_QUERY)) {
				companies = entry.getValue();
			} else {
				if(first) {
					url += "?" + entry.getKey() + "=";
					first = false;
				} else {
					url += "&" + entry.getKey() + "=";
				}
				String[] array = entry.getValue();
				for(int i=0; i<array.length-1; i++) {
					url += array[i]+ ",";
				}
				url += array[array.length-1];
			}
		}
			
		for(String c : companies) {
			String completeUrl = base + "stock/" + c + "/"+ url;
			HashMap<String, Object> hmap = new HashMap<String, Object>();
			hmap.put(Const.COMPANY_NAME, getCompanyName(c));
			hmap.put(Const.COMPANY_LOGO, getCompanyLogo(c));
			hmap.put(Const.DATE, date);
			try {
				hmap.put(Const.PRICES, (doUnirestCall(completeUrl)));
			} catch (IOException e) {
				String msg = "Exception reading body tree from " + completeUrl;
				LOGGER.error(msg, e);
				hmap.put(Const.PRICES, null);
			}
			
			Db.addNode((new ObjectMapper()).valueToTree(hmap));
		}
		res.type("application/json");
		return "{\"message\": \"successful operation\"}";
	}
	
	public String calculateRange(int b_year, int b_month, int b_day) {
		String r = null;
		if (b_year > 5) {
			r = "max";
		} else if (b_year <= 5 && b_year > 2) {
			r = "5y";
		} else if (b_year <= 2 && b_year >= 1) {
			r = "2y";
		} else if (b_year == 0 && b_month > 6) {
			r = "1y";
		} else if (b_year == 0 && b_month <= 6 && b_month >= 3) {
			r = "6m";
		} else if (b_year == 0 && b_month < 3 && b_month >= 1) {
			r = "3m";
		} else if (b_year == 0 && b_month < 1 && b_day > 5) {
			r = "1m";
		} else if (b_year == 0 && b_month == 0 && b_day >= 5) {
			r = "1mm";
		} else if (b_year == 0 && b_month == 0 && b_day > 0) {
			r = "5d";
		} else {
			r = "max";
		}
		return r;
	}

	/**
	 * This method is used to check that date for each instance of History Prices,
	 * is between range.
	 * 
	 * @param node JsonNode containing all prices.
	 * @param tA TimeLine from.
	 * @param tB TimeLine to.
	 * @return JsonNode, without priceses that are not between range.
	 */
	public JsonNode checkRange(JsonNode node, TimeLine tA, TimeLine tB) {
		ArrayNode array = JsonNodeFactory.instance.arrayNode();
		if(node.isArray()) {
			for(JsonNode obj : node) {
				TimeLine tl = new TimeLine();
				tl.parseFromSource(obj.get("date").asText());
				if(tA.beforeEqual(tl) && tl.beforeEqual(tB)) {
					array.add(obj);
				}
			}
		}
		return array;
	}
	
	/**
	 * This method is used to get company name.
	 * GET /stock/{symbol}/company
	 * 
	 * @param company, id.
	 * @return String, the name of the company.
	 */
	public String getCompanyName(String company) {
		String completeUrl = base + "stock/" + company + "/company";
		try {
			return (doUnirestCall(completeUrl)).get("companyName").asText();
		} catch (IOException e) {
			LOGGER.error("Exception occured in method getCompanyName "+completeUrl, e);
			return null;
		}
	}
	
	/**
	 * This method is used to get company url to the picture logo.
	 * GET /stock/{symbol}/logo
	 * 
	 * @param company, id.
	 * @return String, the name of the company.
	 */
	public String getCompanyLogo(String company) {
		String completeUrl = base + "stock/" + company + "/logo";
		try {
			return (doUnirestCall(completeUrl)).get("url").asText();
		} catch (IOException e) {
			LOGGER.error("Exception occured in method getCompanyLogo "+completeUrl, e);
			return null;
		}
	}
	
	/**
	 * This method is used as abstraction for Unirest calls.
	 * 
	 * @param completeUrl, api endpoint.
	 * @return JsonNode, response from api endpoint.
	 * @throws IOException when there is an error reading body tree.
	 */
	public JsonNode doUnirestCall(String completeUrl) throws IOException {
		HttpResponse<String> response = Unirest.get(completeUrl)
				.header("accept", "application/json")
				.queryString("token", pub_key)
				.asString();

		String body = response.getBody();
		JsonNode jn = null;
		jn = (new ObjectMapper()).readTree(body);
		return jn;
	}
	
	/**
	 * This method is used to get content of resource file. 
	 * In this particular setup it is used to get <i>homepage.html</i>
	 *  
	 * @param req api request.
	 * @param res api response.
	 * @param fileName name of the resource file.
	 * @return String represents content of the resource file.
	 * @throws FileNotFoundException 
	 */
	public String getResourceFile(Request req, Response res) throws FileNotFoundException {
		LOGGER.info("getResourceFile: " + req.pathInfo());
		String fileName = req.params(Const.PATH_PAGE);
		File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
		
		String page = "";
		try (FileReader reader = new FileReader(file); BufferedReader br = new BufferedReader(reader)) {
			String line;
			while ((line = br.readLine()) != null) {
				page += line;
			}
		} catch (IOException e) {
			String msg = "Exception "+e+" occured while reading from resource file "+fileName;
			LOGGER.error(msg);
			return null;
		}
		return page;
	}
}
