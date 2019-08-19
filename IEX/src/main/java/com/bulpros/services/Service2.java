package com.bulpros.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.bulpros.exceptions.DateParsingException;
import com.bulpros.exceptions.RangeException;
import com.bulpros.util.Const;
import com.bulpros.util.Db;
import com.bulpros.util.TimeLine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

public class Service2 {
	private static final Logger LOGGER = Logger.getLogger(Service2.class);

	/**
	 * This method is used to get History Prices from db for given company
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
			HashMap<String, Object> check = new HashMap<String, Object>();
			ArrayNode result = JsonNodeFactory.instance.arrayNode();
			Iterator<JsonNode> iterator = Db.getIterator();
			while(iterator.hasNext()) {
				JsonNode node = iterator.next();
				if(node.get(Const.COMPANY_NAME).asText().toLowerCase().contains(company.toLowerCase())) {
					if(node.has(Const.TIME_FROM)) {
						// range array
						ArrayNode array = (ArrayNode) node.get(Const.PRICES);
						for(JsonNode jn : array) {
							TimeLine instDate = new TimeLine();
							instDate.parseFromSource(jn.get(Const.DATE).asText());
							
							if(!check.containsKey(instDate.toString()) && tl_begin.before(instDate) && instDate.before(tl_end)) {
								result.add(jn);
								check.put(instDate.toString(), new Object());
							}
						}
					}
				}
			}
			res.type("application/json");
			return result.toString();
		} else {
			String msg = "Range is incorect\", \"tFrom\":\""+tl_begin.toString()+"\", \"tTo\":\""+tl_end.toString()+"";
			LOGGER.error(msg);
			throw new RangeException("{\"errorMessage\":\""+msg+"\"}");
		}
	}
	
	/**
	 * This method is used to get History Prices from db for given company,
	 * which is set as path parameter, for given date.
	 * 
	 * @param req api Request info.
	 * @param res api Respose info
	 * @return String <i>successful operation</i> if everything goes well, <i>failed operation</i> otherwise.
	 * @throws DateParsingException 
	 * @throws DateParsingThrowable 
	 */
	public String getCompanyHistoryPricesDate(Request req, Response res) throws DateParsingException {
		LOGGER.info("getCompanyHistoryPricesDate: " + req.pathInfo());
		Map<String, String> params = req.params();
		String company = params.get(Const.PATH_COMPANY);
		String date = params.get(Const.PATH_DATE);
		
		TimeLine tl_begin = new TimeLine();
		boolean done = tl_begin.parse(date);
		
		if(!done) {
			String msg = "Parsing date "+date+" failed.";
			LOGGER.error(msg);
			throw new DateParsingException("{\"message\":\""+msg+"\"}");
		}
		
		Iterator<JsonNode> iterator = Db.getIterator();
		while(iterator.hasNext()) {
			JsonNode node = iterator.next();
			if(node.get(Const.COMPANY_NAME).asText().toLowerCase().contains(company.toLowerCase())) {
				if(!node.has(Const.TIME_FROM)){
					// date instance
					JsonNode inst = node.get(Const.PRICES);
					inst = ((ArrayNode)inst).get(0);
					TimeLine instDate = new TimeLine();
					instDate.parseFromSource(inst.get(Const.DATE).asText());
					
					if(tl_begin.beforeEqual(instDate) && instDate.beforeEqual(tl_begin)) {
						res.type("application/json");
						return inst.toString();
					}
				}
			}
		}
		
		res.type("application/json");
		return "{\"message\": \"failed operation or no data available\"}";
	}
	
	/**
	 * This method is used to get History Prices from db for companies,
	 * which are provided as query parameter. All prices between range.
	 * 
	 * @param req api Request info.
	 * @param res api Response info.
	 * @return String <i>successful operation</i> if everything goes well, <i>failed operation</i> otherwise.
	 * @throws DateParsingException occurs when parsing dates passed through API call.
	 * @throws RangeException occurs when there is something wrong with range. 
	 */
	public String getCompaniesHistoryPricesRange(Request req, Response res) throws DateParsingException, RangeException {
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
			ArrayNode result = JsonNodeFactory.instance.arrayNode();
			for(String company : companies) {
				HashMap<String, Object> companyElement = null;
				ArrayList<Object> listPrices = null;
				HashMap<String, Object> check = new HashMap<String, Object>();
				Iterator<JsonNode> iterator = Db.getIterator();
				while(iterator.hasNext()) {
					JsonNode node = iterator.next();
					if(node.get(Const.COMPANY_NAME).asText().toLowerCase().contains(company.toLowerCase())) {
						if(companyElement == null) {
							companyElement = new HashMap<String, Object>();
							listPrices = new ArrayList<Object>();
							companyElement.put(Const.PRICES, listPrices);
							companyElement.put(Const.COMPANY_NAME, node.get(Const.COMPANY_NAME).asText());
						}
						if(node.has(Const.TIME_FROM)) {
							// range
							ArrayNode arrayNode = (ArrayNode) node.get(Const.PRICES);
							for(JsonNode jn : arrayNode) {
								TimeLine instDate = new TimeLine();
								instDate.parseFromSource(jn.get(Const.DATE).asText());
								if(!check.containsKey(instDate.toString()) && tl_begin.before(instDate) && instDate.before(tl_end)) {
									listPrices.add(jn);
									check.put(instDate.toString(), new Object());
								}
							}
						}
					}
				}
				if(companyElement != null) {
					result.add((new ObjectMapper()).convertValue(companyElement, JsonNode.class));
				}
			}
			res.type("application/json");
			return result.toString();
		} else {
			String msg = "Range is incorect tFrom: "+tl_begin.toString()+", tTo:"+tl_end.toString();
			LOGGER.error(msg);
			throw new RangeException("{\"errorMessage\":\""+msg+"\"}");
		}
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
		
		String[] companies = null;
		Set<Entry<String, String[]>> map = qm.toMap().entrySet();
		Iterator<Entry<String, String[]>> iter = map.iterator();
		while(iter.hasNext()) {
			Entry<String, String[]> entry = iter.next();
			if(entry.getKey().equals(Const.COMPANY_QUERY)) {
				companies = entry.getValue();
			}
		}
			
		ArrayNode result = JsonNodeFactory.instance.arrayNode();
		for(String company : companies) {
			HashMap<String, Object> companyElement = null;
			ArrayList<Object> listPrices = null;
			HashMap<String, Object> check = new HashMap<String, Object>();
			Iterator<JsonNode> iterator = Db.getIterator();
			while(iterator.hasNext()) {
				JsonNode node = iterator.next();
				if(node.get(Const.COMPANY_NAME).asText().toLowerCase().contains(company.toLowerCase())) {
					if(companyElement == null) {
						companyElement = new HashMap<String, Object>();
						listPrices = new ArrayList<Object>();
						companyElement.put(Const.PRICES, listPrices);
						companyElement.put(Const.COMPANY_NAME, node.get(Const.COMPANY_NAME).asText());
					}
					if(!node.has(Const.TIME_FROM)) {
						// date
						JsonNode dateNode = node.get(Const.PRICES);
						dateNode = ((ArrayNode)dateNode).get(0);
						TimeLine instDate = new TimeLine();
						instDate.parseFromSource(dateNode.get(Const.DATE).asText());
						if(tl_begin.beforeEqual(instDate) && instDate.beforeEqual(tl_begin)) {
							listPrices.add(dateNode);
							break;
						}
					}
				}
			}
			if(companyElement != null) {
				result.add((new ObjectMapper()).convertValue(companyElement, JsonNode.class));
			}
		}
		
		res.type("application/json");
		return result.toString();
	}
}
