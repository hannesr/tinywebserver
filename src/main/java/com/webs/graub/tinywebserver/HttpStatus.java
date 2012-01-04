package com.webs.graub.tinywebserver;

/**
 * A class that represents HTTP Status
 * (see http://en.wikipedia.org/wiki/List_of_HTTP_status_codes)
 * For comfy reasons this object can be used either as a RuntimeException
 * or as a normal storage object.
 */
public class HttpStatus extends RuntimeException {

	// most commonly used status codes
	public static final String CONTINUE = "100";
	public static final String OK = "200";
	public static final String BAD_REQUEST = "400";
	public static final String FORBIDDEN = "403";
	public static final String NOT_FOUND = "404";
	public static final String METHOD_NOT_ALLOWED = "405";
	public static final String LENGTH_REQUIRED = "411";
	public static final String INTERNAL_SERVER_ERROR = "500";
	public static final String NOT_IMPLEMENTED = "501";
	public static final String HTTP_VERSION_NOT_SUPPORTED = "505";

	public static final String SUBRANGE_SEPARATOR = ".";

	public static String message(String code) {
		if 		(code.equals(CONTINUE)) return "Continue";
		else if (code.equals(OK)) return "OK";
		else if (code.equals(FORBIDDEN)) return "Forbidden";
		else if (code.equals(BAD_REQUEST)) return "Bad Request";
		else if (code.equals(NOT_FOUND)) return "Not Found";
		else if (code.equals(METHOD_NOT_ALLOWED)) return "Method Not Allowed";
		else if (code.equals(LENGTH_REQUIRED)) return "Length Required";
		else if (code.equals(INTERNAL_SERVER_ERROR)) return "Internal Server Error";
		else if (code.equals(NOT_IMPLEMENTED)) return "Not Implemented";
		else if (code.equals(HTTP_VERSION_NOT_SUPPORTED)) return "HTTP Version Not Supported";
		else {
			// cut off the sub-status specifier, if such exists
			if (code.contains(SUBRANGE_SEPARATOR))
				code = code.substring(0, code.indexOf(SUBRANGE_SEPARATOR));
			// check range
			int c = Integer.parseInt(code);
			if 		(c>=100 && c<=199) return "Informational";
			else if (c>=200 && c<=299) return "Success";
			else if (c>=300 && c<=399) return "Redirection";
			else if (c>=400 && c<=499) return "Client Error";
			else if (c>=500 && c<=599) return "Server Error";
			else throw new RuntimeException("Unrecognised HTTP status code: "+code);
		}
	}

	// members
	private static final long serialVersionUID = -5756041809584820369L;
	String mCode;
	String mMessage;

	public HttpStatus(String code) {
		this(code, message(code));
	}

	public HttpStatus(String code, String message) {
		this.mCode = code;
		this.mMessage = message;
	}

	public String getCode() {
		return mCode;
	}

	public int getStatusCode() {
		if (mCode.contains(SUBRANGE_SEPARATOR))
			return Integer.parseInt(mCode.substring(0, mCode.indexOf(SUBRANGE_SEPARATOR)));
		else
			return Integer.parseInt(mCode);
	}

	public boolean isSevereError() {
		int c = getStatusCode();
		return (c==400 || c>=500);
	}

	public String getMessage() {
		return mMessage;
	}

	@Override
	public String toString() {
		return mCode + " " + mMessage;
	}
}
