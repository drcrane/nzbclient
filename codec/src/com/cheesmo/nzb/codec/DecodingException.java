package com.cheesmo.nzb.codec;

public class DecodingException extends Exception {
	private static final long serialVersionUID = -8090790126251548610L;

	String message = null;
	
	public DecodingException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	
	

}
