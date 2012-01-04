package com.webs.graub.tinywebserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents a HTTP request that 
 * @author rompphan
 *
 */
class HttpRequest {

	private String mCommand;
	private URI mUri;
	private String mVersion;
	private Http.Version mVersionMode;
	private HashMap<String,String> mHeaders;
	private HttpStatus mStatus;

	HttpRequest() {
		mCommand = null;
		mUri = URI.create("/");
		mVersion = "";
		mVersionMode = Http.Version.HTTP1_1;
		mHeaders = new HashMap<String,String>();
		mStatus = null;
	}

	void readRequestLine(InputStream in) throws IOException {
		// read and set request line parameters
		String requestline[] = Util.readLine(in).split("[\\s]+", 3);
		if (requestline.length==3) {
			mCommand = recogniseCommand(requestline[0]);
			mUri = recogniseUri(requestline[1]);
			mVersion = requestline[2];
			mVersionMode = recogniseVersionMode(requestline[2]);
		} else {
			setStatus(new HttpStatus(HttpStatus.BAD_REQUEST));
		}

	}


	private String recogniseCommand(String cmd) {
		if (cmd.equals(Http.GET) || cmd.equals(Http.HEAD) ||
			cmd.equals(Http.POST) || cmd.equals(Http.PUT) ||
			cmd.equals(Http.DELETE)|| cmd.equals(Http.OPTIONS)||
			cmd.equals(Http.TRACE)) {
			return cmd;
		} else {
			setStatus(new HttpStatus(HttpStatus.BAD_REQUEST));
			return "";
		}
	}

	private URI recogniseUri(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			mStatus = new HttpStatus(HttpStatus.BAD_REQUEST);
			return URI.create("/");
		}
	}

	private Http.Version recogniseVersionMode(String versionString) {
		int p=versionString.indexOf("/");
		String protocolPart = versionString.substring(0, p).trim();
		if (!protocolPart.equals("HTTP")) {
			mStatus = new HttpStatus(HttpStatus.BAD_REQUEST);
			return Http.Version.HTTP1_0;
		}
		String versionPart = versionString.substring(p+1).trim();
		if (versionPart.equals("1.0")) {
			return Http.Version.HTTP1_0;
		} else if (versionPart.equals("1.1")) {
			return Http.Version.HTTP1_1;
		} else if (versionPart.substring(0,2).equals("1.")) {
			// Client version is HTTP/1.X -> serve it as HTTP/1.1
			return Http.Version.HTTP1_1;
		} else {
			mStatus = new HttpStatus(HttpStatus.BAD_REQUEST);
			return Http.Version.HTTP1_0;
		}
	}


	void readHeaders(InputStream in) throws IOException {
		// read header section
		if (!Util.readHeaders(in, mHeaders)) {
			setStatus(new HttpStatus(HttpStatus.BAD_REQUEST));
		}

		// validate headers
		validate();
	}

	private void validate() {
		if (mStatus != null)
			return; // already bad

		if (getVersionMode()==Http.Version.HTTP1_1 && !mHeaders.containsKey("Host"))
			setStatus(new HttpStatus(HttpStatus.BAD_REQUEST));

		// add more validation here if needed
	}

	
	String getCommand() {
		return mCommand;
	}
	
	URI getUri() {
		return mUri;
	}

	String getVersionString() {
		return mVersion;
	}

	Http.Version getVersionMode() {
		return mVersionMode;
	}

	Map<String,String> getHeaders() {
		return mHeaders;
	}

	HttpStatus getStatus() {
		return mStatus;
	}
	
	private void setStatus(HttpStatus status) {
		// can not double-set
		if (mStatus == null)
			mStatus = status;
	}

	boolean shouldCloseConnection() {
		// if there is a severe error, force close
		if (mStatus!=null)
			if (mStatus.isSevereError())
				return true;
		// for HTTP 1.0 connection always closed
		// for unknown versions, connection closed for safety reasons.
		if (mVersionMode != Http.Version.HTTP1_1)
			return true;
		// if "Connection: close" header sent, connection will be closed.
		if (mHeaders.containsKey("Connection"))
			if (mHeaders.get("Connection").toLowerCase().equals("close"))
				return true;
		// otherwise connection can be kept alive
		return false;
	}

}
