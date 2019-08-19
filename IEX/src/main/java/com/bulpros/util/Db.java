package com.bulpros.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The purpose of this class is to serve as temporary Database.
 * And for future work as an abstraction layer for accessing db.
 * 
 * @author igor
 *
 */
public class Db {
	private static List<JsonNode> list = new ArrayList<JsonNode>();
	
	public static void addNode(JsonNode jn) {
		list.add(jn);
	}
	
	public static List<JsonNode> getList() {
		return list;
	}
	
	public static Iterator<JsonNode> getIterator() {
		return list.iterator();
	}
	
	public static void deleteList() {
		list.clear();
	}
}
