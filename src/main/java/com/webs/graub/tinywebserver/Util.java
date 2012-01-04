package com.webs.graub.tinywebserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Small utility for reading and writing HTTP to/from streams
 * @author Hannes R.
 */
public class Util {

	private static final int LINEFBUF_SIZE = 512;
	public static boolean readHeaders(InputStream in, HashMap<String,String> headers) throws IOException {
		// read incoming headers until empty line
		// TODO: [ ] Header lines beginning with space or tab are actually part of the
		//           previous header line, folded into multiple lines for easy reading 
		while (true) {
			String line = readLine(in);
			if (line.trim().length() == 0)
				break;
			int colon = line.indexOf(":");
			if (colon<0) return false;
			String k = line.substring(0, colon).trim();
			String v = line.substring(colon+1).trim();
			if (k.length()==0 || v.length()==0) return false;
			headers.put(k, v);
		}

		return true;
	}

	public static String readLine(InputStream in) throws IOException {
		byte[] linebuf = new byte[LINEFBUF_SIZE];
		int i=0;
		while(true) {
			int b = in.read();
			if (b == -1) break;
			if (b == '\n') break;
			if (i<LINEFBUF_SIZE) linebuf[i++] = (byte)b;
		}
		return new String(linebuf,0,i);
	}

	public static InputStream startReadingContent(InputStream in, HashMap<String,String> headers) {
		String encoding = headers.get(Http.TRANSFER_ENCODING);
		String length = headers.get(Http.CONTENT_LENGTH);
		if (encoding != null && encoding.equals(Http.CHUNKED)) {
			return new ChunkedEncodingInputStream(in);
		} else if (length != null) {
			try {
				return new LimitedInputStream(in, Integer.parseInt(headers.get(Http.CONTENT_LENGTH)));
			} catch (NumberFormatException ex) {
				// Content-length seems to be corrupted. Read all content until EOF.
				return in; 
			}
		} else {
			// Not chunked, and no length given. Read all content until EOF.
			return in;
		}
	}

	public static byte[] readContent(InputStream in, HashMap<String,String> headers) throws IOException {
		InputStream inDecoded = startReadingContent(in, headers);
		ByteArrayOutputStream bufBuilder = new ByteArrayOutputStream();
		while(true) {
			int b = inDecoded.read();
			if (b<0) break;
			bufBuilder.write(b);
		}
		return bufBuilder.toByteArray();
	}

	public static void writeHeaders(OutputStream out,
			HashMap<String, String> headers) throws IOException {
		for (Map.Entry<String, String> e : headers.entrySet()) {
			writeLine(out, e.getKey() + ": " + e.getValue());
		}
		writeLine(out, ""); // empty line marks end of header section
		out.flush();
	}

	public static void writeLine(OutputStream out, String line) throws IOException {
		out.write(line.getBytes());
		out.write(Http.CRLF);
	}

}
