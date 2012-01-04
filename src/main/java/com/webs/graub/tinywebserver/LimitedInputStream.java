package com.webs.graub.tinywebserver;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream where the stream size is limited to certain extent.
 * The stream will count the exact bytes read and when the limit has
 * been exceeded, EOF is given. LimitedInputStream is used as
 * FilterInputStream, by giving the source InputStream as argument
 * in constructor. <p>
 * 
 * The purpose of this InputStream is to make reading HTTP content
 * easier - the limit comes from HTTP header which defines the content
 * size, and application just needs to read this stream until EOF
 * (even if the underlying stream would have more data) <p>
 * 
 * @author Hannes R.
 *
 */
public class LimitedInputStream extends FilterInputStream {

	int mLimit;

	public LimitedInputStream(InputStream in, int limit) {
		super(in);
		this.mLimit = limit;
	}

	@Override
	public int read() throws IOException {
		if (mLimit<=0) return -1;
		mLimit--;
		return in.read();
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (mLimit<=0) return -1;
		int toRead = Math.min(len, mLimit);
		toRead = in.read(b, off, toRead);
		mLimit = (toRead<0 ? toRead : mLimit-toRead);
		return toRead;
	}

	@Override
	public int available() {
		return mLimit;
	}


}
