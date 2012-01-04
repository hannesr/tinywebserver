package com.webs.graub.tinywebserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

class Session extends Thread {

	// constants and enums
	static final String errorTemplate = "<html><head><title>Error</title></head>"
			+ "<body><h1>resultLine</h1></body></html>";

	// members
	Library mLibrary;
	Socket mSocket;

	Session(Library library, Socket socket) {
		this.mLibrary = library;
		this.mSocket = socket;
	}

	public void run() {
		try {
			BufferedInputStream in = new BufferedInputStream(mSocket
					.getInputStream());
			HttpRequest request = null;
			do {
				request = new HttpRequest();
				request.readRequestLine(in);
				request.readHeaders(in);
				if (request.getStatus() != null) {
					errorResponse(request, request.getStatus());
				} else if (request.getCommand().equals(Http.GET)
						|| request.getCommand().equals(Http.HEAD)) {
					doGet(request);
				} else if (request.getCommand().equals(Http.POST)) {
					doPost(request);
				} else if (request.getCommand().equals(Http.PUT)
						|| request.getCommand().equals(Http.DELETE)
						|| request.getCommand().equals(Http.OPTIONS)
						|| request.getCommand().equals(Http.TRACE)) {
					errorResponse(request, new HttpStatus(HttpStatus.NOT_IMPLEMENTED));
				} else {
					errorResponse(request, new HttpStatus(HttpStatus.BAD_REQUEST));
				}
			} while (!request.shouldCloseConnection());

			close();

		} catch (Exception ex) {
			// should not be here.
			ex.printStackTrace();
			close();
		}
	}

	private void doGet(HttpRequest request) throws IOException {

		// content = object that will provide the message body
		OutContent content = findContentOrSendError(request, OutContent.class);
		if (content==null) return;
		// TODO: handle requests with If-Modified-Since: or If-Unmodified-Since

		// create headers
		// TODO: configurable headers?
		HashMap<String, String> headers = new HashMap<String, String>();
		setResponseHeaders(headers);
		headers.put(Http.CONTENT_TYPE, content.getMimetype());
		int size = content.getDataSize();
		if (size <= 0 && request.getVersionMode()==Http.Version.HTTP1_1) {
			headers.put(Http.TRANSFER_ENCODING, Http.CHUNKED);
		} else if (size <= 0) {
			// HTTP 1.0 does not support chunked encoding!!!
			errorResponse(request, new HttpStatus(HttpStatus.INTERNAL_SERVER_ERROR));
			return;
		} else {
			headers.put(Http.CONTENT_LENGTH, Integer.toString(size));
		}

		// write headers to output stream
		BufferedOutputStream out = new BufferedOutputStream(mSocket
				.getOutputStream());
		HttpStatus ok = new HttpStatus(HttpStatus.OK);
		Util.writeLine(out, request.getVersionString() + " " + ok.toString());
		Util.writeHeaders(out, headers);
		out.flush();

		// if this was a HEAD request, exit at this point
		if (request.getCommand().equals(Http.HEAD)) {
			return;
		}

		// write content
		if (size > 0) {
			LimitedOutputStream limiter = new LimitedOutputStream(out, size);
			content.out(limiter);
			limiter.complete();
			limiter.flush();
		} else {
			ChunkedEncodingOutputStream chunker = new ChunkedEncodingOutputStream(out);
			content.out(chunker);
			chunker.complete();
			chunker.flush();
		}

		out.flush();

	}

	private void doPost(HttpRequest request) throws IOException {

		// content = object that will provide the message body
		InContent content = findContentOrSendError(request, InContent.class);
		if (content==null) return;

		// TODO: 100 continue

		errorResponse(request, new HttpStatus(HttpStatus.NOT_IMPLEMENTED));
	}

	private <T> T findContentOrSendError(HttpRequest request, Class<T> clz) throws IOException {
		T content = null;
		try {
			content = clz.cast(mLibrary.getContent(request.getUri()));
		} catch (HttpStatus ex) {
			errorResponse(request, ex);
			return null;
		} catch (ClassCastException ex) {
			errorResponse(request, new HttpStatus(HttpStatus.METHOD_NOT_ALLOWED));
			return null;
		}
		if (content == null) {
			errorResponse(request, new HttpStatus(HttpStatus.NOT_FOUND));
			return null;
		}
		return content;
	}

	private void errorResponse(HttpRequest request, HttpStatus status)
			throws IOException {
		// create headers
		HashMap<String, String> headers = new HashMap<String, String>();
		setResponseHeaders(headers);
		if (request.shouldCloseConnection()) {
			headers.put("Connection", "close");
		}

		// write to stream
		BufferedOutputStream out = new BufferedOutputStream(mSocket
				.getOutputStream());
		String resp = Http.versionString(request.getVersionMode()) + " " + status.toString();
		Util.writeLine(out, resp);
		Util.writeHeaders(out, headers);

		// write the error content to stream
		String errorContent = errorTemplate.replace("resultLine", status
				.toString());
		Util.writeLine(out, errorContent);

		Util.writeLine(out, "");
		out.flush();
	}

	private void setResponseHeaders(HashMap<String, String> headers) {
		headers.put("Server", "TinyWebServer (graub.webs.com)");
		headers.put("Date", getServerTime());
	}

	private String getServerTime() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		TimeZone tz = TimeZone.getTimeZone("GMT");
		dateFormat.setTimeZone(tz);
		return dateFormat.format(calendar.getTime());
	}

	private void close() {
		try {
			mSocket.close();
		} catch (IOException e) {
			// do nothing
		}
	}

}
