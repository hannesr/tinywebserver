package com.webs.graub.tinywebserver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Random;

public class MyLibrary implements Library {

	public static String INDEX = "<html>\n<head>\n<title>Index page</title>\n</head>\n" +
			"<body>\n<h1>Index page</h1>\n<ul>\n" +
			"<li> <a href=\"/normal\">Normal text</a>\n" +
			"<li> <a href=\"/chunked\">Chunked text</a>\n" +
			"<li> <a href=\"/png\">Chunked PNG image</a>\n" +
			"<li> <a href=\"/forbidden\">Forbidden page</a>\n" +
			"<li> <a href=\"/nonexistent\">Page that does not exist</a>\n" +
			"</ul></body></html>\n";
	public static String TEXT_DATA = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

	public static byte[] BYTE_DATA = new byte[60000];
	static {
		// fill data array with randomness
		new Random().nextBytes(BYTE_DATA);
	}

	class StringContent implements OutContent {
		String mMime;
		String mContent;
		boolean mChunked;
		StringContent(String mime, String text, boolean chunked) {
			this.mMime = mime;
			this.mContent = text;
			this.mChunked = chunked;
		}
		@Override
		public int getDataSize() { return mChunked ? 0 : mContent.getBytes().length; }
		@Override
		public String getMimetype() { return mMime; }
		@Override
		public void out(OutputStream stream) throws IOException {
			OutputStreamWriter writer = new OutputStreamWriter(stream);
			writer.write(mContent);
			writer.flush();
		}
	}

	class DataContent implements OutContent {
		String mMime;
		byte[] mContent;
		boolean mChunked;
		DataContent(String mime, byte[] data) {
			this.mMime = mime;
			this.mContent = data;
		}
		@Override
		public int getDataSize() { return 0; }
		@Override
		public String getMimetype() { return mMime; }
		@Override
		public void out(OutputStream stream) throws IOException {
			stream.write(mContent);
			stream.flush();
		}
	}


	class FileContent implements OutContent {
		String mMime;
		String mUrl;
		FileContent(String mime, String url) {
			this.mMime = mime;
			this.mUrl = url;
		}
		@Override
		public int getDataSize() { return 0; }
		@Override
		public String getMimetype() { return mMime; }
		@Override
		public void out(OutputStream stream) throws IOException {
			InputStream in = new BufferedInputStream(getClass().getResourceAsStream(mUrl));
			while(true) {
				int b = in.read();
				if (b<0) break;
				stream.write(b);
			}
			stream.flush();
		}
	}

	@Override
	public Content getContent(URI uri) throws HttpStatus {
		String path = uri.getPath();
		if (path.equals("/")) {
			return new StringContent("text/html; charset=iso-8859-1", INDEX, false);
		} else if (path.equals("/normal")) {
			return new StringContent("text/plain", TEXT_DATA, false);
		} else if (path.equals("/chunked")) {
			return new StringContent("text/plain", TEXT_DATA, true);
		} else if (path.equals("/png")) {
			return new FileContent("image/png", "test.png");
		} else if (path.equals("/object")) {
			return new DataContent("application/java-object", BYTE_DATA);
		} else if (path.equals("/forbidden")) {
			throw new HttpStatus(HttpStatus.FORBIDDEN);
		} else {
			return null;
		}
	}

}
