package com.bulpros.services;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import com.bulpros.exceptions.DateParsingException;
import com.bulpros.exceptions.RangeException;
import com.bulpros.util.Const;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import spark.*;

@RunWith(MockitoJUnitRunner.class)
public class Service1Test {
	private JsonNode jn = null;
	
	@Before
	public void init() {
		File file = new File(getClass().getClassLoader().getResource("example.json").getFile());
		
		String content = "";
		try (FileReader reader = new FileReader(file); BufferedReader br = new BufferedReader(reader)) {
			String line;
			while ((line = br.readLine()) != null) {
				content += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			jn = (new ObjectMapper()).readTree(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCompanyHistoryPricesRange_Success() {
		Map<String, String> params = new HashMap<String, String>();
		params.put(Const.PATH_COMPANY, "aapl");
		params.put(Const.PATH_TIMEBEGIN, "20190715");
		params.put(Const.PATH_TIMEEND, "20190814");
		
		Request req = Mockito.mock(Request.class);
		when(req.params()).thenReturn(params);
		when(req.pathInfo()).thenReturn("/iex/company/aapl/20190715/20190814");
		Service1 service = new Service1();
		Service1 serviceSpy = spy(service);
				
		doReturn("Apple Inc.").when(serviceSpy).getCompanyName("aapl");
		doReturn("apple.logo").when(serviceSpy).getCompanyLogo("aapl");
		try {
			doReturn(jn).when(serviceSpy).doUnirestCall("https://sandbox.iexapis.com/stable/stock/aapl/chart/3m");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String resMsg = null;
		try {
			resMsg = serviceSpy.getCompanyHistoryPricesRange(req, Mockito.mock(Response.class));
		} catch (DateParsingException e) {
			e.printStackTrace();
		} catch (RangeException e) {
			e.printStackTrace();
		}
		
		assertEquals(resMsg, "{\"message\":\"successful operation\"}");
	}
	
	@Test
	public void testCompanyHistoryPricesRange_FirstTimeLineException() {
		Map<String, String> params = new HashMap<String, String>();
		params.put(Const.PATH_COMPANY, "aapl");
		params.put(Const.PATH_TIMEBEGIN, "2019071");
		params.put(Const.PATH_TIMEEND, "20190814");
		
		Request req = Mockito.mock(Request.class);
		when(req.params()).thenReturn(params);
		when(req.pathInfo()).thenReturn("/iex/company/aapl/2019071/20190814");
		Service1 service = new Service1();
		Service1 serviceSpy = spy(service);
			
		String resMsg = null;
		String erMsg = null;
		boolean trown = false;
		try {
			 resMsg = serviceSpy.getCompanyHistoryPricesRange(req, Mockito.mock(Response.class));
		} catch (DateParsingException e) {
			trown = true;
			erMsg = e.getMessage();
		} catch (RangeException e) {
			trown = false;
		}
		
		assertTrue(trown);
		assertEquals("{\"errorMessage\":\"Parsing date \"2019071\" failed.\"}", erMsg);
		assertNull(resMsg);
	}
	
	@Test
	public void testCompanyHistoryPricesRange_SecondTimeLineException() {
		Map<String, String> params = new HashMap<String, String>();
		params.put(Const.PATH_COMPANY, "aapl");
		params.put(Const.PATH_TIMEBEGIN, "20190715");
		params.put(Const.PATH_TIMEEND, "2019081");
		
		Request req = Mockito.mock(Request.class);
		when(req.params()).thenReturn(params);
		when(req.pathInfo()).thenReturn("/iex/company/aapl/20190715/2019081");
		Service1 service = new Service1();
		Service1 serviceSpy = spy(service);
			
		String resMsg = null;
		String erMsg = null;
		boolean trown = false;
		try {
			 resMsg = serviceSpy.getCompanyHistoryPricesRange(req, Mockito.mock(Response.class));
		} catch (DateParsingException e) {
			trown = true;
			erMsg = e.getMessage();
		} catch (RangeException e) {
			trown = false;
		}
		
		assertTrue(trown);
		assertEquals("{\"errorMessage\":\"Parsing date \"2019081\" failed.\"}", erMsg);
		assertNull(resMsg);
	}
	
	@Test
	public void testCompanyHistoryPricesRange_ZeroRangeException() {
		LocalDate date1 = LocalDate.now();
		String y1 = Integer.toString(date1.getYear());
		int M = date1.getMonthValue();
		String m1 = (M < 10 ? "0"+Integer.toString(M) : Integer.toString(M));
		String m = Integer.toString(M);
		int D = date1.getDayOfMonth();
		String d1 = (D < 10 ? "0"+Integer.toString(D) : Integer.toString(D));
		String d = Integer.toString(D);
		
		LocalDate date2 = date1.plusDays(30);
		String y2 = Integer.toString(date2.getYear());
		M = date2.getMonthValue();
		String m2 = (M < 10 ? "0"+Integer.toString(M) : Integer.toString(M));
		D = date2.getDayOfMonth();
		String d2 = (D < 10 ? "0"+Integer.toString(D) : Integer.toString(D));
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(Const.PATH_COMPANY, "aapl");
		params.put(Const.PATH_TIMEBEGIN, y1+m1+d1);
		params.put(Const.PATH_TIMEEND, y2+m2+d2);
		
		Request req = Mockito.mock(Request.class);
		when(req.params()).thenReturn(params);
		when(req.pathInfo()).thenReturn("/iex/company/aapl/"+y1+m1+d1+"/"+y2+m2+d2);
		Service1 service = new Service1();
		Service1 serviceSpy = spy(service);
			
		String resMsg = null;
		String erMsg = null;
		boolean trown = false;
		try {
			 resMsg = serviceSpy.getCompanyHistoryPricesRange(req, Mockito.mock(Response.class));
		} catch (DateParsingException e) {
			trown = false;
		} catch (RangeException e) {
			trown = true;
			erMsg = e.getMessage();
		}
		
		assertTrue(trown);
		assertEquals("{\"errorMessage\":\"Range is zero\", \"tFrom\":\""+y1+"-"+m+"-"+d+"\", \"today\":\""+y1+"-"+m+"-"+d+"\"}", erMsg);
		assertNull(resMsg);
	}
	
	@Test
	public void testCompanyHistoryPricesRange_RangeException() {
		LocalDate date1 = LocalDate.now();
		String y1 = Integer.toString(date1.getYear());
		int M = date1.getMonthValue();
		String m1 = (M < 10 ? "0"+Integer.toString(M) : Integer.toString(M));
		String m = Integer.toString(M);
		int D = date1.getDayOfMonth();
		String d1 = (D < 10 ? "0"+Integer.toString(D) : Integer.toString(D));
		String d = Integer.toString(D);
		
		LocalDate date2 = date1.plusDays(30);
		String y2 = Integer.toString(date2.getYear());
		M = date2.getMonthValue();
		String m2 = (M < 10 ? "0"+Integer.toString(M) : Integer.toString(M));
		String m_ = Integer.toString(M);
		D = date2.getDayOfMonth();
		String d2 = (D < 10 ? "0"+Integer.toString(D) : Integer.toString(D));
		String d_ = Integer.toString(D);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(Const.PATH_COMPANY, "aapl");
		params.put(Const.PATH_TIMEBEGIN, y2+m2+d2);
		params.put(Const.PATH_TIMEEND, y1+m1+d1);
		
		Request req = Mockito.mock(Request.class);
		when(req.params()).thenReturn(params);
		when(req.pathInfo()).thenReturn("/iex/company/aapl/"+y2+m2+d2+"/"+y1+m1+d1);
		Service1 service = new Service1();
		Service1 serviceSpy = spy(service);
			
		String resMsg = null;
		String erMsg = null;
		boolean trown = false;
		try {
			 resMsg = serviceSpy.getCompanyHistoryPricesRange(req, Mockito.mock(Response.class));
		} catch (DateParsingException e) {
			trown = false;
		} catch (RangeException e) {
			trown = true;
			erMsg = e.getMessage();
		}
		
		assertTrue(trown);
		assertEquals("{\"errorMessage\":\"Range is incorect\", \"tFrom\":\""+y2+"-"+m_+"-"+d_+"\", \"tTo\":\""+y1+"-"+m+"-"+d+"\"}", erMsg);
		assertNull(resMsg);
	}
}
