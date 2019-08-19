package com.bulpros.main;

import static spark.Spark.*;

import org.apache.log4j.Logger;

import com.bulpros.exceptions.*;
import com.bulpros.services.Service1;
import com.bulpros.services.Service2;
import com.bulpros.util.Const;

public class Main {
	private static final Logger LOGGER = Logger.getLogger(Main.class);
	
    public static void main(String[] args) {
    	LOGGER.info("Starting up...");
    	Service1 service1 = new Service1();
    	Service2 service2 = new Service2();
    	
    	before("/*", (req, res) -> LOGGER.info("Receiving api call"));
    	after((req, res) -> LOGGER.info("Responding to api call"));
    	
    	path("/iex", () -> {
    		path("/company", () -> {
    			get("/"+Const.PATH_COMPANY+"/"+Const.PATH_TIMEBEGIN+"/"+Const.PATH_TIMEEND,
    					(req,res) -> service1.getCompanyHistoryPricesRange(req, res));
    			get("/"+Const.PATH_COMPANY+"/"+Const.PATH_DATE,
    					(req,res) -> service1.getCompanyHistoryPricesDate(req, res));
    		});
    		
    		path("/companies", () -> {
    			get("/"+Const.PATH_TIMEBEGIN+"/"+Const.PATH_TIMEEND,
    					(req,res) -> service1.getCompaniesHistoryPricesRange(req, res));
    			get("/"+Const.PATH_DATE,
    					(req,res) -> service1.getCompaniesHistoryPricesDate(req, res));
    		});
    	});
    	
    	path("/bulpros", () -> {
    		path("/company", () -> {
    			get("/"+Const.PATH_COMPANY+"/"+Const.PATH_TIMEBEGIN+"/"+Const.PATH_TIMEEND,
    					(req,res) -> service2.getCompanyHistoryPricesRange(req, res));
    			get("/"+Const.PATH_COMPANY+"/"+Const.PATH_DATE,
    					(req,res) -> service2.getCompanyHistoryPricesDate(req, res));
    		});
    		
    		path("/companies", () -> {
    			get("/"+Const.PATH_TIMEBEGIN+"/"+Const.PATH_TIMEEND,
    					(req,res) -> service2.getCompaniesHistoryPricesRange(req, res));
    			get("/"+Const.PATH_DATE,
    					(req,res) -> service2.getCompaniesHistoryPricesDate(req, res));
    		});
    	});
    	
    	exception(DateParsingException.class, (exception, request, response) -> {
		    response.type("application/json");
		    response.body(exception.getMessage());
		});
		exception(RangeException.class, (exception, request, response) -> {
			response.type("application/json");
		    response.body(exception.getMessage());
		});
    	notFound((req, res) -> {
		    res.type("application/json");
		    return "{\"message\":\"Error 404, path your looking for does not exists.\"}";
		});
    	internalServerError((req, res) -> {
		    res.type("application/json");
		    return "{\"message\":\"Error 500, internal server error.\"}";
		});
    	
    	LOGGER.info("Finish starting up...");
    }


}