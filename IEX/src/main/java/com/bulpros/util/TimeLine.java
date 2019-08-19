package com.bulpros.util;

public class TimeLine {
	public int year;
	public int month;
	public int day;

	/**
	 * Checking if <i>this</i> TimeLine represents date that is before t2.
	 * Return <i>true</i> if it does. <i>false</i> otherwise.
	 * 
	 * @param t2
	 * @return boolean
	 */
	public boolean before(TimeLine t2) {
		TimeLine t1 = this;
		if (t1.year < t2.year) {
			return true;
		} else if (t1.year == t2.year && t1.month < t2.month) {
			return true;
		} else if (t1.year == t2.year && t1.month == t2.month && t1.day < t2.day) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checking if <i>this</i> TimeLine represents date that is before or equal to t2.
	 * Return <i>true</i> if it does. <i>false</i> otherwise.
	 * 
	 * @param t2
	 * @return boolean
	 */
	public boolean beforeEqual(TimeLine t2) {
		TimeLine t1 = this;
		if (t1.year < t2.year) {
			return true;
		} else if (t1.year == t2.year && t1.month < t2.month) {
			return true;
		} else if (t1.year == t2.year && t1.month == t2.month && t1.day < t2.day) {
			return true;
		} else if (t1.year == t2.year && t1.month == t2.month && t1.day == t2.day) {
			return true;
		}
		return false;
	}

	/**
	 * This method parses String date. Format of date is YYYYMMDD.
	 * Returns <i>true</i> if parsing is done ok. <i>false</i> otherwise.
	 * 
	 * @param date
	 * @return boolean
	 */
	public boolean parse(String date) {
		try {
			year = Integer.parseInt(date.substring(0, 4));
			month = Integer.parseInt(date.substring(4, 6));
			day = Integer.parseInt(date.substring(6, 8));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * This method parses String date. Format of date is YYYY-MM-DD.
	 * Returns <i>true</i> if parsing is done ok. <i>false</i> otherwise.
	 * 
	 * @param date
	 * @return boolean
	 */
	public boolean parseFromSource(String date) {
		try {
			year = Integer.parseInt(date.substring(0, 4));
			month = Integer.parseInt(date.substring(5, 7));
			day = Integer.parseInt(date.substring(8, 10));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public String toString() {
		return year +"-" +month + "-"+ day;
	}
}