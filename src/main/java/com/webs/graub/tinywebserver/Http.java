package com.webs.graub.tinywebserver;

/**
 * A collection of useful constants
 * @author Hannes R.
 */
public class Http {

	enum Version {HTTP1_0, HTTP1_1};

	// HTTP messages
	public static final String GET = "GET";
	public static final String HEAD = "HEAD";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String DELETE = "DELETE";
	public static final String OPTIONS = "OPTIONS";
	public static final String TRACE = "TRACE";

	// known HTTP headers
	// TODO: The header name is not case-sensitive (though the value may be)
	static final String CONTENT_LENGTH = "Content-Length";
	static final String CONTENT_TYPE = "Content-Type";
	static final String TRANSFER_ENCODING = "Transfer-Encoding";

	// HTTP header known values
	static final String CHUNKED = "chunked";

	// HTTP version strings
	public static String versionString(Version v) {
		if (v==Version.HTTP1_0) return "HTTP/1.0";
		else if (v==Version.HTTP1_1) return "HTTP/1.1";
		else return "";
	}

	// Line end bytes
	static final byte[] CRLF = "\r\n".getBytes();




}
