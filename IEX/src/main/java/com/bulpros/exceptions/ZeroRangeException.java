package com.bulpros.exceptions;

public class ZeroRangeException extends RangeException {
	private static final long serialVersionUID = -8977405873261800371L;
	
	public ZeroRangeException(String message) {
		super(message);
	}
}
